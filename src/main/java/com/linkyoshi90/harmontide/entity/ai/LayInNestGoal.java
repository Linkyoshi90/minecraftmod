package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.RiflebirdHarpy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import javax.annotation.Nullable;
import java.util.EnumSet;

/** With an egg on the way and a nest nearby, she flies over, settles into it for a moment and lays the egg there. */
public class LayInNestGoal extends Goal {
    private static final int SETTLE_TICKS = 40;

    private final RiflebirdHarpy bird;
    private final double speed;
    @Nullable
    private BlockPos nest;
    private int ticks;
    private int settled;

    public LayInNestGoal(RiflebirdHarpy bird, double speed) {
        this.bird = bird;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!this.bird.isEggDue()) return false;
        this.nest = this.bird.getEggNest();
        return this.nest != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.bird.isEggDue() && this.nest != null && this.ticks < 600 && this.bird.isNestWithRoom(this.nest);
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.settled = 0;
    }

    @Override
    public void stop() {
        this.nest = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.ticks++;
        double dx = this.bird.getX() - (this.nest.getX() + 0.5), dz = this.bird.getZ() - (this.nest.getZ() + 0.5);
        if (dx * dx + dz * dz > 0.5 * 0.5 || this.bird.getY() > this.nest.getY() + 0.8) {
            if (this.ticks % 10 == 1) this.bird.getNavigation().moveTo(this.nest.getX() + 0.5, this.nest.getY(), this.nest.getZ() + 0.5, this.speed);
            this.bird.getMoveControl().setWantedPosition(this.nest.getX() + 0.5, this.nest.getY() + 0.25, this.nest.getZ() + 0.5, this.speed);
            return;
        }
        this.bird.getNavigation().stop();
        if (++this.settled >= SETTLE_TICKS) this.bird.layEggInNest(this.nest);
    }
}
