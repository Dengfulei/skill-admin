package com.codex.skilladmin.user;

import com.codex.skilladmin.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sys_user")
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(length = 120)
    private String email;

    @Column(nullable = false, length = 128)
    private String password;

    @Column(name = "is_system_admin", nullable = false)
    private Boolean systemAdmin = false;

    @Column(nullable = false)
    private Boolean status = true;
}
