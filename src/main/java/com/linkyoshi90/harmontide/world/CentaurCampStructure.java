package com.linkyoshi90.harmontide.world;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModStructures;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * A centaur camp (2026-11-08): out on the plains and the savanna, three wool tents round a campfire with log benches, hay
 * for the herd and a chest of camp supplies. A band of three centaurs (horses on the plains, zebras on the savanna) keeps to it
 * until befriended. /harmontide locate camp.
 */
public class CentaurCampStructure extends OneChunkStructure {
    public static final MapCodec<CentaurCampStructure> CODEC = simpleCodec(CentaurCampStructure::new);
    public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE, HarmontideMod.id("chests/centaur_camp"));
    public static final int BAND = 3;

    public CentaurCampStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected String kind() {
        return "centaur_camp";
    }

    @Override
    protected int spread() {
        return 6;
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
        return ModStructures.CENTAUR_CAMP.get();
    }

    private static void set(WorldGenLevel level, BlockPos p, BlockState s) {
        level.setBlock(p, s, Block.UPDATE_CLIENTS);
    }

    /** An A-frame tent of wool, three long, its ridge along z, open at both ends. */
    private static void tent(WorldGenLevel level, BlockPos at, BlockState cloth) {
        for (int dz = -1; dz <= 1; dz++) {
            set(level, at.offset(-1, 1, dz), cloth);
            set(level, at.offset(1, 1, dz), cloth);
            set(level, at.offset(0, 2, dz), cloth);
            set(level, at.offset(0, 1, dz), Blocks.AIR.defaultBlockState());
            set(level, at.offset(0, 0, dz), Blocks.COARSE_DIRT.defaultBlockState());   // a trodden floor
        }
    }

    /** The camp, its ground at {@code origin}'s height. The supplies chest. */
    public static BlockPos build(WorldGenLevel level, BlockPos origin, RandomSource random, boolean residents) {
        int x0 = origin.getX(), y0 = origin.getY(), z0 = origin.getZ();
        for (int dx = -6; dx <= 6; dx++) {                                       // level the ground, clear the grass
            for (int dz = -6; dz <= 6; dz++) {
                if (dx * dx + dz * dz > 40) continue;
                BlockPos g = new BlockPos(x0 + dx, y0, z0 + dz);
                for (int dy = -1; dy >= -3; dy--) if (level.getBlockState(g.above(dy)).canBeReplaced()) set(level, g.above(dy), Blocks.DIRT.defaultBlockState());
                if (level.getBlockState(g).canBeReplaced()) set(level, g, Blocks.GRASS_BLOCK.defaultBlockState());
                for (int dy = 1; dy <= 4; dy++) set(level, g.above(dy), Blocks.AIR.defaultBlockState());
            }
        }
        set(level, new BlockPos(x0, y0 + 1, z0), Blocks.CAMPFIRE.defaultBlockState());
        set(level, new BlockPos(x0, y0, z0), Blocks.COBBLESTONE.defaultBlockState());
        BlockState bench = Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        set(level, new BlockPos(x0 - 1, y0 + 1, z0 - 2), bench);
        set(level, new BlockPos(x0 + 1, y0 + 1, z0 - 2), bench);
        set(level, new BlockPos(x0 - 2, y0 + 1, z0), Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
        tent(level, new BlockPos(x0 - 4, y0, z0 + 3), Blocks.WHITE_WOOL.defaultBlockState());
        tent(level, new BlockPos(x0 + 4, y0, z0 + 3), Blocks.BROWN_WOOL.defaultBlockState());
        tent(level, new BlockPos(x0 + 4, y0, z0 - 3), Blocks.WHITE_WOOL.defaultBlockState());
        set(level, new BlockPos(x0 - 4, y0 + 1, z0 - 3), Blocks.HAY_BLOCK.defaultBlockState());
        set(level, new BlockPos(x0 - 5, y0 + 1, z0 - 3), Blocks.HAY_BLOCK.defaultBlockState());
        set(level, new BlockPos(x0 - 4, y0 + 2, z0 - 3), Blocks.HAY_BLOCK.defaultBlockState());
        BlockPos chest = new BlockPos(x0 - 4, y0 + 1, z0 + 2);
        set(level, chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH));
        net.minecraft.world.RandomizableContainer.setBlockEntityLootTable(level, random, chest, LOOT);
        if (residents) {
            boolean savanna = level.getBiome(origin).is(BiomeTags.IS_SAVANNA);
            for (int i = 0; i < BAND; i++) {
                GirlSpecies kind = savanna && i > 0 ? GirlSpecies.ZEBRA_CENTAUR : GirlSpecies.HORSE_CENTAUR;
                double a = i * Math.PI * 2 / BAND;
                RealmManorStructure.settle(level, kind, new BlockPos(x0 + (int) Math.round(Math.cos(a) * 3), y0 + 1, z0 + (int) Math.round(Math.sin(a) * 3)),
                        new BlockPos(x0, y0 + 1, z0));
            }
        }
        return chest;
    }
}
