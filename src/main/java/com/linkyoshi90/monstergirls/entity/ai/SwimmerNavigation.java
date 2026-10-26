package com.linkyoshi90.monstergirls.entity.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Amphibious navigation for swimmers that keep their momentum (merfolk, the sea snake, mollusks, penguins). Vanilla
 * only counts a waypoint as reached within half her width, which a gliding swimmer keeps overshooting, so she circles
 * it; in the water anything within 1.5 blocks counts.
 */
public class SwimmerNavigation extends AmphibiousPathNavigation {
    private static final double REACHED_SQR = 1.5 * 1.5;

    public SwimmerNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected void followThePath() {
        super.followThePath();
        if (this.path != null && !this.path.isDone() && this.mob.isInWater()
                && this.mob.position().distanceToSqr(Vec3.atBottomCenterOf(this.path.getNextNodePos())) < REACHED_SQR) {
            this.path.advance();
        }
    }
}
