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

    /** The nectar fluid's block (see ModFluids). */
    public static final DeferredBlock<net.minecraft.world.level.block.LiquidBlock> NECTAR = BLOCKS.register("nectar",
            () -> new net.minecraft.world.level.block.LiquidBlock(ModFluids.NECTAR.get(),
                    BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WATER).mapColor(MapColor.COLOR_YELLOW)));

    private ModBlocks() {}
}
