INSERT INTO sys_department (name, code, status, deleted)
SELECT '技术部', 'TECH', 1, 0
WHERE NOT EXISTS (
    SELECT 1 FROM sys_department WHERE code = 'TECH'
);

INSERT INTO sys_user (username, display_name, email, password, is_system_admin, status, deleted)
SELECT 'tech_admin', '技术部管理员', 'tech_admin@example.com', '123456', 0, 1, 0
WHERE NOT EXISTS (
    SELECT 1 FROM sys_user WHERE username = 'tech_admin'
);

INSERT INTO sys_user (username, display_name, email, password, is_system_admin, status, deleted)
SELECT 'dengfulei', '邓福磊', 'dengfulei@example.com', '123456', 0, 1, 0
WHERE NOT EXISTS (
    SELECT 1 FROM sys_user WHERE username = 'dengfulei'
);

UPDATE sys_user_department
SET is_primary = 0
WHERE deleted = 0
  AND user_id = (SELECT id FROM sys_user WHERE username = 'tech_admin');

UPDATE sys_user_department
SET is_primary = 0
WHERE deleted = 0
  AND user_id = (SELECT id FROM sys_user WHERE username = 'dengfulei');

INSERT INTO sys_user_department (user_id, department_id, role_code, is_primary, deleted)
SELECT u.id, d.id, 'DEPT_ADMIN', 1, 0
FROM sys_user u
JOIN sys_department d ON d.code = 'TECH'
WHERE u.username = 'tech_admin'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_user_department ud
      WHERE ud.user_id = u.id
        AND ud.department_id = d.id
  );

INSERT INTO sys_user_department (user_id, department_id, role_code, is_primary, deleted)
SELECT u.id, d.id, 'MEMBER', 1, 0
FROM sys_user u
JOIN sys_department d ON d.code = 'TECH'
WHERE u.username = 'dengfulei'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_user_department ud
      WHERE ud.user_id = u.id
        AND ud.department_id = d.id
  );

UPDATE sys_user_department
SET role_code = 'DEPT_ADMIN',
    is_primary = 1,
    deleted = 0
WHERE user_id = (SELECT id FROM sys_user WHERE username = 'tech_admin')
  AND department_id = (SELECT id FROM sys_department WHERE code = 'TECH');

UPDATE sys_user_department
SET role_code = 'MEMBER',
    is_primary = 1,
    deleted = 0
WHERE user_id = (SELECT id FROM sys_user WHERE username = 'dengfulei')
  AND department_id = (SELECT id FROM sys_department WHERE code = 'TECH');
