package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Any boat with someone in it draws seabirds, wild or not: they fly alongside it, off to one side and a little above, keeping
 * pace while it moves and wheeling slowly over it while it sits still. Seagulls ride close and low; the albatross keeps
 * a wider berth, higher up.
 */
public class FollowBoatGoal extends Goal {
    public static final double RANGE = 32.0;

    private final AbstractHarpy harpy;
    private final double beside, above;
    @Nullable
    private Boat boat;
    private int ticks;
    private int duration;
    private double offsetAngle;
    private int side;

    /** A gull's place: three blocks off the side, three and a half up. */
    public FollowBoatGoal(AbstractHarpy harpy) {
        this(harpy, 3.0, 3.5);
    }

    public FollowBoatGoal(AbstractHarpy harpy, double beside, double above) {
        this.harpy = harpy;
        this.beside = beside;
        this.above = above;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private static boolean isManned(Boat boat) {
        return boat.isAlive() && boat.getPassengers().stream().anyMatch(p -> p instanceof Player);
    }

    @Override
    public boolean canUse() {
        if (this.harpy.getRandom().nextInt(reducedTickDelay(40)) != 0) return false;
        Boat nearest = null;
        double best = RANGE * RANGE;
        for (Boat b : this.harpy.level().getEntitiesOfClass(Boat.class, this.harpy.getBoundingBox().inflate(RANGE), FollowBoatGoal::isManned)) {
            double d = b.distanceToSqr(this.harpy);
            if (d < best) {
                best = d;
                nearest = b;
            }
        }
        this.boat = nearest;
        return nearest != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.boat != null && this.ticks < this.duration && isManned(this.boat) && this.harpy.distanceToSqr(this.boat) < 48 * 48;
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.duration = 400 + this.harpy.getRandom().nextInt(800);
        this.offsetAngle = this.harpy.getRandom().nextDouble() * Math.PI * 2;
        if (this.boat != null) {                                     // the side she is already on
            Vec3 right = right(this.boat);
            this.side = this.harpy.position().subtract(this.boat.position()).dot(right) >= 0 ? 1 : -1;
        }
    }

    @Override
    public void stop() {
        this.boat = null;
    }

    private static Vec3 right(Boat boat) {
        double yaw = Math.toRadians(boat.getYRot());
        return new Vec3(-Math.cos(yaw), 0, -Math.sin(yaw));
    }

    /** Where she wants to be now: beside a moving boat, wheeling over a still one. */
    public Vec3 spot() {
        if (this.boat == null) return this.harpy.position();
        double bob = Math.sin(this.ticks * 0.05);
        Vec3 drift = this.boat.getDeltaMovement().multiply(1, 0, 1);
        if (drift.lengthSqr() > 0.0025) {
            return this.boat.position().add(drift.scale(10.0)).add(right(this.boat).scale(this.side * this.beside)).add(0, this.above + bob, 0);
        }
        double angle = this.offsetAngle + this.ticks * 0.03;
        return this.boat.position().add(Math.cos(angle) * this.beside, this.above + bob, Math.sin(angle) * this.beside);
    }

    @Override
    public void tick() {
        this.ticks++;
        if (this.boat == null) return;
        Vec3 spot = this.spot();
        this.harpy.getMoveControl().setWantedPosition(spot.x, spot.y, spot.z, 1.2);
        this.harpy.getLookControl().setLookAt(this.boat);
    }

    @Nullable
    public Boat getBoat() {
        return this.boat;
    }
}
