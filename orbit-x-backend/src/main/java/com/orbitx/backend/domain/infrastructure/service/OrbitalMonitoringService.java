package com.orbitx.backend.domain.infrastructure.service;

import com.orbitx.backend.domain.infrastructure.dto.LocationDto;
import com.orbitx.backend.domain.infrastructure.dto.SatelliteResponse;
import com.orbitx.backend.domain.infrastructure.entity.Satellite;
import com.orbitx.backend.domain.infrastructure.entity.enums.DataLinkStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;

/**
 * Computes real-time satellite positions using simplified circular orbital mechanics.
 * Phase angle is derived from current epoch time and each satellite's orbital period,
 * producing smoothly moving lat/lng coordinates suitable for map rendering.
 */
@Service
public class OrbitalMonitoringService {

    private static final double MU_KM3_S2 = 398600.4418; // Earth gravitational parameter
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final Random RANDOM = new Random();

    public SatelliteResponse buildSatelliteResponse(Satellite satellite) {
        LocationDto position      = calculateOrbitalPosition(satellite);
        double speedKmh           = calculateOrbitalSpeed(satellite.getAltitudeKm());
        double signalStrength     = calculateSignalStrength(satellite.getDataLinkStatus());

        return new SatelliteResponse(
                satellite.getId(),
                satellite.getName(),
                satellite.getOrbitType(),
                satellite.getAltitudeKm(),
                position,
                speedKmh,
                satellite.getDataLinkStatus(),
                signalStrength,
                Instant.now()
        );
    }

    private LocationDto calculateOrbitalPosition(Satellite satellite) {
        long epochSeconds    = Instant.now().getEpochSecond();
        double periodSeconds = satellite.getOrbitalPeriodMin() * 60.0;

        // Angular phase in radians based on elapsed time
        double phase = (2.0 * Math.PI * (epochSeconds % (long) periodSeconds)) / periodSeconds;

        // Longitude sweeps full 360° over one period
        double longitude = Math.toDegrees(phase) % 360.0;
        if (longitude > 180.0) longitude -= 360.0;

        // Latitude oscillates between ±inclination following the ground track
        double latitude = satellite.getInclinationDeg() * Math.sin(phase);
        latitude = Math.max(-90.0, Math.min(90.0, latitude));

        return new LocationDto(
                Math.round(latitude  * 10000.0) / 10000.0,
                Math.round(longitude * 10000.0) / 10000.0
        );
    }

    // Vis-viva simplification for circular orbit: v = sqrt(µ / r)
    private double calculateOrbitalSpeed(double altitudeKm) {
        double radius    = EARTH_RADIUS_KM + altitudeKm;
        double speedKmS  = Math.sqrt(MU_KM3_S2 / radius);
        return Math.round(speedKmS * 3600.0 * 10.0) / 10.0;
    }

    private double calculateSignalStrength(DataLinkStatus status) {
        return switch (status) {
            case ACTIVE      -> Math.round((85.0 + RANDOM.nextDouble() * 14.0) * 10.0) / 10.0;
            case DEGRADED    -> Math.round((35.0 + RANDOM.nextDouble() * 30.0) * 10.0) / 10.0;
            case OFFLINE, MAINTENANCE -> 0.0;
        };
    }
}
