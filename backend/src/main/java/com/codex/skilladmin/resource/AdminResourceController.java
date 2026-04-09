package com.codex.skilladmin.resource;

import com.codex.skilladmin.common.ApiResponse;
import com.codex.skilladmin.security.AuthenticatedUser;
import com.codex.skilladmin.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/resources")
public class AdminResourceController {

    private final ResourceService resourceService;

    public AdminResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public ApiResponse<ResourcePageResponse> list(
            @CurrentUser AuthenticatedUser user,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ResourceType resourceType
    ) {
        return ApiResponse.success(resourceService.listManageableResources(user, keyword, resourceType, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<ResourceDetailResponse> detail(@PathVariable Long id, @CurrentUser AuthenticatedUser user) {
        return ApiResponse.success(resourceService.getDetail(id, user));
    }

    @PostMapping
    public ApiResponse<ResourceDetailResponse> create(
            @Valid @RequestBody ResourceUpsertRequest request,
            @CurrentUser AuthenticatedUser user
    ) {
        return ApiResponse.success(resourceService.create(request, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<ResourceDetailResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ResourceUpsertRequest request,
            @CurrentUser AuthenticatedUser user
    ) {
        return ApiResponse.success(resourceService.update(id, request, user));
    }

    @PatchMapping("/{id}/enabled")
    public ApiResponse<Void> toggleEnabled(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request,
            @CurrentUser AuthenticatedUser user
    ) {
        resourceService.toggleEnabled(id, Boolean.TRUE.equals(request.get("enabled")), user);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @CurrentUser AuthenticatedUser user) {
        resourceService.delete(id, user);
        return ApiResponse.success();
    }
}
