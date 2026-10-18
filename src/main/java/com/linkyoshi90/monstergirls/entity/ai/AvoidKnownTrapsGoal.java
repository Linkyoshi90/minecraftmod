package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.CrowHarpy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** Steps away from any spot where she has been hurt before (cactus, berry bushes, fire, dispenser traps...). */
public class AvoidKnownTrapsGoal extends Goal {
    private static final double TRAP_RADIUS = 2.5;

    private final CrowHarpy crow;
    private final double speed;
    private BlockPos trap;

    public AvoidKnownTrapsGoal(CrowHarpy crow, double speed) {
        this.crow = crow;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        BlockPos here = this.crow.blockPosition();
        this.trap = this.crow.getKnownTraps().stream().filter(t -> t.distSqr(here) <= TRAP_RADIUS * TRAP_RADIUS).findFirst().orElse(null);
        if (this.trap == null) return false;
        Vec3 away = DefaultRandomPos.getPosAway(this.crow, 6, 3, Vec3.atCenterOf(this.trap));
        if (away == null) away = this.crow.position().add(this.crow.position().subtract(Vec3.atCenterOf(this.trap)).normalize().scale(4)).add(0, 1, 0);
        this.crow.getNavigation().moveTo(away.x, away.y, away.z, this.speed);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.crow.getNavigation().isDone();
    }
}
