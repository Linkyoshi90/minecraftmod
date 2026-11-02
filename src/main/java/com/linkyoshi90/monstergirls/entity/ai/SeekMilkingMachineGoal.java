package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.block.MilkingMachineBlockEntity;
import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.Lactation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import javax.annotation.Nullable;
import java.util.EnumSet;

/** With milk to give she looks for a working machine near her, goes to it and waits there until it has milked her. */
public class SeekMilkingMachineGoal<T extends PathfinderMob & Bonded> extends Goal {
    private static final int SEARCH = 24, PATIENCE = 400;
    private final T mob;
    @Nullable
    private BlockPos machine;
    private int patience;

    public SeekMilkingMachineGoal(T mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.mob.tickCount % 41 != 0 || !(this.mob.level() instanceof ServerLevel level) || !Lactation.isLactating(this.mob)) return false;
        this.machine = MilkingMachineBlockEntity.nearestFor(level, this.mob, SEARCH);
        return this.machine != null;
    }

    private boolean stillWanted() {
        return this.mob.level() instanceof ServerLevel level && this.machine != null
                && level.getBlockEntity(this.machine) instanceof MilkingMachineBlockEntity m && m.wants(this.mob, level);
    }

    @Override
    public boolean canContinueToUse() {
        return this.patience > 0 && this.stillWanted();
    }

    /** Otherwise tick() only runs on every other tick, and on some mobs the walking orders below would never fire. */
    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.patience = PATIENCE;
    }

    @Override
    public void stop() {
        this.machine = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.patience--;
        double reach = MilkingMachineBlockEntity.RADIUS;
        if (this.mob.distanceToSqr(this.machine.getCenter()) <= reach * reach) this.mob.getNavigation().stop();
        else if (this.mob.tickCount % 20 == 0) this.mob.getNavigation().moveTo(this.machine.getX() + 0.5, this.machine.getY(), this.machine.getZ() + 0.5, 1.0);
    }
}
