package com.example.myclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class AurixTitleScreen extends Screen {
    private static final int CYAN = 0xFF3CE6FF;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int LOGO_H = 56;
    private static final int STROKE = 10;

    public AurixTitleScreen() {
        super(Text.literal("Aurix Client"));
    }

    private int logoTop() {
        return Math.max(8, this.height / 4 - 30);
    }

    @Override
    protected void init() {
        int w = 200;
        int h = 20;
        int x = this.width / 2 - w / 2;
        int y = logoTop() + LOGO_H + 30;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Gra jednoosobowa"),
                b -> this.client.setScreen(new SelectWorldScreen(this)))
                .dimensions(x, y, w, h).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Gra wieloosobowa"),
                b -> this.client.setScreen(new MultiplayerScreen(this)))
                .dimensions(x, y + 24, w, h).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Opcje"),
                b -> this.client.setScreen(new OptionsScreen(this, this.client.options)))
                .dimensions(x, y + 48, w, h).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Wyjdz z gry"),
                b -> this.client.scheduleStop())
                .dimensions(x, y + 72, w, h).build());
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // tlo: gradient granat -> fiolet
        ctx.fillGradient(0, 0, this.width, this.height, 0xFF0E1230, 0xFF3A1E7A);

        // delikatne linie dekoracyjne
        ctx.fill(0, this.height - 30, this.width, this.height - 29, 0x553CE6FF);
        ctx.fill(0, 30, this.width, 31, 0x553CE6FF);

        // logo: litera A z prostokatow
        int cx = this.width / 2;
        int top = logoTop();
        for (int r = 0; r < LOGO_H; r++) {
            int off = (int) (r * 0.55);
            ctx.fill(cx - off, top + r, cx - off + STROKE, top + r + 1, CYAN);
            ctx.fill(cx + off - STROKE, top + r, cx + off, top + r + 1, CYAN);
        }
        for (int r = 38; r < 44; r++) {
            int off = (int) (r * 0.55);
            ctx.fill(cx - off + STROKE, top + r, cx + off - STROKE, top + r + 1, WHITE);
        }
        ctx.fill(cx - 3, top - 8, cx + 3, top - 2, WHITE);

        // nazwa
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("AURIX CLIENT"),
                cx, top + LOGO_H + 10, WHITE);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawTextWithShadow(this.textRenderer, "Aurix Client 1.0.0", 4, this.height - 12, 0xFFAAAAAA);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
