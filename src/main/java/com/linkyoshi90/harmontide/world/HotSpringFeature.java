package com.linkyoshi90.harmontide.world;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * A hot spring (2026-11-08, entity/HotSprings): a rounded pool two deep, floored with geyserite, rimmed with calcite and a few
 * stones of tuff, in cold and mountain country. Needs fairly level ground; never on water.
 */
public class HotSpringFeature extends Feature<NoneFeatureConfiguration> {
    public static final int MAX_RADIUS = 4, DEPTH = 2;

    public HotSpringFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        if (!HarmontideConfig.hotSprings()) return false;
        WorldGenLevel level = ctx.level();
        BlockPos o = ctx.origin();
        BlockPos surface = new BlockPos(o.getX(), level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, o.getX(), o.getZ()) - 1, o.getZ());
        return place(level, surface, ctx.random()) > 0;
    }

    /** The spring, its water's surface at {@code surface}'s height. How many water blocks it holds (0: the ground wouldn't do). */
    public static int place(WorldGenLevel level, BlockPos surface, RandomSource random) {
        return place(level, surface, random, Heightmap.Types.WORLD_SURFACE_WG);
    }

    /** As above, reading the ground with this heightmap; null takes the ground as level (for tests, whose heightmaps see the test's roof). */
    public static int place(WorldGenLevel level, BlockPos surface, RandomSource random, @javax.annotation.Nullable Heightmap.Types heights) {
        int r = 3 + random.nextInt(MAX_RADIUS - 2);
        int y = surface.getY();
        for (int dx = -r - 1; heights != null && dx <= r + 1; dx++) {      // level ground, solid, dry
            for (int dz = -r - 1; dz <= r + 1; dz++) {
                int x = surface.getX() + dx, z = surface.getZ() + dz;
                int h = level.getHeight(heights, x, z) - 1;
                if (Math.abs(h - y) > 2) return 0;
                BlockState top = level.getBlockState(new BlockPos(x, h, z));
                if (!top.getFluidState().isEmpty()) return 0;
            }
        }
        int water = 0;
        BlockState geyserite = ModBlocks.GEYSERITE.get().defaultBlockState(), rim = Blocks.CALCITE.defaultBlockState();
        for (int dx = -r - 1; dx <= r + 1; dx++) {
            for (int dz = -r - 1; dz <= r + 1; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                BlockPos col = new BlockPos(surface.getX() + dx, y, surface.getZ() + dz);
                if (d <= r - 0.5) {
                    for (int dy = 1; dy <= 4; dy++) level.setBlock(col.above(dy), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);   // clear what grew there
                    for (int dy = 0; dy > -DEPTH; dy--) {
                        level.setBlock(col.above(dy), Blocks.WATER.defaultBlockState(), Block.UPDATE_CLIENTS);
                        water++;
                    }
                    level.setBlock(col.below(DEPTH), geyserite, Block.UPDATE_CLIENTS);
                    level.setBlock(col.below(DEPTH + 1), geyserite, Block.UPDATE_CLIENTS);
                } else if (d <= r + 0.6) {
                    for (int dy = 1; dy <= 3; dy++) level.setBlock(col.above(dy), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    for (int dy = 0; dy >= -DEPTH; dy--) level.setBlock(col.above(dy), random.nextInt(6) == 0 ? Blocks.TUFF.defaultBlockState() : rim, Block.UPDATE_CLIENTS);
                    if (random.nextInt(5) == 0) level.setBlock(col.above(), Blocks.TUFF.defaultBlockState(), Block.UPDATE_CLIENTS);   // a stone to lean on
                }
            }
        }
        return water;
    }
}
