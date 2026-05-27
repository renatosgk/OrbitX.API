package com.orbitx.backend.domain.dashboard.dto;

import com.orbitx.backend.domain.dashboard.entity.enums.DatacenterStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record KpiResponse(
        BigDecimal energyConsumptionKwh,
        double currentTemperatureCelsius,
        BigDecimal carbonEmissionTons,
        double powerUsageEffectiveness,
        DatacenterStatus overallStatus,
        AiInsight aiInsight,
        Instant capturedAt
) {
    public record AiInsight(
            double overheatProbability,
            String recommendation,
            String optimizationMode
    ) {}
}
