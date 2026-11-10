package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.List;

/**
 * The mine a digger leaves (2026-11-10, config "minersDigShafts"): when she digs in, a one-block shaft opens beside her spot, laddered
 * on one wall, and it goes deeper while she is down there (as far as the trip has gone, whenever the spot is loaded), down to a
 * depth that suits what she's after: ten blocks or so for stone and coal, nearly thirty for diamonds. She only digs natural ground
 * (dirt, sand, gravel, stone, ores) and stops short of anything else: a floor someone built, water or lava beside the shaft, a cave
 * under it, a wall that can't hold the ladder. Ore she strikes on the way down comes up with her haul. The shaft stays when she
 * climbs out; the player can use it, fill it, or send her down again (a new trip starts its own).
 */
public final class MineShaft {
    public static final String SHAFT = "Shaft", FACE = "Face", DUG = "Dug", DEEP = "Deep", START = "Start", SPOIL = "Spoil";
    /** Cells dug right away, so the entrance shows as she goes down. */
    public static final int FIRST = 2, MAX = 32;

    private MineShaft() {
    }

    /** How deep a shaft for a vein of this many days goes. */
    public static int depth(double days) {
        return (int) Math.min(MAX, Math.round(8 + days * 4));
    }

    /** As she digs in: picks a side of her spot for the shaft and opens it. Writes the shaft onto her trip entry; whether there is one. */
    public static boolean start(ServerLevel level, CompoundTag trip, BlockPos at, double days) {
        trip.putLong(START, level.getGameTime());
        if (!MonsterGirlsConfig.minersDigShafts()) return false;
        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockPos side = at.relative(d);
            BlockState top = level.getBlockState(side);
            if (!top.canBeReplaced() || !top.getFluidState().isEmpty() || level.getBlockEntity(side) != null) continue;
            if (!canDig(level, side.below(), d)) continue;
            if (!top.isAir()) level.destroyBlock(side, false);                       // a tuft of grass in the way
            trip.putLong(SHAFT, side.below().asLong());
            trip.putInt(FACE, d.get2DDataValue());
            trip.putInt(DEEP, depth(days));
            trip.putInt(DUG, 0);
            dig(level, trip, Math.min(FIRST, depth(days)));
            return true;
        }
        return false;
    }

    /** Every few seconds while she is away and the shaft is loaded: as deep as the trip has gone. */
    public static void progress(ServerLevel level, CompoundTag trip) {
        if (!trip.contains(SHAFT) || !trip.contains(START)) return;
        long start = trip.getLong(START), back = trip.getLong("Back");
        double done = back <= start ? 1.0 : Math.min(1.0, (double) (level.getGameTime() - start) / (back - start));
        dig(level, trip, Math.max(Math.min(FIRST, trip.getInt(DEEP)), (int) Math.ceil(done * trip.getInt(DEEP))));
    }

    /** As she climbs out: the shaft to its full depth, and the ore she struck on the way (taken off the entry). */
    public static List<ItemStack> finish(ServerLevel level, CompoundTag trip) {
        if (trip.contains(SHAFT)) dig(level, trip, trip.getInt(DEEP));
        List<ItemStack> out = new ArrayList<>();
        for (Tag t : trip.getList(SPOIL, Tag.TAG_COMPOUND)) {
            ItemStack s = ItemStack.parseOptional(level.registryAccess(), (CompoundTag) t);
            if (!s.isEmpty()) out.add(s);
        }
        trip.remove(SPOIL);
        return out;
    }

    /** Digs down to this many cells, unless something stops her (then the shaft is done where it is). */
    static void dig(ServerLevel level, CompoundTag trip, int to) {
        if (!trip.contains(SHAFT) || trip.getBoolean("Stopped")) return;
        BlockPos top = BlockPos.of(trip.getLong(SHAFT));
        if (!level.isLoaded(top)) return;                                             // one column: its top loaded is all of it loaded
        Direction d = Direction.from2DDataValue(trip.getInt(FACE));
        ListTag spoil = trip.getList(SPOIL, Tag.TAG_COMPOUND);
        int dug = trip.getInt(DUG);
        while (dug < to) {
            BlockPos cell = top.below(dug);
            if (!canDig(level, cell, d)) {
                trip.putBoolean("Stopped", true);
                break;
            }
            BlockState state = level.getBlockState(cell);
            if (state.is(Tags.Blocks.ORES)) for (ItemStack s : Block.getDrops(state, level, cell, null)) spoil.add(s.save(level.registryAccess()));
            level.setBlock(cell, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, d.getOpposite()), Block.UPDATE_ALL);
            dug++;
            if (dug == to) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), cell.getX() + 0.5, cell.getY() + 0.5, cell.getZ() + 0.5,
                        12, 0.3, 0.3, 0.3, 0.1);
                level.playSound(null, cell, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.6F, 0.9F);
            }
        }
        trip.putInt(DUG, dug);
        if (!spoil.isEmpty()) trip.put(SPOIL, spoil);
    }

    /** Natural ground she may dig out, with a floor under it, a wall for the ladder, and no water or lava beside it. */
    static boolean canDig(ServerLevel level, BlockPos cell, Direction face) {
        if (cell.getY() <= level.getMinBuildHeight() + 1) return false;
        BlockState state = level.getBlockState(cell);
        if (!natural(state) || level.getBlockEntity(cell) != null || !natural(level.getBlockState(cell.below()))) return false;
        if (!state.getFluidState().isEmpty() || !level.getFluidState(cell.below()).isEmpty()) return false;
        for (Direction d : Direction.Plane.HORIZONTAL) if (!level.getFluidState(cell.relative(d)).isEmpty()) return false;
        return Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, face.getOpposite()).canSurvive(level, cell);
    }

    /** Ground nobody built: dirt, sand, gravel, clay, terracotta, stone of every world, ores. */
    public static boolean natural(BlockState s) {
        return s.is(BlockTags.DIRT) || s.is(BlockTags.SAND) || s.is(Tags.Blocks.GRAVELS) || s.is(BlockTags.BASE_STONE_OVERWORLD)
                || s.is(BlockTags.BASE_STONE_NETHER) || s.is(Tags.Blocks.ORES) || s.is(Tags.Blocks.STONES) || s.is(BlockTags.TERRACOTTA)
                || s.is(Blocks.CLAY) || s.is(Blocks.CALCITE) || s.is(Blocks.DRIPSTONE_BLOCK) || s.is(Blocks.SANDSTONE)
                || s.is(Blocks.RED_SANDSTONE) || s.is(Blocks.END_STONE) || s.is(Blocks.SOUL_SOIL);
    }
}
