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
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * A dragon's lair (2026-11-08): up in the mountains a mound of rough stone and basalt, hollow inside, with a low tunnel in.
 * The floor glitters with her hoard (blocks of raw gold and gold, amethyst growing from the walls, a chest of treasure), and
 * on it sleeps a wyvern or a wyrm, who makes the place her lair (Dragon: whatever shiny she finds she puts in that chest) and
 * keeps to it until befriended. /harmontide locate lair.
 */
public class DragonLairStructure extends OneChunkStructure {
    public static final MapCodec<DragonLairStructure> CODEC = simpleCodec(DragonLairStructure::new);
    public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE, HarmontideMod.id("chests/dragon_lair"));
    public static final int OUTER = 7, INNER = 5;

    public DragonLairStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected String kind() {
        return "dragon_lair";
    }

    @Override
    protected int spread() {
        return 6;
    }

    @Override
    protected int maxSlope() {
        return 8;
    }

    @Override
    protected int originY(GenerationContext context, int lo, int hi) {
        return lo;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.DRAGON_LAIR.get();
    }

    private static void set(WorldGenLevel level, BlockPos p, BlockState s) {
        level.setBlock(p, s, Block.UPDATE_CLIENTS);
    }

    /** The lair, its floor at {@code origin}'s height. The hoard's chest. */
    public static BlockPos build(WorldGenLevel level, BlockPos origin, RandomSource random, boolean residents) {
        int x0 = origin.getX(), y0 = origin.getY(), z0 = origin.getZ();
        BlockState[] rock = {Blocks.STONE.defaultBlockState(), Blocks.COBBLESTONE.defaultBlockState(), Blocks.BASALT.defaultBlockState(),
                Blocks.TUFF.defaultBlockState(), Blocks.ANDESITE.defaultBlockState()};
        for (int dx = -OUTER; dx <= OUTER; dx++) {
            for (int dz = -OUTER; dz <= OUTER; dz++) {
                for (int dy = -1; dy <= OUTER; dy++) {
                    double d = Math.sqrt(dx * dx + dz * dz + dy * dy * 1.4);
                    BlockPos p = new BlockPos(x0 + dx, y0 + dy, z0 + dz);
                    if (dy == -1 && d <= OUTER) set(level, p, rock[random.nextInt(rock.length)]);
                    else if (d <= INNER) set(level, p, dy == 0 ? Blocks.SMOOTH_BASALT.defaultBlockState() : Blocks.AIR.defaultBlockState());
                    else if (d <= OUTER - 0.3 + random.nextDouble() * 0.6) set(level, p, rock[random.nextInt(rock.length)]);
                }
            }
        }
        for (int dz = -OUTER; dz <= -INNER + 1; dz++) {                         // the tunnel in, from the north
            for (int dx = -1; dx <= 1; dx++) for (int dy = 1; dy <= 3; dy++) set(level, new BlockPos(x0 + dx, y0 + dy, z0 + dz), Blocks.AIR.defaultBlockState());
            for (int dx = -1; dx <= 1; dx++) set(level, new BlockPos(x0 + dx, y0, z0 + dz), Blocks.SMOOTH_BASALT.defaultBlockState());
        }
        // the hoard on the floor, amethyst on the walls
        for (int i = 0; i < 10; i++) {
            int dx = random.nextInt(7) - 3, dz = random.nextInt(5) - 1;
            BlockPos p = new BlockPos(x0 + dx, y0 + 1, z0 + dz);
            if (level.getBlockState(p).isAir()) set(level, p, random.nextInt(3) == 0 ? Blocks.GOLD_BLOCK.defaultBlockState() : Blocks.RAW_GOLD_BLOCK.defaultBlockState());
        }
        for (Direction d : new Direction[]{Direction.EAST, Direction.WEST, Direction.SOUTH}) {
            for (int dy = 2; dy <= 3; dy++) {
                BlockPos wall = new BlockPos(x0, y0 + dy, z0).relative(d, INNER);
                BlockPos in = wall.relative(d.getOpposite());
                if (!level.getBlockState(wall).isAir() && level.getBlockState(in).isAir()) {
                    set(level, wall, Blocks.AMETHYST_BLOCK.defaultBlockState());
                    set(level, in, Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(AmethystClusterBlock.FACING, d.getOpposite()));
                }
            }
        }
        BlockPos chest = new BlockPos(x0, y0 + 1, z0 + INNER - 2);
        set(level, chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH));
        net.minecraft.world.RandomizableContainer.setBlockEntityLootTable(level, random, chest, LOOT);
        if (residents) {
            GirlSpecies kind = random.nextBoolean() ? GirlSpecies.WYVERN_DRAGON : GirlSpecies.WYRM_DRAGON;
            RealmManorStructure.settle(level, kind, new BlockPos(x0, y0 + 1, z0), chest);
        }
        return chest;
    }
}
