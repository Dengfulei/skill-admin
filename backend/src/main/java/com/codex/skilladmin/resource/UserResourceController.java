package com.codex.skilladmin.resource;

import com.codex.skilladmin.common.ApiResponse;
import com.codex.skilladmin.security.AuthenticatedUser;
import com.codex.skilladmin.security.CurrentUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user/resources")
public class UserResourceController {

    private final ResourceService resourceService;

    public UserResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/available")
    public ApiResponse<List<ResourceSummaryResponse>> available(@CurrentUser AuthenticatedUser user) {
        return ApiResponse.success(resourceService.listAvailableResources(user));
    }

    @GetMapping("/apply-catalog")
    public ApiResponse<List<ResourceSummaryResponse>> applyCatalog(@CurrentUser AuthenticatedUser user) {
        return ApiResponse.success(resourceService.listDepartmentApplyCatalog(user));
    }
}
