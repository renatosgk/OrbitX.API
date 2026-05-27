package com.orbitx.backend.domain.dashboard.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;

@Service
public class TelemetrySimulatorService {

    private static final Random RANDOM = new Random();

    private static final double BASE_TEMP_C       = 22.0;
    private static final double TEMP_AMPLITUDE_C  = 7.5;
    private static final double NOISE_SIGMA_C     = 1.2;

    private static final double BASE_ENERGY_KWH   = 2000.0;
    private static final double ENERGY_SIGMA_KWH  = 140.0;
    private static final double MIN_ENERGY_KWH    = 1400.0;

    public double generateCurrentTemperature() {
        long epochSeconds = Instant.now().getEpochSecond();
        double sinComponent = Math.sin((double) epochSeconds / 3600.0 * Math.PI) * TEMP_AMPLITUDE_C;
        double noise        = RANDOM.nextGaussian() * NOISE_SIGMA_C;
        return round(BASE_TEMP_C + sinComponent + noise, 2);
    }

    public double generateTemperatureForLocation(double latitude) {
        double equatorBias  = (1.0 - Math.abs(latitude) / 90.0) * 4.5;
        return round(generateCurrentTemperature() + equatorBias, 2);
    }

    public double generateEnergyConsumption() {
        double fluctuation = RANDOM.nextGaussian() * ENERGY_SIGMA_KWH;
        return round(Math.max(MIN_ENERGY_KWH, BASE_ENERGY_KWH + fluctuation), 2);
    }

    public double generateCarbonEmission(double energyKwh) {
        double renewableFraction = 0.55 + RANDOM.nextDouble() * 0.20;
        return round((energyKwh * 0.233 * (1.0 - renewableFraction)) / 1000.0, 4);
    }

    public double generatePue() {
        double basePue     = 1.28;
        double fluctuation = RANDOM.nextGaussian() * 0.04;
        return round(Math.max(1.01, Math.min(2.0, basePue + fluctuation)), 3);
    }

    private double round(double value, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }
}
