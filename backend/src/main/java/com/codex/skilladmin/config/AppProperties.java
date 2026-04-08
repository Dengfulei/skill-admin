package com.codex.skilladmin.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class AppProperties {

    private static final String DEFAULT_SECRET = "codex-skill-admin-demo-secret-codex-skill-admin-demo";
    private static final long DEFAULT_EXPIRATION_HOURS = 12L;

    @NotBlank
    private String secret = DEFAULT_SECRET;

    @Positive
    private long expirationHours = DEFAULT_EXPIRATION_HOURS;
}
