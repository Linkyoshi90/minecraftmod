package com.linkyoshi90.monstergirls.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Vanilla's door goals only work for ground navigation, so this one is for flyers: opens wooden doors on her
 * current path as she reaches them, steers her through at floor level (flying paths like to cross a doorway at
 * the height of the door's upper half, where her head hits the lintel), resumes her route, and closes the door
 * once she's through. Uses no goal flags, so it runs alongside whatever she's actually doing.
 */
public class OpenDoorsGoal extends Goal {
    private static final int MAX_THREAD_TICKS = 60;

    private final Mob mob;
    private final List<BlockPos> openedDoors = new ArrayList<>();
    /** Lower half of the door she's currently passing through, or null. */
    private BlockPos threading;
    private Vec3 threadDirection;
    private BlockPos resumeTarget;
    private int threadTicks;

    public OpenDoorsGoal(Mob mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.threading != null) {
            this.threadThroughDoor();
        } else {
            Path path = this.mob.getNavigation().getPath();
            if (path != null && !path.isDone()) {
                // Look at the next couple of path nodes for a closed door within reach.
                for (int i = path.getNextNodeIndex(); i < Math.min(path.getNextNodeIndex() + 2, path.getNodeCount()); i++) {
                    Node node = path.getNode(i);
                    for (int dy = -1; dy <= 1; dy++) {
                        BlockPos pos = new BlockPos(node.x, node.y + dy, node.z);
                        if (this.mob.distanceToSqr(pos.getCenter()) <= 2.25 * 2.25 && this.open(pos, path)) return;
                    }
                }
            }
        }
        // Close doors behind her.
        this.openedDoors.removeIf(pos -> {
            if (pos.equals(this.threading) || this.mob.distanceToSqr(pos.getCenter()) < 2.5 * 2.5) return false;
            BlockState state = this.mob.level().getBlockState(pos);
            if (state.getBlock() instanceof DoorBlock door && door.isOpen(state)) door.setOpen(this.mob, this.mob.level(), state, pos, false);
            return true;
        });
    }

    private boolean open(BlockPos pos, Path path) {
        BlockState state = this.mob.level().getBlockState(pos);
        if (!DoorBlock.isWoodenDoor(state) || !(state.getBlock() instanceof DoorBlock door) || door.isOpen(state)) return false;
        BlockPos lower = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        door.setOpen(this.mob, this.mob.level(), state, lower, true);
        this.openedDoors.add(lower.immutable());

        Vec3 toDoor = Vec3.atBottomCenterOf(lower).subtract(this.mob.position()).multiply(1, 0, 1);
        if (toDoor.lengthSqr() < 1.0E-4) return true;
        this.threading = lower.immutable();
        this.threadDirection = toDoor.normalize();
        this.resumeTarget = path.getTarget();
        this.threadTicks = 0;
        this.mob.getNavigation().stop();
        return true;
    }

    private void threadThroughDoor() {
        Vec3 door = Vec3.atBottomCenterOf(this.threading);
        boolean through = this.mob.position().subtract(door).dot(this.threadDirection) > 0.9;
        if (through || ++this.threadTicks > MAX_THREAD_TICKS) {
            this.threading = null;
            if (this.resumeTarget != null) {
                this.mob.getNavigation().moveTo(this.resumeTarget.getX() + 0.5, this.resumeTarget.getY(), this.resumeTarget.getZ() + 0.5, 1.0);
            }
            return;
        }
        Vec3 aim = door.add(this.threadDirection.scale(1.6));
        this.mob.getNavigation().stop(); // other goals may have re-pathed meanwhile; their route resumes once she's through
        this.mob.getMoveControl().setWantedPosition(aim.x, door.y, aim.z, 1.0);
    }
}
