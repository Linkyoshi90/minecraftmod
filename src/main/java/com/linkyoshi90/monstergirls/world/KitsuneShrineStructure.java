package com.linkyoshi90.monstergirls.world;

import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModStructures;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.Optional;

/**
 * A kitsune shrine (2026-11-08): in cherry groves, bamboo jungles and birch woods, a gravel path under three red torii gates to a
 * small dark-oak shrine on a raised floor, stone lanterns along the way, and inside an offering box (block/OfferingBoxBlock):
 * leave food, draw a fortune. A red kitsune shrine maiden keeps it until befriended (RealmManorStructure.keepHome).
 * /monstergirls locate shrine. Built in code, 7 by 15, the path from -z, inside one chunk.
 */
public class KitsuneShrineStructure extends Structure {
    public static final MapCodec<KitsuneShrineStructure> CODEC = simpleCodec(KitsuneShrineStructure::new);
    public static final int HALF_W = 3, PATH_FROM = -7, HALL_FROM = 3, HALL_TO = 7, FOUNDATION = 6;
    public static final int[] TORII = {-6, -3, 0};

    public KitsuneShrineStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX(), z = chunk.getMiddleBlockZ();
        var gen = context.chunkGenerator();
        int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE;
        for (int dx = -HALF_W; dx <= HALF_W; dx += HALF_W) {
            for (int dz = PATH_FROM; dz <= HALL_TO; dz += 7) {
                int h = gen.getFirstOccupiedHeight(x + dx, z + dz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
                lo = Math.min(lo, h);
                hi = Math.max(hi, h);
            }
        }
        if (hi - lo > 4) return Optional.empty();
        int ocean = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        if (ocean < gen.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState())) return Optional.empty();
        BlockPos origin = new BlockPos(x, hi - 1, z);
        long seed = context.random().nextLong();
        return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new Piece(origin, seed))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.KITSUNE_SHRINE.get();
    }

    public static class Piece extends StructurePiece {
        private final BlockPos origin;
        private final long seed;

        public Piece(BlockPos origin, long seed) {
            super(ModStructures.KITSUNE_SHRINE_PIECE.get(), 0, new BoundingBox(origin.getX() - HALF_W - 1, origin.getY() - FOUNDATION, origin.getZ() + PATH_FROM,
                    origin.getX() + HALF_W + 1, origin.getY() + 8, origin.getZ() + HALL_TO));
            this.origin = origin;
            this.seed = seed;
        }

        public Piece(StructurePieceSerializationContext context, CompoundTag tag) {
            super(ModStructures.KITSUNE_SHRINE_PIECE.get(), tag);
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

    private static BlockState stairs(Block b, Direction facing) {
        return b.defaultBlockState().setValue(StairBlock.FACING, facing);
    }

    /** The shrine, its ground at {@code origin}'s height. Where the offering box stands. */
    public static BlockPos build(WorldGenLevel level, BlockPos origin, RandomSource random, boolean maiden) {
        int x0 = origin.getX(), y0 = origin.getY(), z0 = origin.getZ();
        BlockState air = Blocks.AIR.defaultBlockState(), red = Blocks.RED_TERRACOTTA.defaultBlockState(), dark = Blocks.DARK_OAK_PLANKS.defaultBlockState();
        for (int dx = -HALF_W; dx <= HALF_W; dx++) {                          // level ground, cleared
            for (int dz = PATH_FROM; dz <= HALL_TO; dz++) {
                for (int dy = -1; dy >= -FOUNDATION; dy--) {
                    BlockPos p = new BlockPos(x0 + dx, y0 + dy, z0 + dz);
                    BlockState here = level.getBlockState(p);
                    if (!here.canBeReplaced() && !here.is(net.minecraft.tags.BlockTags.LEAVES) && !here.is(net.minecraft.tags.BlockTags.LOGS)) break;
                    set(level, p, Blocks.DIRT.defaultBlockState());
                }
                set(level, new BlockPos(x0 + dx, y0, z0 + dz), Math.abs(dx) <= 1 && dz < HALL_FROM ? Blocks.GRAVEL.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState());
                for (int dy = 1; dy <= 7; dy++) set(level, new BlockPos(x0 + dx, y0 + dy, z0 + dz), air);
            }
        }
        for (int dz : TORII) {                                                   // the torii: red pillars, a red tie beam, a black lintel
            for (int dy = 1; dy <= 4; dy++) {
                set(level, new BlockPos(x0 - 2, y0 + dy, z0 + dz), red);
                set(level, new BlockPos(x0 + 2, y0 + dy, z0 + dz), red);
            }
            for (int dx = -1; dx <= 1; dx++) set(level, new BlockPos(x0 + dx, y0 + 4, z0 + dz), red);
            for (int dx = -2; dx <= 2; dx++) set(level, new BlockPos(x0 + dx, y0 + 5, z0 + dz), Blocks.BLACKSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM));
            set(level, new BlockPos(x0 - 3, y0 + 5, z0 + dz), stairs(Blocks.BLACKSTONE_STAIRS, Direction.EAST));
            set(level, new BlockPos(x0 + 3, y0 + 5, z0 + dz), stairs(Blocks.BLACKSTONE_STAIRS, Direction.WEST));
        }
        for (int dz : new int[]{-5, -2, 1}) {                                    // stone lanterns
            for (int side : new int[]{-3, 3}) {
                set(level, new BlockPos(x0 + side, y0 + 1, z0 + dz), Blocks.STONE_BRICK_WALL.defaultBlockState());
                set(level, new BlockPos(x0 + side, y0 + 2, z0 + dz), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, false));
            }
        }
        // the hall: a raised floor, four posts, a roof, steps up to it
        for (int dx = -HALF_W; dx <= HALF_W; dx++) {
            for (int dz = HALL_FROM; dz <= HALL_TO; dz++) {
                set(level, new BlockPos(x0 + dx, y0 + 1, z0 + dz), dark);
                boolean corner = Math.abs(dx) == HALF_W && (dz == HALL_FROM || dz == HALL_TO);
                if (corner) for (int dy = 2; dy <= 4; dy++) set(level, new BlockPos(x0 + dx, y0 + dy, z0 + dz), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
                else if (dz == HALL_TO && Math.abs(dx) < HALF_W) for (int dy = 2; dy <= 4; dy++) set(level, new BlockPos(x0 + dx, y0 + dy, z0 + dz), dark);   // the back wall
                set(level, new BlockPos(x0 + dx, y0 + 5, z0 + dz), dark);
            }
        }
        for (int dz = HALL_FROM - 1; dz <= HALL_TO; dz++) {                     // a ridged roof with overhangs (none behind: the chunk ends there)
            for (int dx = -HALF_W - 1; dx <= HALF_W + 1; dx++) {
                int ax = Math.abs(dx);
                if (ax == HALF_W + 1) set(level, new BlockPos(x0 + dx, y0 + 5, z0 + dz), stairs(Blocks.DARK_OAK_STAIRS, dx < 0 ? Direction.EAST : Direction.WEST));
                if (ax <= 2 && ax >= 1) set(level, new BlockPos(x0 + dx, y0 + 6, z0 + dz), stairs(Blocks.DARK_OAK_STAIRS, dx < 0 ? Direction.EAST : Direction.WEST));
                if (ax == 0) set(level, new BlockPos(x0 + dx, y0 + 7, z0 + dz), Blocks.DARK_OAK_SLAB.defaultBlockState());
                if (ax == 0) set(level, new BlockPos(x0 + dx, y0 + 6, z0 + dz), dark);
            }
        }
        set(level, new BlockPos(x0, y0 + 1, z0 + HALL_FROM - 1), stairs(Blocks.DARK_OAK_STAIRS, Direction.SOUTH));   // the step up
        BlockPos box = new BlockPos(x0, y0 + 2, z0 + HALL_TO - 1);
        set(level, box, ModBlocks.OFFERING_BOX.get().defaultBlockState());
        set(level, new BlockPos(x0 - 1, y0 + 2, z0 + HALL_TO - 1), Blocks.LANTERN.defaultBlockState());
        set(level, new BlockPos(x0 + 1, y0 + 2, z0 + HALL_TO - 1), Blocks.LANTERN.defaultBlockState());
        set(level, new BlockPos(x0, y0 + 4, z0 + HALL_FROM), Blocks.BELL.defaultBlockState()
                .setValue(net.minecraft.world.level.block.BellBlock.FACING, Direction.NORTH)
                .setValue(net.minecraft.world.level.block.BellBlock.ATTACHMENT, net.minecraft.world.level.block.state.properties.BellAttachType.CEILING));
        if (maiden) RealmManorStructure.settle(level, GirlSpecies.RED_KITSUNE, new BlockPos(x0 + 1, y0 + 2, z0 + HALL_FROM + 1), box);
        return box;
    }
}
