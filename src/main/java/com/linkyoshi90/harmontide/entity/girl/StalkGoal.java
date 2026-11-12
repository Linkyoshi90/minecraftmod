package com.linkyoshi90.harmontide.entity.girl;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.function.Predicate;

/**
 * Creeps after her chosen prey, low and slow, and pounces once she is close (the cats). With {@code onlyUnseen} she moves
 * only while her prey is not looking at her and freezes the moment it turns round (the living doll, the jack o'lantern).
 * Give it a better priority than her melee attack: once she has pounced, the attack goal takes over.
 */
public class StalkGoal extends Goal {
    private final AbstractMonsterGirl girl;
    private final double speed;
    private final double pounceRange;
    private final boolean onlyUnseen;
    private final Predicate<LivingEntity> when;
    private int repath;

    public StalkGoal(AbstractMonsterGirl girl, double speed, double pounceRange, boolean onlyUnseen, Predicate<LivingEntity> when) {
        this.girl = girl;
        this.speed = speed;
        this.pounceRange = pounceRange;
        this.onlyUnseen = onlyUnseen;
        this.when = when;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        LivingEntity t = this.girl.getTarget();
        return t != null && t.isAlive() && !this.girl.isOrderedToSit() && this.girl.distanceToSqr(t) > this.pounceRange * this.pounceRange
                && this.girl.distanceToSqr(t) < 32 * 32 && this.when.test(t);
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.repath = 0;
    }

    @Override
    public void stop() {
        if (this.girl.getAction() == AbstractMonsterGirl.Action.STALK) this.girl.setAction(AbstractMonsterGirl.Action.NONE);
        LivingEntity t = this.girl.getTarget();
        if (t != null && t.isAlive() && this.girl.distanceToSqr(t) <= this.pounceRange * this.pounceRange + 0.5 && this.girl.onGround()) {
            Vec3 d = t.position().subtract(this.girl.position()).normalize();
            this.girl.setDeltaMovement(d.x * 0.7, 0.42, d.z * 0.7);
            this.girl.setAction(AbstractMonsterGirl.Action.NONE);
            this.girl.startTimedAction(AbstractMonsterGirl.Action.POUNCE, 16);
        }
    }

    /** Whether the prey is looking her way and can see her. */
    public static boolean isWatchedBy(LivingEntity girl, LivingEntity watcher) {
        Vec3 look = watcher.getViewVector(1.0F).normalize();
        Vec3 to = girl.getEyePosition().subtract(watcher.getEyePosition());
        double dist = to.length();
        return look.dot(to.scale(1.0 / dist)) > 1.0 - 0.35 / Math.max(1.0, dist * 0.25) && watcher.hasLineOfSight(girl);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity t = this.girl.getTarget();
        if (t == null) return;
        if (this.onlyUnseen && isWatchedBy(this.girl, t)) {
            this.girl.getNavigation().stop();
            this.girl.setDeltaMovement(0, this.girl.getDeltaMovement().y, 0);
            this.girl.setAction(AbstractMonsterGirl.Action.NONE);
            return;
        }
        this.girl.getLookControl().setLookAt(t, 10.0F, 10.0F);
        if (this.girl.getAction() == AbstractMonsterGirl.Action.NONE) this.girl.setAction(AbstractMonsterGirl.Action.STALK);
        if (--this.repath <= 0) {
            this.repath = 8;
            this.girl.getNavigation().moveTo(t, this.speed);
        }
    }
}
