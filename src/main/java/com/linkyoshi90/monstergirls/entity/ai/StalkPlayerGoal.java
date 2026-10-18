package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.OwlHarpy;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * At night an owl sometimes picks a player and shadows them: she keeps 8 to 14 blocks away, lands, stares and
 * hoots now and then. Unsettling, but she never attacks.
 */
public class StalkPlayerGoal extends Goal {
    private static final double NEAR = 8.0;
    private static final double FAR = 14.0;

    private final OwlHarpy owl;
    private Player target;
    private int ticks;
    private int duration;
    private long nextTime;

    public StalkPlayerGoal(OwlHarpy owl) {
        this.owl = owl;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.owl.level().isDay() || this.owl.level().getGameTime() < this.nextTime || this.owl.getTarget() != null) return false;
        if (this.owl.getRandom().nextInt(reducedTickDelay(400)) != 0) return false;
        this.target = this.owl.level().getNearestPlayer(this.owl.getX(), this.owl.getY(), this.owl.getZ(), 24.0,
                e -> e instanceof Player p && !p.isSpectator());
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticks < this.duration && this.target.isAlive() && !this.owl.level().isDay() && this.owl.getTarget() == null
                && this.owl.distanceToSqr(this.target) < 40 * 40;
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.duration = 600 + this.owl.getRandom().nextInt(900);
    }

    @Override
    public void stop() {
        this.target = null;
        this.nextTime = this.owl.level().getGameTime() + 1200 + this.owl.getRandom().nextInt(2400);
    }

    @Override
    public void tick() {
        this.ticks++;
        this.owl.getLookControl().setLookAt(this.target, 20.0F, 20.0F);
        double dist = Math.sqrt(this.owl.distanceToSqr(this.target));
        if (dist > FAR) {
            if (this.ticks % 20 == 1) this.owl.getNavigation().moveTo(this.target, 1.0);
        } else if (dist < NEAR) {
            if (this.ticks % 20 == 1) {
                Vec3 away = this.owl.position().subtract(this.target.position()).multiply(1, 0, 1).normalize().scale(4);
                this.owl.getNavigation().moveTo(this.owl.getX() + away.x, this.owl.getY(), this.owl.getZ() + away.z, 1.0);
            }
        } else {
            this.owl.getNavigation().stop();
            if (this.ticks % 160 == 80) this.owl.hoot();
        }
    }

    public Player getStalked() {
        return this.target;
    }
}
