package com.linkyoshi90.harmontide.entity.girl;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Supplier;

/** Stays close to someone: a young one to her mother, a herd to its lead mare. Re-asks the supplier every few seconds. */
public class FollowEntityGoal extends Goal {
    private final PathfinderMob mob;
    private final Supplier<LivingEntity> who;
    private final double speed;
    private final float start;
    private final float stop;
    @Nullable
    private LivingEntity leader;
    private int repath;

    public FollowEntityGoal(PathfinderMob mob, Supplier<LivingEntity> who, double speed, float start, float stop) {
        this.mob = mob;
        this.who = who;
        this.speed = speed;
        this.start = start;
        this.stop = stop;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.tickCount % 19 != 0 || this.mob.getTarget() != null || com.linkyoshi90.harmontide.entity.Orders.commanded(this.mob)) return false;
        this.leader = this.who.get();
        return this.leader != null && this.leader.isAlive() && this.mob.distanceToSqr(this.leader) > this.start * this.start;
    }

    @Override
    public boolean canContinueToUse() {
        return this.leader != null && this.leader.isAlive() && this.mob.distanceToSqr(this.leader) > this.stop * this.stop
                && this.mob.distanceToSqr(this.leader) < 48 * 48 && this.mob.getTarget() == null;
    }

    @Override
    public void start() {
        this.repath = 0;
    }

    @Override
    public void stop() {
        this.leader = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.mob.getLookControl().setLookAt(this.leader, 10.0F, this.mob.getMaxHeadXRot());
        if (--this.repath <= 0) {
            this.repath = 10;
            this.mob.getNavigation().moveTo(this.leader, this.speed);
        }
    }
}
