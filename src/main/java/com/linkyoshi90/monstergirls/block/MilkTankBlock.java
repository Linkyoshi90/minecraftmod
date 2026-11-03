package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.entity.Dairy;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/** The milk tank (MilkTankBlockEntity): pour in by the bucket, fill an empty one from it, or let hoppers do both. Comparators read how full. */
public class MilkTankBlock extends BaseEntityBlock {
    public static final MapCodec<MilkTankBlock> CODEC = simpleCodec(MilkTankBlock::new);
    /** Eighths full, for the milk behind the glass. */
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 8);

    public MilkTankBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MilkTankBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!(level.getBlockEntity(pos) instanceof MilkTankBlockEntity tank) || !Dairy.isMilk(stack) && !stack.is(Items.BUCKET)) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        if (stack.is(Items.BUCKET)) {
            ItemStack milk = tank.draw();
            if (milk.isEmpty()) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, milk));
            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else if (tank.pour(stack, player)) {
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET)));
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else {
            player.displayClientMessage(Component.translatable(tank.amount() >= MilkTankBlockEntity.CAPACITY ? "message.monstergirls.tank_full" : "message.monstergirls.tank_other"), true);
            return ItemInteractionResult.SUCCESS;
        }
        report(tank, player);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof MilkTankBlockEntity tank)) return InteractionResult.PASS;
        if (!level.isClientSide) report(tank, player);
        return InteractionResult.SUCCESS;
    }

    private static void report(MilkTankBlockEntity tank, Player player) {
        Component what = tank.amount() == 0 ? Component.translatable("message.monstergirls.tank_nothing") : tank.kind().getHoverName();
        player.displayClientMessage(Component.translatable("message.monstergirls.tank", tank.amount(), MilkTankBlockEntity.CAPACITY, what, tank.buckets()), true);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MilkTankBlockEntity tank ? tank.comparator() : 0;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MilkTankBlockEntity tank) {   // its own buckets, as full as the milk goes
            int full = Math.min(tank.amount(), tank.buckets()), empty = tank.buckets() - full;
            ItemStack kind = tank.kind();
            if (full > 0) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), kind.copyWithCount(full));
            if (empty > 0) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.BUCKET, empty));
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
