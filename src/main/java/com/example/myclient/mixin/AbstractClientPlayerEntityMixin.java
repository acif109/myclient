package com.example.myclient.mixin;

import com.example.myclient.Cosmetics;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {
    // Podmieniamy tekstury peleryny i elytry tylko dla lokalnego gracza.
    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void aurix$cosmetics(CallbackInfoReturnable<SkinTextures> cir) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if ((Object) this != mc.player) return;

        Identifier cape = Cosmetics.capeTexture();
        Identifier wings = Cosmetics.wingsTexture();
        boolean backWings = Cosmetics.backWingsActive();
        if (cape == null && wings == null && !backWings) return;

        SkinTextures original = cir.getReturnValue();
        cir.setReturnValue(new SkinTextures(
                original.texture(),
                backWings ? Cosmetics.LOCAL_MARKER : original.textureUrl(),
                cape != null ? cape : original.capeTexture(),
                wings != null ? wings : original.elytraTexture(),
                original.model(),
                original.secure()
        ));
    }
}
