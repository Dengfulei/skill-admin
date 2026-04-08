package com.codex.skilladmin.department;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long> {

    List<DepartmentEntity> findAllByDeletedFalseAndStatusTrueOrderByIdAsc();
}
