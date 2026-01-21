package br.com.ifba.sididoc.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class LangChainConfig {

    @Value("${langchain4j.gemini.api-key}")
    private String geminiApiKey;

    @Value("${langchain4j.vector-store.table}")
    private String vectorTable;

    @Value("${langchain4j.vector-store.dimension}")
    private int vectorDimension;

    @Value("${langchain4j.gemini.model-name}")
    private String embeddingName;

    @Value("${langchain4j.gemini.chat-model-name}")
    private String chatModelName;

    @Bean
    public EmbeddingModel embeddingModel() {
        return GoogleAiEmbeddingModel.builder()
                .apiKey(geminiApiKey)
                .modelName(embeddingName)
                .timeout(java.time.Duration.ofSeconds(120))
                .build();
    }

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiApiKey)
                .modelName(chatModelName)
                .timeout(java.time.Duration.ofSeconds(120))
                .logRequestsAndResponses(true)
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(DataSource dataSource) {
        return PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(dataSource)
                .table(vectorTable)
                .dimension(vectorDimension)

                .build();
    }
}
