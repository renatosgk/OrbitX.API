package com.orbitx.backend.integration.climate;

import com.orbitx.backend.integration.climate.dto.ClimateDataDto;
import com.orbitx.backend.integration.climate.fallback.ClimateApiClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name  = "climate-api",
        url   = "${integration.climate-api.url:http://localhost:8090}",
        fallback = ClimateApiClientFallback.class
)
public interface ClimateApiClient {

    @GetMapping("/api/v1/climate/current")
    ClimateDataDto getCurrentClimate(
            @RequestParam("lat") double latitude,
            @RequestParam("lon") double longitude
    );
}
