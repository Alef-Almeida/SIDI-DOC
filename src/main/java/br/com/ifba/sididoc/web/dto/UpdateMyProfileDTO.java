package br.com.ifba.sididoc.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

//Update do perfil do próprio usuário, sem alterar role
public record UpdateMyProfileDTO(
        @NotBlank(message = "O nome é obrigatório.")
        String name,

        @Email(message = "E-mail inválido.")
        String email
) {}
