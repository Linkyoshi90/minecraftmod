package com.linkyoshi90.monstergirls.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A harpy perch (2026-11-08, entity/Colonies): a spruce post with a hay nest on top. A passing flock that comes down within
 * reach of one settles round it for good, a colony of its own.
 */
public class HarpyPerchBlock extends Block {
    public static final MapCodec<HarpyPerchBlock> CODEC = simpleCodec(HarpyPerchBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(Block.box(6, 0, 6, 10, 13, 10), Block.box(2, 13, 2, 14, 16, 14));

    public HarpyPerchBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
