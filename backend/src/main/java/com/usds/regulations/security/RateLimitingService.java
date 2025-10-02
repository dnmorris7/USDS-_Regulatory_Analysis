package com.usds.regulations.security;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class RateLimitingService {
    
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    
    // Rate limit configurations from application.properties
    @Value("${rate-limit.api.general.requests-per-minute:60}")
    private int apiCallsPerMinute;
    
    @Value("${rate-limit.api.export.requests-per-hour:10}")
    private int exportCallsPerHour;
    
    @Value("${rate-limit.api.auth.requests-per-minute:5}")
    private int authAttemptsPerMinute;
    
    @Value("${rate-limit.cache.max-size:1000}")
    private int cacheMaxSize;
    
    /**
     * Creates a new bucket with appropriate rate limits based on the type
     */
    public Bucket createNewBucket(RateLimitType type) {
        return switch (type) {
            case API_GENERAL -> Bucket.builder()
                .addLimit(Bandwidth.classic(apiCallsPerMinute, Refill.intervally(apiCallsPerMinute, Duration.ofMinutes(1))))
                .build();
            case EXPORT_HEAVY -> Bucket.builder()
                .addLimit(Bandwidth.classic(exportCallsPerHour, Refill.intervally(exportCallsPerHour, Duration.ofHours(1))))
                .build();
            case AUTH_SENSITIVE -> Bucket.builder()
                .addLimit(Bandwidth.classic(authAttemptsPerMinute, Refill.intervally(authAttemptsPerMinute, Duration.ofMinutes(1))))
                .build();
        };
    }
    
    /**
     * Resolves a bucket for the given key, creating one if it doesn't exist
     */
    public Bucket resolveBucket(String key, RateLimitType type) {
        return cache.computeIfAbsent(key, k -> createNewBucket(type));
    }
    
    /**
     * Attempts to consume one token from the bucket for the given client and type
     * @return true if the request is allowed, false if rate limit exceeded
     */
    public boolean tryConsume(String clientId, RateLimitType type) {
        return resolveBucket(clientId, type).tryConsume(1);
    }
    
    /**
     * Gets available tokens for a client (useful for debugging/monitoring)
     */
    public long getAvailableTokens(String clientId, RateLimitType type) {
        return resolveBucket(clientId, type).getAvailableTokens();
    }
    
    /**
     * Clears rate limit data for a specific client (admin function)
     */
    public void clearRateLimit(String clientId) {
        cache.remove(clientId);
    }
    
    /**
     * Clears all rate limit data (admin function)
     */
    public void clearAllRateLimits() {
        cache.clear();
    }
    
    /**
     * Gets current cache size for monitoring
     */
    public int getCacheSize() {
        return cache.size();
    }
    
    /**
     * Different types of rate limits for different endpoint categories
     */
    public enum RateLimitType {
        API_GENERAL,    // Standard API endpoints - 60 requests/minute
        EXPORT_HEAVY,   // Heavy operations like CSV export - 10 requests/hour
        AUTH_SENSITIVE  // Authentication endpoints - 5 requests/minute
    }
}