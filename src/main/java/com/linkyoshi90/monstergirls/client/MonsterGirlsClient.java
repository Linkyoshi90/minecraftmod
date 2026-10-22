package com.linkyoshi90.monstergirls.client;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.AlbatrossHarpy;
import com.linkyoshi90.monstergirls.entity.Bullywug;
import com.linkyoshi90.monstergirls.entity.Bullfrog;
import com.linkyoshi90.monstergirls.entity.TreeFrog;
import com.linkyoshi90.monstergirls.entity.PoisonDartFrog;
import com.linkyoshi90.monstergirls.entity.GlassFrog;
import com.linkyoshi90.monstergirls.entity.GoliathFrog;
import com.linkyoshi90.monstergirls.entity.DesertRainFrog;
import com.linkyoshi90.monstergirls.entity.FireBelliedToad;
import com.linkyoshi90.monstergirls.entity.CaneToad;
import com.linkyoshi90.monstergirls.entity.Natterjack;
import com.linkyoshi90.monstergirls.entity.SurinamToad;
import com.linkyoshi90.monstergirls.entity.MucusToad;
import com.linkyoshi90.monstergirls.entity.OwlHarpy;
import com.linkyoshi90.monstergirls.entity.PhoenixHarpy;
import com.linkyoshi90.monstergirls.entity.RiflebirdHarpy;
import com.linkyoshi90.monstergirls.entity.SeagullHarpy;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import com.linkyoshi90.monstergirls.registry.ModGirls;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

@EventBusSubscriber(modid = MonsterGirlsMod.MODID, value = Dist.CLIENT)
public final class MonsterGirlsClient {
    private MonsterGirlsClient() {}

    /** Nectar looks like water, tinted gold, and is see-through like it. */
    @SubscribeEvent
    public static void clientExtensions(net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event) {
        event.registerFluidType(new net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions() {
            @Override
            public net.minecraft.resources.ResourceLocation getStillTexture() {
                return net.minecraft.resources.ResourceLocation.withDefaultNamespace("block/water_still");
            }

            @Override
            public net.minecraft.resources.ResourceLocation getFlowingTexture() {
                return net.minecraft.resources.ResourceLocation.withDefaultNamespace("block/water_flow");
            }

            @Override
            public net.minecraft.resources.ResourceLocation getOverlayTexture() {
                return net.minecraft.resources.ResourceLocation.withDefaultNamespace("block/water_overlay");
            }

            @Override
            public int getTintColor() {
                return 0xE0F2B04A;
            }
        }, com.linkyoshi90.monstergirls.registry.ModFluids.NECTAR_TYPE.get());
    }

    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public static void clientSetup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(com.linkyoshi90.monstergirls.registry.ModFluids.NECTAR.get(), net.minecraft.client.renderer.RenderType.translucent());
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(com.linkyoshi90.monstergirls.registry.ModFluids.FLOWING_NECTAR.get(), net.minecraft.client.renderer.RenderType.translucent());
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SPARROW_HARPY.get(), SparrowHarpyRenderer::new);
        event.registerEntityRenderer(ModEntities.CROW_HARPY.get(), CrowHarpyRenderer::new);
        event.registerEntityRenderer(ModEntities.OSTRICH_HARPY.get(), OstrichHarpyRenderer::new);
        event.registerEntityRenderer(ModEntities.OWL_HARPY.get(), ctx -> new HarpyRenderer<OwlHarpy>(ctx, "owl_harpy", 0.85F, 0.35F, OwlHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.SEAGULL_HARPY.get(), ctx -> new HarpyRenderer<SeagullHarpy>(ctx, "seagull_harpy", 0.75F, 0.3F, SeagullHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.ALBATROSS_HARPY.get(), ctx -> new HarpyRenderer<AlbatrossHarpy>(ctx, "albatross_harpy", 0.85F, 0.4F, AlbatrossHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.RIFLEBIRD_HARPY.get(), ctx -> new HarpyRenderer<RiflebirdHarpy>(ctx, "riflebird_harpy", 0.75F, 0.3F, RiflebirdHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.BULLYWUG.get(), ctx -> new AnuranRenderer<Bullywug>(ctx, "bullywug", 1.0F, 0.4F));
        event.registerEntityRenderer(ModEntities.MUCUS_TOAD.get(), ctx -> new AnuranRenderer<MucusToad>(ctx, "mucus_toad", 1.0F, 0.45F));
        event.registerEntityRenderer(ModEntities.BULLFROG.get(), ctx -> new AnuranRenderer<Bullfrog>(ctx, "bullfrog", 1.0F, 0.49F));
        event.registerEntityRenderer(ModEntities.TREE_FROG.get(), ctx -> new AnuranRenderer<TreeFrog>(ctx, "tree_frog", 1.0F, 0.39F));
        event.registerEntityRenderer(ModEntities.POISON_DART_FROG.get(), ctx -> new AnuranRenderer<PoisonDartFrog>(ctx, "poison_dart_frog", 1.0F, 0.35F));
        event.registerEntityRenderer(ModEntities.GLASS_FROG.get(), ctx -> new GlassFrogRenderer(ctx));
        event.registerEntityRenderer(ModEntities.GOLIATH_FROG.get(), ctx -> new AnuranRenderer<GoliathFrog>(ctx, "goliath_frog", 1.0F, 0.63F));
        event.registerEntityRenderer(ModEntities.DESERT_RAIN_FROG.get(), ctx -> new AnuranRenderer<DesertRainFrog>(ctx, "desert_rain_frog", 1.0F, 0.39F));
        event.registerEntityRenderer(ModEntities.FIRE_BELLIED_TOAD.get(), ctx -> {
            AnuranRenderer<FireBelliedToad> renderer = new AnuranRenderer<>(ctx, "fire_bellied_toad", 1.0F, 0.35F);
            renderer.addRenderLayer(new AutoGlowingGeoLayer<>(renderer)); // the lantern glows (fire_bellied_toad_glowmask.png)
            return renderer;
        });
        event.registerEntityRenderer(ModEntities.CANE_TOAD.get(), ctx -> new AnuranRenderer<CaneToad>(ctx, "cane_toad", 1.0F, 0.45F));
        event.registerEntityRenderer(ModEntities.NATTERJACK.get(), ctx -> new AnuranRenderer<Natterjack>(ctx, "natterjack", 1.0F, 0.35F));
        event.registerEntityRenderer(ModEntities.SURINAM_TOAD.get(), ctx -> new AnuranRenderer<SurinamToad>(ctx, "surinam_toad", 1.0F, 0.4F));
        for (GirlSpecies s : GirlSpecies.values()) {
            event.registerEntityRenderer(ModGirls.type(s), ctx -> new GirlRenderer(ctx, s));
        }
        event.registerEntityRenderer(ModEntities.PHOENIX_HARPY.get(), ctx -> {
            HarpyRenderer<PhoenixHarpy> renderer = new HarpyRenderer<>(ctx, "phoenix_harpy", 0.85F, 0.35F, PhoenixHarpy::getVariantSuffix);
            renderer.addRenderLayer(new AutoGlowingGeoLayer<>(renderer)); // flames glow (phoenix_harpy[_variant]_glowmask.png)
            return renderer;
        });
    }
}
