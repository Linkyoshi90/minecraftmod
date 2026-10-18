package com.linkyoshi90.monstergirls.registry;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.AnuranSpawnBlock;
import com.linkyoshi90.monstergirls.block.HarpyEggBlock;
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

    private ModBlocks() {}
}
