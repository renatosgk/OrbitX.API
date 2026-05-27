package com.orbitx.backend.integration.climate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Simplified response from the external Climate Data API.
 * Used to adjust datacenter temperature simulations with real ambient conditions.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClimateDataDto(
        String city,
        double ambientTemperatureCelsius,
        double humidity,
        String condition,    // "CLEAR", "CLOUDY", "RAIN", "STORM"
        double coolingLoadFactor  // 0.8–1.4 multiplier for datacenter cooling demand
) {
    /** Safe fallback when the climate API is unavailable */
    public static ClimateDataDto neutral() {
        return new ClimateDataDto("UNKNOWN", 20.0, 50.0, "CLEAR", 1.0);
    }
}
