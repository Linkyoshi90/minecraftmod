package com.linkyoshi90.monstergirls.client;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Renderer for the frog and toad families: geo/entity/&lt;species&gt;.geo.json and textures/entity/&lt;species&gt;[_&lt;variant&gt;].png, with
 * their own models for tadpoles (&lt;species&gt;_tadpole) and froglets (&lt;species&gt;_froglet). The Surinam toad's young
 * are the adult model at a small scale.
 */
public class AnuranRenderer<T extends AbstractAnuran> extends GeoEntityRenderer<T> {
    public AnuranRenderer(EntityRendererProvider.Context context, String species, float scale, float shadowRadius) {
        super(context, new DefaultedEntityGeoModel<>(MonsterGirlsMod.id(species), true) {
            private final Map<String, ResourceLocation> textures = new ConcurrentHashMap<>();

            @Override
            public ResourceLocation getModelResource(T animatable) {
                return animatable.isBaby() && animatable.hasYoungModels() ? YoungModels.geo(animatable.modelName()) : super.getModelResource(animatable);
            }

            @Override
            public ResourceLocation getAnimationResource(T animatable) {
                return animatable.isBaby() && animatable.hasYoungModels() ? YoungModels.anim(animatable.modelName()) : super.getAnimationResource(animatable);
            }

            /** An expecting frog or toad grows round in the belly as her day goes on. */
            @Override
            public void setCustomAnimations(T animatable, long instanceId, AnimationState<T> animationState) {
                super.setCustomAnimations(animatable, instanceId, animationState);
                Bellies.anuran(this, animatable);
                if (animatable instanceof com.linkyoshi90.monstergirls.entity.Bullywug wug) {   // the outfit that comes off
                    boolean off = wug.isUndressed();
                    for (String b : new String[]{"skirt", "cattails"}) {
                        var bone = this.getAnimationProcessor().getBone(b);
                        if (bone != null) bone.setHidden(off);
                    }
                }
            }

            @Override
            public ResourceLocation getTextureResource(T animatable) {
                String paint = Paints.shown(animatable.getPaintName());                    // a painted one: her paint over the default colours
                if (animatable.isBaby() && animatable.hasYoungModels()) return Paints.of(YoungModels.tex(animatable.modelName()), paint);
                if (paint != null) return Paints.of(MonsterGirlsMod.id("textures/entity/" + species + ".png"), paint);
                // colour variants: textures/entity/<species>_<variant>.png (the default colours have no suffix)
                return this.textures.computeIfAbsent(animatable.getVariantSuffix(),
                        suffix -> MonsterGirlsMod.id("textures/entity/" + species + suffix + ".png"));
            }
        });
        this.withScale(scale);
        this.addRenderLayer(new MoonEyesLayer<>(this));
        this.addRenderLayer(new PaintGlowLayer<>(this, a -> a.getPaintName()));
        this.shadowRadius = shadowRadius;
    }

    /** The young have their own models, drawn at their own size; only the Surinam toad's are a shrunken adult. */
    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, T animatable, BakedGeoModel model,
                                    boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        if (animatable.isBaby() && animatable.hasYoungModels()) {
            super.scaleModelForRender(1.0F, 1.0F, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
        } else {
            float scale = animatable.isBaby() ? AbstractAnuran.BABY_SCALE : 1.0F;
            super.scaleModelForRender(widthScale * scale, heightScale * scale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
        }
    }

    /** A jello-painted one is see-through. */
    @Override
    public net.minecraft.client.renderer.RenderType getRenderType(T animatable, net.minecraft.resources.ResourceLocation texture,
                                                                @javax.annotation.Nullable net.minecraft.client.renderer.MultiBufferSource buffers, float partialTick) {
        return Paints.translucent(animatable.getPaintName()) ? net.minecraft.client.renderer.RenderType.entityTranslucent(texture)
                : super.getRenderType(animatable, texture, buffers, partialTick);
    }
}
