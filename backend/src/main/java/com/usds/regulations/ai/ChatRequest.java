package com.usds.regulations.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request object for AI chat messages
 */
public class ChatRequest {
    
    @NotBlank(message = "Message cannot be empty")
    @Size(max = 4000, message = "Message too long (max 4000 characters)")
    private String message;
    
    @NotBlank(message = "Model must be specified")
    private String model;
    
    private String conversationId;
    
    private String userId;
    
    private Boolean stream = false;

    // Constructors
    public ChatRequest() {}
    
    public ChatRequest(String message, String model) {
        this.message = message;
        this.model = model;
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    @Override
    public String toString() {
        return "ChatRequest{" +
                "message='" + message + '\'' +
                ", model='" + model + '\'' +
                ", conversationId='" + conversationId + '\'' +
                ", userId='" + userId + '\'' +
                ", stream=" + stream +
                '}';
    }
}
