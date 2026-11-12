package com.linkyoshi90.harmontide.world;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModStructures;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * A desert oasis (2026-11-08): a pool of clear water ringed with grass, sugar cane and palms, and beside it the broken pillars of
 * a sandstone shrine, an altar with a chest, and the sphinx who keeps it, riddles and all (Centaur's riddles: answer with the
 * right thing in hand). She keeps to it until befriended. /harmontide locate oasis.
 */
public class OasisStructure extends OneChunkStructure {
    public static final MapCodec<OasisStructure> CODEC = simpleCodec(OasisStructure::new);
    public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE, HarmontideMod.id("chests/oasis"));
    public static final int POOL = 3;

    public OasisStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected String kind() {
        return "oasis";
    }

    @Override
    protected int spread() {
        return 7;
    }

    @Override
    protected int maxSlope() {
        return 4;
    }

    @Override
    protected int originY(GenerationContext context, int lo, int hi) {
        return lo - 1;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.OASIS.get();
    }

    private static void set(WorldGenLevel level, BlockPos p, BlockState s) {
        level.setBlock(p, s, Block.UPDATE_CLIENTS);
    }

    private static void palm(WorldGenLevel level, BlockPos foot, RandomSource random) {
        int h = 5 + random.nextInt(2);
        BlockState log = Blocks.JUNGLE_LOG.defaultBlockState(), leaf = Blocks.JUNGLE_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int dy = 0; dy < h; dy++) set(level, foot.above(dy), log);
        BlockPos crown = foot.above(h);
        set(level, crown, leaf);
        for (Direction d : Direction.Plane.HORIZONTAL) {                       // fronds drooping out on four sides
            set(level, crown.relative(d), leaf);
            set(level, crown.relative(d, 2), leaf);
            set(level, crown.relative(d, 3).below(), leaf);
        }
        set(level, crown.above(), leaf);
        set(level, foot.above(h - 1).relative(Direction.NORTH), Blocks.COCOA.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CocoaBlock.FACING, Direction.SOUTH).setValue(net.minecraft.world.level.block.CocoaBlock.AGE, 2));
    }

    /** The oasis, its ground at {@code origin}'s height (the pool sunk into it). The altar's chest. */
    public static BlockPos build(WorldGenLevel level, BlockPos origin, RandomSource random, boolean residents) {
        int x0 = origin.getX(), y0 = origin.getY(), z0 = origin.getZ();
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > 7.2) continue;
                BlockPos g = new BlockPos(x0 + dx, y0, z0 + dz);
                for (int dy = -1; dy >= -4; dy--) if (level.getBlockState(g.above(dy)).canBeReplaced()) set(level, g.above(dy), Blocks.SAND.defaultBlockState());
                for (int dy = 1; dy <= 8; dy++) if (!level.getBlockState(g.above(dy)).isAir()) set(level, g.above(dy), Blocks.AIR.defaultBlockState());
                if (d <= POOL + 0.3) {
                    set(level, g, Blocks.WATER.defaultBlockState());
                    set(level, g.below(), d <= POOL - 1 ? Blocks.WATER.defaultBlockState() : Blocks.SAND.defaultBlockState());
                    set(level, g.below(2), Blocks.SAND.defaultBlockState());
                } else if (d <= POOL + 2.3) {
                    set(level, g, Blocks.GRASS_BLOCK.defaultBlockState());
                    if (random.nextInt(4) == 0) set(level, g.above(), Blocks.SHORT_GRASS.defaultBlockState());
                    if (d <= POOL + 1.3 && random.nextInt(5) == 0) {
                        set(level, g.above(), Blocks.SUGAR_CANE.defaultBlockState());
                        set(level, g.above(2), Blocks.SUGAR_CANE.defaultBlockState());
                    }
                } else {
                    set(level, g, Blocks.SAND.defaultBlockState());
                }
            }
        }
        palm(level, new BlockPos(x0 - 5, y0 + 1, z0 + 1), random);
        palm(level, new BlockPos(x0 + 4, y0 + 1, z0 - 4), random);
        if (random.nextBoolean()) palm(level, new BlockPos(x0 + 1, y0 + 1, z0 + 4), random);
        // the ruined shrine on the east side: four pillars, some broken, round an altar
        for (int[] q : new int[][]{{4, 2}, {6, 2}, {4, 5}, {6, 5}}) {
            int h = 1 + random.nextInt(4);
            for (int dy = 1; dy <= h; dy++) set(level, new BlockPos(x0 + q[0], y0 + dy, z0 + q[1]), dy == h && h >= 3 ? Blocks.CHISELED_SANDSTONE.defaultBlockState() : Blocks.CUT_SANDSTONE.defaultBlockState());
        }
        for (int dx = 4; dx <= 6; dx++) for (int dz = 2; dz <= 5; dz++) set(level, new BlockPos(x0 + dx, y0, z0 + dz), Blocks.SMOOTH_SANDSTONE.defaultBlockState());
        BlockPos chest = new BlockPos(x0 + 5, y0 + 1, z0 + 4);
        set(level, chest.below(), Blocks.CHISELED_SANDSTONE.defaultBlockState());
        set(level, chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST));
        net.minecraft.world.RandomizableContainer.setBlockEntityLootTable(level, random, chest, LOOT);
        if (residents) RealmManorStructure.settle(level, GirlSpecies.SPHINX_CENTAUR, new BlockPos(x0 + 3, y0 + 1, z0 + 3), chest);
        return chest;
    }
}
