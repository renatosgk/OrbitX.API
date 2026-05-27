package com.orbitx.backend.domain.dashboard.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class AiPredictionServiceTest {

    private final AiPredictionService service = new AiPredictionService();

    @Test
    void calculateOverheatProbability_returnsHighProbabilityAtCriticalTemp() {
        assertThat(service.calculateOverheatProbability(31.0)).isEqualTo(0.97);
    }

    @Test
    void calculateOverheatProbability_returnsLowProbabilityBelowWarningThreshold() {
        assertThat(service.calculateOverheatProbability(18.0)).isLessThan(0.30);
    }

    @ParameterizedTest
    @CsvSource({
            "26.0, 0.30",
            "28.0, 0.60",
            "30.0, 0.90"
    })
    void calculateOverheatProbability_scalesWithTemperatureInWarningZone(
            double temp, double expectedMin) {
        double prob = service.calculateOverheatProbability(temp);
        assertThat(prob).isGreaterThanOrEqualTo(expectedMin - 0.05);
    }

    @Test
    void resolveOptimizationMode_returnsTurboCoolingAtCriticalTemp() {
        assertThat(service.resolveOptimizationMode(31.0, 1.30)).isEqualTo("TURBO_COOLING");
    }

    @Test
    void resolveOptimizationMode_returnsGreenModeUnderOptimalConditions() {
        assertThat(service.resolveOptimizationMode(20.0, 1.20)).isEqualTo("GREEN_MODE");
    }

    @Test
    void generateCoolingRecommendation_containsCriticalKeywordAtHighTemp() {
        String rec = service.generateCoolingRecommendation(31.0, 1.30);
        assertThat(rec).containsIgnoringCase("CRITICAL");
    }

    @Test
    void generateCoolingRecommendation_containsStableKeywordUnderNormalConditions() {
        String rec = service.generateCoolingRecommendation(21.0, 1.25);
        assertThat(rec).containsIgnoringCase("STABLE");
    }
}
