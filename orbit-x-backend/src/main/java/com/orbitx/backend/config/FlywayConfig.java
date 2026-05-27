package com.orbitx.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway strategy: repair() before migrate().
 *
 * On Oracle (non-transactional DDL), a failed migration leaves a FAILED
 * entry in flyway_schema_history.  repair() removes that entry so the
 * fixed script can be re-applied on the next startup.
 */
@Slf4j
@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy repairThenMigrate() {
        return (Flyway flyway) -> {
            log.info("Flyway: running repair() before migrate() to clear any failed migration entries");
            flyway.repair();
            flyway.migrate();
        };
    }
}
