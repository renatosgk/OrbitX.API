package com.orbitx.backend.domain.assistant.service;

import com.orbitx.backend.domain.assistant.dto.ChatMessage;
import com.orbitx.backend.domain.assistant.dto.ChatRequest;
import com.orbitx.backend.domain.assistant.dto.ChatResponse;
import com.orbitx.backend.domain.assistant.port.AssistantPort;
import com.orbitx.backend.domain.assistant.rag.KnowledgeBaseConfig;
import com.orbitx.backend.domain.assistant.tools.DatacenterTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring AI assistant usando Groq (llama-3.3-70b-versatile via endpoint OpenAI-compatible).
 *
 * Padrões ativos:
 *  ┌─────────────────────────────────────────────────────────────────────┐
 *  │  RAG (Retrieval-Augmented Generation)                               │
 *  │    → KnowledgeBaseConfig.getRelevantContext() faz busca por         │
 *  │      palavras-chave nos chunks de domínio e injeta no system prompt │
 *  │      (sem VectorStore — Groq não suporta embeddings)               │
 *  │                                                                     │
 *  │  Tool Calling (@Tool)                                               │
 *  │    → O modelo decide autonomamente quando chamar getLiveKpis(),     │
 *  │      getDatacenterStatus(), getActiveAlerts(), getSustainabilityMetrics() │
 *  │                                                                     │
 *  │  MCP (Model Context Protocol)                                       │
 *  │    → System prompt estruturado com contexto do ecossistema          │
 *  │      Orbit X, regras de domínio e instruções de comportamento       │
 *  └─────────────────────────────────────────────────────────────────────┘
 *
 * Ativo quando: spring.ai.openai.api-key != "CONFIGURE_ME" (ou seja,
 * quando a chave Groq estiver configurada, que é o padrão atual).
 */
@Slf4j
@Service
@ConditionalOnExpression("'${spring.ai.openai.api-key:}'.startsWith('gsk_')")
public class SpringAiAssistantService implements AssistantPort {

    private static final String MODEL_ID        = "orbit-x-ai-groq-llama3.3";
    private static final int    MAX_HISTORY_SIZE = 20;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            Você é o Orbit X AI, assistente especialista em monitoramento inteligente de datacenters sustentáveis.

            DOMÍNIO DE CONHECIMENTO:
            {rag_context}

            REGRAS DE COMPORTAMENTO:
            - Sempre use as tools disponíveis para buscar dados AO VIVO antes de responder métricas
            - Seja preciso com números; indique a fonte (Live KPIs, ESG Score, etc.)
            - Responda no mesmo idioma do usuário (Português ou Inglês)
            - Formate respostas com seções claras quando pertinente
            - Nunca invente valores de sensores — se não souber, use as tools

            TOOLS DISPONÍVEIS (chame quando necessário):
            - getLiveKpis() — temperatura, energia, PUE, carbon, status atual
            - getDatacenterStatus() — estado térmico de todos os datacenters
            - getActiveAlerts() — alertas não resolvidos do motor de IA
            - getSustainabilityMetrics() — score ESG, carbon offset, comparativo
            """;

    private final ChatClient       chatClient;
    private final KnowledgeBaseConfig knowledgeBase;
    private final DatacenterTools  datacenterTools;

    public SpringAiAssistantService(ChatClient.Builder chatClientBuilder,
                                    KnowledgeBaseConfig knowledgeBase,
                                    DatacenterTools datacenterTools) {
        this.chatClient      = chatClientBuilder.build();
        this.knowledgeBase   = knowledgeBase;
        this.datacenterTools = datacenterTools;
        log.info("SpringAiAssistantService ativo — Groq / llama-3.3-70b-versatile + RAG + Tool Calling");
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        // ── 1. RAG: busca contexto relevante por keyword matching ──────────
        String ragContext   = knowledgeBase.getRelevantContext(request.message());
        String systemPrompt = SYSTEM_PROMPT_TEMPLATE.replace("{rag_context}", ragContext);

        // ── 2. Monta histórico de mensagens para multi-turn conversation ───
        List<Message> history = buildMessageHistory(request.history());

        log.debug("Groq chat — model=llama-3.3-70b msg='{}' historySize={}",
                request.message(), history.size());

        // ── 3. Chama Groq via Spring AI ChatClient com Tool Calling ────────
        String response = chatClient.prompt()
                .system(systemPrompt)
                .messages(history)
                .user(request.message())
                .tools(datacenterTools)          // Tool calling — LLM decide quando invocar
                .call()
                .content();

        List<ChatMessage> updatedHistory = buildUpdatedHistory(request, response);

        return new ChatResponse(
                response,
                MODEL_ID,
                estimateTokens(request.message() + response),
                updatedHistory,
                Instant.now()
        );
    }

    private List<Message> buildMessageHistory(List<ChatMessage> history) {
        if (history == null || history.isEmpty()) return List.of();
        return history.stream()
                .map(msg -> switch (msg.role()) {
                    case "user"      -> (Message) new UserMessage(msg.content());
                    case "assistant" -> (Message) new AssistantMessage(msg.content());
                    default          -> (Message) new UserMessage(msg.content());
                })
                .toList();
    }

    private List<ChatMessage> buildUpdatedHistory(ChatRequest request, String response) {
        List<ChatMessage> history = new ArrayList<>();
        if (request.history() != null) history.addAll(request.history());
        history.add(new ChatMessage("user", request.message()));
        history.add(new ChatMessage("assistant", response));
        if (history.size() > MAX_HISTORY_SIZE) {
            history = history.subList(history.size() - MAX_HISTORY_SIZE, history.size());
        }
        return history;
    }

    private int estimateTokens(String text) {
        return (int) Math.ceil(text.length() / 4.0);
    }
}
