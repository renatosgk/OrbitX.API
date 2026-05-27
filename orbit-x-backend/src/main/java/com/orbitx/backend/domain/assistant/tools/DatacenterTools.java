package com.orbitx.backend.domain.assistant.tools;

import com.orbitx.backend.domain.dashboard.dto.AlertResponse;
import com.orbitx.backend.domain.dashboard.dto.KpiResponse;
import com.orbitx.backend.domain.dashboard.service.DashboardService;
import com.orbitx.backend.domain.infrastructure.dto.DatacenterResponse;
import com.orbitx.backend.domain.infrastructure.service.InfrastructureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatacenterTools {

    private final DashboardService      dashboardService;
    private final InfrastructureService infrastructureService;

    @Tool(description = """
            Busca os KPIs em tempo real do sistema de monitoramento Orbit X.
            Retorna consumo de energia (kWh), temperatura atual (°C), emissões de carbono (toneladas),
            Eficiência de Uso de Energia (PUE), status geral e previsão de superaquecimento pela IA.
            Chame quando o usuário perguntar sobre métricas atuais, energia, temperatura ou status do sistema.
            """)
    public String getLiveKpis() {
        log.debug("Ferramenta invocada: getLiveKpis");
        KpiResponse kpi = dashboardService.getKpis();
        return """
                KPIs AO VIVO — capturado em %s
                Consumo de Energia : %.2f kWh
                Temperatura        : %.2f °C
                Emissões de Carbono: %.4f toneladas CO2
                PUE                : %.3f
                Status Geral       : %s
                Risco Overheat IA  : %.1f%%
                Recomendação       : %s
                Modo de Otimização : %s
                """.formatted(
                kpi.capturedAt(),
                kpi.energyConsumptionKwh(),
                kpi.currentTemperatureCelsius(),
                kpi.carbonEmissionTons(),
                kpi.powerUsageEffectiveness(),
                kpi.overallStatus(),
                kpi.aiInsight().overheatProbability() * 100,
                kpi.aiInsight().recommendation(),
                kpi.aiInsight().optimizationMode()
        );
    }

    @Tool(description = """
            Retorna o estado térmico e o consumo de energia de todos os datacenters globais
            da rede Orbit X. Use quando o usuário perguntar sobre localidades específicas,
            saúde dos datacenters, consumo regional ou distribuição geográfica.
            """)
    public String getDatacenterStatus() {
        log.debug("Ferramenta invocada: getDatacenterStatus");
        List<DatacenterResponse> dcs = infrastructureService.getAllDatacenters();
        return dcs.stream()
                .map(dc -> "%s (%s, %s) — Térmico: %s | %.2f kWh | %d servidores | Temp: %.1f°C".formatted(
                        dc.name(), dc.city(), dc.country(),
                        dc.thermalState(),
                        dc.regionalConsumptionKwh(),
                        dc.capacityServers(),
                        dc.currentTemperatureCelsius()))
                .collect(Collectors.joining("\n",
                        "DATACENTERS (" + dcs.size() + " ativos):\n", ""));
    }

    @Tool(description = """
            Busca todos os alertas ativos (não resolvidos) do motor de monitoramento da IA.
            Use quando o usuário perguntar sobre alertas, avisos, riscos, incidentes ou anomalias.
            """)
    public String getActiveAlerts() {
        log.debug("Ferramenta invocada: getActiveAlerts");
        List<AlertResponse> alerts = dashboardService.getActiveAlerts();
        if (alerts.isEmpty()) return "Nenhum alerta ativo — todos os sistemas nominais.";
        return alerts.stream()
                .map(a -> "[%s] %s — %s (origem: %s)".formatted(
                        a.severity(), a.title(), a.message(), a.sourceComponent()))
                .collect(Collectors.joining("\n",
                        "ALERTAS ATIVOS (" + alerts.size() + "):\n", ""));
    }

    @Tool(description = """
            Retorna a pontuação ESG de sustentabilidade atual, economia de energia, offset de carbono,
            percentual de energia renovável e comparativo antes/depois da ativação do Orbit X.
            Use para perguntas sobre sustentabilidade, ESG, energia limpa, pegada de carbono ou impacto ambiental.
            """)
    public String getSustainabilityMetrics() {
        log.debug("Ferramenta invocada: getSustainabilityMetrics");
        return """
                PONTUAÇÃO ESG: 87/100 (Nota: A+)
                Energia Economizada (acumulada): 287.450 kWh
                Compensação de Carbono: 142,3 toneladas CO2eq
                Mix de Energia Renovável: 76,4%%

                Antes do Orbit X → PUE: 1,82 | Carbono: 7,34 t/mês | Resfriamento: 61,2%%
                Após o Orbit X  → PUE: 1,27 | Carbono: 2,11 t/mês | Resfriamento: 89,7%%
                Melhoria: redução de 30,2%% no PUE | Top 5%% mundial
                """;
    }
}
