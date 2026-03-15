package com.swalrus.tris.capabilities;

import com.google.gson.JsonObject;
import dev.langchain4j.agent.tool.ToolSpecification;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;

public interface Capability {
    String name();
    ToolSpecification toolSpec();
    CompletableFuture<String> execute(Minecraft client, JsonObject args);
    default void onGameMessage(Component message) {}
}
