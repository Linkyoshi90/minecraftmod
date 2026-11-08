package com.linkyoshi90.monstergirls.client;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A possessed villager, pig or cat (entity/Monsterization#POSSESSED) keeps her own body, but her eyes glow the realm's violet,
 * bright even in the dark, as a spider's do. The server tells the client who is possessed ({@link #set}); the textures come
 * from tools/gen_possessed_eyes.py.
 */
public class PossessedEyesLayer<T extends LivingEntity, M extends EntityModel<T>> extends EyesLayer<T, M> {
    private static final Set<Integer> POSSESSED = ConcurrentHashMap.newKeySet();
    private final RenderType type;

    public PossessedEyesLayer(RenderLayerParent<T, M> parent, String texture) {
        super(parent);
        this.type = RenderType.eyes(MonsterGirlsMod.id("textures/entity/possessed/" + texture + ".png"));
    }

    @Override
    public RenderType renderType() {
        return this.type;
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity, float limbSwing, float limbSwingAmount, float partialTick,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        if (!POSSESSED.contains(entity.getId()) || entity.isBaby()) return;
        super.render(pose, buffers, light, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
    }

    /** From the server: this entity is (or no longer is) possessed. */
    public static void set(int entityId, boolean possessed) {
        if (possessed) POSSESSED.add(entityId);
        else POSSESSED.remove(entityId);
    }

    public static boolean shown(int entityId) {
        return POSSESSED.contains(entityId);
    }

    @EventBusSubscriber(modid = MonsterGirlsMod.MODID, value = Dist.CLIENT)
    public static final class Setup {
        private Setup() {}

        @SubscribeEvent
        @SuppressWarnings({"unchecked", "rawtypes"})
        public static void addLayers(EntityRenderersEvent.AddLayers event) {
            add(event, EntityType.VILLAGER, "villager_eyes");
            add(event, EntityType.PIG, "pig_eyes");
            add(event, EntityType.CAT, "cat_eyes");
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static void add(EntityRenderersEvent.AddLayers event, EntityType<?> type, String texture) {
            if (event.getRenderer((EntityType) type) instanceof LivingEntityRenderer renderer) renderer.addLayer(new PossessedEyesLayer(renderer, texture));
        }

        /** Entity ids are only good within one world. */
        @SubscribeEvent
        public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
            POSSESSED.clear();
        }
    }
}
