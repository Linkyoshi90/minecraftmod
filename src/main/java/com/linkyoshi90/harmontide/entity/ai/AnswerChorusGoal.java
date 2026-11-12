package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.Natterjack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Walks (quickly) to the natterjack whose chorus she heard, and stays a few blocks from her. */
public class AnswerChorusGoal extends Goal {
    private static final double ARRIVED_SQR = 9.0;

    private final Natterjack mob;

    public AnswerChorusGoal(Natterjack mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        BlockPos call = this.mob.getCallPos();
        return call != null && !this.mob.isInWater() && call.distSqr(this.mob.blockPosition()) > ARRIVED_SQR;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void tick() {
        BlockPos call = this.mob.getCallPos();
        if (call != null && this.mob.getNavigation().isDone()) {
            this.mob.getNavigation().moveTo(call.getX() + 0.5, call.getY(), call.getZ() + 0.5, 1.3);
        }
    }

    @Override
    public void stop() {
        if (this.mob.getCallPos() != null && this.mob.getCallPos().distSqr(this.mob.blockPosition()) <= ARRIVED_SQR) this.mob.forgetCall();
    }
}
