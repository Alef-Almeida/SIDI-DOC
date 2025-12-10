package br.com.ifba.sididoc.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

//Usando record para melhoria do código
public record CompleteRegistrationDTO(

        @NotBlank(message = "Token é obrigatório.")
        String token,

        //Utilizando regex para validar a senha
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Mínimo de 8 digitos, necessario incluir um caractere especial, letra maiúscula e um número."
        )
        String newPassword
) {}
