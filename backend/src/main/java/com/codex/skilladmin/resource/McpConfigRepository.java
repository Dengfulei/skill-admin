package com.codex.skilladmin.resource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface McpConfigRepository extends JpaRepository<McpConfigEntity, Long> {

    @Modifying
    @Query(value = "DELETE FROM mcp_config WHERE resource_id = ?1", nativeQuery = true)
    void hardDeleteByResourceId(Long resourceId);
}
