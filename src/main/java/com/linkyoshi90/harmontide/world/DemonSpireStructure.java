package com.linkyoshi90.harmontide.world;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Dairy;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.linkyoshi90.harmontide.registry.ModStructures;
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
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LecternBlock;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Optional;

/**
 * The Demon's spire (2026-11-08): where the realm's power sits, a tower of polished blackstone in the badlands, four storeys
 * and a roof. A great hall with two succubi on guard; a library (bookshelves, an enchanting table, a lectern, a chest of
 * books); a bedchamber with two realm censers; a treasury of gold; and on the roof under the open sky, a throne before crying
 * obsidian where the Demon herself waits. A ladder climbs the back wall through every floor. The house keeps to the spire
 * until befriended (RealmManorStructure.keepHome). /harmontide locate spire. Built in code, 15 by 15, inside one chunk.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public class DemonSpireStructure extends Structure {
    public static final MapCodec<DemonSpireStructure> CODEC = simpleCodec(DemonSpireStructure::new);
    public static final ResourceKey<LootTable> LIBRARY = ResourceKey.create(Registries.LOOT_TABLE, HarmontideMod.id("chests/demon_spire_library"));
    public static final ResourceKey<LootTable> TREASURY = ResourceKey.create(Registries.LOOT_TABLE, HarmontideMod.id("chests/demon_spire_treasury"));
    public static final int HALF = 7, STOREY = 7, STOREYS = 4, ROOF = STOREY * STOREYS, FOUNDATION = 8, LADDER_X = 5;

    public DemonSpireStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX(), z = chunk.getMiddleBlockZ();
        var gen = context.chunkGenerator();
        int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE;
        for (int dx = -HALF; dx <= HALF; dx += HALF) {
            for (int dz = -HALF; dz <= HALF; dz += HALF) {
                int h = gen.getFirstOccupiedHeight(x + dx, z + dz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
                lo = Math.min(lo, h);
                hi = Math.max(hi, h);
            }
        }
        if (hi - lo > 6) return Optional.empty();
        int ocean = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int top = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        if (ocean < top) return Optional.empty();
        BlockPos origin = new BlockPos(x, lo, z);
        long seed = context.random().nextLong();
        return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new Piece(origin, seed))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.DEMON_SPIRE.get();
    }

    public static class Piece extends StructurePiece {
        private final BlockPos origin;
        private final long seed;

        public Piece(BlockPos origin, long seed) {
            super(ModStructures.DEMON_SPIRE_PIECE.get(), 0, new BoundingBox(origin.getX() - HALF, origin.getY() - FOUNDATION, origin.getZ() - HALF,
                    origin.getX() + HALF, origin.getY() + ROOF + 4, origin.getZ() + HALF));
            this.origin = origin;
            this.seed = seed;
        }

        public Piece(StructurePieceSerializationContext context, CompoundTag tag) {
            super(ModStructures.DEMON_SPIRE_PIECE.get(), tag);
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
            if (!HarmontideConfig.demonRealm() || !box.isInside(this.origin.getX(), box.minY(), this.origin.getZ())) return;
            build(level, this.origin, RandomSource.create(this.seed), true);
        }
    }

    private static void set(WorldGenLevel level, BlockPos p, BlockState s) {
        level.setBlock(p, s, Block.UPDATE_CLIENTS);
    }

    private static BlockState stairs(Direction facing) {
        return ModBlocks.REALM_STAIRS.get().defaultBlockState().setValue(StairBlock.FACING, facing);
    }

    /** Where the ladder runs: against the back wall, up through every floor. */
    public static boolean ladderColumn(int dx, int dz) {
        return dx == LADDER_X && dz == HALF - 1;
    }

    /** The spire, its ground floor at {@code origin}'s height. The throne's spot on the roof. */
    public static BlockPos build(WorldGenLevel level, BlockPos origin, RandomSource random, boolean residents) {
        int x0 = origin.getX(), y0 = origin.getY(), z0 = origin.getZ();
        BlockState stone = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), floor = ModBlocks.REALM_PLANKS.get().defaultBlockState();
        BlockState pillar = ModBlocks.REALM_LOG.get().defaultBlockState(), window = Blocks.PURPLE_STAINED_GLASS_PANE.defaultBlockState();
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH), air = Blocks.AIR.defaultBlockState();
        for (int dx = -HALF; dx <= HALF; dx++) {
            for (int dz = -HALF; dz <= HALF; dz++) {
                for (int dy = -1; dy >= -FOUNDATION; dy--) {                    // down to solid ground
                    BlockPos p = new BlockPos(x0 + dx, y0 + dy, z0 + dz);
                    if (!level.getBlockState(p).canBeReplaced()) break;
                    set(level, p, stone);
                }
                boolean edgeX = Math.abs(dx) == HALF, edgeZ = Math.abs(dz) == HALF, edge = edgeX || edgeZ;
                for (int dy = 0; dy <= ROOF + 3; dy++) {
                    BlockPos p = new BlockPos(x0 + dx, y0 + dy, z0 + dz);
                    BlockState s = air;
                    int inStorey = dy % STOREY;
                    if (dy < ROOF && edge) {
                        if (edgeX && edgeZ) s = pillar;
                        else if (inStorey == 0) s = stone;
                        else if ((inStorey == 3 || inStorey == 4) && (edgeX ? Math.abs(dz) % 3 == 1 : Math.abs(dx) % 3 == 1)) s = window;
                        else s = stone;
                    } else if (dy < ROOF && inStorey == 0) {
                        s = dy == 0 && dx == 0 ? Blocks.PURPLE_WOOL.defaultBlockState() : floor;
                    } else if (dy == ROOF) {
                        s = stone;                                              // the roof
                    } else if (dy == ROOF + 1 && edge) {
                        s = (dx + dz) % 2 == 0 ? Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState() : air;   // battlements
                    }
                    if (ladderColumn(dx, dz) && dy >= 1 && dy <= ROOF) s = ladder;
                    set(level, p, s);
                }
            }
        }
        BlockState candles = Blocks.PURPLE_CANDLE.defaultBlockState().setValue(CandleBlock.LIT, true).setValue(CandleBlock.CANDLES, 3);
        // the great hall: the door, a runner, candles
        BlockState door = ModBlocks.REALM_DOOR.get().defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH);
        set(level, new BlockPos(x0, y0 + 1, z0 - HALF), door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        set(level, new BlockPos(x0, y0 + 2, z0 - HALF), door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        for (int dz = -HALF + 1; dz <= HALF - 1; dz++) set(level, new BlockPos(x0, y0, z0 + dz), Blocks.PURPLE_WOOL.defaultBlockState());
        for (int d : new int[]{-4, 0, 4}) {
            set(level, new BlockPos(x0 - HALF + 1, y0 + 1, z0 + d), candles);
            set(level, new BlockPos(x0 + HALF - 1, y0 + 1, z0 + d), candles);
        }
        // the library
        int ly = y0 + STOREY;
        for (int dx = -HALF + 1; dx <= HALF - 1; dx++) {
            for (int dz = -HALF + 1; dz <= HALF - 1; dz++) {
                if (Math.abs(dx) != HALF - 1 && Math.abs(dz) != HALF - 1 || ladderColumn(dx, dz) || dx == LADDER_X && dz == HALF - 2) continue;
                for (int dy = 1; dy <= 3; dy++) set(level, new BlockPos(x0 + dx, ly + dy, z0 + dz), Blocks.BOOKSHELF.defaultBlockState());
            }
        }
        set(level, new BlockPos(x0, ly + 1, z0), Blocks.ENCHANTING_TABLE.defaultBlockState());
        set(level, new BlockPos(x0 - 3, ly + 1, z0 + 3), Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.NORTH));
        BlockPos books = new BlockPos(x0 + 3, ly + 1, z0 - 3);
        set(level, books, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST));
        net.minecraft.world.RandomizableContainer.setBlockEntityLootTable(level, random, books, LIBRARY);
        // the bedchamber: a bed, two censers (unlit: a heartfruit lights them), crying obsidian, candles
        int by = y0 + STOREY * 2;
        BlockState bed = Blocks.PURPLE_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH);
        set(level, new BlockPos(x0 - 3, by + 1, z0 + 2), bed.setValue(BedBlock.PART, BedPart.FOOT));
        set(level, new BlockPos(x0 - 3, by + 1, z0 + 1), bed.setValue(BedBlock.PART, BedPart.HEAD));
        set(level, new BlockPos(x0 - 5, by + 1, z0 - 3), ModBlocks.REALM_CENSER.get().defaultBlockState());
        set(level, new BlockPos(x0 + 3, by + 1, z0 - 3), ModBlocks.REALM_CENSER.get().defaultBlockState());
        for (int dy = 1; dy <= 3; dy++) set(level, new BlockPos(x0 - 3, by + dy, z0 - HALF + 1), Blocks.CRYING_OBSIDIAN.defaultBlockState());
        set(level, new BlockPos(x0 - 1, by + 1, z0 + 1), candles);
        // the treasury: gold, amethyst and the chest
        int ty = y0 + STOREY * 3;
        for (int[] g : new int[][]{{-5, -5}, {-5, 3}, {3, -5}, {-4, -5}}) set(level, new BlockPos(x0 + g[0], ty + 1, z0 + g[1]), Blocks.GOLD_BLOCK.defaultBlockState());
        set(level, new BlockPos(x0 + 2, ty + 1, z0 + 2), Blocks.AMETHYST_BLOCK.defaultBlockState());
        set(level, new BlockPos(x0 + 2, ty + 2, z0 + 2), Blocks.AMETHYST_CLUSTER.defaultBlockState());
        BlockPos hoard = new BlockPos(x0, ty + 1, z0);
        set(level, hoard, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH));
        net.minecraft.world.RandomizableContainer.setBlockEntityLootTable(level, random, hoard, TREASURY);
        // the roof: a throne before crying obsidian under the open sky
        int ry = y0 + ROOF;
        BlockPos throne = new BlockPos(x0, ry + 1, z0 + 3);
        set(level, throne, stairs(Direction.SOUTH));
        set(level, throne.west(), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState());
        set(level, throne.east(), Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState());
        set(level, throne.west().above(), candles);
        set(level, throne.east().above(), candles);
        for (int dy = 1; dy <= 3; dy++) set(level, new BlockPos(x0, ry + dy, z0 + 4), Blocks.CRYING_OBSIDIAN.defaultBlockState());
        if (residents) {
            BlockPos hall = new BlockPos(x0, y0 + 1, z0);
            RealmManorStructure.settle(level, GirlSpecies.SUCCUBUS_DEVIL, new BlockPos(x0 - 2, y0 + 1, z0 - 4), hall);
            RealmManorStructure.settle(level, GirlSpecies.SUCCUBUS_DEVIL, new BlockPos(x0 + 2, y0 + 1, z0 - 4), hall);
            RealmManorStructure.settle(level, GirlSpecies.DEMON_DEVIL, throne.north(), throne);
        }
        return throne;
    }

    /** A player who climbs onto the spire's roof. */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().tickCount % 40 != 29 || !(event.getEntity().level() instanceof ServerLevel level)) return;
        var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ModStructures.DEMON_SPIRE_KEY);
        if (structure == null) return;
        var start = level.structureManager().getStructureWithPieceAt(event.getEntity().blockPosition(), structure);
        if (!start.isValid()) return;
        Dairy.award(event.getEntity(), "demon_spire");
        if (event.getEntity().getY() >= start.getBoundingBox().minY() + FOUNDATION + ROOF) Dairy.award(event.getEntity(), "demon_spire_roof");
    }
}
