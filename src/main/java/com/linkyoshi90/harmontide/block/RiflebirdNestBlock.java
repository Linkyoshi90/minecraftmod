package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.registry.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A woven nest a player can set out (in a jungle, ideally). Riflebirds nearby adopt it as their home and lay their
 * unfertilised eggs in it, up to three at a time. Use it with an empty hand to take an egg; she doesn't mind.
 */
public class RiflebirdNestBlock extends Block {
    public static final MapCodec<RiflebirdNestBlock> CODEC = simpleCodec(RiflebirdNestBlock::new);
    public static final int MAX_EGGS = 3;
    public static final IntegerProperty EGGS = IntegerProperty.create("eggs", 0, MAX_EGGS);
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 4.0, 15.0);

    public RiflebirdNestBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(EGGS, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(EGGS);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public static boolean hasRoom(BlockState state) {
        return state.getBlock() instanceof RiflebirdNestBlock && state.getValue(EGGS) < MAX_EGGS;
    }

    /** A riflebird lays one more egg into the nest. */
    public static void addEgg(Level level, BlockPos pos, BlockState state) {
        level.setBlockAndUpdate(pos, state.setValue(EGGS, state.getValue(EGGS) + 1));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        int eggs = state.getValue(EGGS);
        if (eggs == 0) return InteractionResult.PASS;
        if (!level.isClientSide) {
            ItemStack egg = new ItemStack(ModItems.RIFLEBIRD_EGG.get());
            if (!player.getInventory().add(egg)) player.drop(egg, false);
            level.setBlockAndUpdate(pos, state.setValue(EGGS, eggs - 1));
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.4F, 1.4F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
