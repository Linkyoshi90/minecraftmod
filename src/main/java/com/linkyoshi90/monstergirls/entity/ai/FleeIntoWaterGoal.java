package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
import com.linkyoshi90.monstergirls.entity.Bullywug;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * A bullywug that has lost the argument (hurt while on her own) bolts for the nearest water and leaps in; with no
 * water around she just runs from whoever hurt her.
 */
public class FleeIntoWaterGoal extends Goal {
    private final Bullywug mob;

    public FleeIntoWaterGoal(Bullywug mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.mob.getFleeTicks() > 0 && !this.mob.isInWater();
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.getFleeTicks() > 0 && !this.mob.isInWater() && !this.mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        this.mob.setAggressive(true); // drives the fast "hop" animation
        BlockPos water = AbstractAnuran.findWater(this.mob.level(), this.mob.blockPosition(), 16);
        if (water != null) {
            this.mob.getNavigation().moveTo(water.getX() + 0.5, water.getY(), water.getZ() + 0.5, 1.5);
            return;
        }
        Vec3 from = this.mob.getFleeFrom();
        Vec3 away = from == null ? DefaultRandomPos.getPos(this.mob, 12, 5) : DefaultRandomPos.getPosAway(this.mob, 12, 5, from);
        if (away != null) this.mob.getNavigation().moveTo(away.x, away.y, away.z, 1.5);
    }

    @Override
    public void tick() {
        if (this.mob.getNavigation().isDone() && this.mob.getFleeTicks() > 0 && !this.mob.isInWater()) this.start();
    }

    @Override
    public void stop() {
        this.mob.setAggressive(false);
        if (this.mob.isInWater()) this.mob.setFleeTicks(0);
    }
}
