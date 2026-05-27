package com.orbitx.backend.domain.dashboard.service;

import com.orbitx.backend.domain.dashboard.dto.AlertResponse;
import com.orbitx.backend.domain.dashboard.dto.KpiResponse;
import com.orbitx.backend.domain.dashboard.entity.enums.AlertSeverity;
import com.orbitx.backend.domain.dashboard.entity.enums.DatacenterStatus;
import com.orbitx.backend.domain.dashboard.repository.AlertRepository;
import com.orbitx.backend.messaging.dto.AlertEvent;
import com.orbitx.backend.messaging.publisher.AlertEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TelemetrySimulatorService telemetry;
    private final AiPredictionService       aiPrediction;
    private final AlertRepository           alertRepository;
    private final AlertEventPublisher       alertPublisher;

    public KpiResponse getKpis() {
        double temperature   = telemetry.generateCurrentTemperature();
        double energyKwh     = telemetry.generateEnergyConsumption();
        double carbonTons    = telemetry.generateCarbonEmission(energyKwh);
        double pue           = telemetry.generatePue();
        double overheatProb  = aiPrediction.calculateOverheatProbability(temperature);
        String recommendation     = aiPrediction.generateCoolingRecommendation(temperature, pue);
        String optimizationMode   = aiPrediction.resolveOptimizationMode(temperature, pue);
        DatacenterStatus status   = resolveStatus(temperature, pue, overheatProb);

        if (overheatProb >= 0.70) {
            publishThermalAlert(temperature, overheatProb, recommendation);
        }

        return new KpiResponse(
                BigDecimal.valueOf(energyKwh).setScale(2, RoundingMode.HALF_UP),
                temperature,
                BigDecimal.valueOf(carbonTons).setScale(4, RoundingMode.HALF_UP),
                pue,
                status,
                new KpiResponse.AiInsight(overheatProb, recommendation, optimizationMode),
                Instant.now()
        );
    }

    public List<AlertResponse> getActiveAlerts() {
        var dbAlerts = alertRepository.findByResolvedFalseOrderByCreatedAtDesc();
        if (!dbAlerts.isEmpty()) {
            return dbAlerts.stream().map(AlertResponse::from).toList();
        }
        return generateDynamicAlerts();
    }

    private void publishThermalAlert(double temp, double prob, String recommendation) {
        AlertEvent event = AlertEvent.of(
                "Risco Térmico Crítico Detectado",
                recommendation,
                AlertSeverity.CRITICAL,
                "AI_ENGINE",
                null,
                temp,
                prob
        );
        alertPublisher.publishThermalCritical(event);
        log.warn("Evento crítico térmico publicado — temp={}°C prob={}%", temp,
                Math.round(prob * 100));
    }

    private List<AlertResponse> generateDynamicAlerts() {
        double temp           = telemetry.generateCurrentTemperature();
        double pue            = telemetry.generatePue();
        String recommendation = aiPrediction.generateCoolingRecommendation(temp, pue);
        AlertSeverity sev     = temp >= 30.0 ? AlertSeverity.CRITICAL
                              : temp >= 26.0 ? AlertSeverity.HIGH
                              : AlertSeverity.MEDIUM;

        if (sev == AlertSeverity.CRITICAL || sev == AlertSeverity.HIGH) {
            alertPublisher.publishAlert(AlertEvent.of(
                    "Análise Térmica IA", recommendation, sev,
                    "AI_ENGINE", 1L, temp,
                    aiPrediction.calculateOverheatProbability(temp)
            ));
        }

        return List.of(
                new AlertResponse(1L, "Análise Térmica IA", recommendation,
                        sev, "AI_ENGINE", 1L, false, Instant.now()),
                new AlertResponse(2L, "Monitor de Eficiência PUE",
                        "PUE atual: %.3f — Linha de base: 1.200. Δ: +%.3f".formatted(pue, pue - 1.2),
                        AlertSeverity.LOW, "ENERGY_MONITOR", null, false,
                        Instant.now().minusSeconds(300)),
                new AlertResponse(3L, "Atualização de Emissão de Carbono",
                        "Mix de energia renovável em 63,4% neste ciclo. Compensação de carbono no prazo.",
                        AlertSeverity.LOW, "ESG_MODULE", null, false,
                        Instant.now().minusSeconds(900))
        );
    }

    private DatacenterStatus resolveStatus(double temp, double pue, double overheatProb) {
        if (overheatProb >= 0.70)       return DatacenterStatus.ONLINE;
        if (temp < 24.0 && pue < 1.30)  return DatacenterStatus.LOW_CARBON;
        if (pue < 1.35)                  return DatacenterStatus.AI_OPTIMIZED;
        if (temp < 27.0)                 return DatacenterStatus.COOLING_STABLE;
        return DatacenterStatus.ONLINE;
    }
}
