package com.codex.skilladmin.apply;

import com.codex.skilladmin.common.BusinessException;
import com.codex.skilladmin.permission.PermissionType;
import com.codex.skilladmin.permission.ResourcePermissionEntity;
import com.codex.skilladmin.permission.ResourcePermissionRepository;
import com.codex.skilladmin.resource.ResourceEntity;
import com.codex.skilladmin.resource.ResourceRepository;
import com.codex.skilladmin.resource.ResourceService;
import com.codex.skilladmin.resource.ScopeLevel;
import com.codex.skilladmin.security.AuthenticatedUser;
import com.codex.skilladmin.user.UserDepartmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccessRequestService {

    private final AccessRequestRepository accessRequestRepository;
    private final ResourceService resourceService;
    private final ResourceRepository resourceRepository;
    private final ResourcePermissionRepository resourcePermissionRepository;
    private final UserDepartmentRepository userDepartmentRepository;

    public AccessRequestService(
            AccessRequestRepository accessRequestRepository,
            ResourceService resourceService,
            ResourceRepository resourceRepository,
            ResourcePermissionRepository resourcePermissionRepository,
            UserDepartmentRepository userDepartmentRepository
    ) {
        this.accessRequestRepository = accessRequestRepository;
        this.resourceService = resourceService;
        this.resourceRepository = resourceRepository;
        this.resourcePermissionRepository = resourcePermissionRepository;
        this.userDepartmentRepository = userDepartmentRepository;
    }

    @Transactional
    public AccessRequestResponse submit(SubmitApplicationRequest request, AuthenticatedUser user) {
        ResourceEntity resource = resourceService.findResource(request.resourceId());
        if (user.isSystemAdmin()) {
            throw new BusinessException(403, "系统管理员不通过部门技能申请链路使用资源");
        }
        if (resource.getScopeLevel() != ScopeLevel.DEPARTMENT) {
            throw new BusinessException("只有部门级资源支持申请");
        }
        if (!Boolean.TRUE.equals(resource.getApprovalRequired())) {
            throw new BusinessException("该资源无需申请，可由管理员直接分配");
        }
        if (user.getDepartmentAdminIds().contains(resource.getOwnerDepartmentId())) {
            throw new BusinessException(403, "部门管理员不通过部门技能申请链路使用本部门资源");
        }
        if (resource.getOwnerDepartmentId() == null || CollectionUtils.isEmpty(user.getDepartmentIds())
                || !user.getDepartmentIds().contains(resource.getOwnerDepartmentId())) {
            throw new BusinessException(403, "仅本部门成员可申请该资源");
        }
        if (!resource.getEnabled() || resource.getStatus() != com.codex.skilladmin.resource.ResourceStatus.ACTIVE) {
            throw new BusinessException("当前资源未启用，暂不可申请");
        }
        if (resourceService.canUseResource(user, resource.getCode())) {
            throw new BusinessException("当前用户已拥有该资源权限");
        }
        accessRequestRepository.findByResourceIdAndApplicantUserIdAndStatusAndDeletedFalse(
                resource.getId(), user.getId(), AccessRequestStatus.PENDING
        ).ifPresent(item -> {
            throw new BusinessException("请勿重复提交申请");
        });

        AccessRequestEntity entity = new AccessRequestEntity();
        entity.setResourceId(resource.getId());
        entity.setApplicantUserId(user.getId());
        entity.setDepartmentId(resource.getOwnerDepartmentId());
        entity.setReason(request.reason());
        entity.setStatus(AccessRequestStatus.PENDING);
        accessRequestRepository.save(entity);
        return toResponse(entity, resource.getName());
    }

    public List<AccessRequestResponse> listMine(AuthenticatedUser user) {
        return accessRequestRepository.findAllByApplicantUserIdAndDeletedFalseOrderByIdDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AccessRequestResponse> listReviewable(AuthenticatedUser user) {
        if (CollectionUtils.isEmpty(user.getDepartmentAdminIds())) {
            return List.of();
        }
        List<Long> departmentIds = user.getDepartmentAdminIds().stream().toList();

        return accessRequestRepository.findAllByDepartmentIdInAndDeletedFalseOrderByIdDesc(departmentIds).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AccessRequestResponse review(Long id, ReviewApplicationRequest request, AuthenticatedUser user) {
        AccessRequestEntity entity = accessRequestRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException(404, "申请记录不存在"));
        ResourceEntity resource = resourceService.findResource(entity.getResourceId());
        if (!user.getDepartmentAdminIds().contains(entity.getDepartmentId())) {
            throw new BusinessException(403, "无权审批该申请");
        }
        if (entity.getStatus() != AccessRequestStatus.PENDING) {
            throw new BusinessException("该申请已经处理过");
        }
        if (resource.getScopeLevel() != ScopeLevel.DEPARTMENT
                || !Boolean.TRUE.equals(resource.getApprovalRequired())
                || !resource.getEnabled()
                || resource.getStatus() != com.codex.skilladmin.resource.ResourceStatus.ACTIVE
                || !entity.getDepartmentId().equals(resource.getOwnerDepartmentId())) {
            throw new BusinessException("当前资源状态已变化，无法继续审批该申请");
        }
        entity.setStatus(Boolean.TRUE.equals(request.approved()) ? AccessRequestStatus.APPROVED : AccessRequestStatus.REJECTED);
        entity.setReviewedBy(user.getId());
        entity.setReviewedAt(LocalDateTime.now());
        entity.setReviewComment(request.reviewComment());
        accessRequestRepository.save(entity);

        if (entity.getStatus() == AccessRequestStatus.APPROVED) {
            if (!userDepartmentRepository.existsByUserIdAndDepartmentIdAndDeletedFalse(entity.getApplicantUserId(), entity.getDepartmentId())) {
                throw new BusinessException("申请人已不在该部门，不能继续授权");
            }
            ResourcePermissionEntity permission = new ResourcePermissionEntity();
            permission.setResourceId(entity.getResourceId());
            permission.setTargetScope(ScopeLevel.PERSONAL);
            permission.setUserId(entity.getApplicantUserId());
            permission.setPermissionType(PermissionType.USE);
            permission.setEnabled(true);
            permission.setCreatedBy(user.getId());
            permission.setDeleted(false);
            resourcePermissionRepository.save(permission);
        }
        return toResponse(entity, resource.getName());
    }

    private AccessRequestResponse toResponse(AccessRequestEntity entity) {
        String resourceName = resourceRepository.findByIdAndDeletedFalse(entity.getResourceId())
                .map(ResourceEntity::getName)
                .orElse("-");
        return toResponse(entity, resourceName);
    }

    private AccessRequestResponse toResponse(AccessRequestEntity entity, String resourceName) {
        return new AccessRequestResponse(
                entity.getId(),
                entity.getResourceId(),
                resourceName,
                entity.getApplicantUserId(),
                entity.getDepartmentId(),
                entity.getReason(),
                entity.getStatus(),
                entity.getReviewComment()
        );
    }
}
