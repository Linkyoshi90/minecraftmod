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
 * tools/gen_bullywug.py and tools/gen_cane_toad.py) and says how a variant is chosen for a given biome. The colour is
 * rolled once per individual (spawn, hatching) and saved by name; it never changes behaviour. Species without an entry
 * here have no variants.
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

    private static final Map<String, Set> SETS = Map.of(
            "bullywug", new Set(BULLYWUG, AnuranVariants::bullywug),
            "cane_toad", new Set(CANE_TOAD, AnuranVariants::caneToad));

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
