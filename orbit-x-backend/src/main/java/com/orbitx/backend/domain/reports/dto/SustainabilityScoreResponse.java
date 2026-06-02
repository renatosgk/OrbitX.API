package com.orbitx.backend.domain.reports.dto;
import java.math.BigDecimal;
import java.time.Instant;
public record SustainabilityScoreResponse(
        int esgScore,
        String esgGrade,
        BigDecimal energySavedKwhAccumulated,
        BigDecimal carbonOffsetTons,
        double renewableEnergyPercent,
        Comparison beforeOrbitX,
        Comparison afterOrbitX,
        Instant calculatedAt
) {
    public record Comparison(
            double averagePue,
            BigDecimal averageEnergyKwh,
            BigDecimal carbonEmissionTons,
            double coolingEfficiencyPercent
    ) {}
}
