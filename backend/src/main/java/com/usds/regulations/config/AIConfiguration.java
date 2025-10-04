package com.usds.regulations.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * AI Configuration Properties
 * 
 * Centralized configuration for all AI-related settings.
 * Loads from environment variables via .env file (never committed to git).
 * 
 * Security Features:
 * - API keys stored in .env file (git-ignored)
 * - Validation of all critical properties
 * - Support for multiple AI providers (Ollama, OpenAI, Anthropic, Google)
 * - Rate limiting and safety controls
 * 
 * @author USDS Development Team
 */
@Configuration
@ConfigurationProperties(prefix = "ai")
@Validated
public class AIConfiguration {

    /**
     * Load environment variables from .env file
     * This ensures API keys are never hardcoded in the application
     */
    @Bean
    public Dotenv dotenv() {
        return Dotenv.configure()
                .ignoreIfMissing() // Don't fail if .env is missing (use defaults)
                .load();
    }

    // ==================== FEATURE TOGGLE ====================
    
    @NotNull
    private Boolean featureEnabled = true;

    // ==================== DEFAULT MODEL ====================
    
    @NotBlank
    private String defaultModel = "GEMMA3_27B";

    // ==================== MODEL PARAMETERS ====================
    
    private ModelConfig model = new ModelConfig();

    public static class ModelConfig {
        @Min(1)
        private Integer maxTokens = 4000;
        
        @DecimalMin("0.0")
        private Double temperature = 0.7;
        
        @DecimalMin("0.0")
        private Double topP = 0.9;
        
        @Min(1)
        private Integer contextLength = 4096;

        // Getters and Setters
        public Integer getMaxTokens() { return maxTokens; }
        public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
        
        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }
        
        public Double getTopP() { return topP; }
        public void setTopP(Double topP) { this.topP = topP; }
        
        public Integer getContextLength() { return contextLength; }
        public void setContextLength(Integer contextLength) { this.contextLength = contextLength; }
    }

    // ==================== OLLAMA (FREE LOCAL AI) ====================
    
    private OllamaConfig ollama = new OllamaConfig();

    public static class OllamaConfig {
        @NotBlank
        private String baseUrl = "http://localhost:11434";
        
        @NotNull
        private Boolean enabled = true;

        // Getters and Setters
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    }

    // ==================== OPENAI (OPTIONAL PAID API) ====================
    
    private OpenAIConfig openai = new OpenAIConfig();

    public static class OpenAIConfig {
        private String apiKey = "";
        private String orgId = "";
        private Boolean enabled = false;

        // Getters and Setters
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { 
            this.apiKey = apiKey;
            // Auto-enable if API key is provided
            if (apiKey != null && !apiKey.isEmpty()) {
                this.enabled = true;
            }
        }
        
        public String getOrgId() { return orgId; }
        public void setOrgId(String orgId) { this.orgId = orgId; }
        
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    }

    // ==================== ANTHROPIC (OPTIONAL PAID API) ====================
    
    private AnthropicConfig anthropic = new AnthropicConfig();

    public static class AnthropicConfig {
        private String apiKey = "";
        private Boolean enabled = false;

        // Getters and Setters
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { 
            this.apiKey = apiKey;
            // Auto-enable if API key is provided
            if (apiKey != null && !apiKey.isEmpty()) {
                this.enabled = true;
            }
        }
        
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    }

    // ==================== GOOGLE AI (OPTIONAL PAID API) ====================
    
    private GoogleAIConfig google = new GoogleAIConfig();

    public static class GoogleAIConfig {
        private String apiKey = "";
        private Boolean enabled = false;

        // Getters and Setters
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { 
            this.apiKey = apiKey;
            // Auto-enable if API key is provided
            if (apiKey != null && !apiKey.isEmpty()) {
                this.enabled = true;
            }
        }
        
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    }

    // ==================== RATE LIMITING ====================
    
    private RateLimitConfig rateLimit = new RateLimitConfig();

    public static class RateLimitConfig {
        @Min(1)
        private Integer requestsPerHour = 100;
        
        @Min(1)
        private Long tokensPerDay = 1000000L;
        
        @DecimalMin("0.0")
        private BigDecimal costThresholdUsd = new BigDecimal("10.00");

        // Getters and Setters
        public Integer getRequestsPerHour() { return requestsPerHour; }
        public void setRequestsPerHour(Integer requestsPerHour) { this.requestsPerHour = requestsPerHour; }
        
        public Long getTokensPerDay() { return tokensPerDay; }
        public void setTokensPerDay(Long tokensPerDay) { this.tokensPerDay = tokensPerDay; }
        
        public BigDecimal getCostThresholdUsd() { return costThresholdUsd; }
        public void setCostThresholdUsd(BigDecimal costThresholdUsd) { this.costThresholdUsd = costThresholdUsd; }
    }

    // ==================== SAFETY & SECURITY ====================
    
    private SafetyConfig safety = new SafetyConfig();

    public static class SafetyConfig {
        @NotNull
        private Boolean contentFilterEnabled = true;
        
        @Min(1)
        private Integer maxConversationLength = 50;
        
        @Min(1)
        private Integer autoClearDays = 90;
        
        @Min(1)
        private Integer maxInputLength = 10000;
        
        @Min(1)
        private Integer maxOutputLength = 8000;

        // Getters and Setters
        public Boolean getContentFilterEnabled() { return contentFilterEnabled; }
        public void setContentFilterEnabled(Boolean contentFilterEnabled) { this.contentFilterEnabled = contentFilterEnabled; }
        
        public Integer getMaxConversationLength() { return maxConversationLength; }
        public void setMaxConversationLength(Integer maxConversationLength) { this.maxConversationLength = maxConversationLength; }
        
        public Integer getAutoClearDays() { return autoClearDays; }
        public void setAutoClearDays(Integer autoClearDays) { this.autoClearDays = autoClearDays; }
        
        public Integer getMaxInputLength() { return maxInputLength; }
        public void setMaxInputLength(Integer maxInputLength) { this.maxInputLength = maxInputLength; }
        
        public Integer getMaxOutputLength() { return maxOutputLength; }
        public void setMaxOutputLength(Integer maxOutputLength) { this.maxOutputLength = maxOutputLength; }
    }

    // ==================== CONVERSATION MANAGEMENT ====================
    
    private ConversationConfig conversation = new ConversationConfig();

    public static class ConversationConfig {
        @NotNull
        private Boolean persistenceEnabled = true;
        
        @Min(1)
        private Integer maxHistoryEntries = 100;
        
        @Min(1)
        private Integer timeoutMinutes = 60;

        // Getters and Setters
        public Boolean getPersistenceEnabled() { return persistenceEnabled; }
        public void setPersistenceEnabled(Boolean persistenceEnabled) { this.persistenceEnabled = persistenceEnabled; }
        
        public Integer getMaxHistoryEntries() { return maxHistoryEntries; }
        public void setMaxHistoryEntries(Integer maxHistoryEntries) { this.maxHistoryEntries = maxHistoryEntries; }
        
        public Integer getTimeoutMinutes() { return timeoutMinutes; }
        public void setTimeoutMinutes(Integer timeoutMinutes) { this.timeoutMinutes = timeoutMinutes; }
    }

    // ==================== ROOT GETTERS & SETTERS ====================

    public Boolean getFeatureEnabled() { return featureEnabled; }
    public void setFeatureEnabled(Boolean featureEnabled) { this.featureEnabled = featureEnabled; }

    public String getDefaultModel() { return defaultModel; }
    public void setDefaultModel(String defaultModel) { this.defaultModel = defaultModel; }

    public ModelConfig getModel() { return model; }
    public void setModel(ModelConfig model) { this.model = model; }

    public OllamaConfig getOllama() { return ollama; }
    public void setOllama(OllamaConfig ollama) { this.ollama = ollama; }

    public OpenAIConfig getOpenai() { return openai; }
    public void setOpenai(OpenAIConfig openai) { this.openai = openai; }

    public AnthropicConfig getAnthropic() { return anthropic; }
    public void setAnthropic(AnthropicConfig anthropic) { this.anthropic = anthropic; }

    public GoogleAIConfig getGoogle() { return google; }
    public void setGoogle(GoogleAIConfig google) { this.google = google; }

    public RateLimitConfig getRateLimit() { return rateLimit; }
    public void setRateLimit(RateLimitConfig rateLimit) { this.rateLimit = rateLimit; }

    public SafetyConfig getSafety() { return safety; }
    public void setSafety(SafetyConfig safety) { this.safety = safety; }

    public ConversationConfig getConversation() { return conversation; }
    public void setConversation(ConversationConfig conversation) { this.conversation = conversation; }
}
