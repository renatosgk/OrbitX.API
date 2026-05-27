package com.orbitx.backend.domain.assistant.service;

import com.orbitx.backend.domain.assistant.dto.ChatMessage;
import com.orbitx.backend.domain.assistant.dto.ChatRequest;
import com.orbitx.backend.domain.assistant.dto.ChatResponse;
import com.orbitx.backend.domain.dashboard.service.AiPredictionService;
import com.orbitx.backend.domain.dashboard.service.TelemetrySimulatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Context-aware assistant engine.
 * Matches keywords in the user message and injects live telemetry into the response,
 * simulating a premium enterprise AI assistant.
 */
@Service
@RequiredArgsConstructor
public class AssistantService {

    private static final String MODEL_ID        = "orbit-x-ai-v2.1";
    private static final int    MAX_HISTORY_SIZE = 20;

    private final TelemetrySimulatorService telemetry;
    private final AiPredictionService       aiPrediction;

    public ChatResponse chat(ChatRequest request) {
        double temp          = telemetry.generateCurrentTemperature();
        double energy        = telemetry.generateEnergyConsumption();
        double pue           = telemetry.generatePue();
        double overheatProb  = aiPrediction.calculateOverheatProbability(temp);
        String recommendation = aiPrediction.generateCoolingRecommendation(temp, pue);

        String responseText = buildResponse(
                request.message().toLowerCase(), temp, energy, pue, overheatProb, recommendation
        );

        List<ChatMessage> history = buildHistory(request, responseText);

        return new ChatResponse(
                responseText,
                MODEL_ID,
                estimateTokens(responseText),
                history,
                Instant.now()
        );
    }

    private String buildResponse(String msg, double temp, double energy,
                                  double pue, double overheatProb, String recommendation) {

        if (containsAny(msg, "temperatura", "thermal", "calor", "refriger", "cooling", "aquecimento")) {
            return """
                    Análise Térmica em Tempo Real — Orbit X AI Engine

                    Temperatura atual: %.1f°C
                    Probabilidade de superaquecimento (2h): %.1f%%

                    Recomendação: %s

                    Status do sistema de refrigeração: OPERACIONAL ✓
                    Modo ativo: %s
                    """.formatted(temp, overheatProb * 100, recommendation,
                    aiPrediction.resolveOptimizationMode(temp, pue));
        }

        if (containsAny(msg, "energia", "energy", "consumo", "kwh", "pue", "watt", "eficiência")) {
            return """
                    Relatório de Consumo Energético — Orbit X Analytics

                    Consumo atual: %.0f kWh
                    PUE (Power Usage Effectiveness): %.3f
                    Benchmark de mercado: 1.58 | Meta Orbit X: < 1.30

                    Desde a ativação da Orbit X:
                    • Redução de consumo: 30.2%%
                    • Energia renovável no mix: 76.4%%
                    • Economia acumulada: 287,450 kWh

                    Modo de otimização ativo: AI_GREEN_MODE ✓
                    """.formatted(energy, pue);
        }

        if (containsAny(msg, "esg", "sustentab", "carbon", "co2", "verde", "green", "emissão", "emissao")) {
            return """
                    Dashboard ESG — Orbit X Sustainability Engine

                    Score ESG atual: 87 / 100  (Classificação: A+)

                    Principais indicadores:
                    • Energia renovável no mix ............ 76.4%%
                    • Carbon offset acumulado ............. 142.3 t CO₂eq
                    • Eficiência de refrigeração .......... 89.7%%
                    • PUE médio atual ..................... 1.27

                    Comparativo:
                    Antes Orbit X → PUE 1.82, Emissão 7.34 t/mês
                    Após  Orbit X → PUE 1.27, Emissão 2.11 t/mês

                    A Orbit X está no top 5%% dos datacenters mais sustentáveis do mundo.
                    """;
        }

        if (containsAny(msg, "satélite", "satelite", "satellite", "orbital", "órbita", "orbita", "sat-")) {
            return """
                    Status da Frota Orbital — Orbit X Satellite Network

                    Satélites monitorados: 5  |  Ativos: 4  |  Degradado: 1
                    Cobertura global estimada: 94.7%%

                    OX-SAT-01 Atlas   (LEO  550 km)  Link: ATIVO       ✓
                    OX-SAT-02 Helios  (LEO  570 km)  Link: ATIVO       ✓
                    OX-SAT-03 Kronos  (MEO 8000 km)  Link: ATIVO       ✓
                    OX-SAT-04 Aurora  (LEO  530 km)  Link: DEGRADADO   ⚠
                    OX-SAT-05 Zenith  (GEO 35786 km) Link: ATIVO       ✓

                    Próxima janela de manutenção OX-SAT-04: 14:00 UTC
                    Telemetria orbital atualizada em tempo real via Orbit X Ground Station.
                    """;
        }

        if (containsAny(msg, "alerta", "alert", "risco", "risk", "aviso", "problema")) {
            return """
                    Central de Alertas — Orbit X Intelligence Hub

                    %s

                    Alertas ativos adicionais:
                    • PUE ligeiramente acima da baseline — modo de otimização acionado
                    • Ciclo de refrigeração do Rack 07A com 98.3%% de eficiência

                    Todos os alertas são monitorados em tempo real. Use a aba de Alertas
                    para ver o histórico completo de eventos e ações tomadas.
                    """.formatted(recommendation);
        }

        if (containsAny(msg, "datacenter", "data center", "servidor", "server", "rack", "infra")) {
            return """
                    Infraestrutura Global — Orbit X Network

                    Datacenters ativos: 6  |  Capacidade total: 32.800 servidores

                    São Paulo Alpha  (BR)  — STABLE   | 18.500 kWh | 4.200 servidores
                    Frankfurt Prime (DE)  — OPTIMAL  | 22.100 kWh | 6.800 servidores
                    Singapore Hub   (SG)  — STABLE   | 19.750 kWh | 5.100 servidores
                    New York Edge   (US)  — OPTIMAL  | 24.300 kWh | 7.200 servidores
                    Tokyo Nexus     (JP)  — CRITICAL | 16.900 kWh | 3.900 servidores ⚠
                    London Core     (UK)  — STABLE   | 20.400 kWh | 5.600 servidores

                    Recomendação: Tokyo Nexus requer atenção imediata no sistema de refrigeração.
                    """;
        }

        // Default: onboarding response
        return """
                Orbit X AI Assistant — v2.1  |  Powered by Orbit X Engine

                Olá! Sou o assistente inteligente da plataforma Orbit X.
                Posso te ajudar com:

                  🌡️  Análise térmica e predição de superaquecimento
                  ⚡  Consumo energético e otimização de PUE
                  🌍  Score ESG e relatórios de sustentabilidade
                  🛰️  Status da frota orbital e cobertura de satélites
                  🚨  Central de alertas e recomendações da IA
                  🖥️  Infraestrutura global e status dos datacenters

                Como posso ajudar hoje?
                """;
    }

    private boolean containsAny(String input, String... keywords) {
        for (String kw : keywords) {
            if (input.contains(kw)) return true;
        }
        return false;
    }

    private List<ChatMessage> buildHistory(ChatRequest request, String responseText) {
        List<ChatMessage> history = new ArrayList<>();
        if (request.history() != null) {
            history.addAll(request.history());
        }
        history.add(new ChatMessage("user", request.message()));
        history.add(new ChatMessage("assistant", responseText));

        // Sliding window — keep last N messages to avoid payload bloat
        if (history.size() > MAX_HISTORY_SIZE) {
            history = history.subList(history.size() - MAX_HISTORY_SIZE, history.size());
        }
        return history;
    }

    private int estimateTokens(String text) {
        return (int) Math.ceil(text.length() / 4.0);
    }
}
