package com.example.myclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class AurixTitleScreen extends Screen {
    private static final int CYAN = 0xFF3CE6FF;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFF8A8FB0;

    private static final int LOGO_H = 52;
    private static final int STROKE = 9;
    private static final int BTN_W = 200;
    private static final int BTN_H = 20;
    private static final int BTN_GAP = 4;

    private static class Entry {
        final String label;
        final Runnable action;

        Entry(String label, Runnable action) {
            this.label = label;
            this.action = action;
        }
    }

    private final List<Entry> entries = new ArrayList<>();
    private long openedAt;
    private float[] hoverAnim = new float[0];

    public AurixTitleScreen() {
        super(Text.literal("Aurix Client"));
    }

    @Override
    protected void init() {
        openedAt = System.currentTimeMillis();
        entries.clear();
        entries.add(new Entry("Gra jednoosobowa", () -> this.client.setScreen(new SelectWorldScreen(this))));
        entries.add(new Entry("Gra wieloosobowa", () -> this.client.setScreen(new MultiplayerScreen(this))));
        entries.add(new Entry("Moduly Aurix", () -> this.client.setScreen(new ModuleScreen(this))));
        entries.add(new Entry("Opcje", () -> this.client.setScreen(new OptionsScreen(this, this.client.options))));
        entries.add(new Entry("Wyjdz z gry", () -> this.client.scheduleStop()));
    }

    // ---------- uklad ----------

    private int blockHeight() {
        return 8 + LOGO_H + 30 + entries.size() * (BTN_H + BTN_GAP);
    }

    private int logoTop() {
        return Math.max(12, (this.height - blockHeight()) / 2 + 8);
    }

    private int buttonsTop() {
        return logoTop() + LOGO_H + 30;
    }

    private int buttonY(int i) {
        return buttonsTop() + i * (BTN_H + BTN_GAP);
    }

    private int buttonX() {
        return this.width / 2 - BTN_W / 2;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private String userLabel(boolean hover) {
        return "Gracz: " + Profile.displayName(this.client) + (hover ? "  (zmien konto)" : "");
    }

    private boolean userHover(double mx, double my) {
        int uw = this.textRenderer.getWidth(userLabel(false));
        return inside(mx, my, this.width - uw - 9, this.height - 16, uw + 9, 14);
    }

    // ---------- rysowanie ----------

    private static void drawA(DrawContext ctx, int cx, int top, int h, int stroke, int color, boolean bar) {
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
                ctx.fill(cx - off + stroke, top + r, cx + off - stroke, top + r + 1, WHITE);
            }
            ctx.fill(cx - 3, top - 8, cx + 3, top - 2, WHITE);
        }
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fillGradient(0, 0, this.width, this.height, 0xFF0A0D26, 0xFF3A1E7A);

        // unoszace sie czasteczki
        double t = (System.currentTimeMillis() % 3_600_000L) / 1000.0;
        for (int i = 0; i < 45; i++) {
            double speed = 5 + (i % 5) * 4;
            int size = 1 + (i % 3);
            int x = (i * 137 + 29) % Math.max(1, this.width);
            int span = this.height + 20;
            int y = this.height - (int) ((t * speed + i * 53) % span);
            int alpha = 0x18 + (i % 4) * 0x10;
            ctx.fill(x, y, x + size, y + size, (alpha << 24) | 0x3CE6FF);
        }

        // linie dekoracyjne
        ctx.fill(0, this.height - 22, this.width, this.height - 21, 0x443CE6FF);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        double t = System.currentTimeMillis() / 1000.0;
        int cx = this.width / 2;
        int top = logoTop();

        // poswiata logo (pulsuje)
        double pulse = 0.5 + 0.5 * Math.sin(t * 2.0);
        int glowAlpha = (int) (26 + 34 * pulse);
        drawA(ctx, cx - 2, top - 2, LOGO_H + 4, STROKE + 4, (glowAlpha << 24) | 0x3CE6FF, false);
        drawA(ctx, cx, top, LOGO_H, STROKE, CYAN, true);

        // nazwa
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("A U R I X"),
                cx, top + LOGO_H + 6, WHITE);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("C L I E N T"),
                cx, top + LOGO_H + 17, CYAN);

        // przyciski
        if (hoverAnim.length != entries.size()) {
            hoverAnim = new float[entries.size()];
        }
        int bx = buttonX();
        for (int i = 0; i < entries.size(); i++) {
            int by = buttonY(i);
            boolean hover = inside(mouseX, mouseY, bx, by, BTN_W, BTN_H);
            hoverAnim[i] += ((hover ? 1f : 0f) - hoverAnim[i]) * 0.25f;
            float hv = hoverAnim[i];
            int x = bx + Math.round(hv * 4);

            Gfx.rr(ctx, x, by, BTN_W, BTN_H, 4, Gfx.lerpColor(0xCC141836, 0xEE2A7C94, hv));
            Gfx.rr(ctx, x, by + 3, 3, BTN_H - 6, 1, Gfx.lerpColor(0xFF3A3F66, CYAN, hv));
            ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(entries.get(i).label),
                    cx + Math.round(hv * 4), by + 6, Gfx.lerpColor(0xFFD6D9F0, WHITE, hv));
        }

        // stopka
        ctx.drawTextWithShadow(this.textRenderer, "Aurix Client 1.0.0", 5, this.height - 14, GRAY);
        boolean userHover = userHover(mouseX, mouseY);
        String user = userLabel(userHover);
        int uw = this.textRenderer.getWidth(user);
        ctx.drawTextWithShadow(this.textRenderer, user, this.width - uw - 5, this.height - 14,
                userHover ? CYAN : GRAY);

        // plynne wejscie (fade-in)
        long elapsed = System.currentTimeMillis() - openedAt;
        int fade = (int) Math.max(0, 255 - elapsed * 255 / 500);
        if (fade > 0) {
            ctx.fill(0, 0, this.width, this.height, fade << 24);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && userHover(mouseX, mouseY)) {
            this.client.setScreen(new AccountScreen(this));
            return true;
        }
        if (button == 0) {
            int bx = buttonX();
            for (int i = 0; i < entries.size(); i++) {
                if (inside(mouseX, mouseY, bx, buttonY(i), BTN_W, BTN_H)) {
                    entries.get(i).action.run();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
