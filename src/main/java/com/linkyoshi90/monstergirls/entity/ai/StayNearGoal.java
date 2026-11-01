package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.Orders;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Patrol and follow orders: she roams as she likes but heads back once she strays past the radius (from her patrol
 * spot, or from her player). A fight may pull her further, but not more than twice as far. Left more than 40 blocks
 * behind her player, she catches up at once, as tamed wolves do.
 */
public class StayNearGoal<T extends PathfinderMob & Bonded> extends Goal {
    private final T mob;
    private Vec3 centre;
    private int repath;
    private int cooldown;

    public StayNearGoal(T mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private double radius() {
        return this.mob.getOrders().command(this.mob) == Orders.Command.PATROL ? Orders.PATROL_RADIUS : Orders.FOLLOW_RADIUS;
    }

    @Override
    public boolean canUse() {
        if (--this.cooldown > 0) return false;              // a countdown, not tickCount: idle goals are only checked every other tick
        this.cooldown = 5;
        if (com.linkyoshi90.monstergirls.entity.BedCall.calledNear(this.mob)) return false;
        this.centre = this.mob.getOrders().centre(this.mob);
        if (this.centre == null) return false;
        double r = this.mob.getTarget() == null ? this.radius() : this.radius() * 2;
        return this.mob.distanceToSqr(this.centre) > r * r;
    }

    @Override
    public boolean canContinueToUse() {
        this.centre = this.mob.getOrders().centre(this.mob);
        if (com.linkyoshi90.monstergirls.entity.BedCall.calledNear(this.mob)) return false;
        double r = this.radius() * 0.5;
        return this.centre != null && this.mob.distanceToSqr(this.centre) > r * r;
    }

    @Override
    public void start() {
        this.repath = 0;
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.mob.getOrders().command(this.mob) == Orders.Command.FOLLOW && this.mob.distanceToSqr(this.centre) > 40 * 40) {
            this.mob.teleportTo(this.centre.x, this.centre.y, this.centre.z);
            this.mob.getNavigation().stop();
            return;
        }
        if (--this.repath <= 0) {
            this.repath = 20;
            this.mob.getNavigation().moveTo(this.centre.x, this.centre.y, this.centre.z, 1.15);
        }
    }
}
