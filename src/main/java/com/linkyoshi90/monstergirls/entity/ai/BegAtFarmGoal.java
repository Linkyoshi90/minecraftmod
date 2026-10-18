package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * During the day she flies over to a nearby field and waits at its edge for a while. Whenever a player comes
 * close she hops and chirps at them, asking for seeds or bread (feeding is handled by the entity itself).
 */
public class BegAtFarmGoal extends Goal {
    private static final int WAIT_TICKS = 900;

    private final AbstractHarpy harpy;
    private final double speed;
    private BlockPos field;
    private int ticks;
    private Player audience;

    public BegAtFarmGoal(AbstractHarpy harpy, double speed) {
        this.harpy = harpy;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!this.harpy.level().isDay() || this.harpy.isHoldingSomething()) return false;
        if (this.harpy.getRandom().nextInt(reducedTickDelay(1000)) != 0) return false;
        this.field = BlockPos.findClosestMatch(this.harpy.blockPosition(), 16, 4,
                pos -> this.harpy.level().getBlockState(pos).is(BlockTags.CROPS) && this.harpy.level().isEmptyBlock(pos.above())).orElse(null);
        return this.field != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticks < WAIT_TICKS && this.harpy.level().isDay() && !this.harpy.isHoldingSomething();
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.harpy.getNavigation().moveTo(this.field.getX() + 0.5, this.field.getY(), this.field.getZ() + 0.5, this.speed);
    }

    @Override
    public void stop() {
        this.field = null;
        this.audience = null;
        this.harpy.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.ticks++;
        boolean arrived = this.harpy.distanceToSqr(Vec3.atBottomCenterOf(this.field)) < 3.0 * 3.0;
        if (!arrived) {
            if (this.harpy.getNavigation().isDone()) {
                this.harpy.getNavigation().moveTo(this.field.getX() + 0.5, this.field.getY(), this.field.getZ() + 0.5, this.speed);
            }
            return;
        }
        if (this.ticks % 20 == 0) {
            this.audience = this.harpy.level().getNearestPlayer(this.harpy, 7.0);
            if (this.audience != null && this.audience.isSpectator()) this.audience = null;
        }
        if (this.audience == null) {
            // Peck around the field edge.
            if (this.ticks % 40 == 0) this.harpy.getLookControl().setLookAt(Vec3.atCenterOf(this.field.below()));
            return;
        }
        this.harpy.getLookControl().setLookAt(this.audience, 30.0F, 30.0F);
        if (this.ticks % 30 == 0 && this.harpy.onGround()) {
            this.harpy.getJumpControl().jump();
            this.harpy.playSound(this.harpy.getChirpSound(), 0.8F, this.harpy.getVoicePitch() * 1.15F);
        }
    }

    public boolean isBegging() {
        return this.audience != null;
    }
}
