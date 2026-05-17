package com.SI.Crud.funcionarios_SI.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Email(message = "Email invalido")
    @NotBlank(message = "O email e obrigatorio")
    private String email;

    @NotBlank(message = "A senha e obrigatoria")
    private String password;
}
