package com.orbitx.backend.domain.auth.repository;

import com.orbitx.backend.domain.auth.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByTaxId(String taxId);
}
