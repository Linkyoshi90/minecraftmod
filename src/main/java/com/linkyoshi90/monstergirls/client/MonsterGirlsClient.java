package com.linkyoshi90.monstergirls.client;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.AlbatrossHarpy;
import com.linkyoshi90.monstergirls.entity.PenguinHarpy;
import com.linkyoshi90.monstergirls.entity.TenguHarpy;
import com.linkyoshi90.monstergirls.entity.FinchHarpy;
import com.linkyoshi90.monstergirls.entity.FlamingoHarpy;
import com.linkyoshi90.monstergirls.entity.CoinMagpieHarpy;
import com.linkyoshi90.monstergirls.entity.PeaflareHarpy;
import com.linkyoshi90.monstergirls.entity.PumpkingullHarpy;
import com.linkyoshi90.monstergirls.entity.CondoruneHarpy;
import com.linkyoshi90.monstergirls.entity.DodoboldHarpy;
import com.linkyoshi90.monstergirls.entity.RookmournHarpy;
import com.linkyoshi90.monstergirls.entity.CassowrathHarpy;
import com.linkyoshi90.monstergirls.entity.Mailtoad;
import com.linkyoshi90.monstergirls.entity.TricksterRavenHarpy;
import com.linkyoshi90.monstergirls.entity.ToucanHarpy;
import com.linkyoshi90.monstergirls.entity.ZhuqueHarpy;
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
import com.linkyoshi90.monstergirls.entity.Vodyanoy;
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
            // the moon dial's face (models/item/moon_dial.json picks one of eight)
            net.minecraft.client.renderer.item.ItemProperties.register(com.linkyoshi90.monstergirls.registry.ModItems.MOON_DIAL.get(), MonsterGirlsMod.id("phase"),
                    (stack, level, entity, seed) -> {
                        var world = net.minecraft.client.Minecraft.getInstance().level;
                        return world == null ? 0.0F : world.getMoonPhase() / 8.0F;
                    });
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
        event.registerEntityRenderer(ModEntities.TENGU_HARPY.get(), ctx -> new HarpyRenderer<TenguHarpy>(ctx, "tengu_harpy", 0.8F, 0.35F, TenguHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.ZHUQUE_HARPY.get(), ctx -> {
            HarpyRenderer<ZhuqueHarpy> renderer = new HarpyRenderer<>(ctx, "zhuque_harpy", 0.85F, 0.35F, ZhuqueHarpy::getVariantSuffix);
            renderer.addRenderLayer(new AutoGlowingGeoLayer<>(renderer)); // her flame tips glow (zhuque_harpy[_variant]_glowmask.png)
            return renderer;
        });
        event.registerEntityRenderer(ModEntities.ROOKMOURN_HARPY.get(), ctx -> new HarpyRenderer<RookmournHarpy>(ctx, "rookmourn_harpy", 0.8F, 0.3F, RookmournHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.CASSOWRATH_HARPY.get(), ctx -> new HarpyRenderer<CassowrathHarpy>(ctx, "cassowrath_harpy", 1.0F, 0.45F, CassowrathHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.MAILTOAD.get(), ctx -> new AnuranRenderer<Mailtoad>(ctx, "mailtoad", 1.0F, 0.45F));
        event.registerEntityRenderer(ModEntities.DODOBOLD_HARPY.get(), ctx -> new HarpyRenderer<DodoboldHarpy>(ctx, "dodobold_harpy", 0.85F, 0.4F, DodoboldHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.PEAFLARE_HARPY.get(), ctx -> new HarpyRenderer<PeaflareHarpy>(ctx, "peaflare_harpy", 0.85F, 0.35F, PeaflareHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.PUMPKINGULL_HARPY.get(), ctx -> new HarpyRenderer<PumpkingullHarpy>(ctx, "pumpkingull_harpy", 0.75F, 0.3F, PumpkingullHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.CONDORUNE_HARPY.get(), ctx -> new HarpyRenderer<CondoruneHarpy>(ctx, "condorune_harpy", 0.85F, 0.35F, CondoruneHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.COIN_MAGPIE_HARPY.get(), ctx -> new HarpyRenderer<CoinMagpieHarpy>(ctx, "coin_magpie_harpy", 0.8F, 0.3F, CoinMagpieHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.TRICKSTER_RAVEN_HARPY.get(), ctx -> new HarpyRenderer<TricksterRavenHarpy>(ctx, "trickster_raven_harpy", 0.85F, 0.35F, TricksterRavenHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.FLAMINGO_HARPY.get(), ctx -> new HarpyRenderer<FlamingoHarpy>(ctx, "flamingo_harpy", 0.8F, 0.35F, FlamingoHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.FINCH_HARPY.get(), ctx -> new HarpyRenderer<FinchHarpy>(ctx, "finch_harpy", 0.75F, 0.3F, f -> ""));
        event.registerEntityRenderer(ModEntities.TOUCAN_HARPY.get(), ctx -> new HarpyRenderer<ToucanHarpy>(ctx, "toucan_harpy", 0.78F, 0.3F, ToucanHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.PENGUIN_HARPY.get(), ctx -> new HarpyRenderer<PenguinHarpy>(ctx, "penguin_harpy", 0.78F, 0.35F, PenguinHarpy::getVariantSuffix));
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
        event.registerEntityRenderer(ModEntities.VODYANOY.get(), ctx -> new AnuranRenderer<Vodyanoy>(ctx, "vodyanoy", 1.0F, 0.5F));
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
