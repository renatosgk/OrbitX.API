package com.orbitx.backend.domain.auth.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record RegisterRequest(
        @NotBlank(message = "O nome da empresa é obrigatório")
        @Size(min = 2, max = 100, message = "O nome da empresa deve ter entre 2 e 100 caracteres")
        String companyName,
        @NotBlank(message = "O CNPJ é obrigatório")
        String taxId,
        @NotBlank(message = "O nome do administrador é obrigatório")
        @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
        String adminName,
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Informe um endereço de e-mail válido")
        String email,
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
        String password
) {}
