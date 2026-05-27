package com.orbitx.backend.domain.assistant.port;

import com.orbitx.backend.domain.assistant.dto.ChatRequest;
import com.orbitx.backend.domain.assistant.dto.ChatResponse;

/**
 * Port (interface) for the assistant domain.
 * Allows two implementations to coexist:
 *  - SpringAiAssistantService  — active when OPENAI_API_KEY is configured
 *  - KeywordAssistantService   — fallback for dev/offline environments
 */
public interface AssistantPort {
    ChatResponse chat(ChatRequest request);
}
