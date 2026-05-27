package com.orbitx.backend.domain.assistant.rag;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class KnowledgeBaseConfig {

    public String getRelevantContext(String userQuery) {
        String query = userQuery.toLowerCase();
        StringBuilder context = new StringBuilder();

        for (Map.Entry<List<String>, String> entry : KNOWLEDGE_BASE.entrySet()) {
            boolean relevant = entry.getKey().stream()
                    .anyMatch(keyword -> query.contains(keyword));
            if (relevant) {
                context.append(entry.getValue()).append("\n\n");
            }
        }

        return context.isEmpty()
                ? getGeneralContext()
                : context.toString().trim();
    }

    public String getGeneralContext() {
        return """
                Orbit X é uma plataforma de monitoramento inteligente para datacenters sustentáveis.
                Monitora 6 datacenters globais (São Paulo, Frankfurt, Singapore, New York, Tokyo, London)
                com 32.800 servidores no total, 5 satélites em órbita (LEO/MEO/GEO),
                score ESG de 87/100 (A+) e redução de PUE de 1.82 para 1.27 desde a ativação.
                """;
    }

    private static final Map<List<String>, String> KNOWLEDGE_BASE = Map.of(

            List.of("pue", "energia", "energy", "eficiência", "consumo", "kwh", "watt"),
            """
            PUE (Power Usage Effectiveness) = Energia Total do Datacenter / Energia do Equipment de TI.
            PUE ideal: 1.0 (impossível na prática). Média da indústria: 1.58.
            Meta Orbit X com AI Green Mode: PUE < 1.30.
            Antes da Orbit X: PUE médio 1.82. Após ativação: 1.27 (redução de 30.2%).
            Energia economizada acumulada: 287.450 kWh.
            """,

            List.of("temperatura", "thermal", "temperatura", "calor", "refrigeração", "cooling", "superaquecimento"),
            """
            Temperatura operacional ideal para datacenters (ASHRAE A1): 18°C–27°C.
            Orbit X emite alerta CRITICAL quando temperatura > 30°C (probabilidade de superaquecimento > 90%).
            Alerta HIGH: 27°C–30°C (probabilidade 50–90%). Orbit X usa resfriamento preditivo.
            O motor de IA analisa tendências 15–30 minutos à frente usando modelos de séries temporais.
            Modo TURBO_COOLING: acionado acima de 30°C. Modo GREEN_MODE: acionado abaixo de 24°C.
            """,

            List.of("esg", "sustentab", "carbon", "co2", "verde", "emissão", "renovável", "green"),
            """
            Score ESG Orbit X: 87/100 (Classificação A+) — top 5% datacenters globalmente.
            Carbon offset acumulado: 142.3 toneladas de CO2 equivalente.
            Mix de energia renovável atual: 76.4%.
            Antes da Orbit X → Emissão: 7.34 t/mês | Cooling: 61.2%.
            Após  Orbit X → Emissão: 2.11 t/mês | Cooling: 89.7%.
            Relatórios ESG seguem o padrão GHG Protocol Scope 2.
            """,

            List.of("satélite", "satelite", "satellite", "orbital", "órbita", "orbita", "leo", "geo", "meo"),
            """
            Frota orbital Orbit X: 5 satélites.
            OX-SAT-01 Atlas (LEO 550 km) — período 95.5 min — velocidade ~27.600 km/h — ATIVO.
            OX-SAT-02 Helios (LEO 570 km) — período 96.1 min — ATIVO.
            OX-SAT-03 Kronos (MEO 8.000 km) — período 285 min — ATIVO.
            OX-SAT-04 Aurora (LEO 530 km) — DEGRADADO (link parcial).
            OX-SAT-05 Zenith (GEO 35.786 km) — geoestacionário — 24/7 cobertura regional — ATIVO.
            Cobertura global: 94.7%.
            """,

            List.of("alerta", "alert", "risco", "warning", "aviso", "incidente"),
            """
            Severidades de alerta Orbit X:
            CRITICAL — intervenção imediata (temp > 30°C, risco > 90%).
            HIGH     — intervenção em 15 min (temp 27–30°C, risco 50–90%).
            MEDIUM   — monitoramento necessário (temp 24–27°C).
            LOW      — informacional (sugestões de otimização, PUE acima do baseline).
            Alertas CRITICAL e HIGH são publicados via RabbitMQ para o notification-service.
            """,

            List.of("datacenter", "servidor", "rack", "região", "infra", "global"),
            """
            Datacenters Orbit X (6 regiões, 32.800 servidores totais):
            São Paulo Alpha  (BR) — STABLE   — 18.500 kWh — 4.200 servidores
            Frankfurt Prime  (DE) — OPTIMAL  — 22.100 kWh — 6.800 servidores
            Singapore Hub    (SG) — STABLE   — 19.750 kWh — 5.100 servidores
            New York Edge    (US) — OPTIMAL  — 24.300 kWh — 7.200 servidores
            Tokyo Nexus      (JP) — CRITICAL — 16.900 kWh — 3.900 servidores (atenção!)
            London Core      (UK) — STABLE   — 20.400 kWh — 5.600 servidores
            """
    );
}
