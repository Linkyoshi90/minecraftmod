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
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * A sky island (2026-11-08): high over windswept hills and meadows, {@link #HEIGHT} blocks above the ground, a floating island of
 * grass and stone with a birch, wildflowers, a chest, a cloud drifting at its edge, and vines trailing from its underside. A sylph
 * lives there and keeps to it until befriended. Getting up is the player's business. /harmontide locate island.
 */
public class SkyIslandStructure extends OneChunkStructure {
    public static final MapCodec<SkyIslandStructure> CODEC = simpleCodec(SkyIslandStructure::new);
    public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE, HarmontideMod.id("chests/sky_island"));
    public static final int RADIUS = 6, HEIGHT = 45, DEPTH = 7;

    public SkyIslandStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected String kind() {
        return "sky_island";
    }

    @Override
    protected int spread() {
        return 6;
    }

    @Override
    protected int maxSlope() {
        return 40;
    }

    @Override
    protected int originY(GenerationContext context, int lo, int hi) {
        return Math.min(hi + HEIGHT, context.heightAccessor().getMaxBuildHeight() - 20);
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.SKY_ISLAND.get();
    }

    private static void set(WorldGenLevel level, BlockPos p, BlockState s) {
        level.setBlock(p, s, Block.UPDATE_CLIENTS);
    }

    /** The island, its grass at {@code origin}'s height. Its chest. */
    public static BlockPos build(WorldGenLevel level, BlockPos origin, RandomSource random, boolean residents) {
        int x0 = origin.getX(), y0 = origin.getY(), z0 = origin.getZ();
        BlockState[] under = {Blocks.STONE.defaultBlockState(), Blocks.STONE.defaultBlockState(), Blocks.ANDESITE.defaultBlockState(), Blocks.TUFF.defaultBlockState(),
                Blocks.COAL_ORE.defaultBlockState(), Blocks.IRON_ORE.defaultBlockState()};
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz) + random.nextDouble() * 0.6;
                if (d > RADIUS) continue;
                int depth = (int) Math.round((RADIUS - d) / RADIUS * DEPTH) + 1;  // a cone underneath, deepest at the middle
                for (int dy = 0; dy < depth; dy++) {
                    BlockPos p = new BlockPos(x0 + dx, y0 - dy, z0 + dz);
                    set(level, p, dy == 0 ? Blocks.GRASS_BLOCK.defaultBlockState() : dy <= 2 ? Blocks.DIRT.defaultBlockState() : under[random.nextInt(under.length)]);
                }
                BlockPos top = new BlockPos(x0 + dx, y0 + 1, z0 + dz);
                if (d < RADIUS - 1 && random.nextInt(6) == 0) {
                    Block[] flowers = {Blocks.DANDELION, Blocks.POPPY, Blocks.CORNFLOWER, Blocks.OXEYE_DAISY, Blocks.AZURE_BLUET};
                    set(level, top, flowers[random.nextInt(flowers.length)].defaultBlockState());
                } else if (random.nextInt(4) == 0) {
                    set(level, top, Blocks.SHORT_GRASS.defaultBlockState());
                }
                if (d > RADIUS - 1.5 && random.nextInt(4) == 0) {                // vines trailing from the rim
                    for (Direction side : Direction.Plane.HORIZONTAL) {
                        BlockPos hang = new BlockPos(x0 + dx, y0 - 1, z0 + dz).relative(side);
                        if (!level.getBlockState(hang).isAir()) continue;
                        int len = 3 + random.nextInt(8);
                        for (int i = 0; i < len && level.getBlockState(hang.below(i)).isAir(); i++) {
                            set(level, hang.below(i), Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(side.getOpposite()), true));
                        }
                        break;
                    }
                }
            }
        }
        // a birch
        BlockPos trunk = new BlockPos(x0 - 2, y0 + 1, z0 - 1);
        for (int dy = 0; dy < 5; dy++) set(level, trunk.above(dy), Blocks.BIRCH_LOG.defaultBlockState());
        BlockState leaf = Blocks.BIRCH_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 3; dy <= 6; dy++) {
                    int r = dy >= 5 ? 1 : 2;
                    if (Math.abs(dx) > r || Math.abs(dz) > r || (dx == 0 && dz == 0 && dy < 5)) continue;
                    BlockPos p = trunk.offset(dx, dy, dz);
                    if (level.getBlockState(p).isAir()) set(level, p, leaf);
                }
            }
        }
        // a cloud drifting at its edge
        for (int i = 0; i < 9; i++) set(level, new BlockPos(x0 + 3 + random.nextInt(4), y0 + 4 + random.nextInt(2), z0 + 2 + random.nextInt(4)), Blocks.WHITE_WOOL.defaultBlockState());
        BlockPos chest = new BlockPos(x0 + 2, y0 + 1, z0 + 1);
        set(level, chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST));
        net.minecraft.world.RandomizableContainer.setBlockEntityLootTable(level, random, chest, LOOT);
        if (residents) RealmManorStructure.settle(level, GirlSpecies.SYLPH_ELF, new BlockPos(x0, y0 + 1, z0 + 2), chest);
        return chest;
    }
}
