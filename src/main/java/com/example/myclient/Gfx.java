package com.example.myclient;

import net.minecraft.client.gui.DrawContext;

/** Wspolne narzedzia do rysowania. */
public final class Gfx {
    private Gfx() {}

    /** Prostokat z zaokraglonymi rogami. */
    public static void rr(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        if (r <= 0 || w < 2 * r || h < 2 * r) {
            ctx.fill(x, y, x + w, y + h, color);
            return;
        }
        ctx.fill(x, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int dx = r - (int) Math.round(Math.sqrt(r * r - dy * dy));
            ctx.fill(x + dx, y + i, x + w - dx, y + i + 1, color);
            ctx.fill(x + dx, y + h - i - 1, x + w - dx, y + h - i, color);
        }
    }

    /** Plynne przejscie miedzy dwoma kolorami ARGB (t: 0..1). */
    public static int lerpColor(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (a >>> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >>> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int ra = (int) (aa + (ba - aa) * t);
        int rr = (int) (ar + (br - ar) * t);
        int rg = (int) (ag + (bg - ag) * t);
        int rb = (int) (ab + (bb - ab) * t);
        return (ra << 24) | (rr << 16) | (rg << 8) | rb;
    }
}
