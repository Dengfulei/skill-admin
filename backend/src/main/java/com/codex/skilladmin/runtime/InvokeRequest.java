package com.codex.skilladmin.runtime;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record InvokeRequest(
        @NotBlank String resourceCode,
        Map<String, Object> payload
) {
}
