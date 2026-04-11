package com.codex.skilladmin.apply;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccessRequestRepository extends JpaRepository<AccessRequestEntity, Long> {

    void deleteAllByResourceId(Long resourceId);

    List<AccessRequestEntity> findAllByResourceIdAndDeletedFalse(Long resourceId);

    List<AccessRequestEntity> findAllByApplicantUserIdAndDeletedFalseOrderByIdDesc(Long applicantUserId);

    List<AccessRequestEntity> findAllByDepartmentIdInAndDeletedFalseOrderByIdDesc(List<Long> departmentIds);

    Optional<AccessRequestEntity> findByResourceIdAndApplicantUserIdAndStatusAndDeletedFalse(
            Long resourceId, Long applicantUserId, AccessRequestStatus status);

    Optional<AccessRequestEntity> findByIdAndDeletedFalse(Long id);

    @Modifying
    @Query(value = "DELETE FROM resource_apply WHERE resource_id = ?1", nativeQuery = true)
    void hardDeleteAllByResourceId(Long resourceId);
}
