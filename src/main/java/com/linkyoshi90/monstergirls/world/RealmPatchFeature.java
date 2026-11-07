package com.linkyoshi90.monstergirls.world;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.block.RealmGrassBlock;
import com.linkyoshi90.monstergirls.block.RealmLeavesBlock;
import com.linkyoshi90.monstergirls.block.RealmLogBlock;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * A wild patch of the Demon Realm, placed now and then in dark forests and swamps when the world is made: a disc of realm grass (its
 * depth rising towards the rim, so it grows a little and then stops, RealmGrassBlock#DEPTH), the trees over it turned (logs, leaves,
 * weeping vines), crimson roots in place of grass tufts, weepcaps and the odd heartfruit bush. So the realm can be found without
 * meeting a Lilith. Off with the demonRealm option.
 */
public class RealmPatchFeature extends Feature<NoneFeatureConfiguration> {
    public RealmPatchFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        if (!MonsterGirlsConfig.demonRealm()) return false;
        return place(ctx.level(), ctx.origin(), ctx.random());
    }

    /** The patch round a spot, on the surface. How many blocks of ground it took. */
    public static int placePatch(WorldGenLevel level, BlockPos origin, RandomSource random) {
        return placePatch(level, origin, random, (x, z) -> level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1);
    }

    /** The patch round a spot, with the ground's height at each column given (tests under a ceiling give their floor). */
    public static int placePatch(WorldGenLevel level, BlockPos origin, RandomSource random, java.util.function.IntBinaryOperator groundY) {
        int radius = 4 + random.nextInt(3), taken = 0;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > radius + random.nextDouble() * 0.8) continue;
                int x = origin.getX() + dx, z = origin.getZ() + dz;
                BlockPos ground = new BlockPos(x, groundY.applyAsInt(x, z), z);
                BlockState s = level.getBlockState(ground);
                if (!(s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT) || s.is(Blocks.PODZOL) || s.is(Blocks.COARSE_DIRT))) continue;
                BlockState above = level.getBlockState(ground.above());
                turnTree(level, ground, random);                                  // a trunk standing here turns, whatever the ground does
                if (above.isSolidRender(level, ground.above()) || !above.getFluidState().isEmpty()) continue;
                level.setBlock(ground, ModBlocks.REALM_GRASS.get().defaultBlockState().setValue(RealmGrassBlock.DEPTH, Math.min(RealmGrassBlock.MAX_DEPTH, (int) dist)), Block.UPDATE_CLIENTS);
                taken++;
                if (above.is(Blocks.SHORT_GRASS) || above.is(Blocks.FERN)) level.setBlock(ground.above(), Blocks.CRIMSON_ROOTS.defaultBlockState(), Block.UPDATE_CLIENTS);
                else if (above.isAir()) {
                    int roll = random.nextInt(30);
                    if (roll < 3) level.setBlock(ground.above(), ModBlocks.WEEPCAP.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                    else if (roll == 3) level.setBlock(ground.above(), ModBlocks.HEARTFRUIT_BUSH.get().defaultBlockState()
                            .setValue(com.linkyoshi90.monstergirls.block.HeartfruitBushBlock.AGE, random.nextInt(4)), Block.UPDATE_CLIENTS);
                    else if (roll < 8) level.setBlock(ground.above(), Blocks.CRIMSON_ROOTS.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
        return taken;
    }

    private boolean place(WorldGenLevel level, BlockPos origin, RandomSource random) {
        return placePatch(level, origin, random) > 0;
    }

    /** The column above a patch of ground: its logs and leaves turned, vines under the leaves. */
    private static void turnTree(WorldGenLevel level, BlockPos ground, RandomSource random) {
        for (int dy = 1; dy <= 28; dy++) {
            BlockPos p = ground.above(dy);
            BlockState s = level.getBlockState(p);
            RealmLogBlock.Kind log = RealmLogBlock.Kind.of(s);
            if (log != null) {
                level.setBlock(p, ModBlocks.REALM_LOG.get().defaultBlockState().setValue(RealmLogBlock.KIND, log)
                        .setValue(RotatedPillarBlock.AXIS, s.getValue(RotatedPillarBlock.AXIS)), Block.UPDATE_CLIENTS);
                continue;
            }
            RealmLeavesBlock.Kind leaf = RealmLeavesBlock.Kind.of(s);
            if (leaf != null) {
                level.setBlock(p, ModBlocks.REALM_LEAVES.get().defaultBlockState().setValue(RealmLeavesBlock.KIND, leaf)
                        .setValue(LeavesBlock.DISTANCE, s.getValue(LeavesBlock.DISTANCE)).setValue(LeavesBlock.PERSISTENT, s.getValue(LeavesBlock.PERSISTENT))
                        .setValue(LeavesBlock.WATERLOGGED, s.getValue(LeavesBlock.WATERLOGGED)), Block.UPDATE_CLIENTS);
                if (random.nextInt(10) == 0 && level.isEmptyBlock(p.below()) && level.isEmptyBlock(p.below(2))) {
                    level.setBlock(p.below(), Blocks.WEEPING_VINES.defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, 22), Block.UPDATE_CLIENTS);
                }
            }
        }
    }
}
