package com.linkyoshi90.monstergirls.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * Geyserite (2026-11-08): the warm, pale mineral crust of a hot spring (entity/HotSprings). Water over it is hot spring water:
 * it steams, never freezes (ice above it melts, snow on it goes), and whoever soaks in it heals. Mined with a pickaxe; lay it
 * under a pool at home and you have an onsen.
 */
public class GeyseriteBlock extends Block {
    public static final MapCodec<GeyseriteBlock> CODEC = simpleCodec(GeyseriteBlock::new);
    /** How high above it the water still counts as warm. */
    public static final int REACH = 3;

    public GeyseriteBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    /** The surface of the water over this block (the first block above that isn't water), or null if there's no water on it. */
    @javax.annotation.Nullable
    public static BlockPos surface(Level level, BlockPos pos) {
        BlockPos p = pos.above();
        if (!level.getFluidState(p).is(Fluids.WATER)) return null;
        for (int i = 0; i < REACH + 2 && level.getFluidState(p).is(Fluids.WATER); i++) p = p.above();
        return p;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockPos top = surface(level, pos);
        if (top == null || random.nextInt(3) != 0 || !level.getBlockState(top).isAir()) return;
        level.addParticle(random.nextInt(4) == 0 ? ParticleTypes.CAMPFIRE_COSY_SMOKE : ParticleTypes.CLOUD,
                top.getX() + random.nextDouble(), top.getY() + 0.05, top.getZ() + random.nextDouble(), 0.0, 0.03 + random.nextDouble() * 0.02, 0.0);
        if (random.nextInt(6) == 0) level.addParticle(ParticleTypes.BUBBLE_COLUMN_UP, pos.getX() + random.nextDouble(), pos.getY() + 1.1, pos.getZ() + random.nextDouble(), 0, 0.05, 0);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    /** Ice over the spring melts and snow on its rim goes: the water stays open all winter. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        thaw(level, pos);
    }

    public static int thaw(Level level, BlockPos pos) {
        int n = 0;
        for (int dy = 1; dy <= REACH + 1; dy++) {
            BlockPos p = pos.above(dy);
            BlockState s = level.getBlockState(p);
            if (s.is(Blocks.ICE) || s.is(Blocks.FROSTED_ICE)) {
                level.setBlockAndUpdate(p, Blocks.WATER.defaultBlockState());
                n++;
            } else if (s.is(Blocks.SNOW)) {
                level.removeBlock(p, false);
                n++;
            } else if (!s.getFluidState().is(Fluids.WATER)) {
                break;
            }
        }
        return n;
    }
}
