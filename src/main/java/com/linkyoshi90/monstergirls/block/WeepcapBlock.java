package com.linkyoshi90.monstergirls.block;

import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The weepcap: a pale, faintly glowing mushroom of the Demon Realm with a wide drooping bell of a cap, its frilled rim forever weeping
 * cold blue-white droplets. Grows only on realm grass, and spreads slowly over it, as mushrooms do.
 */
public class WeepcapBlock extends BushBlock {
    public static final MapCodec<WeepcapBlock> CODEC = simpleCodec(WeepcapBlock::new);
    /** One random tick of realm grass in this many sprouts a weepcap, unless {@link #CROWD} already grow within three blocks. */
    public static final int SPROUT = 150, CROWD = 3;
    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

    public WeepcapBlock(Properties properties) {
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

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModBlocks.REALM_GRASS.get());
    }

    /** A weepcap on this realm grass, unless the spot is taken or crowded. True if one grew. */
    public static boolean sprout(net.minecraft.world.level.LevelAccessor level, BlockPos ground) {
        BlockPos at = ground.above();
        if (!level.getBlockState(ground).is(ModBlocks.REALM_GRASS.get()) || !level.isEmptyBlock(at)) return false;
        int crowd = 0;
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-3, -1, -3), at.offset(3, 1, 3))) {
            if (level.getBlockState(p).is(ModBlocks.WEEPCAP.get()) && ++crowd >= CROWD) return false;
        }
        level.setBlock(at, ModBlocks.WEEPCAP.get().defaultBlockState(), Block.UPDATE_ALL);
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(30) == 0) sprout(level, pos.offset(random.nextInt(5) - 2, -1 + random.nextInt(3) - 1, random.nextInt(5) - 2));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {                                       // a droplet from somewhere on the rim of her cap
            double a = random.nextDouble() * Math.PI * 2;
            level.addParticle(ParticleTypes.DRIPPING_WATER, pos.getX() + 0.5 + Math.cos(a) * 0.36, pos.getY() + 0.42, pos.getZ() + 0.5 + Math.sin(a) * 0.36, 0, 0, 0);
        }
        if (random.nextInt(10) == 0) {
            level.addParticle(ParticleTypes.GLOW, pos.getX() + 0.2 + random.nextDouble() * 0.6, pos.getY() + 0.5 + random.nextDouble() * 0.4,
                    pos.getZ() + 0.2 + random.nextDouble() * 0.6, 0, 0.005, 0);
        }
    }
}
