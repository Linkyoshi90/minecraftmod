package com.linkyoshi90.monstergirls.client;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.util.function.Function;

/**
 * Two little dark horns on the head of a possessed creature (entity/Monsterization#POSSESSED), beside her glowing eyes
 * ({@link PossessedEyesLayer}): drawn on the vanilla model's head part, so they turn and nod with it. Each kind gives where
 * her head's top is and how big the horns are. Texture: tools/gen_possessed_eyes.py (horns.png).
 */
public class PossessedHornsLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation TEXTURE = MonsterGirlsMod.id("textures/entity/possessed/horns.png");
    private final Function<M, ModelPart> head;
    private final ModelPart horns;

    /** top: the head box's top (its y, in model pixels); x: how far apart; z: how far forward; size: the base's width. */
    public PossessedHornsLayer(RenderLayerParent<T, M> parent, Function<M, ModelPart> head, float top, float x, float z, float size) {
        super(parent);
        this.head = head;
        MeshDefinition mesh = new MeshDefinition();
        float tip = size * 0.5F;
        CubeListBuilder cubes = CubeListBuilder.create();
        for (float sx : new float[]{-x, x}) {
            cubes.texOffs(0, 0).addBox(sx - size / 2, top - size, z - size / 2, size, size, size);
            float lean = sx < 0 ? -tip * 0.5F : tip * 0.5F;                  // the tip leans outward a little
            cubes.texOffs(8, 0).addBox(sx - tip / 2 + lean, top - size - size * 1.2F, z - tip / 2, tip, size * 1.2F, tip);
        }
        mesh.getRoot().addOrReplaceChild("horns", cubes, PartPose.ZERO);
        this.horns = LayerDefinition.create(mesh, 16, 16).bakeRoot().getChild("horns");
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (!PossessedEyesLayer.shown(entity.getId()) || entity.isBaby() || entity.isInvisible()) return;
        ModelPart part = this.head.apply(this.getParentModel());
        if (part == null) return;
        pose.pushPose();
        part.translateAndRotate(pose);
        this.horns.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }

    /** A model's head part by its field name (mojmap names, as NeoForge runs with), or null. */
    private static <M> Function<M, ModelPart> field(Class<?> owner) {
        return model -> {
            try {
                java.lang.reflect.Field f = owner.getDeclaredField("head");
                f.setAccessible(true);
                return (ModelPart) f.get(model);
            } catch (ReflectiveOperationException | ClassCastException e) {
                return null;
            }
        };
    }

    @EventBusSubscriber(modid = MonsterGirlsMod.MODID, value = Dist.CLIENT)
    public static final class Setup {
        private Setup() {}

        @SubscribeEvent
        @SuppressWarnings({"unchecked", "rawtypes"})
        public static void addLayers(EntityRenderersEvent.AddLayers event) {
            Function villagerHead = m -> ((net.minecraft.client.model.VillagerModel) m).getHead();
            add(event, EntityType.VILLAGER, villagerHead, -10, 2.5F, -1, 2);
            add(event, EntityType.WANDERING_TRADER, villagerHead, -10, 2.5F, -1, 2);
            add(event, EntityType.PIG, field(net.minecraft.client.model.QuadrupedModel.class), -4, 2.5F, -5, 2);
            add(event, EntityType.CAT, field(net.minecraft.client.model.OcelotModel.class), -2, 1.5F, -1, 1);
            add(event, EntityType.CHICKEN, field(net.minecraft.client.model.ChickenModel.class), -6, 1.25F, 0, 1);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static void add(EntityRenderersEvent.AddLayers event, EntityType<?> type, Function head, float top, float x, float z, float size) {
            if (event.getRenderer((EntityType) type) instanceof LivingEntityRenderer renderer) {
                renderer.addLayer(new PossessedHornsLayer(renderer, head, top, x, z, size));
            }
        }
    }
}
