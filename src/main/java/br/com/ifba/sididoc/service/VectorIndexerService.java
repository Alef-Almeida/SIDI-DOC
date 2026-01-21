package br.com.ifba.sididoc.service;

import br.com.ifba.sididoc.entity.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorIndexerService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ChatLanguageModel chatLanguageModel;

    public void indexDocument(Document doc, byte[] content) {
        log.info("Iniciando indexação vetorial para o documento: {} (ID: {})", doc.getTitle(), doc.getId());

        try {
            // 1. Extração de Texto (Ingestão)
            DocumentParser parser = new ApacheTikaDocumentParser();
            dev.langchain4j.data.document.Document langchainDoc = parser.parse(new ByteArrayInputStream(content));

            // 2. Estratégia de Splitting (Recursivo)
            // Chunks de 1000 caracteres (com overlap de 150)
            dev.langchain4j.data.document.DocumentSplitter splitter = DocumentSplitters.recursive(1000, 150);
            List<TextSegment> segments = splitter.split(langchainDoc);

            if (segments.isEmpty()) {
                log.warn("Nenhum segmento de texto extraído do documento: {}", doc.getTitle());
                return;
            }

            // 3. Enriquecimento de Metadados
            String storagePath = doc.getMetaData().get("storage_path");
            String originalFilename = doc.getMetaData().get("original_filename");
            String docId = String.valueOf(doc.getId());

            for (TextSegment segment : segments) {
                if (storagePath != null)
                    segment.metadata().put("file_key", storagePath);
                if (originalFilename != null)
                    segment.metadata().put("file_name", originalFilename);

                segment.metadata().put("document_id", docId);
                segment.metadata().put("content_type", doc.getMetaData().getOrDefault("content_type", "unknown"));

                String sizeBytes = doc.getMetaData().get("size_bytes");
                if (sizeBytes != null) {
                    segment.metadata().put("file_size", sizeBytes);
                }

                if (doc.getUploadDate() != null) {
                    segment.metadata().put("upload_date", doc.getUploadDate().toString());
                }
            }

            // 4. Geração de Embeddings e Persistência
            log.debug("Gerando embeddings para {} segmentos...", segments.size());
            embeddingStore.addAll(embeddingModel.embedAll(segments).content(), segments);

            log.info("Indexação concluída com sucesso. {} segmentos armazenados para o documento: {}", segments.size(),
                    doc.getTitle());

        } catch (Exception e) {
            log.error("Erro crítico ao indexar documento: {}", doc.getTitle(), e);
            throw new RuntimeException("Falha na indexação vetorial: " + e.getMessage(), e);
        }
    }

    public List<dev.langchain4j.store.embedding.EmbeddingMatch<TextSegment>> search(String query) {
        log.info("Busca vetorial iniciada para query: {}", query);
        try {
            dev.langchain4j.data.embedding.Embedding queryEmbedding = embeddingModel.embed(query).content();
            // Retorna os 3 resultados mais relevantes com score mínimo de 0.5 (ajustável)
            return embeddingStore.findRelevant(queryEmbedding, 10, 0.68);
        } catch (Exception e) {
            log.error("Erro ao realizar busca vetorial: {}", e.getMessage(), e);
            throw new RuntimeException("Falha na busca vetorial", e);
        }
    }

    public List<dev.langchain4j.store.embedding.EmbeddingMatch<TextSegment>> searchByImage(byte[] imageBytes,
            String mimeType) {
        log.info("Iniciando busca vetorial por imagem. Tamanho: {} bytes, MimeType: {}",
                imageBytes != null ? imageBytes.length : 0, mimeType);
        try {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            UserMessage userMessage = UserMessage.from(
                    TextContent.from(
                            "Analise a imagem e extraia uma transcrição simples, objetiva e direta. Liste apenas as palavras-chave, textos visíveis e conceitos técnicos principais presentes na imagem para otimizar a busca vetorial."),
                    ImageContent.from(base64Image, mimeType));

            Response<AiMessage> response = chatLanguageModel.generate(userMessage);
            String transcription = response.content().text();

            log.info("Transcrição da imagem gerada: {}", transcription);

            return search(transcription);

        } catch (Exception e) {
            log.error("Erro ao realizar busca por imagem: {}", e.getMessage(), e);
            if (e.getCause() != null) {
                log.error("Causa raiz: {}", e.getCause().getMessage(), e.getCause());
            }
            throw new RuntimeException("Falha na busca por imagem: " + e.getMessage(), e);
        }
    }
}
