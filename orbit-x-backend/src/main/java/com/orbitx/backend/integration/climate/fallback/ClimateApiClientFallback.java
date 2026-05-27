package com.orbitx.backend.integration.climate.fallback;

import com.orbitx.backend.integration.climate.ClimateApiClient;
import com.orbitx.backend.integration.climate.dto.ClimateDataDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClimateApiClientFallback implements ClimateApiClient {

    @Override
    public ClimateDataDto getCurrentClimate(double latitude, double longitude) {
        log.warn("ClimateApiClient fallback ativado para lat={} lon={} — usando valores neutros padrão",
                latitude, longitude);
        return ClimateDataDto.neutral();
    }
}
