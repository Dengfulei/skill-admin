package com.codex.skilladmin.resource;

import com.codex.skilladmin.common.ApiResponse;
import com.codex.skilladmin.common.PageResponse;
import com.codex.skilladmin.security.AuthenticatedUser;
import com.codex.skilladmin.security.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/resources")
public class UserResourceController {

    private final ResourceService resourceService;

    public UserResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/available")
    public ApiResponse<ResourcePageResponse> available(
            @CurrentUser AuthenticatedUser user,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ResourceType resourceType
    ) {
        return ApiResponse.success(resourceService.listAvailableResources(user, keyword, resourceType, pageNum, pageSize));
    }

    @GetMapping("/apply-catalog")
    public ApiResponse<PageResponse<ResourceSummaryResponse>> applyCatalog(
            @CurrentUser AuthenticatedUser user,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        return ApiResponse.success(resourceService.listDepartmentApplyCatalog(user, pageNum, pageSize));
    }
}
