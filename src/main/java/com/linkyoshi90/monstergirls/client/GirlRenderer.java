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
import software.bernie.geckolib.renderer.layer.ItemArmorGeoLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One renderer for every species of the later families (models by tools/gen_&lt;family&gt;.py): the species' geo,
 * animations and textures/entity/&lt;species&gt;[_&lt;variant&gt;].png, scaled to her height (young and split slimes
 * smaller), her belly while she is expecting, a kitsune's tails by bond, a nekomata's second tail (true form, or born
 * with it), the wolf spider's brood while she carries one, a mermaid's seashells once she has found some, a wide grin
 * while a centaur runs beside a player, and a centaur's chestplate over her torso. Slimes are translucent; the magma
 * slime glows.
 */
public class GirlRenderer extends GeoEntityRenderer<AbstractMonsterGirl> {
    private final GirlSpecies species;
    /** Species with a glow mask (textures/entity/&lt;texture&gt;_glowmask.png for every colour). */
    static final java.util.Set<String> GLOWING = java.util.Set.of("magma_slime", "will_o_wisp_undead", "salamander_lizardfolk", "jack_o_lantern_devil");
    /** Species drawn see-through (jelly, flame, insect wings, ghosts). */
    static final java.util.Set<String> TRANSLUCENT = java.util.Set.of("will_o_wisp_undead");

    public GirlRenderer(EntityRendererProvider.Context context, GirlSpecies species) {
        super(context, new Model(species));
        this.species = species;
        this.shadowRadius = species.width() * 0.6F;
        if (GLOWING.contains(species.id())) this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
        if (species.family() == GirlSpecies.Family.CENTAUR) this.addRenderLayer(new TorsoArmourLayer(this));
    }

    /** A worn chestplate, fitted to the invisible armor_torso box in her model. */
    static class TorsoArmourLayer extends ItemArmorGeoLayer<AbstractMonsterGirl> {
        TorsoArmourLayer(GirlRenderer renderer) {
            super(renderer);
        }

        @Nullable
        @Override
        protected ItemStack getArmorItemForBone(GeoBone bone, AbstractMonsterGirl girl) {
            return "armor_torso".equals(bone.getName()) ? girl.getItemBySlot(EquipmentSlot.CHEST) : null;
        }

        @Override
        protected EquipmentSlot getEquipmentSlotForBone(GeoBone bone, ItemStack stack, AbstractMonsterGirl girl) {
            return EquipmentSlot.CHEST;
        }

        @Override
        protected ModelPart getModelPartForBone(GeoBone bone, EquipmentSlot slot, ItemStack stack, AbstractMonsterGirl girl, HumanoidModel<?> baseModel) {
            return baseModel.body;
        }
    }

    static class Model extends DefaultedEntityGeoModel<AbstractMonsterGirl> {
        private final GirlSpecies species;
        private final Map<String, ResourceLocation> textures = new ConcurrentHashMap<>();

        Model(GirlSpecies species) {
            super(MonsterGirlsMod.id(species.id()), true);
            this.species = species;
        }

        /** The minotaur bulls have their own model, textures and animation file: &lt;species&gt;_male. */
        private String modelName(AbstractMonsterGirl girl) {
            return girl.hasFlag(AbstractMonsterGirl.FLAG_MALE) ? this.species.id() + "_male" : this.species.id();
        }

        @Override
        public ResourceLocation getModelResource(AbstractMonsterGirl girl) {
            return this.textures.computeIfAbsent("geo:" + this.modelName(girl), s -> MonsterGirlsMod.id("geo/entity/" + this.modelName(girl) + ".geo.json"));
        }

        @Override
        public ResourceLocation getAnimationResource(AbstractMonsterGirl girl) {
            return this.textures.computeIfAbsent("anim:" + this.modelName(girl), s -> MonsterGirlsMod.id("animations/entity/" + this.modelName(girl) + ".animation.json"));
        }

        @Override
        public ResourceLocation getTextureResource(AbstractMonsterGirl girl) {
            String name = this.modelName(girl);
            return this.textures.computeIfAbsent(name + girl.getVariantSuffix(), s -> MonsterGirlsMod.id("textures/entity/" + s + ".png"));
        }

        @Override
        public void setCustomAnimations(AbstractMonsterGirl girl, long instanceId, AnimationState<AbstractMonsterGirl> state) {
            super.setCustomAnimations(girl, instanceId, state);
            var proc = this.getAnimationProcessor();
            // the jack o'lantern hiding as a pumpkin: only the disguise bone shows (it is shrunk away in every animation)
            GeoBone disguise = proc.getBone("disguise");
            boolean hiding = disguise != null && girl.hasFlag(AbstractMonsterGirl.FLAG_HIDDEN);
            if (disguise != null) {
                for (GeoBone b : proc.getRegisteredBones()) b.setHidden(hiding && b != disguise && !b.getName().equals("root"));
                if (hiding) {
                    disguise.setScaleX(1.0F);
                    disguise.setScaleY(1.0F);
                    disguise.setScaleZ(1.0F);
                }
            }
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
            boolean twin = (girl.isTrueForm() || girl.getTails() >= 2) && this.species.family() == GirlSpecies.Family.NEKOMATA;
            for (String b : new String[]{"tail2", "tail2_mid", "tail2_tip"}) {
                GeoBone t = proc.getBone(b);
                if (t != null) t.setHidden(!twin);
            }
            GeoBone brood = proc.getBone("brood");
            if (brood != null) brood.setHidden(!girl.hasFlag(AbstractMonsterGirl.FLAG_CARRYING));
            GeoBone shells = proc.getBone("seashells");
            if (shells != null) shells.setHidden(!girl.hasFlag(AbstractMonsterGirl.FLAG_SHELLS));
            if (hiding) {
                for (GeoBone b : proc.getRegisteredBones()) if (b != disguise && !b.getName().equals("root")) b.setHidden(true);
            }
            GeoBone mouth = proc.getBone("mouth");
            if (mouth != null && girl.hasFlag(AbstractMonsterGirl.FLAG_SMILE)) {      // a grin from ear to ear
                mouth.setScaleX(2.2F);
                mouth.setScaleY(1.5F);
                mouth.setPosZ(-0.4F);                                               // the open mouth slides out of the face
            }
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
        return this.species.family() == GirlSpecies.Family.SLIME || TRANSLUCENT.contains(this.species.id()) || this.species.id().endsWith("_bug") ? RenderType.entityTranslucent(texture) : super.getRenderType(girl, texture, buffers, partialTick);
    }
}
