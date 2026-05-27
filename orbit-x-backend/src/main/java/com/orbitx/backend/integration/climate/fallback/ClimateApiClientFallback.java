package com.orbitx.backend.integration.climate.fallback;

import com.orbitx.backend.integration.climate.ClimateApiClient;
import com.orbitx.backend.integration.climate.dto.ClimateDataDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resilience fallback for ClimateApiClient.
 * Returns a neutral ClimateDataDto when the Climate Service is unreachable,
 * ensuring TelemetrySimulatorService continues operating with internal models.
 */
@Slf4j
@Component
public class ClimateApiClientFallback implements ClimateApiClient {

    @Override
    public ClimateDataDto getCurrentClimate(double latitude, double longitude) {
        log.warn("ClimateApiClient fallback triggered for lat={} lon={} — using neutral defaults",
                latitude, longitude);
        return ClimateDataDto.neutral();
    }
}
