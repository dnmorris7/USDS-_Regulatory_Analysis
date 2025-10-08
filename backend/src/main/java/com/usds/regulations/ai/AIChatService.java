package com.usds.regulations.ai;

import com.usds.regulations.config.AIConfiguration;
import com.usds.regulations.config.AIModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Service for processing AI chat interactions
 * Integrates with Ollama and database queries
 */
@Service
public class AIChatService {

    private static final Logger logger = LoggerFactory.getLogger(AIChatService.class);

    @Autowired
    private OllamaChatModel ollamaChatModel;

    @Autowired
    private AIModelService aiModelService;

    @Autowired
    private AIQueryService aiQueryService;

    @Autowired
    private AIConfiguration aiConfig;

    /**
     * Process a chat message and return full response
     */
    public com.usds.regulations.ai.ChatResponse processMessage(ChatRequest request) {
        logger.info("Processing chat message: {}", request);

        try {
            // Validate AI is enabled
            if (!aiConfig.getFeature().isEnabled()) {
                return com.usds.regulations.ai.ChatResponse.error("AI feature is disabled");
            }

            // Get and validate model
            AIModel model = AIModel.valueOf(request.getModel().toUpperCase());
            aiModelService.validateModelSelection(model);

            // Check if question is database-related
            AIQueryService.QueryResult queryResult = aiQueryService.processQuestion(request.getMessage());

            // Build context-aware prompt
            String enhancedPrompt = buildPrompt(request.getMessage(), queryResult);

            // Configure model options
            OllamaOptions options = OllamaOptions.create()
                    .withModel(model.getModelId())
                    .withTemperature(0.7);

            // Create prompt and get response
            Prompt prompt = new Prompt(enhancedPrompt, options);
            ChatResponse chatResponse = ollamaChatModel.call(prompt);

            // Build response
            String responseText = chatResponse.getResult().getOutput().getContent();
            
            com.usds.regulations.ai.ChatResponse response = 
                com.usds.regulations.ai.ChatResponse.success(responseText, model.getDisplayName());
            response.setConversationId(request.getConversationId());
            response.setTokensUsed(estimateTokens(enhancedPrompt + responseText));

            logger.info("Chat response generated successfully");
            return response;

        } catch (IllegalArgumentException e) {
            logger.error("Invalid model or input: {}", e.getMessage());
            return com.usds.regulations.ai.ChatResponse.error("Invalid model or input: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing chat message: {}", e.getMessage(), e);
            return com.usds.regulations.ai.ChatResponse.error("Error processing message: " + e.getMessage());
        }
    }

    /**
     * Process a chat message with streaming response
     */
    public Flux<String> processMessageStreaming(ChatRequest request) {
        logger.info("Processing streaming chat message: {}", request);

        try {
            // Validate AI is enabled
            if (!aiConfig.getFeature().isEnabled()) {
                return Flux.just("AI feature is disabled");
            }

            // Get and validate model
            AIModel model = AIModel.valueOf(request.getModel().toUpperCase());
            aiModelService.validateModelSelection(model);

            // Check if question is database-related
            AIQueryService.QueryResult queryResult = aiQueryService.processQuestion(request.getMessage());

            // Build context-aware prompt
            String enhancedPrompt = buildPrompt(request.getMessage(), queryResult);

            // Configure model options
            OllamaOptions options = OllamaOptions.create()
                    .withModel(model.getModelId())
                    .withTemperature(0.7);

            // Create prompt and stream response
            Prompt prompt = new Prompt(enhancedPrompt, options);
            Flux<ChatResponse> responseFlux = ollamaChatModel.stream(prompt);

            // Extract text content from stream
            return responseFlux
                    .map(chatResponse -> chatResponse.getResult().getOutput().getContent())
                    .doOnError(error -> logger.error("Streaming error: {}", error.getMessage()))
                    .doOnComplete(() -> logger.info("Streaming completed"));

        } catch (Exception e) {
            logger.error("Error processing streaming message: {}", e.getMessage(), e);
            return Flux.just("Error: " + e.getMessage());
        }
    }

    /**
     * Build an enhanced prompt with database context
     */
    private String buildPrompt(String userMessage, AIQueryService.QueryResult queryResult) {
        StringBuilder prompt = new StringBuilder();

        // System context
        prompt.append("You are an AI assistant for the USDS Regulatory Analysis System. ");
        prompt.append("You help analysts understand and query Code of Federal Regulations (CFR) data. ");
        prompt.append("Be concise, accurate, and helpful.\n\n");

        // Add database query results if available
        if (queryResult.isSuccess() && queryResult.hasData()) {
            prompt.append("**Database Query Results:**\n");
            prompt.append(queryResult.getSummary()).append("\n\n");
            
            // Add relevant data details
            if (queryResult.getData().containsKey("count")) {
                prompt.append("Count: ").append(queryResult.getData().get("count")).append("\n");
            }
            
            prompt.append("\n");
        }

        // Add user question
        prompt.append("**User Question:**\n");
        prompt.append(userMessage).append("\n\n");

        prompt.append("**Instructions:**\n");
        prompt.append("- Use the database results above to answer the question\n");
        prompt.append("- If the question is about CFR data, reference the query results\n");
        prompt.append("- Format your response in clear, readable markdown\n");
        prompt.append("- If you don't have enough information, say so\n\n");

        prompt.append("**Your Response:**\n");

        return prompt.toString();
    }

    /**
     * Estimate token count (rough approximation)
     * 1 token ≈ 4 characters
     */
    private int estimateTokens(String text) {
        return text.length() / 4;
    }
}
