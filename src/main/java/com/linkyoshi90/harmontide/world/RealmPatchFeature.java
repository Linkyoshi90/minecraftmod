package com.linkyoshi90.harmontide.world;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.block.RealmGrassBlock;
import com.linkyoshi90.harmontide.block.RealmLeavesBlock;
import com.linkyoshi90.harmontide.block.RealmLogBlock;
import com.linkyoshi90.harmontide.registry.ModBlocks;
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
        if (!HarmontideConfig.demonRealm()) return false;
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
                if (!RealmGrassBlock.takeable(s)) continue;
                BlockState above = level.getBlockState(ground.above());
                turnTree(level, ground, random);                                  // a trunk standing here turns, whatever the ground does
                if (above.isSolidRender(level, ground.above()) || !above.getFluidState().isEmpty()) continue;
                level.setBlock(ground, RealmGrassBlock.variantFor(level, ground, s, (int) dist), Block.UPDATE_CLIENTS);
                taken++;
                if (above.is(Blocks.SHORT_GRASS) || above.is(Blocks.FERN)) level.setBlock(ground.above(), Blocks.CRIMSON_ROOTS.defaultBlockState(), Block.UPDATE_CLIENTS);
                else if (above.isAir()) {
                    int roll = random.nextInt(30);
                    if (roll < 3) level.setBlock(ground.above(), ModBlocks.WEEPCAP.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                    else if (roll == 3) level.setBlock(ground.above(), ModBlocks.HEARTFRUIT_BUSH.get().defaultBlockState()
                            .setValue(com.linkyoshi90.harmontide.block.HeartfruitBushBlock.AGE, random.nextInt(4)), Block.UPDATE_CLIENTS);
                    else if (roll < 8) level.setBlock(ground.above(), Blocks.CRIMSON_ROOTS.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
        return taken;
    }

    private boolean place(WorldGenLevel level, BlockPos origin, RandomSource random) {
        int taken = placePatch(level, origin, random);
        if (taken > 0 && random.nextInt(RESIDENTS) == 0) settle(level, origin, random);
        if (taken > 0 && random.nextInt(SHRINE) == 0) {
            BlockPos ground = new BlockPos(origin.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.getX(), origin.getZ()) - 1, origin.getZ());
            placeShrine(level, ground, random);
        }
        return taken > 0;
    }

    /** One patch in this many has residents: two possessed pigs or chickens. */
    public static final int RESIDENTS = 3;

    /** Two possessed pigs or chickens on the patch, if the possession option is on. How many settled. */
    public static int settle(WorldGenLevel level, BlockPos origin, RandomSource random) {
        if (!HarmontideConfig.possession()) return 0;
        net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob> type = random.nextBoolean()
                ? net.minecraft.world.entity.EntityType.PIG : net.minecraft.world.entity.EntityType.CHICKEN;
        int n = 0;
        for (int i = 0; i < 2; i++) {
            int x = origin.getX() + random.nextInt(5) - 2, z = origin.getZ() + random.nextInt(5) - 2;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (!RealmGrassBlock.realm(level.getBlockState(new BlockPos(x, y - 1, z)))) continue;
            net.minecraft.world.entity.Mob mob = type.create(level.getLevel());
            if (mob == null) continue;
            mob.moveTo(x + 0.5, y, z + 0.5, random.nextFloat() * 360.0F, 0.0F);
            com.linkyoshi90.harmontide.entity.Monsterization.markPossessed(mob);
            level.addFreshEntityWithPassengers(mob);
            n++;
        }
        return n;
    }

    /** One patch in this many has a shrine at its heart. */
    public static final int SHRINE = 4;
    public static final net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> SHRINE_LOOT =
            net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, com.linkyoshi90.harmontide.HarmontideMod.id("chests/realm_shrine"));

    /**
     * A little shrine of the realm on this ground: a chiseled blackstone stone with a chest on it (the realm's gifts, SHRINE_LOOT),
     * and at the corners blackstone pillars, each with one lit purple candle (dim, so the realm stays). True if it stood.
     */
    public static boolean placeShrine(WorldGenLevel level, BlockPos ground, RandomSource random) {
        if (!level.getBlockState(ground.above()).canBeReplaced() || !level.getBlockState(ground.above(2)).canBeReplaced()) return false;
        level.setBlock(ground, Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        BlockPos chest = ground.above();
        level.setBlock(chest, Blocks.CHEST.defaultBlockState().setValue(net.minecraft.world.level.block.ChestBlock.FACING,
                net.minecraft.core.Direction.Plane.HORIZONTAL.getRandomDirection(random)), Block.UPDATE_CLIENTS);
        net.minecraft.world.RandomizableContainer.setBlockEntityLootTable(level, random, chest, SHRINE_LOOT);
        for (int dx : new int[]{-2, 2}) {
            for (int dz : new int[]{-2, 2}) {
                BlockPos foot = ground.offset(dx, 0, dz);
                for (int dy = 1; dy <= 3; dy++) if (!level.getBlockState(foot.above(dy)).canBeReplaced()) foot = null;
                if (foot == null || !level.getBlockState(foot).isSolid()) continue;
                level.setBlock(foot.above(), Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(foot.above(2), Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(foot.above(3), Blocks.PURPLE_CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.LIT, true), Block.UPDATE_CLIENTS);
            }
        }
        return true;
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
