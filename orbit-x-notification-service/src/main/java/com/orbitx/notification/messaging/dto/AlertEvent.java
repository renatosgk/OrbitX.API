package com.orbitx.notification.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

/**
 * Mirror of the AlertEvent published by orbit-x-backend.
 * Kept in sync via shared schema convention (not a shared library, to preserve service autonomy).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AlertEvent(
        String eventId,
        String title,
        String message,
        String severity,
        String sourceComponent,
        Long datacenterId,
        double currentTemperatureCelsius,
        double overheatProbability,
        Instant occurredAt
) {}
