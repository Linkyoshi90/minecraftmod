package com.linkyoshi90.harmontide.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A silk ladder (entity/girl/Arachne#spinLadder): strands an arachne strung up a wall, climbable like a ladder (it is in the
 * climbable tag). It dries and blows away {@link SilkBridgeBlock#LASTS} ticks after it was spun, like her bridges.
 */
public class SilkLadderBlock extends LadderBlock {
    public SilkLadderBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (!old.is(this)) level.scheduleTick(pos, this, SilkBridgeBlock.LASTS + level.random.nextInt(200));
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
        level.sendParticles(ParticleTypes.WHITE_ASH, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.2, 0.3, 0.2, 0.01);
    }
}
