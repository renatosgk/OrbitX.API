package com.orbitx.backend.domain.infrastructure.entity;

import com.orbitx.backend.domain.infrastructure.entity.enums.DataLinkStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "satellites")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Satellite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "orbit_type", nullable = false)
    private String orbitType;

    @Column(name = "altitude_km", nullable = false)
    private double altitudeKm;

    @Column(name = "inclination_deg", nullable = false)
    private double inclinationDeg;

    @Column(name = "orbital_period_min", nullable = false)
    private double orbitalPeriodMin;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_link_status", nullable = false)
    @Builder.Default
    private DataLinkStatus dataLinkStatus = DataLinkStatus.ACTIVE;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "launched_at")
    private Instant launchedAt;
}
