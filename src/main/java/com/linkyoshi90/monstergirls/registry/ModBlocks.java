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

    /** An alraune rooted as a flower (she unfolds when it is used or broken). */
    public static final DeferredBlock<com.linkyoshi90.monstergirls.block.AlrauneBloomBlock> ALRAUNE_BLOOM = BLOCKS.registerBlock("alraune_bloom",
            com.linkyoshi90.monstergirls.block.AlrauneBloomBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).instabreak().noCollission().sound(SoundType.AZALEA_LEAVES)
                    .offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY));

    /** The nectar fluid's block (see ModFluids). */
    public static final DeferredBlock<net.minecraft.world.level.block.LiquidBlock> NECTAR = BLOCKS.register("nectar",
            () -> new net.minecraft.world.level.block.LiquidBlock(ModFluids.NECTAR.get(),
                    BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.WATER).mapColor(MapColor.COLOR_YELLOW)));

    private ModBlocks() {}
}
