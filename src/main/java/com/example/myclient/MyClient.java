package com.example.myclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class MyClient implements ClientModInitializer {
    private static KeyBinding menuKey;
    private static KeyBinding zoomKey;

    // zoom
    private static boolean zooming = false;
    private static int savedFov = 70;
    private static final int ZOOM_FOV = 30;

    // CPS
    private static final Deque<Long> leftClicks = new ArrayDeque<>();
    private static final Deque<Long> rightClicks = new ArrayDeque<>();
    private static boolean lastLeft = false;
    private static boolean lastRight = false;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public void onInitializeClient() {
        Config.load();
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> Config.save());

        // stylizacja ekranu wyboru swiata i listy serwerow
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof SelectWorldScreen || screen instanceof MultiplayerScreen) {
                ScreenEvents.afterRender(screen).register((scr, ctx, mouseX, mouseY, tickDelta) ->
                        AurixOverlay.render(ctx, scr.width, scr.height));
            }
        });

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.myclient.toggle_hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.myclient"
        ));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.myclient.zoom",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                "category.myclient"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // podmiana oryginalnego menu glownego na menu Aurix
            if (client.currentScreen != null && client.currentScreen.getClass() == TitleScreen.class) {
                client.setScreen(new AurixTitleScreen());
            }

            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ModuleScreen());
                }
            }

            handleZoom(client);
        });

        HudElementRegistry.addLast(Identifier.of("myclient", "info_hud"),
                (ctx, tickCounter) -> renderHud(ctx));
    }

    private static void handleZoom(MinecraftClient client) {
        if (client.player == null) return;
        boolean want = Modules.ZOOM.enabled && zoomKey.isPressed() && client.currentScreen == null;
        if (want && !zooming) {
            savedFov = client.options.getFov().getValue();
            client.options.getFov().setValue(ZOOM_FOV);
            zooming = true;
        } else if (!want && zooming) {
            client.options.getFov().setValue(savedFov);
            zooming = false;
        }
    }

    private static void pollClicks(MinecraftClient mc) {
        long now = System.currentTimeMillis();
        long handle = mc.getWindow().getHandle();
        boolean left = mc.currentScreen == null
                && GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean right = mc.currentScreen == null
                && GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if (left && !lastLeft) leftClicks.addLast(now);
        if (right && !lastRight) rightClicks.addLast(now);
        lastLeft = left;
        lastRight = right;
        while (!leftClicks.isEmpty() && leftClicks.peekFirst() < now - 1000) leftClicks.pollFirst();
        while (!rightClicks.isEmpty() && rightClicks.peekFirst() < now - 1000) rightClicks.pollFirst();
    }

    private static String direction(MinecraftClient mc) {
        return switch (mc.player.getHorizontalFacing()) {
            case NORTH -> "N (polnoc, -Z)";
            case SOUTH -> "S (poludnie, +Z)";
            case EAST -> "E (wschod, +X)";
            case WEST -> "W (zachod, -X)";
            default -> "?";
        };
    }

    private static String ping(MinecraftClient mc) {
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (handler == null) return "Ping: -";
        PlayerListEntry entry = handler.getPlayerListEntry(mc.player.getUuid());
        if (entry == null) return "Ping: -";
        return "Ping: " + entry.getLatency() + " ms";
    }

    private static void renderHud(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;

        pollClicks(mc);

        List<String> lines = new ArrayList<>();
        if (Modules.FPS.enabled) lines.add("FPS: " + mc.getCurrentFps());
        if (Modules.COORDS.enabled) {
            lines.add(String.format("XYZ: %.1f / %.1f / %.1f",
                    mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        }
        if (Modules.CPS.enabled) lines.add("CPS: " + leftClicks.size() + " | " + rightClicks.size());
        if (Modules.PING.enabled) lines.add(ping(mc));
        if (Modules.DIRECTION.enabled) lines.add("Kierunek: " + direction(mc));
        if (Modules.CLOCK.enabled) lines.add("Czas: " + LocalTime.now().format(TIME_FORMAT));

        int y = 5;
        for (String line : lines) {
            ctx.drawTextWithShadow(mc.textRenderer, line, 5, y, 0xFFFFFFFF);
            y += 12;
        }

        if (Modules.ARMOR.enabled) {
            y += 3;
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            for (EquipmentSlot slot : slots) {
                ItemStack stack = mc.player.getEquippedStack(slot);
                if (stack.isEmpty()) continue;
                ctx.drawItem(stack, 5, y);
                ctx.drawStackOverlay(mc.textRenderer, stack, 5, y);
                y += 18;
            }
        }

        if (Modules.KEYSTROKES.enabled) {
            int sw = mc.getWindow().getScaledWidth();
            int sh = mc.getWindow().getScaledHeight();
            int bx = sw - 3 * 18 - 2 * 2 - 6;
            int by = sh - 2 * 18 - 2 - 6;
            drawKey(ctx, mc, "W", bx + 20, by, mc.options.forwardKey.isPressed());
            drawKey(ctx, mc, "A", bx, by + 20, mc.options.leftKey.isPressed());
            drawKey(ctx, mc, "S", bx + 20, by + 20, mc.options.backKey.isPressed());
            drawKey(ctx, mc, "D", bx + 40, by + 20, mc.options.rightKey.isPressed());
        }
    }

    private static void drawKey(DrawContext ctx, MinecraftClient mc, String label, int x, int y, boolean pressed) {
        ctx.fill(x, y, x + 18, y + 18, pressed ? 0xAAFFFFFF : 0x77000000);
        ctx.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(label), x + 9, y + 5,
                pressed ? 0xFF000000 : 0xFFFFFFFF);
    }
}
