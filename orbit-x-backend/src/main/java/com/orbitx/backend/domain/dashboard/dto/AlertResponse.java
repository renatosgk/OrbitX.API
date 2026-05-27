package com.orbitx.backend.domain.dashboard.dto;

import com.orbitx.backend.domain.dashboard.entity.Alert;
import com.orbitx.backend.domain.dashboard.entity.enums.AlertSeverity;

import java.time.Instant;

public record AlertResponse(
        Long id,
        String title,
        String message,
        AlertSeverity severity,
        String sourceComponent,
        Long datacenterId,
        boolean resolved,
        Instant createdAt
) {
    public static AlertResponse from(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getTitle(),
                alert.getMessage(),
                alert.getSeverity(),
                alert.getSourceComponent(),
                alert.getDatacenterId(),
                alert.isResolved(),
                alert.getCreatedAt()
        );
    }
}
