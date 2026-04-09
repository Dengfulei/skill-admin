package com.codex.skilladmin.resource;

import com.codex.skilladmin.common.BusinessException;
import com.codex.skilladmin.common.PageResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.codex.skilladmin.permission.PermissionType;
import com.codex.skilladmin.permission.ResourcePermissionEntity;
import com.codex.skilladmin.permission.ResourcePermissionRepository;
import com.codex.skilladmin.security.AuthenticatedUser;
import com.codex.skilladmin.user.UserDepartmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final SkillConfigRepository skillConfigRepository;
    private final McpConfigRepository mcpConfigRepository;
    private final ResourcePermissionRepository resourcePermissionRepository;
    private final UserDepartmentRepository userDepartmentRepository;
    private final ObjectMapper objectMapper;

    public ResourceService(
            ResourceRepository resourceRepository,
            SkillConfigRepository skillConfigRepository,
            McpConfigRepository mcpConfigRepository,
            ResourcePermissionRepository resourcePermissionRepository,
            UserDepartmentRepository userDepartmentRepository,
            ObjectMapper objectMapper
    ) {
        this.resourceRepository = resourceRepository;
        this.skillConfigRepository = skillConfigRepository;
        this.mcpConfigRepository = mcpConfigRepository;
        this.resourcePermissionRepository = resourcePermissionRepository;
        this.userDepartmentRepository = userDepartmentRepository;
        this.objectMapper = objectMapper;
    }

    public ResourcePageResponse listManageableResources(
            AuthenticatedUser user,
            String keyword,
            ResourceType resourceType,
            Integer pageNum,
            Integer pageSize
    ) {
        List<ResourceSummaryResponse> resources = resourceRepository.findAllByDeletedFalseOrderByIdDesc().stream()
                .filter(resource -> canManageResource(user, resource))
                .map(this::toSummary)
                .filter(resource -> matchesResourceType(resource, resourceType))
                .filter(resource -> matchesKeyword(resource, keyword))
                .toList();
        return buildResourcePage(resources, pageNum, pageSize);
    }

    public ResourceDetailResponse getDetail(Long id, AuthenticatedUser user) {
        ResourceEntity resource = findResource(id);
        if (!canManageResource(user, resource) && !canUseResource(user, resource)) {
            throw new BusinessException(403, "无权查看该资源");
        }
        return buildDetail(resource);
    }

    @Transactional
    public ResourceDetailResponse create(ResourceUpsertRequest request, AuthenticatedUser user) {
        ResourceEntity resource = new ResourceEntity();
        fillResource(resource, request, user);
        resource = resourceRepository.save(resource);
        saveDetailConfig(resource.getId(), request);
        replacePermissions(resource, request.permissions(), user);
        return buildDetail(resource);
    }

    @Transactional
    public ResourceDetailResponse update(Long id, ResourceUpsertRequest request, AuthenticatedUser user) {
        ResourceEntity resource = findResource(id);
        if (!canManageResource(user, resource)) {
            throw new BusinessException(403, "无权修改该资源");
        }
        fillResource(resource, request, user);
        resource = resourceRepository.save(resource);
        saveDetailConfig(resource.getId(), request);
        replacePermissions(resource, request.permissions(), user);
        return buildDetail(resource);
    }

    @Transactional
    public void toggleEnabled(Long id, boolean enabled, AuthenticatedUser user) {
        ResourceEntity resource = findResource(id);
        if (!canManageResource(user, resource)) {
            throw new BusinessException(403, "无权变更启用状态");
        }
        resource.setEnabled(enabled);
        resource.setStatus(enabled ? ResourceStatus.ACTIVE : ResourceStatus.DISABLED);
        resource.setUpdatedBy(user.getId());
        resourceRepository.save(resource);
    }

    @Transactional
    public void delete(Long id, AuthenticatedUser user) {
        ResourceEntity resource = findResource(id);
        if (!canManageResource(user, resource)) {
            throw new BusinessException(403, "无权删除该资源");
        }
        resource.setDeleted(true);
        resource.setUpdatedBy(user.getId());
        resourceRepository.save(resource);
    }

    public ResourcePageResponse listAvailableResources(
            AuthenticatedUser user,
            String keyword,
            ResourceType resourceType,
            Integer pageNum,
            Integer pageSize
    ) {
        List<ResourceSummaryResponse> resources = findAvailableResources(user).stream()
                .filter(resource -> matchesResourceType(resource, resourceType))
                .filter(resource -> matchesKeyword(resource, keyword))
                .toList();
        return buildResourcePage(resources, pageNum, pageSize);
    }

    public PageResponse<ResourceSummaryResponse> listDepartmentApplyCatalog(AuthenticatedUser user, Integer pageNum, Integer pageSize) {
        List<ResourceSummaryResponse> resources = findDepartmentApplyCatalog(user);
        return paginate(resources, pageNum, pageSize);
    }

    private List<ResourceSummaryResponse> findAvailableResources(AuthenticatedUser user) {
        Set<Long> resourceIds = new HashSet<>();
        resourceIds.addAll(resourcePermissionRepository
                .findAllByTargetScopeAndPermissionTypeAndEnabledTrueAndDeletedFalse(ScopeLevel.PUBLIC, PermissionType.USE)
                .stream()
                .map(ResourcePermissionEntity::getResourceId)
                .toList());
        if (!CollectionUtils.isEmpty(user.getDepartmentIds())) {
            resourceIds.addAll(resourcePermissionRepository
                    .findAllByTargetScopeAndDepartmentIdInAndPermissionTypeAndEnabledTrueAndDeletedFalse(
                            ScopeLevel.DEPARTMENT, user.getDepartmentIds(), PermissionType.USE)
                    .stream()
                    .map(ResourcePermissionEntity::getResourceId)
                    .toList());
        }
        resourceIds.addAll(resourcePermissionRepository
                .findAllByTargetScopeAndUserIdAndPermissionTypeAndEnabledTrueAndDeletedFalse(
                        ScopeLevel.PERSONAL, user.getId(), PermissionType.USE)
                .stream()
                .map(ResourcePermissionEntity::getResourceId)
                .toList());
        if (resourceIds.isEmpty()) {
            return List.of();
        }
        return resourceRepository.findAllByIdInAndDeletedFalseOrderByIdDesc(resourceIds).stream()
                .filter(resource -> resource.getEnabled() && resource.getStatus() == ResourceStatus.ACTIVE)
                .filter(resource -> canUseResource(user, resource))
                .map(this::toSummary)
                .toList();
    }

    private List<ResourceSummaryResponse> findDepartmentApplyCatalog(AuthenticatedUser user) {
        if (CollectionUtils.isEmpty(user.getDepartmentIds())) {
            return List.of();
        }
        Set<Long> alreadyAccessible = findAvailableResources(user).stream()
                .map(ResourceSummaryResponse::id)
                .collect(Collectors.toSet());
        return resourceRepository.findAllByOwnerDepartmentIdInAndDeletedFalse(user.getDepartmentIds()).stream()
                .filter(resource -> resource.getScopeLevel() == ScopeLevel.DEPARTMENT)
                .filter(resource -> Boolean.TRUE.equals(resource.getApprovalRequired()))
                .filter(resource -> !alreadyAccessible.contains(resource.getId()))
                .filter(resource -> resource.getEnabled() && resource.getStatus() == ResourceStatus.ACTIVE)
                .map(this::toSummary)
                .toList();
    }

    public boolean canUseResource(AuthenticatedUser user, String code) {
        ResourceEntity resource = resourceRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> new BusinessException(404, "资源不存在"));
        return canUseResource(user, resource);
    }

    public boolean canUseResource(AuthenticatedUser user, ResourceEntity resource) {
        if (!resource.getEnabled() || resource.getStatus() != ResourceStatus.ACTIVE) {
            return false;
        }
        if (resource.getScopeLevel() == ScopeLevel.PUBLIC) {
            return resourcePermissionRepository
                    .findAllByTargetScopeAndPermissionTypeAndEnabledTrueAndDeletedFalse(ScopeLevel.PUBLIC, PermissionType.USE)
                    .stream()
                    .anyMatch(permission -> permission.getResourceId().equals(resource.getId()));
        }
        if (resource.getScopeLevel() == ScopeLevel.PERSONAL) {
            return Objects.equals(resource.getOwnerUserId(), user.getId())
                    && resourcePermissionRepository.existsByResourceIdAndTargetScopeAndUserIdAndPermissionTypeAndEnabledTrueAndDeletedFalse(
                    resource.getId(), ScopeLevel.PERSONAL, user.getId(), PermissionType.USE);
        }
        if (!isDepartmentMember(user, resource.getOwnerDepartmentId())) {
            return false;
        }
        boolean hasDepartment = resourcePermissionRepository
                .findAllByTargetScopeAndDepartmentIdInAndPermissionTypeAndEnabledTrueAndDeletedFalse(
                        ScopeLevel.DEPARTMENT, user.getDepartmentIds(), PermissionType.USE)
                .stream()
                .anyMatch(permission -> permission.getResourceId().equals(resource.getId()));
        if (hasDepartment) {
            return true;
        }
        return resourcePermissionRepository.existsByResourceIdAndTargetScopeAndUserIdAndPermissionTypeAndEnabledTrueAndDeletedFalse(
                resource.getId(), ScopeLevel.PERSONAL, user.getId(), PermissionType.USE);
    }

    public boolean canManageResource(AuthenticatedUser user, ResourceEntity resource) {
        if (user.isSystemAdmin()) {
            return true;
        }
        if (resource.getScopeLevel() == ScopeLevel.PERSONAL) {
            return Objects.equals(resource.getOwnerUserId(), user.getId());
        }
        if (resource.getScopeLevel() == ScopeLevel.DEPARTMENT) {
            return resource.getOwnerDepartmentId() != null && user.getDepartmentAdminIds().contains(resource.getOwnerDepartmentId());
        }
        return false;
    }

    public ResourceEntity findResource(Long id) {
        return resourceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(404, "资源不存在"));
    }

    private void fillResource(ResourceEntity resource, ResourceUpsertRequest request, AuthenticatedUser user) {
        validateCreateOrUpdateRequest(request, user, resource.getId());
        resource.setResourceType(request.resourceType());
        resource.setName(request.name());
        resource.setCode(request.code());
        resource.setDescription(request.description());
        resource.setScopeLevel(request.scopeLevel());
        resource.setOwnerDepartmentId(request.scopeLevel() == ScopeLevel.DEPARTMENT ? request.ownerDepartmentId() : null);
        resource.setOwnerUserId(request.scopeLevel() == ScopeLevel.PERSONAL ? Optional.ofNullable(request.ownerUserId()).orElse(user.getId()) : null);
        resource.setStatus(request.status());
        resource.setEnabled(Optional.ofNullable(request.enabled()).orElse(true));
        resource.setApprovalRequired(request.scopeLevel() == ScopeLevel.DEPARTMENT && Optional.ofNullable(request.approvalRequired()).orElse(false));
        if (resource.getCreatedBy() == null) {
            resource.setCreatedBy(user.getId());
        }
        resource.setUpdatedBy(user.getId());
    }

    private void validateCreateOrUpdateRequest(ResourceUpsertRequest request, AuthenticatedUser user, Long id) {
        if (request.scopeLevel() == ScopeLevel.PUBLIC && !user.isSystemAdmin()) {
            throw new BusinessException(403, "只有系统管理员可以维护公共资源");
        }
        if (request.scopeLevel() == ScopeLevel.DEPARTMENT) {
            if (request.ownerDepartmentId() == null) {
                throw new BusinessException("部门级资源必须绑定所属部门");
            }
            if (!user.isSystemAdmin() && !user.getDepartmentAdminIds().contains(request.ownerDepartmentId())) {
                throw new BusinessException(403, "只有部门管理员可以维护本部门资源");
            }
        }
        if (request.scopeLevel() == ScopeLevel.PERSONAL) {
            Long ownerUserId = Optional.ofNullable(request.ownerUserId()).orElse(user.getId());
            if (!ownerUserId.equals(user.getId()) && !user.isSystemAdmin()) {
                throw new BusinessException(403, "个人资源只能由本人或管理员维护");
            }
        }
        if (request.scopeLevel() != ScopeLevel.DEPARTMENT && Boolean.TRUE.equals(request.approvalRequired())) {
            throw new BusinessException("仅部门级资源支持申请审批模式");
        }
        validatePermissionAssignments(request, user);
        resourceRepository.findByCodeAndDeletedFalse(request.code()).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), id)) {
                throw new BusinessException("资源编码已存在");
            }
        });
    }

    private void saveDetailConfig(Long resourceId, ResourceUpsertRequest request) {
        if (request.resourceType() == ResourceType.SKILL) {
            McpConfigEntity mcpConfig = mcpConfigRepository.findById(resourceId).orElse(null);
            if (mcpConfig != null) {
                mcpConfigRepository.delete(mcpConfig);
            }
            SkillConfigEntity skillConfig = skillConfigRepository.findById(resourceId).orElseGet(SkillConfigEntity::new);
            skillConfig.setResourceId(resourceId);
            skillConfig.setVersion(request.version());
            skillConfig.setManifestJson(normalizeJson(request.manifestJson(), JsonKind.OBJECT, "{}"));
            skillConfig.setEntrypoint(request.entrypoint());
            skillConfig.setIcon(request.icon());
            skillConfigRepository.save(skillConfig);
            return;
        }
        SkillConfigEntity skillConfig = skillConfigRepository.findById(resourceId).orElse(null);
        if (skillConfig != null) {
            skillConfigRepository.delete(skillConfig);
        }
        McpConfigEntity mcpConfig = mcpConfigRepository.findById(resourceId).orElseGet(McpConfigEntity::new);
        mcpConfig.setResourceId(resourceId);
        mcpConfig.setServerName(Optional.ofNullable(request.serverName()).orElse(""));
        mcpConfig.setTransportType(Optional.ofNullable(request.transportType()).orElse("STDIO"));
        mcpConfig.setCommandLine(normalizeText(request.commandLine()));
        mcpConfig.setArgsJson(normalizeJson(request.argsJson(), JsonKind.ARRAY, null));
        mcpConfig.setEnvJson(normalizeJson(request.envJson(), JsonKind.OBJECT, null));
        mcpConfig.setEndpointUrl(normalizeText(request.endpointUrl()));
        mcpConfig.setHeadersJson(normalizeJson(request.headersJson(), JsonKind.OBJECT, null));
        mcpConfigRepository.save(mcpConfig);
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String normalizeJson(String value, JsonKind kind, String defaultValue) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        String trimmed = value.trim();
        try {
            JsonNode node = objectMapper.readTree(trimmed);
            if (kind == JsonKind.ARRAY && !node.isArray()) {
                throw new BusinessException(400, "JSON 配置格式不正确，需要传入 JSON 数组");
            }
            if (kind == JsonKind.OBJECT && !node.isObject()) {
                throw new BusinessException(400, "JSON 配置格式不正确，需要传入 JSON 对象");
            }
            return trimmed;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            String label = kind == JsonKind.ARRAY ? "JSON 数组" : "JSON 对象";
            throw new BusinessException(400, "JSON 配置格式不正确，请输入合法的" + label);
        }
    }

    private enum JsonKind {
        ARRAY,
        OBJECT
    }

    private void replacePermissions(ResourceEntity resource, List<PermissionAssignmentRequest> assignments, AuthenticatedUser user) {
        List<ResourcePermissionEntity> existing = resourcePermissionRepository.findAllByResourceIdAndDeletedFalse(resource.getId());
        if (resource.getScopeLevel() == ScopeLevel.DEPARTMENT && Boolean.TRUE.equals(resource.getApprovalRequired())) {
            syncApprovalRequiredPermissions(existing);
            return;
        }
        existing.forEach(permission -> {
            permission.setDeleted(true);
            permission.setEnabled(false);
        });
        resourcePermissionRepository.saveAll(existing);

        List<PermissionAssignmentRequest> finalAssignments = assignments;
        if (CollectionUtils.isEmpty(assignments)) {
            finalAssignments = buildDefaultAssignments(resource, user);
        }
        List<ResourcePermissionEntity> permissions = finalAssignments.stream()
                .map(assignment -> {
                    ResourcePermissionEntity permission = new ResourcePermissionEntity();
                    permission.setResourceId(resource.getId());
                    permission.setTargetScope(assignment.targetScope());
                    permission.setDepartmentId(assignment.departmentId());
                    permission.setUserId(assignment.userId());
                    permission.setPermissionType(PermissionType.USE);
                    permission.setEnabled(true);
                    permission.setCreatedBy(user.getId());
                    permission.setDeleted(false);
                    return permission;
                })
                .toList();
        resourcePermissionRepository.saveAll(permissions);
    }

    private void syncApprovalRequiredPermissions(List<ResourcePermissionEntity> existing) {
        List<ResourcePermissionEntity> changedPermissions = existing.stream()
                .filter(permission -> permission.getTargetScope() != ScopeLevel.PERSONAL)
                .peek(permission -> {
                    permission.setDeleted(true);
                    permission.setEnabled(false);
                })
                .toList();
        if (!changedPermissions.isEmpty()) {
            resourcePermissionRepository.saveAll(changedPermissions);
        }
    }

    private void validatePermissionAssignments(ResourceUpsertRequest request, AuthenticatedUser user) {
        if (request.scopeLevel() == ScopeLevel.DEPARTMENT && Boolean.TRUE.equals(request.approvalRequired())) {
            if (!CollectionUtils.isEmpty(request.permissions())) {
                throw new BusinessException("开启申请后，部门级资源不能预置任何可用权限");
            }
            return;
        }
        if (CollectionUtils.isEmpty(request.permissions())) {
            return;
        }
        Long ownerDepartmentId = request.ownerDepartmentId();
        Long ownerUserId = Optional.ofNullable(request.ownerUserId()).orElse(user.getId());
        for (PermissionAssignmentRequest assignment : request.permissions()) {
            validateAssignmentShape(assignment);
            if (request.scopeLevel() == ScopeLevel.PUBLIC) {
                if (assignment.targetScope() != ScopeLevel.PUBLIC) {
                    throw new BusinessException("公共级资源只能授权给全员");
                }
                continue;
            }
            if (request.scopeLevel() == ScopeLevel.PERSONAL) {
                if (assignment.targetScope() != ScopeLevel.PERSONAL || !Objects.equals(assignment.userId(), ownerUserId)) {
                    throw new BusinessException("个人级资源只能授权给资源本人");
                }
                continue;
            }
            if (assignment.targetScope() == ScopeLevel.PUBLIC) {
                throw new BusinessException("部门级资源不能授权为公共可用");
            }
            if (assignment.targetScope() == ScopeLevel.DEPARTMENT
                    && !Objects.equals(assignment.departmentId(), ownerDepartmentId)) {
                throw new BusinessException("部门级资源只能授权给所属部门");
            }
            if (assignment.targetScope() == ScopeLevel.PERSONAL
                    && !userDepartmentRepository.existsByUserIdAndDepartmentIdAndDeletedFalse(assignment.userId(), ownerDepartmentId)) {
                throw new BusinessException("部门级资源只能授权给本部门成员");
            }
        }
    }

    private void validateAssignmentShape(PermissionAssignmentRequest assignment) {
        if (assignment.targetScope() == ScopeLevel.PUBLIC) {
            if (assignment.departmentId() != null || assignment.userId() != null) {
                throw new BusinessException("公共授权不能指定部门或个人");
            }
            return;
        }
        if (assignment.targetScope() == ScopeLevel.DEPARTMENT) {
            if (assignment.departmentId() == null || assignment.userId() != null) {
                throw new BusinessException("部门授权必须指定部门，且不能指定个人");
            }
            return;
        }
        if (assignment.userId() == null || assignment.departmentId() != null) {
            throw new BusinessException("个人授权必须指定个人，且不能指定部门");
        }
    }

    private boolean isDepartmentMember(AuthenticatedUser user, Long departmentId) {
        return departmentId != null && !CollectionUtils.isEmpty(user.getDepartmentIds()) && user.getDepartmentIds().contains(departmentId);
    }

    private List<PermissionAssignmentRequest> buildDefaultAssignments(ResourceEntity resource, AuthenticatedUser user) {
        if (resource.getScopeLevel() == ScopeLevel.PUBLIC) {
            return List.of(new PermissionAssignmentRequest(ScopeLevel.PUBLIC, null, null));
        }
        if (resource.getScopeLevel() == ScopeLevel.PERSONAL) {
            return List.of(new PermissionAssignmentRequest(ScopeLevel.PERSONAL, null, resource.getOwnerUserId()));
        }
        if (Boolean.TRUE.equals(resource.getApprovalRequired())) {
            return List.of();
        }
        return List.of(new PermissionAssignmentRequest(ScopeLevel.DEPARTMENT, resource.getOwnerDepartmentId(), null));
    }

    private ResourceDetailResponse buildDetail(ResourceEntity resource) {
        SkillConfigEntity skillConfig = skillConfigRepository.findById(resource.getId()).orElse(null);
        McpConfigEntity mcpConfig = mcpConfigRepository.findById(resource.getId()).orElse(null);
        List<ResourcePermissionView> permissions = resourcePermissionRepository.findAllByResourceIdAndDeletedFalse(resource.getId())
                .stream()
                .filter(item -> !item.getDeleted())
                .map(item -> new ResourcePermissionView(
                        item.getId(),
                        item.getTargetScope(),
                        item.getDepartmentId(),
                        item.getUserId(),
                        item.getPermissionType(),
                        item.getEnabled()
                ))
                .toList();
        return new ResourceDetailResponse(toSummary(resource), skillConfig, mcpConfig, permissions);
    }

    private ResourceSummaryResponse toSummary(ResourceEntity resource) {
        return new ResourceSummaryResponse(
                resource.getId(),
                resource.getName(),
                resource.getCode(),
                resource.getResourceType(),
                resource.getScopeLevel(),
                resource.getStatus(),
                resource.getEnabled(),
                resource.getApprovalRequired(),
                resource.getOwnerDepartmentId(),
                resource.getOwnerUserId(),
                resource.getDescription()
        );
    }

    private ResourcePageResponse buildResourcePage(List<ResourceSummaryResponse> resources, Integer pageNum, Integer pageSize) {
        PageResponse<ResourceSummaryResponse> page = paginate(resources, pageNum, pageSize);
        return new ResourcePageResponse(
                page.records(),
                page.total(),
                page.pageNum(),
                page.pageSize(),
                buildStats(resources)
        );
    }

    private ResourceListStatsResponse buildStats(List<ResourceSummaryResponse> resources) {
        return new ResourceListStatsResponse(
                resources.size(),
                resources.stream().filter(item -> item.resourceType() == ResourceType.SKILL).count(),
                resources.stream().filter(item -> item.resourceType() == ResourceType.MCP).count(),
                resources.stream().filter(item -> item.scopeLevel() == ScopeLevel.PUBLIC).count(),
                resources.stream().filter(item -> item.scopeLevel() == ScopeLevel.DEPARTMENT).count(),
                resources.stream().filter(item -> item.scopeLevel() == ScopeLevel.PERSONAL).count()
        );
    }

    private <T> PageResponse<T> paginate(List<T> items, Integer pageNum, Integer pageSize) {
        int safePageNum = normalizePageNum(pageNum);
        int safePageSize = normalizePageSize(pageSize);
        int fromIndex = Math.min((safePageNum - 1) * safePageSize, items.size());
        int toIndex = Math.min(fromIndex + safePageSize, items.size());
        return new PageResponse<>(items.subList(fromIndex, toIndex), items.size(), safePageNum, safePageSize);
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    private boolean matchesKeyword(ResourceSummaryResponse resource, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
        return containsIgnoreCase(resource.name(), normalizedKeyword) || containsIgnoreCase(resource.code(), normalizedKeyword);
    }

    private boolean matchesResourceType(ResourceSummaryResponse resource, ResourceType resourceType) {
        return resourceType == null || resource.resourceType() == resourceType;
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
