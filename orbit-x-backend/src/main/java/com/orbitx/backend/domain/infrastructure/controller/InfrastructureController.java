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
@Tag(name = "Infrastructure", description = "Global datacenters and orbital satellite network")
@SecurityRequirement(name = "BearerAuth")
public class InfrastructureController {

    private final InfrastructureService infrastructureService;

    @GetMapping("/datacenters")
    @Operation(
            summary = "List all datacenters",
            description = "Returns the global datacenter fleet with live thermal state and energy metrics. Cached for 30 seconds."
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
            summary = "List active satellites",
            description = "Returns real-time orbital positions, speed, and signal strength for all Orbit X satellites. Cached for 15 seconds."
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
