package com.codex.skilladmin.department;

import com.codex.skilladmin.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/meta/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @GetMapping
    public ApiResponse<List<DepartmentEntity>> list() {
        return ApiResponse.success(departmentRepository.findAllByDeletedFalseAndStatusTrueOrderByIdAsc());
    }
}
