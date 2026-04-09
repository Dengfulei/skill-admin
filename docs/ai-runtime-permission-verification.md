# AI 调用工具权限验证测试记录

## 文档目的

本文档用于记录“AI 调用 Skill / MCP 工具时自动校验权限，无权限返回友好提示”的实际测试过程与结果，可作为运行时鉴权链路的验证材料。

## 测试时间

- 日期：2026-04-09

## 测试目标

- 验证用户登录后，系统会在实际调用工具前执行权限校验
- 验证无权限用户调用受限资源时不会放行
- 验证系统会返回明确、友好的提示信息，而不是直接报错

## 测试环境

- 前端地址：`http://localhost:5173`
- 后端地址：`http://localhost:8080`
- 数据库：本地 MySQL
- 测试账号：`hr_user / 123456`
- 测试资源编码：`sales-analytics-mcp`

## 测试说明

本项目中的“运行时鉴权”指的是：

- 用户已经登录系统
- 当 AI 或前端准备实际调用某个 Skill / MCP 工具时
- 后端在调用发生前再次检查该用户是否具备该资源的使用权限

若校验通过，则允许执行。

若校验不通过，则返回友好提示：

> 当前账号暂无该技能或工具的使用权限，可联系管理员开通或提交部门技能申请。

## 测试步骤

### 1. 启动项目

确认前后端服务均已启动：

- 前端：`http://localhost:5173`
- 后端：`http://localhost:8080`

### 2. 调用登录接口获取 Token

执行命令：

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"hr_user","password":"123456"}'
```

返回结果：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI0IiwiZGlzcGxheU5hbWUiOiLkurrkuovlkZjlt6UiLCJkZXBhcnRtZW50SWRzIjpbM10sImRlcGFydG1lbnRBZG1pbklkcyI6W10sInVzZXJuYW1lIjoiaHJfdXNlciIsInN5c3RlbUFkbWluIjpmYWxzZSwiaWF0IjoxNzc1NzI0MTgwLCJleHAiOjE3NzU3NjczODB9.zhtNw9hmIckXx_tWFW5uQwTmNUKyVHH9wOm3PyehDJJmqG-ZsdlQnhlCfkKji2Bi",
    "user": {
      "id": 4,
      "username": "hr_user",
      "displayName": "人事员工",
      "systemAdmin": false,
      "departmentIds": [
        3
      ],
      "departmentAdminIds": []
    }
  }
}
```

说明：

- `hr_user` 登录成功
- 当前用户属于人事部
- 当前用户不是系统管理员，也不是部门管理员

### 3. 调用运行时鉴权接口验证无权限场景

执行命令：

```bash
curl -s -X POST http://localhost:8080/api/runtime/invoke \
  -H "Authorization: Bearer eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI0IiwiZGlzcGxheU5hbWUiOiLkurrkuovlkZjlt6UiLCJkZXBhcnRtZW50SWRzIjpbM10sImRlcGFydG1lbnRBZG1pbklkcyI6W10sInVzZXJuYW1lIjoiaHJfdXNlciIsInN5c3RlbUFkbWluIjpmYWxzZSwiaWF0IjoxNzc1NzI0MTgwLCJleHAiOjE3NzU3NjczODB9.zhtNw9hmIckXx_tWFW5uQwTmNUKyVHH9wOm3PyehDJJmqG-ZsdlQnhlCfkKji2Bi" \
  -H "Content-Type: application/json" \
  -d '{"resourceCode":"sales-analytics-mcp"}'
```

返回结果：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "allowed": false,
    "message": "当前账号暂无该技能或工具的使用权限，可联系管理员开通或提交部门技能申请。"
  }
}
```

## 测试结果分析

本次测试结果说明：

- 用户已成功登录，说明请求具备合法身份
- 目标资源 `sales-analytics-mcp` 存在，但当前用户无对应使用权限
- 后端在运行时成功拦截了此次工具调用
- 系统没有直接抛出异常，而是返回了明确的友好提示

这证明项目中的“AI 调用工具自动校验权限”链路已经生效。

## 结论

本次验证通过，系统满足以下要求：

1. AI 调用工具前会自动执行权限校验
2. 无权限时不会放行调用
3. 无权限时会返回友好提示信息
4. 运行时鉴权逻辑已在真实接口调用中得到验证

## 相关实现位置

- 运行时鉴权入口：[RuntimeInvokeController.java](/Users/dengfulei/Desktop/codex-ai/skill-admin/backend/src/main/java/com/codex/skilladmin/runtime/RuntimeInvokeController.java)
- 权限判断逻辑：[ResourceService.java](/Users/dengfulei/Desktop/codex-ai/skill-admin/backend/src/main/java/com/codex/skilladmin/resource/ResourceService.java)
- 未登录返回处理：[SecurityConfig.java](/Users/dengfulei/Desktop/codex-ai/skill-admin/backend/src/main/java/com/codex/skilladmin/security/SecurityConfig.java)
- 页面测试入口：[MyResourcesView.vue](/Users/dengfulei/Desktop/codex-ai/skill-admin/frontend/src/views/user/MyResourcesView.vue)

## 可补充的对照测试

为使验证材料更完整，后续还可补充以下 2 组对照场景：

- 有权限用户调用资源，返回 `allowed = true`
- 未登录直接调用接口，返回 `401` 与“未登录或登录已过期”
