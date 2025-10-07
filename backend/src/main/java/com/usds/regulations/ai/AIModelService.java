package com.usds.regulations.ai;

import com.usds.regulations.config.AIConfiguration;
import com.usds.regulations.config.AIModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing AI model availability and selection
 */
@Service
public class AIModelService {

    private static final Logger logger = LoggerFactory.getLogger(AIModelService.class);

    @Autowired
    private AIConfiguration aiConfig;

    @Value("${ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Get all available models based on current configuration
     */
    public List<AIModel> getAvailableModels() {
        return Arrays.stream(AIModel.values())
                .filter(this::isModelAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Check if a specific model is available
     */
    public boolean isModelAvailable(AIModel model) {
        return switch (model.getProvider()) {
            case "openai" -> aiConfig.getOpenai().isEnabled();
            case "anthropic" -> aiConfig.getAnthropic().isEnabled();
            case "google" -> aiConfig.getGoogle().isEnabled();
            case "ollama" -> isOllamaAvailable() && isModelPulledInOllama(model.getModelName());
            default -> false;
        };
    }

    /**
     * Check if Ollama service is running
     */
    public boolean isOllamaAvailable() {
        try {
            String response = restTemplate.getForObject(ollamaBaseUrl + "/api/tags", String.class);
            logger.info("Ollama is available at {}", ollamaBaseUrl);
            return response != null;
        } catch (Exception e) {
            logger.warn("Ollama is not available at {}: {}", ollamaBaseUrl, e.getMessage());
            return false;
        }
    }

    /**
     * Check if a specific model is pulled in Ollama
     */
    private boolean isModelPulledInOllama(String modelName) {
        try {
            String response = restTemplate.getForObject(ollamaBaseUrl + "/api/tags", String.class);
            boolean isPulled = response != null && response.contains("\"name\":\"" + modelName + "\"");
            if (!isPulled) {
                logger.debug("Model {} is not pulled in Ollama", modelName);
            }
            return isPulled;
        } catch (Exception e) {
            logger.error("Error checking Ollama models: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get the default model (prioritize Ollama/free models)
     */
    public AIModel getDefaultModel() {
        // Try configured default first
        String defaultModelName = aiConfig.getFeature().getDefaultModel();
        
        try {
            AIModel defaultModel = AIModel.valueOf(defaultModelName);
            if (isModelAvailable(defaultModel)) {
                logger.info("Using configured default model: {}", defaultModel.getDisplayName());
                return defaultModel;
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid default model configured: {}", defaultModelName);
        }
        
        // Prioritize free Ollama models
        List<AIModel> availableModels = getAvailableModels();
        
        AIModel ollamaModel = availableModels.stream()
                .filter(m -> "ollama".equals(m.getProvider()))
                .findFirst()
                .orElse(null);
        
        if (ollamaModel != null) {
            logger.info("Using first available Ollama model: {}", ollamaModel.getDisplayName());
            return ollamaModel;
        }
        
        // Fall back to any available model
        return availableModels.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                    "No AI models are available. Please configure API keys or pull Ollama models."
                ));
    }

    /**
     * Validate model selection before processing
     */
    public void validateModelSelection(AIModel model) {
        if (!aiConfig.getFeature().isEnabled()) {
            throw new IllegalStateException("AI feature is disabled");
        }
        
        if (!isModelAvailable(model)) {
            String errorMsg = "ollama".equals(model.getProvider())
                ? String.format("Model %s is not pulled in Ollama. Run: docker exec -it ollama ollama pull %s", 
                    model.getDisplayName(), model.getModelName())
                : String.format("Model %s requires an API key in .env file", model.getDisplayName());
            
            throw new IllegalArgumentException(errorMsg);
        }
    }

    /**
     * Get Ollama status information
     */
    public OllamaInfo getOllamaInfo() {
        boolean available = isOllamaAvailable();
        List<String> pulledModels = List.of();
        
        if (available) {
            try {
                String response = restTemplate.getForObject(ollamaBaseUrl + "/api/tags", String.class);
                pulledModels = Arrays.stream(AIModel.values())
                        .filter(m -> "ollama".equals(m.getProvider()))
                        .filter(m -> response != null && response.contains("\"name\":\"" + m.getModelName() + "\""))
                        .map(AIModel::getDisplayName)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                logger.error("Error parsing Ollama models: {}", e.getMessage());
            }
        }
        
        return new OllamaInfo(available, ollamaBaseUrl, pulledModels);
    }

    /**
     * Ollama status information
     */
    public static class OllamaInfo {
        private final boolean available;
        private final String baseUrl;
        private final List<String> pulledModels;

        public OllamaInfo(boolean available, String baseUrl, List<String> pulledModels) {
            this.available = available;
            this.baseUrl = baseUrl;
            this.pulledModels = pulledModels;
        }

        public boolean isAvailable() {
            return available;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public List<String> getPulledModels() {
            return pulledModels;
        }
    }
}
