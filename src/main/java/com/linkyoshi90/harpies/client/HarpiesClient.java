package com.linkyoshi90.harpies.client;

import com.linkyoshi90.harpies.HarpiesMod;
import com.linkyoshi90.harpies.entity.AlbatrossHarpy;
import com.linkyoshi90.harpies.entity.OwlHarpy;
import com.linkyoshi90.harpies.entity.PhoenixHarpy;
import com.linkyoshi90.harpies.entity.RiflebirdHarpy;
import com.linkyoshi90.harpies.entity.SeagullHarpy;
import com.linkyoshi90.harpies.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

@EventBusSubscriber(modid = HarpiesMod.MODID, value = Dist.CLIENT)
public final class HarpiesClient {
    private HarpiesClient() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SPARROW_HARPY.get(), SparrowHarpyRenderer::new);
        event.registerEntityRenderer(ModEntities.CROW_HARPY.get(), CrowHarpyRenderer::new);
        event.registerEntityRenderer(ModEntities.OSTRICH_HARPY.get(), OstrichHarpyRenderer::new);
        event.registerEntityRenderer(ModEntities.OWL_HARPY.get(), ctx -> new HarpyRenderer<OwlHarpy>(ctx, "owl_harpy", 0.85F, 0.35F, OwlHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.SEAGULL_HARPY.get(), ctx -> new HarpyRenderer<SeagullHarpy>(ctx, "seagull_harpy", 0.75F, 0.3F, SeagullHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.ALBATROSS_HARPY.get(), ctx -> new HarpyRenderer<AlbatrossHarpy>(ctx, "albatross_harpy", 0.85F, 0.4F, AlbatrossHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.RIFLEBIRD_HARPY.get(), ctx -> new HarpyRenderer<RiflebirdHarpy>(ctx, "riflebird_harpy", 0.75F, 0.3F, RiflebirdHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.PHOENIX_HARPY.get(), ctx -> {
            HarpyRenderer<PhoenixHarpy> renderer = new HarpyRenderer<>(ctx, "phoenix_harpy", 0.85F, 0.35F, PhoenixHarpy::getVariantSuffix);
            renderer.addRenderLayer(new AutoGlowingGeoLayer<>(renderer)); // flames glow (phoenix_harpy[_variant]_glowmask.png)
            return renderer;
        });
    }
}
