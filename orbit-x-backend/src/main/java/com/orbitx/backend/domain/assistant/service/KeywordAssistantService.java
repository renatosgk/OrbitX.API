package com.orbitx.backend.domain.assistant.service;

import com.orbitx.backend.domain.assistant.dto.ChatMessage;
import com.orbitx.backend.domain.assistant.dto.ChatRequest;
import com.orbitx.backend.domain.assistant.dto.ChatResponse;
import com.orbitx.backend.domain.assistant.port.AssistantPort;
import com.orbitx.backend.domain.dashboard.service.AiPredictionService;
import com.orbitx.backend.domain.dashboard.service.TelemetrySimulatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Offline/dev fallback assistant — active when OPENAI_API_KEY is not configured.
 * Uses keyword matching + live telemetry to produce contextually accurate responses.
 * No external API calls required.
 */
@Slf4j
@Service
@ConditionalOnMissingBean(SpringAiAssistantService.class)
@RequiredArgsConstructor
public class KeywordAssistantService implements AssistantPort {

    private static final String MODEL_ID = "orbit-x-ai-v2.1-offline";
    private static final int    MAX_HISTORY_SIZE = 20;

    private final TelemetrySimulatorService telemetry;
    private final AiPredictionService       aiPrediction;

    @Override
    public ChatResponse chat(ChatRequest request) {
        log.debug("KeywordAssistantService handling chat (Spring AI not configured)");
        double temp         = telemetry.generateCurrentTemperature();
        double energy       = telemetry.generateEnergyConsumption();
        double pue          = telemetry.generatePue();
        double overheatProb = aiPrediction.calculateOverheatProbability(temp);
        String recommendation = aiPrediction.generateCoolingRecommendation(temp, pue);

        String response = buildResponse(request.message().toLowerCase(),
                temp, energy, pue, overheatProb, recommendation);

        List<ChatMessage> history = buildHistory(request, response);
        return new ChatResponse(response, MODEL_ID, estimateTokens(response), history, Instant.now());
    }

    private String buildResponse(String msg, double temp, double energy,
                                  double pue, double overheatProb, String recommendation) {

        if (containsAny(msg, "temperatura", "thermal", "calor", "refriger", "cooling", "aquecimento"))
            return formatThermal(temp, pue, overheatProb, recommendation);

        if (containsAny(msg, "energia", "energy", "consumo", "kwh", "pue", "eficiência"))
            return formatEnergy(energy, pue);

        if (containsAny(msg, "esg", "sustentab", "carbon", "co2", "verde", "green", "emissão"))
            return FORMAT_ESG;

        if (containsAny(msg, "satélite", "satelite", "satellite", "orbital", "órbita"))
            return FORMAT_SATELLITES;

        if (containsAny(msg, "alerta", "alert", "risco", "aviso", "problema"))
            return "Central de Alertas — Orbit X\n\n" + recommendation +
                    "\n\nTodos os alertas são monitorados em tempo real pelo motor de IA.";

        if (containsAny(msg, "datacenter", "servidor", "rack", "infra"))
            return FORMAT_DATACENTERS;

        return FORMAT_DEFAULT;
    }

    private String formatThermal(double temp, double pue, double overheatProb, String rec) {
        return """
                Análise Térmica — Orbit X AI Engine

                Temperatura atual : %.1f °C
                Risco overheat    : %.1f%%
                Modo              : %s
                PUE               : %.3f

                Recomendação: %s
                """.formatted(temp, overheatProb * 100,
                aiPrediction.resolveOptimizationMode(temp, pue), pue, rec);
    }

    private String formatEnergy(double energy, double pue) {
        return """
                Relatório de Consumo — Orbit X Analytics

                Consumo atual : %.0f kWh
                PUE           : %.3f  (meta: < 1.30)
                Benchmark     : 1.58  |  Economia: 30.2%%
                Renovável     : 76.4%%  |  Economizado: 287,450 kWh acumulados
                """.formatted(energy, pue);
    }

    private static final String FORMAT_ESG = """
            Dashboard ESG — Orbit X Sustainability

            Score: 87/100 (A+)   Carbon offset: 142.3 t CO₂eq
            Renovável: 76.4%%    PUE médio: 1.27

            Antes Orbit X → PUE 1.82 | Emissão 7.34 t/mês
            Após  Orbit X → PUE 1.27 | Emissão 2.11 t/mês
            Top 5%% datacenters sustentáveis globais.
            """;

    private static final String FORMAT_SATELLITES = """
            Frota Orbital — Orbit X Satellite Network

            OX-SAT-01 Atlas   (LEO  550 km) — ATIVO     ✓
            OX-SAT-02 Helios  (LEO  570 km) — ATIVO     ✓
            OX-SAT-03 Kronos  (MEO 8000 km) — ATIVO     ✓
            OX-SAT-04 Aurora  (LEO  530 km) — DEGRADADO ⚠
            OX-SAT-05 Zenith  (GEO 35786 km)— ATIVO     ✓

            Cobertura: 94.7%%  |  Manutenção OX-SAT-04: 14:00 UTC
            """;

    private static final String FORMAT_DATACENTERS = """
            Infraestrutura Global — 6 regiões ativas

            São Paulo Alpha  (BR) STABLE   | 18.500 kWh | 4.200 srv
            Frankfurt Prime  (DE) OPTIMAL  | 22.100 kWh | 6.800 srv
            Singapore Hub    (SG) STABLE   | 19.750 kWh | 5.100 srv
            New York Edge    (US) OPTIMAL  | 24.300 kWh | 7.200 srv
            Tokyo Nexus      (JP) CRITICAL | 16.900 kWh | 3.900 srv ⚠
            London Core      (UK) STABLE   | 20.400 kWh | 5.600 srv
            """;

    private static final String FORMAT_DEFAULT = """
            Orbit X AI Assistant — Offline Mode

            Posso ajudar com:  🌡️ Térmica  ⚡ Energia  🌍 ESG  🛰️ Satélites  🚨 Alertas

            Dica: Configure OPENAI_API_KEY para ativar o modo AI completo com RAG e Tool Calling.
            """;

    private boolean containsAny(String input, String... keywords) {
        for (String kw : keywords) {
            if (input.contains(kw)) return true;
        }
        return false;
    }

    private List<ChatMessage> buildHistory(ChatRequest request, String response) {
        List<ChatMessage> history = new ArrayList<>();
        if (request.history() != null) history.addAll(request.history());
        history.add(new ChatMessage("user", request.message()));
        history.add(new ChatMessage("assistant", response));
        if (history.size() > MAX_HISTORY_SIZE)
            history = history.subList(history.size() - MAX_HISTORY_SIZE, history.size());
        return history;
    }

    private int estimateTokens(String text) {
        return (int) Math.ceil(text.length() / 4.0);
    }
}
