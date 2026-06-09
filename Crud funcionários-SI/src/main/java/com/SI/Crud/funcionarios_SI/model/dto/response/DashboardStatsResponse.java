package com.SI.Crud.funcionarios_SI.model.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class DashboardStatsResponse {

    private long totalEmployees;
    private long totalDepartments;
    private BigDecimal totalPayroll;
    private BigDecimal averageSalary;

    // folha salarial por departamento
    private List<Map<String, Object>> salaryByDepartment;

    // distribuição por cargo
    private List<Map<String, Object>> countByPosition;

    // distribuição por status
    private List<Map<String, Object>> countByStatus;
}