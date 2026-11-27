package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.CrowHarpy;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CrowHarpyRenderer extends GeoEntityRenderer<CrowHarpy> {
    /** The geo model is ~2 blocks tall; crows are medium-sized. Keep in sync with the hitbox in ModEntities. */
    public static final float SCALE = 0.85F;

    private static final Map<String, ResourceLocation> TEXTURES = new ConcurrentHashMap<>();

    public CrowHarpyRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(HarmontideMod.id("crow_harpy"), true) {
            @Override
            public ResourceLocation getModelResource(CrowHarpy crow) {
                return crow.isBaby() ? YoungModels.geo(crow.modelName()) : super.getModelResource(crow);
            }

            @Override
            public ResourceLocation getAnimationResource(CrowHarpy crow) {
                return crow.isBaby() ? YoungModels.anim(crow.modelName()) : super.getAnimationResource(crow);
            }

            /** Her belly rounds out while she is expecting. */
            @Override
            public void setCustomAnimations(CrowHarpy harpy, long instanceId, AnimationState<CrowHarpy> animationState) {
                super.setCustomAnimations(harpy, instanceId, animationState);
                var proc = this.getAnimationProcessor();
                Jiggle.install(proc);
                Bellies.harpy(this, harpy);
                PupilLook.lookAt(this, harpy);
                CowgirlPose.apply(proc, harpy, animationState.getPartialTick());
                BirthPose.apply(proc, harpy, animationState.getPartialTick());
                FeedPose.apply(proc, harpy, animationState.getPartialTick());
                OralPose.apply(proc, harpy, animationState.getPartialTick());
                StandPose.apply(proc, harpy, animationState.getPartialTick());
                FacePose.apply(proc, harpy, animationState.getPartialTick());
                TribePose.apply(proc, harpy, animationState.getPartialTick());
                MountPose.apply(proc, harpy, animationState.getPartialTick());
                MilkPose.apply(proc, harpy);
                Jiggle.apply(proc, harpy, animationState.getPartialTick());
            }

            @Override
            public ResourceLocation getTextureResource(CrowHarpy crow) {
                ResourceLocation tex;
                if (crow.isBaby()) tex = Paints.of(YoungModels.tex(crow.modelName()), crow.getPaintName());
                else if (Paints.shown(crow.getPaintName()) != null) tex = Paints.of(HarmontideMod.id("textures/entity/crow_harpy.png"), crow.getPaintName());
                else tex = TEXTURES.computeIfAbsent(crow.getVariantSuffix(), suffix -> HarmontideMod.id("textures/entity/crow_harpy" + suffix + ".png"));
                return crow.isBare() ? Topless.worn(tex, this.getModelResource(crow)) : tex;
            }
        });
        this.withScale(SCALE);
        this.shadowRadius = 0.35F;
        this.addRenderLayer(new HeldItemLayer<>(this));
        this.addRenderLayer(new MoonEyesLayer<>(this));
        this.addRenderLayer(new PaintGlowLayer<>(this, CrowHarpy::getPaintName));
        this.addRenderLayer(new WeddingLookLayer<>(this, false));
    }

    /** Hatchlings are the adult model at a small scale. */
    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, CrowHarpy animatable, BakedGeoModel model,
                                    boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        // the hatchling model is drawn at its own size, whatever the species' scale is
        if (animatable.isBaby()) super.scaleModelForRender(1.0F, 1.0F, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
        else super.scaleModelForRender(widthScale, heightScale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }

    /** A jello-painted one is see-through. */
    @Override
    public net.minecraft.client.renderer.RenderType getRenderType(com.linkyoshi90.harmontide.entity.CrowHarpy animatable, net.minecraft.resources.ResourceLocation texture,
                                                                @javax.annotation.Nullable net.minecraft.client.renderer.MultiBufferSource buffers, float partialTick) {
        return Paints.translucent(animatable.getPaintName()) ? net.minecraft.client.renderer.RenderType.entityTranslucent(texture)
                : super.getRenderType(animatable, texture, buffers, partialTick);
    }
}
