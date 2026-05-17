package com.SI.Crud.funcionarios_SI.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "O nome e obrigatorio")
    private String name;

    @Email(message = "Email invalido")
    @NotBlank(message = "O email e obrigatorio")
    private String email;

    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
    @NotBlank(message = "A senha e obrigatoria")
    private String password;

    private String role;
}
