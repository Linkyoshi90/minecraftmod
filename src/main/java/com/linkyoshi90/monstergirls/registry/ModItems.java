package com.linkyoshi90.monstergirls.registry;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.world.item.CreativeModeTabs;
import com.linkyoshi90.monstergirls.block.HarpyEggBlock;
import com.linkyoshi90.monstergirls.item.HarpyEggItem;
import com.linkyoshi90.monstergirls.item.PhoenixAshesItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MonsterGirlsMod.MODID);

    public static final DeferredItem<DeferredSpawnEggItem> SPARROW_HARPY_SPAWN_EGG = ITEMS.register("sparrow_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SPARROW_HARPY, 0x8A5A34, 0xEAD9B8, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> CROW_HARPY_SPAWN_EGG = ITEMS.register("crow_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.CROW_HARPY, 0x16151F, 0x6A5A9E, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> OSTRICH_HARPY_SPAWN_EGG = ITEMS.register("ostrich_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.OSTRICH_HARPY, 0x201C1A, 0xD8C4A6, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> OWL_HARPY_SPAWN_EGG = ITEMS.register("owl_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.OWL_HARPY, 0x6E4E30, 0xE2C690, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SEAGULL_HARPY_SPAWN_EGG = ITEMS.register("seagull_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SEAGULL_HARPY, 0xEEF0F4, 0x2A3A6C, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> ALBATROSS_HARPY_SPAWN_EGG = ITEMS.register("albatross_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.ALBATROSS_HARPY, 0xF4F2EC, 0x3A3530, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> RIFLEBIRD_HARPY_SPAWN_EGG = ITEMS.register("riflebird_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.RIFLEBIRD_HARPY, 0x101018, 0x30B0A0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> PHOENIX_HARPY_SPAWN_EGG = ITEMS.register("phoenix_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.PHOENIX_HARPY, 0xC81E10, 0xFFD060, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> TENGU_HARPY_SPAWN_EGG = ITEMS.register("tengu_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.TENGU_HARPY, 0x16151F, 0xF0EAD6, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> ZHUQUE_HARPY_SPAWN_EGG = ITEMS.register("zhuque_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.ZHUQUE_HARPY, 0xD8301A, 0x2A9A6A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> ROOKMOURN_HARPY_SPAWN_EGG = ITEMS.register("rookmourn_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.ROOKMOURN_HARPY, 0x16141E, 0xD8C8A0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> CASSOWRATH_HARPY_SPAWN_EGG = ITEMS.register("cassowrath_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.CASSOWRATH_HARPY, 0x14161E, 0x2C5CAA, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MAILTOAD_SPAWN_EGG = ITEMS.register("mailtoad_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MAILTOAD, 0x325CB4, 0xD2A632, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> DODOBOLD_HARPY_SPAWN_EGG = ITEMS.register("dodobold_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.DODOBOLD_HARPY, 0x2A3C74, 0xF0C450, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> PEAFLARE_HARPY_SPAWN_EGG = ITEMS.register("peaflare_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.PEAFLARE_HARPY, 0x1E62B4, 0x2AA87A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> PUMPKINGULL_HARPY_SPAWN_EGG = ITEMS.register("pumpkingull_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.PUMPKINGULL_HARPY, 0xE88A2A, 0x1E1A1C, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> CONDORUNE_HARPY_SPAWN_EGG = ITEMS.register("condorune_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.CONDORUNE_HARPY, 0x18181C, 0xF0F0F2, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MIRROR_CRANE_HARPY_SPAWN_EGG = ITEMS.register("mirror_crane_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MIRROR_CRANE_HARPY, 0x2A48B0, 0xF0F4FF, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> EMBERU_HARPY_SPAWN_EGG = ITEMS.register("emberu_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.EMBERU_HARPY, 0x223A8C, 0xC8D8F0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> IRONBEAK_HARPY_SPAWN_EGG = ITEMS.register("ironbeak_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.IRONBEAK_HARPY, 0x3C6ED2, 0xB0B8C8, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> IBISET_HARPY_SPAWN_EGG = ITEMS.register("ibiset_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.IBISET_HARPY, 0x2E56BA, 0xECC85A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> STIGOWL_HARPY_SPAWN_EGG = ITEMS.register("stigowl_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.STIGOWL_HARPY, 0x1A1E2E, 0xA8B0D0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> YATAGARASU_HARPY_SPAWN_EGG = ITEMS.register("yatagarasu_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.YATAGARASU_HARPY, 0x141418, 0xB4D2F0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SKYCROW_HARPY_SPAWN_EGG = ITEMS.register("skycrow_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SKYCROW_HARPY, 0x141A30, 0x5AB8F8, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> RELIQUARY_DOVE_HARPY_SPAWN_EGG = ITEMS.register("reliquary_dove_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.RELIQUARY_DOVE_HARPY, 0x7A9AC8, 0xE8C050, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> POULTRY_HARPY_SPAWN_EGG = ITEMS.register("poultry_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.POULTRY_HARPY, 0xF0ECE4, 0xC83A2A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> LANDHARPY_HARPY_SPAWN_EGG = ITEMS.register("landharpy_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.LANDHARPY_HARPY, 0xE8E8F0, 0x3A5A9A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> ARGENTOWLIS_HARPY_SPAWN_EGG = ITEMS.register("argentowlis_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.ARGENTOWLIS_HARPY, 0x2A3E7A, 0xDCE4F4, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> IBISKING_HARPY_SPAWN_EGG = ITEMS.register("ibisking_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.IBISKING_HARPY, 0x1E3A8A, 0xE8C050, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MOTMORA_HARPY_SPAWN_EGG = ITEMS.register("motmora_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MOTMORA_HARPY, 0x1E4AC8, 0xE8C050, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> PELICAN_HARPY_SPAWN_EGG = ITEMS.register("pelican_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.PELICAN_HARPY, 0xF0ECE0, 0xE8A02A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SWAN_HARPY_SPAWN_EGG = ITEMS.register("swan_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SWAN_HARPY, 0xF4F4F8, 0xE8902A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> DUCK_HARPY_SPAWN_EGG = ITEMS.register("duck_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.DUCK_HARPY, 0x1E6A4A, 0xD8C4A8, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> HUMMINGBIRD_HARPY_SPAWN_EGG = ITEMS.register("hummingbird_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.HUMMINGBIRD_HARPY, 0xC81E4A, 0x2A9A5A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> ROC_HARPY_SPAWN_EGG = ITEMS.register("roc_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.ROC_HARPY, 0x2C4A88, 0xB4CCE8, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> ZIZ_HARPY_SPAWN_EGG = ITEMS.register("ziz_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.ZIZ_HARPY, 0x3C96E6, 0xF6D650, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> BENNU_HARPY_SPAWN_EGG = ITEMS.register("bennu_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.BENNU_HARPY, 0x4C7CBA, 0xFAD882, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> GARUDA_HARPY_SPAWN_EGG = ITEMS.register("garuda_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.GARUDA_HARPY, 0xEE9C42, 0x3C7CBE, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> ALKONOST_HARPY_SPAWN_EGG = ITEMS.register("alkonost_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.ALKONOST_HARPY, 0xF0EEE0, 0xE6C890, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> EMU_HARPY_SPAWN_EGG = ITEMS.register("emu_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.EMU_HARPY, 0x68523C, 0x8CA8D0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> LECHUZA_HARPY_SPAWN_EGG = ITEMS.register("lechuza_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.LECHUZA_HARPY, 0x844C26, 0xCE9260, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MACAW_HARPY_SPAWN_EGG = ITEMS.register("macaw_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MACAW_HARPY, 0x2888DC, 0xFACE46, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> COIN_MAGPIE_HARPY_SPAWN_EGG = ITEMS.register("coin_magpie_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.COIN_MAGPIE_HARPY, 0x1A3440, 0xF0F0F4, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> TRICKSTER_RAVEN_HARPY_SPAWN_EGG = ITEMS.register("trickster_raven_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.TRICKSTER_RAVEN_HARPY, 0x16141E, 0x6A6496, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> FLAMINGO_HARPY_SPAWN_EGG = ITEMS.register("flamingo_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.FLAMINGO_HARPY, 0xF08AA8, 0x1E1E22, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> FINCH_HARPY_SPAWN_EGG = ITEMS.register("finch_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.FINCH_HARPY, 0x6A3A24, 0xE8C060, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> TOUCAN_HARPY_SPAWN_EGG = ITEMS.register("toucan_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.TOUCAN_HARPY, 0x141418, 0xF08A18, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> PENGUIN_HARPY_SPAWN_EGG = ITEMS.register("penguin_harpy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.PENGUIN_HARPY, 0x141820, 0xF4F6FA, new Item.Properties()));

    public static final DeferredItem<DeferredSpawnEggItem> BULLYWUG_SPAWN_EGG = ITEMS.register("bullywug_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.BULLYWUG, 0x5E9E3C, 0xE9D874, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> MUCUS_TOAD_SPAWN_EGG = ITEMS.register("mucus_toad_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.MUCUS_TOAD, 0x7A6A4A, 0xC8A060, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> BULLFROG_SPAWN_EGG = ITEMS.register("bullfrog_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.BULLFROG, 0x5E7028, 0xD6D0A0, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> TREE_FROG_SPAWN_EGG = ITEMS.register("tree_frog_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.TREE_FROG, 0x46B440, 0xFAD432, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> POISON_DART_FROG_SPAWN_EGG = ITEMS.register("poison_dart_frog_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.POISON_DART_FROG, 0x1454D6, 0x061C64, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> GLASS_FROG_SPAWN_EGG = ITEMS.register("glass_frog_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.GLASS_FROG, 0xA6DCB0, 0xDC3840, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> GOLIATH_FROG_SPAWN_EGG = ITEMS.register("goliath_frog_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.GOLIATH_FROG, 0x6B6A2A, 0xD2C08A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> DESERT_RAIN_FROG_SPAWN_EGG = ITEMS.register("desert_rain_frog_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.DESERT_RAIN_FROG, 0xCEA078, 0x06040A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> FIRE_BELLIED_TOAD_SPAWN_EGG = ITEMS.register("fire_bellied_toad_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.FIRE_BELLIED_TOAD, 0x6E7A3A, 0xE2461C, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> CANE_TOAD_SPAWN_EGG = ITEMS.register("cane_toad_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.CANE_TOAD, 0x8A6A44, 0xC8B27A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> NATTERJACK_SPAWN_EGG = ITEMS.register("natterjack_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.NATTERJACK, 0x8A7A52, 0xE2D24A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> VODYANOY_SPAWN_EGG = ITEMS.register("vodyanoy_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.VODYANOY, 0x4E6024, 0x46923A, new Item.Properties()));
    public static final DeferredItem<DeferredSpawnEggItem> SURINAM_TOAD_SPAWN_EGG = ITEMS.register("surinam_toad_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SURINAM_TOAD, 0x624A2E, 0xDCC8A4, new Item.Properties()));

    /** Laid by a riflebird for someone she's fond of; unfertilised, so it's just a (fancy) snack. */
    public static final DeferredItem<Item> RIFLEBIRD_EGG = ITEMS.registerItem("riflebird_egg",
            props -> new Item(props.stacksTo(16).food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.4F).build())));
    /** Laid by a sparrow roosting on a hopper: unfertilised, eaten raw or baked like any egg. */
    public static final DeferredItem<Item> SPARROW_EGG = ITEMS.registerItem("sparrow_egg",
            props -> new Item(props.stacksTo(16).food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build())));
    /** Unfertilised eggs every grown harpy lays on a hen's schedule (see AbstractHarpy#unfertilisedEgg). */
    public static final DeferredItem<Item> GULL_EGG = laidEgg("gull_egg", 3, 0.2F);
    public static final DeferredItem<Item> CROW_EGG = laidEgg("crow_egg", 2, 0.2F);
    public static final DeferredItem<Item> OWL_EGG = laidEgg("owl_egg", 3, 0.2F);
    public static final DeferredItem<Item> ALBATROSS_EGG = laidEgg("albatross_egg", 5, 0.4F);
    public static final DeferredItem<Item> OSTRICH_EGG = laidEgg("ostrich_egg", 8, 0.6F);
    public static final DeferredItem<Item> TENGU_EGG = laidEgg("tengu_egg", 3, 0.2F);
    public static final DeferredItem<Item> TOUCAN_EGG = laidEgg("toucan_egg", 2, 0.2F);
    public static final DeferredItem<Item> FINCH_EGG = laidEgg("finch_egg", 1, 0.1F);
    public static final DeferredItem<Item> FLAMINGO_EGG = laidEgg("flamingo_egg", 3, 0.2F);
    public static final DeferredItem<Item> MAGPIE_EGG = laidEgg("magpie_egg", 2, 0.2F);
    public static final DeferredItem<Item> ROOK_EGG = laidEgg("rook_egg", 2, 0.2F);
    public static final DeferredItem<Item> PEAFOWL_EGG = laidEgg("peafowl_egg", 4, 0.4F);
    public static final DeferredItem<Item> DODO_EGG = laidEgg("dodo_egg", 6, 0.5F);
    public static final DeferredItem<Item> CASSOWARY_EGG = laidEgg("cassowary_egg", 8, 0.6F);
    public static final DeferredItem<Item> RAVEN_EGG = laidEgg("raven_egg", 2, 0.2F);
    public static final DeferredItem<Item> ZHUQUE_EGG = laidEgg("zhuque_egg", 4, 0.3F);
    public static final DeferredItem<Item> PENGUIN_EGG = laidEgg("penguin_egg", 5, 0.4F);
    /** The phoenix's: still warm, it leaves you fire resistant for a little while. */
    public static final DeferredItem<Item> EMBER_EGG = ITEMS.registerItem("ember_egg",
            props -> new Item(props.stacksTo(16).food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).alwaysEdible()
                    .effect(() -> new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 600, 0), 1.0F).build())));

    private static DeferredItem<Item> laidEgg(String name, int nutrition, float saturation) {
        return ITEMS.registerItem(name, props -> new Item(props.stacksTo(16).food(new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build())));
    }

    /** What a lamia leaves behind every few days: a light leather (crafts into leather) and a keepsake. */
    public static final DeferredItem<Item> SHED_SKIN = ITEMS.registerSimpleItem("shed_skin", new Item.Properties().stacksTo(64));
    /** A tarantula's moult. */
    public static final DeferredItem<Item> EXOSKELETON = ITEMS.registerSimpleItem("exoskeleton", new Item.Properties().stacksTo(64));
    /** A woven nest for riflebirds to adopt and lay their eggs in. */
    public static final DeferredItem<BlockItem> RIFLEBIRD_NEST = ITEMS.registerSimpleBlockItem(ModBlocks.RIFLEBIRD_NEST);
    /** An Easter Bunny's painted egg (place it to hide it again). */
    public static final DeferredItem<BlockItem> PAINTED_EGG = ITEMS.registerSimpleBlockItem(ModBlocks.PAINTED_EGG);
    public static final DeferredItem<BlockItem> HEARTH = ITEMS.registerSimpleBlockItem(ModBlocks.HEARTH);
    public static final DeferredItem<BlockItem> MILKING_MACHINE = ITEMS.registerSimpleBlockItem(ModBlocks.MILKING_MACHINE);
    public static final DeferredItem<BlockItem> PICNIC_BLANKET = ITEMS.registerSimpleBlockItem(ModBlocks.PICNIC_BLANKET);
    public static final DeferredItem<BlockItem> VANITY_MIRROR = ITEMS.registerSimpleBlockItem(ModBlocks.VANITY_MIRROR);
    public static final DeferredItem<BlockItem> COOKING_POT = ITEMS.registerSimpleBlockItem(ModBlocks.COOKING_POT);
    public static final DeferredItem<BlockItem> SOAKING_TUB = ITEMS.registerSimpleBlockItem(ModBlocks.SOAKING_TUB);
    public static final DeferredItem<BlockItem> SPARRING_POST = ITEMS.registerSimpleBlockItem(ModBlocks.SPARRING_POST);
    public static final DeferredItem<com.linkyoshi90.monstergirls.item.ScoutPackItem> SCOUT_PACK = ITEMS.register("scout_pack", () -> new com.linkyoshi90.monstergirls.item.ScoutPackItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<BlockItem> BEDROLL = ITEMS.registerSimpleBlockItem(ModBlocks.BEDROLL);
    public static final DeferredItem<BlockItem> FIGURINE = ITEMS.registerSimpleBlockItem(ModBlocks.FIGURINE);
    public static final DeferredItem<Item> SKETCHBOOK = ITEMS.registerSimpleItem("sketchbook", new Item.Properties().durability(16));
    public static final DeferredItem<BlockItem> GIFT_BOX = ITEMS.registerSimpleBlockItem(ModBlocks.GIFT_BOX);
    public static final DeferredItem<BlockItem> BUTTER_CHURN = ITEMS.registerSimpleBlockItem(ModBlocks.BUTTER_CHURN);
    public static final DeferredItem<BlockItem> CHEESE_SHELF = ITEMS.registerSimpleBlockItem(ModBlocks.CHEESE_SHELF);
    public static final DeferredItem<BlockItem> MILK_TANK = ITEMS.registerSimpleBlockItem(ModBlocks.MILK_TANK);
    public static final DeferredItem<BlockItem> FRIDGE = ITEMS.registerSimpleBlockItem(ModBlocks.FRIDGE);
    public static final DeferredItem<BlockItem> NESTING_BOX = ITEMS.registerSimpleBlockItem(ModBlocks.NESTING_BOX);
    public static final DeferredItem<com.linkyoshi90.monstergirls.item.FriendCompassItem> FRIEND_COMPASS = ITEMS.register("friend_compass", () -> new com.linkyoshi90.monstergirls.item.FriendCompassItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> FRIENDSHIP_BRACELET = ITEMS.registerSimpleItem("friendship_bracelet", new Item.Properties().stacksTo(16));
    /** Given for meeting every kind on one branch of the tree of life (Journal.branch); in the offhand it gives that branch's milk's first effect. */
    public static final DeferredItem<com.linkyoshi90.monstergirls.item.BranchCharmItem> BRANCH_CHARM = ITEMS.register("branch_charm",
            () -> new com.linkyoshi90.monstergirls.item.BranchCharmItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> LOVE_LETTER = ITEMS.registerSimpleItem("love_letter", new Item.Properties().stacksTo(16));
    /** A fancy salami: food for the player, and fed to a close friend she misses them more (Missing). */
    public static final DeferredItem<Item> SALAMI = ITEMS.registerItem("salami",
            props -> new Item(props.stacksTo(16).food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.8F).build())));
    public static final DeferredItem<BlockItem> TAR_SHROOM = ITEMS.registerSimpleBlockItem(ModBlocks.TAR_SHROOM);
    /** The Demon Realm (entity/DemonRealm): its grass, its heart-shaped fruit (plants the bush), and the holy water that drives it back. */
    public static final DeferredItem<BlockItem> REALM_GRASS = ITEMS.registerSimpleBlockItem(ModBlocks.REALM_GRASS);
    public static final DeferredItem<BlockItem> REALM_LEAVES = ITEMS.registerSimpleBlockItem(ModBlocks.REALM_LEAVES);
    public static final DeferredItem<BlockItem> REALM_LOG = ITEMS.registerSimpleBlockItem(ModBlocks.REALM_LOG);
    public static final DeferredItem<BlockItem> WEEPCAP = ITEMS.registerSimpleBlockItem(ModBlocks.WEEPCAP);
    public static final DeferredItem<BlockItem> REALM_CENSER = ITEMS.registerSimpleBlockItem(ModBlocks.REALM_CENSER);
    public static final DeferredItem<BlockItem> REALM_PLANKS = ITEMS.registerSimpleBlockItem(ModBlocks.REALM_PLANKS);
    public static final DeferredItem<BlockItem> REALM_STAIRS = ITEMS.registerSimpleBlockItem(ModBlocks.REALM_STAIRS);
    public static final DeferredItem<BlockItem> REALM_SLAB = ITEMS.registerSimpleBlockItem(ModBlocks.REALM_SLAB);
    public static final DeferredItem<BlockItem> REALM_FENCE = ITEMS.registerSimpleBlockItem(ModBlocks.REALM_FENCE);
    public static final DeferredItem<BlockItem> REALM_FENCE_GATE = ITEMS.registerSimpleBlockItem(ModBlocks.REALM_FENCE_GATE);
    public static final DeferredItem<net.minecraft.world.item.DoubleHighBlockItem> REALM_DOOR = ITEMS.registerItem("realm_door",
            props -> new net.minecraft.world.item.DoubleHighBlockItem(ModBlocks.REALM_DOOR.get(), props));
    public static final DeferredItem<BlockItem> REALM_TRAPDOOR = ITEMS.registerSimpleBlockItem(ModBlocks.REALM_TRAPDOOR);
    public static final DeferredItem<BlockItem> REALM_BUTTON = ITEMS.registerSimpleBlockItem(ModBlocks.REALM_BUTTON);
    public static final DeferredItem<BlockItem> REALM_PRESSURE_PLATE = ITEMS.registerSimpleBlockItem(ModBlocks.REALM_PRESSURE_PLATE);
    /** Weepcap stew: the realm's pale mushrooms let you see in the dark for a while. */
    public static final DeferredItem<Item> WEEPCAP_STEW = ITEMS.registerItem("weepcap_stew", props -> new Item(props.stacksTo(1)
            .craftRemainder(net.minecraft.world.item.Items.BOWL)
            .food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.6F).usingConvertsTo(net.minecraft.world.item.Items.BOWL)
                    .effect(() -> new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION, 1200, 0), 1.0F).build())));
    public static final DeferredItem<net.minecraft.world.item.ItemNameBlockItem> HEARTFRUIT = ITEMS.register("heartfruit",
            () -> new net.minecraft.world.item.ItemNameBlockItem(ModBlocks.HEARTFRUIT_BUSH.get(),
                    new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.5F).build())));
    /** A holstaur's milk, drawn into a glass bottle (entity/girl/Minotaur): rich and sweet, it makes you strong for a while. */
    public static final DeferredItem<Item> HOLSTAUR_MILK = ITEMS.registerItem("holstaur_milk", props -> new Item(props.stacksTo(16)
            .craftRemainder(net.minecraft.world.item.Items.GLASS_BOTTLE)
            .food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.8F).alwaysEdible().usingConvertsTo(net.minecraft.world.item.Items.GLASS_BOTTLE)
                    .effect(() -> new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 3600, 0), 1.0F)
                    .effect(() -> new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 100, 0), 1.0F).build())) {
        @Override
        public net.minecraft.world.item.UseAnim getUseAnimation(net.minecraft.world.item.ItemStack stack) {
            return net.minecraft.world.item.UseAnim.DRINK;
        }

        @Override
        public net.minecraft.sounds.SoundEvent getEatingSound() {
            return net.minecraft.sounds.SoundEvents.GENERIC_DRINK;
        }
    });
    /** Heartfruit, holstaur milk, wheat and sugar: fed to a close friend she is lovestruck three times as long (entity/DemonRealm). */
    public static final DeferredItem<Item> HEARTFRUIT_TART = ITEMS.registerItem("heartfruit_tart", props -> new Item(props
            .food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.6F).build())));
    /** The exchange liaison's desk, and the permit she issues (entity/Liaison). */
    public static final DeferredItem<BlockItem> EXCHANGE_DESK = ITEMS.registerSimpleBlockItem(ModBlocks.EXCHANGE_DESK);
    public static final DeferredItem<Item> HOMESTAY_PERMIT = ITEMS.registerSimpleItem("homestay_permit", new Item.Properties().stacksTo(1));
    /** Clothes for her wardrobe (entity/Wardrobe): she wears them over her own when the moment comes. */
    public static final DeferredItem<Item> RAINCOAT = ITEMS.registerSimpleItem("raincoat", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> WINTER_COAT = ITEMS.registerSimpleItem("winter_coat", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> YUKATA = ITEMS.registerSimpleItem("yukata", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> MAID_APRON = ITEMS.registerSimpleItem("maid_apron", new Item.Properties().stacksTo(1));
    /** Given to a devoted friend, a proposal (entity/Marriage). */
    public static final DeferredItem<Item> WEDDING_RING = ITEMS.registerSimpleItem("wedding_ring", new Item.Properties().stacksTo(1));
    public static final DeferredItem<com.linkyoshi90.monstergirls.item.HolyWaterItem> HOLY_WATER = ITEMS.register("holy_water",
            () -> new com.linkyoshi90.monstergirls.item.HolyWaterItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<com.linkyoshi90.monstergirls.item.UmbrellaItem> UMBRELLA = ITEMS.register("umbrella", () -> new com.linkyoshi90.monstergirls.item.UmbrellaItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> GROOMING_BRUSH = ITEMS.registerSimpleItem("grooming_brush", new Item.Properties().durability(64));
    public static final DeferredItem<com.linkyoshi90.monstergirls.item.ToyBallItem> TOY_BALL = ITEMS.register("toy_ball", () -> new com.linkyoshi90.monstergirls.item.ToyBallItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<com.linkyoshi90.monstergirls.item.WhistleItem> WHISTLE = ITEMS.register("friend_whistle", () -> new com.linkyoshi90.monstergirls.item.WhistleItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<com.linkyoshi90.monstergirls.item.DairyItem> HOMEMADE_MEAL = ITEMS.register("homemade_meal", () -> new com.linkyoshi90.monstergirls.item.DairyItem(new Item.Properties().stacksTo(16)
            .food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(10).saturationModifier(0.8F).usingConvertsTo(net.minecraft.world.item.Items.BOWL).build())));
    /** A fertilised harpy egg: place it somewhere solid and it hatches in about a day. */
    public static final DeferredItem<BlockItem> HARPY_EGG = ITEMS.register("harpy_egg",
            () -> new HarpyEggItem(ModBlocks.HARPY_EGG.get(), new Item.Properties().stacksTo(16)));
    /** What a slain phoenix leaves; warm it by a fire to hatch a chick. */
    public static final DeferredItem<PhoenixAshesItem> PHOENIX_ASHES = ITEMS.registerItem("phoenix_ashes",
            props -> new PhoenixAshesItem(props.stacksTo(16).rarity(Rarity.RARE)));

    // --- Harmontide (answers of 2026-10-22): foods, materials and gear from the girls ----------------------------------

    private static DeferredItem<Item> food(String name, int nutrition, float saturation) {
        return ITEMS.registerItem(name, props -> new Item(props.food(new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build())));
    }

    /** Made from a minotaur cow's milk (or brought by her). */
    public static final DeferredItem<com.linkyoshi90.monstergirls.item.DairyItem> CHEESE = ITEMS.registerItem("cheese",
            props -> new com.linkyoshi90.monstergirls.item.DairyItem(props.food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.8F).build())));
    /** Churned from a bucket of milk (ButterChurnBlock); a little of her family's effects. */
    public static final DeferredItem<com.linkyoshi90.monstergirls.item.DairyItem> BUTTER = ITEMS.registerItem("butter",
            props -> new com.linkyoshi90.monstergirls.item.DairyItem(props.food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.3F).fast().build())));
    /** Milk, sugar, snow and a bowl (item.IceCreamRecipe). */
    public static final DeferredItem<com.linkyoshi90.monstergirls.item.DairyItem> ICE_CREAM = ITEMS.registerItem("ice_cream",
            props -> new com.linkyoshi90.monstergirls.item.DairyItem(props.stacksTo(16).food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.4F).alwaysEdible()
                    .usingConvertsTo(net.minecraft.world.item.Items.BOWL).build())));
    /** The Inaba's present at full devotion; also the base of mochi. */
    public static final DeferredItem<Item> RICE_CAKE = food("rice_cake", 5, 0.6F);
    public static final DeferredItem<Item> APPLE_MOCHI = food("apple_mochi", 7, 0.8F);
    public static final DeferredItem<Item> MELON_MOCHI = food("melon_mochi", 7, 0.8F);
    public static final DeferredItem<Item> BERRY_MOCHI = food("berry_mochi", 7, 0.8F);
    public static final DeferredItem<Item> CHOCOLATE_MOCHI = food("chocolate_mochi", 7, 0.8F);
    public static final DeferredItem<Item> HONEY_MOCHI = food("honey_mochi", 8, 0.8F);
    public static final DeferredItem<Item> PUMPKIN_MOCHI = food("pumpkin_mochi", 7, 0.8F);
    public static final java.util.List<DeferredItem<Item>> MOCHI = java.util.List.of(APPLE_MOCHI, MELON_MOCHI, BERRY_MOCHI, CHOCOLATE_MOCHI, HONEY_MOCHI, PUMPKIN_MOCHI);

    /** One bucket of milk per family. They stack as empty buckets do. */
    public static final java.util.Map<com.linkyoshi90.monstergirls.item.FamilyMilk, DeferredItem<com.linkyoshi90.monstergirls.item.FamilyMilkItem>> MILK = familyMilks();

    private static java.util.Map<com.linkyoshi90.monstergirls.item.FamilyMilk, DeferredItem<com.linkyoshi90.monstergirls.item.FamilyMilkItem>> familyMilks() {
        var milks = new java.util.EnumMap<com.linkyoshi90.monstergirls.item.FamilyMilk, DeferredItem<com.linkyoshi90.monstergirls.item.FamilyMilkItem>>(com.linkyoshi90.monstergirls.item.FamilyMilk.class);
        for (var milk : com.linkyoshi90.monstergirls.item.FamilyMilk.values()) {
            milks.put(milk, ITEMS.registerItem(milk.id(), props -> new com.linkyoshi90.monstergirls.item.FamilyMilkItem(milk,
                    props.stacksTo(16).craftRemainder(net.minecraft.world.item.Items.BUCKET))));
        }
        // the buckets from before the taxonomy (2026-11-05) turn into their clade's bucket in old saves
        for (var old : com.linkyoshi90.monstergirls.item.FamilyMilk.OLD.entrySet()) {
            String from = old.getKey().toLowerCase(java.util.Locale.ROOT) + "_milk";
            if (!from.equals(old.getValue().id())) ITEMS.addAlias(com.linkyoshi90.monstergirls.MonsterGirlsMod.id(from), com.linkyoshi90.monstergirls.MonsterGirlsMod.id(old.getValue().id()));
        }
        return milks;
    }

    /** Brushed off an arachne now and then: the material of silk trousers and shoes. */
    public static final DeferredItem<Item> SPIDER_SILK = ITEMS.registerSimpleItem("spider_silk", new Item.Properties());
    /** Shed by merfolk, wild or not: flippers are made of it. */
    public static final DeferredItem<Item> MERFOLK_SCALE = ITEMS.registerSimpleItem("merfolk_scale", new Item.Properties());
    /** A flora girl's nectar: waters and feeds a field. */
    public static final DeferredItem<com.linkyoshi90.monstergirls.item.NectarItem> NECTAR_BOTTLE = ITEMS.registerItem("nectar_bottle",
            props -> new com.linkyoshi90.monstergirls.item.NectarItem(props.stacksTo(16).craftRemainder(net.minecraft.world.item.Items.GLASS_BOTTLE)));
    /** An arachne's silk-wrapped present. */
    public static final DeferredItem<com.linkyoshi90.monstergirls.item.CocoonItem> SILK_COCOON = ITEMS.registerItem("silk_cocoon",
            props -> new com.linkyoshi90.monstergirls.item.CocoonItem(props.stacksTo(16)));
    /** A young merfolk carried in a bucket of water. */
    public static final DeferredItem<com.linkyoshi90.monstergirls.item.MerfolkBucketItem> MERFOLK_BUCKET = ITEMS.registerItem("merfolk_bucket",
            props -> new com.linkyoshi90.monstergirls.item.MerfolkBucketItem(props.stacksTo(1)));

    public static final DeferredItem<net.minecraft.world.item.ArmorItem> SNAKESKIN_CLOAK = ITEMS.registerItem("snakeskin_cloak",
            props -> new net.minecraft.world.item.ArmorItem(ModArmorMaterials.SNAKESKIN, net.minecraft.world.item.ArmorItem.Type.CHESTPLATE,
                    props.durability(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE.getDurability(6))));
    /** Iron's protection, but silk frays: about half an iron piece's durability. */
    public static final DeferredItem<net.minecraft.world.item.ArmorItem> SILK_TROUSERS = ITEMS.registerItem("silk_trousers",
            props -> new net.minecraft.world.item.ArmorItem(ModArmorMaterials.SPIDER_SILK, net.minecraft.world.item.ArmorItem.Type.LEGGINGS,
                    props.durability(net.minecraft.world.item.ArmorItem.Type.LEGGINGS.getDurability(7))));
    public static final DeferredItem<net.minecraft.world.item.ArmorItem> SILK_SHOES = ITEMS.registerItem("silk_shoes",
            props -> new net.minecraft.world.item.ArmorItem(ModArmorMaterials.SPIDER_SILK, net.minecraft.world.item.ArmorItem.Type.BOOTS,
                    props.durability(net.minecraft.world.item.ArmorItem.Type.BOOTS.getDurability(7))));
    public static final DeferredItem<com.linkyoshi90.monstergirls.item.FlippersItem> FLIPPERS = ITEMS.registerItem("flippers",
            props -> new com.linkyoshi90.monstergirls.item.FlippersItem(ModArmorMaterials.MERFOLK_SCALE, props.durability(net.minecraft.world.item.ArmorItem.Type.BOOTS.getDurability(10))));
    /** Leather and feathers: slows a fall and softens the landing (see item/GearEvents). */
    public static final DeferredItem<com.linkyoshi90.monstergirls.item.WingedCapItem> WINGED_CAP = ITEMS.registerItem("winged_cap",
            props -> new com.linkyoshi90.monstergirls.item.WingedCapItem(ModArmorMaterials.WINGED_CAP,
                    props.durability(net.minecraft.world.item.ArmorItem.Type.HELMET.getDurability(8))));

    /** An alraune rooted as a flower, to plant somewhere new. */
    public static final DeferredItem<BlockItem> GIRL_JAR = ITEMS.register("girl_jar",
            () -> new BlockItem(ModBlocks.GIRL_JAR.get(), new Item.Properties().stacksTo(1)));
    public static final DeferredItem<BlockItem> ALRAUNE_BLOOM = ITEMS.register("alraune_bloom",
            () -> new BlockItem(ModBlocks.ALRAUNE_BLOOM.get(), new Item.Properties().stacksTo(1)));

    /** A bucket of nectar (three bottles' worth): pour it into a field. */
    public static final DeferredItem<net.minecraft.world.item.BucketItem> NECTAR_BUCKET = ITEMS.register("nectar_bucket",
            () -> new net.minecraft.world.item.BucketItem(ModFluids.NECTAR.get(), new Item.Properties().craftRemainder(net.minecraft.world.item.Items.BUCKET).stacksTo(1)));

    /** Brought by healer girls (or made from nectar and honey): heals a befriended girl or vanilla friend. */
    public static final DeferredItem<Item> HEALING_BALM = ITEMS.registerSimpleItem("healing_balm", new Item.Properties().stacksTo(16));

    /** Shows tonight's moon phase, and how many nights until the full moon. */
    public static final DeferredItem<com.linkyoshi90.monstergirls.item.MoonDialItem> MOON_DIAL = ITEMS.registerItem("moon_dial",
            props -> new com.linkyoshi90.monstergirls.item.MoonDialItem(props.stacksTo(1)));

    /** The Harmontide journal: the creatures you have met and befriended (book + feather). */
    public static final DeferredItem<com.linkyoshi90.monstergirls.item.JournalItem> HARMONTIDE_JOURNAL = ITEMS.registerItem("harmontide_journal",
            props -> new com.linkyoshi90.monstergirls.item.JournalItem(props.stacksTo(1)));

    /** Creative only (no recipe): fills a girl's friendship with you to the full, for testing. */
    public static final DeferredItem<Item> FRIENDSHIP_CHARM = ITEMS.registerSimpleItem("friendship_charm", new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

    // --- the Harmontide creative tab: everything the mod adds -------------------------------------------------------

    public static final DeferredRegister<net.minecraft.world.item.CreativeModeTab> TABS = DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, MonsterGirlsMod.MODID);
    public static final net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.item.CreativeModeTab, net.minecraft.world.item.CreativeModeTab> TAB =
            TABS.register("harmontide", () -> net.minecraft.world.item.CreativeModeTab.builder()
                    .title(net.minecraft.network.chat.Component.translatable("itemGroup.monstergirls"))
                    .icon(() -> HarpyEggItem.of(ModBlocks.HARPY_EGG.get(), HarpyEggBlock.Species.values()[0]))
                    .displayItems((params, out) -> {
                        for (var e : ITEMS.getEntries()) {                     // items and blocks first, spawn eggs last
                            Item item = e.get();
                            if (item instanceof DeferredSpawnEggItem) continue;
                            if (item == HARPY_EGG.get()) {
                                for (HarpyEggBlock.Species species : HarpyEggBlock.Species.values()) out.accept(HarpyEggItem.of(ModBlocks.HARPY_EGG.get(), species));
                            } else {
                                out.accept(item);
                            }
                        }
                        for (var e : ITEMS.getEntries()) if (e.get() instanceof DeferredSpawnEggItem) out.accept(e.get());
                    }).build());

    private ModItems() {}

    /** Spawn eggs also go in vanilla's Spawn Eggs tab, as players expect; everything else lives in the Harmontide tab. */
    public static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            for (var e : ITEMS.getEntries()) if (e.get() instanceof DeferredSpawnEggItem) event.accept(e.get());
        }
    }
}
