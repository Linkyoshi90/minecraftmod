package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.util.function.Predicate;

/** Harpies and frogs at the milking machine: &lt;species&gt;_blush.png (her cheeks alone) drawn over her. Girls have GirlRenderer.BlushLayer. */
public class BlushOverlayLayer<T extends LivingEntity & GeoAnimatable> extends GeoRenderLayer<T> {
    private final RenderType blush;
    private final Predicate<T> blushing;

    public BlushOverlayLayer(GeoRenderer<T> renderer, String species, Predicate<T> blushing) {
        super(renderer);
        this.blush = RenderType.entityTranslucent(HarmontideMod.id("textures/entity/" + species + "_blush.png"));
        this.blushing = blushing;
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource buffers,
                       VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (animatable.isBaby() || !this.blushing.test(animatable)) return;
        this.getRenderer().reRender(model, poseStack, buffers, animatable, this.blush, buffers.getBuffer(this.blush), partialTick, packedLight, packedOverlay, -1);
    }
}
