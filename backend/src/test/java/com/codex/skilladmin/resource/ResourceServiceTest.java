package com.codex.skilladmin.resource;

import com.codex.skilladmin.apply.AccessRequestRepository;
import com.codex.skilladmin.common.BusinessException;
import com.codex.skilladmin.permission.ResourcePermissionRepository;
import com.codex.skilladmin.security.AuthenticatedUser;
import com.codex.skilladmin.user.UserDepartmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;
    @Mock
    private AccessRequestRepository accessRequestRepository;
    @Mock
    private SkillConfigRepository skillConfigRepository;
    @Mock
    private McpConfigRepository mcpConfigRepository;
    @Mock
    private ResourcePermissionRepository resourcePermissionRepository;
    @Mock
    private UserDepartmentRepository userDepartmentRepository;

    private ResourceService resourceService;

    @BeforeEach
    void setUp() {
        resourceService = new ResourceService(
                resourceRepository,
                accessRequestRepository,
                skillConfigRepository,
                mcpConfigRepository,
                resourcePermissionRepository,
                userDepartmentRepository,
                new ObjectMapper()
        );
    }

    @Test
    void listManageableResources_excludesPersonalResourcesFromSharedManagementPage() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .id(10L)
                .username("tech_admin")
                .displayName("Tech Admin")
                .systemAdmin(false)
                .departmentIds(Set.of(2L))
                .departmentAdminIds(Set.of(2L))
                .build();

        ResourceEntity departmentResource = resource(1L, ScopeLevel.DEPARTMENT, 2L, null);
        ResourceEntity personalResource = resource(2L, ScopeLevel.PERSONAL, null, 10L);

        when(resourceRepository.findAllByDeletedFalseOrderByIdDesc()).thenReturn(List.of(personalResource, departmentResource));

        ResourcePageResponse page = resourceService.listManageableResources(user, null, null, 1, 10);

        assertEquals(1, page.records().size());
        assertEquals(1L, page.records().get(0).id());
        assertEquals(1L, page.stats().departmentCount());
        assertEquals(0L, page.stats().personalCount());
    }

    @Test
    void create_rejectsPersonalResourceAssignedToAnotherUser() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .id(10L)
                .username("alice")
                .displayName("Alice")
                .systemAdmin(true)
                .departmentIds(Set.of())
                .departmentAdminIds(Set.of())
                .build();

        ResourceUpsertRequest request = request(
                ScopeLevel.PERSONAL,
                null,
                99L
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> resourceService.create(request, user));
        assertEquals("个人资源只能由本人维护", exception.getMessage());
    }

    @Test
    void create_rejectsDepartmentResourceForSystemAdminWithoutDepartmentAdminRole() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .id(1L)
                .username("admin")
                .displayName("System Admin")
                .systemAdmin(true)
                .departmentIds(Set.of())
                .departmentAdminIds(Set.of())
                .build();

        ResourceUpsertRequest request = request(
                ScopeLevel.DEPARTMENT,
                2L,
                null
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> resourceService.create(request, user));
        assertEquals("只有部门管理员可以维护本部门资源", exception.getMessage());
    }

    @Test
    void update_rejectsChangingResourceScopeOrOwner() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .id(10L)
                .username("tech_admin")
                .displayName("Tech Admin")
                .systemAdmin(false)
                .departmentIds(Set.of(2L))
                .departmentAdminIds(Set.of(2L))
                .build();

        ResourceEntity existing = resource(1L, ScopeLevel.DEPARTMENT, 2L, null);

        when(resourceRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(existing));

        ResourceUpsertRequest request = request(
                ScopeLevel.PERSONAL,
                null,
                10L
        );

        BusinessException exception = assertThrows(BusinessException.class, () -> resourceService.update(1L, request, user));
        assertEquals("资源创建后不允许修改权限级别", exception.getMessage());
    }

    private ResourceEntity resource(Long id, ScopeLevel scopeLevel, Long departmentId, Long ownerUserId) {
        ResourceEntity entity = new ResourceEntity();
        entity.setId(id);
        entity.setName("resource-" + id);
        entity.setCode("resource-" + id);
        entity.setResourceType(ResourceType.SKILL);
        entity.setScopeLevel(scopeLevel);
        entity.setOwnerDepartmentId(departmentId);
        entity.setOwnerUserId(ownerUserId);
        entity.setStatus(ResourceStatus.ACTIVE);
        entity.setEnabled(true);
        entity.setApprovalRequired(false);
        entity.setCreatedBy(1L);
        entity.setUpdatedBy(1L);
        entity.setDeleted(false);
        return entity;
    }

    private ResourceUpsertRequest request(ScopeLevel scopeLevel, Long ownerDepartmentId, Long ownerUserId) {
        return new ResourceUpsertRequest(
                ResourceType.SKILL,
                "demo",
                "demo-code",
                "demo",
                scopeLevel,
                ownerDepartmentId,
                ownerUserId,
                ResourceStatus.ACTIVE,
                true,
                false,
                "1.0.0",
                "{}",
                "index.ts",
                "Book",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of()
        );
    }
}
