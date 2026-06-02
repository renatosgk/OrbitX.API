package com.orbitx.backend.domain.auth.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
@Entity
@Table(name = "companies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(name = "tax_id", unique = true, nullable = false)
    private String taxId;
    @Column(name = "admin_email", nullable = false)
    private String adminEmail;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CompanyPlan plan = CompanyPlan.ENTERPRISE;
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
    public enum CompanyPlan {
        STARTER, PROFESSIONAL, ENTERPRISE
    }
}
