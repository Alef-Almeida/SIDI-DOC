package br.com.ifba.sididoc.web.dto;

import br.com.ifba.sididoc.entity.Document;

import java.time.LocalDateTime;
import java.util.Map;

public record DocumentResponseDTO(
        Long id,
        String title,
        String sectorName,
        String categoryName,
        String type,
        String status,
        LocalDateTime uploadDate,
        Long sizeBytes,
        String downloadUrl
) {
    public static DocumentResponseDTO fromEntity(Document doc) {
        // Tratamento de segurança para metadata nulo
        Map<String, String> meta = doc.getMetaData();
        String sizeStr = (meta != null) ? meta.getOrDefault("size_bytes", "0") : "0";

        //Tratamento seguro para Relacionamentos (caso venha nulo)
        String sector = (doc.getSector() != null) ? doc.getSector().getName() : "N/A";
        String category = (doc.getCategory() != null) ? doc.getCategory().getName() : "Sem Categoria";
        return new DocumentResponseDTO(
                doc.getId(),
                doc.getTitle(),
                doc.getType().name(),
                doc.getStatus().name(),
                sector,
                category,
                doc.getUploadDate(),
                Long.valueOf(sizeStr),// Conversão segura
                doc.getPublicUrl()
        );
    }
}