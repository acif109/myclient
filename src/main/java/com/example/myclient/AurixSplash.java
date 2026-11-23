package com.example.myclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Util;

public final class AurixSplash {
    private static final int LOGO_H = 64;
    private static final int STROKE = 11;

    private AurixSplash() {}

    private static int argb(int alpha, int rgb) {
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    private static void drawA(DrawContext ctx, int cx, int top, int h, int stroke, int color, int barColor, boolean bar) {
        for (int r = 0; r < h; r++) {
            int off = (int) (r * 0.55);
            ctx.fill(cx - off, top + r, cx - off + stroke, top + r + 1, color);
            ctx.fill(cx + off - stroke, top + r, cx + off, top + r + 1, color);
        }
        if (bar) {
            int from = (int) (h * 0.68);
            int to = (int) (h * 0.78);
            for (int r = from; r < to; r++) {
                int off = (int) (r * 0.55);
                ctx.fill(cx - off + stroke, top + r, cx + off - stroke, top + r + 1, barColor);
            }
            ctx.fill(cx - 3, top - 9, cx + 3, top - 3, barColor);
        }
    }

    /** progress: 0..1 postep ladowania, completeTime: -1 dopoki trwa ladowanie. */
    public static void render(DrawContext ctx, float progress, long completeTime) {
        long now = Util.getMeasuringTimeMs();
        float f = completeTime > -1L ? (now - completeTime) / 1000.0F : -1.0F;
        float mul = f >= 1.0F ? 1.0F - Math.min(1.0F, Math.max(0.0F, f - 1.0F)) : 1.0F;
        int alpha = (int) (mul * 255);
        if (alpha < 8) return;

        int w = ctx.getScaledWindowWidth();
        int h = ctx.getScaledWindowHeight();
        int cx = w / 2;

        // tlo
        ctx.fillGradient(0, 0, w, h, argb(alpha, 0x0A0D26), argb(alpha, 0x3A1E7A));

        // logo z pulsujaca poswiata
        double pulse = 0.5 + 0.5 * Math.sin(now / 350.0);
        int glow = (int) ((26 + 34 * pulse) * alpha / 255.0);
        int top = h / 2 - LOGO_H / 2 - 14;
        if (glow > 3) {
            drawA(ctx, cx - 2, top - 2, LOGO_H + 4, STROKE + 4, argb(glow, 0x3CE6FF), 0, false);
        }
        drawA(ctx, cx, top, LOGO_H, STROKE, argb(alpha, 0x3CE6FF), argb(alpha, 0xFFFFFF), true);

        // pasek postepu
        int barW = 160;
        int barH = 4;
        int bx = cx - barW / 2;
        int by = top + LOGO_H + 24;
        float p = completeTime > -1L ? 1.0F : Math.min(1.0F, Math.max(0.0F, progress));
        ctx.fill(bx - 1, by - 1, bx + barW + 1, by + barH + 1, argb(alpha * 2 / 5, 0x3CE6FF));
        ctx.fill(bx, by, bx + barW, by + barH, argb(alpha, 0x141836));
        ctx.fill(bx, by, bx + (int) (barW * p), by + barH, argb(alpha, 0x3CE6FF));
    }
}
