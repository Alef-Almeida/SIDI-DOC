package br.com.ifba.sididoc.service;

import br.com.ifba.sididoc.entity.Document;
import br.com.ifba.sididoc.repository.DocumentRepository;
import br.com.ifba.sididoc.exception.CloudStorageException;
import br.com.ifba.sididoc.exception.ResourceNotFoundException;
import br.com.ifba.sididoc.web.dto.DocumentExportDTO; // Import atualizado
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentExportService {

    private final DocumentRepository documentRepository;
    private final S3Client s3Client;

    @Value("${supabase.bucket.name}")
    private String bucketName;

    @Transactional(readOnly = true)
    public DocumentExportDTO exportDocument(Long documentId) {
        log.info("Iniciando exportação do documento ID: {}", documentId);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado com ID: " + documentId));

        return downloadFromStorage(document);
    }

    @Transactional(readOnly = true)
    public DocumentExportDTO exportDocumentsAsZip(List<Long> documentIds) {
        log.info("Iniciando exportação ZIP para {} documentos", documentIds.size());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            Set<String> usedFilenames = new HashSet<>();

            for (Long id : documentIds) {
                try {
                    Document document = documentRepository.findById(id).orElse(null);

                    if (document == null) {
                        log.warn("Documento ID {} não encontrado. Pulando...", id);
                        continue;
                    }

                    // Obtém o DTO do arquivo individual
                    DocumentExportDTO fileDto = downloadFromStorage(document);

                    String entryName = fileDto.filename();
                    int count = 1;
                    while (usedFilenames.contains(entryName)) {
                        if (entryName.contains(".")) {
                            int dotIndex = entryName.lastIndexOf(".");
                            String name = entryName.substring(0, dotIndex);
                            String ext = entryName.substring(dotIndex);
                            entryName = name + "(" + count + ")" + ext;
                        } else {
                            entryName = entryName + "(" + count + ")";
                        }
                        count++;
                    }
                    usedFilenames.add(entryName);

                    ZipEntry entry = new ZipEntry(entryName);
                    zos.putNextEntry(entry);
                    zos.write(fileDto.data());
                    zos.closeEntry();

                } catch (Exception e) {
                    log.error("Erro ao processar documento ID {} para o ZIP", id, e);
                }
            }

            zos.finish();

            // Retorna o DTO com o binário do ZIP
            return new DocumentExportDTO(
                    baos.toByteArray(),
                    "documentos_exportados.zip",
                    "application/zip"
            );

        } catch (IOException e) {
            throw new CloudStorageException("Erro fatal ao criar arquivo ZIP.", e);
        }
    }

    //Método auxiliar para buscar no S3.
    private DocumentExportDTO downloadFromStorage(Document document) {

        String storagePath = document.getMetaData().get("storage_path");
        String originalFilename = document.getMetaData().get("original_filename");
        String contentType = document.getMetaData().get("content_type");

        if (storagePath == null) {
            throw new CloudStorageException("Metadado 'storage_path' ausente no documento " + document.getId());
        }

        String finalKey = extractKey(storagePath);
        log.info("Download S3 -> Path Original: [{}], Key Limpa: [{}]", storagePath, finalKey);

        if (originalFilename == null) originalFilename = "doc_" + document.getId() + ".pdf";
        if (contentType == null) contentType = "application/pdf";

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(finalKey)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
            byte[] data = objectBytes.asByteArray();

            return new DocumentExportDTO(data, originalFilename, contentType);

        } catch (Exception e) {
            log.error("Erro S3 ao baixar key: {}", storagePath, e);
            throw new CloudStorageException("Falha na comunicação com o Storage.", e);
        }
    }

    private String extractKey(String path) {
        if (path.contains(bucketName + "/")) {
            return path.substring(path.indexOf(bucketName + "/") + bucketName.length() + 1);
        }
        return path;
    }
}