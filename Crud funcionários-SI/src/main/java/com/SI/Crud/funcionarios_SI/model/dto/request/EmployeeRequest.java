package com.SI.Crud.funcionarios_SI.model.dto.request;

import com.SI.Crud.funcionarios_SI.model.enums.EmployeeStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class EmployeeRequest {

    @NotBlank(message = "O nome e obrigatorio")
    private String name;

    @Email(message = "Email invalido")
    @NotBlank(message = "O email e obrigatorio")
    private String email;

    private String phone;

    private EmployeeStatus status;

    private String reason; // motivo da alteração (opcional)

    @NotBlank(message = "O cargo e obrigatorio")
    private String position;

    @NotNull(message = "O salario e obrigatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "O salario deve ser maior que zero")
    private BigDecimal salary;

    @NotNull(message = "O departamento e obrigatorio")
    private Long departmentId;
}
