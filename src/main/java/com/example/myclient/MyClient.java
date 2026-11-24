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
import java.util.Deque;

public class MyClient implements ClientModInitializer {
    private static KeyBinding menuKey;
    private static KeyBinding hudEditorKey;
    private static KeyBinding zoomKey;

    // zoom
    private static boolean startupApplied = false;
    private static boolean zooming = false;
    private static int savedFov = 70;
    private static final int ZOOM_FOV = 30;

    // CPS
    private static final Deque<Long> leftClicks = new ArrayDeque<>();
    private static final Deque<Long> rightClicks = new ArrayDeque<>();
    private static boolean lastLeft = false;
    private static boolean lastRight = false;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // rozmiary elementow HUD (w pikselach ekranu skalowanego)
    public static final int ARMOR_W = 16;
    public static final int ARMOR_H = 70;
    public static final int KEYS_W = 58;
    public static final int KEYS_H = 38;
    public static final int TEXT_H = 15;

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
        hudEditorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.myclient.hud_editor",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_CONTROL,
                "category.myclient"
        ));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.myclient.zoom",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                "category.myclient"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // przywrocenie ostatnio uzywanego konta offline po starcie gry
            if (!startupApplied) {
                startupApplied = true;
                if (!Profile.active.isEmpty()) {
                    Profile.applyOffline(client, Profile.active);
                }
            }

            // podmiana oryginalnego menu glownego na menu Aurix
            if (client.currentScreen != null && client.currentScreen.getClass() == TitleScreen.class) {
                client.setScreen(new AurixTitleScreen());
            }

            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ModuleScreen());
                }
            }
            while (hudEditorKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new HudEditorScreen(null));
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
            case NORTH -> "N (północ, -Z)";
            case SOUTH -> "S (południe, +Z)";
            case EAST -> "E (wschód, +X)";
            case WEST -> "W (zachód, -X)";
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

    // ---------- HUD: tresc, rozmiar, rysowanie ----------

    /** Tekst modulu tekstowego albo null, jesli modul rysuje cos innego. */
    public static String hudText(MinecraftClient mc, Module m) {
        boolean inWorld = mc.player != null;
        if (m == Modules.FPS) return "FPS: " + mc.getCurrentFps();
        if (m == Modules.COORDS) {
            return inWorld
                    ? String.format("XYZ: %.1f / %.1f / %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ())
                    : "XYZ: 0.0 / 64.0 / 0.0";
        }
        if (m == Modules.CPS) return "CPS: " + leftClicks.size() + " | " + rightClicks.size();
        if (m == Modules.PING) return inWorld ? ping(mc) : "Ping: -";
        if (m == Modules.DIRECTION) return "Kierunek: " + (inWorld ? direction(mc) : "N (północ, -Z)");
        if (m == Modules.CLOCK) return "Czas: " + LocalTime.now().format(TIME_FORMAT);
        return null;
    }

    public static int hudWidth(MinecraftClient mc, Module m) {
        if (m == Modules.ARMOR) return ARMOR_W;
        if (m == Modules.KEYSTROKES) return KEYS_W;
        String text = hudText(mc, m);
        return text == null ? 0 : mc.textRenderer.getWidth(text) + 10;
    }

    public static int hudHeight(Module m) {
        if (m == Modules.ARMOR) return ARMOR_H;
        if (m == Modules.KEYSTROKES) return KEYS_H;
        return TEXT_H;
    }

    /** Rysuje jeden modul HUD w podanym miejscu. preview = tryb edytora (pokazuje puste pola). */
    public static void drawHud(DrawContext ctx, MinecraftClient mc, Module m, int x, int y, boolean preview) {
        if (m == Modules.ARMOR) {
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            for (int i = 0; i < slots.length; i++) {
                int sy = y + i * 18;
                ItemStack stack = mc.player != null ? mc.player.getEquippedStack(slots[i]) : ItemStack.EMPTY;
                if (!stack.isEmpty()) {
                    Gfx.rr(ctx, x - 2, sy - 2, 20, 20, 3, 0x88101430);
                    ctx.drawItem(stack, x, sy);
                    ctx.drawStackOverlay(mc.textRenderer, stack, x, sy);
                } else if (preview) {
                    Gfx.rr(ctx, x - 2, sy - 2, 20, 20, 3, 0x44FFFFFF);
                }
            }
        } else if (m == Modules.KEYSTROKES) {
            drawKey(ctx, mc, "W", x + 20, y, mc.options.forwardKey.isPressed());
            drawKey(ctx, mc, "A", x, y + 20, mc.options.leftKey.isPressed());
            drawKey(ctx, mc, "S", x + 20, y + 20, mc.options.backKey.isPressed());
            drawKey(ctx, mc, "D", x + 40, y + 20, mc.options.rightKey.isPressed());
        } else {
            String text = hudText(mc, m);
            if (text != null) {
                int w = mc.textRenderer.getWidth(text) + 10;
                Gfx.rr(ctx, x, y, w, TEXT_H, 3, 0x99101430);
                ctx.fill(x, y + 3, x + 2, y + TEXT_H - 3, 0xFF3CE6FF);
                ctx.drawTextWithShadow(mc.textRenderer, text, x + 6, y + 4, 0xFFFFFFFF);
            }
        }
    }

    private static void drawKey(DrawContext ctx, MinecraftClient mc, String label, int x, int y, boolean pressed) {
        Gfx.rr(ctx, x, y, 18, 18, 3, pressed ? 0xDD3CE6FF : 0x99101430);
        ctx.drawCenteredTextWithShadow(mc.textRenderer, Text.literal(label), x + 9, y + 5,
                pressed ? 0xFF0A0D26 : 0xFFFFFFFF);
    }

    private static void renderHud(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;

        pollClicks(mc);

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        for (Module m : Modules.ALL) {
            if (!m.hud || !m.enabled) continue;
            int w = hudWidth(mc, m);
            int h = hudHeight(m);
            int x = Math.max(0, Math.min(m.x, sw - w));
            int y = Math.max(0, Math.min(m.y, sh - h));
            drawHud(ctx, mc, m, x, y, false);
        }
    }
}
