package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.entity.HomeLife;
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

/** A standing mirror. Your hand on it, and the nearest friend tries on the next of her colours (HomeLife.mirror). */
public class VanityMirrorBlock extends HorizontalDirectionalBlock {
    public static final com.mojang.serialization.MapCodec<VanityMirrorBlock> CODEC = simpleCodec(VanityMirrorBlock::new);
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    public VanityMirrorBlock(Properties properties) {
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
        if (level instanceof ServerLevel server && !HomeLife.mirror(server, pos, player)) {
            player.displayClientMessage(Component.translatable("message.harmontide.mirror_nobody"), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Mobs path round it (or over it) instead of walking into it: a collision shape short of a full cube otherwise reads as open ground to the pathfinder. */
    @Override
    protected boolean isPathfindable(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.pathfinder.PathComputationType type) {
        return type != net.minecraft.world.level.pathfinder.PathComputationType.LAND && super.isPathfindable(state, type);
    }
}
