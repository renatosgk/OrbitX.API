package com.orbitx.backend.domain.infrastructure.service;
import com.orbitx.backend.config.CacheConfig;
import com.orbitx.backend.domain.dashboard.service.TelemetrySimulatorService;
import com.orbitx.backend.domain.infrastructure.dto.DatacenterResponse;
import com.orbitx.backend.domain.infrastructure.dto.SatelliteResponse;
import com.orbitx.backend.domain.infrastructure.repository.DatacenterRepository;
import com.orbitx.backend.domain.infrastructure.repository.SatelliteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class InfrastructureService {
    private final DatacenterRepository     datacenterRepository;
    private final SatelliteRepository      satelliteRepository;
    private final TelemetrySimulatorService telemetry;
    private final OrbitalMonitoringService  orbitalMonitoring;
    @Cacheable(value = CacheConfig.CACHE_DATACENTERS, key = "'all'")
    public List<DatacenterResponse> getAllDatacenters() {
        log.debug("Cache MISS — carregando datacenters do banco + telemetria");
        return datacenterRepository.findByActiveTrue().stream()
                .map(dc -> DatacenterResponse.from(
                        dc, telemetry.generateTemperatureForLocation(dc.getLatitude())))
                .toList();
    }
    @Cacheable(value = CacheConfig.CACHE_SATELLITES, key = "'all'")
    public List<SatelliteResponse> getAllActiveSatellites() {
        log.debug("Cache MISS — calculando posições orbitais");
        return satelliteRepository.findByActiveTrue().stream()
                .map(orbitalMonitoring::buildSatelliteResponse)
                .toList();
    }
    @Scheduled(fixedDelay = 15_000)
    @CacheEvict(value = CacheConfig.CACHE_SATELLITES, allEntries = true)
    public void evictSatelliteCache() {
        log.debug("Cache de satélites expirado (agendado 15s)");
    }
    @Scheduled(fixedDelay = 30_000)
    @CacheEvict(value = CacheConfig.CACHE_DATACENTERS, allEntries = true)
    public void evictDatacenterCache() {
        log.debug("Cache de datacenters expirado (agendado 30s)");
    }
}
