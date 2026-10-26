package com.linkyoshi90.monstergirls.entity;

import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Texture-only colour variants of the frog and toad species. A species with variants lists its variant names (index 0
 * is the default and has no texture suffix: "bullywug.png"; the others are "bullywug_golden.png" and so on, written by
 * tools/gen_bullywug.py, tools/gen_cane_toad.py and, for every other species, tools/gen_anuran_variants.py) and says how
 * a variant is chosen for a given biome. The colour is rolled once per individual (spawn, hatching) and saved by name;
 * it never changes behaviour. Every frog and toad species has five colours.
 */
public final class AnuranVariants {
    /** One species' variants: their names (default first) and how a biome (plus the rare rolls) picks an index. */
    public record Set(List<String> names, BiFunction<Holder<Biome>, RandomSource, Integer> picker) {
        public int pick(Holder<Biome> biome, RandomSource random) {
            return this.picker.apply(biome, random);
        }

        /** The index of a saved name; unknown names fall back to the default. */
        public int indexOf(String name) {
            int index = this.names.indexOf(name);
            return Math.max(index, 0);
        }

        public String nameAt(int index) {
            return index >= 0 && index < this.names.size() ? this.names.get(index) : this.names.get(0);
        }
    }

    public static final List<String> BULLYWUG = List.of("green", "golden", "blue", "leopard", "marsh", "albino");
    public static final List<String> CANE_TOAD = List.of("cane", "olive", "rust", "grey", "sand", "black");

    /** One bullywug in this many is the rare blue (axanthic) morph, one in this many the albino, wherever they spawn. */
    public static final int BLUE_RARITY = 25;
    public static final int ALBINO_RARITY = 40;
    /** One cane toad in this many is the rare black one. */
    public static final int BLACK_RARITY = 30;

    public static final List<String> BULLFROG = List.of("olive", "golden", "blue", "dark", "albino");
    public static final List<String> TREE_FROG = List.of("green", "grey", "lime", "blue", "brown");
    public static final List<String> POISON_DART_FROG = List.of("azure", "strawberry", "golden", "green", "orange");
    public static final List<String> GLASS_FROG = List.of("lime", "emerald", "yellow", "blue", "spotted");
    public static final List<String> GOLIATH_FROG = List.of("olive", "dark", "green", "rust", "grey");
    public static final List<String> DESERT_RAIN_FROG = List.of("sand", "pale", "grey", "rufous", "dark");
    public static final List<String> MUCUS_TOAD = List.of("brown", "olive", "red", "grey", "blue");
    public static final List<String> FIRE_BELLIED_TOAD = List.of("oriental", "yellow", "european", "giant", "blue");
    public static final List<String> NATTERJACK = List.of("olive", "green", "brown", "red", "grey");
    public static final List<String> SURINAM_TOAD = List.of("brown", "dark", "olive", "pale", "rust");
    public static final List<String> VODYANOY = List.of("river", "bog", "old", "lake", "pale");
    public static final List<String> MAILTOAD = List.of("post", "teal", "green", "grey", "gold");

    /** One in this many of the newer species is the rare colour (the last one listed below), wherever it spawns. */
    public static final int RARE = 30;

    private static final Map<String, Set> SETS = Map.ofEntries(
            Map.entry("bullywug", new Set(BULLYWUG, AnuranVariants::bullywug)),
            Map.entry("cane_toad", new Set(CANE_TOAD, AnuranVariants::caneToad)),
            Map.entry("bullfrog", new Set(BULLFROG, common(BULLFROG, "albino"))),
            Map.entry("tree_frog", new Set(TREE_FROG, (b, r) -> b.is(Tags.Biomes.IS_TAIGA) || b.is(Biomes.DARK_FOREST)
                    ? TREE_FROG.indexOf("grey") : common(TREE_FROG, "blue").apply(b, r))),
            Map.entry("poison_dart_frog", new Set(POISON_DART_FROG, common(POISON_DART_FROG, "golden"))),
            Map.entry("glass_frog", new Set(GLASS_FROG, common(GLASS_FROG, "blue"))),
            Map.entry("goliath_frog", new Set(GOLIATH_FROG, common(GOLIATH_FROG, "grey"))),
            Map.entry("desert_rain_frog", new Set(DESERT_RAIN_FROG, (b, r) -> b.is(Tags.Biomes.IS_DESERT) && r.nextInt(3) == 0
                    ? DESERT_RAIN_FROG.indexOf("pale") : common(DESERT_RAIN_FROG, "dark").apply(b, r))),
            Map.entry("mucus_toad", new Set(MUCUS_TOAD, common(MUCUS_TOAD, "blue"))),
            Map.entry("fire_bellied_toad", new Set(FIRE_BELLIED_TOAD, (b, r) -> b.is(Tags.Biomes.IS_MOUNTAIN) && r.nextInt(2) == 0
                    ? FIRE_BELLIED_TOAD.indexOf("yellow") : common(FIRE_BELLIED_TOAD, "blue").apply(b, r))),
            Map.entry("natterjack", new Set(NATTERJACK, common(NATTERJACK, "red"))),
            Map.entry("mailtoad", new Set(MAILTOAD, common(MAILTOAD, "gold"))),
            Map.entry("surinam_toad", new Set(SURINAM_TOAD, common(SURINAM_TOAD, "pale"))),
            Map.entry("vodyanoy", new Set(VODYANOY, common(VODYANOY, "pale"))));

    /**
     * The newer species' rule: the rare colour one time in {@link #RARE}; otherwise the default half the time and the
     * other three sharing the rest.
     */
    static BiFunction<Holder<Biome>, RandomSource, Integer> common(List<String> names, String rare) {
        int rareIndex = names.indexOf(rare);
        return (biome, random) -> {
            if (random.nextInt(RARE) == 0) return rareIndex;
            if (random.nextBoolean()) return 0;
            int i;
            do {
                i = 1 + random.nextInt(names.size() - 1);
            } while (i == rareIndex);
            return i;
        };
    }

    private AnuranVariants() {
    }

    /** The variants of a species (the id as in the entity registry, e.g. "bullywug"), or null if it has none. */
    @Nullable
    public static Set forSpecies(String species) {
        return SETS.get(species);
    }

    /**
     * Green is the swamp and river default. Mangrove swamps, jungles and savannas are golden; plains, meadows and ordinary
     * woods are leopard; half of the swamps and all dark forests are the dark marsh frogs. Blue and albino are rare rolls
     * anywhere.
     */
    static int bullywug(Holder<Biome> biome, RandomSource random) {
        if (random.nextInt(ALBINO_RARITY) == 0) return BULLYWUG.indexOf("albino");
        if (random.nextInt(BLUE_RARITY) == 0) return BULLYWUG.indexOf("blue");
        String name;
        if (biome.is(Biomes.MANGROVE_SWAMP) || biome.is(Tags.Biomes.IS_JUNGLE) || biome.is(Tags.Biomes.IS_SAVANNA)) name = "golden";
        else if (biome.is(Biomes.DARK_FOREST)) name = "marsh";
        else if (biome.is(Tags.Biomes.IS_SWAMP)) name = random.nextBoolean() ? "marsh" : "green";
        else if (biome.is(Tags.Biomes.IS_PLAINS) || biome.is(Biomes.MEADOW) || biome.is(Tags.Biomes.IS_FOREST)) name = "leopard";
        else name = "green";
        return BULLYWUG.indexOf(name);
    }

    /**
     * Brown "cane" is the swamp and farmland default. Jungles and wet woods are olive, savannas and badlands rust, mountains
     * and stony shores grey, deserts sand; black is a rare roll anywhere.
     */
    static int caneToad(Holder<Biome> biome, RandomSource random) {
        if (random.nextInt(BLACK_RARITY) == 0) return CANE_TOAD.indexOf("black");
        String name;
        if (biome.is(Tags.Biomes.IS_JUNGLE) || biome.is(Biomes.DARK_FOREST) || biome.is(Biomes.OLD_GROWTH_BIRCH_FOREST)) name = "olive";
        else if (biome.is(Tags.Biomes.IS_SAVANNA) || biome.is(Tags.Biomes.IS_BADLANDS)) name = "rust";
        else if (biome.is(Tags.Biomes.IS_MOUNTAIN) || biome.is(Biomes.STONY_SHORE)) name = "grey";
        else if (biome.is(Tags.Biomes.IS_DESERT) || biome.is(Tags.Biomes.IS_SANDY)) name = "sand";
        else name = "cane";
        return CANE_TOAD.indexOf(name);
    }
}
