package com.linkyoshi90.harpies.registry;

import com.linkyoshi90.harpies.HarpiesMod;
import com.linkyoshi90.harpies.block.RiflebirdNestBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(HarpiesMod.MODID);

    public static final DeferredBlock<RiflebirdNestBlock> RIFLEBIRD_NEST = BLOCKS.registerBlock("riflebird_nest", RiflebirdNestBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(0.5F).sound(SoundType.GRASS).noOcclusion()
                    .pushReaction(PushReaction.DESTROY));

    private ModBlocks() {}
}
