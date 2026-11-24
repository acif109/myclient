package com.example.myclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CosmeticsScreen extends Screen {
    private static final int CYAN = 0xFF3CE6FF;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFFB4B9D6;

    private static final int PANEL_W = 370;
    private static final int PANEL_H = 268;
    private static final int CHIP_W = 52;
    private static final int CHIP_H = 30;
    private static final int GAP = 5;
    private static final int ROW_STEP = 62;

    private static class Row {
        final String title;
        final List<Cosmetics.Option> options;
        final Supplier<String> getter;
        final Consumer<String> setter;

        Row(String title, List<Cosmetics.Option> options, Supplier<String> getter, Consumer<String> setter) {
            this.title = title;
            this.options = options;
            this.getter = getter;
            this.setter = setter;
        }
    }

    private final Row[] rows = {
            new Row("Peleryna", Cosmetics.CAPES, () -> Cosmetics.cape, v -> Cosmetics.cape = v),
            new Row("Skrzydła na plecach", Cosmetics.BACK_WINGS, () -> Cosmetics.backWings, v -> Cosmetics.backWings = v),
            new Row("Skrzydła elytry (po założeniu elytry)", Cosmetics.WINGS, () -> Cosmetics.wings, v -> Cosmetics.wings = v)
    };

    private final Screen parent;

    public CosmeticsScreen(Screen parent) {
        super(Text.literal("Kosmetyki"));
        this.parent = parent;
    }

    private int px() {
        return this.width / 2 - PANEL_W / 2;
    }

    private int py() {
        return this.height / 2 - PANEL_H / 2;
    }

    private int rowTop(int r) {
        return py() + 36 + r * ROW_STEP;
    }

    private int chipX(int i) {
        return px() + 16 + i * (CHIP_W + GAP);
    }

    private int chipY(int r) {
        return rowTop(r) + 14;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public void close() {
        Config.save();
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

        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("KOSMETYKI").formatted(Formatting.BOLD), this.width / 2, y + 12, CYAN);

        for (int r = 0; r < rows.length; r++) {
            Row row = rows[r];
            ctx.drawTextWithShadow(this.textRenderer,
                    Text.literal(row.title).formatted(Formatting.BOLD), x + 16, rowTop(r), WHITE);
            drawRow(ctx, row, r, mouseX, mouseY);
        }

        ctx.drawTextWithShadow(this.textRenderer, "Kosmetyki widzisz tylko Ty.", x + 16, y + PANEL_H - 24, GRAY);

        int bx = x + PANEL_W - 96;
        int by = y + PANEL_H - 30;
        boolean hover = inside(mouseX, mouseY, bx, by, 80, 20);
        Gfx.rr(ctx, bx, by, 80, 20, 4, hover ? 0xCC3CE6FF : 0x33FFFFFF);
        Gfx.rr(ctx, bx + 1, by + 1, 78, 18, 3, hover ? 0xFF1C3A5A : 0xFF14193A);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Zamknij"), bx + 40, by + 6, WHITE);
    }

    private void drawRow(DrawContext ctx, Row row, int r, int mouseX, int mouseY) {
        int cy = chipY(r);
        String selectedId = row.getter.get();
        for (int i = 0; i < row.options.size(); i++) {
            Cosmetics.Option o = row.options.get(i);
            int cx = chipX(i);
            boolean selected = o.id().equals(selectedId);
            boolean hover = inside(mouseX, mouseY, cx, cy, CHIP_W, CHIP_H);

            int border = selected ? CYAN : (hover ? 0x88FFFFFF : 0x33FFFFFF);
            Gfx.rr(ctx, cx - 2, cy - 2, CHIP_W + 4, CHIP_H + 4, 5, border);
            Gfx.rr(ctx, cx, cy, CHIP_W, CHIP_H, 4, 0xFF10132B);

            if (o.id().equals("none")) {
                ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("x"),
                        cx + CHIP_W / 2, cy + CHIP_H / 2 - 4, GRAY);
            } else {
                ctx.fillGradient(cx + 2, cy + 2, cx + CHIP_W - 2, cy + CHIP_H - 2, o.color1(), o.color2());
            }

            String label = o.id().equals("none") && r == 2 ? "Domyślne" : o.name();
            ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(label),
                    cx + CHIP_W / 2, cy + CHIP_H + 5, selected ? WHITE : GRAY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = px();
            int y = py();
            if (inside(mouseX, mouseY, x + PANEL_W - 96, y + PANEL_H - 30, 80, 20)) {
                this.close();
                return true;
            }
            for (int r = 0; r < rows.length; r++) {
                Row row = rows[r];
                for (int i = 0; i < row.options.size(); i++) {
                    if (inside(mouseX, mouseY, chipX(i), chipY(r), CHIP_W, CHIP_H)) {
                        row.setter.accept(row.options.get(i).id());
                        Config.save();
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
