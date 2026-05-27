package com.orbitx.backend.domain.dashboard.service;

import com.orbitx.backend.domain.dashboard.entity.Alert;
import com.orbitx.backend.domain.dashboard.entity.enums.AlertSeverity;
import com.orbitx.backend.domain.dashboard.repository.AlertRepository;
import com.orbitx.backend.messaging.publisher.AlertEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock TelemetrySimulatorService telemetry;
    @Mock AiPredictionService       aiPrediction;
    @Mock AlertRepository           alertRepository;
    @Mock AlertEventPublisher       alertPublisher;

    @InjectMocks DashboardService dashboardService;

    @Test
    void getKpis_returnsWellFormedResponse() {
        stubTelemetry(24.0, 2000.0, 0.105, 1.28);
        when(aiPrediction.calculateOverheatProbability(24.0)).thenReturn(0.20);
        when(aiPrediction.generateCoolingRecommendation(24.0, 1.28)).thenReturn("STABLE: all clear");
        when(aiPrediction.resolveOptimizationMode(24.0, 1.28)).thenReturn("GREEN_MODE");

        var kpis = dashboardService.getKpis();

        assertThat(kpis).isNotNull();
        assertThat(kpis.currentTemperatureCelsius()).isEqualTo(24.0);
        assertThat(kpis.energyConsumptionKwh()).isNotNull();
        assertThat(kpis.aiInsight().optimizationMode()).isEqualTo("GREEN_MODE");
    }

    @Test
    void getKpis_publishesThermalAlertWhenOverheatProbabilityMeetsThreshold() {
        stubTelemetry(31.0, 2100.0, 0.15, 1.45);
        when(aiPrediction.calculateOverheatProbability(31.0)).thenReturn(0.97);
        when(aiPrediction.generateCoolingRecommendation(31.0, 1.45)).thenReturn("CRITICAL: emergency");
        when(aiPrediction.resolveOptimizationMode(31.0, 1.45)).thenReturn("TURBO_COOLING");

        dashboardService.getKpis();

        verify(alertPublisher).publishThermalCritical(any());
    }

    @Test
    void getKpis_doesNotPublishAlertWhenTemperatureIsSafe() {
        stubTelemetry(20.0, 1900.0, 0.08, 1.22);
        when(aiPrediction.calculateOverheatProbability(20.0)).thenReturn(0.05);
        when(aiPrediction.generateCoolingRecommendation(20.0, 1.22)).thenReturn("STABLE");
        when(aiPrediction.resolveOptimizationMode(20.0, 1.22)).thenReturn("GREEN_MODE");

        dashboardService.getKpis();

        verifyNoInteractions(alertPublisher);
    }

    @Test
    void getActiveAlerts_returnsDatabaseResultsWhenPresent() {
        Alert stored = Alert.builder()
                .id(42L).title("Rack Overheat").message("Rack 07A above threshold")
                .severity(AlertSeverity.HIGH).resolved(false).build();
        when(alertRepository.findByResolvedFalseOrderByCreatedAtDesc()).thenReturn(List.of(stored));

        var alerts = dashboardService.getActiveAlerts();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).title()).isEqualTo("Rack Overheat");
        assertThat(alerts.get(0).id()).isEqualTo(42L);
    }

    @Test
    void getActiveAlerts_generatesDynamicAlertsWhenDatabaseIsEmpty() {
        when(alertRepository.findByResolvedFalseOrderByCreatedAtDesc()).thenReturn(List.of());
        when(telemetry.generateCurrentTemperature()).thenReturn(24.0);
        when(telemetry.generatePue()).thenReturn(1.28);
        when(aiPrediction.generateCoolingRecommendation(24.0, 1.28)).thenReturn("STABLE");
        when(aiPrediction.calculateOverheatProbability(24.0)).thenReturn(0.20);

        var alerts = dashboardService.getActiveAlerts();

        assertThat(alerts).isNotEmpty();
    }

    private void stubTelemetry(double temp, double energy, double carbon, double pue) {
        when(telemetry.generateCurrentTemperature()).thenReturn(temp);
        when(telemetry.generateEnergyConsumption()).thenReturn(energy);
        when(telemetry.generateCarbonEmission(energy)).thenReturn(carbon);
        when(telemetry.generatePue()).thenReturn(pue);
    }
}
