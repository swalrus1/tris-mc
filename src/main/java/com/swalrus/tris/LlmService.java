package com.swalrus.tris;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.swalrus.tris.capabilities.Capability;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.DefaultChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class LlmService {
    private static final Gson GSON = new Gson();
    private static final List<ToolSpecification> TOOLS = ToolExecutor.ALL.stream()
            .map(Capability::toolSpec)
            .toList();

    public static ChatLanguageModel createModel(TrisConfig config) {
        String key = config.apiKey == null ? "" : config.apiKey.trim();
        String model = config.model == null ? "" : config.model.trim();
        return switch (config.provider) {
            case OPENAI -> OpenAiChatModel.builder()
                    .apiKey(key)
                    .modelName(model)
                    .build();
            case ANTHROPIC -> AnthropicChatModel.builder()
                    .apiKey(key)
                    .modelName(model)
                    .build();
            case GOOGLE_GEMINI -> GoogleAiGeminiChatModel.builder()
                    .apiKey(key)
                    .modelName(model)
                    .build();
            case DEEPSEEK -> OpenAiChatModel.builder()
                    .baseUrl("https://api.deepseek.com/v1")
                    .apiKey(key)
                    .modelName(model)
                    .build();
            case MISTRAL -> MistralAiChatModel.builder()
                    .apiKey(key)
                    .modelName(model)
                    .build();
            case OLLAMA -> OllamaChatModel.builder()
                    .baseUrl(key.isBlank() ? "http://localhost:11434" : key)
                    .modelName(model)
                    .build();
        };
    }

    /**
     * Runs the conversation loop until the model returns a plain text response.
     * Mutates {@code history} in place (appends AI messages and tool results).
     */
    public static String chat(ChatLanguageModel model, List<ChatMessage> history, Minecraft client) throws Exception {
        while (true) {
            ChatRequest request = ChatRequest.builder()
                    .messages(history)
                    .parameters(DefaultChatRequestParameters.builder()
                            .toolSpecifications(TOOLS)
                            .build())
                    .build();

            ChatResponse response = model.chat(request);
            AiMessage aiMessage = response.aiMessage();
            history.add(aiMessage);

            if (!aiMessage.hasToolExecutionRequests()) {
                return aiMessage.text();
            }

            String pendingScreenshot = null;
            ToolExecutionRequest screenshotRequest = null;

            for (ToolExecutionRequest toolReq : aiMessage.toolExecutionRequests()) {
                JsonObject args = toolReq.arguments() != null && !toolReq.arguments().isBlank()
                        ? GSON.fromJson(toolReq.arguments(), JsonObject.class)
                        : new JsonObject();
                String result = ToolExecutor.execute(client, toolReq.name(), args).get(30, TimeUnit.SECONDS);

                if ("getScreenshot".equals(toolReq.name())) {
                    pendingScreenshot = result;
                    screenshotRequest = toolReq;
                    history.add(ToolExecutionResultMessage.from(toolReq, "Screenshot captured successfully."));
                } else {
                    history.add(ToolExecutionResultMessage.from(toolReq, result));
                }
            }

            // Inject screenshot image as a user message so vision-capable models can see it
            if (pendingScreenshot != null) {
                history.add(UserMessage.from(
                        TextContent.from("Here is the screenshot:"),
                        ImageContent.from(pendingScreenshot, "image/png")
                ));
            }
        }
    }
}
