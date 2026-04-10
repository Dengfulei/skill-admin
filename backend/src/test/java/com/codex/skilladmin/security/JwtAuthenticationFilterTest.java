package com.codex.skilladmin.security;

import com.codex.skilladmin.auth.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthService authService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loadsFreshUserStateFromDatabaseInsteadOfTrustingJwtClaims() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenProvider, authService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-value");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AuthenticatedUser refreshedUser = AuthenticatedUser.builder()
                .id(7L)
                .username("tech_admin")
                .displayName("Tech Admin")
                .systemAdmin(false)
                .departmentIds(Set.of(2L))
                .departmentAdminIds(Set.of(2L))
                .build();

        when(jwtTokenProvider.parseUserId("token-value")).thenReturn(7L);
        when(authService.currentUser(7L)).thenReturn(refreshedUser);

        filter.doFilter(request, response, (req, res) -> {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            assertNotNull(principal);
            assertEquals(refreshedUser, principal);
        });

        verify(jwtTokenProvider).parseUserId("token-value");
        verify(authService).currentUser(7L);
    }
}
