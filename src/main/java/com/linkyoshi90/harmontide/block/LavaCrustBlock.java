package com.linkyoshi90.harmontide.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Lava crust (entity/Workshop#crustPath): a fire kind's friend walks on lava where she has cooled it, for a few seconds. Then it
 * melts back into the lava source it was ({@link #LASTS} ticks and a little), so nothing is ever taken from the world.
 */
public class LavaCrustBlock extends Block {
    public static final int LASTS = 120;

    public LavaCrustBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (!old.is(this)) level.scheduleTick(pos, this, LASTS + level.random.nextInt(60));
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlockAndUpdate(pos, Blocks.LAVA.defaultBlockState());
        level.sendParticles(ParticleTypes.LAVA, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 2, 0.3, 0.0, 0.3, 0.0);
    }
}
