package com.codex.skilladmin.resource;

public record ResourceSummaryResponse(
        Long id,
        String name,
        String code,
        ResourceType resourceType,
        ScopeLevel scopeLevel,
        ResourceStatus status,
        Boolean enabled,
        Boolean approvalRequired,
        Long ownerDepartmentId,
        Long ownerUserId,
        String description
) {
}
