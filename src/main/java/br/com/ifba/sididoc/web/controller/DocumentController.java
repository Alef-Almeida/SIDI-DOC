package br.com.ifba.sididoc.web.controller;

import br.com.ifba.sididoc.entity.Document;
import br.com.ifba.sididoc.service.DocumentService;
import br.com.ifba.sididoc.service.ImageToPdfService;
import br.com.ifba.sididoc.util.InMemoryMultipartFile;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final ImageToPdfService imageToPdfService;

    public DocumentController(DocumentService documentService, ImageToPdfService imageToPdfService) {
        this.documentService = documentService;
        this.imageToPdfService = imageToPdfService;
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
    /**
     * Endpoint EXCLUSIVO para o Módulo de Scanner.
     * Recebe múltiplas imagens, converte para PDF e salva.
     */
    @PostMapping(value = "/upload/scanned", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponseDTO> uploadScannedImages(
            @RequestParam("files") List<MultipartFile> files, // Aceita LISTA de imagens
            @RequestParam("title") String title) {

        try {
            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            System.out.println(">>> 📠 Recebendo " + files.size() + " imagens do Scanner...");

            // 1. O 'ImageToPdfService' (Task 1) transforma as fotos em um único PDF em memória
            byte[] pdfBytes = imageToPdfService.convertImagesToPdf(files);

            // 2. Cria um nome para o arquivo gerado
            String generatedFilename = (title != null && !title.isBlank() ? title : "documento_scanner") + ".pdf";

            // 3. O ADAPTER: Transforma o byte[] em algo que o seu Service aceita (MultipartFile)
            // (Você precisa daquela classe InMemoryMultipartFile que te passei antes)
            MultipartFile fileToProcess = new InMemoryMultipartFile(
                    "file",
                    generatedFilename,
                    "application/pdf",
                    pdfBytes
            );

            // 4. Monta o DTO que o seu serviço espera
            // (Ajuste aqui se seu DTO tiver mais campos obrigatórios)
            UploadDocumentDTO uploadDTO = new UploadDocumentDTO(fileToProcess, title);

            // 5. REAPROVEITAMENTO TOTAL: Chama o método que já existe e funciona!
            // O DocumentService nem sabe que isso veio de um scanner.
            DocumentResponseDTO response = documentService.uploadDocument(uploadDTO);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // Em produção, use log.error
            return ResponseEntity.internalServerError().build();
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