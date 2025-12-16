package br.com.ifba.sididoc.web.dto;

import br.com.ifba.sididoc.entity.DocumentCategory;
import jakarta.validation.constraints.NotNull;

public record DocumentCategoryResponseDTO(
        Long id,
        String name,
        String description,
        boolean active
){
    public static DocumentCategoryResponseDTO fromEntity(DocumentCategory doc){
        return new DocumentCategoryResponseDTO(
                doc.getId(),
                doc.getName(),
                doc.getDescription(),
                doc.isActive()
        );
    }
}
