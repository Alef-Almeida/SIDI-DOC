package br.com.ifba.sididoc.service;

import br.com.ifba.sididoc.entity.Document;
import br.com.ifba.sididoc.entity.DocumentBatch;
import br.com.ifba.sididoc.enums.DocumentType;
import br.com.ifba.sididoc.enums.ProcessingStatus;
import br.com.ifba.sididoc.exception.*;
import br.com.ifba.sididoc.repository.DocumentRepository;
import br.com.ifba.sididoc.web.dto.DocumentResponseDTO;
import br.com.ifba.sididoc.web.dto.DownloadDocumentDTO;
import br.com.ifba.sididoc.web.dto.UploadDocumentDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.OutputStream;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RequiredArgsConstructor
@Service
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final S3Client s3Client;
    @Value("${supabase.bucket.name}")
    private String bucketName;
    @Value("${supabase.project.url}")
    private String supabaseProjectUrl;
    private final DocumentCategoryService documentCategoryService;
    private final SectorService sectorService;
    private final VectorIndexerService vectorIndexerService;
    private final DocumentBatchService batchService;

    @Transactional
    public Document uploadDocument(UploadDocumentDTO dto, Long sectorId) {
        MultipartFile file = dto.file();
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        long size = file.getSize();

        log.info("Iniciando processamento de upload. Arquivo: [{}], Tipo: [{}], Tamanho: [{} bytes]", originalFilename,
                contentType, size);

        String title = validateAndExtractTitle(originalFilename);
        DocumentType type = detectDocumentType(contentType);
        String extension = getFileExtension(originalFilename);
        String storageKey = UUID.randomUUID().toString() + "." + extension;
        String fullStoragePath = generateStoragePath(storageKey);

        log.debug("Metadados extraídos com sucesso. Título: '{}', Caminho Storage: '{}'", title, fullStoragePath);

        try {
            log.info("Enviando arquivo para o Supabase Storage (Bucket: {})...", bucketName);
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fullStoragePath)
                    .contentType(contentType)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            log.info("Upload para o Storage concluído com sucesso.");

        } catch (Exception e) {
            log.error("Falha crítica ao enviar arquivo para o Storage. Caminho: {}", fullStoragePath, e);
            throw new CloudStorageException("Erro ao enviar arquivo para o Storage: " + e.getMessage(), e);
        }

        Document document = new Document();
        document.setCategory(documentCategoryService.findById(dto.categoryId()));

        if (dto.batchCode() != null && !dto.batchCode().isBlank()) {
            DocumentBatch batch = batchService.findByCode(dto.batchCode());
            document.setBatch(batch);
        }

        document.setSector(sectorService.findById(sectorId));
        document.setTitle(title);
        document.setType(type);
        document.setUploadDate(LocalDateTime.now());
        document.setStatus(ProcessingStatus.PENDING);
        document.getMetaData().put("original_filename", originalFilename);
        document.getMetaData().put("storage_path", fullStoragePath);
        document.getMetaData().put("content_type", contentType);
        document.getMetaData().put("size_bytes", String.valueOf(size));
        document.getMetaData().put("bucket", bucketName);

        try {
            log.debug("Tentando salvar registro do documento no banco de dados...");
            Document savedDoc = documentRepository.save(document);
            log.info("Documento persistido no banco com sucesso. ID: {}", savedDoc.getId());

            // Indexação Vetorial Assíncrona (ou síncrona dependendo do requisito)
            try {
                vectorIndexerService.indexDocument(savedDoc, file.getBytes());
            } catch (Exception e) {
                log.error("Erro ao indexar documento ID {}: {}", savedDoc.getId(), e.getMessage());
                // Não lançar exceção para não abortar o upload se a indexação falhar (opcional)
            }

            savedDoc.setPublicUrl(buildPublicUrl(fullStoragePath));
            return savedDoc;

        } catch (DataIntegrityViolationException e) {
            log.error("Erro de integridade ao salvar documento no banco. Título: {}", title, e);
            throw new DatabaseException("Erro de integridade no banco de dados.");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler bytes do arquivo para indexação", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<Document> findAll(Pageable pageable) {
        log.info("Buscando lista de documentos. Página: {}, Tamanho: {}", pageable.getPageNumber(),
                pageable.getPageSize());

        Page<Document> page = documentRepository.findAll(pageable);

        page.getContent().forEach(doc -> {
            String storagePath = doc.getMetaData().get("storage_path");
            if (storagePath != null && !storagePath.isBlank()) {
                doc.setPublicUrl(buildPublicUrl(storagePath));
            }
        });

        return page;
    }

    private DocumentType detectDocumentType(String contentType) {
        if (contentType == null) {
            log.warn("Tentativa de upload com Content-Type nulo.");
            throw new InvalidDocumentTypeException("Tipo do arquivo desconhecido/nulo.");
        }

        if (contentType.equals("application/pdf")) {
            return DocumentType.PDF;
        } else if (contentType.startsWith("image/")) {
            return DocumentType.IMAGE;
        } else {
            log.warn("Tentativa de upload de formato não suportado: {}", contentType);
            throw new InvalidDocumentTypeException(
                    "O formato do documento não é suportado. Apenas PDF e Imagens são permitidos.");
        }
    }

    private String validateAndExtractTitle(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new InvalidDocumentTitleException("O nome do arquivo é obrigatório.");
        }

        int lastDotIndex = filename.lastIndexOf(".");

        if (lastDotIndex == 0) {
            throw new InvalidDocumentTitleException(
                    "Nome de arquivo inválido. O arquivo não pode conter apenas a extensão (ex: '.pdf'). Renomeie o arquivo.");
        }

        String title;
        if (lastDotIndex == -1) {
            title = filename;
        } else {
            title = filename.substring(0, lastDotIndex);
        }

        if (title.isBlank()) {
            throw new InvalidDocumentTitleException("O título do documento não pode ser vazio.");
        }

        return title;
    }

    private String buildPublicUrl(String storagePath) {
        return String.format("%s/storage/v1/object/public/%s/%s",
                supabaseProjectUrl,
                bucketName,
                storagePath);
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        return filename.substring(lastDotIndex + 1);
    }

    private String generateStoragePath(String filename) {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%d/%02d/%s", now.getYear(), now.getMonthValue(), filename);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponseDTO> findBySectorAndCategory(Long sectorId, Long categoryId) {
        log.info("Buscando documentos - Setor: {}, Categoria: {}", sectorId, categoryId);

        List<Document> documents = documentRepository.findBySector_IdAndCategory_IdOrderByTitleAsc(sectorId,
                categoryId);

        documents.forEach(doc -> {
            String storagePath = doc.getMetaData().get("storage_path");
            if (storagePath != null && !storagePath.isBlank()) {
                doc.setPublicUrl(buildPublicUrl(storagePath));
            }
        });

        return documents.stream()
                .map(DocumentResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<Document> findBySector(Long sectorId, Pageable pageable) {
        log.info("Buscando documentos do Setor ID: [{}].", sectorId);

        Page<Document> page = documentRepository.findBySectorId(sectorId, pageable);

        page.getContent().forEach(doc -> {
            String storagePath = doc.getMetaData().get("storage_path");
            if (storagePath != null && !storagePath.isBlank()) {
                doc.setPublicUrl(buildPublicUrl(storagePath));
            }
        });

        return page;
    }

    @Transactional(readOnly = true)
    public List<Document> findAllById(List<Long> ids) {
        return documentRepository.findAllById(ids);
    }

    @Transactional(readOnly = true)
    public DownloadDocumentDTO downloadDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado."));

        String storagePath = document.getMetaData().get("storage_path");
        String originalFilename = document.getMetaData().get("original_filename");
        String contentType = document.getMetaData().get("content_type");

        long size = 0;
        if (document.getMetaData().containsKey("size_bytes")) {
            size = Long.parseLong(document.getMetaData().get("size_bytes"));
        }

        if (storagePath == null)
            throw new IllegalStateException("Caminho do arquivo não encontrado.");

        try {
            log.info("Abrindo stream de download do S3: {}", storagePath);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storagePath)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(getObjectRequest);

            if (size == 0) {
                size = s3Stream.response().contentLength();
            }

            return new DownloadDocumentDTO(originalFilename, contentType, s3Stream, size);

        } catch (Exception e) {
            log.error("Erro ao iniciar download do S3: {}", storagePath, e);
            throw new CloudStorageException("Erro ao conectar com armazenamento.", e);
        }
    }

    @Transactional(readOnly = true)
    public void downloadDocumentsAsZip(List<Long> documentIds, OutputStream outputStream) {
        log.info("Iniciando geração de ZIP para {} documentos...", documentIds.size());
        List<Document> documents = documentRepository.findAllById(documentIds);

        Set<String> usedPaths = new HashSet<>();

        try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {

            for (Document doc : documents) {
                String storagePath = doc.getMetaData().get("storage_path");
                String originalName = doc.getMetaData().getOrDefault("original_filename", "doc_" + doc.getId());

                String sectorName = doc.getSector() != null ? doc.getSector().getName() : "Sem Setor";
                String categoryName = doc.getCategory() != null ? doc.getCategory().getName() : "Sem Categoria";

                String safeSector = sanitizeFileName(sectorName);
                String safeCategory = sanitizeFileName(categoryName);
                String safeFilename = sanitizeFileName(originalName);

                String folderStructure = safeSector + "/" + safeCategory + "/";

                String fullPath = folderStructure + safeFilename;

                int counter = 1;
                while (usedPaths.contains(fullPath)) {
                    int dotIndex = safeFilename.lastIndexOf(".");
                    String newFileName;
                    if (dotIndex != -1) {
                        newFileName = safeFilename.substring(0, dotIndex) + " (" + counter + ")"
                                + safeFilename.substring(dotIndex);
                    } else {
                        newFileName = safeFilename + " (" + counter + ")";
                    }

                    fullPath = folderStructure + newFileName;
                    counter++;
                }
                usedPaths.add(fullPath);

                try {
                    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(storagePath)
                            .build();

                    try (var s3Stream = s3Client.getObject(getObjectRequest)) {

                        ZipEntry zipEntry = new ZipEntry(fullPath);
                        zipOut.putNextEntry(zipEntry);

                        StreamUtils.copy(s3Stream, zipOut);

                        zipOut.closeEntry();
                    }

                } catch (Exception e) {
                    log.error("Erro ao adicionar arquivo ID {} ao ZIP: {}", doc.getId(), e.getMessage());
                    zipOut.putNextEntry(new ZipEntry(folderStructure + "ERRO_" + doc.getId() + ".txt"));
                    zipOut.write(("Não foi possível baixar o arquivo original: " + originalName + ". Erro: "
                            + e.getMessage()).getBytes());
                    zipOut.closeEntry();
                }
            }

            log.info("ZIP gerado com sucesso.");

        } catch (Exception e) {
            log.error("Erro fatal ao gerar ZIP.", e);
            throw new RuntimeException("Erro ao gerar arquivo compactado.", e);
        }
    }

    private String sanitizeFileName(String input) {
        if (input == null)
            return "Desconhecido";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String safe = normalized.replaceAll("[^a-zA-Z0-9\\.\\-_ ]", "");
        return safe.trim();
    }
}