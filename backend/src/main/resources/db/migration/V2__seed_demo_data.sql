INSERT INTO sys_department (id, name, code, status, deleted)
VALUES (1, '平台部', 'PLATFORM', 1, 0),
       (2, '销售部', 'SALES', 1, 0),
       (3, '人事部', 'HR', 1, 0);

INSERT INTO sys_user (id, username, display_name, email, password, is_system_admin, status, deleted)
VALUES (1, 'admin', '系统管理员', 'admin@example.com', '123456', 1, 1, 0),
       (2, 'sales_admin', '销售管理员', 'sales_admin@example.com', '123456', 0, 1, 0),
       (3, 'sales_user', '销售员工', 'sales_user@example.com', '123456', 0, 1, 0),
       (4, 'hr_user', '人事员工', 'hr_user@example.com', '123456', 0, 1, 0),
       (5, 'alice', '个人用户 Alice', 'alice@example.com', '123456', 0, 1, 0);

INSERT INTO sys_user_department (user_id, department_id, role_code, is_primary, deleted)
VALUES (2, 2, 'DEPT_ADMIN', 1, 0),
       (3, 2, 'MEMBER', 1, 0),
       (4, 3, 'MEMBER', 1, 0),
       (5, 1, 'MEMBER', 1, 0);

INSERT INTO skill_resource (id, resource_type, name, code, description, scope_level, owner_department_id, owner_user_id, status, enabled, approval_required, created_by, updated_by, deleted)
VALUES (1, 'SKILL', '通用知识库技能', 'public-knowledge-skill', '全员可用的公共知识库技能', 'PUBLIC', NULL, NULL, 'ACTIVE', 1, 0, 1, 1, 0),
       (2, 'MCP', '销售分析 MCP', 'sales-analytics-mcp', '销售部专属 MCP 工具，需申请后使用', 'DEPARTMENT', 2, NULL, 'ACTIVE', 1, 1, 2, 2, 0),
       (3, 'SKILL', 'Alice 私人工具箱', 'alice-private-skill', '仅 Alice 可见的个人技能', 'PERSONAL', NULL, 5, 'ACTIVE', 1, 0, 5, 5, 0);

INSERT INTO skill_config (resource_id, version, manifest_json, entrypoint, icon)
VALUES (1, '1.0.0', JSON_OBJECT('name', 'public-knowledge-skill', 'description', '公共知识库技能'), 'index.ts', 'Book'),
       (3, '1.0.0', JSON_OBJECT('name', 'alice-private-skill', 'description', 'Alice 的个人技能'), 'main.py', 'User');

INSERT INTO mcp_config (resource_id, server_name, transport_type, command_line, args_json, env_json, endpoint_url, headers_json)
VALUES (2, 'sales-analytics', 'STDIO', 'npx', JSON_ARRAY('@demo/sales-analytics-mcp'), JSON_OBJECT('ENV', 'demo'), NULL, NULL);

INSERT INTO resource_permission (resource_id, target_scope, department_id, user_id, permission_type, enabled, created_by, deleted)
VALUES (1, 'PUBLIC', NULL, NULL, 'USE', 1, 1, 0),
       (3, 'PERSONAL', NULL, 5, 'USE', 1, 5, 0);
