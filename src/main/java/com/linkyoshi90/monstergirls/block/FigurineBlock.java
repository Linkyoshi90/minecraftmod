package com.linkyoshi90.monstergirls.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import javax.annotation.Nullable;

/** A small statue of a friend on a stand, drawn from the sketchbook (FigurineBlockEntity, client/FigurineRenderer). */
public class FigurineBlock extends SimpleTickerBlock {
    public static final MapCodec<FigurineBlock> CODEC = simpleCodec(FigurineBlock::new);

    public FigurineBlock(Properties properties) {
        super(properties, Block.box(3, 0, 3, 13, 12, 13));
        this.registerDefaultState(this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HorizontalDirectionalBlock.FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FigurineBlockEntity(pos, state);
    }

    /** Broken in creative, nothing drops, as with any block. */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.player.Player player) {
        if (player.isCreative() && level.getBlockEntity(pos) instanceof FigurineBlockEntity figurine) figurine.keep = false;
        return super.playerWillDestroy(level, pos, state, player);
    }

    /** She keeps who she shows when the block is broken. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moved) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof FigurineBlockEntity figurine && figurine.keep) Block.popResource(level, pos, figurine.toItem());
        super.onRemove(state, level, pos, next, moved);
    }
}
