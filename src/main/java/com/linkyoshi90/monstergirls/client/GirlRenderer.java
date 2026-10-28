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
    static final java.util.Set<String> GLOWING = java.util.Set.of("magma_slime", "will_o_wisp_undead", "salamander_lizardfolk", "jack_o_lantern_devil",
            "jellyfish_mollusk", "dark_elf", "ender_slime", "aurorantler_centaur", "axolume_mermaid", "blaze_cat_nekomata", "moonseal_mermaid", "chochin_obake_devil");
    /** Species drawn see-through (jelly, flame, insect wings, ghosts). */
    static final java.util.Set<String> TRANSLUCENT = java.util.Set.of("will_o_wisp_undead", "jellyfish_mollusk", "cheshire_nekomata");

    public GirlRenderer(EntityRendererProvider.Context context, GirlSpecies species) {
        super(context, new Model(species));
        this.species = species;
        this.shadowRadius = species.width() * 0.6F;
        if (GLOWING.contains(species.id())) this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
        if (species.family() == GirlSpecies.Family.CENTAUR) this.addRenderLayer(new TorsoArmourLayer(this));
        if (species == GirlSpecies.SHEEP_MINOTAUR || species == GirlSpecies.RHIWOOL_MINOTAUR) this.addRenderLayer(new DyedWoolLayer(this));
        this.addRenderLayer(new MoonEyesLayer<>(this));
        this.addRenderLayer(new HeldItemLayer(this));
        this.addRenderLayer(new PaintGlowLayer<>(this, AbstractMonsterGirl::getPaintName));
        if (species == GirlSpecies.CHESHIRE_NEKOMATA) {                    // her grin glows, whole, however faded the rest of her is
            this.addRenderLayer(new AutoGlowingGeoLayer<>(this) {
                @Override
                public void render(PoseStack poseStack, AbstractMonsterGirl girl, BakedGeoModel model, RenderType renderType, MultiBufferSource buffers,
                                   VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                    RenderType glowType = this.getRenderType(girl);
                    if (glowType == null) return;
                    this.getRenderer().reRender(model, poseStack, buffers, girl, glowType, buffers.getBuffer(glowType), partialTick, 0xF000F0, packedOverlay, -1);
                }
            });
        }
        if (species == GirlSpecies.FIREFLY_BUG) {                          // every firefly pulses in time with the others (the world's clock)
            this.addRenderLayer(new AutoGlowingGeoLayer<>(this) {
                @Override
                public void render(PoseStack poseStack, AbstractMonsterGirl girl, BakedGeoModel model, RenderType renderType, MultiBufferSource buffers,
                                   VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
                    float glow = fireflyPulse(girl.level().getGameTime() + partialTick);
                    RenderType glowType = this.getRenderType(girl);
                    if (glow <= 0.02F || glowType == null) return;
                    int c = (int) (glow * 255);
                    this.getRenderer().reRender(model, poseStack, buffers, girl, glowType, buffers.getBuffer(glowType), partialTick, 0xF000F0, packedOverlay,
                            c << 24 | c << 16 | c << 8 | c);
                }
            });
        }
    }

    /** A firefly's glow over her 3-second cycle: it swells and fades for two seconds, then she is dark for one. */
    static float fireflyPulse(float time) {
        float phase = time % 60.0F;
        if (phase >= 40.0F) return 0.0F;
        float s = net.minecraft.util.Mth.sin((float) Math.PI * phase / 40.0F);
        return s * s;
    }

    /** How much of the cheshire cat shows to this player: all of her when looked at straight, fading to nothing (but her grin) aside. */
    static float cheshireAlpha(AbstractMonsterGirl girl) {
        var viewer = net.minecraft.client.Minecraft.getInstance().player;
        if (viewer == null) return 1.0F;
        net.minecraft.world.phys.Vec3 to = girl.position().add(0, girl.getBbHeight() * 0.6, 0).subtract(viewer.getEyePosition());
        if (to.lengthSqr() < 1.5 * 1.5) return 1.0F;
        double dot = to.normalize().dot(viewer.getViewVector(1.0F));
        return (float) net.minecraft.util.Mth.clamp((dot - 0.94) / 0.04, 0.0, 1.0);
    }

    /** A girl with her own death animation (the golem breaks apart) is not tipped over as she dies. */
    @Override
    protected float getDeathMaxRotation(AbstractMonsterGirl girl) {
        return girl.hasDeathAnimation() ? 0.0F : super.getDeathMaxRotation(girl);
    }

    /** A dyed (or merged) slime is drawn in her own colour, any colour at all. */
    @Override
    public software.bernie.geckolib.util.Color getRenderColor(AbstractMonsterGirl girl, float partialTick, int packedLight) {
        if (this.species == GirlSpecies.CHESHIRE_NEKOMATA) {
            return software.bernie.geckolib.util.Color.ofARGB(cheshireAlpha(girl), 1.0F, 1.0F, 1.0F);
        }
        if (girl instanceof com.linkyoshi90.monstergirls.entity.girl.SlimeGirl slime && slime.hasCustomColour()) {
            return software.bernie.geckolib.util.Color.ofOpaque(slime.colour());
        }
        return super.getRenderColor(girl, partialTick, packedLight);
    }

    /** A dyed sheep's fleece: &lt;model&gt;_wool.png (the wool alone, in white) drawn over her again in the dye's colour. */
    static class DyedWoolLayer extends software.bernie.geckolib.renderer.layer.GeoRenderLayer<AbstractMonsterGirl> {
        DyedWoolLayer(GirlRenderer renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poseStack, AbstractMonsterGirl girl, BakedGeoModel model, RenderType renderType, MultiBufferSource buffers,
                           VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            if (!(girl instanceof com.linkyoshi90.monstergirls.entity.girl.Minotaur m) || m.getWoolDye() == null || m.isShorn() || girl.getPaint() > 0) return;
            String name = girl.getSpecies().id() + (girl.hasFlag(AbstractMonsterGirl.FLAG_MALE) ? "_male" : "") + "_wool";
            RenderType wool = RenderType.entityCutoutNoCull(MonsterGirlsMod.id("textures/entity/" + name + ".png"));
            this.getRenderer().reRender(model, poseStack, buffers, girl, wool, buffers.getBuffer(wool), partialTick, packedLight, packedOverlay,
                    m.getWoolDye().getTextureDiffuseColor());
        }
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

        /** The minotaur bulls have their own model, textures and animation file: &lt;species&gt;_male; so do the homage looks
         *  in {@link AbstractMonsterGirl#OWN_MODELS}: &lt;species&gt;_&lt;variant&gt;. */
        private String modelName(AbstractMonsterGirl girl) {
            if (girl.hasFlag(AbstractMonsterGirl.FLAG_MALE)) return this.species.id() + "_male";
            return ownModel(girl) ? this.species.id() + "_" + girl.getVariantName() : this.species.id();
        }

        private static boolean ownModel(AbstractMonsterGirl girl) {
            return AbstractMonsterGirl.OWN_MODELS.contains(girl.getVariantName());
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
            if (girl instanceof com.linkyoshi90.monstergirls.entity.girl.SlimeGirl slime && slime.hasCustomColour()) {   // a light grey copy, tinted by her colour
                return this.textures.computeIfAbsent(name + "_tint", s -> MonsterGirlsMod.id("textures/entity/" + s + ".png"));
            }
            String paint = girl.getPaintName();
            if (paint != null) return this.textures.computeIfAbsent(name + "_paint_" + paint, s -> MonsterGirlsMod.id("textures/entity/" + s + ".png"));
            String suffix = ownModel(girl) ? "" : girl.getVariantSuffix();         // an own model's texture is its own name
            return this.textures.computeIfAbsent(name + suffix, s -> MonsterGirlsMod.id("textures/entity/" + s + ".png"));
        }

        @Override
        public void setCustomAnimations(AbstractMonsterGirl girl, long instanceId, AnimationState<AbstractMonsterGirl> state) {
            super.setCustomAnimations(girl, instanceId, state);
            var proc = this.getAnimationProcessor();
            var look = state.getData(software.bernie.geckolib.constant.DataTickets.ENTITY_MODEL_DATA);
            AimPose.apply(proc, girl, look == null ? 0.0F : look.headPitch(), state.getPartialTick());   // a weapon raised over any animation
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
            if (girl.isInjured()) {                                          // a broken wing hangs limp
                for (String w : new String[]{"wing_l", "wing2_l"}) {
                    GeoBone b = proc.getBone(w);
                    // ponytail: droop angle not checked in a dev client; flip the sign if it points up
                    if (b != null) {
                        b.setRotX(0.5F);
                        b.setRotY(0.3F);
                        b.setRotZ(-1.1F);
                    }
                }
            }
            GeoBone tail = proc.getBone("tail");                               // a gecko's tail grows back after she sheds it
            if (tail != null && girl.getSpecies() == GirlSpecies.GECKO_LIZARDFOLK) tail.setHidden(girl.hasFlag(AbstractMonsterGirl.FLAG_TAILLESS));
            for (String b : new String[]{"wrap_arm_r", "wrap_arm_l", "wrap_fore_r", "wrap_fore_l", "wrap_shin_r", "wrap_shin_l"}) {   // the mummy's layers
                GeoBone w = proc.getBone(b);
                if (w != null) w.setHidden(girl.hasFlag(AbstractMonsterGirl.FLAG_UNWRAP_A));
            }
            for (String b : new String[]{"wrap_torso", "wrap_thigh_r", "wrap_thigh_l"}) {
                GeoBone w = proc.getBone(b);
                if (w != null) w.setHidden(girl.hasFlag(AbstractMonsterGirl.FLAG_UNWRAP_B));
            }
            for (String b : new String[]{"wool", "wool_hips", "wool_head"}) {         // a shorn sheep
                GeoBone w = proc.getBone(b);
                if (w != null) w.setHidden(girl.hasFlag(AbstractMonsterGirl.FLAG_SHORN));
            }
            GeoBone pack = proc.getBone("pack");                               // the yak's chest, once she carries one
            if (pack != null) pack.setHidden(!girl.hasFlag(AbstractMonsterGirl.FLAG_CHEST));
            GeoBone blindfold = proc.getBone("blindfold");                     // the medusa's, once she has a friend
            if (blindfold != null) blindfold.setHidden(!girl.hasFlag(AbstractMonsterGirl.FLAG_BLINDFOLD));
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
        return this.species.family() == GirlSpecies.Family.SLIME || TRANSLUCENT.contains(this.species.id()) || this.species.id().endsWith("_bug")
                || Paints.translucent(girl.getPaintName()) ? RenderType.entityTranslucent(texture) : super.getRenderType(girl, texture, buffers, partialTick);
    }

    /** What she holds in her right hand (a weapon from her friend, an elf's bow), drawn at her hand bone. */
    static class HeldItemLayer extends software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer<AbstractMonsterGirl> {
        HeldItemLayer(GirlRenderer renderer) {
            super(renderer, (bone, girl) -> "hand_r".equals(bone.getName()) ? girl.getMainHandItem() : null, (bone, girl) -> null);
        }

        @Override
        protected net.minecraft.world.item.ItemDisplayContext getTransformTypeForStack(software.bernie.geckolib.cache.object.GeoBone bone,
                                                                                      net.minecraft.world.item.ItemStack stack, AbstractMonsterGirl girl) {
            return net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        }

        @Override
        protected void renderStackForBone(com.mojang.blaze3d.vertex.PoseStack pose, software.bernie.geckolib.cache.object.GeoBone bone,
                                          net.minecraft.world.item.ItemStack stack, AbstractMonsterGirl girl,
                                          net.minecraft.client.renderer.MultiBufferSource buffers, float partialTick, int light, int overlay) {
            pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90.0F));         // out of her fist, pointing forward
            pose.translate(0.0F, 0.06F, -0.1F);
            super.renderStackForBone(pose, bone, stack, girl, buffers, partialTick, light, overlay);
        }
    }
}
