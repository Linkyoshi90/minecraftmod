package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.SparrowHarpy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Sparrows nest in nooks and hollows, and the bowl of a hopper is just the right size: she picks one nearby that no other sparrow
 * has claimed, settles on it and roosts there, laying an unfertilised egg into it every so often (see
 * {@link SparrowHarpy#tickRoost}). Anything more urgent (danger, food, a song) can still call her away; she comes back.
 */
public class RoostInHopperGoal extends Goal {
    public static final int SEARCH_RADIUS = 12;

    private final SparrowHarpy sparrow;
    private int ticks;

    public RoostInHopperGoal(SparrowHarpy sparrow) {
        this.sparrow = sparrow;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.sparrow.isBaby() || this.sparrow.isOrderedToSit() || this.sparrow.getTarget() != null || this.sparrow.tickCount % 23 != 0) return false;
        BlockPos roost = this.sparrow.getRoost();
        if (roost != null && !isRoostable(this.sparrow.level(), roost)) {
            this.sparrow.setRoost(roost = null);
        }
        if (roost == null) {
            roost = findHopper(this.sparrow);
            if (roost == null) return false;
            this.sparrow.setRoost(roost);
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        BlockPos roost = this.sparrow.getRoost();
        return roost != null && isRoostable(this.sparrow.level(), roost) && !this.sparrow.isOrderedToSit() && this.sparrow.getTarget() == null;
    }

    @Override
    public void start() {
        this.ticks = 0;
    }

    @Override
    public void stop() {
        this.sparrow.setRoosting(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /** A hopper with room above it for a small bird (she settles into its bowl). */
    public static boolean isRoostable(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.HOPPER) && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
    }

    /** The closest free hopper around her (one no other sparrow nearby calls her own). */
    @Nullable
    public static BlockPos findHopper(SparrowHarpy sparrow) {
        Level level = sparrow.level();
        BlockPos origin = sparrow.blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-SEARCH_RADIUS, -6, -SEARCH_RADIUS), origin.offset(SEARCH_RADIUS, 6, SEARCH_RADIUS))) {
            if (!isRoostable(level, pos)) continue;
            double dist = pos.distSqr(origin);
            if (dist >= bestDist || isClaimed(sparrow, pos)) continue;
            bestDist = dist;
            best = pos.immutable();
        }
        return best;
    }

    private static boolean isClaimed(SparrowHarpy self, BlockPos pos) {
        return !self.level().getEntitiesOfClass(SparrowHarpy.class, self.getBoundingBox().inflate(SEARCH_RADIUS * 2),
                s -> s != self && pos.equals(s.getRoost())).isEmpty();
    }

    @Override
    public void tick() {
        this.ticks++;
        BlockPos roost = this.sparrow.getRoost();
        Vec3 top = Vec3.atBottomCenterOf(roost.above());
        double dx = this.sparrow.getX() - top.x, dz = this.sparrow.getZ() - top.z;
        boolean onTop = dx * dx + dz * dz < 0.35 * 0.35 && Math.abs(this.sparrow.getY() - top.y) < 0.6;
        if (!onTop) {
            this.sparrow.setRoosting(false);
            if (this.ticks % 10 == 1) this.sparrow.getNavigation().moveTo(top.x, top.y, top.z, 1.0);
            this.sparrow.getMoveControl().setWantedPosition(top.x, top.y + 0.1, top.z, 1.0);
            return;
        }
        this.sparrow.getNavigation().stop();
        this.sparrow.setRoosting(true);
    }
}
