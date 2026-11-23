package com.example.myclient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Nakladka w stylu Aurix rysowana na ekranach wyboru swiata i listy serwerow. */
public final class AurixOverlay {
    private static final int CYAN = 0xFF3CE6FF;
    private static final int WHITE = 0xFFFFFFFF;

    private AurixOverlay() {}

    public static void render(DrawContext ctx, int w, int h) {
        // fioletowo-granatowy "szklany" filtr na calym ekranie
        ctx.fillGradient(0, 0, w, h, 0x480E1230, 0x583A1E7A);

        // unoszace sie czasteczki
        double t = (System.currentTimeMillis() % 3_600_000L) / 1000.0;
        for (int i = 0; i < 40; i++) {
            double speed = 5 + (i % 5) * 4;
            int size = 1 + (i % 3);
            int x = (i * 137 + 29) % Math.max(1, w);
            int y = h - (int) ((t * speed + i * 53) % (h + 20));
            int alpha = 0x12 + (i % 4) * 0x0C;
            ctx.fill(x, y, x + size, y + size, (alpha << 24) | 0x3CE6FF);
        }

        // linia pod naglowkiem
        ctx.fill(0, 32, w, 33, 0x443CE6FF);

        // mini logo "A" + napis w lewym gornym rogu
        int cx = 18;
        int top = 9;
        for (int r = 0; r < 16; r++) {
            int off = (int) (r * 0.55);
            ctx.fill(cx - off, top + r, cx - off + 3, top + r + 1, CYAN);
            ctx.fill(cx + off - 3, top + r, cx + off, top + r + 1, CYAN);
        }
        for (int r = 10; r < 12; r++) {
            int off = (int) (r * 0.55);
            ctx.fill(cx - off + 3, top + r, cx + off - 3, top + r + 1, WHITE);
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        ctx.drawTextWithShadow(mc.textRenderer, "AURIX", 32, 11, WHITE);
        ctx.drawTextWithShadow(mc.textRenderer, "CLIENT", 32, 21, CYAN);
    }
}
