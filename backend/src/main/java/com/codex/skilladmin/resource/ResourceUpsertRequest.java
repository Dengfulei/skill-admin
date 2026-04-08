package com.codex.skilladmin.resource;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ResourceUpsertRequest(
        @NotNull ResourceType resourceType,
        @NotBlank String name,
        @NotBlank String code,
        String description,
        @NotNull ScopeLevel scopeLevel,
        Long ownerDepartmentId,
        Long ownerUserId,
        @NotNull ResourceStatus status,
        Boolean enabled,
        Boolean approvalRequired,
        String version,
        String manifestJson,
        String entrypoint,
        String icon,
        String serverName,
        String transportType,
        String commandLine,
        String argsJson,
        String envJson,
        String endpointUrl,
        String headersJson,
        @Valid List<PermissionAssignmentRequest> permissions
) {
}
