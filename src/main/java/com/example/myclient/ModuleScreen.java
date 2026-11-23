package com.example.myclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ModuleScreen extends Screen {
    private ButtonWidget fpsButton;
    private ButtonWidget coordsButton;

    public ModuleScreen() {
        super(Text.literal("MyClient"));
    }

    private static Text label(String name, boolean on) {
        return Text.literal(name + ": " + (on ? "ON" : "OFF"));
    }

    @Override
    protected void init() {
        int w = 150;
        int h = 20;
        int x = this.width / 2 - w / 2;
        int y = this.height / 2 - 30;

        fpsButton = ButtonWidget.builder(label("FPS", MyClient.fpsEnabled), b -> {
            MyClient.fpsEnabled = !MyClient.fpsEnabled;
            b.setMessage(label("FPS", MyClient.fpsEnabled));
        }).dimensions(x, y, w, h).build();

        coordsButton = ButtonWidget.builder(label("Koordynaty", MyClient.coordsEnabled), b -> {
            MyClient.coordsEnabled = !MyClient.coordsEnabled;
            b.setMessage(label("Koordynaty", MyClient.coordsEnabled));
        }).dimensions(x, y + 26, w, h).build();

        ButtonWidget close = ButtonWidget.builder(Text.literal("Zamknij"), b -> this.close())
                .dimensions(x, y + 62, w, h).build();

        this.addDrawableChild(fpsButton);
        this.addDrawableChild(coordsButton);
        this.addDrawableChild(close);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("MyClient - moduly"), this.width / 2, this.height / 2 - 50, 0xFFFFFFFF);
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
