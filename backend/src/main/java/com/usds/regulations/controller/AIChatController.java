package com.usds.regulations.controller;

import com.usds.regulations.ai.*;
import com.usds.regulations.config.AIConfiguration;
import com.usds.regulations.config.AIModel;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * REST Controller for AI Chat functionality
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIChatController {

    private static final Logger logger = LoggerFactory.getLogger(AIChatController.class);

    @Autowired
    private AIChatService aiChatService;

    @Autowired
    private AIModelService aiModelService;

    @Autowired
    private AIConfiguration aiConfig;

    /**
     * Get available AI models
     */
    @GetMapping("/models")
    public ResponseEntity<?> getAvailableModels() {
        try {
            List<AIModel> availableModels = aiModelService.getAvailableModels();
            AIModelService.OllamaInfo ollamaInfo = aiModelService.getOllamaInfo();

            var modelDetails = availableModels.stream()
                    .map(model -> Map.of(
                            "id", model.name(),
                            "displayName", model.getDisplayName(),
                            "provider", model.getProvider(),
                            "modelName", model.getModelName()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "models", modelDetails,
                    "defaultModel", aiModelService.getDefaultModel().name(),
                    "featureEnabled", aiConfig.getFeature().isEnabled(),
                    "ollama", Map.of(
                            "available", ollamaInfo.isAvailable(),
                            "baseUrl", ollamaInfo.getBaseUrl(),
                            "pulledModels", ollamaInfo.getPulledModels()
                    )
            ));
        } catch (Exception e) {
            logger.error("Error getting available models: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to get available models",
                    "details", e.getMessage()
            ));
        }
    }

    /**
     * Send a chat message and get full response
     */
    @PostMapping("/chat")
    public ResponseEntity<?> chat(@Valid @RequestBody ChatRequest request) {
        logger.info("Received chat request: {}", request);

        try {
            ChatResponse response = aiChatService.processMessage(request);

            if (!response.isSuccess()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", response.getError()
                ));
            }

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid request",
                    "details", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Error processing chat: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to process chat",
                    "details", e.getMessage()
            ));
        }
    }

    /**
     * Send a chat message with streaming response (SSE)
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStreaming(@Valid @RequestBody ChatRequest request) {
        logger.info("Received streaming chat request: {}", request);
        request.setStream(true);
        
        return aiChatService.processMessageStreaming(request)
                .doOnError(error -> logger.error("Streaming error: {}", error.getMessage()))
                .doOnComplete(() -> logger.info("Streaming completed"));
    }

    /**
     * Check Ollama status
     */
    @GetMapping("/ollama/status")
    public ResponseEntity<?> getOllamaStatus() {
        AIModelService.OllamaInfo info = aiModelService.getOllamaInfo();

        return ResponseEntity.ok(Map.of(
                "available", info.isAvailable(),
                "baseUrl", info.getBaseUrl(),
                "pulledModels", info.getPulledModels(),
                "message", info.isAvailable()
                        ? "Ollama is running and ready"
                        : "Ollama is not available. Check if Docker container is running."
        ));
    }

    /**
     * Get AI configuration (admin only)
     */
    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getConfiguration() {
        return ResponseEntity.ok(Map.of(
                "enabled", aiConfig.getFeature().isEnabled(),
                "defaultModel", aiConfig.getFeature().getDefaultModel(),
                "maxTokens", aiConfig.getFeature().getMaxTokens(),
                "rateLimit", Map.of(
                        "requestsPerHour", aiConfig.getRateLimit().getRequestsPerHour(),
                        "tokensPerDay", aiConfig.getRateLimit().getTokensPerDay(),
                        "costThresholdUsd", aiConfig.getRateLimit().getCostThresholdUsd()
                ),
                "providers", Map.of(
                        "openai", aiConfig.getOpenai().isEnabled(),
                        "anthropic", aiConfig.getAnthropic().isEnabled(),
                        "google", aiConfig.getGoogle().isEnabled(),
                        "ollama", aiModelService.isOllamaAvailable()
                )
        ));
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        boolean ollamaAvailable = aiModelService.isOllamaAvailable();
        boolean aiEnabled = aiConfig.getFeature().isEnabled();
        List<AIModel> availableModels = aiModelService.getAvailableModels();

        return ResponseEntity.ok(Map.of(
                "status", aiEnabled && !availableModels.isEmpty() ? "UP" : "DOWN",
                "aiEnabled", aiEnabled,
                "ollamaAvailable", ollamaAvailable,
                "availableModelsCount", availableModels.size()
        ));
    }
}
