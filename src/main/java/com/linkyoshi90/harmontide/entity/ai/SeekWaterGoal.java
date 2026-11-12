package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.AbstractAnuran;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** A dried-out anuran heads for the nearest water (rain does just as well, but she can't wait for it). */
public class SeekWaterGoal extends Goal {
    private static final int SEARCH_RADIUS = 16;

    private final AbstractAnuran mob;
    private int searchCooldown;
    private BlockPos target;

    public SeekWaterGoal(AbstractAnuran mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isDry() || this.mob.isWet()) return false;
        if (this.searchCooldown > 0) {
            this.searchCooldown--;
            return false;
        }
        this.searchCooldown = 40;
        this.target = AbstractAnuran.findReachableWater(this.mob, SEARCH_RADIUS);
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.isWet() && !this.mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.target.getX() + 0.5, this.target.getY(), this.target.getZ() + 0.5, 1.2);
    }

    @Override
    public void stop() {
        this.target = null;
    }
}
