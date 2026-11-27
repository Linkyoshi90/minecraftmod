package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.Group;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * She walks over to a bed where a friend is already with him, then stays put while the scene places her.
 * The pose and the trade of places are {@link Group}.
 */
public class GroupGoal extends Goal {
    private final Mob girl;

    public GroupGoal(Mob girl) {
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return Group.guest(this.girl);
    }

    @Override
    public boolean canContinueToUse() {
        return Group.guest(this.girl);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (Group.approaching(this.girl)) Group.steer(this.girl);
        else {
            this.girl.getNavigation().stop();
            this.girl.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        }
    }
}
