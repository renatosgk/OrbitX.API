package com.orbitx.backend.domain.infrastructure.repository;
import com.orbitx.backend.domain.infrastructure.entity.Datacenter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DatacenterRepository extends JpaRepository<Datacenter, Long> {
    List<Datacenter> findByActiveTrue();
}
