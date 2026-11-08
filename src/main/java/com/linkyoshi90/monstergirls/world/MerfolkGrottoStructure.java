package com.linkyoshi90.monstergirls.world;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Colonies;
import com.linkyoshi90.monstergirls.entity.Dairy;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
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
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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

import java.util.List;
import java.util.Optional;

/**
 * A merfolk grotto (2026-11-08): on the floor of a warm or lukewarm sea, a dome of prismarine set with sea lanterns, coral
 * growing round its foot and through its arches, a sandy floor and, in the middle, a sunken chest of the sea's treasures. A
 * mermaid queen holds court there with three of the reef's girls; befriend her and her court takes you in (entity/Colonies). They
 * keep to the grotto until befriended (RealmManorStructure.keepHome). /monstergirls locate grotto. Built in code, inside one chunk.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public class MerfolkGrottoStructure extends Structure {
    public static final MapCodec<MerfolkGrottoStructure> CODEC = simpleCodec(MerfolkGrottoStructure::new);
    public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE, MonsterGirlsMod.id("chests/merfolk_grotto"));
    public static final int RADIUS = 6, MIN_DEPTH = 9;
    public static final List<GirlSpecies> QUEENS = List.of(GirlSpecies.DOLPHIN_MERMAID, GirlSpecies.REEFFIN_MERMAID, GirlSpecies.KOI_MERMAID);
    public static final List<GirlSpecies> COURT = List.of(GirlSpecies.SEAHORSE_MERMAID, GirlSpecies.PUFFERFISH_MERMAID, GirlSpecies.OCTOPUS_MOLLUSK,
            GirlSpecies.CUTTLEFISH_MOLLUSK, GirlSpecies.NAUTILUS_MOLLUSK, GirlSpecies.ANEMONE_MOLLUSK);

    public MerfolkGrottoStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX(), z = chunk.getMiddleBlockZ();
        var gen = context.chunkGenerator();
        int floor = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int top = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        if (top - floor < MIN_DEPTH + RADIUS / 2) return Optional.empty();       // deep enough to hold the dome under water
        BlockPos origin = new BlockPos(x, floor, z);
        long seed = context.random().nextLong();
        return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new Piece(origin, seed))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.MERFOLK_GROTTO.get();
    }

    public static class Piece extends StructurePiece {
        private final BlockPos origin;
        private final long seed;

        public Piece(BlockPos origin, long seed) {
            super(ModStructures.MERFOLK_GROTTO_PIECE.get(), 0, new BoundingBox(origin.getX() - RADIUS - 1, origin.getY() - 3, origin.getZ() - RADIUS - 1,
                    origin.getX() + RADIUS + 1, origin.getY() + RADIUS + 2, origin.getZ() + RADIUS + 1));
            this.origin = origin;
            this.seed = seed;
        }

        public Piece(StructurePieceSerializationContext context, CompoundTag tag) {
            super(ModStructures.MERFOLK_GROTTO_PIECE.get(), tag);
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

    private static final Block[] CORAL = {Blocks.TUBE_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK, Blocks.FIRE_CORAL_BLOCK, Blocks.HORN_CORAL_BLOCK};
    private static final Block[] FANS = {Blocks.TUBE_CORAL_WALL_FAN, Blocks.BRAIN_CORAL_WALL_FAN, Blocks.BUBBLE_CORAL_WALL_FAN, Blocks.FIRE_CORAL_WALL_FAN, Blocks.HORN_CORAL_WALL_FAN};
    private static final Block[] PLANTS = {Blocks.TUBE_CORAL, Blocks.BRAIN_CORAL, Blocks.BUBBLE_CORAL, Blocks.FIRE_CORAL, Blocks.HORN_CORAL};

    /** The grotto on the sea floor at {@code origin}, filled with water. The chest's spot. */
    public static BlockPos build(WorldGenLevel level, BlockPos origin, RandomSource random, boolean residents) {
        int x0 = origin.getX(), y0 = origin.getY(), z0 = origin.getZ();
        BlockState water = Blocks.WATER.defaultBlockState();
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                double flat = Math.sqrt(dx * dx + dz * dz);
                if (flat > RADIUS + 0.5) continue;
                set(level, new BlockPos(x0 + dx, y0 - 1, z0 + dz), Blocks.PRISMARINE_BRICKS.defaultBlockState());   // a floor under the sand
                set(level, new BlockPos(x0 + dx, y0, z0 + dz), Blocks.SAND.defaultBlockState());
                for (int dy = 1; dy <= RADIUS + 1; dy++) {
                    double d = Math.sqrt(dx * dx + dz * dz + (dy - 1) * (dy - 1));
                    BlockPos p = new BlockPos(x0 + dx, y0 + dy, z0 + dz);
                    if (d > RADIUS + 0.5) {
                        if (dy <= 2 && flat <= RADIUS + 0.5) set(level, p, water);   // clear round the foot
                        continue;
                    }
                    boolean shell = d > RADIUS - 0.6;
                    boolean arch = shell && dy <= 3 && (Math.abs(dx) <= 1 || Math.abs(dz) <= 1);   // four doorways
                    boolean lantern = shell && dy == 4 && (dx + dz) % 3 == 0;
                    BlockState s = water;
                    if (shell && !arch) s = lantern ? Blocks.SEA_LANTERN.defaultBlockState()
                            : (dx * 31 + dy * 17 + dz * 7) % 5 == 0 ? Blocks.DARK_PRISMARINE.defaultBlockState() : Blocks.PRISMARINE.defaultBlockState();
                    set(level, p, s);
                }
            }
        }
        // coral round the foot, through the doorways and on the walls inside
        for (int i = 0; i < 24; i++) {
            double a = random.nextDouble() * Math.PI * 2, r = RADIUS - 1.5 - random.nextDouble() * 2;
            BlockPos p = new BlockPos(x0 + (int) Math.round(Math.cos(a) * r), y0 + 1, z0 + (int) Math.round(Math.sin(a) * r));
            if (!level.getBlockState(p).is(Blocks.WATER)) continue;
            int k = random.nextInt(CORAL.length);
            if (random.nextInt(3) == 0) {
                set(level, p, CORAL[k].defaultBlockState());
                if (level.getBlockState(p.above()).is(Blocks.WATER)) set(level, p.above(), PLANTS[random.nextInt(PLANTS.length)].defaultBlockState());
            } else {
                set(level, p, PLANTS[k].defaultBlockState());
            }
        }
        for (Direction d : Direction.Plane.HORIZONTAL) {                       // fans on the inner walls
            BlockPos p = new BlockPos(x0, y0 + 3, z0).relative(d, RADIUS - 2);
            if (level.getBlockState(p.relative(d)).is(Blocks.PRISMARINE) && level.getBlockState(p).is(Blocks.WATER)) {
                set(level, p, FANS[random.nextInt(FANS.length)].defaultBlockState().setValue(BaseCoralWallFanBlock.FACING, d.getOpposite()));
            }
        }
        for (int[] q : new int[][]{{2, 2}, {-2, -2}, {2, -3}, {-3, 2}}) {     // sea pickles to light the floor
            set(level, new BlockPos(x0 + q[0], y0 + 1, z0 + q[1]), Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 3));
        }
        BlockPos chest = new BlockPos(x0, y0 + 1, z0);
        set(level, chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH).setValue(BlockStateProperties.WATERLOGGED, true));
        net.minecraft.world.RandomizableContainer.setBlockEntityLootTable(level, random, chest, LOOT);
        if (residents) {
            AbstractMonsterGirl queen = RealmManorStructure.settle(level, QUEENS.get(random.nextInt(QUEENS.size())), new BlockPos(x0, y0 + 2, z0 + 2), chest);
            if (queen != null) Colonies.mark(queen, origin, true, "entity.monstergirls.grotto_queen");
            for (int i = 0; i < 3; i++) {
                double a = i * Math.PI * 2 / 3;
                BlockPos at = new BlockPos(x0 + (int) Math.round(Math.cos(a) * 3), y0 + 2, z0 + (int) Math.round(Math.sin(a) * 3));
                AbstractMonsterGirl g = RealmManorStructure.settle(level, COURT.get(random.nextInt(COURT.size())), at, chest);
                if (g != null) Colonies.mark(g, origin, false);
            }
        }
        return chest;
    }

    /** A player who swims into a grotto. */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().tickCount % 40 != 31 || !(event.getEntity().level() instanceof ServerLevel level)) return;
        var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ModStructures.MERFOLK_GROTTO_KEY);
        if (structure != null && level.structureManager().getStructureWithPieceAt(event.getEntity().blockPosition(), structure).isValid()) {
            Dairy.award(event.getEntity(), "merfolk_grotto_found");
        }
    }
}
