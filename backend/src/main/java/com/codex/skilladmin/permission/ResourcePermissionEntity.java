package com.codex.skilladmin.permission;

import com.codex.skilladmin.common.BaseEntity;
import com.codex.skilladmin.resource.ScopeLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "resource_permission")
public class ResourcePermissionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_scope", nullable = false, length = 20)
    private ScopeLevel targetScope;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false, length = 20)
    private PermissionType permissionType;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;
}
