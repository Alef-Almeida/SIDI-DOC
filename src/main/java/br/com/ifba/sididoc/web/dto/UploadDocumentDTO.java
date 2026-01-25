package br.com.ifba.sididoc.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record UploadDocumentDTO(
        @NotNull(message = "A lista de arquivos não pode ser nula.")
        @NotEmpty(message = "Selecione pelo menos um arquivo.")
        List<MultipartFile> files,

        @NotNull(message = "A categoria é obrigatória.")
        Long categoryId,

        String batchCode
) {}