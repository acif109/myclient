package com.example.myclient.mixin;

import com.example.myclient.AurixSplash;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public abstract class SplashOverlayMixin {
    @Shadow
    private float progress;

    @Shadow
    private long reloadCompleteTime;

    // Rysujemy ekran Aurix na wierzchu oryginalnego ekranu ladowania Mojang.
    @Inject(method = "render", at = @At("TAIL"))
    private void aurix$render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        AurixSplash.render(context, this.progress, this.reloadCompleteTime);
    }
}
