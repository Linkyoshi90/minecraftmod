package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.entity.Orders;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * A water bird's paddle (entity/Traversal): now and then she makes for open water within {@link #REACH} and floats about on
 * it for a while, drifting from spot to spot. Left be when she is under orders, carrying someone or fighting.
 */
public class PaddleGoal extends Goal {
    public static final int REACH = 12, DRIFT = 6;
    private final AbstractHarpy bird;
    private int stay;

    public PaddleGoal(AbstractHarpy bird) {
        this.bird = bird;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    private boolean free() {
        return !Orders.commanded(this.bird) && !this.bird.isOrderedToSit() && !this.bird.isVehicle() && !this.bird.isPassenger() && this.bird.getTarget() == null;
    }

    @Override
    public boolean canUse() {
        if (!this.free() || this.bird.getRandom().nextInt(this.bird.isInWater() ? 60 : 400) != 0) return false;
        Vec3 spot = this.waterSpot(this.bird.isInWater() ? DRIFT : REACH);
        if (spot == null) return false;
        this.stay = 300 + this.bird.getRandom().nextInt(900);
        return this.bird.getNavigation().moveTo(spot.x, spot.y, spot.z, 0.8);
    }

    @Override
    public boolean canContinueToUse() {
        return this.stay > 0 && this.free();
    }

    @Override
    public void tick() {
        this.stay--;
        if (this.bird.getNavigation().isDone() && this.bird.isInWater() && this.bird.getRandom().nextInt(80) == 0) {
            Vec3 next = this.waterSpot(DRIFT);
            if (next != null) this.bird.getNavigation().moveTo(next.x, next.y, next.z, 0.6);
        }
    }

    @Override
    public void stop() {
        this.stay = 0;
    }

    /** Open water within reach (a water block with air over it), as the spot on its surface she floats at. */
    @Nullable
    public Vec3 waterSpot(int reach) {
        BlockPos at = this.bird.blockPosition();
        for (int i = 0; i < 16; i++) {
            BlockPos p = at.offset(this.bird.getRandom().nextInt(reach * 2 + 1) - reach, this.bird.getRandom().nextInt(7) - 4, this.bird.getRandom().nextInt(reach * 2 + 1) - reach);
            if (this.bird.level().getFluidState(p).is(FluidTags.WATER) && this.bird.level().getBlockState(p.above()).isAir()) {
                return new Vec3(p.getX() + 0.5, p.getY() + 0.7, p.getZ() + 0.5);
            }
        }
        return null;
    }
}
