package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.leviathan.DreamLeviathan;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

/**
 * The Dream-Leviathan's body: a humpback-shaped whale of night sky (tools/gen_dream_leviathan.py paints her skins on this layout, so
 * keep the boxes and texture offsets in step with it). 142 units from nose to fluke; her calf is the same body drawn small. Shared
 * by her renderer (client/DreamLeviathanRenderer), the far shape in the sky (client/DreamSky) and the calf (client/DreamCalfRenderer).
 */
public final class DreamLeviathanModel {
    private static final String TEX = "textures/entity/dream_leviathan/";
    public static final ResourceLocation HEALTHY = tex("healthy"), SICK_1 = tex("sick_1"), SICK_2 = tex("sick_2"), SICK_3 = tex("sick_3"),
            DEAD = tex("dead"), ROT = tex("rot"), BONES = tex("bones"), GLOW = tex("healthy_glow"), SICK_GLOW = tex("sick_glow"),
            CALF = tex("calf"), CALF_GLOW = tex("calf_glow");
    /** How far her lowest point (the jaw) is below the model's middle, in model units. */
    public static final float BELLY = 12.0F;

    private final ModelPart root, body, head, jaw, tail1, tail2, tail3, fluke, finL, finR;

    private static ResourceLocation tex(String name) {
        return HarmontideMod.id(TEX + name + ".png");
    }

    public DreamLeviathanModel() {
        this.root = layer().bakeRoot();
        this.body = this.root.getChild("body");
        this.head = this.body.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.tail1 = this.body.getChild("tail1");
        this.tail2 = this.tail1.getChild("tail2");
        this.tail3 = this.tail2.getChild("tail3");
        this.fluke = this.tail3.getChild("fluke");
        this.finL = this.body.getChild("fin_l");
        this.finR = this.body.getChild("fin_r");
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        PartDefinition body = r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-12, -10, -20, 24, 20, 40), PartPose.ZERO);
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(128, 0).addBox(-11, -9, -30, 22, 16, 30), PartPose.offset(0, -1, -20));
        head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 60).addBox(-10, 0, -29, 20, 6, 28), PartPose.offset(0, 7, 0));
        PartDefinition t1 = body.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(96, 60).addBox(-9, -8, 0, 18, 15, 22), PartPose.offset(0, -1, 20));
        PartDefinition t2 = t1.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(176, 60).addBox(-6, -5, 0, 12, 10, 20), PartPose.offset(0, 0, 21));
        PartDefinition t3 = t2.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(0, 97).addBox(-3.5F, -3, 0, 7, 6, 16), PartPose.offset(0, 0, 19));
        t3.addOrReplaceChild("fluke", CubeListBuilder.create().texOffs(46, 97).addBox(-20, -1, 0, 40, 2, 14), PartPose.offset(0, 0, 14));
        body.addOrReplaceChild("fin_l", CubeListBuilder.create().texOffs(0, 120).addBox(0, -1, -4.5F, 30, 2, 9), PartPose.offsetAndRotation(12, 6, -12, 0, -0.35F, 0.45F));
        body.addOrReplaceChild("fin_r", CubeListBuilder.create().texOffs(80, 120).addBox(-30, -1, -4.5F, 30, 2, 9), PartPose.offsetAndRotation(-12, 6, -12, 0, 0.35F, -0.45F));
        body.addOrReplaceChild("dorsal", CubeListBuilder.create().texOffs(160, 120).addBox(-1.5F, -5, 0, 3, 5, 6), PartPose.offset(0, -10, 10));
        return LayerDefinition.create(mesh, 256, 256);
    }

    /** Poses her for a moment: swimming through the air, lying on the ground breathing with her mouth open, or limp and dead. */
    public void pose(int mode, float t) {
        float sway = 0.08F;
        if (mode == DreamLeviathan.FLYING || mode == DreamLeviathan.RISING) {
            this.tail1.xRot = Mth.sin(t * sway) * 0.12F;
            this.tail2.xRot = Mth.sin(t * sway - 0.8F) * 0.18F;
            this.tail3.xRot = Mth.sin(t * sway - 1.6F) * 0.22F;
            this.fluke.xRot = Mth.sin(t * sway - 2.4F) * 0.30F;
            this.head.xRot = -Mth.sin(t * sway) * 0.03F;
            float flap = Mth.sin(t * 0.06F) * 0.22F;
            this.finL.zRot = 0.45F + flap;
            this.finR.zRot = -0.45F - flap;
            this.jaw.xRot = 0;
        } else if (mode == DreamLeviathan.LANDED) {
            float breath = Mth.sin(t * 0.03F);
            this.tail1.xRot = 0.04F + breath * 0.01F;
            this.tail2.xRot = 0.06F;
            this.tail3.xRot = 0.05F + Mth.sin(t * 0.02F) * 0.04F;
            this.fluke.xRot = Mth.sin(t * 0.02F - 1) * 0.08F;
            this.head.xRot = 0.03F;
            this.finL.zRot = 0.12F;
            this.finR.zRot = -0.12F;
            this.jaw.xRot = 0.2F + breath * 0.05F;
        } else {
            this.tail1.xRot = 0.05F;
            this.tail2.xRot = 0.1F;
            this.tail3.xRot = 0.1F;
            this.fluke.xRot = 0.15F;
            this.head.xRot = 0.05F;
            this.finL.zRot = 0.6F;
            this.finR.zRot = -0.2F;
            this.jaw.xRot = 0.32F;
        }
    }

    /**
     * Turns the pose stack from her middle into model space: lifted so her belly is at the bottom, turned to her heading, tipped by her
     * pitch, rolled onto her side when dead, and scaled to {@code scale} (model units to blocks: 1/16 per unit at 1).
     */
    public static void place(PoseStack ps, float yaw, float pitch, float scale, boolean onSide, boolean lift) {
        if (lift) ps.translate(0, (onSide ? 12.0F : BELLY) / 16.0F * scale, 0);
        ps.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        ps.mulPose(Axis.XP.rotationDegrees(-pitch));
        if (onSide) ps.mulPose(Axis.ZP.rotationDegrees(78.0F));
        ps.scale(-scale, -scale, scale);
    }

    /** Her skin and glow for how she is (see DreamLeviathan: mode, sickness 0..3, rot stage 0..2). */
    public static ResourceLocation skin(int mode, int sick, int rot) {
        if (mode == DreamLeviathan.DEAD) return rot >= 2 ? BONES : rot == 1 ? ROT : DEAD;
        return switch (sick) {
            case 0 -> HEALTHY;
            case 1 -> SICK_1;
            case 2 -> SICK_2;
            default -> SICK_3;
        };
    }

    @Nullable
    public static ResourceLocation glow(int mode, int sick) {
        if (mode == DreamLeviathan.DEAD) return null;
        return sick == 0 ? GLOW : SICK_GLOW;
    }

    /** Draws her: the skin lit by the world (light), then the glowing stars and runes over it at full brightness. */
    public void render(PoseStack ps, MultiBufferSource buffers, ResourceLocation skin, @Nullable ResourceLocation glow, int light, int overlay,
                       float alpha, float glowAlpha) {
        int a = Mth.clamp((int) (alpha * 255), 0, 255);
        this.root.render(ps, buffers.getBuffer(RenderType.entityTranslucent(skin)), light, overlay, FastColor.ARGB32.color(a, 255, 255, 255));
        if (glow != null && glowAlpha > 0) {
            int g = Mth.clamp((int) (glowAlpha * alpha * 255), 0, 255);
            this.root.render(ps, buffers.getBuffer(RenderType.eyes(glow)), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                    FastColor.ARGB32.color(255, g, g, g));
        }
    }
}
