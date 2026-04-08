package com.codex.skilladmin.common;

import java.util.List;

public record PageResponse<T>(
        List<T> records,
        long total,
        int pageNum,
        int pageSize
) {
}
