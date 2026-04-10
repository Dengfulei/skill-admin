package com.codex.skilladmin.apply;

import com.codex.skilladmin.common.BusinessException;
import com.codex.skilladmin.permission.ResourcePermissionRepository;
import com.codex.skilladmin.resource.ResourceEntity;
import com.codex.skilladmin.resource.ResourceRepository;
import com.codex.skilladmin.resource.ResourceService;
import com.codex.skilladmin.resource.ResourceStatus;
import com.codex.skilladmin.resource.ScopeLevel;
import com.codex.skilladmin.security.AuthenticatedUser;
import com.codex.skilladmin.user.UserDepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessRequestServiceTest {

    @Mock
    private AccessRequestRepository accessRequestRepository;
    @Mock
    private ResourceService resourceService;
    @Mock
    private ResourceRepository resourceRepository;
    @Mock
    private ResourcePermissionRepository resourcePermissionRepository;
    @Mock
    private UserDepartmentRepository userDepartmentRepository;

    private AccessRequestService accessRequestService;

    @BeforeEach
    void setUp() {
        accessRequestService = new AccessRequestService(
                accessRequestRepository,
                resourceService,
                resourceRepository,
                resourcePermissionRepository,
                userDepartmentRepository
        );
    }

    @Test
    void submit_rejectsDisabledDepartmentResource() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .id(8L)
                .username("sales_user")
                .displayName("Sales User")
                .systemAdmin(false)
                .departmentIds(Set.of(2L))
                .departmentAdminIds(Set.of())
                .build();
        ResourceEntity resource = departmentResource(100L, ResourceStatus.DISABLED, false, true);

        when(resourceService.findResource(100L)).thenReturn(resource);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> accessRequestService.submit(new SubmitApplicationRequest(100L, "need access"), user)
        );

        assertEquals("当前资源未启用，暂不可申请", exception.getMessage());
    }

    @Test
    void submit_rejectsSystemAdmin() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .id(1L)
                .username("admin")
                .displayName("System Admin")
                .systemAdmin(true)
                .departmentIds(Set.of(2L))
                .departmentAdminIds(Set.of())
                .build();
        ResourceEntity resource = departmentResource(100L, ResourceStatus.ACTIVE, true, true);

        when(resourceService.findResource(100L)).thenReturn(resource);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> accessRequestService.submit(new SubmitApplicationRequest(100L, "need access"), user)
        );

        assertEquals("系统管理员不通过部门技能申请链路使用资源", exception.getMessage());
    }

    @Test
    void submit_rejectsDepartmentAdminForOwnDepartmentResource() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .id(2L)
                .username("sales_admin")
                .displayName("Sales Admin")
                .systemAdmin(false)
                .departmentIds(Set.of(2L))
                .departmentAdminIds(Set.of(2L))
                .build();
        ResourceEntity resource = departmentResource(100L, ResourceStatus.ACTIVE, true, true);

        when(resourceService.findResource(100L)).thenReturn(resource);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> accessRequestService.submit(new SubmitApplicationRequest(100L, "need access"), user)
        );

        assertEquals("部门管理员不通过部门技能申请链路使用本部门资源", exception.getMessage());
    }

    @Test
    void review_rejectsWhenResourceStateChangedAfterSubmission() {
        AuthenticatedUser reviewer = AuthenticatedUser.builder()
                .id(9L)
                .username("sales_admin")
                .displayName("Sales Admin")
                .systemAdmin(false)
                .departmentIds(Set.of(2L))
                .departmentAdminIds(Set.of(2L))
                .build();

        AccessRequestEntity application = new AccessRequestEntity();
        application.setId(5L);
        application.setResourceId(100L);
        application.setApplicantUserId(8L);
        application.setDepartmentId(2L);
        application.setStatus(AccessRequestStatus.PENDING);
        application.setDeleted(false);

        ResourceEntity resource = departmentResource(100L, ResourceStatus.ACTIVE, false, false);

        when(accessRequestRepository.findByIdAndDeletedFalse(5L)).thenReturn(Optional.of(application));
        when(resourceService.findResource(100L)).thenReturn(resource);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> accessRequestService.review(5L, new ReviewApplicationRequest(true, "approved"), reviewer)
        );

        assertEquals("当前资源状态已变化，无法继续审批该申请", exception.getMessage());
    }

    private ResourceEntity departmentResource(Long id, ResourceStatus status, boolean enabled, boolean approvalRequired) {
        ResourceEntity entity = new ResourceEntity();
        entity.setId(id);
        entity.setName("dept-resource");
        entity.setCode("dept-resource");
        entity.setScopeLevel(ScopeLevel.DEPARTMENT);
        entity.setOwnerDepartmentId(2L);
        entity.setStatus(status);
        entity.setEnabled(enabled);
        entity.setApprovalRequired(approvalRequired);
        entity.setDeleted(false);
        return entity;
    }
}
