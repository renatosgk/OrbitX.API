package com.orbitx.backend.domain.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ChatRequest(
        @NotBlank(message = "Message cannot be blank")
        @Size(max = 2000, message = "Message must be at most 2000 characters")
        String message,

        List<ChatMessage> history
) {}
