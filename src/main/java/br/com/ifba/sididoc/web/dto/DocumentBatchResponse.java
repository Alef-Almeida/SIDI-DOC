package br.com.ifba.sididoc.web.dto;

import br.com.ifba.sididoc.entity.DocumentBatch;

public record DocumentBatchResponse(
        String code
) {
    public static DocumentBatchResponse fromEntity(DocumentBatch documentBatch) {
        return new DocumentBatchResponse(
                documentBatch.getCode()
        );
    }
}
