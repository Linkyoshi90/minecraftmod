package com.linkyoshi90.monstergirls.registry;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.AnuranSpawnBlock;
import com.linkyoshi90.monstergirls.block.GirlEggBlock;
import com.linkyoshi90.monstergirls.block.HarpyEggBlock;
import com.linkyoshi90.monstergirls.block.PaintedEggBlock;
import com.linkyoshi90.monstergirls.block.RiflebirdNestBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MonsterGirlsMod.MODID);

    public static final DeferredBlock<RiflebirdNestBlock> RIFLEBIRD_NEST = BLOCKS.registerBlock("riflebird_nest", RiflebirdNestBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(0.5F).sound(SoundType.GRASS).noOcclusion()
                    .pushReaction(PushReaction.DESTROY));

    /** A fertilised harpy egg (the species is a block state). */
    public static final DeferredBlock<HarpyEggBlock> HARPY_EGG = BLOCKS.registerBlock("harpy_egg", HarpyEggBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(0.5F).sound(SoundType.METAL).noOcclusion()
                    .pushReaction(PushReaction.DESTROY));
    /** Frog and toad spawn on a still water surface (laid by a mother, never crafted). */
    public static final DeferredBlock<AnuranSpawnBlock> ANURAN_SPAWN = BLOCKS.registerBlock("anuran_spawn", AnuranSpawnBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.WATER).strength(0.0F).sound(SoundType.FROGSPAWN).noOcclusion().noCollission()
                    .pushReaction(PushReaction.DESTROY));

    /** Eggs, egg sacs and roe of the later families (the species lives in the block entity). */
    public static final DeferredBlock<GirlEggBlock> GIRL_EGG = BLOCKS.registerBlock("girl_egg", GirlEggBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(0.5F).sound(SoundType.FROGSPAWN).noOcclusion()
                    .pushReaction(PushReaction.DESTROY).randomTicks());

    /** A painted egg the Easter Bunny hid in the grass. */
    public static final DeferredBlock<PaintedEggBlock> PAINTED_EGG = BLOCKS.registerBlock("painted_egg", PaintedEggBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(0.2F).sound(SoundType.DECORATED_POT).noOcclusion()
                    .pushReaction(PushReaction.DESTROY));

    /** A firefly or a fairy caught in a jar: a light (she flies out when it is opened or broken). */
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.GirlJarBlock> GIRL_JAR = BLOCKS.registerBlock("girl_jar",
            com.linkyoshi90.monstergirls.block.GirlJarBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(0.3F).sound(SoundType.GLASS).noOcclusion().lightLevel(s -> 12)
                    .pushReaction(PushReaction.DESTROY));
    /** An alraune rooted as a flower (she unfolds when it is used or broken). */
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.AlrauneBloomBlock> ALRAUNE_BLOOM = BLOCKS.registerBlock("alraune_bloom",
            com.linkyoshi90.monstergirls.block.AlrauneBloomBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).instabreak().noCollission().sound(SoundType.AZALEA_LEAVES)
                    .offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY));

    /** A lich's phylactery: the vessel of her soul (she places it herself). */
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.PhylacteryBlock> PHYLACTERY = BLOCKS.registerBlock("phylactery",
            com.linkyoshi90.monstergirls.block.PhylacteryBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(2.0F, 6.0F).sound(SoundType.AMETHYST).noOcclusion()
                    .lightLevel(s -> 10).pushReaction(PushReaction.BLOCK));

    /** The Harmontide hearth: the heart of a settlement of befriended girls (HearthBlock). */
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.HearthBlock> HEARTH = BLOCKS.registerBlock("hearth",
            com.linkyoshi90.monstergirls.block.HearthBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0F, 6.0F).sound(SoundType.STONE).lightLevel(s -> 13)
                    .requiresCorrectToolForDrops());

    /** Fills buckets from a friend standing by it (MilkingMachineBlock). */
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.MilkingMachineBlock> MILKING_MACHINE = BLOCKS.registerBlock("milking_machine",
            com.linkyoshi90.monstergirls.block.MilkingMachineBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F, 6.0F).sound(SoundType.COPPER).requiresCorrectToolForDrops().noOcclusion());

    // the thirty-fourth wave: life at home
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.PicnicBlanketBlock> PICNIC_BLANKET = BLOCKS.registerBlock("picnic_blanket",
            com.linkyoshi90.monstergirls.block.PicnicBlanketBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(0.1F).sound(SoundType.WOOL).noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.VanityMirrorBlock> VANITY_MIRROR = BLOCKS.registerBlock("vanity_mirror",
            com.linkyoshi90.monstergirls.block.VanityMirrorBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0F).sound(SoundType.GLASS).noOcclusion());
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.CookingPotBlock> COOKING_POT = BLOCKS.registerBlock("cooking_pot",
            com.linkyoshi90.monstergirls.block.CookingPotBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).sound(SoundType.METAL).noOcclusion().requiresCorrectToolForDrops());
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.SoakingTubBlock> SOAKING_TUB = BLOCKS.registerBlock("soaking_tub",
            com.linkyoshi90.monstergirls.block.SoakingTubBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5F).sound(SoundType.WOOD).noOcclusion());
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.SparringPostBlock> SPARRING_POST = BLOCKS.registerBlock("sparring_post",
            com.linkyoshi90.monstergirls.block.SparringPostBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5F).sound(SoundType.WOOD).noOcclusion());

    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.BedrollBlock> BEDROLL = BLOCKS.registerBlock("bedroll",
            com.linkyoshi90.monstergirls.block.BedrollBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(0.2F).sound(SoundType.WOOL).noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.FigurineBlock> FIGURINE = BLOCKS.registerBlock("figurine",
            com.linkyoshi90.monstergirls.block.FigurineBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5F).sound(SoundType.WOOD).noOcclusion().noLootTable());

    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.GiftBoxBlock> GIFT_BOX = BLOCKS.registerBlock("gift_box",
            com.linkyoshi90.monstergirls.block.GiftBoxBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(1.0F).sound(SoundType.WOOD).noOcclusion());

    // dairy (entity.Dairy): the churn, the cheese shelf, the milk tank, and the fridge the girls eat from
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.ButterChurnBlock> BUTTER_CHURN = BLOCKS.registerBlock("butter_churn",
            com.linkyoshi90.monstergirls.block.ButterChurnBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5F).sound(SoundType.WOOD).noOcclusion());
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.CheeseShelfBlock> CHEESE_SHELF = BLOCKS.registerBlock("cheese_shelf",
            com.linkyoshi90.monstergirls.block.CheeseShelfBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0F).sound(SoundType.WOOD).noOcclusion());
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.MilkTankBlock> MILK_TANK = BLOCKS.registerBlock("milk_tank",
            com.linkyoshi90.monstergirls.block.MilkTankBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F, 6.0F).sound(SoundType.COPPER).noOcclusion().requiresCorrectToolForDrops());
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.FridgeBlock> FRIDGE = BLOCKS.registerBlock("fridge",
            com.linkyoshi90.monstergirls.block.FridgeBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.SNOW).strength(2.0F, 6.0F).sound(SoundType.METAL).noOcclusion().requiresCorrectToolForDrops());

    /** A box of hay that catches the eggs laid near it (NestingBoxBlock). */
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.NestingBoxBlock> NESTING_BOX = BLOCKS.registerBlock("nesting_box",
            com.linkyoshi90.monstergirls.block.NestingBoxBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0F).sound(SoundType.WOOD).noOcclusion());

    /** The nectar fluid's block (see ModFluids). */
    public static final DeferredBlock<net.minecraft.world.level.block.LiquidBlock> NECTAR = BLOCKS.register("nectar",
            () -> new net.minecraft.world.level.block.LiquidBlock(ModFluids.NECTAR.get(),
                    BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WATER).mapColor(MapColor.COLOR_YELLOW)));

    /** A black swamp mushroom whose spores make friends nearby miss their player (TarShroomBlock). */
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.TarShroomBlock> TAR_SHROOM = BLOCKS.registerBlock("tar_shroom",
            com.linkyoshi90.monstergirls.block.TarShroomBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instabreak().noCollission().randomTicks().sound(SoundType.FUNGUS)
                    .offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY));

    /** Demon Realm grass: grows around a Lilith and spreads in the dark (RealmGrassBlock, entity/DemonRealm). */
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.RealmGrassBlock> REALM_GRASS = BLOCKS.registerBlock("realm_grass",
            com.linkyoshi90.monstergirls.block.RealmGrassBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_MAGENTA).randomTicks().strength(0.6F).sound(SoundType.NYLIUM));
    /** Realm variants (block/RealmGrassBlock#variantFor): the frost realm, the bog realm, realm sand. */
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.RealmGrassBlock.Frost> REALM_FROST = BLOCKS.registerBlock("realm_frost",
            com.linkyoshi90.monstergirls.block.RealmGrassBlock.Frost::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).randomTicks().strength(0.6F).sound(SoundType.NYLIUM));
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.RealmGrassBlock.Bog> REALM_BOG = BLOCKS.registerBlock("realm_bog",
            com.linkyoshi90.monstergirls.block.RealmGrassBlock.Bog::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PURPLE).randomTicks().strength(0.6F).sound(SoundType.MUD));
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.RealmSandBlock> REALM_SAND = BLOCKS.registerBlock("realm_sand",
            com.linkyoshi90.monstergirls.block.RealmSandBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).randomTicks().strength(0.5F).sound(SoundType.SAND));

    /** The bush that bears heartfruit, on realm grass only (HeartfruitBushBlock). */
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.HeartfruitBushBlock> HEARTFRUIT_BUSH = BLOCKS.registerBlock("heartfruit_bush",
            com.linkyoshi90.monstergirls.block.HeartfruitBushBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).randomTicks().noCollission().instabreak().sound(SoundType.SWEET_BERRY_BUSH)
                    .lightLevel(s -> s.getValue(com.linkyoshi90.monstergirls.block.HeartfruitBushBlock.AGE) == 3 ? 5 : 0)
                    .pushReaction(PushReaction.DESTROY));

    /** Leaves the Demon Realm has crept into: they weep violet sap and remember what they were (RealmLeavesBlock). */
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.RealmLeavesBlock> REALM_LEAVES = BLOCKS.registerBlock("realm_leaves",
            com.linkyoshi90.monstergirls.block.RealmLeavesBlock::new,
            BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.OAK_LEAVES).mapColor(MapColor.COLOR_PURPLE));

    /** A trunk the realm has crept up: dark, violet-veined bark that remembers its wood (RealmLogBlock). */
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.RealmLogBlock> REALM_LOG = BLOCKS.registerBlock("realm_log",
            com.linkyoshi90.monstergirls.block.RealmLogBlock::new,
            BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.OAK_LOG).mapColor(MapColor.COLOR_PURPLE).lightLevel(s -> 2).sound(SoundType.STEM));

    /** The weepcap: a pale glowing realm mushroom weeping droplets from its rim (WeepcapBlock). */
    // Demon Realm wood (2026-11-08): the dark oak set in deep purple; it never turns back in the light (tools/gen_realm_wood.py)
    private static BlockBehaviour.Properties realmWood() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASS)
                .strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava();
    }

    public static final DeferredBlock<net.minecraft.world.level.block.Block> REALM_PLANKS = BLOCKS.registerBlock("realm_planks", net.minecraft.world.level.block.Block::new, realmWood());
    public static final DeferredBlock<net.minecraft.world.level.block.StairBlock> REALM_STAIRS = BLOCKS.registerBlock("realm_stairs",
            p -> new net.minecraft.world.level.block.StairBlock(REALM_PLANKS.get().defaultBlockState(), p), realmWood());
    public static final DeferredBlock<net.minecraft.world.level.block.SlabBlock> REALM_SLAB = BLOCKS.registerBlock("realm_slab", net.minecraft.world.level.block.SlabBlock::new, realmWood());
    public static final DeferredBlock<net.minecraft.world.level.block.FenceBlock> REALM_FENCE = BLOCKS.registerBlock("realm_fence", net.minecraft.world.level.block.FenceBlock::new, realmWood());
    public static final DeferredBlock<net.minecraft.world.level.block.FenceGateBlock> REALM_FENCE_GATE = BLOCKS.registerBlock("realm_fence_gate",
            p -> new net.minecraft.world.level.block.FenceGateBlock(net.minecraft.world.level.block.state.properties.WoodType.DARK_OAK, p), realmWood().forceSolidOn());
    public static final DeferredBlock<net.minecraft.world.level.block.DoorBlock> REALM_DOOR = BLOCKS.registerBlock("realm_door",
            p -> new net.minecraft.world.level.block.DoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.DARK_OAK, p), realmWood().strength(3.0F).noOcclusion().pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<net.minecraft.world.level.block.TrapDoorBlock> REALM_TRAPDOOR = BLOCKS.registerBlock("realm_trapdoor",
            p -> new net.minecraft.world.level.block.TrapDoorBlock(net.minecraft.world.level.block.state.properties.BlockSetType.DARK_OAK, p), realmWood().strength(3.0F).noOcclusion()
                    .isValidSpawn((s, l, pos, t) -> false));
    public static final DeferredBlock<net.minecraft.world.level.block.ButtonBlock> REALM_BUTTON = BLOCKS.registerBlock("realm_button",
            p -> new net.minecraft.world.level.block.ButtonBlock(net.minecraft.world.level.block.state.properties.BlockSetType.DARK_OAK, 30, p),
            BlockBehaviour.Properties.of().noCollission().strength(0.5F).pushReaction(PushReaction.DESTROY).sound(SoundType.WOOD));
    public static final DeferredBlock<net.minecraft.world.level.block.PressurePlateBlock> REALM_PRESSURE_PLATE = BLOCKS.registerBlock("realm_pressure_plate",
            p -> new net.minecraft.world.level.block.PressurePlateBlock(net.minecraft.world.level.block.state.properties.BlockSetType.DARK_OAK, p),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).forceSolidOn().noCollission().strength(0.5F).ignitedByLava().pushReaction(PushReaction.DESTROY));

    /** A calcite basin of holy water: the realm can't take hold near it (block/HolyFontBlock). */
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.HolyFontBlock> HOLY_FONT = BLOCKS.registerBlock("holy_font",
            com.linkyoshi90.monstergirls.block.HolyFontBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).requiresCorrectToolForDrops().strength(2.0F).sound(SoundType.CALCITE).lightLevel(s -> 6).noOcclusion());

    /** A lantern burning the realm's violet flame: the realm's air at home, without its ground (block/RealmCenserBlock). */
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.RealmCenserBlock> REALM_CENSER = BLOCKS.registerBlock("realm_censer",
            com.linkyoshi90.monstergirls.block.RealmCenserBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).forceSolidOn().requiresCorrectToolForDrops().strength(3.5F).sound(SoundType.LANTERN)
                    .lightLevel(s -> s.getValue(com.linkyoshi90.monstergirls.block.RealmCenserBlock.LIT) ? 10 : 0).noOcclusion().pushReaction(PushReaction.DESTROY));
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.WeepcapBlock> WEEPCAP = BLOCKS.registerBlock("weepcap",
            com.linkyoshi90.monstergirls.block.WeepcapBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).noCollission().instabreak().randomTicks().sound(SoundType.FUNGUS)
                    .lightLevel(s -> 6).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY));

    /** The exchange liaison's workstation (registry/ModVillagers, entity/Liaison). */
    public static final DeferredBlock<net.minecraft.world.level.block.Block> EXCHANGE_DESK = BLOCKS.registerSimpleBlock("exchange_desk",
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD));

    private ModBlocks() {}
}
