package com.linkyoshi90.monstergirls.registry;

import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Arachne;
import com.linkyoshi90.monstergirls.entity.girl.Canid;
import com.linkyoshi90.monstergirls.entity.girl.Rodent;
import com.linkyoshi90.monstergirls.entity.girl.Crustacean;
import com.linkyoshi90.monstergirls.entity.girl.Marsupial;
import com.linkyoshi90.monstergirls.entity.girl.BatGirl;
import com.linkyoshi90.monstergirls.entity.girl.Rider;
import com.linkyoshi90.monstergirls.entity.girl.Theropod;
import com.linkyoshi90.monstergirls.entity.girl.Armoured;
import com.linkyoshi90.monstergirls.entity.girl.Construct;
import com.linkyoshi90.monstergirls.entity.girl.Centaur;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Jackalope;
import com.linkyoshi90.monstergirls.entity.girl.Minotaur;
import com.linkyoshi90.monstergirls.entity.girl.Elf;
import com.linkyoshi90.monstergirls.entity.girl.Undead;
import com.linkyoshi90.monstergirls.entity.girl.Lizardfolk;
import com.linkyoshi90.monstergirls.entity.girl.Dragon;
import com.linkyoshi90.monstergirls.entity.girl.Devil;
import com.linkyoshi90.monstergirls.entity.girl.Bug;
import com.linkyoshi90.monstergirls.entity.girl.Flora;
import com.linkyoshi90.monstergirls.entity.girl.Mollusk;
import com.linkyoshi90.monstergirls.entity.girl.Kitsune;
import com.linkyoshi90.monstergirls.entity.girl.Lamia;
import com.linkyoshi90.monstergirls.entity.girl.Merfolk;
import com.linkyoshi90.monstergirls.entity.girl.Nekomata;
import com.linkyoshi90.monstergirls.entity.girl.SlimeGirl;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.EnumMap;
import java.util.Map;

/**
 * The entity types, attributes, spawn rules and spawn eggs of every {@link GirlSpecies} (the families after the
 * harpies and anurans), registered from the species table instead of one field per species.
 */
public final class ModGirls {
    public static final Map<GirlSpecies, DeferredHolder<EntityType<?>, EntityType<AbstractMonsterGirl>>> TYPES = new EnumMap<>(GirlSpecies.class);
    public static final Map<GirlSpecies, DeferredItem<DeferredSpawnEggItem>> EGGS = new EnumMap<>(GirlSpecies.class);

    static {
        for (GirlSpecies s : GirlSpecies.values()) {
            EntityType.EntityFactory<AbstractMonsterGirl> factory = (type, level) -> create(type, level, s);
            DeferredHolder<EntityType<?>, EntityType<AbstractMonsterGirl>> holder = ModEntities.ENTITY_TYPES.register(s.id(), () -> {
                EntityType.Builder<AbstractMonsterGirl> b = EntityType.Builder.of(factory, s.category())
                        .sized(s.width(), s.height()).eyeHeight(s.height() * 0.88F).clientTrackingRange(10);
                if (fireImmune(s)) b = b.fireImmune();
                return b.build(s.id());
            });
            TYPES.put(s, holder);
            EGGS.put(s, ModItems.ITEMS.register(s.id() + "_spawn_egg", () -> new DeferredSpawnEggItem(holder, s.eggColor(), s.eggSpots(), new Item.Properties())));
        }
    }

    private ModGirls() {}

    /** Species that lava and fire cannot hurt. */
    public static boolean fireImmune(GirlSpecies s) {
        return switch (s.id()) {
            case "magma_slime", "volcano_jackalope", "blood_elf", "salamander_lizardfolk", "will_o_wisp_undead", "greater_devil", "lesser_devil",
                 "wyrm_dragon" -> true;
            default -> false;
        };
    }

    /** A family's own conditions on top of the usual spawn rules (the full moon, lava nearby, a village...). */
    private static boolean familyRule(GirlSpecies s, ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        return switch (s.family()) {
            case JACKALOPE -> Jackalope.spawnRule(s, level, pos, random);
            case ELF -> Elf.spawnRule(s, level, pos, random);
            case UNDEAD -> Undead.spawnRule(s, level, pos, random);
            default -> true;
        };
    }

    /** Forces the static registration above (called from the mod constructor before the registers are attached). */
    public static void init() {
    }

    @javax.annotation.Nullable
    public static GirlSpecies speciesOf(EntityType<?> type) {
        for (var e : TYPES.entrySet()) if (e.getValue().get() == type) return e.getKey();
        return null;
    }

    public static EntityType<AbstractMonsterGirl> type(GirlSpecies s) {
        return TYPES.get(s).get();
    }

    private static AbstractMonsterGirl create(EntityType<AbstractMonsterGirl> type, net.minecraft.world.level.Level level, GirlSpecies s) {
        return switch (s.family()) {
            case NEKOMATA -> new Nekomata(type, level, s);
            case KITSUNE -> new Kitsune(type, level, s);
            case LAMIA -> new Lamia(type, level, s);
            case ARACHNE -> new Arachne(type, level, s);
            case CENTAUR -> new Centaur(type, level, s);
            case MERFOLK -> new Merfolk(type, level, s);
            case SLIME -> new SlimeGirl(type, level, s);
            case JACKALOPE -> new Jackalope(type, level, s);
            case MINOTAUR -> new Minotaur(type, level, s);
            case MOLLUSK -> new Mollusk(type, level, s);
            case ELF -> new Elf(type, level, s);
            case UNDEAD -> new Undead(type, level, s);
            case LIZARDFOLK -> new Lizardfolk(type, level, s);
            case DRAGON -> new Dragon(type, level, s);
            case DEVIL -> new Devil(type, level, s);
            case BUG -> new Bug(type, level, s);
            case FLORA -> new Flora(type, level, s);
            case CANID -> new Canid(type, level, s);
            case RODENT -> new Rodent(type, level, s);
            case CRUSTACEAN -> new Crustacean(type, level, s);
            case MARSUPIAL -> new Marsupial(type, level, s);
            case ARMOURED -> new Armoured(type, level, s);
            case CONSTRUCT -> new Construct(type, level, s);
            case BAT -> new BatGirl(type, level, s);
            case THEROPOD -> new Theropod(type, level, s);
            case RIDER -> new Rider(type, level, s);
        };
    }

    private static AttributeSupplier.Builder attributes(GirlSpecies s) {
        return switch (s.family()) {
            case NEKOMATA -> Nekomata.createAttributes(s);
            case KITSUNE -> Kitsune.createAttributes(s);
            case LAMIA -> Lamia.createAttributes(s);
            case ARACHNE -> Arachne.createAttributes(s);
            case CENTAUR -> Centaur.createAttributes(s);
            case MERFOLK -> Merfolk.createAttributes(s);
            case SLIME -> SlimeGirl.createAttributes(s);
            case JACKALOPE -> Jackalope.createAttributes(s);
            case MINOTAUR -> Minotaur.createAttributes(s);
            case MOLLUSK -> Mollusk.createAttributes(s);
            case ELF -> Elf.createAttributes(s);
            case UNDEAD -> Undead.createAttributes(s);
            case LIZARDFOLK -> Lizardfolk.createAttributes(s);
            case DRAGON -> Dragon.createAttributes(s);
            case DEVIL -> Devil.createAttributes(s);
            case BUG -> Bug.createAttributes(s);
            case FLORA -> Flora.createAttributes(s);
            case CANID -> Canid.createAttributes(s);
            case RODENT -> Rodent.createAttributes(s);
            case CRUSTACEAN -> Crustacean.createAttributes(s);
            case MARSUPIAL -> Marsupial.createAttributes(s);
            case ARMOURED -> Armoured.createAttributes(s);
            case CONSTRUCT -> Construct.createAttributes(s);
            case BAT -> BatGirl.createAttributes(s);
            case THEROPOD -> Theropod.createAttributes(s);
            case RIDER -> Rider.createAttributes(s);
        };
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        for (GirlSpecies s : GirlSpecies.values()) event.put(type(s), attributes(s).build());
    }

    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        for (GirlSpecies s : GirlSpecies.values()) {
            boolean water = s.category() == net.minecraft.world.entity.MobCategory.WATER_CREATURE || s.category() == net.minecraft.world.entity.MobCategory.UNDERGROUND_WATER_CREATURE;
            event.register(type(s), water ? SpawnPlacementTypes.IN_WATER : SpawnPlacementTypes.ON_GROUND,
                    water ? Heightmap.Types.OCEAN_FLOOR : Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    water ? ModGirls::checkWaterSpawn : ModGirls::checkLandSpawn, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        }
    }

    /** Land girls: any solid ground, and only one natural spawn attempt in three succeeds (rare encounters matter). */
    private static boolean checkLandSpawn(EntityType<AbstractMonsterGirl> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (!Mob.checkMobSpawnRules(type, level, spawnType, pos, random)) return false;
        GirlSpecies s = speciesOf(type);
        if (spawnType != MobSpawnType.NATURAL) return true;
        if (s != null && s.category() == net.minecraft.world.entity.MobCategory.MONSTER
                && (level.getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL || !net.minecraft.world.entity.monster.Monster.isDarkEnoughToSpawn(level, pos, random))) {
            return false;                                       // the night walkers need the dark, like any monster
        }
        return random.nextInt(3) == 0 && (s == null || familyRule(s, level, pos, random));
    }

    private static boolean checkWaterSpawn(EntityType<AbstractMonsterGirl> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos.above()).is(FluidTags.WATER)
                && (spawnType != MobSpawnType.NATURAL || random.nextInt(3) == 0);
    }
}
