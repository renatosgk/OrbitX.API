package com.orbitx.backend.domain.dashboard.dto;

import com.orbitx.backend.domain.dashboard.entity.enums.DatacenterStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Real-time KPI payload for the main dashboard.
 * All numeric types are chosen to map cleanly to TypeScript:
 *   BigDecimal → string (use parseFloat on the frontend)
 *   double     → number
 *   Instant    → ISO-8601 string via Jackson
 */
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
