package br.com.ifba.sididoc.web.dto;

import br.com.ifba.sididoc.enums.DocumentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record UploadDocumentDTO(
        @NotNull(message = "O título é obrigatório")
        @Size(min = 3, message = "O título deve ter no mínimo 3 caracteres")
        String title,
        @NotNull(message = "O tipo do documento é obrigatório")
        DocumentType type,
        @NotNull(message = "O arquivo é obrigatório")
        MultipartFile file
) {}