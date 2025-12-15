package br.com.ifba.sididoc.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record UserSectorDTO(
        @NotNull(message = "O email é obrigatório")
        @Email(message = "Insira um email válido")
        String email,
        @NotNull(message = "O código do setor é obrigatório")
        String code
) {}
