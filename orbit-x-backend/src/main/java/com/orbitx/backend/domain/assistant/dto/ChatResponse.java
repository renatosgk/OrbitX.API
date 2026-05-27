package com.orbitx.backend.domain.assistant.dto;

import java.time.Instant;
import java.util.List;

public record ChatResponse(
        String response,
        String model,
        int tokensUsed,
        List<ChatMessage> updatedHistory,
        Instant respondedAt
) {}
