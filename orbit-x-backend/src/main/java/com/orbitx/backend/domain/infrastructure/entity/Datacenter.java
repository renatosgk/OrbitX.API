package com.orbitx.backend.domain.infrastructure.entity;
import com.orbitx.backend.domain.infrastructure.entity.enums.ThermalState;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity
@Table(name = "datacenters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Datacenter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String country;
    @Column(nullable = false)
    private double latitude;
    @Column(nullable = false)
    private double longitude;
    @Enumerated(EnumType.STRING)
    @Column(name = "thermal_state", nullable = false)
    @Builder.Default
    private ThermalState thermalState = ThermalState.STABLE;
    @Column(name = "regional_consumption_kwh", precision = 12, scale = 2)
    private BigDecimal regionalConsumptionKwh;
    @Column(name = "capacity_servers")
    private Integer capacityServers;
    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
