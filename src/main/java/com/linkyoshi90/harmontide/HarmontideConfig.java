package com.linkyoshi90.harmontide;

import com.linkyoshi90.harmontide.entity.AbstractAnuran;
import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/** Options shared by client and server (config/harmontide-common.toml, or Mods > Harmontide > Config in the game). */
public final class HarmontideConfig {
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
            .comment("Only one of each legendary kind comes to every " + com.linkyoshi90.harmontide.entity.Rarity.REGION + " blocks square, and she stays. One who dies unbefriended frees the region again.")
            .define("legendaryOncePerRegion", true);

    public static final ModConfigSpec.IntValue ENCOUNTER_RATE = BUILDER
            .comment("How often a wild one meets you in trouble (caught in a web, chased by zombies, lost in the rain), as a percent of normal",
                    "(normal is one in " + com.linkyoshi90.harmontide.entity.Encounters.RARITY + "; 0 turns them off).")
            .defineInRange("encounterRate", 100, 0, MAX_RATE);

    public static final ModConfigSpec.BooleanValue SPAWN_CONDITIONS = BUILDER
            .comment("Some kinds come out only at their moment in the overworld: the moon rabbit under a full moon, the vampire at night,",
                    "the ziz in a storm, the kappa in the rain. Off, they come whenever.")
            .define("spawnConditions", true);

    public static final ModConfigSpec.BooleanValue CHORES = BUILDER
            .comment("Friends on patrol do the chores around their patrol spot: gather eggs and wool, take honey from full hives, shear sheep, fish.")
            .define("chores", true);

    public static final ModConfigSpec.BooleanValue FESTIVALS = BUILDER
            .comment("Three festivals in every " + com.linkyoshi90.harmontide.entity.girl.Festivals.CYCLE + " days (a dance, a harvest feast, a gift exchange): the girls at your hearth gather round it at dusk.")
            .define("festivals", true);

    public static final ModConfigSpec.BooleanValue TRAVELLING_MARKET = BUILDER
            .comment("Every " + com.linkyoshi90.harmontide.entity.girl.Market.EVERY + " days, when you are home, a travelling girl from far away sets up by your hearth and trades her finds for emeralds.")
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

    public static final ModConfigSpec.BooleanValue MINERS_DIG_SHAFTS = BUILDER
            .comment("A friend sent mining digs a real laddered shaft beside her spot, deeper the longer she's down (natural ground only: never through",
                    "anything built, never into water, lava or a cave). Off: she goes underground without touching the world.")
            .define("minersDigShafts", true);

    public static final ModConfigSpec.IntValue HEARTH_SAFE_RADIUS = BUILDER
            .comment("No hostile mob spawns naturally within this many blocks of a hearth with " + com.linkyoshi90.harmontide.block.HearthBlockEntity.SAFE_RESIDENTS + " or more living there (0 turns it off).")
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
    private static final Map<com.linkyoshi90.harmontide.entity.Clade, ModConfigSpec.IntValue> FAMILY_RATES = new EnumMap<>(com.linkyoshi90.harmontide.entity.Clade.class);

    public static final ModConfigSpec.BooleanValue MILK_DRIPS;
    public static final ModConfigSpec.BooleanValue FLIRTING;
    public static final ModConfigSpec.BooleanValue DEMON_REALM;
    public static final ModConfigSpec.BooleanValue REALM_FOG;
    public static final ModConfigSpec.BooleanValue LUCKY_MOMENTS;
    public static final ModConfigSpec.BooleanValue COURTSHIP;
    public static final ModConfigSpec.BooleanValue INSPECTIONS;
    public static final ModConfigSpec.BooleanValue CARE_NEEDS;
    public static final ModConfigSpec.BooleanValue LEGEND_TRIALS;
    public static final ModConfigSpec.BooleanValue SPIRIT_PACTS;
    public static final ModConfigSpec.BooleanValue HEADGEAR;
    public static final ModConfigSpec.BooleanValue WANDERING_MERCHANTS;
    public static final ModConfigSpec.BooleanValue SQUAD;
    public static final ModConfigSpec.BooleanValue WIFE_BLESSINGS;
    public static final ModConfigSpec.BooleanValue FOOD_WEB;
    public static final ModConfigSpec.BooleanValue HERDS;
    public static final ModConfigSpec.BooleanValue TERRITORIES;
    public static final ModConfigSpec.BooleanValue PARTNERSHIPS;
    public static final ModConfigSpec.BooleanValue DEAD_REALM;
    public static final ModConfigSpec.BooleanValue SABBATH;
    public static final ModConfigSpec.BooleanValue SEA_WEDDINGS;
    public static final ModConfigSpec.BooleanValue GHOST_POSSESSION;
    public static final ModConfigSpec.BooleanValue COOKING;
    public static final ModConfigSpec.BooleanValue DULLAHAN_HEADS;
    public static final ModConfigSpec.BooleanValue CURSED_GEAR;
    public static final ModConfigSpec.BooleanValue MONSTERIZATION;
    public static final ModConfigSpec.BooleanValue POSSESSION;
    public static final ModConfigSpec.BooleanValue REALM_SUMMONS;
    public static final ModConfigSpec.BooleanValue POSSESSION_SPREADS;
    public static final ModConfigSpec.BooleanValue ROSE_MOON;
    public static final ModConfigSpec.BooleanValue ORDER_PATROLS;
    public static final ModConfigSpec.BooleanValue HAMLETS;
    public static final ModConfigSpec.BooleanValue SWEETHEARTS;
    public static final ModConfigSpec.BooleanValue DATES;
    public static final ModConfigSpec.BooleanValue NAME_NEWBORNS;
    public static final ModConfigSpec.BooleanValue COMPLIMENTS;
    public static final ModConfigSpec.BooleanValue HEROS_WELCOME;
    public static final ModConfigSpec.BooleanValue CRAVINGS;
    public static final ModConfigSpec.BooleanValue SHARED_DREAMS;
    public static final ModConfigSpec.BooleanValue PACKED_LUNCH;
    public static final ModConfigSpec.BooleanValue REUNIONS;
    public static final ModConfigSpec.BooleanValue DAUGHTERS_MOVE_OUT;
    public static final ModConfigSpec.BooleanValue HOT_SPRINGS;
    public static final ModConfigSpec.BooleanValue AURORA_NIGHTS;
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
        for (com.linkyoshi90.harmontide.entity.Clade clade : com.linkyoshi90.harmontide.entity.Clade.values()) {
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

        COURTSHIP = BUILDER
                .comment("Old customs: a wild fighter beaten fairly to half health yields and respects you, a wild spinner or tigress who catches you and is not struck claims you, a wild house spirit who keeps your hearth for a while stays fond of you.")
                .define("courtship", true);

        INSPECTIONS = BUILDER
                .comment("With a homestay permit and a hearth with residents, an inspector from the exchange office calls every week: beds, food put away, the residents well and fond of you, no monsters about. Emeralds for a good home, Hero of the Village for a perfect one, a warning for a poor one.")
                .define("inspections", true);

        CARE_NEEDS = BUILDER
                .comment("Care needs: a merfolk resident wants a pool (6+ water blocks) by her hearth and lives in it; lamias crawl in the cold unless warmed (winter coat, fire, hot spring); harpies moult every few days and give their favourite feathers.")
                .define("careNeeds", true);

        LEGEND_TRIALS = BUILDER
                .comment("A wild legend sets a player she meets a trial (an offering, a fair duel to half health, or a night's vigil beside her); passing it wins her fondness and her relic.")
                .define("legendTrials", true);

        SPIRIT_PACTS = BUILDER
                .comment("A devoted salamander, undine, sylph or gnome given an amethyst shard (crouching) binds herself to you: her element goes with you everywhere (burning blows, healing water, light quick steps, strength deep underground).")
                .define("spiritPacts", true);

        HEADGEAR = BUILDER
                .comment("A devoted kitsune, jackalope, devil or flora friend gives you her kind's headgear once (fox mask: luck; bunny ears: monsters hunting you glow; witch hat: more experience; flower crown: bees spare you, the sun heals you).")
                .define("headgear", true);

        WANDERING_MERCHANTS = BUILDER
                .comment("Now and then a wild girl comes as a wandering merchant with her kind's wares (an empty hand opens her trades). After two days she moves on, unless she has made a friend.")
                .define("wanderingMerchants", true);

        SQUAD = BUILDER
                .comment("Enlist up to four fighter friends with a goat horn (crouch, use it on her): a blown horn rallies them to you to fight, they turn on whatever strikes you, and they march home when the Order raids your hearth.")
                .define("squad", true);

        PARTNERSHIPS = BUILDER
                .comment("Partnerships between kinds: cleaner crustaceans and big sea girls, hermit crabs and anemones, small rodents and rabbits riding big grazers, ants guarding plant girls, pollinators and plant girls, scavengers trailing hunters, and mixed herds sharing alarms.")
                .define("partnerships", true);

        TERRITORIES = BUILDER
                .comment("Territories: wild hunters claim a home range, patrol its edge and scent-mark it; rivals of the same kind keep off and settle elsewhere. A befriended dog, cat, bear or snake kind near you lets you see the marks.")
                .define("territories", true);

        HERDS = BUILDER
                .comment("Herds and schools: wild grazers keep together on land and wild fishers school in the water; a lookout calls the alarm at a monster, a hunter or a stranger rushing up, and the herd flees together or bunches up and stands its ground.")
                .define("herds", true);

        FOOD_WEB = BUILDER
                .comment("The food web: wild girls feed themselves as their diet says (hunters bring down wild animals their size, fishers take fish, grazers crop the grass, scavengers eat what was left), move on when the land is eaten out, and thinned-out herds come back over time. Farm animals (tame, named, leashed, penned, in villages, near a hearth) are left alone.")
                .define("foodWeb", true);

        WIFE_BLESSINGS = BUILDER
                .comment("Wives bless the land: each wife near her husband's hearth brings the home her kind's blessing (crops grow, herds thrive, fishing luck, monsters weakened, haste, gentle regeneration).")
                .define("wifeBlessings", true);

        DEAD_REALM = BUILDER
                .comment("The realm of the dead: a wild lich or koschei seeds grave-grass that spreads in the dark, keeps twilight mist, and turns zombies and skeletons that linger on it. Sunlight, light and holy water push it back.")
                .define("deadRealm", true);

        SABBATH = BUILDER
                .comment("The Sabbath: realm deeds earn standing with the witches' Sabbath (ranks, a witch familiar, Sabbath trades and a title), the realm-side mirror of the Order's notoriety.")
                .define("sabbath", true);

        SEA_WEDDINGS = BUILDER
                .comment("Sea weddings: a wedding to a sea kind held by the sea, blessed by a sea bishop, gives water breathing near the ocean; a merfolk close friend may once give mermaid blood (a lasting extra heart).")
                .define("seaWeddings", true);

        GHOST_POSSESSION = BUILDER
                .comment("Ghost possession: a ghost friend can slip into you for a minute (night vision, monsters lose track of you, a faint glow); wild ghosts do it as a prank.")
                .define("ghostPossession", true);

        COOKING = BUILDER
                .comment("Every girl has her own cooking skill: a home-cooked meal she makes for you may be a treat with a buff, or burnt love food (mild sickness, but she is happy you ate it).")
                .define("cooking", true);

        DULLAHAN_HEADS = BUILDER
                .comment("A startled dullahan's head pops off: hand it back for her thanks, or she chases it.")
                .define("dullahanHeads", true);

        CURSED_GEAR = BUILDER
                .comment("Cursed gear: living armour and cursed blades found in old places work as gear, whisper, grow fond of whoever uses them, and at full fondness step out as a girl.")
                .define("cursedGear", true);

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
                .comment("A player who keeps company with the Demon Realm slowly becomes kin to it: night vision in its dark, its own let them be and they move quickly there, and in the end it feeds them, makes them strong at night and gives two more hearts. Realm spouses add to it; Order sisters sprinkle kindred players and chaplains won't bless them. Holy water washes it out.")
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

        NAME_NEWBORNS = BUILDER
                .comment("Little ones born to a player come with a name of their own (a name tag still renames them).")
                .define("nameNewborns", true);

        COMPLIMENTS = BUILDER
                .comment("Put on something new and the first friend who sees you says something nice about it.")
                .define("compliments", true);

        HEROS_WELCOME = BUILDER
                .comment("Great vanilla deeds (the dragon slain, a village saved...) reach home: your friends at the hearth throw you a party.")
                .define("herosWelcome", true);

        CRAVINGS = BUILDER
                .comment("An expecting girl craves one food; fed it before her time, she has twins.")
                .define("cravings", true);

        SHARED_DREAMS = BUILDER
                .comment("A wife who shared your bed may wake having dreamt of a real landmark nearby, and says which way.")
                .define("sharedDreams", true);

        PACKED_LUNCH = BUILDER
                .comment("Of a morning, a wife you pass hands you a packed lunch.")
                .define("packedLunch", true);

        REUNIONS = BUILDER
                .comment("Reunion Day (day 12 of every 24): at dusk your grown children who live away come home to your hearth with their households, and go back the next morning.")
                .define("reunions", true);

        DAUGHTERS_MOVE_OUT = BUILDER
                .comment("A grown daughter who settles at your hearth builds a house of her own a little way off (two beds, a cradle), and her villager sweetheart moves in once they are wed. Off: she builds the usual hut.")
                .define("daughtersMoveOut", true);

        HOT_SPRINGS = BUILDER
                .comment("Hot springs: water over geyserite steams, never freezes, heals and warms whoever soaks in it (friends come and join you), and cures a girl's cold. Off stops the effect and new springs in new chunks.")
                .define("hotSprings", true);

        AURORA_NIGHTS = BUILDER
                .comment("One clear night in six, an aurora over cold country: a frost-touched girl or two comes out near each player (gone at dawn unless befriended), and aurora shards fall, a gift every girl loves.")
                .define("auroraNights", true);

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
                .comment("How many monster girls one player may marry (0: any number). Spouses are one happy family: no jealousy, only more warmth.")
                .defineInRange("maxSpouses", 0, 0, 64);

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

    private HarmontideConfig() {}

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

    public static boolean minersDigShafts() {
        return SPEC.isLoaded() ? MINERS_DIG_SHAFTS.get() : MINERS_DIG_SHAFTS.getDefault();
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
        com.linkyoshi90.harmontide.entity.Clade clade = com.linkyoshi90.harmontide.entity.Clade.of(mob);
        if (clade == null && mob instanceof AbstractHarpy) clade = com.linkyoshi90.harmontide.entity.Clade.AVES;
        if (clade == null && mob instanceof AbstractAnuran) clade = com.linkyoshi90.harmontide.entity.Clade.AMPHIBIA;
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

    public static boolean auroraNights() {
        return SPEC.isLoaded() ? AURORA_NIGHTS.get() : AURORA_NIGHTS.getDefault();
    }

    public static boolean hotSprings() {
        return SPEC.isLoaded() ? HOT_SPRINGS.get() : HOT_SPRINGS.getDefault();
    }

    public static boolean compliments() {
        return SPEC.isLoaded() ? COMPLIMENTS.get() : COMPLIMENTS.getDefault();
    }

    public static boolean herosWelcome() {
        return SPEC.isLoaded() ? HEROS_WELCOME.get() : HEROS_WELCOME.getDefault();
    }

    public static boolean cravings() {
        return SPEC.isLoaded() ? CRAVINGS.get() : CRAVINGS.getDefault();
    }

    public static boolean sharedDreams() {
        return SPEC.isLoaded() ? SHARED_DREAMS.get() : SHARED_DREAMS.getDefault();
    }

    public static boolean reunions() {
        return SPEC.isLoaded() ? REUNIONS.get() : REUNIONS.getDefault();
    }

    public static boolean daughtersMoveOut() {
        return SPEC.isLoaded() ? DAUGHTERS_MOVE_OUT.get() : DAUGHTERS_MOVE_OUT.getDefault();
    }

    public static boolean packedLunch() {
        return SPEC.isLoaded() ? PACKED_LUNCH.get() : PACKED_LUNCH.getDefault();
    }

    public static boolean nameNewborns() {
        return SPEC.isLoaded() ? NAME_NEWBORNS.get() : NAME_NEWBORNS.getDefault();
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

    public static boolean squad() {
        return SPEC.isLoaded() ? SQUAD.get() : SQUAD.getDefault();
    }

    public static boolean partnerships() {
        return SPEC.isLoaded() ? PARTNERSHIPS.get() : PARTNERSHIPS.getDefault();
    }

    public static boolean territories() {
        return SPEC.isLoaded() ? TERRITORIES.get() : TERRITORIES.getDefault();
    }

    public static boolean herds() {
        return SPEC.isLoaded() ? HERDS.get() : HERDS.getDefault();
    }

    public static boolean foodWeb() {
        return SPEC.isLoaded() ? FOOD_WEB.get() : FOOD_WEB.getDefault();
    }

    public static boolean wifeBlessings() {
        return SPEC.isLoaded() ? WIFE_BLESSINGS.get() : WIFE_BLESSINGS.getDefault();
    }

    public static boolean deadRealm() {
        return SPEC.isLoaded() ? DEAD_REALM.get() : DEAD_REALM.getDefault();
    }

    public static boolean sabbath() {
        return SPEC.isLoaded() ? SABBATH.get() : SABBATH.getDefault();
    }

    public static boolean seaWeddings() {
        return SPEC.isLoaded() ? SEA_WEDDINGS.get() : SEA_WEDDINGS.getDefault();
    }

    public static boolean ghostPossession() {
        return SPEC.isLoaded() ? GHOST_POSSESSION.get() : GHOST_POSSESSION.getDefault();
    }

    public static boolean cooking() {
        return SPEC.isLoaded() ? COOKING.get() : COOKING.getDefault();
    }

    public static boolean dullahanHeads() {
        return SPEC.isLoaded() ? DULLAHAN_HEADS.get() : DULLAHAN_HEADS.getDefault();
    }

    public static boolean cursedGear() {
        return SPEC.isLoaded() ? CURSED_GEAR.get() : CURSED_GEAR.getDefault();
    }

    public static boolean wanderingMerchants() {
        return SPEC.isLoaded() ? WANDERING_MERCHANTS.get() : WANDERING_MERCHANTS.getDefault();
    }

    public static boolean headgear() {
        return SPEC.isLoaded() ? HEADGEAR.get() : HEADGEAR.getDefault();
    }

    public static boolean spiritPacts() {
        return SPEC.isLoaded() ? SPIRIT_PACTS.get() : SPIRIT_PACTS.getDefault();
    }

    public static boolean legendTrials() {
        return SPEC.isLoaded() ? LEGEND_TRIALS.get() : LEGEND_TRIALS.getDefault();
    }

    public static boolean careNeeds() {
        return SPEC.isLoaded() ? CARE_NEEDS.get() : CARE_NEEDS.getDefault();
    }

    public static boolean inspections() {
        return SPEC.isLoaded() ? INSPECTIONS.get() : INSPECTIONS.getDefault();
    }

    public static boolean courtship() {
        return SPEC.isLoaded() ? COURTSHIP.get() : COURTSHIP.getDefault();
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
