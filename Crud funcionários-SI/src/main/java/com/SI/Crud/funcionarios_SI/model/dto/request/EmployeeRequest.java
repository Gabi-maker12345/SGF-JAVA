package com.SI.Crud.funcionarios_SI.model.dto.request;

import com.SI.Crud.funcionarios_SI.model.enums.EmployeeStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    @Pattern(regexp = "^(\\+244\\d{9})?$", message = "O telefone deve seguir o formato angolano +244000000000")
    private String phone;

    private EmployeeStatus status;

    @NotBlank(message = "O cargo e obrigatorio")
    private String position;

    @NotNull(message = "O salario e obrigatorio")
    @DecimalMin(value = "100000.00", inclusive = true, message = "O salário mínimo angolano é 100.000kzs, insira valores apartir disso")
    private BigDecimal salary = new BigDecimal("100000.00");

    @NotNull(message = "O departamento e obrigatorio")
    private Long departmentId;
}
