package com.SI.Crud.funcionarios_SI.model.dto.response;

import com.SI.Crud.funcionarios_SI.model.enums.EmployeeStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TrashEmployeeResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String position;
    private BigDecimal salary;
    private String departmentName;
    private EmployeeStatus status;
    private LocalDateTime deletedAt;
}