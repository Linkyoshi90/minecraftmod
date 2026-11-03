package com.linkyoshi90.monstergirls.client;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.AlbatrossHarpy;
import com.linkyoshi90.monstergirls.entity.PenguinHarpy;
import com.linkyoshi90.monstergirls.entity.TenguHarpy;
import com.linkyoshi90.monstergirls.entity.FinchHarpy;
import com.linkyoshi90.monstergirls.entity.FlamingoHarpy;
import com.linkyoshi90.monstergirls.entity.CoinMagpieHarpy;
import com.linkyoshi90.monstergirls.entity.PeaflareHarpy;
import com.linkyoshi90.monstergirls.entity.EmberuHarpy;
import com.linkyoshi90.monstergirls.entity.IbisetHarpy;
import com.linkyoshi90.monstergirls.entity.IronbeakHarpy;
import com.linkyoshi90.monstergirls.entity.StigowlHarpy;
import com.linkyoshi90.monstergirls.entity.YatagarasuHarpy;
import com.linkyoshi90.monstergirls.entity.SkycrowHarpy;
import com.linkyoshi90.monstergirls.entity.ReliquaryDoveHarpy;
import com.linkyoshi90.monstergirls.entity.PoultryHarpy;
import com.linkyoshi90.monstergirls.entity.LandharpyHarpy;
import com.linkyoshi90.monstergirls.entity.ArgentowlisHarpy;
import com.linkyoshi90.monstergirls.entity.IbiskingHarpy;
import com.linkyoshi90.monstergirls.entity.MotmoraHarpy;
import com.linkyoshi90.monstergirls.entity.PelicanHarpy;
import com.linkyoshi90.monstergirls.entity.SwanHarpy;
import com.linkyoshi90.monstergirls.entity.DuckHarpy;
import com.linkyoshi90.monstergirls.entity.HummingbirdHarpy;
import com.linkyoshi90.monstergirls.entity.RocHarpy;
import com.linkyoshi90.monstergirls.entity.ZizHarpy;
import com.linkyoshi90.monstergirls.entity.BennuHarpy;
import com.linkyoshi90.monstergirls.entity.GarudaHarpy;
import com.linkyoshi90.monstergirls.entity.AlkonostHarpy;
import com.linkyoshi90.monstergirls.entity.EmuHarpy;
import com.linkyoshi90.monstergirls.entity.LechuzaHarpy;
import com.linkyoshi90.monstergirls.entity.MacawHarpy;
import com.linkyoshi90.monstergirls.entity.MirrorCraneHarpy;
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

    /** The fridge opens in vanilla's chest screen. */
    @SubscribeEvent
    public static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(com.linkyoshi90.monstergirls.registry.ModMenus.FRIDGE.get(), net.minecraft.client.gui.screens.inventory.ContainerScreen::new);
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
    public static void registerParticles(net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(com.linkyoshi90.monstergirls.registry.ModParticles.MILK_DRIP.get(), MilkDripParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(com.linkyoshi90.monstergirls.registry.ModBlockEntities.FIGURINE.get(), FigurineRenderer::new);
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
        event.registerEntityRenderer(ModEntities.MIRROR_CRANE_HARPY.get(), ctx -> new HarpyRenderer<MirrorCraneHarpy>(ctx, "mirror_crane_harpy", 0.85F, 0.35F, MirrorCraneHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.EMBERU_HARPY.get(), ctx -> new HarpyRenderer<EmberuHarpy>(ctx, "emberu_harpy", 0.85F, 0.4F, EmberuHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.IRONBEAK_HARPY.get(), ctx -> new HarpyRenderer<IronbeakHarpy>(ctx, "ironbeak_harpy", 0.8F, 0.3F, IronbeakHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.IBISET_HARPY.get(), ctx -> new HarpyRenderer<IbisetHarpy>(ctx, "ibiset_harpy", 0.85F, 0.35F, IbisetHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.STIGOWL_HARPY.get(), ctx -> new HarpyRenderer<StigowlHarpy>(ctx, "stigowl_harpy", 0.85F, 0.35F, StigowlHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.YATAGARASU_HARPY.get(), ctx -> new HarpyRenderer<YatagarasuHarpy>(ctx, "yatagarasu_harpy", 0.8F, 0.3F, YatagarasuHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.SKYCROW_HARPY.get(), ctx -> new HarpyRenderer<SkycrowHarpy>(ctx, "skycrow_harpy", 0.9F, 0.35F, SkycrowHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.RELIQUARY_DOVE_HARPY.get(), ctx -> new HarpyRenderer<ReliquaryDoveHarpy>(ctx, "reliquary_dove_harpy", 0.85F, 0.35F, ReliquaryDoveHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.POULTRY_HARPY.get(), ctx -> new HarpyRenderer<PoultryHarpy>(ctx, "poultry_harpy", 0.85F, 0.4F, PoultryHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.LANDHARPY_HARPY.get(), ctx -> new HarpyRenderer<LandharpyHarpy>(ctx, "landharpy_harpy", 0.85F, 0.4F, LandharpyHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.ARGENTOWLIS_HARPY.get(), ctx -> new HarpyRenderer<ArgentowlisHarpy>(ctx, "argentowlis_harpy", 1.3F, 0.5F, ArgentowlisHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.IBISKING_HARPY.get(), ctx -> new HarpyRenderer<IbiskingHarpy>(ctx, "ibisking_harpy", 0.85F, 0.35F, IbiskingHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.MOTMORA_HARPY.get(), ctx -> new HarpyRenderer<MotmoraHarpy>(ctx, "motmora_harpy", 0.85F, 0.35F, MotmoraHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.PELICAN_HARPY.get(), ctx -> new HarpyRenderer<PelicanHarpy>(ctx, "pelican_harpy", 0.85F, 0.35F, PelicanHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.SWAN_HARPY.get(), ctx -> new HarpyRenderer<SwanHarpy>(ctx, "swan_harpy", 0.85F, 0.35F, SwanHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.DUCK_HARPY.get(), ctx -> new HarpyRenderer<DuckHarpy>(ctx, "duck_harpy", 0.85F, 0.35F, DuckHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.HUMMINGBIRD_HARPY.get(), ctx -> new HarpyRenderer<HummingbirdHarpy>(ctx, "hummingbird_harpy", 0.72F, 0.25F, HummingbirdHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.ROC_HARPY.get(), ctx -> new HarpyRenderer<RocHarpy>(ctx, "roc_harpy", 2.1F, 1.0F, RocHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.ZIZ_HARPY.get(), ctx -> new HarpyRenderer<ZizHarpy>(ctx, "ziz_harpy", 0.85F, 0.35F, ZizHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.BENNU_HARPY.get(), ctx -> new HarpyRenderer<BennuHarpy>(ctx, "bennu_harpy", 0.85F, 0.35F, BennuHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.GARUDA_HARPY.get(), ctx -> new HarpyRenderer<GarudaHarpy>(ctx, "garuda_harpy", 0.82F, 0.35F, GarudaHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.ALKONOST_HARPY.get(), ctx -> new HarpyRenderer<AlkonostHarpy>(ctx, "alkonost_harpy", 0.8F, 0.3F, AlkonostHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.EMU_HARPY.get(), ctx -> new HarpyRenderer<EmuHarpy>(ctx, "emu_harpy", 0.85F, 0.4F, EmuHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.LECHUZA_HARPY.get(), ctx -> new HarpyRenderer<LechuzaHarpy>(ctx, "lechuza_harpy", 0.85F, 0.35F, LechuzaHarpy::getVariantSuffix));
        event.registerEntityRenderer(ModEntities.MACAW_HARPY.get(), ctx -> new HarpyRenderer<MacawHarpy>(ctx, "macaw_harpy", 0.78F, 0.3F, MacawHarpy::getVariantSuffix));
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
