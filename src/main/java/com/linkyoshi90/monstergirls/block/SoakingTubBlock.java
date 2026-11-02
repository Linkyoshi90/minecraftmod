package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;

import javax.annotation.Nullable;

/** A wooden tub: fill it with a water bucket and stand in it (SoakingTubBlockEntity). Clothes stay on; it's a soak, not a scene. */
public class SoakingTubBlock extends SimpleTickerBlock {
    public static final MapCodec<SoakingTubBlock> CODEC = simpleCodec(SoakingTubBlock::new);
    public static final BooleanProperty FILLED = BooleanProperty.create("filled");

    public SoakingTubBlock(Properties properties) {
        super(properties, Shapes.join(Block.box(0, 0, 0, 16, 8, 16), Block.box(2, 2, 2, 14, 8, 14), net.minecraft.world.phys.shapes.BooleanOp.ONLY_FIRST));
        this.registerDefaultState(this.stateDefinition.any().setValue(FILLED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FILLED);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SoakingTubBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntities.SOAKING_TUB.get(), SoakingTubBlockEntity::serverTick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        boolean filled = state.getValue(FILLED);
        if (stack.is(Items.WATER_BUCKET) && !filled) {
            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, state.setValue(FILLED, true));
                if (!player.getAbilities().instabuild) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stack.is(Items.BUCKET) && filled) {
            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, state.setValue(FILLED, false));
                if (!player.getAbilities().instabuild) player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
