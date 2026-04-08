package com.codex.skilladmin.resource;

import jakarta.validation.constraints.NotNull;

public record PermissionAssignmentRequest(
        @NotNull ScopeLevel targetScope,
        Long departmentId,
        Long userId
) {
}
