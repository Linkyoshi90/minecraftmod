package com.linkyoshi90.harpies.registry;

import com.linkyoshi90.harpies.HarpiesMod;
import net.minecraft.world.item.CreativeModeTabs;
import com.linkyoshi90.harpies.item.PhoenixAshesItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(HarpiesMod.MODID);

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

    /** Laid by a riflebird for someone she's fond of; unfertilised, so it's just a (fancy) snack. */
    public static final DeferredItem<Item> RIFLEBIRD_EGG = ITEMS.registerItem("riflebird_egg",
            props -> new Item(props.stacksTo(16).food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.4F).build())));
    /** A woven nest for riflebirds to adopt and lay their eggs in. */
    public static final DeferredItem<BlockItem> RIFLEBIRD_NEST = ITEMS.registerSimpleBlockItem(ModBlocks.RIFLEBIRD_NEST);
    /** What a slain phoenix leaves; warm it by a fire to hatch a chick. */
    public static final DeferredItem<PhoenixAshesItem> PHOENIX_ASHES = ITEMS.registerItem("phoenix_ashes",
            props -> new PhoenixAshesItem(props.stacksTo(16).rarity(Rarity.RARE)));

    private ModItems() {}

    public static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) event.accept(RIFLEBIRD_EGG);
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
        }
    }
}
