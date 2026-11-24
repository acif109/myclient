package com.example.myclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Util;

import java.util.Map;

/** Wlasny ekran ladowania gry. Nie uzywa czcionki, bo w trakcie startu nie jest jeszcze zaladowana. */
public final class AurixSplash {
    private static final int LOGO_H = 60;
    private static final int STROKE = 10;

    // czcionka bitmapowa 5x7
    private static final Map<Character, String[]> FONT = Map.of(
            'A', new String[]{"01110", "10001", "10001", "11111", "10001", "10001", "10001"},
            'U', new String[]{"10001", "10001", "10001", "10001", "10001", "10001", "01110"},
            'R', new String[]{"11110", "10001", "10001", "11110", "10100", "10010", "10001"},
            'I', new String[]{"11111", "00100", "00100", "00100", "00100", "00100", "11111"},
            'X', new String[]{"10001", "10001", "01010", "00100", "01010", "10001", "10001"},
            'C', new String[]{"01110", "10001", "10000", "10000", "10000", "10001", "01110"},
            'L', new String[]{"10000", "10000", "10000", "10000", "10000", "10000", "11111"},
            'E', new String[]{"11111", "10000", "10000", "11110", "10000", "10000", "11111"},
            'N', new String[]{"10001", "11001", "10101", "10011", "10001", "10001", "10001"},
            'T', new String[]{"11111", "00100", "00100", "00100", "00100", "00100", "00100"}
    );

    private AurixSplash() {}

    private static int argb(int alpha, int rgb) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (rgb & 0xFFFFFF);
    }

    private static void rr(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
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

    private static int textWidth(String text, int scale, int spacing) {
        return text.length() * 5 * scale + (text.length() - 1) * spacing;
    }

    /** Rysuje tekst czcionka bitmapowa, wysrodkowany w poziomie. */
    private static void drawText(DrawContext ctx, String text, int cx, int y, int scale, int spacing, int color) {
        int x = cx - textWidth(text, scale, spacing) / 2;
        for (char ch : text.toCharArray()) {
            String[] rows = FONT.get(ch);
            if (rows != null) {
                for (int r = 0; r < 7; r++) {
                    for (int c = 0; c < 5; c++) {
                        if (rows[r].charAt(c) == '1') {
                            ctx.fill(x + c * scale, y + r * scale, x + (c + 1) * scale, y + (r + 1) * scale, color);
                        }
                    }
                }
            }
            x += 5 * scale + spacing;
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

        // unoszace sie czasteczki
        double t = now / 1000.0;
        for (int i = 0; i < 50; i++) {
            double speed = 6 + (i % 5) * 4;
            int size = 1 + (i % 3);
            int x = (i * 137 + 29) % Math.max(1, w);
            int y = h - (int) ((t * speed + i * 53) % (h + 20));
            int pa = (0x20 + (i % 4) * 0x14) * alpha / 255;
            if (pa > 3) ctx.fill(x, y, x + size, y + size, argb(pa, 0x3CE6FF));
        }

        // uklad pionowy: logo, nazwa, podpis, pasek
        int totalH = LOGO_H + 14 + 21 + 8 + 14 + 26 + 6;
        int top = Math.max(14, (h - totalH) / 2);

        // logo z pulsujaca poswiata
        double pulse = 0.5 + 0.5 * Math.sin(now / 350.0);
        int glow = (int) ((26 + 34 * pulse) * alpha / 255.0);
        if (glow > 3) {
            drawA(ctx, cx - 3, top - 3, LOGO_H + 6, STROKE + 6, argb(glow, 0x3CE6FF), 0, false);
        }
        drawA(ctx, cx, top, LOGO_H, STROKE, argb(alpha, 0x3CE6FF), argb(alpha, 0xFFFFFF), true);

        // nazwa czcionka bitmapowa
        int nameY = top + LOGO_H + 14;
        drawText(ctx, "AURIX", cx, nameY, 3, 3, argb(alpha, 0xFFFFFF));
        drawText(ctx, "CLIENT", cx, nameY + 21 + 8, 2, 4, argb(alpha, 0x3CE6FF));

        // pasek postepu
        int barW = 180;
        int barH = 6;
        int bx = cx - barW / 2;
        int by = nameY + 21 + 8 + 14 + 26;
        float p = completeTime > -1L ? 1.0F : Math.min(1.0F, Math.max(0.0F, progress));
        int filled = (int) (barW * p);

        rr(ctx, bx - 1, by - 1, barW + 2, barH + 2, 3, argb(alpha * 2 / 5, 0x3CE6FF));
        rr(ctx, bx, by, barW, barH, 3, argb(alpha, 0x101430));
        if (filled >= 6) {
            rr(ctx, bx, by, filled, barH, 3, argb(alpha, 0x3CE6FF));
            // przesuwajacy sie blask na wypelnionej czesci
            int hx = (int) ((now / 6) % (barW + 40)) - 20;
            int from = Math.max(0, hx);
            int to = Math.min(filled - 3, hx + 18);
            if (to > from) {
                ctx.fill(bx + from, by + 1, bx + to, by + barH - 1, argb(alpha * 2 / 5, 0xFFFFFF));
            }
        }

        // animowane kropki pod paskiem
        for (int i = 0; i < 3; i++) {
            double d = 0.5 + 0.5 * Math.sin(now / 250.0 - i * 0.9);
            int da = (int) ((60 + 160 * d) * alpha / 255.0);
            int dx = cx - 10 + i * 10;
            if (da > 3) ctx.fill(dx, by + 14, dx + 4, by + 18, argb(da, 0xFFFFFF));
        }
    }
}
