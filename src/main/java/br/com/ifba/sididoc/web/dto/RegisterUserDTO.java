package br.com.ifba.sididoc.web.dto;

import br.com.ifba.sididoc.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.List;

//Usando record para melhoria do código
public record RegisterUserDTO(

        @NotBlank(message = "Nome é obrigatório.")
        @Pattern(
                regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ ]{3,}$",
                message = "Nome inválido, use ao menos 3 letras."
        )
        String name,

        @Email(message = "E-mail inválido.")
        @NotBlank(message = "E-mail é obrigatório.")
        String email,

        List<Long> sectorIds,

        Role role
) {}
