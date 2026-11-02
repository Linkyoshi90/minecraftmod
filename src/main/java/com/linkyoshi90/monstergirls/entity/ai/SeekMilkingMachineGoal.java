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
    private static final int SEARCH = 24, PATIENCE = 600, UNSTICK = 4;
    private final T mob;
    @Nullable
    private BlockPos machine;
    /** Where she means to stand: a free spot beside it. Walking at the machine itself leaves big girls shoving at corners. */
    @Nullable
    private BlockPos spot;
    private net.minecraft.world.phys.Vec3 lastPos = net.minecraft.world.phys.Vec3.ZERO;
    private int patience, stuck;

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
        this.stuck = 0;
        this.lastPos = this.mob.position();
        this.spot = this.pickSpot();
    }

    /** A random spot round the machine she has room to stand on, or the machine's own position with none. */
    private BlockPos pickSpot() {
        java.util.List<BlockPos> free = new java.util.ArrayList<>();
        for (BlockPos p : BlockPos.betweenClosed(this.machine.offset(-1, -1, -1), this.machine.offset(1, 1, 1))) {
            if (this.mob.level().getBlockState(p.below()).isFaceSturdy(this.mob.level(), p.below(), net.minecraft.core.Direction.UP)
                    && this.mob.level().noCollision(this.mob, this.mob.getType().getSpawnAABB(p.getX() + 0.5, p.getY(), p.getZ() + 0.5))) free.add(p.immutable());
        }
        return free.isEmpty() ? this.machine : free.get(this.mob.getRandom().nextInt(free.size()));
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
        if (this.mob.distanceToSqr(this.machine.getCenter()) <= reach * reach) {
            this.mob.getNavigation().stop();
            this.mob.blushFor(40);
            return;
        }
        if (this.mob.tickCount % 20 == 0) this.mob.getNavigation().moveTo(this.spot.getX() + 0.5, this.spot.getY(), this.spot.getZ() + 0.5, 1.0);
        if (this.mob.tickCount % 40 != 0) return;
        if (this.mob.position().distanceToSqr(this.lastPos) >= 1.0) {
            this.stuck = 0;
        } else if (++this.stuck >= UNSTICK) {                  // the walk has failed: she steps across the last of the way
            this.mob.teleportTo(this.spot.getX() + 0.5, this.spot.getY(), this.spot.getZ() + 0.5);
            this.mob.getNavigation().stop();
        } else {                                               // a hop over the lip of a low block, and try another side
            this.mob.getJumpControl().jump();
            this.spot = this.pickSpot();
        }
        this.lastPos = this.mob.position();
    }
}
