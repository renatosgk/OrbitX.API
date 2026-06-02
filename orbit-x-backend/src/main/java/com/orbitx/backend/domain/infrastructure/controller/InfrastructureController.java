package com.orbitx.backend.domain.infrastructure.controller;
import com.orbitx.backend.domain.dashboard.controller.DashboardController;
import com.orbitx.backend.domain.infrastructure.dto.DatacenterResponse;
import com.orbitx.backend.domain.infrastructure.dto.SatelliteResponse;
import com.orbitx.backend.domain.infrastructure.service.InfrastructureService;
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
@RequestMapping("/api/v1/infrastructure")
@RequiredArgsConstructor
@Tag(name = "Infraestrutura", description = "Datacenters globais e rede de satélites orbitais")
@SecurityRequirement(name = "BearerAuth")
public class InfrastructureController {
    private final InfrastructureService infrastructureService;
    @GetMapping("/datacenters")
    @Operation(
            summary = "Listar todos os datacenters",
            description = "Retorna a frota global de datacenters com estado térmico ao vivo e métricas de energia. Cache de 30 segundos."
    )
    public ResponseEntity<CollectionModel<EntityModel<DatacenterResponse>>> getDatacenters() {
        List<EntityModel<DatacenterResponse>> items = infrastructureService.getAllDatacenters()
                .stream()
                .map(dc -> EntityModel.of(dc,
                        linkTo(methodOn(InfrastructureController.class).getDatacenters()).withSelfRel(),
                        linkTo(methodOn(DashboardController.class).getKpis()).withRel("kpis"),
                        linkTo(methodOn(InfrastructureController.class).getSatellites()).withRel("satellites")
                ))
                .toList();
        CollectionModel<EntityModel<DatacenterResponse>> model = CollectionModel.of(items,
                linkTo(methodOn(InfrastructureController.class).getDatacenters()).withSelfRel(),
                linkTo(methodOn(InfrastructureController.class).getSatellites()).withRel("satellites"),
                linkTo(methodOn(DashboardController.class).getAlerts()).withRel("alerts")
        );
        return ResponseEntity.ok(model);
    }
    @GetMapping("/satellites")
    @Operation(
            summary = "Listar satélites ativos",
            description = "Retorna posições orbitais, velocidade e intensidade de sinal em tempo real de todos os satélites Orbit X. Cache de 15 segundos."
    )
    public ResponseEntity<CollectionModel<EntityModel<SatelliteResponse>>> getSatellites() {
        List<EntityModel<SatelliteResponse>> items = infrastructureService.getAllActiveSatellites()
                .stream()
                .map(sat -> EntityModel.of(sat,
                        linkTo(methodOn(InfrastructureController.class).getSatellites()).withSelfRel(),
                        linkTo(methodOn(InfrastructureController.class).getDatacenters()).withRel("datacenters")
                ))
                .toList();
        CollectionModel<EntityModel<SatelliteResponse>> model = CollectionModel.of(items,
                linkTo(methodOn(InfrastructureController.class).getSatellites()).withSelfRel(),
                linkTo(methodOn(InfrastructureController.class).getDatacenters()).withRel("datacenters")
        );
        return ResponseEntity.ok(model);
    }
}
