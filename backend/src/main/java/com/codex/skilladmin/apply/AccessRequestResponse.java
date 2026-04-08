package com.codex.skilladmin.apply;

public record AccessRequestResponse(
        Long id,
        Long resourceId,
        String resourceName,
        Long applicantUserId,
        Long departmentId,
        String reason,
        AccessRequestStatus status,
        String reviewComment
) {
}
