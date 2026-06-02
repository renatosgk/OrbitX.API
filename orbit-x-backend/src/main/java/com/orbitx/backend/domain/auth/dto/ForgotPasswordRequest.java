package com.orbitx.backend.domain.auth.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
public record ForgotPasswordRequest(
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Informe um endereço de e-mail válido")
        String email
) {}
