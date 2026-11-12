package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.Bonded;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Told to sit (right-click with a stick), she stays where she is until told to get up the same way. It holds only
 * movement, so she still floats in water and still looks around; the sitting pose itself is her "sit" action.
 */
public class SitWhenOrderedGoal<T extends PathfinderMob & Bonded> extends Goal {
    private final T mob;

    public SitWhenOrderedGoal(T mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.mob.isOrderedToSit() && !this.inDanger();
    }

    /** A sitting girl doesn't sit and burn: she gets up, saves herself and sits again once it's over. */
    private boolean inDanger() {
        return this.mob.isOnFire() || this.mob.isInLava() || this.mob.needsWater();
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.isOrderedToSit() && !this.inDanger();
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.mob.getNavigation().stop();
        this.mob.setDeltaMovement(this.mob.getDeltaMovement().multiply(0.0, 1.0, 0.0));
    }
}
