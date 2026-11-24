package com.example.myclient;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/** Skrzydla na plecach lokalnego gracza (widoczne tylko dla Ciebie). */
public class WingsFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private final ModelPart root;
    private final ModelPart left;
    private final ModelPart right;

    public WingsFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
        super(context);
        ModelData data = new ModelData();
        ModelPartData rootData = data.getRoot();
        rootData.addChild("left",
                ModelPartBuilder.create().uv(0, 0).cuboid(0.0F, 0.0F, 0.0F, 24.0F, 20.0F, 1.0F),
                ModelTransform.NONE);
        rootData.addChild("right",
                ModelPartBuilder.create().mirrored().uv(0, 0).cuboid(-24.0F, 0.0F, 0.0F, 24.0F, 20.0F, 1.0F),
                ModelTransform.NONE);
        this.root = TexturedModelData.of(data, 64, 32).createModel();
        this.left = this.root.getChild("left");
        this.right = this.root.getChild("right");
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                       PlayerEntityRenderState state, float limbAngle, float limbDistance) {
        // tylko lokalny gracz: oznaczamy go w mixinie przez textureUrl
        if (state.skinTextures == null
                || !Cosmetics.LOCAL_MARKER.equals(state.skinTextures.textureUrl())) {
            return;
        }
        Identifier texture = Cosmetics.backWingsTexture();
        if (texture == null) return;

        float flap = (float) Math.sin(state.age * 0.12F) * 0.07F;

        left.setPivot(1.5F, 0.5F, 2.6F);
        left.yaw = -0.5F - flap;
        left.roll = -0.30F - flap;

        right.setPivot(-1.5F, 0.5F, 2.6F);
        right.yaw = 0.5F + flap;
        right.roll = 0.30F + flap;

        matrices.push();
        // przejdz do ukladu korpusu (uwzglednia np. skradanie sie)
        this.getContextModel().body.rotate(matrices);
        VertexConsumer consumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(texture));
        this.root.render(matrices, consumer, light, OverlayTexture.DEFAULT_UV);
        matrices.pop();
    }
}
