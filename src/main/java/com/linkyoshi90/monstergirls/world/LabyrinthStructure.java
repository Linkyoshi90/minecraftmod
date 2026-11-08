package com.linkyoshi90.monstergirls.world;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Dairy;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModStructures;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * A Minoan labyrinth (2026-11-08): under the savanna and the plains, a ring of mossy stone marks a shaft with a ladder down
 * {@link #DEPTH} blocks into a maze of stone bricks, seven cells by seven, different every time, dimly lit by a few soul lanterns.
 * At its heart, a room with a chest (gold, iron, a saddle, golden horse armour, a ball of string...) and the Minoan minotaur who
 * guards it, as her kind always has (strangers on her ground are set upon; befriended, she's free to leave). /monstergirls locate
 * labyrinth. Built in code, 15 by 15, inside one chunk.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public class LabyrinthStructure extends Structure {
    public static final MapCodec<LabyrinthStructure> CODEC = simpleCodec(LabyrinthStructure::new);
    public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE, MonsterGirlsMod.id("chests/labyrinth"));
    public static final int CELLS = 7, HALF = CELLS, DEPTH = 12, WALL = 3;

    public LabyrinthStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX(), z = chunk.getMiddleBlockZ();
        var gen = context.chunkGenerator();
        int top = gen.getFirstOccupiedHeight(x - HALF + 1, z - HALF + 1, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int ocean = gen.getFirstOccupiedHeight(x - HALF + 1, z - HALF + 1, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        if (ocean < top || top - DEPTH < context.heightAccessor().getMinBuildHeight() + 8) return Optional.empty();
        BlockPos origin = new BlockPos(x, top, z);
        long seed = context.random().nextLong();
        return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new Piece(origin, seed))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.LABYRINTH.get();
    }

    public static class Piece extends StructurePiece {
        private final BlockPos origin;
        private final long seed;

        public Piece(BlockPos origin, long seed) {
            super(ModStructures.LABYRINTH_PIECE.get(), 0, new BoundingBox(origin.getX() - HALF, origin.getY() - DEPTH - 1, origin.getZ() - HALF,
                    origin.getX() + HALF, origin.getY() + 3, origin.getZ() + HALF));
            this.origin = origin;
            this.seed = seed;
        }

        public Piece(StructurePieceSerializationContext context, CompoundTag tag) {
            super(ModStructures.LABYRINTH_PIECE.get(), tag);
            this.origin = BlockPos.of(tag.getLong("Origin"));
            this.seed = tag.getLong("Seed");
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            tag.putLong("Origin", this.origin.asLong());
            tag.putLong("Seed", this.seed);
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator, RandomSource random, BoundingBox box,
                                ChunkPos chunk, BlockPos pivot) {
            if (!box.isInside(this.origin.getX(), box.minY(), this.origin.getZ())) return;
            int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, this.origin.getX() - HALF + 1, this.origin.getZ() - HALF + 1);
            build(level, this.origin, surface, RandomSource.create(this.seed), true);
        }
    }

    private static void set(WorldGenLevel level, BlockPos p, BlockState s) {
        level.setBlock(p, s, Block.UPDATE_CLIENTS);
    }

    /** A maze of CELLS by CELLS: open[x][z] on the (2*CELLS+1) grid of blocks, carved from the corner by a random walk. */
    public static boolean[][] maze(RandomSource random) {
        int n = 2 * CELLS + 1;
        boolean[][] open = new boolean[n][n];
        boolean[][] seen = new boolean[CELLS][CELLS];
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{0, 0});
        seen[0][0] = true;
        open[1][1] = true;
        int[][] steps = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!stack.isEmpty()) {
            int[] c = stack.peek();
            List<int[]> next = new ArrayList<>();
            for (int[] s : steps) {
                int x = c[0] + s[0], z = c[1] + s[1];
                if (x >= 0 && z >= 0 && x < CELLS && z < CELLS && !seen[x][z]) next.add(new int[]{x, z});
            }
            if (next.isEmpty()) {
                stack.pop();
                continue;
            }
            Collections.shuffle(next, new java.util.Random(random.nextLong()));
            int[] to = next.get(0);
            seen[to[0]][to[1]] = true;
            open[1 + 2 * to[0]][1 + 2 * to[1]] = true;
            open[1 + c[0] + to[0]][1 + c[1] + to[1]] = true;                  // the wall between them
            stack.push(to);
        }
        for (int x = CELLS - 1; x <= CELLS + 1; x++) for (int z = CELLS - 1; z <= CELLS + 1; z++) open[x][z] = true;   // the room at its heart
        return open;
    }

    /** The labyrinth under {@code origin}, its shaft rising to {@code surface}. The heart's chest. */
    public static BlockPos build(WorldGenLevel level, BlockPos origin, int surface, RandomSource random, boolean guard) {
        int x0 = origin.getX() - HALF, z0 = origin.getZ() - HALF, y0 = origin.getY() - DEPTH;
        boolean[][] open = maze(random);
        BlockState[] walls = {Blocks.STONE_BRICKS.defaultBlockState(), Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), Blocks.CRACKED_STONE_BRICKS.defaultBlockState()};
        int n = 2 * CELLS + 1;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                set(level, new BlockPos(x0 + i, y0, z0 + j), Blocks.POLISHED_ANDESITE.defaultBlockState());
                set(level, new BlockPos(x0 + i, y0 + WALL + 1, z0 + j), Blocks.STONE_BRICKS.defaultBlockState());
                for (int dy = 1; dy <= WALL; dy++) {
                    set(level, new BlockPos(x0 + i, y0 + dy, z0 + j), open[i][j] ? Blocks.AIR.defaultBlockState() : walls[random.nextInt(walls.length)]);
                }
            }
        }
        for (int k = 0; k < 6; k++) {                                          // a few soul lanterns hung from the ceiling
            int i = 1 + 2 * random.nextInt(CELLS), j = 1 + 2 * random.nextInt(CELLS);
            set(level, new BlockPos(x0 + i, y0 + WALL, z0 + j), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }
        // the shaft: up from the first cell to the surface, walled, with a ladder on its north side
        int sx = x0 + 1, sz = z0 + 1;
        for (int y = y0 + 1; y <= surface; y++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    if (y <= y0 + WALL && (dx != 0 || dz != -1)) continue;      // down in the maze, only the ladder's wall
                    BlockPos p = new BlockPos(sx + dx, y, sz + dz);
                    if (y == surface) set(level, p, (dx + dz) % 2 == 0 ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState() : Blocks.CHISELED_STONE_BRICKS.defaultBlockState());   // a ring at the top
                    else set(level, p, Blocks.STONE_BRICKS.defaultBlockState());
                }
            }
            set(level, new BlockPos(sx, y, sz), Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
        }
        // the heart: a chest, a lantern, and its guard
        BlockPos chest = new BlockPos(origin.getX(), y0 + 1, origin.getZ());
        set(level, chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH));
        net.minecraft.world.RandomizableContainer.setBlockEntityLootTable(level, random, chest, LOOT);
        set(level, chest.above(WALL - 1), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        if (guard) RealmManorStructure.settle(level, GirlSpecies.MINOAN_MINOTAUR, chest.south(), chest);
        return chest;
    }

    /** A player inside a labyrinth, and at its heart. */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().tickCount % 40 != 37 || !(event.getEntity().level() instanceof ServerLevel level)) return;
        var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ModStructures.LABYRINTH_KEY);
        if (structure == null) return;
        var start = level.structureManager().getStructureWithPieceAt(event.getEntity().blockPosition(), structure);
        if (!start.isValid()) return;
        Dairy.award(event.getEntity(), "labyrinth");
        BlockPos heart = start.getBoundingBox().getCenter();
        if (Math.abs(event.getEntity().getX() - heart.getX()) <= 2 && Math.abs(event.getEntity().getZ() - heart.getZ()) <= 2
                && event.getEntity().getY() < start.getBoundingBox().minY() + 6) Dairy.award(event.getEntity(), "labyrinth_heart");
    }
}
