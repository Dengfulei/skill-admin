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
@Table(name = "mcp_config")
public class McpConfigEntity {

    @Id
    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "server_name", nullable = false, length = 100)
    private String serverName;

    @Column(name = "transport_type", nullable = false, length = 20)
    private String transportType;

    @Column(name = "command_line", length = 500)
    private String commandLine;

    @Column(name = "args_json", columnDefinition = "json")
    private String argsJson;

    @Column(name = "env_json", columnDefinition = "json")
    private String envJson;

    @Column(name = "endpoint_url", length = 255)
    private String endpointUrl;

    @Column(name = "headers_json", columnDefinition = "json")
    private String headersJson;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
