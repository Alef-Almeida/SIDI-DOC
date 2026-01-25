package br.com.ifba.sididoc.web.dto;

public record CategorySuggestionDTO(
        Long id,
        String name,
        boolean found
) {}
