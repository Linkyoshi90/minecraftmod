package com.linkyoshi90.harmontide.entity.girl;

import com.linkyoshi90.harmontide.entity.Orders;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * A day for the day girls, a day for the night girls: she lies down and sleeps through her off hours (the night, or
 * the day if she is nocturnal, see AbstractMonsterGirl#isNocturnal) wherever she is, on solid ground. A hit, a
 * player's hand or a target wakes her for half a minute; joining her player in bed (ShareBedGoal) comes first. Under a
 * follow order she stays up and keeps up. The nekomata keep their own naps and merfolk rest in the water their own way.
 */
public class SleepGoal extends Goal {
    private final AbstractMonsterGirl girl;
    private int cooldown;

    public SleepGoal(AbstractMonsterGirl girl) {
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    /** Her sleeping hours: night for most, day for the nocturnal. */
    public boolean bedtime() {
        return this.girl.restingHours();
    }

    /** Wild and out in the open, she first beds down somewhere covered (under trees, an overhang, a roof). */
    @javax.annotation.Nullable private net.minecraft.core.BlockPos shelter;
    private int walking;

    @javax.annotation.Nullable
    public static net.minecraft.core.BlockPos cover(AbstractMonsterGirl g) {
        var level = g.level();
        var r = g.getRandom();
        net.minecraft.core.BlockPos at = g.blockPosition();
        for (int i = 0; i < 24; i++) {
            net.minecraft.core.BlockPos p = at.offset(r.nextInt(25) - 12, r.nextInt(7) - 3, r.nextInt(25) - 12);
            if (level.isEmptyBlock(p) && level.isEmptyBlock(p.above()) && level.getBlockState(p.below()).isFaceSturdy(level, p.below(), net.minecraft.core.Direction.UP)
                    && !level.canSeeSky(p.above()) && level.getFluidState(p).isEmpty()) return p;
        }
        return null;
    }

    private boolean mayStayAsleep() {
        return this.bedtime() && this.girl.getTarget() == null && !this.girl.isInWater() && !this.girl.isPassenger() && !this.girl.isVehicle()
                && this.girl.level().getGameTime() >= this.girl.getWokenUntil()
                && this.girl.getOrders().command(this.girl) != Orders.Command.FOLLOW;
    }

    @Override
    public boolean canUse() {
        if (--this.cooldown > 0) return false;
        this.cooldown = 20;
        GirlSpecies.Family f = this.girl.getSpecies().family();
        if (f == GirlSpecies.Family.NEKOMATA || f == GirlSpecies.Family.MERFOLK) return false;
        return this.girl.onGround() && this.girl.getAction() == AbstractMonsterGirl.Action.NONE && this.mayStayAsleep();
    }

    @Override
    public boolean canContinueToUse() {
        return this.mayStayAsleep();
    }

    @Override
    public void start() {
        this.girl.getNavigation().stop();
        this.walking = 0;
        this.shelter = !this.girl.hasAnyAffection() && this.girl.level().canSeeSky(this.girl.blockPosition().above()) ? cover(this.girl) : null;
        if (this.shelter == null) this.girl.setAction(AbstractMonsterGirl.Action.LYING);
    }

    @Override
    public void tick() {
        if (this.shelter != null) {                                         // on her way to somewhere covered first
            if (++this.walking > 200 || this.girl.blockPosition().closerThan(this.shelter, 1.5)) {
                this.shelter = null;
                this.girl.setAction(AbstractMonsterGirl.Action.LYING);
            } else {
                if (this.walking % 20 == 1) this.girl.getNavigation().moveTo(this.shelter.getX() + 0.5, this.shelter.getY(), this.shelter.getZ() + 0.5, 0.9);
                return;
            }
        }
        this.girl.getNavigation().stop();
        this.girl.setDeltaMovement(this.girl.getDeltaMovement().multiply(0.0, 1.0, 0.0));
        if (this.girl.getAction() == AbstractMonsterGirl.Action.NONE) this.girl.setAction(AbstractMonsterGirl.Action.LYING);
    }

    @Override
    public void stop() {
        if (this.girl.getAction() == AbstractMonsterGirl.Action.LYING) this.girl.setAction(AbstractMonsterGirl.Action.NONE);
    }

    public boolean isSleeping() {
        return this.girl.getAction() == AbstractMonsterGirl.Action.LYING && this.mayStayAsleep();
    }
}
