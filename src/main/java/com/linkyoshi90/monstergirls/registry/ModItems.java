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
    public static final DeferredItem<DeferredSpawnEggItem> SURINAM_TOAD_SPAWN_EGG = ITEMS.register("surinam_toad_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.SURINAM_TOAD, 0x624A2E, 0xDCC8A4, new Item.Properties()));

    /** Laid by a riflebird for someone she's fond of; unfertilised, so it's just a (fancy) snack. */
    public static final DeferredItem<Item> RIFLEBIRD_EGG = ITEMS.registerItem("riflebird_egg",
            props -> new Item(props.stacksTo(16).food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.4F).build())));
    /** Laid by a sparrow roosting in a dropper: unfertilised, eaten raw or baked like any egg. */
    public static final DeferredItem<Item> SPARROW_EGG = ITEMS.registerItem("sparrow_egg",
            props -> new Item(props.stacksTo(16).food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build())));
    /** A woven nest for riflebirds to adopt and lay their eggs in. */
    public static final DeferredItem<BlockItem> RIFLEBIRD_NEST = ITEMS.registerSimpleBlockItem(ModBlocks.RIFLEBIRD_NEST);
    /** A fertilised harpy egg: place it somewhere solid and it hatches in about a day. */
    public static final DeferredItem<BlockItem> HARPY_EGG = ITEMS.register("harpy_egg",
            () -> new HarpyEggItem(ModBlocks.HARPY_EGG.get(), new Item.Properties().stacksTo(16)));
    /** What a slain phoenix leaves; warm it by a fire to hatch a chick. */
    public static final DeferredItem<PhoenixAshesItem> PHOENIX_ASHES = ITEMS.registerItem("phoenix_ashes",
            props -> new PhoenixAshesItem(props.stacksTo(16).rarity(Rarity.RARE)));

    private ModItems() {}

    public static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(RIFLEBIRD_EGG);
            event.accept(SPARROW_EGG);
        }
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            for (HarpyEggBlock.Species species : HarpyEggBlock.Species.values()) event.accept(HarpyEggItem.of(ModBlocks.HARPY_EGG.get(), species));
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) event.accept(PHOENIX_ASHES);
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) event.accept(RIFLEBIRD_NEST);
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(SPARROW_HARPY_SPAWN_EGG);
            event.accept(CROW_HARPY_SPAWN_EGG);
            event.accept(OSTRICH_HARPY_SPAWN_EGG);
            event.accept(RIFLEBIRD_HARPY_SPAWN_EGG);
            event.accept(PHOENIX_HARPY_SPAWN_EGG);
            event.accept(OWL_HARPY_SPAWN_EGG);
            event.accept(SEAGULL_HARPY_SPAWN_EGG);
            event.accept(ALBATROSS_HARPY_SPAWN_EGG);
            event.accept(BULLYWUG_SPAWN_EGG);
            event.accept(MUCUS_TOAD_SPAWN_EGG);
            event.accept(BULLFROG_SPAWN_EGG);
            event.accept(TREE_FROG_SPAWN_EGG);
            event.accept(POISON_DART_FROG_SPAWN_EGG);
            event.accept(GLASS_FROG_SPAWN_EGG);
            event.accept(GOLIATH_FROG_SPAWN_EGG);
            event.accept(DESERT_RAIN_FROG_SPAWN_EGG);
            event.accept(FIRE_BELLIED_TOAD_SPAWN_EGG);
            event.accept(CANE_TOAD_SPAWN_EGG);
            event.accept(NATTERJACK_SPAWN_EGG);
            event.accept(SURINAM_TOAD_SPAWN_EGG);
        }
    }
}
