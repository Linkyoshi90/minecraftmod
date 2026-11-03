package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * A tadpole does exactly one thing: swim about in the water. While she runs, every other goal (which all need to
 * move, jump or look, FloatGoal included, so she never leaps out) is held back, so a tadpole never fights, hunts, sulks or goes to bed. Out of the water she
 * wriggles towards the nearest pond.
 */
public class TadpoleGoal extends Goal {
    private static final int STUCK_TICKS = 40;

    private final AbstractAnuran mob;
    private Vec3 lastPos = Vec3.ZERO;
    private int stillTicks;

    public TadpoleGoal(AbstractAnuran mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.mob.isTadpole();
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.isTadpole();
    }

    /** Otherwise tick() runs every other tick, and the stranded count and the wriggle below lag behind. */
    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (!this.mob.isInWater() && this.unstick()) return;
        if (!this.mob.getNavigation().isDone() || this.mob.getRandom().nextInt(20) != 0) return;
        if (this.mob.isInWater()) {
            Vec3 pos = BehaviorUtils.getRandomSwimmablePos(this.mob, 6, 3);
            // stay in the water: only pick spots that are wet themselves
            if (pos != null && this.mob.level().getFluidState(BlockPos.containing(pos)).is(FluidTags.WATER)) this.mob.getNavigation().moveTo(pos.x, pos.y, pos.z, 1.0);
        } else {
            BlockPos water = AbstractAnuran.findWater(this.mob.level(), this.mob.blockPosition(), 12);
            if (water != null) this.mob.getNavigation().moveTo(water.getX() + 0.5, water.getY(), water.getZ() + 0.5, 1.0);
        }
    }

    /**
     * Stranded on land, a tadpole's path sometimes stalls (she is too small to step where the land path wants her
     * to). If she hasn't moved for a couple of seconds she drops the path and wriggles straight at the water.
     */
    private boolean unstick() {
        if (this.mob.position().distanceToSqr(this.lastPos) > 0.01) {
            this.lastPos = this.mob.position();
            this.stillTicks = 0;
            return false;
        }
        if (++this.stillTicks < STUCK_TICKS) return false;
        BlockPos water = AbstractAnuran.findWater(this.mob.level(), this.mob.blockPosition(), 12);
        if (water == null) return false;
        this.mob.getNavigation().stop();
        this.mob.getMoveControl().setWantedPosition(water.getX() + 0.5, water.getY(), water.getZ() + 0.5, 1.0);
        if (this.mob.onGround() && this.mob.getRandom().nextInt(10) == 0) this.mob.getJumpControl().jump();
        if (this.mob.onGround() && this.stillTicks % 10 == 0) {          // and if even that stalls, a flop straight at it
            Vec3 to = Vec3.atBottomCenterOf(water).subtract(this.mob.position()).multiply(1, 0, 1).normalize().scale(0.25);
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(to.x, 0.2, to.z));
        }
        return true;
    }
}
