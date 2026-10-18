package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.CrowHarpy;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Very rarely, a crow sneaks up behind a player, does a flawless creeper hiss right behind them and flutters
 * off giggling. Harmless, but you will turn around.
 */
public class CreeperPrankGoal extends Goal {
    private final CrowHarpy crow;
    private final double speed;
    private long nextPrankTime;
    private Player target;
    private int ticks;
    private boolean done;

    public CreeperPrankGoal(CrowHarpy crow, double speed) {
        this.crow = crow;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.crow.level().getGameTime() < this.nextPrankTime || this.crow.isHoldingSomething() || this.crow.isHatchling()) return false;
        if (this.crow.getRandom().nextInt(reducedTickDelay(3000)) != 0) return false;
        this.target = this.crow.level().getNearestPlayer(this.crow.getX(), this.crow.getY(), this.crow.getZ(), 14.0,
                e -> e instanceof Player p && !p.isSpectator() && !this.crow.holdsGrudgeAgainst(p));
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticks < 300 && this.target.isAlive() && (!this.done || !this.crow.getNavigation().isDone());
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.done = false;
    }

    @Override
    public void stop() {
        this.target = null;
        this.nextPrankTime = this.crow.level().getGameTime() + 24000 + this.crow.getRandom().nextInt(24000);
    }

    /** A spot just behind the player's back. */
    private Vec3 behind() {
        Vec3 look = this.target.getLookAngle().multiply(1, 0, 1).normalize();
        return this.target.position().subtract(look.scale(1.6));
    }

    @Override
    public void tick() {
        this.ticks++;
        if (this.done) return;
        Vec3 spot = this.behind();
        if (this.crow.position().distanceToSqr(spot) > 1.5 * 1.5) {
            if (this.ticks % 10 == 1) this.crow.getNavigation().moveTo(spot.x, spot.y, spot.z, this.speed);
            return;
        }
        this.crow.getLookControl().setLookAt(this.target);
        this.crow.level().playSound(null, this.crow.getX(), this.crow.getY(), this.crow.getZ(), SoundEvents.CREEPER_PRIMED,
                SoundSource.HOSTILE, 1.0F, 0.5F);
        this.crow.startTimedAction(AbstractHarpy.Action.PRANKING, 60);
        this.done = true;
        Vec3 away = DefaultRandomPos.getPosAway(this.crow, 10, 5, this.target.position());
        if (away != null) this.crow.getNavigation().moveTo(away.x, away.y + 2, away.z, this.speed * 1.4);
    }
}
