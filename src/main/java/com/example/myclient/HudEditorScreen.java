package com.example.myclient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class HudEditorScreen extends Screen {
    private static final int CYAN = 0xFF3CE6FF;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFF8A8FB0;
    private static final int PAD = 2;

    private final Screen parent;
    private Module dragged = null;
    private int offX, offY;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("Edytor HUD"));
        this.parent = parent;
    }

    @Override
    public void close() {
        Config.save();
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private int resetX() {
        return this.width / 2 - 60;
    }

    private int resetY() {
        return this.height - 34;
    }

    private int clampX(Module m, int x) {
        int w = MyClient.hudWidth(MinecraftClient.getInstance(), m);
        return Math.max(0, Math.min(x, this.width - w));
    }

    private int clampY(Module m, int y) {
        return Math.max(0, Math.min(y, this.height - MyClient.hudHeight(m)));
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // lekkie przyciemnienie, gra pozostaje widoczna
        ctx.fill(0, 0, this.width, this.height, 0x66000000);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        MinecraftClient mc = MinecraftClient.getInstance();

        boolean any = false;
        for (Module m : Modules.ALL) {
            if (!m.hud || !m.enabled) continue;
            any = true;
            int w = MyClient.hudWidth(mc, m);
            int h = MyClient.hudHeight(m);
            int x = clampX(m, m.x);
            int y = clampY(m, m.y);

            MyClient.drawHud(ctx, mc, m, x, y, true);

            boolean hover = inside(mouseX, mouseY, x - PAD, y - PAD, w + PAD * 2, h + PAD * 2);
            int color = (m == dragged || hover) ? CYAN : 0x88FFFFFF;
            int x1 = x - PAD, y1 = y - PAD, x2 = x + w + PAD, y2 = y + h + PAD;
            ctx.fill(x1, y1, x2, y1 + 1, color);
            ctx.fill(x1, y2 - 1, x2, y2, color);
            ctx.fill(x1, y1, x1 + 1, y2, color);
            ctx.fill(x2 - 1, y1, x2, y2, color);
        }

        // naglowek i podpowiedzi
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("EDYTOR HUD"), this.width / 2, 10, CYAN);
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Przeciagnij modul myszka, aby zmienic jego polozenie  |  Esc - zapisz i zamknij"),
                this.width / 2, 24, WHITE);
        if (!any) {
            ctx.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("Brak wlaczonych modulow - wlacz je w menu Aurix (Prawy Shift)"),
                    this.width / 2, this.height / 2, GRAY);
        }

        // przycisk resetu
        boolean hoverReset = inside(mouseX, mouseY, resetX(), resetY(), 120, 20);
        ctx.fill(resetX(), resetY(), resetX() + 120, resetY() + 20, hoverReset ? 0xEE2A7C94 : 0xCC141836);
        ctx.fill(resetX(), resetY(), resetX() + 3, resetY() + 20, hoverReset ? CYAN : 0xFF3A3F66);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Resetuj pozycje"),
                resetX() + 60, resetY() + 6, WHITE);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (inside(mouseX, mouseY, resetX(), resetY(), 120, 20)) {
                for (Module m : Modules.ALL) {
                    if (m.hud) m.resetPosition();
                }
                Config.save();
                return true;
            }
            MinecraftClient mc = MinecraftClient.getInstance();
            // od gory (ostatni narysowany) do dolu
            for (int i = Modules.ALL.size() - 1; i >= 0; i--) {
                Module m = Modules.ALL.get(i);
                if (!m.hud || !m.enabled) continue;
                int w = MyClient.hudWidth(mc, m);
                int h = MyClient.hudHeight(m);
                int x = clampX(m, m.x);
                int y = clampY(m, m.y);
                if (inside(mouseX, mouseY, x - PAD, y - PAD, w + PAD * 2, h + PAD * 2)) {
                    dragged = m;
                    offX = (int) mouseX - x;
                    offY = (int) mouseY - y;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragged != null && button == 0) {
            dragged.x = clampX(dragged, (int) mouseX - offX);
            dragged.y = clampY(dragged, (int) mouseY - offY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragged != null && button == 0) {
            dragged = null;
            Config.save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
