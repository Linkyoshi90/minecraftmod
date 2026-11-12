package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.AbstractAnuran;
import com.linkyoshi90.harmontide.entity.Bullywug;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/** The pond contest: two bullywugs close together face off and bellow at each other, the loudest wins the pond. */
public class BellowGoal extends Goal {
    private static final int CONTEST_TICKS = 70;
    private static final double RANGE = 10.0;

    private final Bullywug mob;
    private Bullywug rival;
    private int ticksLeft;

    public BellowGoal(Bullywug mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isBaby()) return false;
        if (this.mob.getAction() != AbstractAnuran.Action.NONE || !this.mob.onGround() || this.mob.isInWater()) return false;
        if (this.mob.getTarget() != null || this.mob.getFleeTicks() > 0) return false;
        if (this.mob.getRandom().nextInt(300) != 0) return false;
        this.rival = this.findRival();
        return this.rival != null;
    }

    private Bullywug findRival() {
        List<Bullywug> others = this.mob.level().getEntitiesOfClass(Bullywug.class, this.mob.getBoundingBox().inflate(RANGE),
                o -> o != this.mob && o.getType() == this.mob.getType() && o.isAlive() && o.getAction() == AbstractAnuran.Action.NONE && o.onGround() && !o.isInWater());
        return others.stream().min(Comparator.comparingDouble(this.mob::distanceToSqr)).orElse(null);
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticksLeft > 0 && this.rival != null && this.rival.isAlive() && this.mob.getTarget() == null
                && this.mob.getAction() == AbstractAnuran.Action.BELLOWING;
    }

    @Override
    public void start() {
        this.ticksLeft = CONTEST_TICKS;
        this.mob.getNavigation().stop();
        this.mob.setAction(AbstractAnuran.Action.BELLOWING);
        this.rival.getNavigation().stop();
        this.rival.startTimedAction(AbstractAnuran.Action.BELLOWING, CONTEST_TICKS);
        this.mob.playSound(this.mob.getBellowSound(), 1.4F, this.mob.getVoicePitch());
    }

    @Override
    public void tick() {
        this.ticksLeft--;
        this.mob.getLookControl().setLookAt(this.rival);
        if (this.ticksLeft == CONTEST_TICKS / 2) {
            this.rival.playSound(this.rival.getBellowSound(), 1.4F, this.rival.getVoicePitch());
        }
    }

    @Override
    public void stop() {
        if (this.mob.getAction() == AbstractAnuran.Action.BELLOWING) this.mob.setAction(AbstractAnuran.Action.NONE);
        this.rival = null;
    }
}
