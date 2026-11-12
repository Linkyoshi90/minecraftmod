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
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * An old graveyard (2026-11-08): in forests, taigas and plains, a plot fenced in dark oak, rows of mossy headstones with
 * candles left burning on some of them, a dead tree, and a little stone crypt at the back with a chest inside. A yurei drifts
 * among the graves with a will-o'-wisp for company; they keep to it until befriended. /harmontide locate graveyard.
 */
public class GraveyardStructure extends OneChunkStructure {
    public static final MapCodec<GraveyardStructure> CODEC = simpleCodec(GraveyardStructure::new);
    public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE, HarmontideMod.id("chests/graveyard"));
    public static final int HALF = 6;

    public GraveyardStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected String kind() {
        return "graveyard";
    }

    @Override
    protected int spread() {
        return HALF;
    }

    @Override
    protected int maxSlope() {
        return 3;
    }

    @Override
    protected int originY(GenerationContext context, int lo, int hi) {
        return hi - 1;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.GRAVEYARD.get();
    }

    private static void set(WorldGenLevel level, BlockPos p, BlockState s) {
        level.setBlock(p, s, Block.UPDATE_CLIENTS);
    }

    /** The graveyard, its ground at {@code origin}'s height. The crypt's chest. */
    public static BlockPos build(WorldGenLevel level, BlockPos origin, RandomSource random, boolean residents) {
        int x0 = origin.getX(), y0 = origin.getY(), z0 = origin.getZ();
        for (int dx = -HALF; dx <= HALF; dx++) {
            for (int dz = -HALF; dz <= HALF; dz++) {
                BlockPos g = new BlockPos(x0 + dx, y0, z0 + dz);
                for (int dy = -1; dy >= -3; dy--) if (level.getBlockState(g.above(dy)).canBeReplaced()) set(level, g.above(dy), Blocks.DIRT.defaultBlockState());
                set(level, g, random.nextInt(4) == 0 ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState());
                for (int dy = 1; dy <= 5; dy++) set(level, g.above(dy), Blocks.AIR.defaultBlockState());
                boolean edge = Math.abs(dx) == HALF || Math.abs(dz) == HALF;
                if (edge && !(dz == -HALF && Math.abs(dx) <= 1)) set(level, g.above(), Blocks.DARK_OAK_FENCE.defaultBlockState());   // a gap for the gate
            }
        }
        // rows of headstones, each over a mound of coarse dirt
        BlockState[] stones = {Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState(), Blocks.STONE_BRICK_WALL.defaultBlockState(), Blocks.MOSSY_COBBLESTONE_WALL.defaultBlockState()};
        for (int dx = -4; dx <= 4; dx += 2) {
            for (int dz = -3; dz <= 1; dz += 4) {
                if (dx == 0) continue;                                          // the path up the middle
                BlockPos head = new BlockPos(x0 + dx, y0 + 1, z0 + dz);
                set(level, head, stones[random.nextInt(stones.length)]);
                set(level, head.south().below(), Blocks.COARSE_DIRT.defaultBlockState());
                set(level, head.south(2).below(), Blocks.COARSE_DIRT.defaultBlockState());
                if (random.nextInt(3) == 0) set(level, head.above(), Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT, true)
                        .setValue(CandleBlock.CANDLES, 1 + random.nextInt(3)));
            }
        }
        for (int dz = -HALF + 1; dz <= 3; dz++) set(level, new BlockPos(x0, y0, z0 + dz), Blocks.GRAVEL.defaultBlockState());
        // a dead tree
        BlockPos tree = new BlockPos(x0 - 4, y0 + 1, z0 + 4);
        for (int dy = 0; dy < 4; dy++) set(level, tree.above(dy), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        set(level, tree.above(3).east(), Blocks.SPRUCE_FENCE.defaultBlockState());
        set(level, tree.above(2).west(), Blocks.SPRUCE_FENCE.defaultBlockState());
        // the crypt at the back: stone bricks, a doorway, a chest
        for (int dx = 1; dx <= 3; dx++) {
            for (int dz = 3; dz <= 5; dz++) {
                for (int dy = 1; dy <= 3; dy++) {
                    boolean wall = dx == 1 || dx == 3 || dz == 5 || dy == 3;
                    set(level, new BlockPos(x0 + dx, y0 + dy, z0 + dz), wall ? (random.nextInt(3) == 0 ? Blocks.CRACKED_STONE_BRICKS : Blocks.STONE_BRICKS).defaultBlockState()
                            : Blocks.AIR.defaultBlockState());
                }
            }
        }
        set(level, new BlockPos(x0 + 2, y0 + 1, z0 + 3), Blocks.AIR.defaultBlockState());
        set(level, new BlockPos(x0 + 2, y0 + 2, z0 + 3), Blocks.AIR.defaultBlockState());
        set(level, new BlockPos(x0 + 2, y0 + 4, z0 + 4), Blocks.STONE_BRICK_SLAB.defaultBlockState());
        BlockPos chest = new BlockPos(x0 + 2, y0 + 1, z0 + 4);
        set(level, chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH));
        net.minecraft.world.RandomizableContainer.setBlockEntityLootTable(level, random, chest, LOOT);
        if (residents) {
            RealmManorStructure.settle(level, GirlSpecies.YUREI_UNDEAD, new BlockPos(x0 - 1, y0 + 1, z0 - 1), new BlockPos(x0, y0 + 1, z0));
            RealmManorStructure.settle(level, GirlSpecies.WILL_O_WISP_UNDEAD, new BlockPos(x0 + 1, y0 + 1, z0 - 4), new BlockPos(x0, y0 + 1, z0));
        }
        return chest;
    }
}
