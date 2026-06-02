package com.orbitx.backend.domain.infrastructure.dto;
import com.orbitx.backend.domain.infrastructure.entity.enums.DataLinkStatus;
import java.time.Instant;
public record SatelliteResponse(
        Long id,
        String name,
        String orbitType,
        double altitudeKm,
        LocationDto currentPosition,
        double speedKmh,
        DataLinkStatus dataLinkStatus,
        double signalStrengthPercent,
        Instant positionCapturedAt
) {}
