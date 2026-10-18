package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.function.Supplier;

/** Pet-style following of a bonded player: flies after them, and catches up by teleporting when left far behind. */
public class FollowLeaderGoal extends Goal {
    private final AbstractHarpy harpy;
    private final Supplier<Player> leader;
    private final double speed;
    private final float startDistance;
    private final float stopDistance;
    private Player target;
    private int repathDelay;

    public FollowLeaderGoal(AbstractHarpy harpy, Supplier<Player> leader, double speed, float startDistance, float stopDistance) {
        this.harpy = harpy;
        this.leader = leader;
        this.speed = speed;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        Player player = this.leader.get();
        if (player == null || player.isSpectator() || this.harpy.distanceToSqr(player) < this.startDistance * this.startDistance) return false;
        this.target = player;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target.isAlive() && this.leader.get() == this.target
                && this.harpy.distanceToSqr(this.target) > this.stopDistance * this.stopDistance;
    }

    @Override
    public void start() {
        this.repathDelay = 0;
    }

    @Override
    public void stop() {
        this.target = null;
        this.harpy.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.harpy.getLookControl().setLookAt(this.target, 10.0F, this.harpy.getMaxHeadXRot());
        if (--this.repathDelay > 0) return;
        this.repathDelay = adjustedTickDelay(10);
        if (this.harpy.distanceToSqr(this.target) > 24 * 24) {
            this.teleportNearTarget();
        } else {
            this.harpy.getNavigation().moveTo(this.target, this.speed);
        }
    }

    private void teleportNearTarget() {
        BlockPos center = this.target.blockPosition();
        for (int i = 0; i < 10; i++) {
            BlockPos pos = center.offset(this.harpy.getRandom().nextInt(7) - 3, this.harpy.getRandom().nextInt(3), this.harpy.getRandom().nextInt(7) - 3);
            if (pos.distManhattan(center) < 2) continue;
            double x = pos.getX() + 0.5, y = pos.getY(), z = pos.getZ() + 0.5;
            if (this.harpy.level().noCollision(this.harpy, this.harpy.getBoundingBox().move(x - this.harpy.getX(), y - this.harpy.getY(), z - this.harpy.getZ()))
                    && !this.harpy.level().getBlockState(pos.below()).isAir()) {
                this.harpy.moveTo(x, y, z, this.harpy.getYRot(), this.harpy.getXRot());
                this.harpy.getNavigation().stop();
                return;
            }
        }
    }
}
