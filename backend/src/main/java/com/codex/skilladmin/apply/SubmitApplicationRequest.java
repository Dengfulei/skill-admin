package com.codex.skilladmin.apply;

import jakarta.validation.constraints.NotNull;

public record SubmitApplicationRequest(
        @NotNull Long resourceId,
        String reason
) {
}
