package com.orbitx.backend.domain.dashboard.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class AiPredictionService {

    private static final double CRITICAL_THRESHOLD = 30.0;
    private static final double WARNING_THRESHOLD  = 26.0;
    private static final Random RANDOM = new Random();

    private static final List<String> RACK_LABELS = List.of(
            "Rack 04B", "Rack 07A", "Rack 12C", "Rack 02D", "Rack 09F", "Rack 15A", "Rack 18E"
    );

    public double calculateOverheatProbability(double currentTemp) {
        if (currentTemp >= CRITICAL_THRESHOLD) return 0.97;
        if (currentTemp >= WARNING_THRESHOLD) {
            double normalized = (currentTemp - WARNING_THRESHOLD)
                    / (CRITICAL_THRESHOLD - WARNING_THRESHOLD);
            return round(0.30 + normalized * 0.60, 3);
        }
        return round(Math.max(0.02, (currentTemp / WARNING_THRESHOLD) * 0.25), 3);
    }

    public String generateCoolingRecommendation(double temperature, double pue) {
        if (temperature >= CRITICAL_THRESHOLD) {
            return "CRITICAL: Immediate intervention required. Activating emergency cooling protocol across all zones.";
        }
        if (temperature >= WARNING_THRESHOLD) {
            String rack    = RACK_LABELS.get(RANDOM.nextInt(RACK_LABELS.size()));
            int minutes    = 5 + RANDOM.nextInt(20);
            return String.format(
                    "WARNING: Thermal elevation risk detected in %s within ~%d min. AI recommends activating zone-specific cooling.",
                    rack, minutes
            );
        }
        if (pue > 1.5) {
            return "OPTIMIZATION: PUE is above efficiency threshold. Redistributing compute loads estimated to reduce overhead by 13%.";
        }
        if (pue > 1.35) {
            return "INFO: Green Mode cooling algorithm available for current load profile. Estimated 9% energy reduction.";
        }
        return "STABLE: All thermal metrics are within optimal parameters. AI Engine monitoring — no action required.";
    }

    public String resolveOptimizationMode(double temperature, double pue) {
        if (temperature >= CRITICAL_THRESHOLD) return "TURBO_COOLING";
        if (temperature >= WARNING_THRESHOLD)  return "PREDICTIVE_COOLING";
        if (pue > 1.4)                         return "LOAD_BALANCING";
        return "GREEN_MODE";
    }

    private double round(double value, int decimals) {
        double scale = Math.pow(10, decimals);
        return Math.round(value * scale) / scale;
    }
}
