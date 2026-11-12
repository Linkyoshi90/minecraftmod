package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.entity.girl.Cradle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A wooden cradle. The little ones nap by it at night and grow faster; rock it with an empty hand (entity/girl/Cradle). */
public class CradleBlock extends HorizontalDirectionalBlock {
    public static final com.mojang.serialization.MapCodec<CradleBlock> CODEC = simpleCodec(CradleBlock::new);
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 10, 15);

    public CradleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level instanceof ServerLevel server) {
            int n = Cradle.rock(server, pos, player);
            player.displayClientMessage(Component.translatable(n > 0 ? "message.harmontide.cradle.rocked" : "message.harmontide.cradle.empty", n), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
