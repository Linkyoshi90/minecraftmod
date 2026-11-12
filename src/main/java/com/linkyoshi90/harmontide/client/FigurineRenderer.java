package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.block.FigurineBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

/** Draws a small copy of the friend on the figurine's stand, scaled to fit whatever her size. */
public class FigurineRenderer implements BlockEntityRenderer<FigurineBlockEntity> {
    private static final float FIT = 0.75F;

    public FigurineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FigurineBlockEntity figurine, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (figurine.getLevel() == null) return;
        Entity shown = figurine.shown(figurine.getLevel());
        if (shown == null) return;
        float scale = FIT / Math.max(shown.getBbHeight(), shown.getBbWidth() * 1.5F);
        pose.pushPose();
        pose.translate(0.5, 0.13, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-figurine.getBlockState().getValue(HorizontalDirectionalBlock.FACING).toYRot()));
        pose.scale(scale, scale, scale);
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);
        dispatcher.render(shown, 0, 0, 0, 0, 0, pose, buffers, light);
        dispatcher.setRenderShadow(true);
        pose.popPose();
    }
}
