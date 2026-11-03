package com.linkyoshi90.monstergirls;

import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/** Options shared by client and server (config/monstergirls-common.toml, or Mods > MonsterGirls > Config in the game). */
public final class MonsterGirlsConfig {
    /** One wild one in this many is painted, at a paint rate of 100%. */
    public static final int PAINT_RARITY = 64;
    /** Rates run from 0 to this percent. NeoForge's config screen draws integer ranges under 256 as sliders. */
    private static final int MAX_RATE = 200;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue WILD_SPAWN_CHANCE = BUILDER
            .comment("How often a natural spawn of a girl goes ahead once the game has picked her (0 to 1).",
                    "Most species have their own one-in-three check as well.")
            .defineInRange("wildSpawnChance", 0.375, 0.0, 1.0);

    public static final ModConfigSpec.IntValue WILD_SPACING = BUILDER
            .comment("No girl spawns naturally within this many blocks of another wild one (0 turns this off).",
                    "Friends don't count, and neither do the rest of her own group.")
            .defineInRange("wildSpacing", 128, 0, 512);

    public static final ModConfigSpec.IntValue SPAWN_RATE = BUILDER
            .comment("Natural spawns of every family, as a percent of normal. Multiplies wildSpawnChance;",
                    "wildSpacing still limits how many can be around at once.")
            .defineInRange("spawnRate", 100, 0, MAX_RATE);

    public static final ModConfigSpec.IntValue VARIANT_RATE = BUILDER
            .comment("How often a wild one has a colour other than the default, as a percent of normal.",
                    "Harpies and anurans take their colours from the biome, so above 100 changes nothing for them.")
            .defineInRange("variantRate", 100, 0, MAX_RATE);

    public static final ModConfigSpec.IntValue MISSING_RATE = BUILDER
            .comment("How quickly friends start to miss a player they haven't seen, in percent (0 turns it off, 200 is twice as soon).")
            .defineInRange("missingRate", 100, 0, MAX_RATE);
    public static final ModConfigSpec.IntValue CONCEPTION_CHANCE = BUILDER
            .comment("The chance of young after a shared night, in percent. One who missed you beats it; a full moon is always certain.")
            .defineInRange("conceptionChance", 50, 0, 100);
    public static final ModConfigSpec.IntValue PAINT_RATE = BUILDER
            .comment("How often a wild one is painted, as a percent of normal (normal is one in " + PAINT_RARITY + ").")
            .defineInRange("paintRate", 100, 0, MAX_RATE);

    private static final ModConfigSpec.IntValue HARPY_RATE;
    private static final ModConfigSpec.IntValue ANURAN_RATE;
    private static final Map<GirlSpecies.Family, ModConfigSpec.IntValue> FAMILY_RATES = new EnumMap<>(GirlSpecies.Family.class);

    public static final ModConfigSpec.BooleanValue MILK_DRIPS;
    public static final ModConfigSpec.BooleanValue FLIRTING;
    public static final ModConfigSpec.BooleanValue MILK_EMBARRASSED;
    public static final ModConfigSpec.BooleanValue MILK_HARNESS;
    public static final ModConfigSpec.BooleanValue MILK_DISCOMFORT;
    public static final ModConfigSpec.BooleanValue RECIPE_FLAVOURS;

    static {
        BUILDER.comment("Natural spawns of one family, as a percent of normal, on top of spawnRate.").push("familySpawnRates");
        HARPY_RATE = BUILDER.defineInRange("harpy", 100, 0, MAX_RATE);
        ANURAN_RATE = BUILDER.defineInRange("anuran", 100, 0, MAX_RATE);
        for (GirlSpecies.Family family : GirlSpecies.Family.values()) {
            FAMILY_RATES.put(family, BUILDER.defineInRange(family.name().toLowerCase(Locale.ROOT), 100, 0, MAX_RATE));
        }
        BUILDER.pop();

        FLIRTING = BUILDER
                .comment("Close friends now and then answer a pat with a teasing line and a few hearts.")
                .define("flirting", true);

        BUILDER.push("milking");
        MILK_DRIPS = BUILDER
                .comment("A minotaur cow whose milk is ready again, and anyone in the last stage of expecting, drips from her chest until she is milked.")
                .define("milkDrips", false);
        MILK_EMBARRASSED = BUILDER
                .comment("A minotaur cow looks away while she is being milked, and blushes at a bucket. Everyone blushes at the machine regardless.")
                .define("milkEmbarrassed", false);
        MILK_HARNESS = BUILDER
                .comment("The milking machine becomes a pump cart, and a minotaur cow wears its harness and two pumps while it works.")
                .define("milkHarness", false);
        MILK_DISCOMFORT = BUILDER
                .comment("Left unmilked for ten minutes, she grows uncomfortably full: slower, a little grumpy, and she asks her friends for the machine. Milking her is a relief.")
                .define("milkDiscomfort", true);
        RECIPE_FLAVOURS = BUILDER
                .comment("A dish made from a flavouring food (any mod's recipe: a berry pie, a honey cake) flavours her milk as that food does, one flavour per dish.")
                .define("recipeFlavours", true);
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    private MonsterGirlsConfig() {}

    /** Safe to call before the config has loaded (falls back to the default). */
    public static double wildSpawnChance() {
        return SPEC.isLoaded() ? WILD_SPAWN_CHANCE.get() : WILD_SPAWN_CHANCE.getDefault();
    }

    public static int wildSpacing() {
        return SPEC.isLoaded() ? WILD_SPACING.get() : WILD_SPACING.getDefault();
    }

    private static double rate(ModConfigSpec.IntValue value) {
        return (SPEC.isLoaded() ? value.get() : value.getDefault()) / 100.0;
    }

    /** The spawn rate for this mob's family, with the overall one: 1 is normal. */
    public static double spawnRate(Mob mob) {
        double family = 1.0;
        if (mob instanceof AbstractMonsterGirl girl) family = rate(FAMILY_RATES.get(girl.getSpecies().family()));
        else if (mob instanceof AbstractHarpy) family = rate(HARPY_RATE);
        else if (mob instanceof AbstractAnuran) family = rate(ANURAN_RATE);
        return rate(SPAWN_RATE) * family;
    }

    /** The chance a wild one is painted. */
    public static double paintChance() {
        return rate(PAINT_RATE) / PAINT_RARITY;
    }

    /** Whether a wild one painted by chance {@link #paintChance()}. */
    public static boolean rollsPaint(RandomSource random) {
        return random.nextDouble() < paintChance();
    }

    /** The chance of something other than the default colours, where that is normally the given chance. */
    public static double variantChance(double normal) {
        return Math.min(1.0, normal * rate(VARIANT_RATE));
    }

    /** Whether a colour the biome chose stays, for the families that have no other say. */
    public static boolean keepsBiomeVariant(RandomSource random) {
        return random.nextDouble() < rate(VARIANT_RATE);
    }

    public static int missingRate() {
        return SPEC.isLoaded() ? MISSING_RATE.get() : MISSING_RATE.getDefault();
    }

    public static int conceptionChance() {
        return SPEC.isLoaded() ? CONCEPTION_CHANCE.get() : CONCEPTION_CHANCE.getDefault();
    }

    public static boolean flirting() {
        return SPEC.isLoaded() ? FLIRTING.get() : FLIRTING.getDefault();
    }

    public static boolean milkDrips() {
        return SPEC.isLoaded() ? MILK_DRIPS.get() : MILK_DRIPS.getDefault();
    }

    public static boolean milkEmbarrassed() {
        return SPEC.isLoaded() ? MILK_EMBARRASSED.get() : MILK_EMBARRASSED.getDefault();
    }

    public static boolean milkDiscomfort() {
        return SPEC.isLoaded() ? MILK_DISCOMFORT.get() : MILK_DISCOMFORT.getDefault();
    }

    public static boolean recipeFlavours() {
        return SPEC.isLoaded() ? RECIPE_FLAVOURS.get() : RECIPE_FLAVOURS.getDefault();
    }

    public static boolean milkHarness() {
        return SPEC.isLoaded() ? MILK_HARNESS.get() : MILK_HARNESS.getDefault();
    }
}
