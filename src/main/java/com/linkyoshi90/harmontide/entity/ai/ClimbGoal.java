package com.linkyoshi90.harmontide.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * Up a wall or a tree trunk: she heads straight into a climbable column a few blocks taller than she is and climbs it
 * (the mob's onClimbable must say yes while she presses against it). Now and then, or at once when `urgent` (a scared
 * black bear goes up a tree), and she stays up as long as it lasts.
 */
public class ClimbGoal extends Goal {
    private final PathfinderMob mob;
    private final int chance;
    private final BooleanSupplier urgent;
    private final Predicate<BlockState> climbable;
    private final int hold;
    @Nullable
    private Vec3 target;
    private int ticks;

    /** chance: one in this many each second; hold: how long she stays up once there (ticks). */
    public ClimbGoal(PathfinderMob mob, int chance, BooleanSupplier urgent, Predicate<BlockState> climbable, int hold) {
        this.mob = mob;
        this.chance = chance;
        this.urgent = urgent;
        this.climbable = climbable;
        this.hold = hold;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isBaby() || this.mob.getTarget() != null || this.mob.isInWater() || this.mob.isPassenger()) return false;
        if (!this.urgent.getAsBoolean() && (this.mob.tickCount % 20 != 0 || this.mob.getRandom().nextInt(this.chance) != 0)) return false;
        this.target = this.findColumn();
        return this.target != null;
    }

    /** The nearest column within 8 blocks that rises from her feet to at least 3 blocks above them: aim at its third block. */
    @Nullable
    private Vec3 findColumn() {
        BlockPos o = this.mob.blockPosition();
        Vec3 best = null;
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-8, 0, -8), o.offset(8, 0, 8))) {
            if (p.getX() == o.getX() && p.getZ() == o.getZ()) continue;
            if (!this.climbable.test(this.mob.level().getBlockState(p)) || !this.climbable.test(this.mob.level().getBlockState(p.above(3)))) continue;
            Vec3 v = Vec3.atCenterOf(p.above(3));
            if (best == null || v.distanceToSqr(this.mob.position()) < best.distanceToSqr(this.mob.position())) best = v;
        }
        return best;
    }

    @Override
    public void start() {
        this.ticks = this.hold + 100;
        this.mob.getNavigation().stop();
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null && this.mob.getTarget() == null && (this.ticks > 0 || this.urgent.getAsBoolean());
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.ticks--;
        this.mob.getLookControl().setLookAt(this.target.x, this.target.y, this.target.z);
        this.mob.getMoveControl().setWantedPosition(this.target.x, this.target.y, this.target.z, 1.0);   // into the column: she climbs it
    }

    @Override
    public void stop() {
        this.target = null;
    }
}
