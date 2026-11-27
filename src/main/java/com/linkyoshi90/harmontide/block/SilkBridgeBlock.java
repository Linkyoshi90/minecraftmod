package com.linkyoshi90.harmontide.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A silk bridge (entity/girl/Arachne#spinBridge): a sheet of spider silk strung across a gap, thin as a carpet and just as
 * walkable. It dries and blows away {@link #LASTS} ticks after it was spun; breaking it gives nothing back.
 */
public class SilkBridgeBlock extends Block {
    /** Five minutes. */
    public static final int LASTS = 6000;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 1, 16);

    public SilkBridgeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        if (!old.is(this)) level.scheduleTick(pos, this, LASTS + level.random.nextInt(200));   // not all at once: it frays from strand to strand
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.removeBlock(pos, false);
        level.sendParticles(ParticleTypes.WHITE_ASH, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, 3, 0.3, 0.05, 0.3, 0.01);
    }
}
