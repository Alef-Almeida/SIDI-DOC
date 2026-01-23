package br.com.ifba.sididoc.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateBatchDTO(
        @NotBlank(message = "O código é obrigatório.")
        String code,

        @NotBlank(message = "A descrição é obrigatória.")
        String description
) {}