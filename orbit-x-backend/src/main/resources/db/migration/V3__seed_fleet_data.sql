INSERT INTO datacenters (name, city, country, latitude, longitude, thermal_state, regional_consumption_kwh, capacity_servers, active, created_at)
SELECT 'Orbit X - Sao Paulo Alpha', 'Sao Paulo', 'Brazil', -23.5505, -46.6333, 'STABLE', 18500.00, 4200, 1, SYSTIMESTAMP
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM datacenters WHERE name = 'Orbit X - Sao Paulo Alpha');

INSERT INTO datacenters (name, city, country, latitude, longitude, thermal_state, regional_consumption_kwh, capacity_servers, active, created_at)
SELECT 'Orbit X - Frankfurt Prime', 'Frankfurt', 'Germany', 50.1109, 8.6821, 'OPTIMAL', 22100.00, 6800, 1, SYSTIMESTAMP
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM datacenters WHERE name = 'Orbit X - Frankfurt Prime');

INSERT INTO datacenters (name, city, country, latitude, longitude, thermal_state, regional_consumption_kwh, capacity_servers, active, created_at)
SELECT 'Orbit X - Singapore Hub', 'Singapore', 'Singapore', 1.3521, 103.8198, 'STABLE', 19750.00, 5100, 1, SYSTIMESTAMP
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM datacenters WHERE name = 'Orbit X - Singapore Hub');

INSERT INTO datacenters (name, city, country, latitude, longitude, thermal_state, regional_consumption_kwh, capacity_servers, active, created_at)
SELECT 'Orbit X - New York Edge', 'New York', 'United States', 40.7128, -74.0060, 'OPTIMAL', 24300.00, 7200, 1, SYSTIMESTAMP
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM datacenters WHERE name = 'Orbit X - New York Edge');

INSERT INTO datacenters (name, city, country, latitude, longitude, thermal_state, regional_consumption_kwh, capacity_servers, active, created_at)
SELECT 'Orbit X - Tokyo Nexus', 'Tokyo', 'Japan', 35.6762, 139.6503, 'CRITICAL', 16900.00, 3900, 1, SYSTIMESTAMP
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM datacenters WHERE name = 'Orbit X - Tokyo Nexus');

INSERT INTO datacenters (name, city, country, latitude, longitude, thermal_state, regional_consumption_kwh, capacity_servers, active, created_at)
SELECT 'Orbit X - London Core', 'London', 'United Kingdom', 51.5074, -0.1278, 'STABLE', 20400.00, 5600, 1, SYSTIMESTAMP
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM datacenters WHERE name = 'Orbit X - London Core');

INSERT INTO satellites (name, orbit_type, altitude_km, inclination_deg, orbital_period_min, data_link_status, active, launched_at)
SELECT 'OX-SAT-01 Atlas', 'LEO', 550.0, 53.0, 95.5, 'ACTIVE', 1, TIMESTAMP '2023-03-15 10:00:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM satellites WHERE name = 'OX-SAT-01 Atlas');

INSERT INTO satellites (name, orbit_type, altitude_km, inclination_deg, orbital_period_min, data_link_status, active, launched_at)
SELECT 'OX-SAT-02 Helios', 'LEO', 570.0, 53.0, 96.1, 'ACTIVE', 1, TIMESTAMP '2023-06-20 14:30:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM satellites WHERE name = 'OX-SAT-02 Helios');

INSERT INTO satellites (name, orbit_type, altitude_km, inclination_deg, orbital_period_min, data_link_status, active, launched_at)
SELECT 'OX-SAT-03 Kronos', 'MEO', 8000.0, 55.0, 285.0, 'ACTIVE', 1, TIMESTAMP '2023-09-10 08:00:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM satellites WHERE name = 'OX-SAT-03 Kronos');

INSERT INTO satellites (name, orbit_type, altitude_km, inclination_deg, orbital_period_min, data_link_status, active, launched_at)
SELECT 'OX-SAT-04 Aurora', 'LEO', 530.0, 97.6, 95.0, 'DEGRADED', 1, TIMESTAMP '2024-01-22 18:00:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM satellites WHERE name = 'OX-SAT-04 Aurora');

INSERT INTO satellites (name, orbit_type, altitude_km, inclination_deg, orbital_period_min, data_link_status, active, launched_at)
SELECT 'OX-SAT-05 Zenith', 'GEO', 35786.0, 0.0, 1436.1, 'ACTIVE', 1, TIMESTAMP '2022-11-05 12:00:00'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM satellites WHERE name = 'OX-SAT-05 Zenith');
