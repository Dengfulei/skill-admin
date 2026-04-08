package com.codex.skilladmin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.jwt")
public class AppProperties {

    private String secret;
    private long expirationHours;
}
