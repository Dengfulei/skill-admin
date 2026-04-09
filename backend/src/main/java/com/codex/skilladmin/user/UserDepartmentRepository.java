package com.codex.skilladmin.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserDepartmentRepository extends JpaRepository<UserDepartmentEntity, Long> {

    List<UserDepartmentEntity> findAllByUserIdAndDeletedFalse(Long userId);

    boolean existsByUserIdAndDepartmentIdAndDeletedFalse(Long userId, Long departmentId);

    boolean existsByUserIdAndDepartmentIdAndRoleCodeAndDeletedFalse(Long userId, Long departmentId, UserDepartmentRole roleCode);

    List<UserDepartmentEntity> findAllByDepartmentIdInAndDeletedFalse(Collection<Long> departmentIds);
}
