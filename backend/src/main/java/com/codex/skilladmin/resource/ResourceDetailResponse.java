package com.codex.skilladmin.resource;

import java.util.List;

public record ResourceDetailResponse(
        ResourceSummaryResponse resource,
        SkillConfigEntity skillConfig,
        McpConfigEntity mcpConfig,
        List<ResourcePermissionView> permissions
) {
}
