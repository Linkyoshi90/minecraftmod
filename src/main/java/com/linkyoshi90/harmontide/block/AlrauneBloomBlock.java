package com.linkyoshi90.harmontide.block;

import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModGirls;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * An alraune rooted in the ground as a flower: dug up with a shovel at high friendship (see Flora#familyInteract),
 * carried as an item and planted wherever you like. Right-click the bloom (or break it) and she unfolds again,
 * with everything she was (bonds, colour, age) kept in the block entity.
 */
public class AlrauneBloomBlock extends BushBlock implements EntityBlock {
    public static final MapCodec<AlrauneBloomBlock> CODEC = simpleCodec(AlrauneBloomBlock::new);
    private static final VoxelShape SHAPE = net.minecraft.world.level.block.Block.box(3.0, 0.0, 3.0, 13.0, 14.0, 13.0);

    public AlrauneBloomBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlrauneBloomBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) level.removeBlock(pos, false);         // onRemove lets her out
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** However the bloom goes (a hand, a pickaxe, a piston, an explosion), she steps out of it: nothing is lost. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel server) {
            CompoundTag girl = level.getBlockEntity(pos) instanceof AlrauneBloomBlockEntity be ? be.getGirl() : null;
            release(server, pos, girl);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /** The alraune unfolds from her bloom (a new one when the bloom carried nobody, e.g. from the creative menu). */
    public static void release(ServerLevel level, BlockPos pos, @Nullable CompoundTag girl) {
        Entity e = ModGirls.type(GirlSpecies.ALRAUNE_FLORA).create(level);
        if (e == null) return;
        if (girl != null) e.load(girl);
        e.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(e);
        level.playSound(null, pos, SoundEvents.AZALEA_LEAVES_BREAK, SoundSource.BLOCKS, 1.0F, 1.2F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 10, 0.4, 0.4, 0.4, 0.0);
    }
}
