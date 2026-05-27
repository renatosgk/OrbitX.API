package com.orbitx.backend.domain.infrastructure.dto;

import com.orbitx.backend.domain.infrastructure.entity.Datacenter;
import com.orbitx.backend.domain.infrastructure.entity.enums.ThermalState;

import java.math.BigDecimal;

public record DatacenterResponse(
        Long id,
        String name,
        String city,
        String country,
        LocationDto location,
        ThermalState thermalState,
        double currentTemperatureCelsius,
        BigDecimal regionalConsumptionKwh,
        Integer capacityServers
) {
    public static DatacenterResponse from(Datacenter dc, double temperature) {
        return new DatacenterResponse(
                dc.getId(),
                dc.getName(),
                dc.getCity(),
                dc.getCountry(),
                new LocationDto(dc.getLatitude(), dc.getLongitude()),
                dc.getThermalState(),
                temperature,
                dc.getRegionalConsumptionKwh(),
                dc.getCapacityServers()
        );
    }
}
