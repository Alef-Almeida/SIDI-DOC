package br.com.ifba.sididoc.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SearchRequestDTO {
    @NotBlank(message = "A query de busca não pode estar vazia")
    private String query;
}
