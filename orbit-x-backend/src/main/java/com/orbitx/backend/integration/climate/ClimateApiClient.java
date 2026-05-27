package com.orbitx.backend.integration.climate;

import com.orbitx.backend.integration.climate.dto.ClimateDataDto;
import com.orbitx.backend.integration.climate.fallback.ClimateApiClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for the external Orbit X Climate Data microservice.
 *
 * Why Feign over RestTemplate / WebClient?
 *  - Declarative interface eliminates boilerplate HTTP code.
 *  - Native integration with Spring Cloud LoadBalancer for service-discovery routing.
 *  - Built-in support for circuit breaking via Resilience4j / Hystrix.
 *  - Feign interceptors allow transparent JWT propagation between microservices.
 *
 * In production: replace `url` with the service name registered in Eureka/Consul:
 *   @FeignClient(name = "climate-service")
 */
@FeignClient(
        name  = "climate-api",
        url   = "${integration.climate-api.url:http://localhost:8090}",
        fallback = ClimateApiClientFallback.class
)
public interface ClimateApiClient {

    /**
     * Fetch current ambient climate conditions for a geographic location.
     * Used by TelemetrySimulatorService to bias temperature models.
     */
    @GetMapping("/api/v1/climate/current")
    ClimateDataDto getCurrentClimate(
            @RequestParam("lat") double latitude,
            @RequestParam("lon") double longitude
    );
}
