package com.example.myclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ModuleScreen extends Screen {
    private static final int CARD_W = 112;
    private static final int CARD_H = 32;
    private static final int GAP = 6;
    private static final int PAD = 14;
    private static final int HEADER_H = 40;
    private static final int FOOTER_H = 24;

    private static final int CYAN = 0xFF3CE6FF;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFF8A8FB0;
    private static final int GREEN = 0xFF4CFFA0;

    // uklad (liczony w computeLayout)
    private int cols, rows, panelX, panelY, panelW, panelH, gridX, gridY;

    private final Screen parent;

    public ModuleScreen() {
        this(null);
    }

    public ModuleScreen(Screen parent) {
        super(Text.literal("Aurix Client"));
        this.parent = parent;
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    private void computeLayout() {
        cols = this.width >= 420 ? 3 : 2;
        rows = (Modules.ALL.size() + cols - 1) / cols;
        int gridW = cols * CARD_W + (cols - 1) * GAP;
        int gridH = rows * CARD_H + (rows - 1) * GAP;
        panelW = gridW + PAD * 2;
        panelH = HEADER_H + gridH + PAD + FOOTER_H;
        panelX = this.width / 2 - panelW / 2;
        panelY = this.height / 2 - panelH / 2;
        gridX = panelX + PAD;
        gridY = panelY + HEADER_H;
    }

    private int cardX(int i) {
        return gridX + (i % cols) * (CARD_W + GAP);
    }

    private int cardY(int i) {
        return gridY + (i / cols) * (CARD_H + GAP);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    protected void init() {
        computeLayout();
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // polprzezroczyste tlo, gra zostaje widoczna
        ctx.fillGradient(0, 0, this.width, this.height, 0xB00E1230, 0xB03A1E7A);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        computeLayout();

        // ramka + panel
        ctx.fill(panelX - 1, panelY - 1, panelX + panelW + 1, panelY + panelH + 1, 0x883CE6FF);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xEE0B0E24);

        drawHeader(ctx, mouseX, mouseY);

        // karty modulow
        int active = 0;
        for (int i = 0; i < Modules.ALL.size(); i++) {
            Module m = Modules.ALL.get(i);
            if (m.enabled) active++;
            drawCard(ctx, m, cardX(i), cardY(i), mouseX, mouseY);
        }

        // stopka
        int footY = panelY + panelH - FOOTER_H + 8;
        ctx.fill(panelX + PAD, panelY + panelH - FOOTER_H, panelX + panelW - PAD,
                panelY + panelH - FOOTER_H + 1, 0x333CE6FF);
        ctx.drawTextWithShadow(this.textRenderer, "Kliknij modul, aby go wlaczyc / wylaczyc",
                panelX + PAD, footY, GRAY);
        String count = "Aktywne: " + active + "/" + Modules.ALL.size();
        int cw = this.textRenderer.getWidth(count);
        ctx.drawTextWithShadow(this.textRenderer, count, panelX + panelW - PAD - cw, footY, CYAN);
    }

    private void drawHeader(DrawContext ctx, int mouseX, int mouseY) {
        // mini logo "A"
        int lx = panelX + PAD + 10;
        int ly = panelY + 12;
        for (int r = 0; r < 18; r++) {
            int off = (int) (r * 0.55);
            ctx.fill(lx - off, ly + r, lx - off + 3, ly + r + 1, CYAN);
            ctx.fill(lx + off - 3, ly + r, lx + off, ly + r + 1, CYAN);
        }
        for (int r = 12; r < 14; r++) {
            int off = (int) (r * 0.55);
            ctx.fill(lx - off + 3, ly + r, lx + off - 3, ly + r + 1, WHITE);
        }

        ctx.drawTextWithShadow(this.textRenderer, "AURIX CLIENT", lx + 18, panelY + 11, WHITE);
        ctx.drawTextWithShadow(this.textRenderer, "Moduly", lx + 18, panelY + 22, GRAY);

        // przycisk zamkniecia (X)
        int bx = panelX + panelW - PAD - 16;
        int by = panelY + 12;
        boolean hover = inside(mouseX, mouseY, bx, by, 16, 16);
        ctx.fill(bx, by, bx + 16, by + 16, hover ? 0xFFCC3355 : 0x88222640);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("x"), bx + 8, by + 4, WHITE);

        // linia pod naglowkiem
        ctx.fill(panelX + PAD, panelY + HEADER_H - 8, panelX + panelW - PAD,
                panelY + HEADER_H - 7, 0x553CE6FF);
    }

    private void drawCard(DrawContext ctx, Module m, int x, int y, int mouseX, int mouseY) {
        boolean hover = inside(mouseX, mouseY, x, y, CARD_W, CARD_H);
        int bg;
        if (m.enabled) {
            bg = hover ? 0xFF2A7C94 : 0xFF1E5F73;
        } else {
            bg = hover ? 0xFF2A2F55 : 0xFF1B1F3A;
        }
        ctx.fill(x, y, x + CARD_W, y + CARD_H, bg);
        // pasek akcentu z lewej
        ctx.fill(x, y, x + 2, y + CARD_H, m.enabled ? CYAN : 0xFF3A3F66);

        ctx.drawTextWithShadow(this.textRenderer, m.name, x + 8, y + 7, WHITE);
        ctx.drawTextWithShadow(this.textRenderer, m.enabled ? "ON" : "OFF", x + 8, y + 19,
                m.enabled ? GREEN : GRAY);

        // przelacznik
        int sx = x + CARD_W - 32;
        int sy = y + CARD_H / 2 - 6;
        ctx.fill(sx, sy, sx + 24, sy + 12, m.enabled ? CYAN : 0xFF3A3F66);
        int kx = m.enabled ? sx + 14 : sx + 2;
        ctx.fill(kx, sy + 2, kx + 8, sy + 10, WHITE);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            computeLayout();
            int bx = panelX + panelW - PAD - 16;
            int by = panelY + 12;
            if (inside(mouseX, mouseY, bx, by, 16, 16)) {
                this.close();
                return true;
            }
            for (int i = 0; i < Modules.ALL.size(); i++) {
                if (inside(mouseX, mouseY, cardX(i), cardY(i), CARD_W, CARD_H)) {
                    Module m = Modules.ALL.get(i);
                    m.enabled = !m.enabled;
                    Config.save();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
