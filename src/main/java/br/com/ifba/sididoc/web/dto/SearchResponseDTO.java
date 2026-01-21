package br.com.ifba.sididoc.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponseDTO {
    private String embeddingId;
    private String text;
    private Double score;
    private Long documentId;
    private Map<String, ?> metadata;
}
