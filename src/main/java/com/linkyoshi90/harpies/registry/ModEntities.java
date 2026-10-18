package com.linkyoshi90.harpies.registry;

import com.linkyoshi90.harpies.HarpiesMod;
import com.linkyoshi90.harpies.entity.AlbatrossHarpy;
import com.linkyoshi90.harpies.entity.CrowHarpy;
import com.linkyoshi90.harpies.entity.OstrichHarpy;
import com.linkyoshi90.harpies.entity.OwlHarpy;
import com.linkyoshi90.harpies.entity.PhoenixHarpy;
import com.linkyoshi90.harpies.entity.RiflebirdHarpy;
import com.linkyoshi90.harpies.entity.SeagullHarpy;
import com.linkyoshi90.harpies.entity.SparrowHarpy;
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
            DeferredRegister.create(Registries.ENTITY_TYPE, HarpiesMod.MODID);

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
    }
}
