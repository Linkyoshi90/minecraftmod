package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.SparrowHarpy;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class SparrowHarpyRenderer extends GeoEntityRenderer<SparrowHarpy> {
    /** The geo model is 2 blocks tall; sparrows are the small ones. Keep in sync with the hitbox in ModEntities. */
    public static final float SCALE = 0.75F;

    /** model name (HarmontideClientConfig.sparrowModel) -> geo, base texture and per-variant textures */
    private static final Map<String, ResourceLocation> MODELS = new HashMap<>();
    private static final Map<String, ResourceLocation> BASE_TEXTURES = new HashMap<>();
    private static final Map<String, Map<SparrowHarpy.Variant, ResourceLocation>> TEXTURES = new HashMap<>();

    static {
        for (String name : new String[]{"sparrow_harpy", "sparrow_harpy_detailed", "sparrow_harpy_soft"}) {
            MODELS.put(name, HarmontideMod.id("geo/entity/" + name + ".geo.json"));
            BASE_TEXTURES.put(name, HarmontideMod.id("textures/entity/" + name + ".png"));
            Map<SparrowHarpy.Variant, ResourceLocation> byVariant = new EnumMap<>(SparrowHarpy.Variant.class);
            for (SparrowHarpy.Variant variant : SparrowHarpy.Variant.values()) {
                String suffix = variant == SparrowHarpy.Variant.HOUSE ? "" : "_" + variant.getSerializedName();
                byVariant.put(variant, HarmontideMod.id("textures/entity/" + name + suffix + ".png"));
            }
            TEXTURES.put(name, byVariant);
        }
    }

    public SparrowHarpyRenderer(EntityRendererProvider.Context context) {
        // Resolves geo/entity/sparrow_harpy.geo.json and animations/entity/...; true = head follows look direction.
        // The detailed and soft models (tools/gen_sparrow_detailed_harpy.py, gen_sparrow_soft_harpy.py) have the same
        // bones and reuse these animations.
        super(context, new DefaultedEntityGeoModel<>(HarmontideMod.id("sparrow_harpy"), true) {
            @Override
            public ResourceLocation getModelResource(SparrowHarpy harpy) {
                if (harpy.isBaby()) return YoungModels.geo(harpy.modelName());
                return MODELS.get(HarmontideClientConfig.sparrowModel());
            }

            @Override
            public ResourceLocation getAnimationResource(SparrowHarpy harpy) {
                return harpy.isBaby() ? YoungModels.anim(harpy.modelName()) : super.getAnimationResource(harpy);
            }

            /** Her belly rounds out while she is expecting. */
            @Override
            public void setCustomAnimations(SparrowHarpy harpy, long instanceId, AnimationState<SparrowHarpy> animationState) {
                super.setCustomAnimations(harpy, instanceId, animationState);
                var proc = this.getAnimationProcessor();
                Jiggle.install(proc);
                Bellies.harpy(this, harpy);
                PupilLook.lookAt(this, harpy);
                CowgirlPose.apply(proc, harpy, animationState.getPartialTick());
                MilkPose.apply(proc, harpy);
                Jiggle.apply(proc, harpy, animationState.getPartialTick());
            }

            @Override
            public ResourceLocation getTextureResource(SparrowHarpy harpy) {
                ResourceLocation tex;
                if (harpy.isBaby()) tex = Paints.of(YoungModels.tex(harpy.modelName()), harpy.getPaintName());
                else {
                    String model = HarmontideClientConfig.sparrowModel();
                    tex = Paints.shown(harpy.getPaintName()) != null ? Paints.of(BASE_TEXTURES.get(model), harpy.getPaintName())
                            : TEXTURES.get(model).get(harpy.getVariant());
                }
                return harpy.isBare() ? Topless.worn(tex, this.getModelResource(harpy)) : tex;
            }
        });
        this.withScale(SCALE);
        this.shadowRadius = 0.3F;
        this.addRenderLayer(new HeldItemLayer<>(this));
        this.addRenderLayer(new MoonEyesLayer<>(this));
        this.addRenderLayer(new PaintGlowLayer<>(this, SparrowHarpy::getPaintName));
        this.addRenderLayer(new WeddingLookLayer<>(this, false));
    }

    /** Hatchlings are the adult model at a small scale. */
    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, SparrowHarpy animatable, BakedGeoModel model,
                                    boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        // the hatchling model is drawn at its own size, whatever the species' scale is
        if (animatable.isBaby()) super.scaleModelForRender(1.0F, 1.0F, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
        else super.scaleModelForRender(widthScale, heightScale, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }

    /** A jello-painted one is see-through. */
    @Override
    public net.minecraft.client.renderer.RenderType getRenderType(com.linkyoshi90.harmontide.entity.SparrowHarpy animatable, net.minecraft.resources.ResourceLocation texture,
                                                                @javax.annotation.Nullable net.minecraft.client.renderer.MultiBufferSource buffers, float partialTick) {
        return Paints.translucent(animatable.getPaintName()) ? net.minecraft.client.renderer.RenderType.entityTranslucent(texture)
                : super.getRenderType(animatable, texture, buffers, partialTick);
    }
}
