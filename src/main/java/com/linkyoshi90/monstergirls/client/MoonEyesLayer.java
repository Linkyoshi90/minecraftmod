package com.linkyoshi90.monstergirls.client;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Bonded;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * A girl answering the full moon has glowing eyes: the "eyelids" plane every model has over its eyes (normally tucked
 * just behind the face) is drawn glowing, a hair in front of the face.
 */
public class MoonEyesLayer<T extends GeoAnimatable & Bonded> extends GeoRenderLayer<T> {
    private static final RenderType GLOW = RenderType.eyes(MonsterGirlsMod.id("textures/entity/moon_eyes.png"));
    private static final int COLOUR = 0xFFFF4F7A;

    public MoonEyesLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    public void renderForBone(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource,
                              VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (!animatable.isMoonstruck() || !bone.getName().equals("eyelids")) return;
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, -0.02F);
        this.getRenderer().renderCubesOfBone(poseStack, bone, bufferSource.getBuffer(GLOW), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, COLOUR);
        poseStack.popPose();
    }
}
