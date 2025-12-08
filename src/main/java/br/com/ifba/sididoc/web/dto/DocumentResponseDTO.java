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
    public static DocumentResponseDTO fromEntity(Document doc, String generatedUrl) {
        String sizeStr = doc.getMetaData().getOrDefault("size_bytes", "0");

        return new DocumentResponseDTO(
                doc.getTitle(),
                doc.getType().name(),
                doc.getStatus().name(),
                doc.getUploadDate(),
                Long.valueOf(sizeStr),
                generatedUrl
        );
    }
}