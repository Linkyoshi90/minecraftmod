package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * Walks up to someone standing around on land and pesters them: stands a couple of blocks away, holds out her
 * hands and squawks, hopping on the spot, until they feed her or she gives up.
 */
public class BegForFoodGoal extends Goal {
    private final AbstractHarpy harpy;
    private Player mark;
    private int ticks;
    private long nextTime;

    public BegForFoodGoal(AbstractHarpy harpy) {
        this.harpy = harpy;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    private boolean isMark(Player player) {
        // Standing on solid land (checked by block: a player's onGround flag is only as fresh as their last move packet).
        return player.isAlive() && !player.isSpectator() && player.getVehicle() == null && !player.isInWater()
                && player.level().getBlockState(player.blockPosition().below()).isSolid();
    }

    @Override
    public boolean canUse() {
        if (this.harpy.level().getGameTime() < this.nextTime || this.harpy.isHoldingSomething() || com.linkyoshi90.harmontide.entity.Orders.holdsPlace(this.harpy)) return false;
        if (this.harpy.getRandom().nextInt(reducedTickDelay(600)) != 0) return false;
        this.mark = this.harpy.level().getNearestPlayer(this.harpy.getX(), this.harpy.getY(), this.harpy.getZ(), 12.0,
                e -> e instanceof Player p && this.isMark(p));
        return this.mark != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticks < 300 && this.isMark(this.mark) && this.harpy.distanceToSqr(this.mark) < 16 * 16;
    }

    @Override
    public void start() {
        this.ticks = 0;
    }

    @Override
    public void stop() {
        if (this.harpy.getAction() == AbstractHarpy.Action.BEGGING) this.harpy.setAction(AbstractHarpy.Action.NONE);
        this.mark = null;
        this.nextTime = this.harpy.level().getGameTime() + 1200 + this.harpy.getRandom().nextInt(1200);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.ticks++;
        this.harpy.getLookControl().setLookAt(this.mark, 30.0F, 30.0F);
        if (this.harpy.distanceToSqr(this.mark) > 3.0 * 3.0) {
            if (this.harpy.getAction() == AbstractHarpy.Action.BEGGING) this.harpy.setAction(AbstractHarpy.Action.NONE);
            if (this.ticks % 10 == 1) this.harpy.getNavigation().moveTo(this.mark, 1.0);
            return;
        }
        this.harpy.getNavigation().stop();
        this.harpy.setAction(AbstractHarpy.Action.BEGGING);
        if (this.ticks % 24 == 0) this.harpy.playSound(this.harpy.getChirpSound(), 1.0F, this.harpy.getVoicePitch() * 1.1F);
    }
}
