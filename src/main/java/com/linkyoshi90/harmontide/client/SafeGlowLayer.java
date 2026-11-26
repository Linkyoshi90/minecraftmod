package com.linkyoshi90.harmontide.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/**
 * The usual glow layer, except on a bare-chest texture. That one is built in memory ({@link Topless}).
 * GeckoLib then looks up a glow mask beside it and crashes, because there is no file to read.
 */
public class SafeGlowLayer<T extends GeoAnimatable> extends AutoGlowingGeoLayer<T> {
    public SafeGlowLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    @Nullable
    protected RenderType getRenderType(T animatable, @Nullable MultiBufferSource bufferSource) {
        ResourceLocation texture = this.getTextureResource(animatable);
        if (texture != null && texture.getPath().startsWith("dynamic/bare/")) return null;
        return super.getRenderType(animatable, bufferSource);
    }
}
