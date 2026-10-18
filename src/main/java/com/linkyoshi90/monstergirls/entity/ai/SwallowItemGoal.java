package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
import com.linkyoshi90.monstergirls.entity.CaneToad;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.Comparator;
import java.util.EnumSet;

/**
 * The cane toad swallows items left lying around, whole, and they are gone for good. Anything that has been on the
 * ground for less than ten seconds is safe, so a player who drops something has time to pick it up again.
 */
public class SwallowItemGoal extends Goal {
    /** Items younger than this (ticks on the ground) are left alone. */
    public static final int MIN_AGE = 200;
    private static final double SEARCH_RANGE = 8.0;
    private static final double REACH_SQR = 6.0;
    private static final int GULP_TICKS = 14;
    private static final int GIVE_UP_TICKS = 200;

    private final CaneToad mob;
    private ItemEntity target;
    private int cooldown;
    private int gulpTicks;
    private int giveUpTicks;

    public SwallowItemGoal(CaneToad mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isBaby()) return false;
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        if ((this.mob.getAction() != AbstractAnuran.Action.NONE && this.mob.getAction() != AbstractAnuran.Action.SITTING) || this.mob.isInWater() || this.mob.getRandom().nextInt(10) != 0) return false;
        this.target = this.mob.level().getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(SEARCH_RANGE),
                        item -> item.isAlive() && item.getAge() >= MIN_AGE && !item.hasPickUpDelay()).stream()
                .min(Comparator.comparingDouble(this.mob::distanceToSqr)).orElse(null);
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null && this.target.isAlive() && this.giveUpTicks > 0;
    }

    @Override
    public void start() {
        this.gulpTicks = 0;
        this.giveUpTicks = GIVE_UP_TICKS;
    }

    @Override
    public void tick() {
        this.giveUpTicks--;
        this.mob.getLookControl().setLookAt(this.target);
        if (this.mob.distanceToSqr(this.target) > REACH_SQR) {
            this.mob.getNavigation().moveTo(this.target, 1.0);
            return;
        }
        this.mob.getNavigation().stop();
        if (this.gulpTicks == 0) {
            this.mob.setAction(AbstractAnuran.Action.GULPING);
            this.mob.playSound(this.mob.getTongueSound(), 1.0F, this.mob.getVoicePitch());
        }
        if (++this.gulpTicks >= GULP_TICKS) {
            this.target.discard();
            this.mob.playSound(this.mob.getEatSound(), 1.0F, this.mob.getVoicePitch());
            this.target = null;
        }
    }

    @Override
    public void stop() {
        this.cooldown = 100 + this.mob.getRandom().nextInt(100);
        if (this.mob.getAction() == AbstractAnuran.Action.GULPING) this.mob.setAction(AbstractAnuran.Action.NONE);
        this.target = null;
    }
}
