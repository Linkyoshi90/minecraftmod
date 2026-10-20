package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.AnuranSpawnBlock;
import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
import com.linkyoshi90.monstergirls.entity.AnuranVariants;
import com.linkyoshi90.monstergirls.entity.Bullfrog;
import com.linkyoshi90.monstergirls.entity.Bullywug;
import com.linkyoshi90.monstergirls.entity.CaneToad;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;

/** Colour variants of the bullywug and the cane toad: chosen to suit the biome, saved, and each with a texture. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class AnuranVariantTests {

    private static Holder<Biome> biome(GameTestHelper helper, ResourceKey<Biome> key) {
        return helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(key);
    }

    /** The names of every variant a species produces in a biome over many rolls. */
    private static Set<String> rolls(GameTestHelper helper, String species, ResourceKey<Biome> key) {
        AnuranVariants.Set set = AnuranVariants.forSpecies(species);
        check(set != null, species + " should have variants");
        Holder<Biome> holder = biome(helper, key);
        Set<String> seen = new HashSet<>();
        RandomSource random = RandomSource.create(7);
        for (int i = 0; i < 600; i++) seen.add(set.nameAt(set.pick(holder, random)));
        return seen;
    }

    private static void only(Set<String> seen, String main, Set<String> never, String what) {
        check(seen.contains(main), what + ": expected " + main + " in " + seen);
        for (String n : never) check(!seen.contains(n), what + ": did not expect " + n + " in " + seen);
    }

    @GameTest(template = PLATFORM, batch = "bullywugColoursSuitTheirBiomes")
    public static void bullywugColoursSuitTheirBiomes(GameTestHelper helper) {
        Set<String> biomeOnly = Set.of("golden", "leopard", "marsh");
        only(rolls(helper, "bullywug", Biomes.MANGROVE_SWAMP), "golden", Set.of("leopard", "marsh", "green"), "mangrove swamp");
        only(rolls(helper, "bullywug", Biomes.JUNGLE), "golden", Set.of("leopard", "marsh", "green"), "jungle");
        only(rolls(helper, "bullywug", Biomes.SAVANNA), "golden", Set.of("leopard", "marsh", "green"), "savanna");
        only(rolls(helper, "bullywug", Biomes.PLAINS), "leopard", Set.of("golden", "marsh", "green"), "plains");
        only(rolls(helper, "bullywug", Biomes.MEADOW), "leopard", Set.of("golden", "marsh", "green"), "meadow");
        only(rolls(helper, "bullywug", Biomes.DARK_FOREST), "marsh", Set.of("golden", "leopard", "green"), "dark forest");
        Set<String> swamp = rolls(helper, "bullywug", Biomes.SWAMP);
        check(swamp.contains("green") && swamp.contains("marsh") && !swamp.contains("golden") && !swamp.contains("leopard"), "swamp: green and marsh: " + swamp);
        only(rolls(helper, "bullywug", Biomes.DESERT), "green", biomeOnly, "everywhere else the default green");
        // the rare morphs turn up anywhere
        Set<String> plains = rolls(helper, "bullywug", Biomes.PLAINS);
        check(plains.contains("blue") && plains.contains("albino"), "blue and albino should be rare rolls anywhere: " + plains);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, batch = "caneToadColoursSuitTheirBiomes")
    public static void caneToadColoursSuitTheirBiomes(GameTestHelper helper) {
        Set<String> biomeOnly = Set.of("olive", "rust", "grey", "sand");
        only(rolls(helper, "cane_toad", Biomes.JUNGLE), "olive", Set.of("rust", "grey", "sand", "cane"), "jungle");
        only(rolls(helper, "cane_toad", Biomes.DARK_FOREST), "olive", Set.of("rust", "grey", "sand", "cane"), "dark forest");
        only(rolls(helper, "cane_toad", Biomes.SAVANNA), "rust", Set.of("olive", "grey", "sand", "cane"), "savanna");
        only(rolls(helper, "cane_toad", Biomes.BADLANDS), "rust", Set.of("olive", "grey", "sand", "cane"), "badlands");
        only(rolls(helper, "cane_toad", Biomes.JAGGED_PEAKS), "grey", Set.of("olive", "rust", "sand", "cane"), "mountains");
        only(rolls(helper, "cane_toad", Biomes.STONY_SHORE), "grey", Set.of("olive", "rust", "sand", "cane"), "stony shore");
        only(rolls(helper, "cane_toad", Biomes.DESERT), "sand", Set.of("olive", "rust", "grey", "cane"), "desert");
        only(rolls(helper, "cane_toad", Biomes.SWAMP), "cane", biomeOnly, "swamp");
        only(rolls(helper, "cane_toad", Biomes.PLAINS), "cane", biomeOnly, "plains");
        check(rolls(helper, "cane_toad", Biomes.PLAINS).contains("black"), "black should be a rare roll anywhere");
        helper.succeed();
    }

    private static <T extends AbstractAnuran> T roundTrip(GameTestHelper helper, net.minecraft.world.entity.EntityType<T> type, T source) {
        CompoundTag tag = new CompoundTag();
        source.saveWithoutId(tag);
        T copy = type.create(helper.getLevel());
        copy.load(tag);
        return copy;
    }

    @GameTest(template = PLATFORM, batch = "anuranVariantsSurviveSaving")
    public static void anuranVariantsSurviveSaving(GameTestHelper helper) {
        Bullywug wug = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(3, 2, 3));
        wug.setVariant("leopard");
        Bullywug wugCopy = roundTrip(helper, ModEntities.BULLYWUG.get(), wug);
        check(wugCopy.getVariantName().equals("leopard") && wugCopy.getVariantSuffix().equals("_leopard"), "bullywug variant lost: " + wugCopy.getVariantName());
        wug.setVariant("green");
        check(wug.getVariantSuffix().isEmpty(), "the default green has no texture suffix");

        CaneToad toad = helper.spawn(ModEntities.CANE_TOAD.get(), new BlockPos(8, 2, 8));
        toad.setVariant("black");
        CaneToad toadCopy = roundTrip(helper, ModEntities.CANE_TOAD.get(), toad);
        check(toadCopy.getVariantName().equals("black") && toadCopy.getVariantSuffix().equals("_black"), "cane toad variant lost: " + toadCopy.getVariantName());

        // an unknown name falls back to the default; every species keeps its own list
        wug.setVariant("purple");
        check(wug.getVariantName().equals("green"), "unknown variant names should fall back to the default");
        Bullfrog bullfrog = helper.spawn(ModEntities.BULLFROG.get(), new BlockPos(12, 2, 12));
        bullfrog.setVariant("leopard");
        check(bullfrog.getVariantName().equals("olive") && bullfrog.getVariantSuffix().isEmpty(), "a bullywug colour is not a bullfrog colour");
        bullfrog.setVariant("golden");
        Bullfrog bullfrogCopy = roundTrip(helper, ModEntities.BULLFROG.get(), bullfrog);
        check(bullfrogCopy.getVariantSuffix().equals("_golden"), "bullfrog variant lost: " + bullfrogCopy.getVariantName());
        helper.succeed();
    }

    /** Exactly what /summon does: load the entity from NBT, then run spawn setup (finalizeSpawn) on it. */
    private static <T extends Entity> T summon(GameTestHelper helper, Class<T> type, String id, BlockPos rel, String nbt) {
        Vec3 pos = helper.absoluteVec(Vec3.atBottomCenterOf(rel));
        var source = helper.getLevel().getServer().createCommandSourceStack().withLevel(helper.getLevel());
        try {
            var holder = BuiltInRegistries.ENTITY_TYPE.getHolderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, MonsterGirlsMod.id(id)));
            Entity entity = SummonCommand.createEntity(source, holder, pos, nbt.isEmpty() ? new CompoundTag() : TagParser.parseTag(nbt), true);
            check(type.isInstance(entity), "summoned the wrong thing: " + entity);
            return type.cast(entity);
        } catch (CommandSyntaxException e) {
            throw new GameTestAssertException("summon of " + id + " failed: " + e.getMessage());
        }
    }

    @GameTest(template = PLATFORM, batch = "summonedAnuranColoursStick")
    public static void summonedAnuranColoursStick(GameTestHelper helper) {
        // several times: a re-roll from the biome would only sometimes land on the same colour by chance
        for (int i = 0; i < 6; i++) {
            check(summon(helper, Bullywug.class, "bullywug", new BlockPos(2, 2, 2 + i * 2), "{Variant:\"golden\"}").getVariantName().equals("golden"),
                    "a summoned bullywug lost her colour");
            check(summon(helper, CaneToad.class, "cane_toad", new BlockPos(6, 2, 2 + i * 2), "{Variant:\"rust\"}").getVariantName().equals("rust"),
                    "a summoned cane toad lost her colour");
        }
        // without NBT the biome still decides (a flat plains-like world: never a golden bullywug or a rust cane toad)
        for (int i = 0; i < 6; i++) {
            Bullywug plain = summon(helper, Bullywug.class, "bullywug", new BlockPos(10, 2, 2 + i * 2), "");
            check(!plain.getVariantName().equals("golden"), "a plain summon should still pick by biome");
        }
        helper.succeed();
    }

    private static boolean textureExists(String file) {
        return MonsterGirlsMod.class.getResource("/assets/monstergirls/textures/entity/" + file + ".png") != null;
    }

    @GameTest(template = PLATFORM, batch = "everyAnuranVariantHasATexture")
    public static void everyAnuranVariantHasATexture(GameTestHelper helper) {
        for (var entry : List.of(java.util.Map.entry("bullywug", AnuranVariants.BULLYWUG), java.util.Map.entry("cane_toad", AnuranVariants.CANE_TOAD),
                java.util.Map.entry("bullfrog", AnuranVariants.BULLFROG), java.util.Map.entry("tree_frog", AnuranVariants.TREE_FROG),
                java.util.Map.entry("poison_dart_frog", AnuranVariants.POISON_DART_FROG), java.util.Map.entry("glass_frog", AnuranVariants.GLASS_FROG),
                java.util.Map.entry("goliath_frog", AnuranVariants.GOLIATH_FROG), java.util.Map.entry("desert_rain_frog", AnuranVariants.DESERT_RAIN_FROG),
                java.util.Map.entry("mucus_toad", AnuranVariants.MUCUS_TOAD), java.util.Map.entry("fire_bellied_toad", AnuranVariants.FIRE_BELLIED_TOAD),
                java.util.Map.entry("natterjack", AnuranVariants.NATTERJACK), java.util.Map.entry("surinam_toad", AnuranVariants.SURINAM_TOAD))) {
            check(entry.getValue().size() == 5 || entry.getValue().size() == 6, entry.getKey() + " should have at least five colours");
            for (int i = 0; i < entry.getValue().size(); i++) {
                String suffix = i == 0 ? "" : "_" + entry.getValue().get(i);
                check(textureExists(entry.getKey() + suffix), "missing texture " + entry.getKey() + suffix + ".png");
            }
            check(AnuranVariants.forSpecies(entry.getKey()).names().equals(entry.getValue()), "variant list mismatch for " + entry.getKey());
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM, batch = "hatchedTadpolesGetAColour")
    public static void hatchedTadpolesGetAColour(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(5, 2, 5));
        BlockState spawn = ModBlocks.ANURAN_SPAWN.get().defaultBlockState().setValue(AnuranSpawnBlock.SPECIES, AnuranSpawnBlock.Species.BULLYWUG);
        helper.getLevel().setBlock(pos, spawn, 3);
        AnuranSpawnBlock.hatch(helper.getLevel(), pos, spawn);
        List<Bullywug> young = helper.getLevel().getEntitiesOfClass(Bullywug.class, new AABB(pos).inflate(3));
        check(young.size() >= 2, "a clutch hatches into several tadpoles: " + young.size());
        for (Bullywug tadpole : young) {
            check(AnuranVariants.BULLYWUG.contains(tadpole.getVariantName()), "a tadpole has no colour: " + tadpole.getVariantName());
        }
        helper.succeed();
    }
}
