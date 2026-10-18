package com.linkyoshi90.harpies.entity.ai;

import com.linkyoshi90.harpies.entity.AbstractHarpy;
import com.linkyoshi90.harpies.entity.CrowHarpy;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * A loyal crow with her owner nearby sometimes notices an unopened treasure chest (a world-generated container
 * whose loot hasn't been rolled yet: dungeons, buried treasure, ruins...). She flies to the ground right above
 * it, hops up and down and caws until her owner comes over, and doesn't show the same chest twice.
 */
public class PointOutTreasureGoal extends Goal {
    private static final int SEARCH_CHUNKS = 2;
    private static final int MAX_DEPTH = 40;

    private final CrowHarpy crow;
    private final double speed;
    private BlockPos chest;
    private BlockPos marker;
    private int ticks;

    public PointOutTreasureGoal(CrowHarpy crow, double speed) {
        this.crow = crow;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        Player owner = this.crow.getOwner();
        if (owner == null || this.crow.isHoldingSomething() || this.crow.distanceToSqr(owner) > 24 * 24) return false;
        if (this.crow.getRandom().nextInt(reducedTickDelay(400)) != 0) return false;
        this.chest = this.findTreasure(owner);
        if (this.chest == null) return false;
        this.marker = this.crow.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.chest);
        return true;
    }

    @Nullable
    private BlockPos findTreasure(Player owner) {
        if (!(this.crow.level() instanceof ServerLevel level)) return null;
        BlockPos origin = owner.blockPosition();
        BlockPos best = null;
        for (int cx = -SEARCH_CHUNKS; cx <= SEARCH_CHUNKS; cx++) {
            for (int cz = -SEARCH_CHUNKS; cz <= SEARCH_CHUNKS; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow((origin.getX() >> 4) + cx, (origin.getZ() >> 4) + cz);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    BlockPos pos = be.getBlockPos();
                    if (!(be instanceof RandomizableContainerBlockEntity container) || container.getLootTable() == null) continue;
                    if (pos.getY() < origin.getY() - MAX_DEPTH || pos.getY() > origin.getY() + 8 || this.crow.hasShownChest(pos)) continue;
                    if (best == null || pos.distSqr(origin) < best.distSqr(origin)) best = pos.immutable();
                }
            }
        }
        return best;
    }

    @Override
    public boolean canContinueToUse() {
        Player owner = this.crow.getOwner();
        return owner != null && this.ticks < 1200 && !this.ownerArrived(owner);
    }

    private boolean ownerArrived(Player owner) {
        double dx = owner.getX() - (this.marker.getX() + 0.5), dz = owner.getZ() - (this.marker.getZ() + 0.5);
        return dx * dx + dz * dz < 2.5 * 2.5;
    }

    @Override
    public void start() {
        this.ticks = 0;
    }

    @Override
    public void stop() {
        Player owner = this.crow.getOwner();
        // Done once her owner has come to look (or she has waited a full minute).
        if (this.chest != null && (this.ticks >= 1200 || owner != null && this.ownerArrived(owner))) this.crow.markChestShown(this.chest);
        this.chest = null;
        this.marker = null;
    }

    @Override
    public void tick() {
        this.ticks++;
        double dx = this.crow.getX() - (this.marker.getX() + 0.5), dz = this.crow.getZ() - (this.marker.getZ() + 0.5);
        if (dx * dx + dz * dz > 0.6 * 0.6 || this.crow.getY() > this.marker.getY() + 1.5) {
            if (this.ticks % 10 == 1) this.crow.getNavigation().moveTo(this.marker.getX() + 0.5, this.marker.getY(), this.marker.getZ() + 0.5, this.speed);
            this.crow.getMoveControl().setWantedPosition(this.marker.getX() + 0.5, this.marker.getY(), this.marker.getZ() + 0.5, this.speed);
            return;
        }
        // Here! Bounce and caw at the owner.
        this.crow.getNavigation().stop();
        Player owner = this.crow.getOwner();
        if (owner != null) this.crow.getLookControl().setLookAt(owner);
        if (this.crow.onGround() && this.ticks % 12 == 0) this.crow.getJumpControl().jump();
        if (this.ticks % 40 == 0) this.crow.caw(15);
    }

    public boolean isPointing() {
        return this.marker != null && this.crow.distanceToSqr(this.marker.getBottomCenter()) < 1.5 * 1.5;
    }

    /** For tests. */
    @Nullable
    public BlockPos getChest() {
        return this.chest;
    }
}
