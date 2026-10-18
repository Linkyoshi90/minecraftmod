package com.linkyoshi90.monstergirls.client;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.SparrowHarpy;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.EnumMap;
import java.util.Map;

public class SparrowHarpyRenderer extends GeoEntityRenderer<SparrowHarpy> {
    /** The geo model is 2 blocks tall; sparrows are the small ones. Keep in sync with the hitbox in ModEntities. */
    public static final float SCALE = 0.75F;

    private static final ResourceLocation DETAILED_MODEL = MonsterGirlsMod.id("geo/entity/sparrow_harpy_detailed.geo.json");
    private static final Map<SparrowHarpy.Variant, ResourceLocation> TEXTURES = new EnumMap<>(SparrowHarpy.Variant.class);
    private static final Map<SparrowHarpy.Variant, ResourceLocation> DETAILED_TEXTURES = new EnumMap<>(SparrowHarpy.Variant.class);

    static {
        for (SparrowHarpy.Variant variant : SparrowHarpy.Variant.values()) {
            String suffix = variant == SparrowHarpy.Variant.HOUSE ? "" : "_" + variant.getSerializedName();
            TEXTURES.put(variant, MonsterGirlsMod.id("textures/entity/sparrow_harpy" + suffix + ".png"));
            DETAILED_TEXTURES.put(variant, MonsterGirlsMod.id("textures/entity/sparrow_harpy_detailed" + suffix + ".png"));
        }
    }

    public SparrowHarpyRenderer(EntityRendererProvider.Context context) {
        // Resolves geo/entity/sparrow_harpy.geo.json and animations/entity/...; true = head follows look direction.
        // The detailed model (tools/gen_sparrow_detailed_harpy.py) has the same bones and reuses these animations.
        super(context, new DefaultedEntityGeoModel<>(MonsterGirlsMod.id("sparrow_harpy"), true) {
            @Override
            public ResourceLocation getModelResource(SparrowHarpy harpy) {
                if (harpy.isBaby()) return YoungModels.geo(harpy.modelName());
                return MonsterGirlsClientConfig.detailedSparrow() ? DETAILED_MODEL : super.getModelResource(harpy);
            }

            @Override
            public ResourceLocation getAnimationResource(SparrowHarpy harpy) {
                return harpy.isBaby() ? YoungModels.anim(harpy.modelName()) : super.getAnimationResource(harpy);
            }

            /** Her belly rounds out while she is expecting. */
            @Override
            public void setCustomAnimations(SparrowHarpy harpy, long instanceId, AnimationState<SparrowHarpy> animationState) {
                super.setCustomAnimations(harpy, instanceId, animationState);
                Bellies.harpy(this, harpy);
            }

            @Override
            public ResourceLocation getTextureResource(SparrowHarpy harpy) {
                if (harpy.isBaby()) return YoungModels.tex(harpy.modelName());
                return (MonsterGirlsClientConfig.detailedSparrow() ? DETAILED_TEXTURES : TEXTURES).get(harpy.getVariant());
            }
        });
        this.withScale(SCALE);
        this.shadowRadius = 0.3F;
        this.addRenderLayer(new HeldItemLayer<>(this));
    }

    /** Hatchlings are the adult model at a small scale. */
    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, SparrowHarpy animatable, BakedGeoModel model,
                                    boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        // the hatchling model is drawn at its own size, whatever the species' scale is
        if (animatable.isBaby()) super.scaleModelForRender(1.0F, 1.0F, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
        else super.scaleModelForRender(widthScale, heightScale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }
}
