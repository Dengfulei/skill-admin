package com.codex.skilladmin.permission;

import com.codex.skilladmin.resource.ScopeLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ResourcePermissionRepository extends JpaRepository<ResourcePermissionEntity, Long> {

    void deleteAllByResourceId(Long resourceId);

    List<ResourcePermissionEntity> findAllByResourceIdAndDeletedFalse(Long resourceId);

    List<ResourcePermissionEntity> findAllByTargetScopeAndPermissionTypeAndEnabledTrueAndDeletedFalse(
            ScopeLevel targetScope, PermissionType permissionType);

    List<ResourcePermissionEntity> findAllByTargetScopeAndDepartmentIdInAndPermissionTypeAndEnabledTrueAndDeletedFalse(
            ScopeLevel targetScope, Collection<Long> departmentIds, PermissionType permissionType);

    List<ResourcePermissionEntity> findAllByTargetScopeAndUserIdAndPermissionTypeAndEnabledTrueAndDeletedFalse(
            ScopeLevel targetScope, Long userId, PermissionType permissionType);

    boolean existsByResourceIdAndTargetScopeAndUserIdAndPermissionTypeAndEnabledTrueAndDeletedFalse(
            Long resourceId, ScopeLevel targetScope, Long userId, PermissionType permissionType);

    @Modifying
    @Query(value = "DELETE FROM resource_permission WHERE resource_id = ?1", nativeQuery = true)
    void hardDeleteAllByResourceId(Long resourceId);
}
