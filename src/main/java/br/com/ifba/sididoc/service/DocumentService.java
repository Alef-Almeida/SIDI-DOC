package br.com.ifba.sididoc.service;

import br.com.ifba.sididoc.entity.Document;
import br.com.ifba.sididoc.enums.DocumentType;
import br.com.ifba.sididoc.enums.ProcessingStatus;
import br.com.ifba.sididoc.exception.CloudStorageException;
import br.com.ifba.sididoc.exception.DatabaseException;
import br.com.ifba.sididoc.exception.InvalidDocumentTitleException;
import br.com.ifba.sididoc.exception.InvalidDocumentTypeException;
import br.com.ifba.sididoc.repository.DocumentRepository;
import br.com.ifba.sididoc.web.dto.DocumentResponseDTO;
import br.com.ifba.sididoc.web.dto.UploadDocumentDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class DocumentService {

    @Autowired
    private ExtratorService extratorService;
    private final DocumentRepository documentRepository;
    private final S3Client s3Client;

    @Value("${supabase.bucket.name}")
    private String bucketName;

    @Value("${supabase.project.url}")
    private String supabaseProjectUrl;

    @Transactional
    public DocumentResponseDTO uploadDocument(UploadDocumentDTO dto) throws IOException, TesseractException {
        MultipartFile file = dto.file();
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        String textoExtraido = extratorService.extrair(file);
        long size = file.getSize();

        log.info("Iniciando processamento de upload. Arquivo: [{}], Tipo: [{}], Tamanho: [{} bytes]", originalFilename, contentType, size);

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
        document.setTitle(title);
        document.setExtractedText(textoExtraido);
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
            String publicUrl = buildPublicUrl(fullStoragePath);

            return DocumentResponseDTO.fromEntity(savedDoc, publicUrl);

        } catch (DataIntegrityViolationException e) {
            log.error("Erro de integridade ao salvar documento no banco. Título: {}", title, e);
            throw new DatabaseException("Erro de integridade no banco de dados.");
        }
    }

    @Transactional(readOnly = true)
    public Page<DocumentResponseDTO> findAll(Pageable pageable) {
        log.info("Buscando lista de documentos. Página: {}, Tamanho: {}", pageable.getPageNumber(), pageable.getPageSize());

        return documentRepository.findAll(pageable)
                .map(document -> {
                    String storagePath = document.getMetaData().get("storage_path");

                    String publicUrl = null;
                    if (storagePath != null && !storagePath.isBlank()) {
                        publicUrl = buildPublicUrl(storagePath);
                    }

                    return DocumentResponseDTO.fromEntity(document, publicUrl);
                });
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
            throw new InvalidDocumentTypeException("O formato do documento não é suportado. Apenas PDF e Imagens são permitidos.");
        }
    }

    private String validateAndExtractTitle(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new InvalidDocumentTitleException("O nome do arquivo é obrigatório.");
        }

        int lastDotIndex = filename.lastIndexOf(".");

        if (lastDotIndex == 0) {
            throw new InvalidDocumentTitleException("Nome de arquivo inválido. O arquivo não pode conter apenas a extensão (ex: '.pdf'). Renomeie o arquivo.");
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

    public List<Document> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Limpeza básica: remove caracteres especiais que quebram a query do banco
        // Mantém apenas letras, números e espaços (ajuste conforme necessidade)
        String termoLimpo = query.replaceAll("[^a-zA-Z0-9À-ÿ\\s]", "");

        // 2. Formatação para o Postgres: troca espaços por " & "
        // Ex: "nota fiscal" vira "nota & fiscal"
        String termoFormatado = termoLimpo.trim().replaceAll("\\s+", " & ");

        return documentRepository.searchByTerm(termoFormatado);
    }
}