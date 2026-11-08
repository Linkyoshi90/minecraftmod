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

    public static final ModConfigSpec.IntValue RARE_SPAWN_RATE = BUILDER
            .comment("Natural spawns of the rare kinds (the storied ones: a unicorn, a dullahan), as a percent of a common kind's.")
            .defineInRange("rareSpawnRate", 40, 0, 100);
    public static final ModConfigSpec.IntValue LEGENDARY_SPAWN_RATE = BUILDER
            .comment("Natural spawns of the legendary kinds (the one-of-a-kind figures of myth: Bahamut, the Phoenix), as a percent of a common kind's.")
            .defineInRange("legendarySpawnRate", 10, 0, 100);
    public static final ModConfigSpec.BooleanValue LEGENDARY_ONCE_PER_REGION = BUILDER
            .comment("Only one of each legendary kind comes to every " + com.linkyoshi90.monstergirls.entity.Rarity.REGION + " blocks square, and she stays. One who dies unbefriended frees the region again.")
            .define("legendaryOncePerRegion", true);

    public static final ModConfigSpec.IntValue ENCOUNTER_RATE = BUILDER
            .comment("How often a wild one meets you in trouble (caught in a web, chased by zombies, lost in the rain), as a percent of normal",
                    "(normal is one in " + com.linkyoshi90.monstergirls.entity.Encounters.RARITY + "; 0 turns them off).")
            .defineInRange("encounterRate", 100, 0, MAX_RATE);

    public static final ModConfigSpec.BooleanValue SPAWN_CONDITIONS = BUILDER
            .comment("Some kinds come out only at their moment in the overworld: the moon rabbit under a full moon, the vampire at night,",
                    "the ziz in a storm, the kappa in the rain. Off, they come whenever.")
            .define("spawnConditions", true);

    public static final ModConfigSpec.BooleanValue CHORES = BUILDER
            .comment("Friends on patrol do the chores around their patrol spot: gather eggs and wool, take honey from full hives, shear sheep, fish.")
            .define("chores", true);

    public static final ModConfigSpec.BooleanValue FESTIVALS = BUILDER
            .comment("Three festivals in every " + com.linkyoshi90.monstergirls.entity.girl.Festivals.CYCLE + " days (a dance, a harvest feast, a gift exchange): the girls at your hearth gather round it at dusk.")
            .define("festivals", true);

    public static final ModConfigSpec.BooleanValue TRAVELLING_MARKET = BUILDER
            .comment("Every " + com.linkyoshi90.monstergirls.entity.girl.Market.EVERY + " days, when you are home, a travelling girl from far away sets up by your hearth and trades her finds for emeralds.")
            .define("travellingMarket", true);

    public static final ModConfigSpec.BooleanValue RIVALRIES = BUILDER
            .comment("Wild ones of rival kinds (cats and mice, wolves and rabbits, spiders and bugs...) squabble when they meet: one glowers, the other bolts. Nobody is hurt.")
            .define("rivalries", true);

    public static final ModConfigSpec.BooleanValue TEAMWORK = BUILDER
            .comment("Two of your friends fighting the same foe set each other up: a blow within three seconds of a teammate's lands half again as hard.")
            .define("teamwork", true);

    public static final ModConfigSpec.BooleanValue COMFORT = BUILDER
            .comment("Friends look out for you: the nearest rushes to steady you when you are badly hurt, shares a snack when you are hungry, and they warn you a minute before rain. Friends near say good morning (well rested), and timid ones huddle against you in thunderstorms.")
            .define("comfort", true);

    public static final ModConfigSpec.BooleanValue COLDS = BUILDER
            .comment("Girls left out in cold rain or snow can catch a cold: sneezing and a little slow for three days, until someone brings her soup or honey.")
            .define("colds", true);

    public static final ModConfigSpec.BooleanValue STARGAZING = BUILDER
            .comment("On a clear night, stand still under open sky looking up and your friends nearby sit down beside you; a shooting star brings each of them closer.")
            .define("stargazing", true);

    public static final ModConfigSpec.BooleanValue TRAVEL_TOGETHER = BUILDER
            .comment("Friends told to follow you who are within 16 blocks come along when you go through a portal.")
            .define("travelTogether", true);

    public static final ModConfigSpec.BooleanValue FRIEND_NOTICES = BUILDER
            .comment("Friends tell you how things stand: a warning when one is badly hurt, and a line when your friendship reaches a milestone.")
            .define("friendNotices", true);

    public static final ModConfigSpec.BooleanValue FRIENDS_FAINT = BUILDER
            .comment("A friend who would die faints instead: she keeps a quarter of her health, sits down and waits for you to help her up.",
                    "She can't faint twice within a minute, and /kill or the void still kill her.")
            .define("friendsFaint", false);

    public static final ModConfigSpec.IntValue HEARTH_SAFE_RADIUS = BUILDER
            .comment("No hostile mob spawns naturally within this many blocks of a hearth with " + com.linkyoshi90.monstergirls.block.HearthBlockEntity.SAFE_RESIDENTS + " or more living there (0 turns it off).")
            .defineInRange("hearthSafeRadius", 24, 0, 128);

    public static final ModConfigSpec.IntValue VARIANT_RATE = BUILDER
            .comment("How often a wild one has a colour other than the default, as a percent of normal.",
                    "Harpies and anurans take their colours from the biome, so above 100 changes nothing for them.")
            .defineInRange("variantRate", 100, 0, MAX_RATE);

    public static final ModConfigSpec.IntValue MISSING_RATE = BUILDER
            .comment("How quickly friends start to miss a player they haven't seen, in percent (0 turns it off, 200 is twice as soon).")
            .defineInRange("missingRate", 100, 0, MAX_RATE);
    public static final ModConfigSpec.BooleanValue MISSING_COUNTS_OFFLINE = BUILDER
            .comment("Friends miss a player for days the world went on without them (the server kept running while they were away).",
                    "Off, only days the player was online count.")
            .define("missingCountsOffline", false);
    public static final ModConfigSpec.IntValue CONCEPTION_CHANCE = BUILDER
            .comment("The chance of young after a shared night, in percent. One who missed you beats it; a full moon is always certain.")
            .defineInRange("conceptionChance", 50, 0, 100);
    public static final ModConfigSpec.IntValue PAINT_RATE = BUILDER
            .comment("How often a wild one is painted, as a percent of normal (normal is one in " + PAINT_RARITY + ").")
            .defineInRange("paintRate", 100, 0, MAX_RATE);

    /** One per clade (entity.Clade, TAXONOMY_PLAN.md); harpies are aves and anurans amphibia. */
    private static final Map<com.linkyoshi90.monstergirls.entity.Clade, ModConfigSpec.IntValue> FAMILY_RATES = new EnumMap<>(com.linkyoshi90.monstergirls.entity.Clade.class);

    public static final ModConfigSpec.BooleanValue MILK_DRIPS;
    public static final ModConfigSpec.BooleanValue FLIRTING;
    public static final ModConfigSpec.BooleanValue DEMON_REALM;
    public static final ModConfigSpec.BooleanValue REALM_FOG;
    public static final ModConfigSpec.BooleanValue LUCKY_MOMENTS;
    public static final ModConfigSpec.BooleanValue MONSTERIZATION;
    public static final ModConfigSpec.BooleanValue POSSESSION;
    public static final ModConfigSpec.BooleanValue REALM_SUMMONS;
    public static final ModConfigSpec.BooleanValue POSSESSION_SPREADS;
    public static final ModConfigSpec.BooleanValue ROSE_MOON;
    public static final ModConfigSpec.BooleanValue ORDER_PATROLS;
    public static final ModConfigSpec.BooleanValue HAMLETS;
    public static final ModConfigSpec.BooleanValue SWEETHEARTS;
    public static final ModConfigSpec.BooleanValue DATES;
    public static final ModConfigSpec.BooleanValue HOT_SPRINGS;
    public static final ModConfigSpec.BooleanValue PASSING_FLOCKS;
    public static final ModConfigSpec.BooleanValue REALM_TOUCHED;
    public static final ModConfigSpec.BooleanValue ORDER_RAIDS;
    public static final ModConfigSpec.BooleanValue DREAM_VISITS;
    public static final ModConfigSpec.BooleanValue MATING_SEASON;
    public static final ModConfigSpec.IntValue MAX_SPOUSES;
    public static final ModConfigSpec.BooleanValue MILK_EMBARRASSED;
    public static final ModConfigSpec.BooleanValue MILK_HARNESS;
    public static final ModConfigSpec.BooleanValue MILK_DISCOMFORT;
    public static final ModConfigSpec.BooleanValue RECIPE_FLAVOURS;

    static {
        BUILDER.comment("Natural spawns of one family, as a percent of normal, on top of spawnRate.").push("familySpawnRates");
        for (com.linkyoshi90.monstergirls.entity.Clade clade : com.linkyoshi90.monstergirls.entity.Clade.values()) {
            FAMILY_RATES.put(clade, BUILDER.defineInRange(clade.key(), 100, 0, MAX_RATE));
        }
        BUILDER.pop();

        FLIRTING = BUILDER
                .comment("Close friends now and then answer a pat with a teasing line and a few hearts.")
                .define("flirting", true);

        DEMON_REALM = BUILDER
                .comment("A wild Lilith grows Demon Realm grass around her, and it spreads in the dark until light or holy water drives it back. Off: no new realm grows or spreads.")
                .define("demonRealm", true);

        REALM_FOG = BUILDER
                .comment("Inside the Demon Realm a rose-violet fog closes in, haze drifts and distant sighs play (client side).")
                .define("realmFog", true);

        LUCKY_MOMENTS = BUILDER
                .comment("Slapstick moments: a bathing girl who catches you looking splashes you (a close friend just blushes), and friends beside you now and then trip into you.")
                .define("luckyMoments", true);

        MONSTERIZATION = BUILDER
                .comment("Grown vanilla animals and monsters that linger on Demon Realm grass in the dark turn into monster girls of their kind (a tamed pet stays her owner's friend).")
                .define("monsterization", true);

        POSSESSION = BUILDER
                .comment("Villagers, wandering traders, pigs, cats and chickens on Demon Realm grass in the dark are possessed: they keep their bodies, their eyes glow, and every half day their young with one of their own kind is a girl (succubi and demons, piglettes, nekomata, poultry harpies).")
                .define("possession", true);

        REALM_SUMMONS = BUILDER
                .comment("At night the heart of a Demon Realm now and then calls a wild succubus or demon out of the haze (never more than one near it).")
                .define("realmSummons", true);

        POSSESSION_SPREADS = BUILDER
                .comment("A possessed villager now and then passes it on, at night, to a villager standing close by, so a village slowly turns (holy water frees them).")
                .define("possessionSpreads", true);

        ROSE_MOON = BUILDER
                .comment("About one night in ten the moon rises rose-violet and the Demon Realm stirs: monsterization twice as fast, possession, summons and the realm's spread four times as often.")
                .define("roseMoon", true);

        ORDER_PATROLS = BUILDER
                .comment("By day, patrols of the Order (sister-knights and an inquisitrix) now and then march on a Demon Realm patch near a player to purge it, and come looking for players the Order has marked.")
                .define("orderPatrols", true);

        REALM_TOUCHED = BUILDER
                .comment("A player who keeps company with the Demon Realm slowly becomes kin to it: night vision in its dark, its own let them be, and in the end it feeds them and makes them strong at night. Holy water washes it out.")
                .define("realmTouched", true);

        HAMLETS = BUILDER
                .comment("A hearth with three residents becomes a named hamlet with its own banner; with six, a village, where now and then a wandering girl of a new family comes and asks to stay (up to twelve).")
                .define("hamlets", true);

        SWEETHEARTS = BUILDER
                .comment("A player's grown daughter can take a shine to a villager (or be introduced with a flower). After three mornings together they are wed, and now and then a little one comes along: the player's grandchild (two at most each).")
                .define("sweethearts", true);

        DATES = BUILDER
                .comment("Sneak and use a flower on a close friend to ask her out: she names her kind of place (flowers, the water's edge, a view, the night sky); spend half a minute there together for affection, Regeneration and Luck. One date a day each.")
                .define("dates", true);

        HOT_SPRINGS = BUILDER
                .comment("Hot springs: water over geyserite steams, never freezes, heals and warms whoever soaks in it (friends come and join you), and cures a girl's cold. Off stops the effect and new springs in new chunks.")
                .define("hotSprings", true);

        PASSING_FLOCKS = BUILDER
                .comment("Every eight days a flock of wild harpies of one migratory kind rests near each player under the open sky for a day, then flies on (any who has made a friend stays).")
                .define("passingFlocks", true);

        ORDER_RAIDS = BUILDER
                .comment("A player with strong enough ties to the Demon Realm draws a night crusade of the Order on their hearth.")
                .define("orderRaids", true);

        DREAM_VISITS = BUILDER
                .comment("Succubi visit your dreams: a close succubus friend brings a sweet dream (Luck in the morning); a wild one nearby feeds on some of your experience and leaves a heartfruit.")
                .define("dreamVisits", true);

        MATING_SEASON = BUILDER
                .comment("Each branch of the tree of life has three restless days in every forty: grown friends cling to you, blush, flirt more and come to bed more readily. Cold calms them.")
                .define("matingSeason", true);

        MAX_SPOUSES = BUILDER
                .comment("How many monster girls one player may marry (0: any number).")
                .defineInRange("maxSpouses", 1, 0, 64);

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

    /** The chance a wild one carries an encounter, where that is normally one in rarity. */
    public static double encounterChance(int rarity) {
        return rate(ENCOUNTER_RATE) / rarity;
    }

    public static boolean missingCountsOffline() {
        return SPEC.isLoaded() ? MISSING_COUNTS_OFFLINE.get() : MISSING_COUNTS_OFFLINE.getDefault();
    }

    public static boolean festivals() {
        return SPEC.isLoaded() ? FESTIVALS.get() : FESTIVALS.getDefault();
    }

    public static boolean travellingMarket() {
        return SPEC.isLoaded() ? TRAVELLING_MARKET.get() : TRAVELLING_MARKET.getDefault();
    }

    public static boolean rivalries() {
        return SPEC.isLoaded() ? RIVALRIES.get() : RIVALRIES.getDefault();
    }

    public static boolean teamwork() {
        return SPEC.isLoaded() ? TEAMWORK.get() : TEAMWORK.getDefault();
    }

    public static boolean colds() {
        return SPEC.isLoaded() ? COLDS.get() : COLDS.getDefault();
    }

    public static boolean travelTogether() {
        return SPEC.isLoaded() ? TRAVEL_TOGETHER.get() : TRAVEL_TOGETHER.getDefault();
    }

    public static boolean friendNotices() {
        return SPEC.isLoaded() ? FRIEND_NOTICES.get() : FRIEND_NOTICES.getDefault();
    }

    public static boolean friendsFaint() {
        return SPEC.isLoaded() ? FRIENDS_FAINT.get() : FRIENDS_FAINT.getDefault();
    }

    public static boolean stargazing() {
        return SPEC.isLoaded() ? STARGAZING.get() : STARGAZING.getDefault();
    }

    public static boolean comfort() {
        return SPEC.isLoaded() ? COMFORT.get() : COMFORT.getDefault();
    }

    public static int hearthSafeRadius() {
        return SPEC.isLoaded() ? HEARTH_SAFE_RADIUS.get() : HEARTH_SAFE_RADIUS.getDefault();
    }

    public static boolean chores() {
        return SPEC.isLoaded() ? CHORES.get() : CHORES.getDefault();
    }

    public static boolean spawnConditions() {
        return SPEC.isLoaded() ? SPAWN_CONDITIONS.get() : SPAWN_CONDITIONS.getDefault();
    }

    public static double rareSpawnRate() {
        return rate(RARE_SPAWN_RATE);
    }

    public static double legendarySpawnRate() {
        return rate(LEGENDARY_SPAWN_RATE);
    }

    public static boolean legendaryOncePerRegion() {
        return SPEC.isLoaded() ? LEGENDARY_ONCE_PER_REGION.get() : LEGENDARY_ONCE_PER_REGION.getDefault();
    }

    private static double rate(ModConfigSpec.IntValue value) {
        return (SPEC.isLoaded() ? value.get() : value.getDefault()) / 100.0;
    }

    /** The spawn rate for this mob's clade, with the overall one: 1 is normal. */
    public static double spawnRate(Mob mob) {
        com.linkyoshi90.monstergirls.entity.Clade clade = com.linkyoshi90.monstergirls.entity.Clade.of(mob);
        if (clade == null && mob instanceof AbstractHarpy) clade = com.linkyoshi90.monstergirls.entity.Clade.AVES;
        if (clade == null && mob instanceof AbstractAnuran) clade = com.linkyoshi90.monstergirls.entity.Clade.AMPHIBIA;
        double family = clade == null ? 1.0 : rate(FAMILY_RATES.get(clade));
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

    public static int maxSpouses() {
        return SPEC.isLoaded() ? MAX_SPOUSES.get() : MAX_SPOUSES.getDefault();
    }

    public static boolean matingSeason() {
        return SPEC.isLoaded() ? MATING_SEASON.get() : MATING_SEASON.getDefault();
    }

    public static boolean dreamVisits() {
        return SPEC.isLoaded() ? DREAM_VISITS.get() : DREAM_VISITS.getDefault();
    }

    public static boolean possession() {
        return SPEC.isLoaded() ? POSSESSION.get() : POSSESSION.getDefault();
    }

    public static boolean realmTouched() {
        return SPEC.isLoaded() ? REALM_TOUCHED.get() : REALM_TOUCHED.getDefault();
    }

    public static boolean passingFlocks() {
        return SPEC.isLoaded() ? PASSING_FLOCKS.get() : PASSING_FLOCKS.getDefault();
    }

    public static boolean sweethearts() {
        return SPEC.isLoaded() ? SWEETHEARTS.get() : SWEETHEARTS.getDefault();
    }

    public static boolean hotSprings() {
        return SPEC.isLoaded() ? HOT_SPRINGS.get() : HOT_SPRINGS.getDefault();
    }

    public static boolean dates() {
        return SPEC.isLoaded() ? DATES.get() : DATES.getDefault();
    }

    public static boolean hamlets() {
        return SPEC.isLoaded() ? HAMLETS.get() : HAMLETS.getDefault();
    }

    public static boolean orderPatrols() {
        return SPEC.isLoaded() ? ORDER_PATROLS.get() : ORDER_PATROLS.getDefault();
    }

    public static boolean orderRaids() {
        return SPEC.isLoaded() ? ORDER_RAIDS.get() : ORDER_RAIDS.getDefault();
    }

    public static boolean roseMoon() {
        return SPEC.isLoaded() ? ROSE_MOON.get() : ROSE_MOON.getDefault();
    }

    public static boolean possessionSpreads() {
        return SPEC.isLoaded() ? POSSESSION_SPREADS.get() : POSSESSION_SPREADS.getDefault();
    }

    public static boolean realmSummons() {
        return SPEC.isLoaded() ? REALM_SUMMONS.get() : REALM_SUMMONS.getDefault();
    }

    public static boolean monsterization() {
        return SPEC.isLoaded() ? MONSTERIZATION.get() : MONSTERIZATION.getDefault();
    }

    public static boolean luckyMoments() {
        return SPEC.isLoaded() ? LUCKY_MOMENTS.get() : LUCKY_MOMENTS.getDefault();
    }

    public static boolean realmFog() {
        return SPEC.isLoaded() ? REALM_FOG.get() : REALM_FOG.getDefault();
    }

    public static boolean demonRealm() {
        return SPEC.isLoaded() ? DEMON_REALM.get() : DEMON_REALM.getDefault();
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
