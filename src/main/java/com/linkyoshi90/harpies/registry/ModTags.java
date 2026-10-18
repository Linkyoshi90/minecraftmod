package com.linkyoshi90.harpies.registry;

import com.linkyoshi90.harpies.HarpiesMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    /** Seeds and bread: tempts sparrow harpies and can be fed to them. */
    public static final TagKey<Item> SPARROW_HARPY_FOOD = TagKey.create(Registries.ITEM, HarpiesMod.id("sparrow_harpy_food"));
    /** Small things a sparrow picks up and carries around for a while (and may snatch from your hand). */
    public static final TagKey<Item> SPARROW_HARPY_TRINKETS = TagKey.create(Registries.ITEM, HarpiesMod.id("sparrow_harpy_trinkets"));
    /** Ground a sparrow harpy may naturally spawn on (grass, farmland, sand, paths). */
    public static final TagKey<Block> SPARROW_HARPY_SPAWNABLE_ON = TagKey.create(Registries.BLOCK, HarpiesMod.id("sparrow_harpy_spawnable_on"));
    /** Blocks a sparrow harpy will take a dust bath in. */
    public static final TagKey<Block> SPARROW_HARPY_DUST_BATH = TagKey.create(Registries.BLOCK, HarpiesMod.id("sparrow_harpy_dust_bath"));

    /** What a crow eats: seeds, crops, berries, meat and fish. Also accepted as a small kindness (no gift). */
    public static final TagKey<Item> CROW_HARPY_FOOD = TagKey.create(Registries.ITEM, HarpiesMod.id("crow_harpy_food"));
    /** Shiny things a crow collects, steals first, and accepts as a trade for a gift. */
    public static final TagKey<Item> CROW_HARPY_SHINIES = TagKey.create(Registries.ITEM, HarpiesMod.id("crow_harpy_shinies"));
    public static final TagKey<Block> CROW_HARPY_SPAWNABLE_ON = TagKey.create(Registries.BLOCK, HarpiesMod.id("crow_harpy_spawnable_on"));
    /** Soft ground a crow can scratch a hole in to bury something. */
    public static final TagKey<Block> CROW_HARPY_CACHE_GROUND = TagKey.create(Registries.BLOCK, HarpiesMod.id("crow_harpy_cache_ground"));
    /** Placed within 8 blocks of a field, these keep crow murders from raiding it. */
    public static final TagKey<Block> CROW_HARPY_SCARECROWS = TagKey.create(Registries.BLOCK, HarpiesMod.id("crow_harpy_scarecrows"));

    /** Seeds and plants an ostrich eats on top of any ordinary food (she's an omnivore). */
    public static final TagKey<Item> OSTRICH_HARPY_FOOD = TagKey.create(Registries.ITEM, HarpiesMod.id("ostrich_harpy_food"));
    /** Leafy greens: an ostrich's favourite, worth double affection. */
    public static final TagKey<Item> OSTRICH_HARPY_FAVOURITES = TagKey.create(Registries.ITEM, HarpiesMod.id("ostrich_harpy_favourites"));
    public static final TagKey<Block> OSTRICH_HARPY_SPAWNABLE_ON = TagKey.create(Registries.BLOCK, HarpiesMod.id("ostrich_harpy_spawnable_on"));

    /** Meat, raw or cooked: lures an owl out even by day. */
    public static final TagKey<Item> OWL_HARPY_FOOD = TagKey.create(Registries.ITEM, HarpiesMod.id("owl_harpy_food"));
    /** What an owl hunts at night. */
    public static final TagKey<EntityType<?>> OWL_HARPY_PREY = TagKey.create(Registries.ENTITY_TYPE, HarpiesMod.id("owl_harpy_prey"));
    public static final TagKey<Block> OWL_HARPY_SPAWNABLE_ON = TagKey.create(Registries.BLOCK, HarpiesMod.id("owl_harpy_spawnable_on"));
    public static final TagKey<Block> SEAGULL_HARPY_SPAWNABLE_ON = TagKey.create(Registries.BLOCK, HarpiesMod.id("seagull_harpy_spawnable_on"));
    public static final TagKey<Block> ALBATROSS_HARPY_SPAWNABLE_ON = TagKey.create(Registries.BLOCK, HarpiesMod.id("albatross_harpy_spawnable_on"));

    /** Gifts a riflebird treasures most (worth three times any food). */
    public static final TagKey<Item> RIFLEBIRD_HARPY_PRECIOUS = TagKey.create(Registries.ITEM, HarpiesMod.id("riflebird_harpy_precious"));
    public static final TagKey<Block> RIFLEBIRD_HARPY_SPAWNABLE_ON = TagKey.create(Registries.BLOCK, HarpiesMod.id("riflebird_harpy_spawnable_on"));
    public static final TagKey<Block> PHOENIX_HARPY_SPAWNABLE_ON = TagKey.create(Registries.BLOCK, HarpiesMod.id("phoenix_harpy_spawnable_on"));

    /** Nests a riflebird adopts and lays her eggs in. */
    public static final TagKey<Block> RIFLEBIRD_HARPY_NESTS = TagKey.create(Registries.BLOCK, HarpiesMod.id("riflebird_harpy_nests"));

    private ModTags() {}
}
