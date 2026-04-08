package com.codex.skilladmin.user;

import com.codex.skilladmin.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sys_user_department")
public class UserDepartmentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_code", nullable = false, length = 32)
    private UserDepartmentRole roleCode = UserDepartmentRole.MEMBER;

    @Column(name = "is_primary", nullable = false)
    private Boolean primary = false;
}
