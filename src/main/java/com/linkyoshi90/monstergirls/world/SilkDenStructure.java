package com.linkyoshi90.monstergirls.world;

import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.item.CocoonItem;
import com.linkyoshi90.monstergirls.registry.ModStructures;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * A silk den (2026-11-08): in a dark forest, a hollow ringed by dead trees (stripped dark oak) with cobwebs strung between them,
 * pale cocoon-pillars of white wool, bones and string underfoot, and a chest of silk cocoons to tear open (CocoonItem: each
 * holds something). A jorogumo, the weaver of the woods, keeps to it until befriended. /monstergirls locate den.
 */
public class SilkDenStructure extends OneChunkStructure {
    public static final MapCodec<SilkDenStructure> CODEC = simpleCodec(SilkDenStructure::new);
    public static final int RADIUS = 6, COCOONS = 4;

    public SilkDenStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected String kind() {
        return "silk_den";
    }

    @Override
    protected int spread() {
        return 6;
    }

    @Override
    protected int maxSlope() {
        return 4;
    }

    @Override
    protected int originY(GenerationContext context, int lo, int hi) {
        return lo - 1;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.SILK_DEN.get();
    }

    private static void set(WorldGenLevel level, BlockPos p, BlockState s) {
        level.setBlock(p, s, Block.UPDATE_CLIENTS);
    }

    /** What's wrapped up in one of her cocoons. */
    public static ItemStack treasure(RandomSource r) {
        int k = r.nextInt(10);
        return switch (k) {
            case 0 -> new ItemStack(Items.DIAMOND);
            case 1, 2 -> new ItemStack(Items.GOLD_INGOT, 2 + r.nextInt(3));
            case 3, 4 -> new ItemStack(Items.EMERALD, 1 + r.nextInt(3));
            case 5 -> new ItemStack(Items.ENDER_PEARL);
            case 6 -> new ItemStack(Items.NAME_TAG);
            default -> new ItemStack(Items.SPIDER_EYE, 2);
        };
    }

    /** The den, its floor at {@code origin}'s height. Its chest. */
    public static BlockPos build(WorldGenLevel level, BlockPos origin, RandomSource random, boolean residents) {
        int x0 = origin.getX(), y0 = origin.getY(), z0 = origin.getZ();
        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > RADIUS + 0.4) continue;
                BlockPos g = new BlockPos(x0 + dx, y0, z0 + dz);
                for (int dy = -1; dy >= -4; dy--) if (level.getBlockState(g.above(dy)).canBeReplaced()) set(level, g.above(dy), Blocks.DIRT.defaultBlockState());
                set(level, g, random.nextInt(3) == 0 ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.PODZOL.defaultBlockState());
                for (int dy = 1; dy <= 7; dy++) set(level, g.above(dy), Blocks.AIR.defaultBlockState());
                if (d < RADIUS - 1 && random.nextInt(14) == 0) set(level, g.above(), Blocks.COBWEB.defaultBlockState());
            }
        }
        // dead trees round the rim, webs strung between them up high
        int trees = 6;
        BlockPos prev = null, first = null;
        for (int i = 0; i < trees; i++) {
            double a = i * Math.PI * 2 / trees;
            BlockPos t = new BlockPos(x0 + (int) Math.round(Math.cos(a) * (RADIUS - 1)), y0 + 1, z0 + (int) Math.round(Math.sin(a) * (RADIUS - 1)));
            int h = 4 + random.nextInt(3);
            for (int dy = 0; dy < h; dy++) set(level, t.above(dy), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
            if (prev != null) web(level, prev.above(3), t.above(3));
            if (first == null) first = t;
            prev = t;
        }
        if (prev != null) web(level, prev.above(3), first.above(3));
        // pale cocoon-pillars and the litter of her meals
        for (int[] q : new int[][]{{-2, 3}, {3, -2}, {2, 3}}) {
            set(level, new BlockPos(x0 + q[0], y0 + 1, z0 + q[1]), Blocks.WHITE_WOOL.defaultBlockState());
            set(level, new BlockPos(x0 + q[0], y0 + 2, z0 + q[1]), Blocks.WHITE_WOOL.defaultBlockState());
            set(level, new BlockPos(x0 + q[0], y0 + 3, z0 + q[1]), Blocks.COBWEB.defaultBlockState());
        }
        set(level, new BlockPos(x0 - 3, y0 + 1, z0 - 2), Blocks.BONE_BLOCK.defaultBlockState());
        BlockPos chest = new BlockPos(x0, y0 + 1, z0 - 2);
        set(level, chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH));
        if (level.getBlockEntity(chest) instanceof ChestBlockEntity box) {
            for (int i = 0; i < COCOONS; i++) box.setItem(i * 3 + random.nextInt(3), CocoonItem.wrapping(treasure(random)));
            box.setItem(20, new ItemStack(Items.STRING, 6 + random.nextInt(10)));
        }
        if (residents) RealmManorStructure.settle(level, GirlSpecies.JOROGUMO_ARACHNE, new BlockPos(x0, y0 + 1, z0 + 1), chest);
        return chest;
    }

    /** A strand of web from one tree to the next, a cobweb every other block. */
    private static void web(WorldGenLevel level, BlockPos a, BlockPos b) {
        int steps = Math.max(Math.abs(a.getX() - b.getX()), Math.abs(a.getZ() - b.getZ()));
        for (int i = 1; i < steps; i += 2) {
            BlockPos p = new BlockPos(a.getX() + (b.getX() - a.getX()) * i / steps, a.getY() - (i == steps / 2 ? 1 : 0), a.getZ() + (b.getZ() - a.getZ()) * i / steps);
            if (level.getBlockState(p).isAir()) set(level, p, Blocks.COBWEB.defaultBlockState());
        }
    }
}
