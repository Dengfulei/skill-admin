package com.codex.skilladmin.security;

import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Value
@Builder
public class AuthenticatedUser {

    Long id;
    String username;
    String displayName;
    boolean systemAdmin;
    Set<Long> departmentIds;
    Set<Long> departmentAdminIds;
}
