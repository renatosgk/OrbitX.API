package com.orbitx.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * spring.ai.openai.embedding.enabled=false (application.yml) desabilita o
 * EmbeddingModel do Spring AI pois Groq não possui endpoint de embeddings.
 * Nosso RAG usa KnowledgeBaseConfig com busca estática por keywords.
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableFeignClients
public class OrbitXApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrbitXApplication.class, args);
    }
}
