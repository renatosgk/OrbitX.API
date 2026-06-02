package com.orbitx.backend.domain.infrastructure.repository;
import com.orbitx.backend.domain.infrastructure.entity.Satellite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SatelliteRepository extends JpaRepository<Satellite, Long> {
    List<Satellite> findByActiveTrue();
}
