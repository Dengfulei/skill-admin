package com.codex.skilladmin.resource;

import java.util.List;

public record ResourcePageResponse(
        List<ResourceSummaryResponse> records,
        long total,
        int pageNum,
        int pageSize,
        ResourceListStatsResponse stats
) {
}
