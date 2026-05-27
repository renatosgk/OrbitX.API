package com.orbitx.notification.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

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
