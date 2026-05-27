package com.orbitx.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine-backed in-process cache.
 *
 * Cache strategy:
 *  - satellites    : short TTL (15 s) — positions update every few seconds
 *  - datacenters   : medium TTL (30 s) — list changes rarely, temperatures fluctuate
 *  - sustainability: long TTL  (5 min) — ESG scores are near-static per session
 *  - kpis          : NOT cached — always real-time, served fresh per request
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_DATACENTERS   = "datacenters";
    public static final String CACHE_SATELLITES    = "satellites";
    public static final String CACHE_SUSTAINABILITY = "sustainability";

    @Value("${cache.datacenters-ttl-seconds:30}")
    private long datacentersTtl;

    @Value("${cache.satellites-ttl-seconds:15}")
    private long satellitesTtl;

    @Value("${cache.sustainability-ttl-seconds:300}")
    private long sustainabilityTtl;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.registerCustomCache(CACHE_DATACENTERS,
                Caffeine.newBuilder()
                        .expireAfterWrite(datacentersTtl, TimeUnit.SECONDS)
                        .maximumSize(500)
                        .recordStats()
                        .build());
        manager.registerCustomCache(CACHE_SATELLITES,
                Caffeine.newBuilder()
                        .expireAfterWrite(satellitesTtl, TimeUnit.SECONDS)
                        .maximumSize(200)
                        .recordStats()
                        .build());
        manager.registerCustomCache(CACHE_SUSTAINABILITY,
                Caffeine.newBuilder()
                        .expireAfterWrite(sustainabilityTtl, TimeUnit.SECONDS)
                        .maximumSize(50)
                        .recordStats()
                        .build());
        return manager;
    }
}
