package com.codex.skilladmin.apply;

import jakarta.validation.constraints.NotNull;

public record ReviewApplicationRequest(
        @NotNull Boolean approved,
        String reviewComment
) {
}
