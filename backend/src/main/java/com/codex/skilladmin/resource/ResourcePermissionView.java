package com.codex.skilladmin.resource;

import com.codex.skilladmin.permission.PermissionType;

public record ResourcePermissionView(
        Long id,
        ScopeLevel targetScope,
        Long departmentId,
        Long userId,
        PermissionType permissionType,
        Boolean enabled
) {
}
