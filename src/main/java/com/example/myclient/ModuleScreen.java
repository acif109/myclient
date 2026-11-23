package com.example.myclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ModuleScreen extends Screen {
    private static final int BTN_W = 140;
    private static final int BTN_H = 20;
    private static final int GAP = 6;

    public ModuleScreen() {
        super(Text.literal("Aurix Client"));
    }

    private static Text label(Module m) {
        return Text.literal(m.name + ": " + (m.enabled ? "ON" : "OFF"));
    }

    @Override
    protected void init() {
        int rows = (Modules.ALL.size() + 1) / 2;
        int totalW = BTN_W * 2 + GAP;
        int startX = this.width / 2 - totalW / 2;
        int startY = this.height / 2 - (rows * (BTN_H + GAP)) / 2;

        for (int i = 0; i < Modules.ALL.size(); i++) {
            final Module m = Modules.ALL.get(i);
            int col = i % 2;
            int row = i / 2;
            int x = startX + col * (BTN_W + GAP);
            int y = startY + row * (BTN_H + GAP);
            this.addDrawableChild(ButtonWidget.builder(label(m), b -> {
                m.enabled = !m.enabled;
                b.setMessage(label(m));
            }).dimensions(x, y, BTN_W, BTN_H).build());
        }

        int closeY = startY + rows * (BTN_H + GAP) + 6;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Zamknij"), b -> this.close())
                .dimensions(this.width / 2 - 70, closeY, 140, BTN_H).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        int rows = (Modules.ALL.size() + 1) / 2;
        int startY = this.height / 2 - (rows * (BTN_H + GAP)) / 2;
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Aurix Client - moduly"), this.width / 2, startY - 18, 0xFFFFFFFF);
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
