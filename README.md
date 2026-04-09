# Skill Admin

基于 `Spring Boot 3 + MySQL 8.0 + Vue3 + Element Plus` 的 Skill / MCP 配置数据库化与三级权限管控演示系统。

## 项目结构

```text
skill-admin/
├── backend/   Spring Boot 3 后端
├── frontend/  Vue3 + Element Plus 前端
└── docs/      设计说明、AI 使用记录、演示脚本
```

## 功能范围

- Skill / MCP 统一资源管理
- 公共级 / 部门级 / 个人级三级权限控制
- 资源 CRUD、启用 / 禁用、按三级分配
- 当前用户可用资源列表
- 部门级资源申请与审批
- AI 工具运行时鉴权与友好提示

## 后端启动

1. 创建数据库：

```sql
CREATE DATABASE skill_admin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 配置环境变量或直接修改 [backend/src/main/resources/application.yml](/Users/dengfulei/Desktop/codex-ai/skill-admin/backend/src/main/resources/application.yml)

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`

3. 启动后端：

```bash
cd backend
mvn spring-boot:run
```

Flyway 会自动执行 MySQL 8.0 建表与初始化脚本。

## 前端启动

```bash
cd frontend
npm install
npm run dev
```

默认代理到 `http://localhost:8080`。

## 演示账号

- `admin / 123456`：系统管理员
- `sales_admin / 123456`：销售部管理员
- `tech_admin / 123456`：技术部管理员
- `sales_user / 123456`：销售部普通员工
- `hr_user / 123456`：人事部普通员工
- `dengfulei / 123456`：技术部员工
- `alice / 123456`：个人用户

## 关键页面

- 管理端：
  - `/admin/resources`
  - `/admin/applications`
- 用户端：
  - `/user/resources`
  - `/user/applications`

## 文档

- [数据库设计文档](/Users/dengfulei/Desktop/codex-ai/skill-admin/docs/schema-design.md)
- [接口与权限说明](/Users/dengfulei/Desktop/codex-ai/skill-admin/docs/api-overview.md)
- [AI 调用工具权限验证测试记录](/Users/dengfulei/Desktop/codex-ai/skill-admin/docs/ai-runtime-permission-verification.md)
- [演示脚本](/Users/dengfulei/Desktop/codex-ai/skill-admin/docs/demo-script.md)
- [AI 辅助编程说明](/Users/dengfulei/Desktop/codex-ai/skill-admin/docs/ai-usage.md)
- [AI 过程记录台账](/Users/dengfulei/Desktop/codex-ai/skill-admin/docs/ai-process-record.md)
- [交付检查表](/Users/dengfulei/Desktop/codex-ai/skill-admin/docs/delivery-checklist.md)

## 当前说明

- 当前版本重点交付赛题主链路，采用演示友好的默认账号与种子数据。
- 为了便于本地演示，账号密码使用明文种子数据；正式环境需要改成加密存储与更严格的审计能力。
