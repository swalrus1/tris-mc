package com.swalrus.tris;

public enum LlmProvider {
    OPENAI("OpenAI"),
    ANTHROPIC("Anthropic"),
    GOOGLE_GEMINI("Google Gemini"),
    DEEPSEEK("DeepSeek"),
    MISTRAL("Mistral"),
    OLLAMA("Ollama (local)");

    private final String displayName;

    LlmProvider(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
