package com.linkyoshi90.harpies.entity.ai;

import com.linkyoshi90.harpies.entity.AbstractHarpy;
import com.linkyoshi90.harpies.entity.CrowHarpy;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Pure mischief: every few minutes a crow picks some other mob, swoops in, pecks it, and flies off cackling.
 * She leaves harpies, people's pets, iron golems and bosses alone.
 */
public class PickOnMobGoal extends Goal {
    private final CrowHarpy crow;
    private final double speed;
    private Mob victim;
    private int ticks;
    private boolean pecked;
    private long nextTime;

    public PickOnMobGoal(CrowHarpy crow, double speed) {
        this.crow = crow;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean isFairGame(Mob mob) {
        return mob.isAlive() && !(mob instanceof AbstractHarpy) && !(mob instanceof TamableAnimal pet && pet.isTame())
                && !(mob instanceof IronGolem) && !(mob instanceof WitherBoss) && !(mob instanceof EnderDragon)
                && this.crow.hasLineOfSight(mob);
    }

    @Override
    public boolean canUse() {
        if (this.crow.isHatchling() || this.crow.isHoldingSomething() || this.crow.level().getGameTime() < this.nextTime) return false;
        if (this.crow.getRandom().nextInt(reducedTickDelay(1800)) != 0) return false;
        List<Mob> mobs = this.crow.level().getEntitiesOfClass(Mob.class, this.crow.getBoundingBox().inflate(12.0), this::isFairGame);
        if (mobs.isEmpty()) return false;
        this.victim = mobs.get(this.crow.getRandom().nextInt(mobs.size()));
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.ticks > 300) return false;
        return this.pecked ? !this.crow.getNavigation().isDone() : this.victim.isAlive();
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.pecked = false;
    }

    @Override
    public void stop() {
        this.victim = null;
        this.nextTime = this.crow.level().getGameTime() + 1200 + this.crow.getRandom().nextInt(2400);
    }

    @Override
    public void tick() {
        this.ticks++;
        if (this.pecked) return;
        this.crow.getLookControl().setLookAt(this.victim, 30.0F, 30.0F);
        double reach = 1.6 + this.victim.getBbWidth() / 2.0;
        if (this.crow.distanceToSqr(this.victim) > reach * reach) {
            if (this.ticks % 10 == 1) this.crow.getNavigation().moveTo(this.victim, this.speed);
            return;
        }
        this.victim.hurt(this.crow.damageSources().mobAttack(this.crow), 1.0F);
        this.crow.laugh();
        this.crow.startTimedAction(AbstractHarpy.Action.PRANKING, 40);
        this.pecked = true;
        Vec3 away = DefaultRandomPos.getPosAway(this.crow, 12, 6, this.victim.position());
        if (away == null) away = this.crow.position().add(this.crow.position().subtract(this.victim.position()).normalize().scale(8)).add(0, 3, 0);
        this.crow.getNavigation().moveTo(away.x, away.y + 2, away.z, this.speed * 1.4);
    }

    /** For tests. */
    public Mob getVictim() {
        return this.victim;
    }
}
