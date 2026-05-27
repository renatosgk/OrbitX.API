package com.orbitx.backend.domain.reports.controller;

import com.orbitx.backend.domain.dashboard.controller.DashboardController;
import com.orbitx.backend.domain.infrastructure.controller.InfrastructureController;
import com.orbitx.backend.domain.reports.dto.SustainabilityScoreResponse;
import com.orbitx.backend.domain.reports.service.ReportsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Relatórios & ESG", description = "Pontuação de sustentabilidade, métricas ESG e exportação PDF")
@SecurityRequirement(name = "BearerAuth")
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/sustainability-score")
    @Operation(
            summary = "Obter pontuação de sustentabilidade ESG",
            description = "Retorna a pontuação ESG atual (0-100), offset de carbono, economia de energia e comparativo antes/depois da ativação do Orbit X. Cache de 5 minutos."
    )
    public ResponseEntity<EntityModel<SustainabilityScoreResponse>> getSustainabilityScore() {
        EntityModel<SustainabilityScoreResponse> model = EntityModel.of(
                reportsService.getSustainabilityScore(),
                linkTo(methodOn(ReportsController.class).getSustainabilityScore()).withSelfRel(),
                linkTo(methodOn(ReportsController.class).exportPdf()).withRel("export-pdf"),
                linkTo(methodOn(DashboardController.class).getKpis()).withRel("live-kpis"),
                linkTo(methodOn(InfrastructureController.class).getDatacenters()).withRel("datacenters")
        );
        return ResponseEntity.ok(model);
    }

    @GetMapping("/export/pdf")
    @Operation(
            summary = "Exportar relatório executivo em PDF",
            description = "Gera e faz download de um relatório executivo de sustentabilidade. Em produção, integre iText ou JasperReports."
    )
    public ResponseEntity<byte[]> exportPdf() {
        byte[] content = reportsService.exportPdfReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"orbit-x-sustainability-report.pdf\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(content.length)
                .body(content);
    }
}
