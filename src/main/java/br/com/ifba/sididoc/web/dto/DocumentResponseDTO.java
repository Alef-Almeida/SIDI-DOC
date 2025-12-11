package br.com.ifba.sididoc.web.dto;

import br.com.ifba.sididoc.entity.Document;

import java.time.LocalDateTime;

public record DocumentResponseDTO(
        String title,
        String type,
        String status,
        LocalDateTime uploadDate,
        Long sizeBytes,
        String downloadUrl
) {
    public static DocumentResponseDTO fromEntity(Document doc) {
        return new DocumentResponseDTO(
                doc.getTitle(),
                doc.getType().name(),
                doc.getStatus().name(),
                doc.getUploadDate(),
                Long.valueOf(doc.getMetaData().get("size_bytes")),
                doc.getPublicUrl()
        );
    }
}