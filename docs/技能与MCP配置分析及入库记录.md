# Codex Skills / MCP 配置分析与入库记录

## 文档目的

记录本次会话中对本机 Codex `skills` 与 `MCP` 配置的分析过程，以及将对应配置写入 `skill_admin` 数据库的处理结果，作为题目“Skill / MCP 配置数据库化”的补充过程材料。

## 会话时间

- 日期：2026-04-08
- 工作目录：`/Users/dengfulei/Desktop/codex-ai/skill-admin`

## 1. 本机 Codex 配置分析

### 1.1 Skills 配置来源

本机 `CODEX_HOME` 未显式设置，因此 Codex 本地技能目录实际使用：

- `~/.codex/skills`

另外，第三方安装的 skills 存放在：

- `~/.agents/skills`

本次会话确认到的技能如下：

#### `~/.codex/skills`

- `.system/imagegen`
- `.system/openai-docs`
- `.system/plugin-creator`
- `.system/skill-creator`
- `.system/skill-installer`
- `frontend-skill`
- `app-ui-polish`

#### `~/.agents/skills`

- `find-skills`
- `pdf`

### 1.2 Skills 元数据特征

每个 Skill 的核心描述信息位于 `SKILL.md` 顶部 frontmatter，关键字段包括：

- `name`
- `description`

因此在数据库化过程中，可以将这些元数据抽取后存入：

- 主表 `skill_resource`
- 详情表 `skill_config`

### 1.3 Skills 异常点

本次分析发现本地 Skill `app-ui-polish` 的 `SKILL.md` frontmatter 不合法：

- `description` 被写成了数组形式
- Codex 日志中出现 `invalid YAML` 报错

因此该 Skill 当前并不能被 Codex 正常加载。为避免把无效配置直接当作可用资源，本次入库时将其记为：

- `status = DRAFT`
- `enabled = 0`

### 1.4 MCP 配置来源

MCP 配置集中在：

- `~/.codex/config.toml`

本次会话确认到当前仅注册了两个 MCP Server：

- `stitch`
- `context7`

具体特征如下：

#### Stitch

- 传输方式：HTTP
- 地址：`https://stitch.googleapis.com/mcp`
- 配置了 `X-Goog-Api-Key`

#### Context7

- 传输方式：STDIO
- 命令：`npx`
- 参数：`-y @upstash/context7-mcp`

### 1.5 MCP 运行时校验结论

本次会话没有只停留在配置文件分析层面，而是做了实际调用验证：

- `stitch` 可以正常返回项目列表
- `context7` 可以正常返回文档库解析结果

说明这两个 MCP 在当前环境中是可用的。

## 2. 数据库化映射思路

### 2.1 与项目表结构的对应关系

项目当前数据库设计已经具备承接 Codex 文件配置的能力，核心表包括：

- `skill_resource`：统一资源主表
- `skill_config`：Skill 详情表
- `mcp_config`：MCP 详情表
- `resource_permission`：资源权限表

本次入库采用如下映射规则：

- Codex `.system` 内置 skills 记为公共资源
- 当前用户本地自定义 skills 记为个人资源
- 当前用户通过 skills 生态安装的 skills 记为个人资源
- 当前用户本地配置的 MCP server 记为个人资源

### 2.2 归属策略

为承接当前机器上的个人配置，本次新增一个演示用户：

- 用户名：`dengfulei`
- 用户 ID：`6`

并将个人 Skill / MCP 资源归属到该用户名下。

## 3. 实际写库内容

### 3.1 写入前数据库现状

写入前数据库 `skill_admin` 中已有演示数据：

- 用户 5 个
- 资源 3 个
- 权限 2 条

本次写入在保留原有演示数据的基础上追加配置快照数据。

### 3.2 新增用户与归属关系

新增内容：

- `sys_user` 新增 1 条：`dengfulei`
- `sys_user_department` 新增 1 条：归属 `平台部`

### 3.3 新增资源

本次新增资源共 11 条：

#### 公共 Skill 5 条

- `codex-imagegen-skill`
- `codex-openai-docs-skill`
- `codex-plugin-creator-skill`
- `codex-skill-creator-skill`
- `codex-skill-installer-skill`

#### 个人 Skill 4 条

- `frontend-skill-local`
- `app-ui-polish-local`
- `find-skills-installed`
- `pdf-installed`

#### 个人 MCP 2 条

- `stitch-mcp-local`
- `context7-mcp-local`

### 3.4 新增 Skill 详情配置

写入 `skill_config` 共 9 条，主要保存：

- 版本标识
- `SKILL.md` 路径
- 原始描述信息
- 安装来源或内置来源

其中：

- 内置 skill 使用 `builtin`
- 本地自定义 skill 使用 `local`
- 安装 skill 使用 `installed`
- 无效 YAML 的本地 skill 使用 `local-draft`

### 3.5 新增 MCP 详情配置

写入 `mcp_config` 共 2 条：

#### `stitch`

- `server_name = stitch`
- `transport_type = HTTP`
- `endpoint_url = https://stitch.googleapis.com/mcp`
- `headers_json` 中记录了请求头

#### `context7`

- `server_name = context7`
- `transport_type = STDIO`
- `command_line = npx`
- `args_json = ["-y", "@upstash/context7-mcp"]`

### 3.6 新增权限

写入 `resource_permission` 共 11 条：

- 公共 Skill 权限 5 条
- 个人资源权限 6 条

权限策略如下：

- 公共技能以 `PUBLIC + USE` 写入
- 个人 Skill / MCP 以 `PERSONAL + USE` 写入

## 4. 写库结果回查

写入完成后已进行回查，确认以下结果：

- `sys_user` 中存在 `id = 6, username = dengfulei`
- `skill_resource` 中存在 `id = 4 ~ 14` 的新增资源
- `skill_config` 中存在 `resource_id = 4 ~ 12` 的配置记录
- `mcp_config` 中存在 `resource_id = 13 ~ 14` 的配置记录
- `resource_permission` 中存在 `id = 3 ~ 13` 的权限记录

资源状态回查结果符合预期：

- 内置公共 Skill 为 `ACTIVE + enabled = 1`
- `frontend-skill`、`find-skills`、`pdf` 为个人可用资源
- `stitch`、`context7` 为个人可用 MCP 资源
- `app-ui-polish` 为 `DRAFT + enabled = 0`

## 5. 本次会话结论

本次会话完成了两项关键工作：

1. 梳理了 Codex 当前真实的 `skills` 与 `MCP` 文件配置来源、结构与运行状态
2. 将这些配置按“公共 / 个人”资源模型成功写入 `skill_admin` 数据库

这说明当前项目设计的数据库模型能够有效承接真实的 Codex 本地配置，不再依赖纯文件方式管理 Skill / MCP，满足题目“配置数据库化”的核心目标。

## 6. 后续可继续优化的点

- 修复 `app-ui-polish` 的 `SKILL.md` frontmatter，使其可正常加载并切换为启用状态
- 将本次手工写入整理为 Flyway 迁移脚本，支持数据库一键重建
- 对 `headers_json`、`env_json` 中的敏感信息增加脱敏展示或密文存储策略
- 后续可增加“从本机配置扫描并自动导入数据库”的后台同步能力
