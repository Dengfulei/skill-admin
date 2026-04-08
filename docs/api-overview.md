# 接口与权限说明

## 认证接口

### `POST /api/auth/login`

登录并返回 JWT。

请求示例：

```json
{
  "username": "admin",
  "password": "123456"
}
```

### `GET /api/auth/me`

返回当前登录用户及部门信息。

## 管理端接口

### `GET /api/admin/resources`

查看当前用户可管理的资源。

- 系统管理员：全部资源
- 部门管理员：本部门资源
- 普通用户：仅本人个人资源

### `POST /api/admin/resources`

创建资源。

支持：

- `resourceType = SKILL / MCP`
- `scopeLevel = PUBLIC / DEPARTMENT / PERSONAL`
- 指定权限分配 `permissions`

### `PUT /api/admin/resources/{id}`

更新资源及配置。

### `PATCH /api/admin/resources/{id}/enabled`

启用 / 禁用资源。

### `DELETE /api/admin/resources/{id}`

逻辑删除资源。

### `GET /api/admin/applications`

查看当前管理员可审批的申请记录。

### `PATCH /api/admin/applications/{id}/review`

审批申请，审批通过后写入个人使用权限。

## 用户端接口

### `GET /api/user/resources/available`

返回当前用户可访问的资源集合：

- 公共授权
- 部门授权
- 个人授权

### `GET /api/user/resources/apply-catalog`

返回当前用户所在部门、且需要申请、且尚未授权给当前用户的资源。

### `GET /api/user/applications`

返回当前用户自己的申请记录。

### `POST /api/user/applications`

提交部门级资源申请。

## 运行时鉴权接口

### `POST /api/runtime/invoke`

模拟 AI 在运行时发起 Skill / MCP 调用。

请求示例：

```json
{
  "resourceCode": "sales-analytics-mcp"
}
```

鉴权逻辑：

1. 资源必须存在
2. 资源必须启用且状态为 `ACTIVE`
3. 命中以下任一授权即可通过
   - `PUBLIC + USE`
   - `DEPARTMENT + USE` 且当前用户属于该部门
   - `PERSONAL + USE` 且授权用户为当前用户

失败时返回友好提示：

> 当前账号暂无该技能或工具的使用权限，可联系管理员开通或提交部门技能申请。

## 服务端权限边界

### 公共级资源

- 仅系统管理员可维护
- 全员可用

### 部门级资源

- 仅该部门管理员可维护
- 可按部门直接授权
- 也可开启申请模式，由管理员审批后按个人授权

### 个人级资源

- 仅本人可维护
- 仅本人可用
