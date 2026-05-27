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
@Tag(name = "Reports & ESG", description = "Sustainability scoring, ESG metrics, and PDF export")
@SecurityRequirement(name = "BearerAuth")
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/sustainability-score")
    @Operation(
            summary = "Get ESG sustainability score",
            description = "Returns the current ESG score (0-100), carbon offset, energy savings, and a before/after comparison of Orbit X activation. Cached for 5 minutes."
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
            summary = "Export executive PDF report",
            description = "Generates and downloads an executive sustainability report. In production, integrate iText or JasperReports."
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
