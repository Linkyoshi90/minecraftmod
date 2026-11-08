package com.linkyoshi90.monstergirls.world;

import com.linkyoshi90.monstergirls.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.Map;
import java.util.Optional;

/**
 * A small code-built structure that fits in the chunk it starts in (2026-11-08: the oasis, the sky island, the silk den). Its
 * subclass says how far it spreads from the chunk's middle, how steep the ground may be, and how to build it; one piece type
 * ("one_chunk") serves them all, finding its builder by {@link #kind()}.
 */
@net.neoforged.fml.common.EventBusSubscriber(modid = com.linkyoshi90.monstergirls.MonsterGirlsMod.MODID)
public abstract class OneChunkStructure extends Structure {
    /** A player who finds one: an advancement named after its kind. */
    @net.neoforged.bus.api.SubscribeEvent
    public static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        if (event.getEntity().tickCount % 40 != 19 || !(event.getEntity().level() instanceof net.minecraft.server.level.ServerLevel level)) return;
        var registry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);
        for (String kind : BUILDERS.keySet()) {
            var structure = registry.get(com.linkyoshi90.monstergirls.MonsterGirlsMod.id(kind));
            if (structure != null && level.structureManager().getStructureWithPieceAt(event.getEntity().blockPosition(), structure).isValid()) {
                com.linkyoshi90.monstergirls.entity.Dairy.award(event.getEntity(), kind);
            }
        }
    }

    public interface Builder {
        BlockPos build(WorldGenLevel level, BlockPos origin, RandomSource random, boolean residents);
    }

    public static final Map<String, Builder> BUILDERS = Map.of(
            "oasis", OasisStructure::build,
            "sky_island", SkyIslandStructure::build,
            "silk_den", SilkDenStructure::build);

    protected OneChunkStructure(StructureSettings settings) {
        super(settings);
    }

    protected abstract String kind();

    /** Half its width: the ground is sampled this far out (at most 7, to stay in the chunk). */
    protected abstract int spread();

    /** The most the ground may rise and fall under it. */
    protected abstract int maxSlope();

    /** The origin's height, from the lowest and highest ground under it. */
    protected int originY(GenerationContext context, int lo, int hi) {
        return hi;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX(), z = chunk.getMiddleBlockZ(), r = this.spread();
        var gen = context.chunkGenerator();
        int lo = Integer.MAX_VALUE, hi = Integer.MIN_VALUE;
        for (int dx = -r; dx <= r; dx += r) {
            for (int dz = -r; dz <= r; dz += r) {
                int h = gen.getFirstOccupiedHeight(x + dx, z + dz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
                lo = Math.min(lo, h);
                hi = Math.max(hi, h);
            }
        }
        if (hi - lo > this.maxSlope()) return Optional.empty();
        int ocean = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        if (ocean < gen.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState())) return Optional.empty();
        BlockPos origin = new BlockPos(x, this.originY(context, lo, hi), z);
        long seed = context.random().nextLong();
        String kind = this.kind();
        return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new Piece(kind, origin, seed))));
    }

    public static class Piece extends StructurePiece {
        private final String kind;
        private final BlockPos origin;
        private final long seed;

        public Piece(String kind, BlockPos origin, long seed) {
            super(ModStructures.ONE_CHUNK_PIECE.get(), 0, new BoundingBox(origin.getX() - 7, origin.getY() - 12, origin.getZ() - 7,
                    origin.getX() + 7, origin.getY() + 16, origin.getZ() + 7));
            this.kind = kind;
            this.origin = origin;
            this.seed = seed;
        }

        public Piece(StructurePieceSerializationContext context, CompoundTag tag) {
            super(ModStructures.ONE_CHUNK_PIECE.get(), tag);
            this.kind = tag.getString("Kind");
            this.origin = BlockPos.of(tag.getLong("Origin"));
            this.seed = tag.getLong("Seed");
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
            tag.putString("Kind", this.kind);
            tag.putLong("Origin", this.origin.asLong());
            tag.putLong("Seed", this.seed);
        }

        @Override
        public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator, RandomSource random, BoundingBox box,
                                ChunkPos chunk, BlockPos pivot) {
            Builder b = BUILDERS.get(this.kind);
            if (b == null || !box.isInside(this.origin.getX(), box.minY(), this.origin.getZ())) return;
            b.build(level, this.origin, RandomSource.create(this.seed), true);
        }
    }
}
