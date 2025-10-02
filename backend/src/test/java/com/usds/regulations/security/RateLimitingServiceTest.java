package com.usds.regulations.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class RateLimitingServiceTest {

    @Test
    public void testApiGeneralRateLimit() {
        RateLimitingService rateLimitingService = new RateLimitingService();
        String clientId = "test-client-1";
        
        // Should allow first 60 requests
        for (int i = 0; i < 60; i++) {
            assertTrue(rateLimitingService.tryConsume(clientId, RateLimitingService.RateLimitType.API_GENERAL), 
                "Request " + (i + 1) + " should be allowed");
        }
        
        // 61st request should be blocked
        assertFalse(rateLimitingService.tryConsume(clientId, RateLimitingService.RateLimitType.API_GENERAL),
            "61st request should be blocked");
        
        // Check available tokens
        assertEquals(0, rateLimitingService.getAvailableTokens(clientId, RateLimitingService.RateLimitType.API_GENERAL));
    }

    @Test
    public void testExportHeavyRateLimit() {
        RateLimitingService rateLimitingService = new RateLimitingService();
        String clientId = "test-client-2";
        
        // Should allow first 10 requests for heavy operations
        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimitingService.tryConsume(clientId, RateLimitingService.RateLimitType.EXPORT_HEAVY), 
                "Export request " + (i + 1) + " should be allowed");
        }
        
        // 11th request should be blocked
        assertFalse(rateLimitingService.tryConsume(clientId, RateLimitingService.RateLimitType.EXPORT_HEAVY),
            "11th export request should be blocked");
    }

    @Test
    public void testAuthSensitiveRateLimit() {
        RateLimitingService rateLimitingService = new RateLimitingService();
        String clientId = "test-client-3";
        
        // Should allow first 5 auth requests
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimitingService.tryConsume(clientId, RateLimitingService.RateLimitType.AUTH_SENSITIVE), 
                "Auth request " + (i + 1) + " should be allowed");
        }
        
        // 6th request should be blocked
        assertFalse(rateLimitingService.tryConsume(clientId, RateLimitingService.RateLimitType.AUTH_SENSITIVE),
            "6th auth request should be blocked");
    }

    @Test
    public void testDifferentClientsIndependentLimits() {
        RateLimitingService rateLimitingService = new RateLimitingService();
        String clientId1 = "client-1";
        String clientId2 = "client-2";
        
        // Consume all tokens for client 1
        for (int i = 0; i < 60; i++) {
            rateLimitingService.tryConsume(clientId1, RateLimitingService.RateLimitType.API_GENERAL);
        }
        
        // Client 1 should be blocked
        assertFalse(rateLimitingService.tryConsume(clientId1, RateLimitingService.RateLimitType.API_GENERAL));
        
        // Client 2 should still be allowed (independent limits)
        assertTrue(rateLimitingService.tryConsume(clientId2, RateLimitingService.RateLimitType.API_GENERAL));
    }

    @Test
    public void testClearRateLimit() {
        RateLimitingService rateLimitingService = new RateLimitingService();
        String clientId = "test-client-clear";
        
        // Consume all tokens
        for (int i = 0; i < 60; i++) {
            rateLimitingService.tryConsume(clientId, RateLimitingService.RateLimitType.API_GENERAL);
        }
        
        // Should be blocked
        assertFalse(rateLimitingService.tryConsume(clientId, RateLimitingService.RateLimitType.API_GENERAL));
        
        // Clear rate limit
        rateLimitingService.clearRateLimit(clientId);
        
        // Should be allowed again after clearing
        assertTrue(rateLimitingService.tryConsume(clientId, RateLimitingService.RateLimitType.API_GENERAL));
    }
}