package com.codex.skilladmin.apply;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccessRequestRepository extends JpaRepository<AccessRequestEntity, Long> {

    void deleteAllByResourceId(Long resourceId);

    List<AccessRequestEntity> findAllByApplicantUserIdAndDeletedFalseOrderByIdDesc(Long applicantUserId);

    List<AccessRequestEntity> findAllByDepartmentIdInAndDeletedFalseOrderByIdDesc(List<Long> departmentIds);

    Optional<AccessRequestEntity> findByResourceIdAndApplicantUserIdAndStatusAndDeletedFalse(
            Long resourceId, Long applicantUserId, AccessRequestStatus status);

    Optional<AccessRequestEntity> findByIdAndDeletedFalse(Long id);
}
