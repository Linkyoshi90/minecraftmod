package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.AbstractAnuran;
import com.linkyoshi90.harmontide.entity.MucusToad;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.Comparator;
import java.util.EnumSet;

/** Pest control: a toad hunts silverfish, endermites and tiny slimes, and flicks them up with her tongue. */
public class EatPestGoal extends Goal {
    private static final double SEARCH_RANGE = 8.0;
    private static final double REACH_SQR = 6.0;
    private static final int TONGUE_TICKS = 12;
    private static final int GIVE_UP_TICKS = 200;

    private final MucusToad mob;
    private LivingEntity prey;
    private int cooldown;
    private int tongueTicks;
    private int giveUpTicks;

    public EatPestGoal(MucusToad mob) {
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
        if ((this.mob.getAction() != AbstractAnuran.Action.NONE && this.mob.getAction() != AbstractAnuran.Action.SITTING) || this.mob.isInWater()) return false;
        if (this.mob.getRandom().nextInt(10) != 0) return false;
        this.prey = this.mob.level().getEntitiesOfClass(LivingEntity.class, this.mob.getBoundingBox().inflate(SEARCH_RANGE),
                        e -> e.isAlive() && this.mob.isPest(e)).stream()
                .min(Comparator.comparingDouble(this.mob::distanceToSqr)).orElse(null);
        return this.prey != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.prey != null && this.prey.isAlive() && this.giveUpTicks > 0 && this.mob.getAction() != AbstractAnuran.Action.DIGGING;
    }

    @Override
    public void start() {
        this.tongueTicks = 0;
        this.giveUpTicks = GIVE_UP_TICKS;
    }

    @Override
    public void tick() {
        this.giveUpTicks--;
        this.mob.getLookControl().setLookAt(this.prey);
        if (this.mob.distanceToSqr(this.prey) > REACH_SQR) {
            this.mob.getNavigation().moveTo(this.prey, 1.1);
            return;
        }
        this.mob.getNavigation().stop();
        if (this.tongueTicks == 0) {
            this.mob.setAction(this.mob.huntAction());
            this.mob.playSound(this.mob.getTongueSound(), 1.0F, this.mob.getVoicePitch());
        }
        if (++this.tongueTicks >= TONGUE_TICKS) {
            this.mob.swallow(this.prey);
            this.mob.playSound(this.mob.getEatSound(), 1.0F, this.mob.getVoicePitch());
            this.mob.heal(1.0F);
            this.prey = null;
        }
    }

    @Override
    public void stop() {
        this.cooldown = 60 + this.mob.getRandom().nextInt(60);
        if (this.mob.getAction() == this.mob.huntAction()) this.mob.setAction(AbstractAnuran.Action.NONE);
        this.prey = null;
    }
}
