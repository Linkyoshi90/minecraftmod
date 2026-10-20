package com.linkyoshi90.monstergirls.client;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Kitsune;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One renderer for every species of the later families (models by tools/gen_&lt;family&gt;.py): the species' geo,
 * animations and textures/entity/&lt;species&gt;[_&lt;variant&gt;].png, scaled to her height (young and split slimes
 * smaller), her belly while she is expecting, a kitsune's tails by bond, a true nekomata's second tail, the wolf
 * spider's brood while she carries one. Slimes are translucent; the magma slime glows.
 */
public class GirlRenderer extends GeoEntityRenderer<AbstractMonsterGirl> {
    private final GirlSpecies species;

    public GirlRenderer(EntityRendererProvider.Context context, GirlSpecies species) {
        super(context, new Model(species));
        this.species = species;
        this.shadowRadius = species.width() * 0.6F;
        if (species == GirlSpecies.MAGMA_SLIME) this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    static class Model extends DefaultedEntityGeoModel<AbstractMonsterGirl> {
        private final GirlSpecies species;
        private final Map<String, ResourceLocation> textures = new ConcurrentHashMap<>();

        Model(GirlSpecies species) {
            super(MonsterGirlsMod.id(species.id()), true);
            this.species = species;
        }

        @Override
        public ResourceLocation getTextureResource(AbstractMonsterGirl girl) {
            return this.textures.computeIfAbsent(girl.getVariantSuffix(), s -> MonsterGirlsMod.id("textures/entity/" + this.species.id() + s + ".png"));
        }

        @Override
        public void setCustomAnimations(AbstractMonsterGirl girl, long instanceId, AnimationState<AbstractMonsterGirl> state) {
            super.setCustomAnimations(girl, instanceId, state);
            var proc = this.getAnimationProcessor();
            GeoBone belly = proc.getBone("belly");
            if (belly != null) {
                float swell = girl.isBaby() ? 0.0F : girl.getBellySwell();
                belly.setHidden(swell <= 0.0F);
                if (swell > 0.0F) {
                    belly.setScaleX(1.05F + 0.3F * swell);
                    belly.setScaleY(1.05F + 0.2F * swell);
                    belly.setScaleZ(1.1F + 1.3F * swell);
                }
            }
            if (girl instanceof Kitsune k) {
                int n = k.visibleTails();
                for (int i = 1; i <= 9; i++) {
                    GeoBone t = proc.getBone("tail_" + i);
                    if (t != null) t.setHidden(i > n);
                }
            }
            boolean twin = girl.isTrueForm() && this.species.family() == GirlSpecies.Family.NEKOMATA;
            for (String b : new String[]{"tail2", "tail2_mid", "tail2_tip"}) {
                GeoBone t = proc.getBone(b);
                if (t != null) t.setHidden(!twin);
            }
            GeoBone brood = proc.getBone("brood");
            if (brood != null) brood.setHidden(!girl.hasFlag(AbstractMonsterGirl.FLAG_CARRYING));
        }
    }

    @Override
    public void scaleModelForRender(float widthScale, float heightScale, PoseStack poseStack, AbstractMonsterGirl girl, BakedGeoModel model,
                                    boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
        float s = this.species.renderScale() * girl.sizeScale();
        super.scaleModelForRender(widthScale * s, heightScale * s, poseStack, girl, model, isReRender, partialTick, packedLight, packedOverlay);
    }

    @Override
    public RenderType getRenderType(AbstractMonsterGirl girl, ResourceLocation texture, @Nullable MultiBufferSource buffers, float partialTick) {
        return this.species.family() == GirlSpecies.Family.SLIME ? RenderType.entityTranslucent(texture) : super.getRenderType(girl, texture, buffers, partialTick);
    }
}
