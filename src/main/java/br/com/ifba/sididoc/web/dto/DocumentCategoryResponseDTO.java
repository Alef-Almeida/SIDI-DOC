package br.com.ifba.sididoc.web.dto;

import br.com.ifba.sididoc.entity.DocumentCategory;
import jakarta.validation.constraints.NotNull;

public record DocumentCategoryResponseDTO(
        String name,
        String description,
        boolean active
){
    public static DocumentCategoryResponseDTO fromEntity(DocumentCategory doc){
        return new DocumentCategoryResponseDTO(
                doc.getName(),
                doc.getDescription(),
                doc.isActive()
        );
    }
}
