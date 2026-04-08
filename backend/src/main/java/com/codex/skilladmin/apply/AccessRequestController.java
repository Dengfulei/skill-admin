package com.codex.skilladmin.apply;

import com.codex.skilladmin.common.ApiResponse;
import com.codex.skilladmin.security.AuthenticatedUser;
import com.codex.skilladmin.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AccessRequestController {

    private final AccessRequestService accessRequestService;

    public AccessRequestController(AccessRequestService accessRequestService) {
        this.accessRequestService = accessRequestService;
    }

    @GetMapping("/api/user/applications")
    public ApiResponse<List<AccessRequestResponse>> listMine(@CurrentUser AuthenticatedUser user) {
        return ApiResponse.success(accessRequestService.listMine(user));
    }

    @PostMapping("/api/user/applications")
    public ApiResponse<AccessRequestResponse> submit(
            @Valid @RequestBody SubmitApplicationRequest request,
            @CurrentUser AuthenticatedUser user
    ) {
        return ApiResponse.success(accessRequestService.submit(request, user));
    }

    @GetMapping("/api/admin/applications")
    public ApiResponse<List<AccessRequestResponse>> listReviewable(@CurrentUser AuthenticatedUser user) {
        return ApiResponse.success(accessRequestService.listReviewable(user));
    }

    @PatchMapping("/api/admin/applications/{id}/review")
    public ApiResponse<AccessRequestResponse> review(
            @PathVariable Long id,
            @Valid @RequestBody ReviewApplicationRequest request,
            @CurrentUser AuthenticatedUser user
    ) {
        return ApiResponse.success(accessRequestService.review(id, request, user));
    }
}
