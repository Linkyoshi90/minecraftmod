package com.linkyoshi90.monstergirls.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Realm sand (2026-11-08, a realm variant): rose-violet sand glinting with tiny hearts, what the Demon Realm makes of a beach or a
 * desert. It spreads, turns back in torchlight and grows heartfruit just as realm grass does (RealmGrassBlock.realmTick), but no
 * weepcaps or roots: too dry. Falls like sand. Purified, it is sand again (red sand in the badlands).
 */
public class RealmSandBlock extends FallingBlock {
    public static final MapCodec<RealmSandBlock> CODEC = simpleCodec(RealmSandBlock::new);
    public static final int DUST = 0xFFC86AA8;

    public RealmSandBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(RealmGrassBlock.DEPTH, 0));
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    @Override
    public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
        return DUST;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(RealmGrassBlock.DEPTH);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        RealmGrassBlock.realmTick(state, level, pos, random, Blocks.SAND);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(14) == 0 && level.getBlockState(pos.above()).isAir()) {
            level.addParticle(RealmGrassBlock.HAZE, pos.getX() + random.nextDouble(), pos.getY() + 1.1 + random.nextDouble() * 0.5, pos.getZ() + random.nextDouble(), 0.0, 0.02, 0.0);
        }
    }
}
