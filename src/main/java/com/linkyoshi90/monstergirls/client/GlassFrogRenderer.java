package com.linkyoshi90.monstergirls.client;

import com.linkyoshi90.monstergirls.entity.GlassFrog;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import software.bernie.geckolib.cache.object.BakedGeoModel;

/** The glass frog fades to a ghost of herself unless you are close: nearly invisible at a distance, solid within a few blocks. */
public class GlassFrogRenderer extends AnuranRenderer<GlassFrog> {
    private static final float FAR_ALPHA = 0.12F;
    private static final double FADE_END = 12.0;

    public GlassFrogRenderer(EntityRendererProvider.Context context) {
        super(context, "glass_frog", 1.0F, 0.35F);
    }

    @Override
    public RenderType getRenderType(GlassFrog animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public void actuallyRender(PoseStack poseStack, GlassFrog animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource,
                               VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay,
                FastColor.ARGB32.color(Math.round(this.alphaFor(animatable) * 255.0F), colour));
    }

    private float alphaFor(GlassFrog frog) {
        var player = Minecraft.getInstance().player;
        if (player == null) return 1.0F;
        double distance = player.distanceTo(frog);
        double t = (distance - GlassFrog.REVEAL_DISTANCE) / (FADE_END - GlassFrog.REVEAL_DISTANCE);
        return Mth.lerp((float) Mth.clamp(t, 0.0, 1.0), 1.0F, FAR_ALPHA);
    }
}
