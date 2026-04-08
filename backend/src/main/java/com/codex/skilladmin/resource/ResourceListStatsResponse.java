package com.codex.skilladmin.resource;

public record ResourceListStatsResponse(
        long total,
        long skillCount,
        long mcpCount,
        long publicCount,
        long departmentCount,
        long personalCount
) {
}
