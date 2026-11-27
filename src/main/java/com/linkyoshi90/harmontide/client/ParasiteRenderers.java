package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Zombie;

import java.util.function.Function;

/**
 * How the Dream-Leviathan's parasites look (leviathan/Parasites): vanilla bodies with their own overgrown skins
 * (tools/gen_dream_leviathan.py), swollen tumours and a hanging tendril grown out of their backs, shoulders and heads, and violet
 * parasite eyes and spots that glow in the dark.
 */
public final class ParasiteRenderers {
    private static final String TEX = "textures/entity/parasite/";
    public static final ResourceLocation ZOMBIE = tex("infested_zombie"), SKELETON = tex("infested_skeleton"), CREEPER = tex("infested_creeper"),
            ZOMBIE_GLOW = tex("infested_zombie_glow"), SKELETON_GLOW = tex("infested_skeleton_glow"), CREEPER_GLOW = tex("infested_creeper_glow"),
            GROWTH = tex("growth"), GROWTH_GLOW = tex("growth_glow");

    private ParasiteRenderers() {
    }

    private static ResourceLocation tex(String name) {
        return HarmontideMod.id(TEX + name + ".png");
    }

    public static ZombieRenderer zombie(EntityRendererProvider.Context ctx) {
        ZombieRenderer r = new ZombieRenderer(ctx) {
            @Override
            public ResourceLocation getTextureLocation(Zombie e) {
                return ZOMBIE;
            }
        };
        r.addLayer(new Growths<>(r, m -> m.body, m -> m.head, false));
        r.addLayer(new Glow<>(r, ZOMBIE_GLOW));
        return r;
    }

    public static SkeletonRenderer skeleton(EntityRendererProvider.Context ctx) {
        SkeletonRenderer r = new SkeletonRenderer(ctx) {
            @Override
            public ResourceLocation getTextureLocation(AbstractSkeleton e) {
                return SKELETON;
            }
        };
        r.addLayer(new Growths<AbstractSkeleton, net.minecraft.client.model.SkeletonModel<AbstractSkeleton>>(r, m -> m.body, m -> m.head, false));
        r.addLayer(new Glow<>(r, SKELETON_GLOW));
        return r;
    }

    public static CreeperRenderer creeper(EntityRendererProvider.Context ctx) {
        CreeperRenderer r = new CreeperRenderer(ctx) {
            @Override
            public ResourceLocation getTextureLocation(Creeper e) {
                return CREEPER;
            }
        };
        r.addLayer(new Growths<>(r, m -> m.root().getChild("body"), m -> m.root().getChild("head"), true));
        r.addLayer(new Glow<>(r, CREEPER_GLOW));
        return r;
    }

    /** The parasite's growths: lumps and a tendril on the back, a swelling on a shoulder and one on the head. */
    static LayerDefinition growthLayer(boolean creeper) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        PartDefinition body = r.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5.0F, 0.5F, 1.0F, 6, 6, 4)
                .texOffs(20, 0).addBox(1.0F, 4.0F, 1.5F, 4, 4, 3)
                .texOffs(0, 10).addBox(creeper ? 2.5F : 3.0F, creeper ? 0.0F : -2.0F, -2.5F, 4, 4, 5)
                .texOffs(18, 10).addBox(-3.0F, 7.0F, -3.5F, 6, 4, 2), PartPose.ZERO);
        body.addOrReplaceChild("tendril", CubeListBuilder.create().texOffs(36, 0).addBox(-0.5F, 0.0F, -0.5F, 1, 9, 1),
                PartPose.offsetAndRotation(-2.0F, 6.0F, 4.0F, 0.5F, 0.0F, 0.2F));
        r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(40, 10).addBox(-5.5F, -9.0F, -2.0F, 4, 4, 4)
                .texOffs(40, 18).addBox(1.5F, -3.0F, -4.6F, 3, 3, 1), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    static final class Growths<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
        private final Function<M, ModelPart> body, head;
        private final ModelPart bodyGrowth, headGrowth;

        Growths(RenderLayerParent<T, M> parent, Function<M, ModelPart> body, Function<M, ModelPart> head, boolean creeper) {
            super(parent);
            this.body = body;
            this.head = head;
            ModelPart root = growthLayer(creeper).bakeRoot();
            this.bodyGrowth = root.getChild("body");
            this.headGrowth = root.getChild("head");
        }

        @Override
        public void render(PoseStack ps, MultiBufferSource buffers, int light, T e, float limbSwing, float limbSwingAmount, float partialTick,
                           float ageInTicks, float netHeadYaw, float headPitch) {
            int overlay = LivingEntityRenderer.getOverlayCoords(e, 0.0F);
            float pulse = 1.0F + 0.04F * net.minecraft.util.Mth.sin(ageInTicks * 0.25F);
            for (int pass = 0; pass < 2; pass++) {
                RenderType type = pass == 0 ? RenderType.entityCutoutNoCull(GROWTH) : RenderType.eyes(GROWTH_GLOW);
                int lit = pass == 0 ? light : LightTexture.FULL_BRIGHT;
                int ov = pass == 0 ? overlay : OverlayTexture.NO_OVERLAY;
                ps.pushPose();
                this.body.apply(this.getParentModel()).translateAndRotate(ps);
                ps.scale(pulse, pulse, pulse);
                this.bodyGrowth.render(ps, buffers.getBuffer(type), lit, ov);
                ps.popPose();
                ps.pushPose();
                this.head.apply(this.getParentModel()).translateAndRotate(ps);
                this.headGrowth.render(ps, buffers.getBuffer(type), lit, ov);
                ps.popPose();
            }
        }
    }

    /** Their glowing parasite eyes and spots. */
    static final class Glow<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
        private final ResourceLocation texture;

        Glow(RenderLayerParent<T, M> parent, ResourceLocation texture) {
            super(parent);
            this.texture = texture;
        }

        @Override
        public void render(PoseStack ps, MultiBufferSource buffers, int light, T e, float limbSwing, float limbSwingAmount, float partialTick,
                           float ageInTicks, float netHeadYaw, float headPitch) {
            this.getParentModel().renderToBuffer(ps, buffers.getBuffer(RenderType.eyes(this.texture)), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        }
    }

    /** For HumanoidModel subclasses the generics need a nudge. */
    static <T extends LivingEntity> ModelPart humanoidBody(HumanoidModel<T> m) {
        return m.body;
    }

    static <T extends net.minecraft.world.entity.Entity> ModelPart creeperRoot(CreeperModel<T> m) {
        return m.root();
    }
}
