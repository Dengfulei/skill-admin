package com.codex.skilladmin.auth;

import com.codex.skilladmin.common.ApiResponse;
import com.codex.skilladmin.security.AuthenticatedUser;
import com.codex.skilladmin.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<AuthenticatedUser> me(@CurrentUser AuthenticatedUser user) {
        return ApiResponse.success(authService.currentUser(user.getId()));
    }
}
