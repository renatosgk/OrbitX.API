package com.orbitx.backend.domain.assistant.port;

import com.orbitx.backend.domain.assistant.dto.ChatRequest;
import com.orbitx.backend.domain.assistant.dto.ChatResponse;

public interface AssistantPort {
    ChatResponse chat(ChatRequest request);
}
