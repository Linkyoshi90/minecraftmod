package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.Breastfeed;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * She walks over to a hungry player and asks. Yes holds them at her chest (Breastfeed). A baby of hers comes first
 * (NurseGoal, which is the higher priority).
 */
public class BreastfeedGoal extends Goal {
    private final Mob girl;

    public BreastfeedGoal(Mob girl) {
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return Breastfeed.active(this.girl) || Breastfeed.wants(this.girl);
    }

    @Override
    public boolean canContinueToUse() {
        return Breastfeed.active(this.girl);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        Breastfeed.ensure(this.girl);
    }

    @Override
    public void stop() {
        Breastfeed.releaseApproach(this.girl);
    }
}
