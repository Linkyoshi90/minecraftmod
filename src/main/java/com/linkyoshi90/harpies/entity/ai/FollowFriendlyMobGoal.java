package com.linkyoshi90.harpies.entity.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * Every so often picks a nearby mob she gets along with (the species decides who: animals and villagers for a
 * sparrow, other crows for a crow) and tags along beside it for a while.
 */
public class FollowFriendlyMobGoal extends Goal {
    private final PathfinderMob mob;
    private final double speed;
    private final float stopDistance;
    private final float searchRadius;
    private final BiPredicate<Mob, Mob> friendly;
    private Mob target;
    private int followTicks;
    private int repathDelay;

    public FollowFriendlyMobGoal(PathfinderMob mob, double speed, float stopDistance, float searchRadius, BiPredicate<Mob, Mob> friendly) {
        this.mob = mob;
        this.speed = speed;
        this.stopDistance = stopDistance;
        this.searchRadius = searchRadius;
        this.friendly = friendly;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean isFriendly(Mob other) {
        if (other == this.mob || other.isInvisible() || !other.isAlive()) return false;
        return this.friendly.test(this.mob, other);
    }

    @Override
    public boolean canUse() {
        if (this.mob.getRandom().nextInt(reducedTickDelay(400)) != 0) return false;
        List<Mob> nearby = this.mob.level().getEntitiesOfClass(Mob.class,
                this.mob.getBoundingBox().inflate(this.searchRadius), this::isFriendly);
        this.target = nearby.stream().min(Comparator.comparingDouble(this.mob::distanceToSqr)).orElse(null);
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.followTicks > 0 && this.target != null && this.target.isAlive()
                && this.mob.distanceToSqr(this.target) < this.searchRadius * this.searchRadius * 4;
    }

    @Override
    public void start() {
        this.followTicks = 200 + this.mob.getRandom().nextInt(400);
        this.repathDelay = 0;
    }

    @Override
    public void stop() {
        this.target = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.followTicks--;
        this.mob.getLookControl().setLookAt(this.target, 10.0F, this.mob.getMaxHeadXRot());
        if (--this.repathDelay > 0) return;
        this.repathDelay = adjustedTickDelay(10);
        if (this.mob.distanceToSqr(this.target) > this.stopDistance * this.stopDistance) {
            this.mob.getNavigation().moveTo(this.target, this.speed);
        } else {
            this.mob.getNavigation().stop();
        }
    }
}
