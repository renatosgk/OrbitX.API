package com.orbitx.backend.domain.assistant.dto;

public record ChatMessage(
        String role,     
        String content
) {}
