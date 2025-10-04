package com.usds.regulations.config;

/**
 * AI Model Enumeration
 * 
 * Defines all available AI models for the USDS Regulations Analysis system.
 * 
 * Model Categories:
 * 1. FREE LOCAL (Ollama) - Run on your own hardware, no API costs
 * 2. PAID APIS - OpenAI, Anthropic, Google (require API keys)
 * 
 * Recommended Default: GEMMA3_27B (best balance of quality and speed for local use)
 * 
 * @author USDS Development Team
 */
public enum AIModel {
    
    // ==================== FREE LOCAL MODELS (Ollama) ====================
    
    /**
     * Google Gemma 3 - 27B Instruction-Tuned (Quantized)
     * Size: 18GB
     * Best for: General conversations, complex regulatory analysis
     * Speed: Moderate (8-12 tokens/sec on decent GPU)
     * RECOMMENDED: Best quality-to-speed ratio for local use
     */
    GEMMA3_27B(
        "gemma3:27b-it-qat",
        "Ollama",
        "Google Gemma 3 - 27B IT (Quantized)",
        18_000L,
        true
    ),
    
    /**
     * Yi 34B
     * Size: 19GB
     * Best for: Reasoning tasks, detailed analysis
     * Speed: Slow (6-10 tokens/sec)
     */
    YI_34B(
        "yi:34b",
        "Ollama",
        "Yi 34B",
        19_000L,
        true
    ),
    
    /**
     * DeepSeek Coder 33B
     * Size: 19GB
     * Best for: Code generation, technical documentation analysis
     * Speed: Slow (6-10 tokens/sec)
     */
    DEEPSEEK_CODER_33B(
        "deepseek-coder:33b",
        "Ollama",
        "DeepSeek Coder 33B",
        19_000L,
        true
    ),
    
    /**
     * Google Gemma 3 - 12B
     * Size: 8GB
     * Best for: Quick responses, simple queries
     * Speed: Fast (15-20 tokens/sec)
     */
    GEMMA3_12B(
        "gemma3:12b",
        "Ollama",
        "Google Gemma 3 - 12B",
        8_000L,
        true
    ),
    
    /**
     * Llama 2 - 13B
     * Size: 7GB
     * Best for: General purpose, older but reliable
     * Speed: Fast (15-20 tokens/sec)
     */
    LLAMA2_13B(
        "llama2:13b",
        "Ollama",
        "Meta Llama 2 - 13B",
        7_000L,
        true
    ),
    
    /**
     * Mistral 7B (Latest)
     * Size: 4GB
     * Best for: Very fast responses, simple tasks
     * Speed: Very Fast (20-30 tokens/sec)
     */
    MISTRAL_LATEST(
        "mistral:latest",
        "Ollama",
        "Mistral 7B",
        4_000L,
        true
    ),
    
    /**
     * Phi-3 Mini (Latest)
     * Size: 2GB
     * Best for: Extremely fast responses, basic queries
     * Speed: Extremely Fast (30-40 tokens/sec)
     */
    PHI3_LATEST(
        "phi3:latest",
        "Ollama",
        "Microsoft Phi-3 Mini",
        2_000L,
        true
    ),
    
    /**
     * Llama 3 - 70B
     * Size: 40GB
     * Best for: Highest quality local inference (requires powerful GPU)
     * Speed: Very Slow (3-6 tokens/sec)
     * WARNING: Requires significant VRAM (24GB+ GPU recommended)
     */
    LLAMA3_70B(
        "llama3:70b",
        "Ollama",
        "Meta Llama 3 - 70B",
        40_000L,
        true
    ),
    
    // ==================== PAID API MODELS ====================
    
    /**
     * OpenAI GPT-4 Turbo
     * Cost: $0.01/1K input tokens, $0.03/1K output tokens
     * Best for: Highest quality responses when cost is not a concern
     * Speed: Fast (API-dependent)
     * Requires: OPENAI_API_KEY in .env
     */
    OPENAI_GPT4_TURBO(
        "gpt-4-turbo-preview",
        "OpenAI",
        "GPT-4 Turbo",
        128_000L,
        false
    ),
    
    /**
     * OpenAI GPT-4
     * Cost: $0.03/1K input tokens, $0.06/1K output tokens
     * Best for: Production-grade responses, complex reasoning
     * Speed: Moderate (API-dependent)
     * Requires: OPENAI_API_KEY in .env
     */
    OPENAI_GPT4(
        "gpt-4",
        "OpenAI",
        "GPT-4",
        8_192L,
        false
    ),
    
    /**
     * OpenAI GPT-3.5 Turbo
     * Cost: $0.0005/1K input tokens, $0.0015/1K output tokens
     * Best for: Cost-effective API usage, simple queries
     * Speed: Very Fast (API-dependent)
     * Requires: OPENAI_API_KEY in .env
     */
    OPENAI_GPT35_TURBO(
        "gpt-3.5-turbo",
        "OpenAI",
        "GPT-3.5 Turbo",
        16_385L,
        false
    ),
    
    /**
     * Anthropic Claude 3 Opus
     * Cost: $0.015/1K input tokens, $0.075/1K output tokens
     * Best for: Long-form analysis, research tasks
     * Speed: Moderate (API-dependent)
     * Requires: ANTHROPIC_API_KEY in .env
     */
    ANTHROPIC_CLAUDE3_OPUS(
        "claude-3-opus-20240229",
        "Anthropic",
        "Claude 3 Opus",
        200_000L,
        false
    ),
    
    /**
     * Anthropic Claude 3 Sonnet
     * Cost: $0.003/1K input tokens, $0.015/1K output tokens
     * Best for: Balanced performance and cost
     * Speed: Fast (API-dependent)
     * Requires: ANTHROPIC_API_KEY in .env
     */
    ANTHROPIC_CLAUDE3_SONNET(
        "claude-3-sonnet-20240229",
        "Anthropic",
        "Claude 3 Sonnet",
        200_000L,
        false
    ),
    
    /**
     * Google Gemini Pro
     * Cost: Free tier available, then $0.00025/1K input tokens
     * Best for: Cost-effective API usage with good quality
     * Speed: Fast (API-dependent)
     * Requires: GOOGLE_AI_API_KEY in .env
     */
    GOOGLE_GEMINI_PRO(
        "gemini-pro",
        "Google",
        "Gemini Pro",
        32_768L,
        false
    );
    
    // ==================== ENUM PROPERTIES ====================
    
    private final String modelId;
    private final String provider;
    private final String displayName;
    private final Long contextWindowTokens;
    private final boolean isFree;
    
    AIModel(String modelId, String provider, String displayName, Long contextWindowTokens, boolean isFree) {
        this.modelId = modelId;
        this.provider = provider;
        this.displayName = displayName;
        this.contextWindowTokens = contextWindowTokens;
        this.isFree = isFree;
    }
    
    // ==================== GETTERS ====================
    
    /**
     * Get the actual model identifier used by the AI provider
     */
    public String getModelId() {
        return modelId;
    }
    
    /**
     * Get the provider name (Ollama, OpenAI, Anthropic, Google)
     */
    public String getProvider() {
        return provider;
    }
    
    /**
     * Get the user-friendly display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the context window size in tokens
     */
    public Long getContextWindowTokens() {
        return contextWindowTokens;
    }
    
    /**
     * Check if this model is free to use (local Ollama)
     */
    public boolean isFree() {
        return isFree;
    }
    
    /**
     * Check if this model requires an API key
     */
    public boolean requiresApiKey() {
        return !isFree;
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Get model by name (case-insensitive)
     */
    public static AIModel fromString(String modelName) {
        if (modelName == null) {
            return GEMMA3_27B; // Default
        }
        
        try {
            return AIModel.valueOf(modelName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Unknown AI model: " + modelName + ". Available models: " + 
                String.join(", ", getAvailableModelNames())
            );
        }
    }
    
    /**
     * Get all available model names
     */
    public static String[] getAvailableModelNames() {
        AIModel[] models = AIModel.values();
        String[] names = new String[models.length];
        for (int i = 0; i < models.length; i++) {
            names[i] = models[i].name();
        }
        return names;
    }
    
    /**
     * Get all free (Ollama) models
     */
    public static AIModel[] getFreeModels() {
        return java.util.Arrays.stream(AIModel.values())
            .filter(AIModel::isFree)
            .toArray(AIModel[]::new);
    }
    
    /**
     * Get all paid API models
     */
    public static AIModel[] getPaidModels() {
        return java.util.Arrays.stream(AIModel.values())
            .filter(model -> !model.isFree())
            .toArray(AIModel[]::new);
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s) - %s [%dK tokens]%s",
            displayName,
            provider,
            modelId,
            contextWindowTokens / 1000,
            isFree ? " [FREE]" : ""
        );
    }
}
