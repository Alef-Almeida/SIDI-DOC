package br.com.ifba.sididoc.web.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record UploadDocumentDTO(
        @NotNull(message = "O arquivo é obrigatório.")
        MultipartFile file,

        @NotNull(message = "A categoria é obrigatória.")
        Long categoryId
) {}