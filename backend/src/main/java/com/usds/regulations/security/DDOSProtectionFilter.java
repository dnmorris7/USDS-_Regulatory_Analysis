package com.usds.regulations.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class DDOSProtectionFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(DDOSProtectionFilter.class);
    
    @Autowired
    private RateLimitingService rateLimitingService;
    
    // Configurable DDOS protection settings
    @Value("${rate-limit.ddos.suspicious-threshold:100}")
    private int suspiciousRequestThreshold;
    
    @Value("${rate-limit.ddos.block-duration-minutes:15}")
    private int blockDurationMinutes;
    
    @Value("${rate-limit.cache.cleanup-threshold:1000}")
    private int cacheCleanupThreshold;
    
    // Track request patterns for anomaly detection
    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> suspiciousIPs = new ConcurrentHashMap<>();
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientIP = getClientIP(httpRequest);
        String requestPath = httpRequest.getRequestURI();
        String userAgent = httpRequest.getHeader("User-Agent");
        
        logger.debug("Processing request from IP: {} to path: {}", clientIP, requestPath);
        
        // Check if IP is temporarily blocked
        if (isBlocked(clientIP)) {
            logger.warn("Blocked request from IP: {} - still in cooldown period", clientIP);
            sendRateLimitResponse(httpResponse, "IP temporarily blocked due to suspicious activity");
            return;
        }
        
        // Apply rate limiting based on endpoint type
        RateLimitingService.RateLimitType limitType = determineLimitType(requestPath);
        
        if (!rateLimitingService.tryConsume(clientIP, limitType)) {
            logger.warn("Rate limit exceeded for IP: {} on endpoint type: {}", clientIP, limitType);
            
            // Mark as suspicious after multiple rate limit violations
            markSuspicious(clientIP);
            
            sendRateLimitResponse(httpResponse, "Rate limit exceeded for " + limitType.name());
            return;
        }
        
        // Track request patterns for anomaly detection
        trackRequest(clientIP, requestPath, userAgent);
        
        // Check for suspicious patterns
        if (isSuspiciousActivity(clientIP)) {
            logger.warn("Suspicious activity detected from IP: {}", clientIP);
            markSuspicious(clientIP);
            sendRateLimitResponse(httpResponse, "Suspicious activity detected - access temporarily restricted");
            return;
        }
        
        // Request is allowed - continue with the chain
        chain.doFilter(request, response);
    }
    
    /**
     * Extracts the real client IP, considering proxies and load balancers
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty() && !"unknown".equalsIgnoreCase(xRealIP)) {
            return xRealIP;
        }
        
        String xForwarded = request.getHeader("X-Forwarded");
        if (xForwarded != null && !xForwarded.isEmpty() && !"unknown".equalsIgnoreCase(xForwarded)) {
            return xForwarded;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Determines the appropriate rate limit type based on the request path
     */
    private RateLimitingService.RateLimitType determineLimitType(String path) {
        if (path.startsWith("/api/export/")) {
            return RateLimitingService.RateLimitType.EXPORT_HEAVY;
        } else if (path.startsWith("/api/auth/")) {
            return RateLimitingService.RateLimitType.AUTH_SENSITIVE;
        }
        return RateLimitingService.RateLimitType.API_GENERAL;
    }
    
    /**
     * Checks if an IP is currently blocked
     */
    private boolean isBlocked(String ip) {
        Long blockTime = suspiciousIPs.get(ip);
        if (blockTime != null) {
            long blockDurationMs = blockDurationMinutes * 60 * 1000L;
            return System.currentTimeMillis() - blockTime < blockDurationMs;
        }
        return false;
    }
    
    /**
     * Marks an IP as suspicious and blocks it temporarily
     */
    private void markSuspicious(String ip) {
        suspiciousIPs.put(ip, System.currentTimeMillis());
        logger.info("IP {} marked as suspicious and temporarily blocked", ip);
    }
    
    /**
     * Tracks request patterns for anomaly detection
     */
    private void trackRequest(String ip, String path, String userAgent) {
        // Simple request counting for pattern analysis
        requestCounts.computeIfAbsent(ip, k -> new AtomicInteger(0)).incrementAndGet();
        
        // Clean up old entries periodically (configurable threshold)
        if (requestCounts.size() > cacheCleanupThreshold) {
            requestCounts.clear();
        }
    }
    
    /**
     * Detects suspicious activity patterns
     */
    private boolean isSuspiciousActivity(String ip) {
        AtomicInteger count = requestCounts.get(ip);
        if (count != null && count.get() > suspiciousRequestThreshold) {
            return true;
        }
        
        // Additional suspicious patterns could be added here:
        // - Rapid sequential requests
        // - Requests with suspicious user agents
        // - Requests hitting only specific endpoints
        
        return false;
    }
    
    /**
     * Sends a standardized rate limit response
     */
    private void sendRateLimitResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Content-Type", "application/json");
        response.setHeader("X-Rate-Limit-Exceeded", "true");
        response.setHeader("Retry-After", "60"); // Suggest retry after 60 seconds
        
        String jsonResponse = String.format(
            "{\"error\": \"Rate Limit Exceeded\", \"message\": \"%s\", \"timestamp\": %d}",
            message, System.currentTimeMillis()
        );
        
        response.getWriter().write(jsonResponse);
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("DDOS Protection Filter initialized");
    }
    
    @Override
    public void destroy() {
        logger.info("DDOS Protection Filter destroyed");
        requestCounts.clear();
        suspiciousIPs.clear();
    }
}