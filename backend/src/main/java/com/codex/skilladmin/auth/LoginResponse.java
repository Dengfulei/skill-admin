package com.codex.skilladmin.auth;

import com.codex.skilladmin.security.AuthenticatedUser;

public record LoginResponse(
        String token,
        AuthenticatedUser user
) {
}
