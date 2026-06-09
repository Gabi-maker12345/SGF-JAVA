package com.SI.Crud.funcionarios_SI.model.dto.request;

import com.SI.Crud.funcionarios_SI.model.enums.EmployeeStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class EmployeeFilterRequest {

    private String name;
    private String position;
    private Long departmentId;
    private EmployeeStatus status;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
}