package com.linkyoshi90.harpies.entity.ai;

import com.linkyoshi90.harpies.entity.AbstractHarpy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * A harpy with a home (a place where she was fed or found a birdbath) drifts back to it whenever she has
 * wandered too far, and settles there for the night.
 */
public class ReturnHomeGoal extends Goal {
    private final AbstractHarpy harpy;
    private final double speed;
    private final double roamRadius;
    private int ticks;

    public ReturnHomeGoal(AbstractHarpy harpy, double speed, double roamRadius) {
        this.harpy = harpy;
        this.speed = speed;
        this.roamRadius = roamRadius;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private double homeDistSqr() {
        BlockPos home = this.harpy.getHomePos();
        return home == null ? 0 : this.harpy.distanceToSqr(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
    }

    @Override
    public boolean canUse() {
        if (this.harpy.getHomePos() == null || this.harpy.getRandom().nextInt(reducedTickDelay(60)) != 0) return false;
        double limit = this.harpy.level().isDay() ? this.roamRadius : 4.0;
        return this.homeDistSqr() > limit * limit;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticks < 600 && this.homeDistSqr() > 2.5 * 2.5;
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.moveHome();
    }

    @Override
    public void stop() {
        this.harpy.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (++this.ticks % 40 == 0 || this.harpy.getNavigation().isDone()) this.moveHome();
    }

    private void moveHome() {
        BlockPos home = this.harpy.getHomePos();
        if (home != null) this.harpy.getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, this.speed);
    }
}
