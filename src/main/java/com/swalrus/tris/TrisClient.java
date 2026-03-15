package com.swalrus.tris;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class TrisClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AutoConfig.register(TrisConfig.class, GsonConfigSerializer::new);
        TrisCommand.register();
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> ToolExecutor.onGameMessage(message));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> TrisCommand.clearHistory());
    }
}
