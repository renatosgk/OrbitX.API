package com.orbitx.backend.domain.dashboard.repository;
import com.orbitx.backend.domain.dashboard.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByResolvedFalseOrderByCreatedAtDesc();
    List<Alert> findByDatacenterIdAndResolvedFalse(Long datacenterId);
}
