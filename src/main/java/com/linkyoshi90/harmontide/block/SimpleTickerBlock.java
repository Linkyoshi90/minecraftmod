package com.linkyoshi90.harmontide.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The home blocks with a ticking block entity: drawn from a plain model, with their own shape. */
abstract class SimpleTickerBlock extends BaseEntityBlock {
    private final VoxelShape shape;

    SimpleTickerBlock(Properties properties, VoxelShape shape) {
        super(properties);
        this.shape = shape;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shape;
    }

    /** Mobs path round it (or over it) instead of walking into it: a collision shape short of a full cube otherwise reads as open ground to the pathfinder. */
    @Override
    protected boolean isPathfindable(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.pathfinder.PathComputationType type) {
        return type != net.minecraft.world.level.pathfinder.PathComputationType.LAND && super.isPathfindable(state, type);
    }
}
