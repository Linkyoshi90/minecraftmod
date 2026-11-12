package com.linkyoshi90.harmontide.world;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Inn;
import com.linkyoshi90.harmontide.registry.ModGirls;
import com.linkyoshi90.harmontide.registry.ModStructures;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Optional;

/**
 * A wayside inn (2026-11-08, entity/girl/Inn): an oak-and-cobblestone common room on the plains and meadows, under a pitched
 * spruce roof. A counter of barrels (one holds the larder), two tables, a fire at the back, two beds for travellers, lanterns. A
 * holstaur keeps it, and girls from far away stop over. /harmontide locate inn. Built in code, 11 by 9, door at -z, one chunk.
 */
public class WaysideInnStructure extends Structure {
    public static final MapCodec<WaysideInnStructure> CODEC = simpleCodec(WaysideInnStructure::new);
    public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE, HarmontideMod.id("chests/wayside_inn"));
    public static final int HALF_W = 5, HALF_L = 4, FOUNDATION = 6;

    public WaysideInnStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX(), z = chunk.getMiddleBlockZ();
        var gen = context.chunkGenerator();
        int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE;
        for (int dx = -HALF_W; dx <= HALF_W; dx += HALF_W) {
            for (int dz = -HALF_L; dz <= HALF_L; dz += HALF_L) {
                int h = gen.getFirstOccupiedHeight(x + dx, z + dz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
                lo = Math.min(lo, h);
                hi = Math.max(hi, h);
            }
        }
        if (hi - lo > 3) return Optional.empty();
        int ocean = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        if (ocean < gen.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState())) return Optional.empty();
        BlockPos origin = new BlockPos(x, hi, z);
        long seed = context.random().nextLong();
        return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new Piece(origin, seed))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.WAYSIDE_INN.get();
    }

    public static class Piece extends StructurePiece {
        private final BlockPos origin;
        private final long seed;

        public Piece(BlockPos origin, long seed) {
            super(ModStructures.WAYSIDE_INN_PIECE.get(), 0, new BoundingBox(origin.getX() - HALF_W - 1, origin.getY() - FOUNDATION, origin.getZ() - HALF_L - 1,
                    origin.getX() + HALF_W + 1, origin.getY() + 9, origin.getZ() + HALF_L + 1));
            this.origin = origin;
            this.seed = seed;
        }

        public Piece(StructurePieceSerializationContext context, CompoundTag tag) {
            super(ModStructures.WAYSIDE_INN_PIECE.get(), tag);
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
            build(level, this.origin, RandomSource.create(this.seed), true);
        }
    }

    private static void set(WorldGenLevel level, BlockPos p, BlockState s) {
        level.setBlock(p, s, Block.UPDATE_CLIENTS);
    }

    /** The inn, its floor at {@code origin}'s height. Where the innkeeper stands. */
    public static BlockPos build(WorldGenLevel level, BlockPos origin, RandomSource random, boolean keeper) {
        int x0 = origin.getX(), y0 = origin.getY(), z0 = origin.getZ();
        BlockState wall = Blocks.OAK_PLANKS.defaultBlockState(), base = Blocks.COBBLESTONE.defaultBlockState(), log = Blocks.STRIPPED_OAK_LOG.defaultBlockState();
        BlockState window = Blocks.GLASS_PANE.defaultBlockState(), floor = Blocks.SPRUCE_PLANKS.defaultBlockState(), air = Blocks.AIR.defaultBlockState();
        for (int dx = -HALF_W; dx <= HALF_W; dx++) {
            for (int dz = -HALF_L; dz <= HALF_L; dz++) {
                for (int dy = -1; dy >= -FOUNDATION; dy--) {
                    BlockPos p = new BlockPos(x0 + dx, y0 + dy, z0 + dz);
                    BlockState here = level.getBlockState(p);
                    if (!here.canBeReplaced() && !here.is(net.minecraft.tags.BlockTags.LEAVES) && !here.is(net.minecraft.tags.BlockTags.LOGS)) break;
                    set(level, p, base);
                }
                set(level, new BlockPos(x0 + dx, y0, z0 + dz), floor);
                boolean edgeX = Math.abs(dx) == HALF_W, edgeZ = Math.abs(dz) == HALF_L;
                for (int dy = 1; dy <= 8; dy++) {
                    BlockPos p = new BlockPos(x0 + dx, y0 + dy, z0 + dz);
                    BlockState s = air;
                    if (dy <= 4 && (edgeX || edgeZ)) {
                        if (edgeX && edgeZ) s = log;
                        else if (dy == 1) s = base;
                        else if (dy >= 2 && dy <= 3 && (edgeZ ? Math.abs(dx) % 3 == 2 : Math.abs(dz) % 3 == 2)) s = window;
                        else s = wall;
                    }
                    int rise = dy - 4;                                         // a pitched roof, ridge along x
                    if (rise >= 1 && Math.abs(dz) == HALF_L + 1 - rise) {
                        s = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, dz < 0 ? Direction.SOUTH : Direction.NORTH);
                    } else if (rise >= 1 && Math.abs(dz) < HALF_L + 1 - rise && edgeX) {
                        s = wall;                                              // the gable ends
                    }
                    if (rise == 5 && dz == 0) s = Blocks.SPRUCE_SLAB.defaultBlockState();
                    set(level, p, s);
                }
            }
        }
        for (int dx = -HALF_W - 1; dx <= HALF_W + 1; dx++) {                   // the eaves over the long walls
            for (int side : new int[]{-1, 1}) {
                set(level, new BlockPos(x0 + dx, y0 + 4, z0 + side * (HALF_L + 1)),
                        Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, side < 0 ? Direction.SOUTH : Direction.NORTH));
            }
        }
        // the door
        BlockState door = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH);
        set(level, new BlockPos(x0, y0 + 1, z0 - HALF_L), door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        set(level, new BlockPos(x0, y0 + 2, z0 - HALF_L), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        // the counter: barrels along the back right, the larder in one
        for (int dx = 1; dx <= 3; dx++) set(level, new BlockPos(x0 + dx, y0 + 1, z0 + 1), Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
        BlockPos larder = new BlockPos(x0 + 1, y0 + 1, z0 + 1);
        net.minecraft.world.RandomizableContainer.setBlockEntityLootTable(level, random, larder, LOOT);
        set(level, new BlockPos(x0 + 3, y0 + 1, z0 + 2), Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP));
        // two tables with stools
        for (int dx : new int[]{-3, -1}) {
            set(level, new BlockPos(x0 + dx, y0 + 1, z0 - 2), Blocks.OAK_FENCE.defaultBlockState());
            set(level, new BlockPos(x0 + dx, y0 + 2, z0 - 2), Blocks.OAK_PRESSURE_PLATE.defaultBlockState());
            set(level, new BlockPos(x0 + dx, y0 + 1, z0 - 3), Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
        }
        // the fire at the back, in a cobblestone hearth
        set(level, new BlockPos(x0 - 2, y0 + 1, z0 + HALF_L - 1), Blocks.CAMPFIRE.defaultBlockState());
        set(level, new BlockPos(x0 - 3, y0 + 1, z0 + HALF_L - 1), base);
        set(level, new BlockPos(x0 - 1, y0 + 1, z0 + HALF_L - 1), base);
        // two beds for travellers along the left wall
        for (int dz : new int[]{0, 2}) {
            BlockState bed = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.WEST);
            set(level, new BlockPos(x0 - HALF_W + 1, y0 + 1, z0 + dz), bed.setValue(BedBlock.PART, BedPart.HEAD));
            set(level, new BlockPos(x0 - HALF_W + 2, y0 + 1, z0 + dz), bed.setValue(BedBlock.PART, BedPart.FOOT));
        }
        set(level, new BlockPos(x0 - 2, y0 + 4, z0), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        set(level, new BlockPos(x0 + 2, y0 + 4, z0), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        BlockPos stand = new BlockPos(x0 + 2, y0 + 1, z0 + 3);                // behind the counter
        if (keeper && ModGirls.type(GirlSpecies.HOLSTAUR_MINOTAUR).create(level.getLevel()) instanceof AbstractMonsterGirl girl) {
            girl.moveTo(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, 180F, 0F);
            girl.finalizeSpawn(level, level.getCurrentDifficultyAt(stand), MobSpawnType.STRUCTURE, null);
            girl.setBaby(false);
            girl.setPersistenceRequired();
            Inn.makeKeeper(girl, new BlockPos(x0, y0 + 1, z0));
            girl.restrictTo(new BlockPos(x0, y0 + 1, z0), Inn.ROOM);
            level.addFreshEntityWithPassengers(girl);
        }
        return stand;
    }
}
