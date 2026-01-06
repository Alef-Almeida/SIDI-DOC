package br.com.ifba.sididoc.web.controller;

import br.com.ifba.sididoc.entity.Document;
import br.com.ifba.sididoc.service.DocumentService;
import br.com.ifba.sididoc.web.dto.DocumentResponseDTO;
import br.com.ifba.sididoc.web.dto.UploadDocumentDTO;
import jakarta.validation.Valid;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponseDTO> upload(@Valid @ModelAttribute UploadDocumentDTO dto) {
        try {
            DocumentResponseDTO response = documentService.uploadDocument(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException | TesseractException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/find-all")
    public ResponseEntity<Page<DocumentResponseDTO>> findAll(@PageableDefault(size = 24, sort = "uploadDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<DocumentResponseDTO> documents = documentService.findAll(pageable);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Document>> search(@RequestParam("q") String query) {
        List<Document> results = documentService.search(query);
        return ResponseEntity.ok(results);
    }
}