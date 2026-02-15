package com.jaypal.oms;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

/**
 * Test Configuration
 *
 * Provides test-specific beans that override production configurations.
 * Uses in-memory caching instead of Redis for faster test execution.
 */
@TestConfiguration
public class TestConfig {

    /**
     * Override Redis cache with in-memory cache for testing
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("products");
    }
}

