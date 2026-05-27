package com.orbitx.backend.domain.dashboard.controller;

import com.orbitx.backend.domain.dashboard.dto.AlertResponse;
import com.orbitx.backend.domain.dashboard.dto.KpiResponse;
import com.orbitx.backend.domain.dashboard.service.DashboardService;
import com.orbitx.backend.domain.infrastructure.controller.InfrastructureController;
import com.orbitx.backend.domain.reports.controller.ReportsController;
import com.orbitx.backend.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "KPIs em tempo real e alertas gerados pela IA")
@SecurityRequirement(name = "BearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/kpis")
    @Operation(
            summary = "Obter KPIs em tempo real",
            description = "Retorna consumo de energia, temperatura, emissões de carbono, PUE e previsão da IA ao vivo. Sem cache — sempre atualizado."
    )
    public ResponseEntity<EntityModel<KpiResponse>> getKpis() {
        KpiResponse kpis = dashboardService.getKpis();

        EntityModel<KpiResponse> model = EntityModel.of(kpis,
                linkTo(methodOn(DashboardController.class).getKpis()).withSelfRel(),
                linkTo(methodOn(DashboardController.class).getAlerts()).withRel("alerts"),
                linkTo(methodOn(InfrastructureController.class).getDatacenters()).withRel("datacenters"),
                linkTo(methodOn(InfrastructureController.class).getSatellites()).withRel("satellites"),
                linkTo(methodOn(ReportsController.class).getSustainabilityScore()).withRel("sustainability")
        );

        return ResponseEntity.ok(model);
    }

    @GetMapping("/alerts")
    @Operation(
            summary = "Listar alertas ativos da IA",
            description = "Lista os alertas não resolvidos gerados pelo motor de predição da IA. Usa alertas dinâmicos quando o banco estiver vazio."
    )
    public ResponseEntity<CollectionModel<EntityModel<AlertResponse>>> getAlerts() {
        List<EntityModel<AlertResponse>> items = dashboardService.getActiveAlerts().stream()
                .map(alert -> EntityModel.of(alert,
                        linkTo(methodOn(DashboardController.class).getAlerts()).withSelfRel(),
                        linkTo(methodOn(DashboardController.class).getKpis()).withRel("kpis")
                ))
                .toList();

        CollectionModel<EntityModel<AlertResponse>> model = CollectionModel.of(items,
                linkTo(methodOn(DashboardController.class).getAlerts()).withSelfRel(),
                linkTo(methodOn(DashboardController.class).getKpis()).withRel("kpis")
        );

        return ResponseEntity.ok(model);
    }
}
