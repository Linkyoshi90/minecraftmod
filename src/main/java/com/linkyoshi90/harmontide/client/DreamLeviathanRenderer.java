package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.leviathan.DreamCalf;
import com.linkyoshi90.harmontide.leviathan.DreamLeviathan;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Draws the Dream-Leviathan's body up close (client/DreamLeviathanModel), and her calf (the same body, small). */
public class DreamLeviathanRenderer extends EntityRenderer<DreamLeviathan> {
    private final DreamLeviathanModel model = new DreamLeviathanModel();

    public DreamLeviathanRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0;
    }

    @Override
    public boolean shouldRender(DreamLeviathan e, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(DreamLeviathan e, float yaw, float partialTick, PoseStack ps, MultiBufferSource buffers, int light) {
        float t = e.tickCount + partialTick;
        int mode = e.mode();
        ps.pushPose();
        if (mode == DreamLeviathan.FLYING || mode == DreamLeviathan.RISING) ps.translate(0, Mth.sin(t * 0.04F) * 0.6F, 0);
        DreamLeviathanModel.place(ps, e.bodyYaw(partialTick), Mth.lerp(partialTick, e.xRotO, e.getXRot()), DreamLeviathan.SCALE,
                mode == DreamLeviathan.DEAD, true);
        this.model.pose(mode, t);
        float glow = e.sick() >= 3 ? 0.35F + 0.25F * Mth.sin(t * 0.21F) * Mth.sin(t * 0.07F) : e.sick() > 0 ? 0.7F : 1.0F;
        this.model.render(ps, buffers, DreamLeviathanModel.skin(mode, e.sick(), e.rot()), DreamLeviathanModel.glow(mode, e.sick()),
                light, OverlayTexture.NO_OVERLAY, 1.0F, glow);
        ps.popPose();
        super.render(e, yaw, partialTick, ps, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(DreamLeviathan e) {
        return DreamLeviathanModel.skin(e.mode(), e.sick(), e.rot());
    }

    /** The calf: her mother's body at a horse's size or less, swimming through the air after her owner. */
    public static class Calf extends EntityRenderer<DreamCalf> {
        /** Model units to blocks at full size: 142 units long makes a calf about 2.2 blocks from nose to fluke. */
        public static final float SCALE = 0.25F;
        private final DreamLeviathanModel model = new DreamLeviathanModel();

        public Calf(EntityRendererProvider.Context context) {
            super(context);
            this.shadowRadius = 0.5F;
        }

        @Override
        public void render(DreamCalf e, float yaw, float partialTick, PoseStack ps, MultiBufferSource buffers, int light) {
            float t = e.tickCount + partialTick;
            float size = SCALE * e.getScale();
            ps.pushPose();
            ps.translate(0, e.getBbHeight() * 0.5F + Mth.sin(t * 0.08F) * 0.08F, 0);
            DreamLeviathanModel.place(ps, Mth.rotLerp(partialTick, e.yBodyRotO, e.yBodyRot), 0, size, false, false);
            this.model.pose(DreamLeviathan.FLYING, t * 1.6F);
            this.model.render(ps, buffers, DreamLeviathanModel.CALF, DreamLeviathanModel.CALF_GLOW, light,
                    OverlayTexture.pack(0.0F, e.hurtTime > 0 || e.deathTime > 0), 1.0F, 1.0F);
            ps.popPose();
            super.render(e, yaw, partialTick, ps, buffers, light);
        }

        @Override
        public ResourceLocation getTextureLocation(DreamCalf e) {
            return DreamLeviathanModel.CALF;
        }
    }
}
