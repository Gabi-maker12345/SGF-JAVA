package com.SI.Crud.funcionarios_SI.service;

import com.SI.Crud.funcionarios_SI.model.dto.response.DashboardStatsResponse;
import com.SI.Crud.funcionarios_SI.repository.DepartmentRepository;
import com.SI.Crud.funcionarios_SI.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardAnalyticsService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public DashboardStatsResponse getStats() {

        // folha por departamento
        List<Map<String, Object>> salaryByDept = employeeRepository.salaryByDepartment()
                .stream().map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("department", row[0]);
                    map.put("totalSalary", row[1]);
                    map.put("totalEmployees", row[2]);
                    map.put("averageSalary", row[3]);
                    return map;
                }).toList();

        // distribuição por cargo
        List<Map<String, Object>> byPosition = employeeRepository.countByPosition()
                .stream().map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("position", row[0]);
                    map.put("count", row[1]);
                    return map;
                }).toList();

        // distribuição por status
        List<Map<String, Object>> byStatus = employeeRepository.countByStatus()
                .stream().map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", row[0]);
                    map.put("count", row[1]);
                    return map;
                }).toList();

        return DashboardStatsResponse.builder()
                .totalEmployees(employeeRepository.countByDeletedAtIsNull())
                .totalDepartments(departmentRepository.count())
                .totalPayroll(employeeRepository.sumActiveSalary())
                .averageSalary(employeeRepository.avgActiveSalary())
                .salaryByDepartment(salaryByDept)
                .countByPosition(byPosition)
                .countByStatus(byStatus)
                .build();
    }
}