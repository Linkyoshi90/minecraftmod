package com.linkyoshi90.monstergirls.client;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Renderer for monstergirls: one geo model named after the species, plus held items. A species with colour or species
 * variants passes a texture suffix function (e.g. "_snowy" -> textures/entity/owl_harpy_snowy.png).
 */
public class HarpyRenderer<T extends AbstractHarpy> extends GeoEntityRenderer<T> {
    public HarpyRenderer(EntityRendererProvider.Context context, String species, float scale, float shadowRadius) {
        this(context, species, scale, shadowRadius, harpy -> "");
    }

    public HarpyRenderer(EntityRendererProvider.Context context, String species, float scale, float shadowRadius,
                         Function<T, String> textureSuffix) {
        super(context, new DefaultedEntityGeoModel<>(MonsterGirlsMod.id(species), true) {
            private final Map<String, ResourceLocation> textures = new ConcurrentHashMap<>();

            @Override
            public ResourceLocation getModelResource(T harpy) {
                return harpy.isBaby() ? YoungModels.geo(harpy.modelName()) : super.getModelResource(harpy);
            }

            @Override
            public ResourceLocation getAnimationResource(T harpy) {
                return harpy.isBaby() ? YoungModels.anim(harpy.modelName()) : super.getAnimationResource(harpy);
            }

            /** Her belly rounds out while she is expecting. */
            @Override
            public void setCustomAnimations(T harpy, long instanceId, AnimationState<T> animationState) {
                super.setCustomAnimations(harpy, instanceId, animationState);
                Bellies.harpy(this, harpy);
            }

            @Override
            public ResourceLocation getTextureResource(T harpy) {
                if (harpy.isBaby()) return Paints.of(YoungModels.tex(harpy.modelName()), harpy.getPaintName());
                if (Paints.shown(harpy.getPaintName()) != null) return Paints.of(MonsterGirlsMod.id("textures/entity/" + species + ".png"), harpy.getPaintName());
                return this.textures.computeIfAbsent(textureSuffix.apply(harpy),
                        suffix -> MonsterGirlsMod.id("textures/entity/" + species + suffix + ".png"));
            }
        });
        this.withScale(scale);
        this.shadowRadius = shadowRadius;
        this.addRenderLayer(new HeldItemLayer<>(this));
        this.addRenderLayer(new BlushOverlayLayer<>(this, species, T::isBlushing));
        this.addRenderLayer(new MoonEyesLayer<>(this));
        this.addRenderLayer(new PaintGlowLayer<>(this, T::getPaintName));
        this.addRenderLayer(new WeddingLookLayer<>(this, false));
    }

    /** Hatchlings are the adult model at a small scale. */
    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, T animatable, BakedGeoModel model,
                                    boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        // the hatchling model is drawn at its own size, whatever the species' scale is
        if (animatable.isBaby()) super.scaleModelForRender(1.0F, 1.0F, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
        else super.scaleModelForRender(widthScale, heightScale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }

    /** A jello-painted one is see-through. */
    @Override
    public net.minecraft.client.renderer.RenderType getRenderType(T animatable, net.minecraft.resources.ResourceLocation texture,
                                                                @javax.annotation.Nullable net.minecraft.client.renderer.MultiBufferSource buffers, float partialTick) {
        return Paints.translucent(animatable.getPaintName()) ? net.minecraft.client.renderer.RenderType.entityTranslucent(texture)
                : super.getRenderType(animatable, texture, buffers, partialTick);
    }
}
