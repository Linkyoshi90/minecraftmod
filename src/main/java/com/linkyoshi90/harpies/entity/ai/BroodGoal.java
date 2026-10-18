package com.linkyoshi90.harpies.entity.ai;

import com.linkyoshi90.harpies.entity.AbstractHarpy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** At night she goes back to her nest and settles down on it until morning (or until someone disturbs her). */
public class BroodGoal extends Goal {
    private final AbstractHarpy harpy;
    private int ticks;

    public BroodGoal(AbstractHarpy harpy) {
        this.harpy = harpy;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    private boolean isNight() {
        return !this.harpy.level().isDay();
    }

    @Override
    public boolean canUse() {
        return this.isNight() && this.harpy.getHomePos() != null && this.harpy.getTarget() == null
                && this.harpy.getRandom().nextInt(reducedTickDelay(20)) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.isNight() && this.harpy.getHomePos() != null && this.harpy.getTarget() == null && this.ticks < 12000;
    }

    @Override
    public void start() {
        this.ticks = 0;
    }

    @Override
    public void stop() {
        if (this.harpy.getAction() == AbstractHarpy.Action.BROODING) this.harpy.setAction(AbstractHarpy.Action.NONE);
    }

    @Override
    public void tick() {
        this.ticks++;
        BlockPos nest = this.harpy.getHomePos();
        if (this.harpy.distanceToSqr(Vec3.atBottomCenterOf(nest)) > 1.2 * 1.2) {
            if (this.harpy.getAction() == AbstractHarpy.Action.BROODING) this.harpy.setAction(AbstractHarpy.Action.NONE);
            if (this.ticks % 20 == 1) this.harpy.getNavigation().moveTo(nest.getX() + 0.5, nest.getY(), nest.getZ() + 0.5, 1.0);
            return;
        }
        this.harpy.getNavigation().stop();
        this.harpy.setAction(AbstractHarpy.Action.BROODING);
    }

    public boolean isBrooding() {
        return this.harpy.getAction() == AbstractHarpy.Action.BROODING;
    }
}
