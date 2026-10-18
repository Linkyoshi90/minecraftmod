package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/** Any boat with someone in it draws gulls: they wheel along a few blocks above and behind it. */
public class FollowBoatGoal extends Goal {
    private final AbstractHarpy harpy;
    private Boat boat;
    private int ticks;
    private int duration;
    private double offsetAngle;

    public FollowBoatGoal(AbstractHarpy harpy) {
        this.harpy = harpy;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private static boolean isManned(Boat boat) {
        return boat.isAlive() && boat.getPassengers().stream().anyMatch(p -> p instanceof Player);
    }

    @Override
    public boolean canUse() {
        if (this.harpy.getRandom().nextInt(reducedTickDelay(40)) != 0) return false;
        List<Boat> boats = this.harpy.level().getEntitiesOfClass(Boat.class, this.harpy.getBoundingBox().inflate(32.0), FollowBoatGoal::isManned);
        if (boats.isEmpty()) return false;
        this.boat = boats.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticks < this.duration && isManned(this.boat) && this.harpy.distanceToSqr(this.boat) < 48 * 48;
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.duration = 400 + this.harpy.getRandom().nextInt(800);
        this.offsetAngle = this.harpy.getRandom().nextDouble() * Math.PI * 2;
    }

    @Override
    public void stop() {
        this.boat = null;
    }

    @Override
    public void tick() {
        this.ticks++;
        // Circle slowly over the boat, a little behind it.
        double angle = this.offsetAngle + this.ticks * 0.03;
        Vec3 behind = this.boat.getDeltaMovement().multiply(1, 0, 1).scale(-6.0);
        Vec3 spot = this.boat.position().add(behind).add(Math.cos(angle) * 3.0, 3.5 + Math.sin(this.ticks * 0.05), Math.sin(angle) * 3.0);
        this.harpy.getMoveControl().setWantedPosition(spot.x, spot.y, spot.z, 1.2);
        this.harpy.getLookControl().setLookAt(this.boat);
    }

    public Boat getBoat() {
        return this.boat;
    }
}
