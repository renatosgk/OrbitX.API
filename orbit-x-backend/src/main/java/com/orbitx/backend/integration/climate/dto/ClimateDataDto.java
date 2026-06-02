package com.orbitx.backend.integration.climate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClimateDataDto(
        String city,
        double ambientTemperatureCelsius,
        double humidity,
        String condition,    
        double coolingLoadFactor  
) {

    public static ClimateDataDto neutral() {
        return new ClimateDataDto("UNKNOWN", 20.0, 50.0, "CLEAR", 1.0);
    }
}
