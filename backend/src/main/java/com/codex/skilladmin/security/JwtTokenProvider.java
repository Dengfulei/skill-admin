package com.codex.skilladmin.security;

import com.codex.skilladmin.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final AppProperties appProperties;

    public JwtTokenProvider(AppProperties appProperties) {
        this.appProperties = appProperties;
        this.secretKey = Keys.hmacShaKeyFor(appProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(AuthenticatedUser user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(appProperties.getExpirationHours(), ChronoUnit.HOURS);
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claims(Map.of(
                        "username", user.getUsername(),
                        "displayName", user.getDisplayName(),
                        "systemAdmin", user.isSystemAdmin(),
                        "departmentIds", user.getDepartmentIds(),
                        "departmentAdminIds", user.getDepartmentAdminIds()
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    public AuthenticatedUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return AuthenticatedUser.builder()
                .id(Long.valueOf(claims.getSubject()))
                .username(claims.get("username", String.class))
                .displayName(claims.get("displayName", String.class))
                .systemAdmin(Boolean.TRUE.equals(claims.get("systemAdmin", Boolean.class)))
                .departmentIds(toLongSet(claims.get("departmentIds")))
                .departmentAdminIds(toLongSet(claims.get("departmentAdminIds")))
                .build();
    }

    private Set<Long> toLongSet(Object value) {
        if (!(value instanceof Collection<?> collection)) {
            return Set.of();
        }
        return collection.stream()
                .map(item -> Long.valueOf(String.valueOf(item)))
                .collect(Collectors.toSet());
    }
}
