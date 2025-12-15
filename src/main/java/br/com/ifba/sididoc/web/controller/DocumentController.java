package br.com.ifba.sididoc.web.controller;

import br.com.ifba.sididoc.entity.Document;
import br.com.ifba.sididoc.jwt.CustomUserDetails;
import br.com.ifba.sididoc.service.DocumentService;
import br.com.ifba.sididoc.web.dto.DocumentResponseDTO;
import br.com.ifba.sididoc.web.dto.UploadDocumentDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    //private final DocumentExportService documentExportService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponseDTO> upload(@Valid @ModelAttribute UploadDocumentDTO dto, @AuthenticationPrincipal CustomUserDetails user) {
        Long sectorId = user.getCurrentSectorId();
        Document document = documentService.uploadDocument(dto, sectorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(DocumentResponseDTO.fromEntity(document));
    }

    @GetMapping(value = "/find-all")
    public ResponseEntity<Page<DocumentResponseDTO>> findAll(@PageableDefault(size = 24, sort = "uploadDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Document> documentsPage = documentService.findAll(pageable);
        Page<DocumentResponseDTO> dtoPage = documentsPage.map(DocumentResponseDTO::fromEntity);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping(value = "/filter")
    public ResponseEntity<List<DocumentResponseDTO>> findBySectorAndCategory(@RequestParam(value = "sectorId") Long sectorId, @RequestParam(value = "categoryId") Long categoryId) {
        List<DocumentResponseDTO> results = documentService.findBySectorAndCategory(sectorId, categoryId);

        // Retorna a lista (pode ser vazia, o que é um resultado válido 200 OK)
        return ResponseEntity.ok(results);
    }
}