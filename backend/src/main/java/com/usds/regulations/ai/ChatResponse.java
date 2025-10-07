package com.usds.regulations.ai;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response object for AI chat messages
 */
public class ChatResponse {
    
    private String response;
    private String model;
    private Integer tokensUsed;
    private LocalDateTime timestamp;
    private String conversationId;
    private Map<String, Object> metadata;
    private boolean success;
    private String error;

    // Constructors
    public ChatResponse() {
        this.timestamp = LocalDateTime.now();
        this.success = true;
    }
    
    public ChatResponse(String response, String model) {
        this();
        this.response = response;
        this.model = model;
    }

    // Static factory methods
    public static ChatResponse success(String response, String model) {
        ChatResponse chatResponse = new ChatResponse(response, model);
        chatResponse.setSuccess(true);
        return chatResponse;
    }
    
    public static ChatResponse error(String errorMessage) {
        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setSuccess(false);
        chatResponse.setError(errorMessage);
        return chatResponse;
    }

    // Getters and Setters
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getTokensUsed() {
        return tokensUsed;
    }

    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "ChatResponse{" +
                "response='" + response + '\'' +
                ", model='" + model + '\'' +
                ", tokensUsed=" + tokensUsed +
                ", timestamp=" + timestamp +
                ", conversationId='" + conversationId + '\'' +
                ", success=" + success +
                ", error='" + error + '\'' +
                '}';
    }
}
