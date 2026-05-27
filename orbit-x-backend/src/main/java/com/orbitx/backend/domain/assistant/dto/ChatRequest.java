package com.orbitx.backend.domain.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ChatRequest(
        @NotBlank(message = "A mensagem não pode estar em branco")
        @Size(max = 2000, message = "A mensagem deve ter no máximo 2000 caracteres")
        String message,

        List<ChatMessage> history
) {}
