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
    public static final DeferredItem<Item> CHEESE = food("cheese", 4, 0.8F);
    /** The Inaba's present at full devotion; also the base of mochi. */
    public static final DeferredItem<Item> RICE_CAKE = food("rice_cake", 5, 0.6F);
    public static final DeferredItem<Item> APPLE_MOCHI = food("apple_mochi", 7, 0.8F);
    public static final DeferredItem<Item> MELON_MOCHI = food("melon_mochi", 7, 0.8F);
    public static final DeferredItem<Item> BERRY_MOCHI = food("berry_mochi", 7, 0.8F);
    public static final DeferredItem<Item> CHOCOLATE_MOCHI = food("chocolate_mochi", 7, 0.8F);
    public static final DeferredItem<Item> HONEY_MOCHI = food("honey_mochi", 8, 0.8F);
    public static final DeferredItem<Item> PUMPKIN_MOCHI = food("pumpkin_mochi", 7, 0.8F);
    public static final java.util.List<DeferredItem<Item>> MOCHI = java.util.List.of(APPLE_MOCHI, MELON_MOCHI, BERRY_MOCHI, CHOCOLATE_MOCHI, HONEY_MOCHI, PUMPKIN_MOCHI);

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
