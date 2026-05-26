package com.SI.Crud.funcionarios_SI.model.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ChangeLogResponse {

    private Long id;
    private Long employeeId;
    private String employeeName;
    private String field;
    private String oldValue;
    private String newValue;
    private String changedBy;
    private String reason;
    private LocalDateTime changedAt;
}