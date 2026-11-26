package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Labor;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Keeps the tick for herself while she is ready to bear, so a stroll does not walk her out of the labor. */
public class LaborGoal extends Goal {
    private final AbstractMonsterGirl girl;

    public LaborGoal(AbstractMonsterGirl girl) {
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.girl.isBaby() || this.girl.getSpecies().family() == GirlSpecies.Family.CONSTRUCT) return false;
        return Labor.active(this.girl) || this.girl.getGestation().isReady();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        Labor.steer(this.girl);
    }
}
