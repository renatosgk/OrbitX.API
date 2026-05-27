package com.orbitx.backend.domain.auth.dto;

import com.orbitx.backend.domain.auth.entity.User;
import com.orbitx.backend.domain.auth.entity.UserRole;

import java.time.Instant;

public record UserProfileDto(
        Long id,
        String name,
        String email,
        UserRole role,
        String companyName,
        String companyTaxId,
        String companyPlan,
        Instant createdAt,
        Instant lastLogin
) {
    public static UserProfileDto from(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCompany().getName(),
                user.getCompany().getTaxId(),
                user.getCompany().getPlan().name(),
                user.getCreatedAt(),
                user.getLastLogin()
        );
    }
}
