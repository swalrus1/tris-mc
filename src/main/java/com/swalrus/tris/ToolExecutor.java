package com.swalrus.tris;

import com.google.gson.JsonObject;
import com.swalrus.tris.capabilities.Capability;
import com.swalrus.tris.capabilities.RunCommandCapability;
import com.swalrus.tris.capabilities.ScreenshotCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ToolExecutor {
    public static final List<Capability> ALL = List.of(
            new RunCommandCapability(),
            new ScreenshotCapability()
    );

    private static final Map<String, Capability> BY_NAME = ALL.stream()
            .collect(Collectors.toMap(Capability::name, c -> c));

    public static void onGameMessage(Component message) {
        ALL.forEach(c -> c.onGameMessage(message));
    }

    public static CompletableFuture<String> execute(Minecraft client, String toolName, JsonObject args) {
        Capability cap = BY_NAME.get(toolName);
        if (cap == null) return CompletableFuture.completedFuture("Unknown tool: " + toolName);
        return cap.execute(client, args);
    }
}
