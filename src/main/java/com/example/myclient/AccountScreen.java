package com.example.myclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class AccountScreen extends Screen {
    private static final int CYAN = 0xFF3CE6FF;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFF8A8FB0;
    private static final int GREEN = 0xFF4CFFA0;
    private static final int RED = 0xFFFF5577;

    private static final int PANEL_W = 300;
    private static final int PANEL_H = 258;
    private static final int ROW_H = 24;
    private static final int VISIBLE = 5;

    private final Screen parent;
    private String input = "";
    private int scroll = 0;

    public AccountScreen(Screen parent) {
        super(Text.literal("Konta"));
        this.parent = parent;
    }

    private int px() {
        return this.width / 2 - PANEL_W / 2;
    }

    private int py() {
        return this.height / 2 - PANEL_H / 2;
    }

    private int listY() {
        return py() + 56;
    }

    private int inputY() {
        return listY() + VISIBLE * ROW_H + 8;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    /** Wiersze listy: null = konto oryginalne, reszta = nicki offline. */
    private List<String> rows() {
        List<String> rows = new ArrayList<>();
        rows.add(null);
        rows.addAll(Profile.accounts);
        return rows;
    }

    private boolean isActive(String row) {
        return row == null ? Profile.active.isEmpty() : row.equals(Profile.active);
    }

    private void select(String row) {
        if (row == null) {
            Profile.restoreOriginal(this.client);
        } else {
            Profile.applyOffline(this.client, row);
        }
        Config.save();
    }

    private void addAccount() {
        String name = input.trim();
        if (!Profile.isValidName(name)) return;
        if (!Profile.accounts.contains(name)) {
            Profile.accounts.add(name);
        }
        Profile.applyOffline(this.client, name);
        Config.save();
        input = "";
        scroll = Math.max(0, rows().size() - VISIBLE);
    }

    private void removeAccount(String name) {
        Profile.accounts.remove(name);
        if (name.equals(Profile.active)) {
            Profile.restoreOriginal(this.client);
        }
        Config.save();
        scroll = Math.max(0, Math.min(scroll, rows().size() - VISIBLE));
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fillGradient(0, 0, this.width, this.height, 0xC00E1230, 0xC03A1E7A);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        int x = px();
        int y = py();

        Gfx.rr(ctx, x - 1, y - 1, PANEL_W + 2, PANEL_H + 2, 7, 0x553CE6FF);
        Gfx.rr(ctx, x, y, PANEL_W, PANEL_H, 6, 0xF2090B1F);

        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("KONTA OFFLINE"), this.width / 2, y + 12, CYAN);
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Konta offline zmieniaja nick w grze."), this.width / 2, y + 27, GRAY);
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Dzialaja na serwerach offline-mode i w LAN."), this.width / 2, y + 38, GRAY);

        // lista kont
        List<String> rows = rows();
        scroll = Math.max(0, Math.min(scroll, Math.max(0, rows.size() - VISIBLE)));
        int lx = x + 16;
        int lw = PANEL_W - 32;
        for (int i = 0; i < VISIBLE; i++) {
            int idx = scroll + i;
            if (idx >= rows.size()) break;
            String row = rows.get(idx);
            int ry = listY() + i * ROW_H;
            boolean hover = inside(mouseX, mouseY, lx, ry, lw, ROW_H - 3);
            boolean active = isActive(row);

            Gfx.rr(ctx, lx, ry, lw, ROW_H - 3, 4, active ? 0xAA3CE6FF : (hover ? 0x55FFFFFF : 0x22FFFFFF));
            Gfx.rr(ctx, lx + 1, ry + 1, lw - 2, ROW_H - 5, 3, active ? 0xFF152840 : (hover ? 0xFF171B3C : 0xFF10132B));

            String label = row == null ? "Konto oryginalne (" + Profile.originalName(this.client) + ")" : row;
            ctx.drawTextWithShadow(this.textRenderer, label, lx + 8, ry + 7, WHITE);

            int rightEdge = lx + lw - 8;
            if (row != null) {
                boolean xHover = inside(mouseX, mouseY, lx + lw - 22, ry + 2, 18, ROW_H - 7);
                ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("x"), lx + lw - 13, ry + 7,
                        xHover ? RED : GRAY);
                rightEdge = lx + lw - 26;
            }
            if (active) {
                String tag = "AKTYWNE";
                ctx.drawTextWithShadow(this.textRenderer, tag, rightEdge - this.textRenderer.getWidth(tag), ry + 7, GREEN);
            }
        }

        // pasek przewijania
        if (rows.size() > VISIBLE) {
            int trackH = VISIBLE * ROW_H - 3;
            int barH = Math.max(14, trackH * VISIBLE / rows.size());
            int barY = listY() + (int) ((trackH - barH) * (scroll / (double) (rows.size() - VISIBLE)));
            ctx.fill(x + PANEL_W - 10, listY(), x + PANEL_W - 7, listY() + trackH, 0x22FFFFFF);
            ctx.fill(x + PANEL_W - 10, barY, x + PANEL_W - 7, barY + barH, 0xAA3CE6FF);
        }

        // pole dodawania
        int iy = inputY();
        int fw = lw - 76;
        Gfx.rr(ctx, lx, iy, fw, 20, 5, 0x883CE6FF);
        Gfx.rr(ctx, lx + 1, iy + 1, fw - 2, 18, 4, 0xFF10132B);
        boolean blink = (System.currentTimeMillis() / 500) % 2 == 0;
        if (input.isEmpty()) {
            ctx.drawTextWithShadow(this.textRenderer, "Nowy nick (3-16 znakow)", lx + 6, iy + 6, GRAY);
        } else {
            ctx.drawTextWithShadow(this.textRenderer, input + (blink ? "_" : ""), lx + 6, iy + 6, WHITE);
        }
        drawButton(ctx, "Dodaj", lx + fw + 6, iy, 70, mouseX, mouseY, Profile.isValidName(input.trim()));

        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Serwery online-mode wymagaja Twojego konta."), this.width / 2, iy + 28, GRAY);

        drawButton(ctx, "Zamknij", x + PANEL_W / 2 - 40, y + PANEL_H - 30, 80, mouseX, mouseY, false);
    }

    private void drawButton(DrawContext ctx, String label, int bx, int by, int bw, int mouseX, int mouseY, boolean primary) {
        boolean hover = inside(mouseX, mouseY, bx, by, bw, 20);
        Gfx.rr(ctx, bx, by, bw, 20, 4, hover ? 0xCC3CE6FF : (primary ? 0x883CE6FF : 0x33FFFFFF));
        Gfx.rr(ctx, bx + 1, by + 1, bw - 2, 18, 3, hover ? 0xFF1C3A5A : 0xFF14193A);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(label), bx + bw / 2, by + 6, WHITE);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = px();
        int y = py();
        int lx = x + 16;
        int lw = PANEL_W - 32;

        if (button == 0 && inside(mouseX, mouseY, x + PANEL_W / 2 - 40, y + PANEL_H - 30, 80, 20)) {
            this.close();
            return true;
        }
        int iy = inputY();
        int fw = lw - 76;
        if (button == 0 && inside(mouseX, mouseY, lx + fw + 6, iy, 70, 20)) {
            addAccount();
            return true;
        }

        List<String> rows = rows();
        for (int i = 0; i < VISIBLE; i++) {
            int idx = scroll + i;
            if (idx >= rows.size()) break;
            String row = rows.get(idx);
            int ry = listY() + i * ROW_H;
            if (!inside(mouseX, mouseY, lx, ry, lw, ROW_H - 3)) continue;

            boolean onDelete = row != null && inside(mouseX, mouseY, lx + lw - 22, ry + 2, 18, ROW_H - 7);
            if (button == 0 && onDelete) {
                removeAccount(row);
            } else if (button == 0) {
                select(row);
            } else if (button == 1 && row != null) {
                removeAccount(row);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int max = Math.max(0, rows().size() - VISIBLE);
        scroll = Math.max(0, Math.min(scroll - (int) Math.signum(verticalAmount), max));
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if ((Character.isLetterOrDigit(chr) || chr == '_') && chr < 128 && input.length() < 16) {
            input += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !input.isEmpty()) {
            input = input.substring(0, input.length() - 1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            addAccount();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
