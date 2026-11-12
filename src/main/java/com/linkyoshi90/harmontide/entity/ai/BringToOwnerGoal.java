package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.entity.CrowHarpy;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/** A loyal crow carrying something shiny flies it over and drops it at her owner's feet. */
public class BringToOwnerGoal extends Goal {
    private final CrowHarpy crow;
    private final double speed;
    private Player owner;
    private int ticks;

    public BringToOwnerGoal(CrowHarpy crow, double speed) {
        this.crow = crow;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.crow.likesToCarry(this.crow.getMainHandItem())) return false;
        this.owner = this.crow.getOwner();
        return this.owner != null && this.crow.distanceToSqr(this.owner) < 64 * 64;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticks < 400 && this.crow.isHoldingSomething() && this.owner.isAlive() && this.crow.getOwner() == this.owner;
    }

    @Override
    public void start() {
        this.ticks = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.crow.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.ticks++;
        this.crow.getLookControl().setLookAt(this.owner, 30.0F, 30.0F);
        if (this.crow.distanceToSqr(this.owner) > 2.0 * 2.0) {
            if (this.ticks % 10 == 1) this.crow.getNavigation().moveTo(this.owner, this.speed);
            return;
        }
        this.crow.dropHeldItem();
        this.crow.startTimedAction(AbstractHarpy.Action.PRANKING, 40);
    }
}
