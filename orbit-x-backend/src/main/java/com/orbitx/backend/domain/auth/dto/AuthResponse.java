package com.orbitx.backend.domain.auth.dto;
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String sessionStatus,
        UserProfileDto user
) {
    public static AuthResponse of(String token, long expiresInMillis, UserProfileDto user) {
        return new AuthResponse(token, "Bearer", expiresInMillis / 1000L, "ACTIVE", user);
    }
}
