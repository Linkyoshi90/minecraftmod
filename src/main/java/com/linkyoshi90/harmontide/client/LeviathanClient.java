package com.linkyoshi90.harmontide.client;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.leviathan.LeviathanRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** The Dream-Leviathan's client side: her renderers and her calf's, the parasites', and the blessing's heart bar. */
@EventBusSubscriber(modid = HarmontideMod.MODID, value = Dist.CLIENT)
public final class LeviathanClient {
    private LeviathanClient() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(LeviathanRegistry.DREAM_LEVIATHAN.get(), DreamLeviathanRenderer::new);
        event.registerEntityRenderer(LeviathanRegistry.DREAM_CALF.get(), DreamLeviathanRenderer.Calf::new);
        event.registerEntityRenderer(LeviathanRegistry.INFESTED_ZOMBIE.get(), ParasiteRenderers::zombie);
        event.registerEntityRenderer(LeviathanRegistry.INFESTED_SKELETON.get(), ParasiteRenderers::skeleton);
        event.registerEntityRenderer(LeviathanRegistry.INFESTED_CREEPER.get(), ParasiteRenderers::creeper);
    }

    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.PLAYER_HEALTH, HarmontideMod.id("dream_heart"), DreamHeartHud::render);
    }
}
