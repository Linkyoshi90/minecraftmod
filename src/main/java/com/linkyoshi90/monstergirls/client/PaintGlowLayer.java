package com.linkyoshi90.monstergirls.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

import java.util.function.Function;

/**
 * The glowing paints (glow, magma, thunder, neon) light up in the dark: textures/entity/&lt;texture&gt;_paint_&lt;paint&gt;_glowmask.png
 * (tools/gen_paints.py), drawn full-bright. Every other creature skips this layer.
 */
public class PaintGlowLayer<T extends GeoAnimatable> extends AutoGlowingGeoLayer<T> {
    private final Function<T, String> paint;

    public PaintGlowLayer(GeoRenderer<T> renderer, Function<T, String> paint) {
        super(renderer);
        this.paint = paint;
    }

    static boolean glows(String paint) {
        return "glow".equals(paint) || "magma".equals(paint) || "thunder".equals(paint) || "neon".equals(paint) || "circuit".equals(paint);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource,
                       VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (glows(this.paint.apply(animatable))) super.render(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
    }
}
