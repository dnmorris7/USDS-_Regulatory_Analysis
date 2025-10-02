package com.usds.regulations.controller;

import com.usds.regulations.security.RateLimitingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/rate-limit")
@CrossOrigin(origins = "*")
public class RateLimitController {

    @Autowired
    private RateLimitingService rateLimitingService;
    
    // Inject configuration values for display
    @Value("${rate-limit.api.general.requests-per-minute:60}")
    private int apiCallsPerMinute;
    
    @Value("${rate-limit.api.export.requests-per-hour:10}")
    private int exportCallsPerHour;
    
    @Value("${rate-limit.api.auth.requests-per-minute:5}")
    private int authAttemptsPerMinute;
    
    @Value("${rate-limit.ddos.suspicious-threshold:100}")
    private int suspiciousThreshold;
    
    @Value("${rate-limit.ddos.block-duration-minutes:15}")
    private int blockDurationMinutes;

    /**
     * Get rate limiting status for a specific client IP
     */
    @GetMapping("/status/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRateLimitStatus(@PathVariable String clientId) {
        Map<String, Object> status = new HashMap<>();
        
        status.put("clientId", clientId);
        status.put("apiGeneral", rateLimitingService.getAvailableTokens(clientId, RateLimitingService.RateLimitType.API_GENERAL));
        status.put("exportHeavy", rateLimitingService.getAvailableTokens(clientId, RateLimitingService.RateLimitType.EXPORT_HEAVY));
        status.put("authSensitive", rateLimitingService.getAvailableTokens(clientId, RateLimitingService.RateLimitType.AUTH_SENSITIVE));
        
        return ResponseEntity.ok(status);
    }

    /**
     * Clear rate limit for a specific client (admin function)
     */
    @DeleteMapping("/clear/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> clearRateLimit(@PathVariable String clientId) {
        rateLimitingService.clearRateLimit(clientId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Rate limit cleared for client: " + clientId);
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Clear all rate limits (admin function)
     */
    @DeleteMapping("/clear-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> clearAllRateLimits() {
        rateLimitingService.clearAllRateLimits();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "All rate limits cleared");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get overall rate limiting statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRateLimitStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("activeBuckets", rateLimitingService.getCacheSize());
        stats.put("timestamp", System.currentTimeMillis());
        
        // Rate limit configurations (from application.properties)
        Map<String, Object> config = new HashMap<>();
        config.put("API_GENERAL", apiCallsPerMinute + " requests/minute");
        config.put("EXPORT_HEAVY", exportCallsPerHour + " requests/hour");
        config.put("AUTH_SENSITIVE", authAttemptsPerMinute + " requests/minute");
        config.put("DDOS_SUSPICIOUS_THRESHOLD", suspiciousThreshold + " requests");
        config.put("DDOS_BLOCK_DURATION", blockDurationMinutes + " minutes");
        stats.put("configuration", config);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Test endpoint to simulate rate limiting
     */
    @GetMapping("/test/{clientId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testRateLimit(@PathVariable String clientId) {
        Map<String, Object> result = new HashMap<>();
        
        boolean allowed = rateLimitingService.tryConsume(clientId, RateLimitingService.RateLimitType.API_GENERAL);
        long remainingTokens = rateLimitingService.getAvailableTokens(clientId, RateLimitingService.RateLimitType.API_GENERAL);
        
        result.put("clientId", clientId);
        result.put("allowed", allowed);
        result.put("remainingTokens", remainingTokens);
        result.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(result);
    }
}