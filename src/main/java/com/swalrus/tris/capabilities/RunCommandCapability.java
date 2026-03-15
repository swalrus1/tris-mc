package com.swalrus.tris.capabilities;

import com.google.gson.JsonObject;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class RunCommandCapability implements Capability {
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean capturing = new AtomicBoolean(false);
    private final List<String> capturedMessages = new ArrayList<>();

    @Override
    public String name() {
        return "executeCommand";
    }

    @Override
    public ToolSpecification toolSpec() {
        return ToolSpecification.builder()
                .name(name())
                .description("Execute a Minecraft in-game command")
                .parameters(JsonObjectSchema.builder()
                        .addStringProperty("command", "The command to execute (with or without leading slash)")
                        .required("command")
                        .build())
                .build();
    }

    @Override
    public void onGameMessage(Component message) {
        if (capturing.get()) {
            capturedMessages.add(message.getString());
        }
    }

    @Override
    public CompletableFuture<String> execute(Minecraft client, JsonObject args) {
        String rawCmd = args.get("command").getAsString();
        String cmd = rawCmd.startsWith("/") ? rawCmd.substring(1) : rawCmd;

        synchronized (capturedMessages) {
            capturedMessages.clear();
        }
        capturing.set(true);

        client.execute(() -> client.getConnection().sendCommand(cmd));

        CompletableFuture<String> future = new CompletableFuture<>();
        SCHEDULER.schedule(() -> {
            capturing.set(false);
            String output;
            synchronized (capturedMessages) {
                output = capturedMessages.isEmpty()
                        ? "(no output)"
                        : String.join("\n", capturedMessages);
            }
            future.complete(output);
        }, 500, TimeUnit.MILLISECONDS);

        return future;
    }
}
