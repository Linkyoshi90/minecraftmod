package com.linkyoshi90.monstergirls.registry;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.AlbatrossHarpy;
import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
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
import com.linkyoshi90.monstergirls.entity.CrowHarpy;
import com.linkyoshi90.monstergirls.entity.MucusToad;
import com.linkyoshi90.monstergirls.entity.OstrichHarpy;
import com.linkyoshi90.monstergirls.entity.OwlHarpy;
import com.linkyoshi90.monstergirls.entity.PhoenixHarpy;
import com.linkyoshi90.monstergirls.entity.PenguinHarpy;
import com.linkyoshi90.monstergirls.entity.TenguHarpy;
import com.linkyoshi90.monstergirls.entity.FinchHarpy;
import com.linkyoshi90.monstergirls.entity.FlamingoHarpy;
import com.linkyoshi90.monstergirls.entity.CoinMagpieHarpy;
import com.linkyoshi90.monstergirls.entity.RookmournHarpy;
import com.linkyoshi90.monstergirls.entity.CassowrathHarpy;
import com.linkyoshi90.monstergirls.entity.Mailtoad;
import com.linkyoshi90.monstergirls.entity.TricksterRavenHarpy;
import com.linkyoshi90.monstergirls.entity.ToucanHarpy;
import com.linkyoshi90.monstergirls.entity.ZhuqueHarpy;
import com.linkyoshi90.monstergirls.entity.RiflebirdHarpy;
import com.linkyoshi90.monstergirls.entity.SeagullHarpy;
import com.linkyoshi90.monstergirls.entity.SparrowHarpy;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MonsterGirlsMod.MODID);

    // The geo model is 2 blocks tall; the renderer draws it at SparrowHarpyRenderer.SCALE.
    public static final DeferredHolder<EntityType<?>, EntityType<SparrowHarpy>> SPARROW_HARPY =
            ENTITY_TYPES.register("sparrow_harpy", () -> EntityType.Builder.of(SparrowHarpy::new, MobCategory.CREATURE)
                    .sized(0.5F, 1.45F)
                    .eyeHeight(1.3F)
                    .clientTrackingRange(8)
                    .build("sparrow_harpy"));

    // Medium-sized: drawn at CrowHarpyRenderer.SCALE (about 1.75 blocks).
    public static final DeferredHolder<EntityType<?>, EntityType<CrowHarpy>> CROW_HARPY =
            ENTITY_TYPES.register("crow_harpy", () -> EntityType.Builder.of(CrowHarpy::new, MobCategory.CREATURE)
                    .sized(0.55F, 1.7F)
                    .eyeHeight(1.5F)
                    .clientTrackingRange(10)
                    .build("crow_harpy"));

    // The tallest harpy: drawn at OstrichHarpyRenderer.SCALE (about 2.3 blocks).
    public static final DeferredHolder<EntityType<?>, EntityType<OstrichHarpy>> OSTRICH_HARPY =
            ENTITY_TYPES.register("ostrich_harpy", () -> EntityType.Builder.of(OstrichHarpy::new, MobCategory.CREATURE)
                    .sized(0.7F, 2.3F)
                    .eyeHeight(2.05F)
                    .passengerAttachments(new Vec3(0.0, 1.45, -0.3)) // piggyback on her back, not on her head
                    .clientTrackingRange(10)
                    .build("ostrich_harpy"));

    public static final DeferredHolder<EntityType<?>, EntityType<OwlHarpy>> OWL_HARPY =
            ENTITY_TYPES.register("owl_harpy", () -> EntityType.Builder.of(OwlHarpy::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.75F)
                    .eyeHeight(1.55F)
                    .clientTrackingRange(10)
                    .build("owl_harpy"));

    public static final DeferredHolder<EntityType<?>, EntityType<SeagullHarpy>> SEAGULL_HARPY =
            ENTITY_TYPES.register("seagull_harpy", () -> EntityType.Builder.of(SeagullHarpy::new, MobCategory.CREATURE)
                    .sized(0.5F, 1.45F)
                    .eyeHeight(1.3F)
                    .clientTrackingRange(10)
                    .build("seagull_harpy"));

    public static final DeferredHolder<EntityType<?>, EntityType<AlbatrossHarpy>> ALBATROSS_HARPY =
            ENTITY_TYPES.register("albatross_harpy", () -> EntityType.Builder.of(AlbatrossHarpy::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.85F)
                    .eyeHeight(1.65F)
                    .clientTrackingRange(10)
                    .build("albatross_harpy"));

    public static final DeferredHolder<EntityType<?>, EntityType<RiflebirdHarpy>> RIFLEBIRD_HARPY =
            ENTITY_TYPES.register("riflebird_harpy", () -> EntityType.Builder.of(RiflebirdHarpy::new, MobCategory.CREATURE)
                    .sized(0.5F, 1.5F)
                    .eyeHeight(1.35F)
                    .clientTrackingRange(10)
                    .build("riflebird_harpy"));

    // Nether-dwelling and hostile when wild, so she counts against the monster cap; immune to fire and lava.
    public static final DeferredHolder<EntityType<?>, EntityType<PhoenixHarpy>> PHOENIX_HARPY =
            ENTITY_TYPES.register("phoenix_harpy", () -> EntityType.Builder.of(PhoenixHarpy::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.75F)
                    .eyeHeight(1.55F)
                    .fireImmune()
                    .clientTrackingRange(10)
                    .build("phoenix_harpy"));

    public static final DeferredHolder<EntityType<?>, EntityType<TenguHarpy>> TENGU_HARPY =
            ENTITY_TYPES.register("tengu_harpy", () -> EntityType.Builder.of(TenguHarpy::new, MobCategory.CREATURE)
                    .sized(0.55F, 1.6F)
                    .eyeHeight(1.45F)
                    .clientTrackingRange(12)
                    .build("tengu_harpy"));

    public static final DeferredHolder<EntityType<?>, EntityType<ZhuqueHarpy>> ZHUQUE_HARPY =
            ENTITY_TYPES.register("zhuque_harpy", () -> EntityType.Builder.of(ZhuqueHarpy::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.75F)
                    .eyeHeight(1.55F)
                    .fireImmune()
                    .clientTrackingRange(10)
                    .build("zhuque_harpy"));

    public static final DeferredHolder<EntityType<?>, EntityType<CoinMagpieHarpy>> COIN_MAGPIE_HARPY =
            ENTITY_TYPES.register("coin_magpie_harpy", () -> EntityType.Builder.of(CoinMagpieHarpy::new, MobCategory.CREATURE)
                    .sized(0.55F, 1.65F)
                    .eyeHeight(1.45F)
                    .clientTrackingRange(10)
                    .build("coin_magpie_harpy"));

    public static final DeferredHolder<EntityType<?>, EntityType<TricksterRavenHarpy>> TRICKSTER_RAVEN_HARPY =
            ENTITY_TYPES.register("trickster_raven_harpy", () -> EntityType.Builder.of(TricksterRavenHarpy::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .eyeHeight(1.6F)
                    .clientTrackingRange(10)
                    .build("trickster_raven_harpy"));

    public static final DeferredHolder<EntityType<?>, EntityType<RookmournHarpy>> ROOKMOURN_HARPY =
            ENTITY_TYPES.register("rookmourn_harpy", () -> EntityType.Builder.of(RookmournHarpy::new, MobCategory.CREATURE)
                    .sized(0.55F, 1.75F)
                    .eyeHeight(1.5F)
                    .clientTrackingRange(10)
                    .build("rookmourn_harpy"));

    public static final DeferredHolder<EntityType<?>, EntityType<CassowrathHarpy>> CASSOWRATH_HARPY =
            ENTITY_TYPES.register("cassowrath_harpy", () -> EntityType.Builder.of(CassowrathHarpy::new, MobCategory.CREATURE)
                    .sized(0.7F, 2.4F)
                    .eyeHeight(2.15F)
                    .passengerAttachments(new Vec3(0.0, 1.45, -0.3))
                    .clientTrackingRange(10)
                    .build("cassowrath_harpy"));

    public static final DeferredHolder<EntityType<?>, EntityType<Mailtoad>> MAILTOAD =
            ENTITY_TYPES.register("mailtoad", () -> EntityType.Builder.of(Mailtoad::new, MobCategory.CREATURE)
                    .sized(0.65F, 1.55F)
                    .eyeHeight(1.4F)
                    .clientTrackingRange(10)
                    .build("mailtoad"));

    public static final DeferredHolder<EntityType<?>, EntityType<FlamingoHarpy>> FLAMINGO_HARPY =
            ENTITY_TYPES.register("flamingo_harpy", () -> EntityType.Builder.of(FlamingoHarpy::new, MobCategory.CREATURE)
                    .sized(0.55F, 1.95F)
                    .eyeHeight(1.75F)
                    .clientTrackingRange(10)
                    .build("flamingo_harpy"));

    public static final DeferredHolder<EntityType<?>, EntityType<FinchHarpy>> FINCH_HARPY =
            ENTITY_TYPES.register("finch_harpy", () -> EntityType.Builder.of(FinchHarpy::new, MobCategory.CREATURE)
                    .sized(0.5F, 1.45F)
                    .eyeHeight(1.3F)
                    .clientTrackingRange(10)
                    .build("finch_harpy"));

    public static final DeferredHolder<EntityType<?>, EntityType<ToucanHarpy>> TOUCAN_HARPY =
            ENTITY_TYPES.register("toucan_harpy", () -> EntityType.Builder.of(ToucanHarpy::new, MobCategory.CREATURE)
                    .sized(0.5F, 1.5F)
                    .eyeHeight(1.35F)
                    .clientTrackingRange(10)
                    .build("toucan_harpy"));

    public static final DeferredHolder<EntityType<?>, EntityType<PenguinHarpy>> PENGUIN_HARPY =
            ENTITY_TYPES.register("penguin_harpy", () -> EntityType.Builder.of(PenguinHarpy::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.3F)
                    .eyeHeight(1.15F)
                    .clientTrackingRange(10)
                    .build("penguin_harpy"));

    // The frog family's archetype: about 1.8 blocks, drawn at full model scale.
    public static final DeferredHolder<EntityType<?>, EntityType<Bullywug>> BULLYWUG =
            ENTITY_TYPES.register("bullywug", () -> EntityType.Builder.of(Bullywug::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .eyeHeight(1.6F)
                    .clientTrackingRange(10)
                    .build("bullywug"));

    // The toad family's archetype: about 1.45 blocks, 1.75 with the hat.
    public static final DeferredHolder<EntityType<?>, EntityType<MucusToad>> MUCUS_TOAD =
            ENTITY_TYPES.register("mucus_toad", () -> EntityType.Builder.of(MucusToad::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.6F)
                    .eyeHeight(1.4F)
                    .clientTrackingRange(10)
                    .build("mucus_toad"));

    public static final DeferredHolder<EntityType<?>, EntityType<Bullfrog>> BULLFROG =
            ENTITY_TYPES.register("bullfrog", () -> EntityType.Builder.of(Bullfrog::new, MobCategory.CREATURE)
                    .sized(0.7F, 2.0F)
                    .eyeHeight(1.8F)
                    .clientTrackingRange(10)
                    .build("bullfrog"));

    public static final DeferredHolder<EntityType<?>, EntityType<TreeFrog>> TREE_FROG =
            ENTITY_TYPES.register("tree_frog", () -> EntityType.Builder.of(TreeFrog::new, MobCategory.CREATURE)
                    .sized(0.55F, 1.4F)
                    .eyeHeight(1.25F)
                    .clientTrackingRange(10)
                    .build("tree_frog"));

    public static final DeferredHolder<EntityType<?>, EntityType<PoisonDartFrog>> POISON_DART_FROG =
            ENTITY_TYPES.register("poison_dart_frog", () -> EntityType.Builder.of(PoisonDartFrog::new, MobCategory.CREATURE)
                    .sized(0.5F, 1.6F)
                    .eyeHeight(1.45F)
                    .clientTrackingRange(10)
                    .build("poison_dart_frog"));

    public static final DeferredHolder<EntityType<?>, EntityType<GlassFrog>> GLASS_FROG =
            ENTITY_TYPES.register("glass_frog", () -> EntityType.Builder.of(GlassFrog::new, MobCategory.CREATURE)
                    .sized(0.5F, 1.5F)
                    .eyeHeight(1.35F)
                    .clientTrackingRange(10)
                    .build("glass_frog"));

    public static final DeferredHolder<EntityType<?>, EntityType<GoliathFrog>> GOLIATH_FROG =
            ENTITY_TYPES.register("goliath_frog", () -> EntityType.Builder.of(GoliathFrog::new, MobCategory.CREATURE)
                    .sized(0.9F, 2.3F)
                    .eyeHeight(2.05F)
                    .clientTrackingRange(10)
                    .build("goliath_frog"));

    public static final DeferredHolder<EntityType<?>, EntityType<DesertRainFrog>> DESERT_RAIN_FROG =
            ENTITY_TYPES.register("desert_rain_frog", () -> EntityType.Builder.of(DesertRainFrog::new, MobCategory.CREATURE)
                    .sized(0.55F, 1.1F)
                    .eyeHeight(1.0F)
                    .clientTrackingRange(10)
                    .build("desert_rain_frog"));

    public static final DeferredHolder<EntityType<?>, EntityType<FireBelliedToad>> FIRE_BELLIED_TOAD =
            ENTITY_TYPES.register("fire_bellied_toad", () -> EntityType.Builder.of(FireBelliedToad::new, MobCategory.CREATURE)
                    .sized(0.5F, 1.45F)
                    .eyeHeight(1.3F)
                    .fireImmune()
                    .clientTrackingRange(10)
                    .build("fire_bellied_toad"));

    public static final DeferredHolder<EntityType<?>, EntityType<CaneToad>> CANE_TOAD =
            ENTITY_TYPES.register("cane_toad", () -> EntityType.Builder.of(CaneToad::new, MobCategory.CREATURE)
                    .sized(0.65F, 1.55F)
                    .eyeHeight(1.4F)
                    .clientTrackingRange(10)
                    .build("cane_toad"));

    public static final DeferredHolder<EntityType<?>, EntityType<Natterjack>> NATTERJACK =
            ENTITY_TYPES.register("natterjack", () -> EntityType.Builder.of(Natterjack::new, MobCategory.CREATURE)
                    .sized(0.5F, 1.25F)
                    .eyeHeight(1.1F)
                    .clientTrackingRange(10)
                    .build("natterjack"));

    public static final DeferredHolder<EntityType<?>, EntityType<Vodyanoy>> VODYANOY =
            ENTITY_TYPES.register("vodyanoy", () -> EntityType.Builder.of(Vodyanoy::new, MobCategory.CREATURE)
                    .sized(0.8F, 1.9F)
                    .eyeHeight(1.7F)
                    .clientTrackingRange(10)
                    .build("vodyanoy"));

    public static final DeferredHolder<EntityType<?>, EntityType<SurinamToad>> SURINAM_TOAD =
            ENTITY_TYPES.register("surinam_toad", () -> EntityType.Builder.of(SurinamToad::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.25F)
                    .eyeHeight(1.0F)
                    .clientTrackingRange(10)
                    .build("surinam_toad"));

    private ModEntities() {}

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(SPARROW_HARPY.get(), SparrowHarpy.createAttributes().build());
        event.put(CROW_HARPY.get(), CrowHarpy.createAttributes().build());
        event.put(OSTRICH_HARPY.get(), OstrichHarpy.createAttributes().build());
        event.put(OWL_HARPY.get(), OwlHarpy.createAttributes().build());
        event.put(SEAGULL_HARPY.get(), SeagullHarpy.createAttributes().build());
        event.put(ALBATROSS_HARPY.get(), AlbatrossHarpy.createAttributes().build());
        event.put(RIFLEBIRD_HARPY.get(), RiflebirdHarpy.createAttributes().build());
        event.put(PHOENIX_HARPY.get(), PhoenixHarpy.createAttributes().build());
        event.put(TENGU_HARPY.get(), TenguHarpy.createAttributes().build());
        event.put(TOUCAN_HARPY.get(), ToucanHarpy.createAttributes().build());
        event.put(FINCH_HARPY.get(), FinchHarpy.createAttributes().build());
        event.put(FLAMINGO_HARPY.get(), FlamingoHarpy.createAttributes().build());
        event.put(COIN_MAGPIE_HARPY.get(), CoinMagpieHarpy.createAttributes().build());
        event.put(ROOKMOURN_HARPY.get(), RookmournHarpy.createAttributes().build());
        event.put(CASSOWRATH_HARPY.get(), CassowrathHarpy.createAttributes().build());
        event.put(MAILTOAD.get(), Mailtoad.createAttributes().build());
        event.put(TRICKSTER_RAVEN_HARPY.get(), TricksterRavenHarpy.createAttributes().build());
        event.put(ZHUQUE_HARPY.get(), ZhuqueHarpy.createAttributes().build());
        event.put(PENGUIN_HARPY.get(), PenguinHarpy.createAttributes().build());
        event.put(BULLYWUG.get(), Bullywug.createAttributes().build());
        event.put(MUCUS_TOAD.get(), MucusToad.createAttributes().build());
        event.put(BULLFROG.get(), Bullfrog.createAttributes().build());
        event.put(TREE_FROG.get(), TreeFrog.createAttributes().build());
        event.put(POISON_DART_FROG.get(), PoisonDartFrog.createAttributes().build());
        event.put(GLASS_FROG.get(), GlassFrog.createAttributes().build());
        event.put(GOLIATH_FROG.get(), GoliathFrog.createAttributes().build());
        event.put(DESERT_RAIN_FROG.get(), DesertRainFrog.createAttributes().build());
        event.put(FIRE_BELLIED_TOAD.get(), FireBelliedToad.createAttributes().build());
        event.put(CANE_TOAD.get(), CaneToad.createAttributes().build());
        event.put(NATTERJACK.get(), Natterjack.createAttributes().build());
        event.put(VODYANOY.get(), Vodyanoy.createAttributes().build());
        event.put(SURINAM_TOAD.get(), SurinamToad.createAttributes().build());
    }

    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(SPARROW_HARPY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SparrowHarpy::checkSparrowHarpySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(CROW_HARPY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                CrowHarpy::checkCrowHarpySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(OSTRICH_HARPY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                OstrichHarpy::checkOstrichHarpySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(OWL_HARPY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                OwlHarpy::checkOwlHarpySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(SEAGULL_HARPY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SeagullHarpy::checkSeagullHarpySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ALBATROSS_HARPY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AlbatrossHarpy::checkAlbatrossHarpySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(RIFLEBIRD_HARPY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                RiflebirdHarpy::checkRiflebirdHarpySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(PHOENIX_HARPY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                PhoenixHarpy::checkPhoenixHarpySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TENGU_HARPY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                TenguHarpy::checkTenguHarpySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TOUCAN_HARPY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ToucanHarpy::checkToucanHarpySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(FINCH_HARPY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                FinchHarpy::checkFinchHarpySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(FLAMINGO_HARPY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                FlamingoHarpy::checkFlamingoHarpySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ROOKMOURN_HARPY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                RookmournHarpy::checkRookmournHarpySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(CASSOWRATH_HARPY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                CassowrathHarpy::checkCassowrathHarpySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(MAILTOAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbstractAnuran::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(COIN_MAGPIE_HARPY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                CoinMagpieHarpy::checkCoinMagpieHarpySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TRICKSTER_RAVEN_HARPY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                TricksterRavenHarpy::checkTricksterRavenHarpySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ZHUQUE_HARPY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ZhuqueHarpy::checkZhuqueHarpySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(PENGUIN_HARPY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                PenguinHarpy::checkPenguinHarpySpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(BULLYWUG.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbstractAnuran::checkWaterSideSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(MUCUS_TOAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbstractAnuran::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(BULLFROG.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbstractAnuran::checkWaterSideSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(TREE_FROG.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbstractAnuran::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(POISON_DART_FROG.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbstractAnuran::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(GLASS_FROG.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbstractAnuran::checkWaterSideSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(GOLIATH_FROG.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbstractAnuran::checkWaterSideSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(DESERT_RAIN_FROG.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbstractAnuran::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(FIRE_BELLIED_TOAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbstractAnuran::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(CANE_TOAD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbstractAnuran::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(NATTERJACK.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbstractAnuran::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(VODYANOY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                AbstractAnuran::checkWaterSideSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(SURINAM_TOAD.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SurinamToad::checkSurinamSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
