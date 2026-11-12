package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.AbstractAnuran;
import com.linkyoshi90.harmontide.entity.Natterjack;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** A natterjack throws back her sac and trills; every other natterjack that hears it comes to join in. */
public class ChorusGoal extends Goal {
    private static final int CHORUS_TICKS = 100;

    private final Natterjack mob;
    private int ticksLeft;

    public ChorusGoal(Natterjack mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isBaby()) return false;
        return this.mob.getAction() == AbstractAnuran.Action.NONE && this.mob.onGround() && !this.mob.isInWater()
                && this.mob.getTarget() == null && this.mob.getCallPos() == null && this.mob.getRandom().nextInt(600) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticksLeft > 0 && this.mob.getAction() == AbstractAnuran.Action.CHORUS
                && this.mob.tickCount - this.mob.getLastHurtByMobTimestamp() > 40;
    }

    @Override
    public void start() {
        this.ticksLeft = CHORUS_TICKS;
        this.mob.getNavigation().stop();
        this.mob.setAction(AbstractAnuran.Action.CHORUS);
        this.mob.playSound(this.mob.getBellowSound(), 1.5F, this.mob.getVoicePitch());
        this.callOthers();
    }

    /** Public so tests and other triggers can sound the call directly. */
    public void callOthers() {
        for (Natterjack other : this.mob.level().getEntitiesOfClass(Natterjack.class, this.mob.getBoundingBox().inflate(Natterjack.CHORUS_RANGE),
                n -> n != this.mob && n.isAlive())) {
            other.hearChorus(this.mob.blockPosition());
        }
    }

    @Override
    public void tick() {
        this.ticksLeft--;
        if (this.ticksLeft == CHORUS_TICKS / 2) this.mob.playSound(this.mob.getBellowSound(), 1.5F, this.mob.getVoicePitch());
    }

    @Override
    public void stop() {
        if (this.mob.getAction() == AbstractAnuran.Action.CHORUS) this.mob.setAction(AbstractAnuran.Action.NONE);
    }
}
