package br.com.ifba.sididoc.web.dto;

import br.com.ifba.sididoc.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


//DTO para atualização de usuário sendo Admin, alterando email e role
public record UpdateUserDTO(
        @NotBlank(message = "O nome é obrigatório.")
        String name,

        @Email(message = "E-mail inválido.")
        String email,

        @NotNull(message = "A role é obrigatória.")
        Role role
) {}
