package com.codex.skilladmin.resource;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "skill_config")
public class SkillConfigEntity {

    @Id
    @Column(name = "resource_id")
    private Long resourceId;

    @Column(length = 50)
    private String version;

    @Column(name = "manifest_json", columnDefinition = "json", nullable = false)
    private String manifestJson;

    @Column(length = 255)
    private String entrypoint;

    @Column(length = 255)
    private String icon;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
