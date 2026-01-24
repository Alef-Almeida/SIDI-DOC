package br.com.ifba.sididoc.web.controller;

import br.com.ifba.sididoc.entity.Document;
import br.com.ifba.sididoc.jwt.CustomUserDetails;
import br.com.ifba.sididoc.service.DocumentService;
import br.com.ifba.sididoc.web.dto.DocumentResponseDTO;
import br.com.ifba.sididoc.web.dto.DownloadDocumentDTO;
import br.com.ifba.sididoc.web.dto.UploadDocumentDTO;
import br.com.ifba.sididoc.web.dto.SearchRequestDTO;
import br.com.ifba.sididoc.web.dto.SearchResponseDTO;
import br.com.ifba.sididoc.service.VectorIndexerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.function.Function;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.data.segment.TextSegment;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final VectorIndexerService vectorIndexerService;
    // private final DocumentExportService documentExportService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<DocumentResponseDTO>> upload(@Valid @ModelAttribute UploadDocumentDTO dto, @AuthenticationPrincipal CustomUserDetails user) {

        Long sectorId = user.getCurrentSectorId();

        List<Document> documents = documentService.uploadDocuments(dto, sectorId);

        List<DocumentResponseDTO> response = documents.stream()
                .map(DocumentResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = "/find-all")
    public ResponseEntity<Page<DocumentResponseDTO>> findAll(
            @PageableDefault(size = 24, sort = "uploadDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Document> documentsPage = documentService.findAll(pageable);
        Page<DocumentResponseDTO> dtoPage = documentsPage.map(DocumentResponseDTO::fromEntity);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping(value = "/filter")
    public ResponseEntity<List<DocumentResponseDTO>> findBySectorAndCategory(
            @RequestParam(value = "sectorId") Long sectorId, @RequestParam(value = "categoryId") Long categoryId) {
        List<DocumentResponseDTO> results = documentService.findBySectorAndCategory(sectorId, categoryId);

        // Retorna a lista (pode ser vazia, o que é um resultado válido 200 OK)
        return ResponseEntity.ok(results);
    }

    @GetMapping(value = "/find-by-sector")
    public ResponseEntity<Page<DocumentResponseDTO>> findBySector(@AuthenticationPrincipal CustomUserDetails user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long sectorId = user.getCurrentSectorId();
        Page<Document> documentsPage = documentService.findBySector(sectorId, pageable);
        Page<DocumentResponseDTO> dtoPage = documentsPage.map(DocumentResponseDTO::fromEntity);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping(value = "/download", params = "id")
    public ResponseEntity<Resource> download(@RequestParam("id") Long id) {
        DownloadDocumentDTO dto = documentService.downloadDocument(id);

        InputStreamResource resource = new InputStreamResource(dto.inputStream());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(dto.contentType()))
                .contentLength(dto.length())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dto.filename() + "\"")
                .body(resource);
    }

    @GetMapping(value = "/download-zip")
    public ResponseEntity<StreamingResponseBody> downloadAsZip(@RequestParam("ids") List<Long> ids) {
        StreamingResponseBody stream = outputStream -> {
            documentService.downloadDocumentsAsZip(ids, outputStream);
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "documentos_sidi_doc.zip" + "\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(stream);
    }

    @PostMapping(value = "/search")
    public ResponseEntity<List<SearchResponseDTO>> search(@RequestBody @Valid SearchRequestDTO request) {
        var matches = vectorIndexerService.search(request.getQuery());
        return ResponseEntity.ok(enrichSearchResults(matches));
    }

    @PostMapping(value = "/search-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<SearchResponseDTO>> searchByImage(@RequestParam("file") MultipartFile file)
            throws IOException {
        var matches = vectorIndexerService.searchByImage(file.getBytes(), file.getContentType());
        return ResponseEntity.ok(enrichSearchResults(matches));
    }

    private List<SearchResponseDTO> enrichSearchResults(List<EmbeddingMatch<TextSegment>> matches) {
        if (matches.isEmpty()) {
            return List.of();
        }

        // 1. Extract IDs from matches
        List<Long> docIds = matches.stream()
                .map(m -> Long.valueOf(m.embedded().metadata().getString("document_id")))
                .distinct()
                .collect(Collectors.toList());

        // 2. Fetch latest Entity data from DB
        Map<Long, Document> docMap = documentService.findAllById(docIds).stream()
                .collect(Collectors.toMap(Document::getId, Function.identity()));

        // 3. Build DTOs merging Vector metadata with DB metadata
        return matches.stream().map(match -> {
            Long docId = Long.valueOf(match.embedded().metadata().getString("document_id"));
            Document doc = docMap.get(docId);

            // Start with existing vector metadata
            Map<String, Object> metadata = new HashMap<>(match.embedded().metadata().asMap());

            // Override/Enrich with fresh DB data
            if (doc != null) {
                // Ensure filename is correct
                if (doc.getTitle() != null) {
                    metadata.put("file_name", doc.getTitle());
                } else if (doc.getMetaData().containsKey("original_filename")) {
                    metadata.put("file_name", doc.getMetaData().get("original_filename"));
                }

                // Content Type
                if (doc.getMetaData().containsKey("content_type")) {
                    metadata.put("content_type", doc.getMetaData().get("content_type"));
                }

                // Size
                if (doc.getMetaData().containsKey("size_bytes")) {
                    metadata.put("file_size", doc.getMetaData().get("size_bytes"));
                }

                // Date
                if (doc.getUploadDate() != null) {
                    metadata.put("upload_date", doc.getUploadDate().toString());
                }
            }

            return SearchResponseDTO.builder()
                    .embeddingId(match.embeddingId())
                    .score(match.score())
                    .text(match.embedded().text())
                    .documentId(docId)
                    .metadata(metadata)
                    .build();
        }).collect(Collectors.toList());
    }
}