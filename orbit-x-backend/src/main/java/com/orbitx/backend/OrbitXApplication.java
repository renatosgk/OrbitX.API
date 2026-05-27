package com.orbitx.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Exclui o auto-configure de Embedding da OpenAI/Groq pois:
 *  - Groq não possui endpoint de embeddings
 *  - Nosso RAG usa KnowledgeBaseConfig (busca estática por keywords)
 *  - Sem a exclusão, o Spring AI tentaria inicializar EmbeddingModel e falharia
 */
@SpringBootApplication(exclude = {
        org.springframework.ai.autoconfigure.openai.OpenAiEmbeddingAutoConfiguration.class
})
@EnableScheduling
@EnableCaching
@EnableFeignClients
public class OrbitXApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrbitXApplication.class, args);
    }
}
