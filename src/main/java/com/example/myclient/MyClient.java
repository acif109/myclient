package com.example.myclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class MyClient implements ClientModInitializer {
    private static boolean hudEnabled = true;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.myclient.toggle_hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.myclient"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                hudEnabled = !hudEnabled;
            }
        });

        HudElementRegistry.addLast(Identifier.of("myclient", "info_hud"), (ctx, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (!hudEnabled || mc.player == null || mc.options.hudHidden) return;

            ctx.drawTextWithShadow(mc.textRenderer, "FPS: " + mc.getCurrentFps(), 5, 5, 0xFFFFFFFF);
            ctx.drawTextWithShadow(mc.textRenderer,
                    String.format("XYZ: %.1f / %.1f / %.1f",
                            mc.player.getX(), mc.player.getY(), mc.player.getZ()),
                    5, 17, 0xFFFFFFFF);
        });
    }
}
