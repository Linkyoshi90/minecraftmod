package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.block.FridgeBlockEntity;
import com.linkyoshi90.monstergirls.entity.Bonded;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import javax.annotation.Nullable;
import java.util.EnumSet;

/** Every five to ten minutes she wanders over to a fridge that feeds her (FridgeBlockEntity.feeds) and helps herself to something. */
public class FridgeGoal<T extends PathfinderMob & Bonded> extends Goal {
    public static final int SEARCH = 16, EVERY = 6000, PATIENCE = 400;
    public static final String NEXT = "FridgeAt";
    private final T mob;
    @Nullable
    private BlockPos fridge;
    private int patience;

    public FridgeGoal(T mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.tickCount % 53 != 0 || !(this.mob.level() instanceof ServerLevel level) || this.mob.isPassenger()) return false;
        long next = this.mob.getPersistentData().getLong(NEXT);
        if (next == 0) {                                     // her first look comes a while after she arrives
            this.mob.getPersistentData().putLong(NEXT, level.getGameTime() + 1 + this.mob.getRandom().nextInt(EVERY));
            return false;
        }
        if (level.getGameTime() < next) return false;
        this.fridge = FridgeBlockEntity.nearestFor(level, this.mob, SEARCH);
        if (this.fridge == null) this.mob.getPersistentData().putLong(NEXT, level.getGameTime() + 1200);   // none near: look again in a minute
        return this.fridge != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.patience > 0 && this.fridge != null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.patience = PATIENCE;
        this.mob.getNavigation().moveTo(this.fridge.getX() + 0.5, this.fridge.getY(), this.fridge.getZ() + 0.5, 1.0);
    }

    @Override
    public void stop() {
        if (this.fridge != null) this.mob.getPersistentData().putLong(NEXT, this.mob.level().getGameTime() + 1200);   // gave up: later
        this.fridge = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.fridge == null) return;                    // she has eaten: the goal ends at the next check (ticks can come in between)
        this.patience--;
        this.mob.getLookControl().setLookAt(this.fridge.getX() + 0.5, this.fridge.getY() + 0.5, this.fridge.getZ() + 0.5);
        if (this.mob.distanceToSqr(this.fridge.getCenter()) > 2.5 * 2.5) {
            if (this.mob.tickCount % 20 == 0) this.mob.getNavigation().moveTo(this.fridge.getX() + 0.5, this.fridge.getY(), this.fridge.getZ() + 0.5, 1.0);
            return;
        }
        this.mob.getNavigation().stop();
        if (this.mob.level() instanceof ServerLevel level && level.getBlockEntity(this.fridge) instanceof FridgeBlockEntity f && f.feeds(this.mob, level)) f.serve(level, this.mob);
        this.mob.getPersistentData().putLong(NEXT, this.mob.level().getGameTime() + EVERY + this.mob.getRandom().nextInt(EVERY));
        this.fridge = null;
    }
}
