package com.linkyoshi90.monstergirls.world;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.HarpyEggBlock;
import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.Colonies;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import com.linkyoshi90.monstergirls.registry.ModStructures;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A harpy aerie (2026-11-08): a sky colony on a peak, a windswept hill or a stony shore. Three or four crags of stone rise from
 * the ground, each topped with a nest of hay; on the tallest roosts the colony's matriarch over a clutch of eggs, and two or
 * three more of her kind keep the others. One kind to a colony, by the land: gulls and albatrosses on the shore, condorunes and
 * tengu on the peaks, crows and owls on the windswept hills. Befriend the matriarch and the colony welcomes you
 * (entity/Colonies). /monstergirls locate aerie. Built in code, inside one chunk.
 */
public class HarpyAerieStructure extends Structure {
    public static final MapCodec<HarpyAerieStructure> CODEC = simpleCodec(HarpyAerieStructure::new);
    public static final int SPREAD = 5, FOUNDATION = 10;

    public HarpyAerieStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX(), z = chunk.getMiddleBlockZ();
        var gen = context.chunkGenerator();
        int top = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int ocean = gen.getFirstOccupiedHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        if (ocean < top) return Optional.empty();
        BlockPos origin = new BlockPos(x, top, z);
        long seed = context.random().nextLong();
        return Optional.of(new GenerationStub(origin, builder -> builder.addPiece(new Piece(origin, seed))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.HARPY_AERIE.get();
    }

    public static class Piece extends StructurePiece {
        private final BlockPos origin;
        private final long seed;

        public Piece(BlockPos origin, long seed) {
            super(ModStructures.HARPY_AERIE_PIECE.get(), 0, new BoundingBox(origin.getX() - SPREAD - 1, origin.getY() - FOUNDATION, origin.getZ() - SPREAD - 1,
                    origin.getX() + SPREAD + 1, origin.getY() + 14, origin.getZ() + SPREAD + 1));
            this.origin = origin;
            this.seed = seed;
        }

        public Piece(StructurePieceSerializationContext context, CompoundTag tag) {
            super(ModStructures.HARPY_AERIE_PIECE.get(), tag);
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

    /** The colony's kind, by the land: two to choose from in each. */
    public static List<Supplier<? extends EntityType<? extends AbstractHarpy>>> kinds(WorldGenLevel level, BlockPos at) {
        var biome = level.getBiome(at);
        if (biome.is(Biomes.STONY_SHORE) || biome.is(BiomeTags.IS_BEACH)) return List.of(ModEntities.SEAGULL_HARPY::get, ModEntities.ALBATROSS_HARPY::get);
        if (biome.is(Biomes.JAGGED_PEAKS) || biome.is(Biomes.STONY_PEAKS) || biome.is(Biomes.FROZEN_PEAKS)) return List.of(ModEntities.CONDORUNE_HARPY::get, ModEntities.TENGU_HARPY::get);
        return List.of(ModEntities.CROW_HARPY::get, ModEntities.OWL_HARPY::get);
    }

    /** One crag: a rough stone pillar from solid ground up to {@code topY}, a nest of hay on it. The spot above the nest. */
    private static BlockPos crag(WorldGenLevel level, int x, int z, int topY, RandomSource random) {
        BlockState[] rock = {Blocks.STONE.defaultBlockState(), Blocks.ANDESITE.defaultBlockState(), Blocks.TUFF.defaultBlockState(), Blocks.COBBLESTONE.defaultBlockState()};
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                boolean corner = dx != 0 && dz != 0;
                int ground = level.getHeight(Heightmap.Types.WORLD_SURFACE, x + dx, z + dz);
                int bottom = Math.max(ground - FOUNDATION, ground - 3);
                for (int y = bottom; y < topY; y++) {
                    if (corner && y > topY - 3) continue;                       // a rougher, rounder top
                    level.setBlock(new BlockPos(x + dx, y, z + dz), rock[random.nextInt(rock.length)], Block.UPDATE_CLIENTS);
                }
                level.setBlock(new BlockPos(x + dx, topY, z + dz), corner ? Blocks.SPRUCE_SLAB.defaultBlockState() : Blocks.HAY_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
                for (int y = topY + 1; y <= topY + 3; y++) level.setBlock(new BlockPos(x + dx, y, z + dz), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        return new BlockPos(x, topY + 1, z);
    }

    /** The aerie round {@code origin} (on its ground). The matriarch's nest. */
    public static BlockPos build(WorldGenLevel level, BlockPos origin, RandomSource random, boolean residents) {
        List<BlockPos> nests = new ArrayList<>();
        int ground = origin.getY();
        nests.add(crag(level, origin.getX(), origin.getZ(), ground + 7 + random.nextInt(3), random));      // the matriarch's, tallest
        int[][] spots = {{SPREAD - 1, 1}, {-SPREAD + 1, 2}, {1, SPREAD - 1}, {-2, -SPREAD + 1}};
        int crags = 2 + random.nextInt(2);
        for (int i = 0; i < crags; i++) {
            int x = origin.getX() + spots[i][0], z = origin.getZ() + spots[i][1];
            int g = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            nests.add(crag(level, x, z, Math.max(g, ground) + 3 + random.nextInt(3), random));
        }
        BlockPos matriarchNest = nests.get(0);
        if (!residents) return matriarchNest;
        var kinds = kinds(level, origin);
        EntityType<? extends AbstractHarpy> kind = kinds.get(random.nextInt(kinds.size())).get();
        AbstractHarpy matriarch = Colonies.settle(level, kind, matriarchNest, origin, true);
        for (int i = 1; i < nests.size(); i++) Colonies.settle(level, kind, nests.get(i), origin, false);
        if (matriarch != null) {                                                // her clutch, beside her on the nest
            HarpyEggBlock.Species species = HarpyEggBlock.Species.forEntityId(matriarch.speciesId());
            BlockPos clutch = matriarchNest.east();
            if (species != null && level.getBlockState(clutch.below()).is(Blocks.HAY_BLOCK)) {
                level.setBlock(clutch, ModBlocks.HARPY_EGG.get().defaultBlockState().setValue(HarpyEggBlock.SPECIES, species).setValue(HarpyEggBlock.EGGS, 2), Block.UPDATE_ALL);
            }
        }
        return matriarchNest;
    }
}
