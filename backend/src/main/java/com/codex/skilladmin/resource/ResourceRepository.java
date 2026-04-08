package com.codex.skilladmin.resource;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ResourceRepository extends JpaRepository<ResourceEntity, Long> {

    List<ResourceEntity> findAllByDeletedFalseOrderByIdDesc();

    List<ResourceEntity> findAllByIdInAndDeletedFalseOrderByIdDesc(Collection<Long> ids);

    Optional<ResourceEntity> findByIdAndDeletedFalse(Long id);

    Optional<ResourceEntity> findByCodeAndDeletedFalse(String code);

    List<ResourceEntity> findAllByOwnerDepartmentIdInAndDeletedFalse(Collection<Long> departmentIds);

    List<ResourceEntity> findAllByOwnerUserIdAndDeletedFalse(Long ownerUserId);
}
