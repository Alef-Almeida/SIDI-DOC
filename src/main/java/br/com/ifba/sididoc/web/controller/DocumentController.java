package br.com.ifba.sididoc.web.controller;

import br.com.ifba.sididoc.entity.Document;
import br.com.ifba.sididoc.service.DocumentExportService;
import br.com.ifba.sididoc.service.DocumentService;
import br.com.ifba.sididoc.web.dto.DocumentResponseDTO;
import br.com.ifba.sididoc.web.dto.UploadDocumentDTO;
import jakarta.validation.Valid;
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
    private final DocumentExportService exportService;

    public DocumentController(DocumentService documentService, DocumentExportService exportService) {
        this.documentService = documentService;
        this.exportService = exportService;
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

    @GetMapping("/{id}/pdf")
    public ResponseEntity<StreamingResponseBody> downloadPdfReport(@PathVariable Long id) {
        Document document = documentService.findById(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio_" + id + ".pdf\"")
                .body(out -> {
                    try {
                        exportService.generatePdfReport(document, out);
                    } catch (Exception e) {
                        throw new RuntimeException("Erro ao gerar PDF", e);
                    }
                });
    }

    @GetMapping("/export/zip")
    public ResponseEntity<StreamingResponseBody> downloadAllZip() {
        List<Document> documents = documentService.findAll();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"documentos_ifba.zip\"")
                .body(out -> {
                    exportService.generateZipExport(documents, out);
                });
    }
}