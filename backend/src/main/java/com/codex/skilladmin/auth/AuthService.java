package com.codex.skilladmin.auth;

import com.codex.skilladmin.common.BusinessException;
import com.codex.skilladmin.security.AuthenticatedUser;
import com.codex.skilladmin.security.JwtTokenProvider;
import com.codex.skilladmin.user.UserDepartmentEntity;
import com.codex.skilladmin.user.UserDepartmentRepository;
import com.codex.skilladmin.user.UserDepartmentRole;
import com.codex.skilladmin.user.UserEntity;
import com.codex.skilladmin.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserDepartmentRepository userDepartmentRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            UserRepository userRepository,
            UserDepartmentRepository userDepartmentRepository,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.userRepository = userRepository;
        this.userDepartmentRepository = userDepartmentRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByUsernameAndDeletedFalseAndStatusTrue(request.username())
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));
        if (!user.getPassword().equals(request.password())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        AuthenticatedUser authenticatedUser = buildAuthenticatedUser(user);
        return new LoginResponse(jwtTokenProvider.createToken(authenticatedUser), authenticatedUser);
    }

    public AuthenticatedUser currentUser(Long userId) {
        UserEntity user = userRepository.findByIdAndDeletedFalseAndStatusTrue(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return buildAuthenticatedUser(user);
    }

    private AuthenticatedUser buildAuthenticatedUser(UserEntity user) {
        Set<UserDepartmentEntity> memberships = Set.copyOf(userDepartmentRepository.findAllByUserIdAndDeletedFalse(user.getId()));
        Set<Long> departmentIds = memberships.stream()
                .map(UserDepartmentEntity::getDepartmentId)
                .collect(Collectors.toSet());
        Set<Long> departmentAdminIds = memberships.stream()
                .filter(item -> item.getRoleCode() == UserDepartmentRole.DEPT_ADMIN)
                .map(UserDepartmentEntity::getDepartmentId)
                .collect(Collectors.toSet());
        return AuthenticatedUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .systemAdmin(Boolean.TRUE.equals(user.getSystemAdmin()))
                .departmentIds(departmentIds)
                .departmentAdminIds(departmentAdminIds)
                .build();
    }
}
