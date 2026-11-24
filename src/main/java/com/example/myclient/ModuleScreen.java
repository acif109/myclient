package com.example.myclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ModuleScreen extends Screen {
    // kolory
    private static final int CYAN = 0xFF3CE6FF;
    private static final int PURPLE = 0xFFB48CFF;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFB4B9D6;
    private static final int GREEN = 0xFF4CFFA0;
    private static final int GOLD = 0xFFFFD34D;

    // kategorie (filtry w panelu bocznym)
    private static final String[] CATS = {"Wszystkie", "Informacje", "Wizualne", "Ulubione"};

    private static final int SIDE_W = 118;
    private static final int CARD_H = 40;
    private static final int GAP = 6;

    private final Screen parent;
    private final long openedAt = System.currentTimeMillis();

    private int category = 0;
    private String query = "";
    private double scroll = 0;

    // uklad (computeLayout)
    private int panelX, panelY, panelW, panelH;
    private int areaL, areaT, areaR, areaB;
    private int cols, cardW;
    private int searchX, searchY, searchW, favX, favW;

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

    // ---------- pomocnicze ----------

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    /** Prostokat z zaokraglonymi rogami. */
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

    private static boolean matchesCategory(Module m, int cat) {
        return switch (cat) {
            case 1 -> m.category.equals(Modules.INFO);
            case 2 -> m.category.equals(Modules.VISUAL);
            case 3 -> m.favorite;
            default -> true;
        };
    }

    private int count(int cat) {
        int n = 0;
        for (Module m : Modules.ALL) {
            if (matchesCategory(m, cat)) n++;
        }
        return n;
    }

    private List<Module> visible() {
        List<Module> list = new ArrayList<>();
        String q = query.toLowerCase();
        for (Module m : Modules.ALL) {
            if (!matchesCategory(m, category)) continue;
            if (!q.isEmpty() && !m.name.toLowerCase().contains(q)) continue;
            list.add(m);
        }
        return list;
    }

    private void computeLayout() {
        panelW = Math.min(this.width - 24, 620);
        panelH = Math.min(this.height - 24, 340);
        panelX = this.width / 2 - panelW / 2;
        panelY = this.height / 2 - panelH / 2;
        double e = Math.min(1.0, (System.currentTimeMillis() - openedAt) / 180.0);
        e = 1 - (1 - e) * (1 - e);
        panelY += (int) ((1 - e) * 14);

        areaL = panelX + SIDE_W + 14;
        areaR = panelX + panelW - 18;
        areaT = panelY + 58;
        areaB = panelY + panelH - 12;

        int areaW = areaR - areaL;
        cols = areaW >= 300 ? 2 : 1;
        cardW = (areaW - (cols - 1) * GAP) / cols;

        favW = 66;
        favX = panelX + panelW - 14 - favW;
        searchW = Math.min(150, favX - 8 - (panelX + SIDE_W + 150));
        searchW = Math.max(80, searchW);
        searchX = favX - 8 - searchW;
        searchY = panelY + 12;
    }

    private int maxScroll(int itemCount) {
        int rows = (itemCount + cols - 1) / cols;
        int contentH = rows * (CARD_H + GAP) - GAP;
        return Math.max(0, contentH - (areaB - areaT));
    }

    private int cardX(int i) {
        return areaL + (i % cols) * (cardW + GAP);
    }

    private int cardY(int i) {
        return areaT + (i / cols) * (CARD_H + GAP) - (int) scroll;
    }

    private int cosmeticsButtonY() {
        return hudButtonY() + 24;
    }

    private int hudButtonY() {
        return sideItemY(CATS.length) + 4;
    }

    private int sideItemY(int i) {
        return panelY + 96 + i * 24;
    }

    // ---------- rysowanie ----------

    @Override
    protected void init() {
        computeLayout();
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fillGradient(0, 0, this.width, this.height, 0xB00E1230, 0xB03A1E7A);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        computeLayout();

        List<Module> mods = visible();
        scroll = Math.max(0, Math.min(scroll, maxScroll(mods.size())));

        // panel
        rr(ctx, panelX - 1, panelY - 1, panelW + 2, panelH + 2, 7, 0x553CE6FF);
        rr(ctx, panelX, panelY, panelW, panelH, 6, 0xF2090B1F);

        drawSidebar(ctx, mouseX, mouseY);
        drawHeader(ctx, mouseX, mouseY, mods.size());
        drawCards(ctx, mods, mouseX, mouseY);
    }

    private void drawSidebar(DrawContext ctx, int mouseX, int mouseY) {
        // tlo panelu bocznego
        rr(ctx, panelX + 6, panelY + 6, SIDE_W - 10, panelH - 12, 5, 0x88101430);

        int cx = panelX + SIDE_W / 2 - 2;

        // logo "A"
        int lt = panelY + 16;
        for (int r = 0; r < 24; r++) {
            int off = (int) (r * 0.55);
            ctx.fill(cx - off, lt + r, cx - off + 4, lt + r + 1, CYAN);
            ctx.fill(cx + off - 4, lt + r, cx + off, lt + r + 1, CYAN);
        }
        for (int r = 16; r < 19; r++) {
            int off = (int) (r * 0.55);
            ctx.fill(cx - off + 4, lt + r, cx + off - 4, lt + r + 1, WHITE);
        }
        ctx.fill(cx - 2, lt - 5, cx + 2, lt - 1, WHITE);

        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("AURIX CLIENT"), cx, panelY + 46, WHITE);

        // wersja
        int chipW = 44;
        rr(ctx, cx - chipW / 2, panelY + 60, chipW, 14, 6, 0x663CE6FF);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("v1.0.0"), cx, panelY + 63, WHITE);

        ctx.drawTextWithShadow(this.textRenderer, "KATEGORIE", panelX + 16, panelY + 84, GRAY);

        for (int i = 0; i < CATS.length; i++) {
            int y = sideItemY(i);
            int x = panelX + 12;
            int w = SIDE_W - 22;
            boolean selected = category == i;
            boolean hover = inside(mouseX, mouseY, x, y, w, 20);
            if (selected) {
                rr(ctx, x, y, w, 20, 4, 0x883CE6FF);
                rr(ctx, x + 1, y + 1, w - 2, 18, 3, 0xFF1C2250);
            } else if (hover) {
                rr(ctx, x, y, w, 20, 4, 0x44FFFFFF);
            }
            String label = (i == 3 ? "* " : "") + CATS[i];
            ctx.drawTextWithShadow(this.textRenderer, label, x + 6, y + 6, selected ? WHITE : 0xFFB8BCD8);
            String n = String.valueOf(count(i));
            ctx.drawTextWithShadow(this.textRenderer, n, x + w - 6 - this.textRenderer.getWidth(n), y + 6,
                    selected ? CYAN : GRAY);
        }

        // przycisk kosmetykow
        int kx2 = panelX + 12;
        int ky2 = cosmeticsButtonY();
        int kw2 = SIDE_W - 22;
        boolean kh = inside(mouseX, mouseY, kx2, ky2, kw2, 20);
        rr(ctx, kx2, ky2, kw2, 20, 4, kh ? 0xCCB48CFF : 0x66B48CFF);
        rr(ctx, kx2 + 1, ky2 + 1, kw2 - 2, 18, 3, kh ? 0xFF2A2250 : 0xFF14193A);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Kosmetyki"),
                kx2 + kw2 / 2, ky2 + 6, WHITE);

        // przycisk edytora HUD
        int ex = panelX + 12;
        int ey = hudButtonY();
        int ew = SIDE_W - 22;
        boolean eh = inside(mouseX, mouseY, ex, ey, ew, 20);
        rr(ctx, ex, ey, ew, 20, 4, eh ? 0xCC3CE6FF : 0x663CE6FF);
        rr(ctx, ex + 1, ey + 1, ew - 2, 18, 3, eh ? 0xFF1C3A5A : 0xFF14193A);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Edytor HUD"),
                ex + ew / 2, ey + 6, WHITE);

        // gracz
        if (panelH >= 300) {
            String name = Profile.displayName(this.client);
            int fy = panelY + panelH - 36;
            boolean nickHover = inside(mouseX, mouseY, panelX + 12, fy, SIDE_W - 22, 22);
            rr(ctx, panelX + 12, fy, 22, 22, 4, CYAN);
            ctx.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal(name.substring(0, 1).toUpperCase()), panelX + 23, fy + 7, 0xFF0A0D26);
            ctx.drawTextWithShadow(this.textRenderer, name, panelX + 40, fy + 2, WHITE);
            ctx.drawTextWithShadow(this.textRenderer, nickHover ? "Konta / nick" : "Aurix 1.0.0",
                    panelX + 40, fy + 13, nickHover ? CYAN : GRAY);
        }
    }

    private void drawHeader(DrawContext ctx, int mouseX, int mouseY, int shown) {
        int hx = panelX + SIDE_W + 14;
        String title = (category == 0 ? "Wszystkie moduły" : CATS[category]) + " (" + shown + ")";
        ctx.drawTextWithShadow(this.textRenderer, Text.literal(title).formatted(Formatting.BOLD), hx, panelY + 13, WHITE);
        ctx.drawTextWithShadow(this.textRenderer, "LPM = włącz/wyłącz  |  PPM = ulubione", hx, panelY + 26, GRAY);

        // pole wyszukiwania
        rr(ctx, searchX, searchY, searchW, 18, 5, 0x44FFFFFF);
        rr(ctx, searchX + 1, searchY + 1, searchW - 2, 16, 4, 0xFF10132B);
        boolean blink = (System.currentTimeMillis() / 500) % 2 == 0;
        if (query.isEmpty()) {
            ctx.drawTextWithShadow(this.textRenderer, "Szukaj modułu...", searchX + 6, searchY + 5, GRAY);
        } else {
            ctx.drawTextWithShadow(this.textRenderer, query + (blink ? "_" : ""), searchX + 6, searchY + 5, WHITE);
        }

        // przycisk ulubionych
        boolean favOn = category == 3;
        boolean hover = inside(mouseX, mouseY, favX, searchY, favW, 18);
        rr(ctx, favX, searchY, favW, 18, 5, favOn ? 0xAA3CE6FF : 0x44FFFFFF);
        rr(ctx, favX + 1, searchY + 1, favW - 2, 16, 4, hover ? 0xFF1C2250 : 0xFF10132B);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("* Ulubione"),
                favX + favW / 2, searchY + 5, favOn ? GOLD : WHITE);

        // linia pod naglowkiem
        ctx.fill(hx, panelY + 46, panelX + panelW - 14, panelY + 47, 0x333CE6FF);
    }

    private void drawCards(DrawContext ctx, List<Module> mods, int mouseX, int mouseY) {
        ctx.enableScissor(areaL - 2, areaT - 2, areaR + 2, areaB + 2);

        if (mods.isEmpty()) {
            ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Brak modułów"),
                    (areaL + areaR) / 2, areaT + 30, GRAY);
        }

        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            int x = cardX(i);
            int y = cardY(i);
            if (y + CARD_H < areaT - 2 || y > areaB + 2) continue;

            boolean hover = inside(mouseX, mouseY, x, y, cardW, CARD_H)
                    && mouseY >= areaT && mouseY <= areaB;
            m.hover += ((hover ? 1f : 0f) - m.hover) * 0.3f;

            if (m.enabled) {
                rr(ctx, x, y, cardW, CARD_H, 5, Gfx.lerpColor(0xAA3CE6FF, 0xFF3CE6FF, m.hover));
                rr(ctx, x + 1, y + 1, cardW - 2, CARD_H - 2, 4, Gfx.lerpColor(0xFF152840, 0xFF1B3A58, m.hover));
            } else {
                rr(ctx, x, y, cardW, CARD_H, 5, Gfx.lerpColor(0x22FFFFFF, 0x55FFFFFF, m.hover));
                rr(ctx, x + 1, y + 1, cardW - 2, CARD_H - 2, 4, Gfx.lerpColor(0xFF10132B, 0xFF171B3C, m.hover));
            }

            // ikona (pierwsza litera), kolor zalezy od kategorii
            int tint = m.category.equals(Modules.INFO) ? CYAN : PURPLE;
            rr(ctx, x + 7, y + 6, 28, 28, 5, m.enabled ? Gfx.lerpColor(0xFF1A2040, tint, 0.35f) : 0xFF1A1F42);
            ctx.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal(m.name.substring(0, 1)).formatted(Formatting.BOLD),
                    x + 21, y + 16, m.enabled ? WHITE : GRAY);

            // nazwa (pogrubiona) i opis
            Text nameText = Text.literal(m.name).formatted(Formatting.BOLD);
            ctx.drawTextWithShadow(this.textRenderer, nameText, x + 44, y + 9, WHITE);
            if (m.favorite) {
                ctx.drawTextWithShadow(this.textRenderer, "*",
                        x + 44 + this.textRenderer.getWidth(nameText) + 4, y + 9, GOLD);
            }
            ctx.drawTextWithShadow(this.textRenderer, m.desc, x + 44, y + 23, GRAY);

            // przelacznik
            int sx = x + cardW - 38;
            int sy = y + CARD_H / 2 - 7;
            if (m.anim < 0) m.anim = m.enabled ? 1f : 0f;
            m.anim += ((m.enabled ? 1f : 0f) - m.anim) * 0.3f;
            rr(ctx, sx, sy, 30, 14, 6, Gfx.lerpColor(0xFF3A3F66, GREEN, m.anim));
            int kx = sx + 2 + Math.round(16 * m.anim);
            rr(ctx, kx, sy + 2, 10, 10, 4, WHITE);

            // napis stanu obok przelacznika
            String state = m.enabled ? "WŁ." : "WYŁ.";
            ctx.drawTextWithShadow(this.textRenderer, state,
                    sx - this.textRenderer.getWidth(state) - 6, y + CARD_H / 2 - 4,
                    m.enabled ? GREEN : GRAY);
        }

        ctx.disableScissor();

        // pasek przewijania
        int max = maxScroll(mods.size());
        if (max > 0) {
            int trackH = areaB - areaT;
            int barH = Math.max(16, trackH * trackH / (trackH + max));
            int barY = areaT + (int) ((trackH - barH) * (scroll / max));
            int bx = panelX + panelW - 12;
            ctx.fill(bx, areaT, bx + 3, areaB, 0x22FFFFFF);
            ctx.fill(bx, barY, bx + 3, barY + barH, 0xAA3CE6FF);
        }
    }

    // ---------- obsluga wejscia ----------

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        computeLayout();

        // blok gracza -> zmiana nicku
        if (button == 0 && panelH >= 300
                && inside(mouseX, mouseY, panelX + 12, panelY + panelH - 36, SIDE_W - 22, 22)) {
            this.client.setScreen(new AccountScreen(this));
            return true;
        }

        // przycisk kosmetykow
        if (button == 0 && inside(mouseX, mouseY, panelX + 12, cosmeticsButtonY(), SIDE_W - 22, 20)) {
            this.client.setScreen(new CosmeticsScreen(this));
            return true;
        }

        // przycisk edytora HUD
        if (button == 0 && inside(mouseX, mouseY, panelX + 12, hudButtonY(), SIDE_W - 22, 20)) {
            this.client.setScreen(new HudEditorScreen(this));
            return true;
        }

        // panel boczny
        for (int i = 0; i < CATS.length; i++) {
            if (inside(mouseX, mouseY, panelX + 12, sideItemY(i), SIDE_W - 22, 20)) {
                category = i;
                scroll = 0;
                return true;
            }
        }

        // przycisk ulubionych
        if (inside(mouseX, mouseY, favX, searchY, favW, 18)) {
            category = category == 3 ? 0 : 3;
            scroll = 0;
            return true;
        }

        // karty
        if (mouseY >= areaT && mouseY <= areaB) {
            List<Module> mods = visible();
            for (int i = 0; i < mods.size(); i++) {
                if (inside(mouseX, mouseY, cardX(i), cardY(i), cardW, CARD_H)) {
                    Module m = mods.get(i);
                    if (button == 0) {
                        m.enabled = !m.enabled;
                    } else if (button == 1) {
                        m.favorite = !m.favorite;
                    } else {
                        break;
                    }
                    Config.save();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        computeLayout();
        scroll = Math.max(0, Math.min(scroll - verticalAmount * 16, maxScroll(visible().size())));
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (chr >= 32 && chr != 127 && query.length() < 20) {
            query += chr;
            scroll = 0;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            this.close();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !query.isEmpty()) {
            query = query.substring(0, query.length() - 1);
            scroll = 0;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
