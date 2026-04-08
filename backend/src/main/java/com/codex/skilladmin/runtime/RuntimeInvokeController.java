package com.codex.skilladmin.runtime;

import com.codex.skilladmin.common.ApiResponse;
import com.codex.skilladmin.resource.ResourceService;
import com.codex.skilladmin.security.AuthenticatedUser;
import com.codex.skilladmin.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/runtime")
public class RuntimeInvokeController {

    private final ResourceService resourceService;

    public RuntimeInvokeController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping("/invoke")
    public ApiResponse<RuntimeInvokeResponse> invoke(
            @Valid @RequestBody InvokeRequest request,
            @CurrentUser AuthenticatedUser user
    ) {
        boolean allowed = resourceService.canUseResource(user, request.resourceCode());
        String message = allowed
                ? "鉴权通过，可执行 Skill/MCP 调用"
                : "当前账号暂无该技能或工具的使用权限，可联系管理员开通或提交部门技能申请。";
        return ApiResponse.success(new RuntimeInvokeResponse(allowed, message));
    }
}
