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
    // Moduly (wlaczane w menu pod Prawym Shiftem)
    public static boolean fpsEnabled = true;
    public static boolean coordsEnabled = true;

    private static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.myclient.toggle_hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.myclient"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ModuleScreen());
                }
            }
        });

        HudElementRegistry.addLast(Identifier.of("myclient", "info_hud"), (ctx, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.options.hudHidden) return;

            int y = 5;
            if (fpsEnabled) {
                ctx.drawTextWithShadow(mc.textRenderer, "FPS: " + mc.getCurrentFps(), 5, y, 0xFFFFFFFF);
                y += 12;
            }
            if (coordsEnabled) {
                ctx.drawTextWithShadow(mc.textRenderer,
                        String.format("XYZ: %.1f / %.1f / %.1f",
                                mc.player.getX(), mc.player.getY(), mc.player.getZ()),
                        5, y, 0xFFFFFFFF);
            }
        });
    }
}
