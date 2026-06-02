package com.orbitx.backend.domain.reports.service;

import com.orbitx.backend.config.CacheConfig;
import com.orbitx.backend.domain.reports.dto.SustainabilityScoreResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class ReportsService {

    @Cacheable(value = CacheConfig.CACHE_SUSTAINABILITY, key = "'score'")
    public SustainabilityScoreResponse getSustainabilityScore() {
        var before = new SustainabilityScoreResponse.Comparison(
                1.82, BigDecimal.valueOf(31500.0), BigDecimal.valueOf(7.34), 61.2
        );
        var after = new SustainabilityScoreResponse.Comparison(
                1.27, BigDecimal.valueOf(18500.0), BigDecimal.valueOf(2.11), 89.7
        );
        return new SustainabilityScoreResponse(
                87, "A+",
                BigDecimal.valueOf(287450.0),
                BigDecimal.valueOf(142.3),
                76.4,
                before, after,
                Instant.now()
        );
    }

    public byte[] exportPdfReport() {

        return buildReportContent().getBytes();
    }

    private String buildReportContent() {
        return """
                %%PDF-1.4
                %%%% Orbit X — Executive Sustainability Report
                %%%% Generated: %s

                ESG Score ......... 87 / 100  (Grade: A+)
                Energy Saved ...... 287,450 kWh accumulated
                Carbon Offset ..... 142.3 t CO2eq
                Renewable Mix ..... 76.4%%

                ┌──────────────────────────────────┐
                │  Before Orbit X  │  After Orbit X │
                │  PUE   : 1.82    │  PUE   : 1.27  │
                │  Energy: 31,500  │  Energy: 18,500│
                │  Carbon: 7.34 t  │  Carbon: 2.11 t│
                │  Cooling: 61.2%% │  Cooling: 89.7%%│
                └──────────────────────────────────┘

                Improvement: 30.2%% PUE reduction
                Ranking: Top 5%% globally

                [Orbit X AI Engine — Confidential Executive Report]
                """.formatted(Instant.now());
    }
}
