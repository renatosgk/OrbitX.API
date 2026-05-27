package com.orbitx.backend.domain.assistant.dto;

public record ChatMessage(
        String role,     // "user" | "assistant"
        String content
) {}
