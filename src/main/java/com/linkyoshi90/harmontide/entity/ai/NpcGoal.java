package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.NpcSex;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * She runs to the other one. Once they are together, {@link NpcSex} places them and this goal just holds her still.
 */
public class NpcGoal extends Goal {
    private final Mob girl;

    public NpcGoal(Mob girl) {
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return NpcSex.active(this.girl);
    }

    @Override
    public boolean canContinueToUse() {
        return NpcSex.active(this.girl);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (NpcSex.approaching(this.girl)) NpcSex.steer(this.girl);
        else {
            this.girl.getNavigation().stop();
            this.girl.setDeltaMovement(Vec3.ZERO);
        }
    }
}
