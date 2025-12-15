package br.com.ifba.sididoc.web.dto;

import br.com.ifba.sididoc.entity.DocumentCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

public record DocumentCategoryRequestDTO(
        @NotBlank(message = "O nome é obrigatório.")
        String name,

        String description
){}
