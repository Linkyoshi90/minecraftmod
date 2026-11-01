package com.linkyoshi90.monstergirls.client;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.OstrichHarpy;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.EnumMap;
import java.util.Map;

public class OstrichHarpyRenderer extends GeoEntityRenderer<OstrichHarpy> {
    /** The geo model is ~2.45 blocks tall. Keep in sync with the hitbox in ModEntities. */
    public static final float SCALE = 0.95F;

    private static final Map<OstrichHarpy.Variant, ResourceLocation> TEXTURES = new EnumMap<>(OstrichHarpy.Variant.class);

    static {
        for (OstrichHarpy.Variant variant : OstrichHarpy.Variant.values()) {
            String suffix = variant == OstrichHarpy.Variant.OSTRICH ? "" : "_" + variant.getSerializedName();
            TEXTURES.put(variant, MonsterGirlsMod.id("textures/entity/ostrich_harpy" + suffix + ".png"));
        }
    }

    public OstrichHarpyRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(MonsterGirlsMod.id("ostrich_harpy"), true) {
            @Override
            public ResourceLocation getModelResource(OstrichHarpy ostrich) {
                return ostrich.isBaby() ? YoungModels.geo(ostrich.modelName()) : super.getModelResource(ostrich);
            }

            @Override
            public ResourceLocation getAnimationResource(OstrichHarpy ostrich) {
                return ostrich.isBaby() ? YoungModels.anim(ostrich.modelName()) : super.getAnimationResource(ostrich);
            }

            /** Her belly rounds out while she is expecting. */
            @Override
            public void setCustomAnimations(OstrichHarpy harpy, long instanceId, AnimationState<OstrichHarpy> animationState) {
                super.setCustomAnimations(harpy, instanceId, animationState);
                Bellies.harpy(this, harpy);
            }

            @Override
            public ResourceLocation getTextureResource(OstrichHarpy ostrich) {
                if (ostrich.isBaby()) return Paints.of(YoungModels.tex(ostrich.modelName()), ostrich.getPaintName());
                if (Paints.shown(ostrich.getPaintName()) != null) return Paints.of(MonsterGirlsMod.id("textures/entity/ostrich_harpy.png"), ostrich.getPaintName());
                return TEXTURES.get(ostrich.getVariant());
            }
        });
        this.withScale(SCALE);
        this.shadowRadius = 0.5F;
        this.addRenderLayer(new MoonEyesLayer<>(this));
        this.addRenderLayer(new PaintGlowLayer<>(this, OstrichHarpy::getPaintName));
    }

    /** Hatchlings are the adult model at a small scale. */
    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, OstrichHarpy animatable, BakedGeoModel model,
                                    boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        // the hatchling model is drawn at its own size, whatever the species' scale is
        if (animatable.isBaby()) super.scaleModelForRender(1.0F, 1.0F, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
        else super.scaleModelForRender(widthScale, heightScale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }

    /** A jello-painted one is see-through. */
    @Override
    public net.minecraft.client.renderer.RenderType getRenderType(com.linkyoshi90.monstergirls.entity.OstrichHarpy animatable, net.minecraft.resources.ResourceLocation texture,
                                                                @javax.annotation.Nullable net.minecraft.client.renderer.MultiBufferSource buffers, float partialTick) {
        return Paints.translucent(animatable.getPaintName()) ? net.minecraft.client.renderer.RenderType.entityTranslucent(texture)
                : super.getRenderType(animatable, texture, buffers, partialTick);
    }
}
