package com.codex.skilladmin.resource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SkillConfigRepository extends JpaRepository<SkillConfigEntity, Long> {

    @Modifying
    @Query(value = "DELETE FROM skill_config WHERE resource_id = ?1", nativeQuery = true)
    void hardDeleteByResourceId(Long resourceId);
}
