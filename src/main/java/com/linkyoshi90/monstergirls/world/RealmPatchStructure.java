package com.linkyoshi90.monstergirls.world;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.registry.ModStructures;
import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.Optional;

/**
 * A wild patch of the Demon Realm as a structure (2026-11-08), so /locate structure monstergirls:realm_patch (and
 * /monstergirls locate realm) can find one. It places what RealmPatchFeature places, at the middle of its chunk: the patch
 * (radius under seven, so it never leaves the chunk), the trees over it turned, sometimes residents and a shrine. Where and how
 * often: data/monstergirls/worldgen/structure_set/realm_patch.json; in which biomes: the has_structure/realm_patch tag.
 * It is placed after the trees (step top_layer_modification), so they are there to turn.
 */
public class RealmPatchStructure extends Structure {
    public static final MapCodec<RealmPatchStructure> CODEC = simpleCodec(RealmPatchStructure::new);

    public RealmPatchStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX(), z = chunk.getMiddleBlockZ();
        int y = context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        BlockPos origin = new BlockPos(x, y, z);
        long seed = context.random().nextLong();
        return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new Piece(origin, seed))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.REALM_PATCH.get();
    }

    /** The one piece: the patch round its origin, laid when its chunk is decorated. */
    public static class Piece extends StructurePiece {
        private final BlockPos origin;
        private final long seed;

        public Piece(BlockPos origin, long seed) {
            super(ModStructures.REALM_PATCH_PIECE.get(), 0,
                    new BoundingBox(origin.getX() - 7, origin.getY() - 12, origin.getZ() - 7, origin.getX() + 7, origin.getY() + 30, origin.getZ() + 7));
            this.origin = origin;
            this.seed = seed;
        }

        public Piece(StructurePieceSerializationContext context, CompoundTag tag) {
            super(ModStructures.REALM_PATCH_PIECE.get(), tag);
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
            if (!MonsterGirlsConfig.demonRealm() || !box.isInside(this.origin.getX(), box.minY(), this.origin.getZ())) return;
            build(level, this.origin, RandomSource.create(this.seed));
        }

        /** The patch, maybe residents, maybe a shrine. How many blocks of ground it took. */
        public static int build(WorldGenLevel level, BlockPos origin, RandomSource random) {
            int taken = RealmPatchFeature.placePatch(level, origin, random);
            if (taken > 0 && random.nextInt(RealmPatchFeature.RESIDENTS) == 0) RealmPatchFeature.settle(level, origin, random);
            if (taken > 0 && random.nextInt(RealmPatchFeature.SHRINE) == 0) {
                BlockPos ground = new BlockPos(origin.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.getX(), origin.getZ()) - 1, origin.getZ());
                RealmPatchFeature.placeShrine(level, ground, random);
            }
            return taken;
        }
    }
}
