package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.function.Predicate;

/**
 * Stays put and plays a resting action: a bullywug sitting on her lily pad, a toad sitting or digging in under
 * soft soil for the day, a surinam toad sweeping the mud. Starts at random while {@code condition} holds, ends when
 * it stops holding, when she is hurt, or after the duration.
 */
public class AnuranRestGoal<T extends AbstractAnuran> extends Goal {
    private final T mob;
    private final AbstractAnuran.Action action;
    private final int chance;
    private final int minTicks;
    private final int maxTicks;
    private final Predicate<T> condition;
    /** Rests in the water (a surinam toad on the bottom) instead of on dry ground. */
    private final boolean aquatic;
    private int ticksLeft;

    public AnuranRestGoal(T mob, AbstractAnuran.Action action, int chance, int minTicks, int maxTicks, Predicate<T> condition) {
        this(mob, action, chance, minTicks, maxTicks, condition, false);
    }

    public AnuranRestGoal(T mob, AbstractAnuran.Action action, int chance, int minTicks, int maxTicks, Predicate<T> condition, boolean aquatic) {
        this.mob = mob;
        this.action = action;
        this.chance = chance;
        this.minTicks = minTicks;
        this.maxTicks = maxTicks;
        this.condition = condition;
        this.aquatic = aquatic;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    private boolean placeOk() {
        return this.aquatic ? this.mob.isInWater() : this.mob.onGround() && !this.mob.isInWater();
    }

    @Override
    public boolean canUse() {
        AbstractAnuran.Action current = this.mob.getAction();
        // a sitting anuran can still be called to a more important rest (digging in, burrowing, sleeping)
        boolean free = !this.mob.isInBed()                  // asleep by the bed after her turn: she stays there
                && (current == AbstractAnuran.Action.NONE || (current == AbstractAnuran.Action.SITTING && this.action != AbstractAnuran.Action.SITTING));
        return free && this.placeOk()
                && this.mob.getTarget() == null && this.condition.test(this.mob)
                && this.mob.getRandom().nextInt(this.chance) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticksLeft > 0 && this.mob.getAction() == this.action && this.placeOk()
                && this.mob.getTarget() == null && this.condition.test(this.mob)
                && this.mob.tickCount - this.mob.getLastHurtByMobTimestamp() > 40;
    }

    @Override
    public void start() {
        this.ticksLeft = this.minTicks + this.mob.getRandom().nextInt(Math.max(1, this.maxTicks - this.minTicks));
        this.mob.getNavigation().stop();
        this.mob.setAction(this.action);
    }

    @Override
    public void tick() {
        this.ticksLeft--;
    }

    @Override
    public void stop() {
        if (this.mob.getAction() == this.action) this.mob.setAction(AbstractAnuran.Action.NONE);
    }
}
