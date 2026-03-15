package com.swalrus.tris;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class TrisCommand {
    private static final List<ChatMessage> history = new ArrayList<>();
    private static ChatLanguageModel cachedModel;
    private static String cachedConfigState;

    public static void clearHistory() {
        history.clear();
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(
                ClientCommandManager.literal("tris")
                    .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String message = StringArgumentType.getString(ctx, "message");
                            sendToLlm(ctx.getSource().getClient(), message);
                            return 1;
                        }))
            )
        );
    }

    private static void sendToLlm(Minecraft client, String userMessage) {
        TrisConfig config = TrisConfig.get();

        boolean needsKey = config.provider != LlmProvider.OLLAMA;
        if (needsKey && (config.apiKey == null || config.apiKey.isBlank())) {
            client.player.displayClientMessage(
                    Component.literal("§cTris: API key not set. Open the Tris config via Mod Menu."), false);
            return;
        }

        // Rebuild model only when config changes
        String configState = config.provider + ":" + config.model + ":" + config.apiKey + ":" + config.systemPrompt;
        if (cachedModel == null || !configState.equals(cachedConfigState)) {
            cachedModel = LlmService.createModel(config);
            cachedConfigState = configState;
            history.clear();
        }

        if (history.isEmpty()) {
            String systemText = "Always respond in English.";
            if (config.systemPrompt != null && !config.systemPrompt.isBlank()) {
                systemText += "\n" + config.systemPrompt.trim();
            }
            history.add(SystemMessage.from(systemText));
        }

        history.add(UserMessage.from(userMessage));
        client.player.displayClientMessage(Component.literal("§7You: " + userMessage), false);

        int historySize = history.size();
        ChatLanguageModel model = cachedModel;
        Thread.ofVirtual().start(() -> {
            try {
                String response = LlmService.chat(model, history, client);
                String prefix = config.provider.toString();
                client.execute(() -> client.player.displayClientMessage(
                        Component.literal("§b" + prefix + ": " + response), false));
            } catch (Exception e) {
                while (history.size() > historySize) history.remove(history.size() - 1);
                client.execute(() -> client.player.displayClientMessage(
                        Component.literal("§cTris error: " + e.getMessage()), false));
            }
        });
    }
}
