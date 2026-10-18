package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
import com.linkyoshi90.monstergirls.entity.GoliathFrog;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** The goliath's answer to trouble: lift a foot, bring it down, and knock everything nearby back. */
public class StompGoal extends Goal {
    public static final double RADIUS = 5.0;
    private static final int IMPACT_TICK = 14;
    private static final int TOTAL_TICKS = 30;
    private static final int COOLDOWN = 100;

    private final GoliathFrog mob;
    private int ticks;
    private int cooldown;

    public StompGoal(GoliathFrog mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isBaby()) return false;
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        return this.mob.isStompWanted() && this.mob.onGround();
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticks < TOTAL_TICKS;
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.mob.clearStompWanted();
        this.mob.getNavigation().stop();
        this.mob.setAction(AbstractAnuran.Action.STOMPING);
    }

    @Override
    public void tick() {
        if (++this.ticks == IMPACT_TICK) this.impact();
    }

    private void impact() {
        this.mob.level().playSound(null, this.mob.blockPosition(), SoundEvents.IRON_GOLEM_ATTACK, SoundSource.NEUTRAL, 1.5F, 0.5F);
        for (LivingEntity e : this.mob.level().getEntitiesOfClass(LivingEntity.class, this.mob.getBoundingBox().inflate(RADIUS),
                x -> x != this.mob && x.isAlive() && !(x instanceof GoliathFrog))) {
            e.knockback(1.2, this.mob.getX() - e.getX(), this.mob.getZ() - e.getZ());
            e.setDeltaMovement(e.getDeltaMovement().add(0.0, 0.25, 0.0));
            e.hurtMarked = true; // players only feel it once the velocity is synced
        }
    }

    @Override
    public void stop() {
        this.cooldown = COOLDOWN;
        if (this.mob.getAction() == AbstractAnuran.Action.STOMPING) this.mob.setAction(AbstractAnuran.Action.NONE);
    }
}
