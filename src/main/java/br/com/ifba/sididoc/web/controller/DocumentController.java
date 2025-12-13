package br.com.ifba.sididoc.web.controller;

import br.com.ifba.sididoc.entity.Document;
import br.com.ifba.sididoc.service.DocumentExportService;
import br.com.ifba.sididoc.service.DocumentService;
import br.com.ifba.sididoc.web.dto.DocumentExportDTO;
import br.com.ifba.sididoc.web.dto.DocumentResponseDTO;
import br.com.ifba.sididoc.web.dto.UploadDocumentDTO;
import jakarta.validation.Valid;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentExportService documentExportService;

    public DocumentController(DocumentService documentService, DocumentExportService exportService) {
        this.documentService = documentService;
        this.documentExportService = exportService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponseDTO> upload(@Valid @ModelAttribute UploadDocumentDTO dto) {
        try {
            DocumentResponseDTO response = documentService.uploadDocument(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadSingle(@PathVariable Long id) {
        DocumentExportDTO exportDto = documentExportService.exportDocument(id);
        return buildDownloadResponse(exportDto);
    }

    @GetMapping("/zip-export")
    public ResponseEntity<Resource> downloadZip(@RequestParam List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        DocumentExportDTO exportDto = documentExportService.exportDocumentsAsZip(ids);
        return buildDownloadResponse(exportDto);
    }

    private ResponseEntity<Resource> buildDownloadResponse(DocumentExportDTO exportDto) {
        ByteArrayResource resource = new ByteArrayResource(exportDto.data());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(exportDto.data().length)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + exportDto.filename() + "\"")
                .body(resource);
    }
}