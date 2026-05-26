package com.SI.Crud.funcionarios_SI.controller;

import com.SI.Crud.funcionarios_SI.model.dto.response.EmployeeResponse;
import com.SI.Crud.funcionarios_SI.model.enums.EmployeeStatus;
import com.SI.Crud.funcionarios_SI.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeFilterController {

    private final EmployeeRepository employeeRepository;

    @GetMapping("/filter")
    public ResponseEntity<List<EmployeeResponse>> filter(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) EmployeeStatus status,
            @RequestParam(required = false) BigDecimal salaryMin,
            @RequestParam(required = false) BigDecimal salaryMax
    ) {
        List<EmployeeResponse> result = employeeRepository
                .findWithFilters(name, position, departmentId, status, salaryMin, salaryMax)
                .stream()
                .map(e -> EmployeeResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .email(e.getEmail())
                        .phone(e.getPhone())
                        .position(e.getPosition())
                        .salary(e.getSalary())
                        .departmentId(e.getDepartment() != null ? e.getDepartment().getId() : null)
                        .departmentName(e.getDepartment() != null ? e.getDepartment().getName() : null)
                        .status(e.getStatus())
                        .build())
                .toList();

        return ResponseEntity.ok(result);
    }
}