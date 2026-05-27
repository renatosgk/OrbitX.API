package com.orbitx.backend.messaging.dto;

import com.orbitx.backend.domain.dashboard.entity.enums.AlertSeverity;

import java.time.Instant;

public record AlertEvent(
        String eventId,
        String title,
        String message,
        AlertSeverity severity,
        String sourceComponent,
        Long datacenterId,
        double currentTemperatureCelsius,
        double overheatProbability,
        Instant occurredAt
) {
    public static AlertEvent of(String title, String message, AlertSeverity severity,
                                 String source, Long datacenterId,
                                 double temp, double overheatProb) {
        return new AlertEvent(
                "EVT-" + System.currentTimeMillis(),
                title, message, severity, source, datacenterId,
                temp, overheatProb, Instant.now()
        );
    }
}
