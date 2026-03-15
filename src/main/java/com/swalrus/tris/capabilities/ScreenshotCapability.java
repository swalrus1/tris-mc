package com.swalrus.tris.capabilities;

import com.google.gson.JsonObject;
import dev.langchain4j.agent.tool.ToolSpecification;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

public class ScreenshotCapability implements Capability {

    @Override
    public String name() {
        return "getScreenshot";
    }

    @Override
    public ToolSpecification toolSpec() {
        return ToolSpecification.builder()
                .name(name())
                .description("Capture the current game view as a screenshot")
                .build();
    }

    @Override
    public CompletableFuture<String> execute(Minecraft client, JsonObject args) {
        CompletableFuture<String> future = new CompletableFuture<>();
        client.execute(() ->
            Screenshot.takeScreenshot(client.getMainRenderTarget(), image -> {
                try {
                    Path tmp = Files.createTempFile("tris-ss", ".png");
                    image.writeToFile(tmp);

                    BufferedImage full = ImageIO.read(tmp.toFile());
                    Files.delete(tmp);

                    int targetWidth = 512;
                    int targetHeight = full.getHeight() * targetWidth / full.getWidth();
                    BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = scaled.createGraphics();
                    g.drawImage(full.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH), 0, 0, null);
                    g.dispose();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(scaled, "PNG", baos);
                    future.complete(Base64.getEncoder().encodeToString(baos.toByteArray()));
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            })
        );
        return future;
    }
}
