package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.CrowCaches;
import com.linkyoshi90.monstergirls.entity.CrowHarpy;
import com.linkyoshi90.monstergirls.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * A crow with something she'd rather keep flies off to a patch of soft ground nearby (out of everyone's sight if
 * she can manage it), scratches a hole, tucks the item in and pats the dirt flat. The item is buried in that block
 * (see {@link CrowCaches}): whoever digs the block up finds it.
 */
public class CacheItemGoal extends Goal {
    private static final int DIG_TICKS = 40;
    private static final int RANGE = 10;

    private final CrowHarpy crow;
    private final double speed;
    @Nullable
    private BlockPos ground;
    private int ticks;
    private int digTicks;

    public CacheItemGoal(CrowHarpy crow, double speed) {
        this.crow = crow;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!this.crow.wantsToCache() || !this.crow.isHoldingSomething() || !(this.crow.level() instanceof ServerLevel level)) return false;
        // Odd interval: goals are only polled every other tick, so an even one never fires for half of all crows.
        if (this.crow.tickCount % 21 != 0) return false;
        this.ground = this.findSpot(level);
        return this.ground != null;
    }

    @Nullable
    private BlockPos findSpot(ServerLevel level) {
        CrowCaches caches = CrowCaches.get(level);
        BlockPos fallback = null;
        for (int i = 0; i < 24; i++) {
            int x = this.crow.getBlockX() + this.crow.getRandom().nextInt(RANGE * 2 + 1) - RANGE;
            int z = this.crow.getBlockZ() + this.crow.getRandom().nextInt(RANGE * 2 + 1) - RANGE;
            BlockPos ground = this.groundBelow(level, x, z);
            if (ground == null || !level.getBlockState(ground).is(ModTags.CROW_HARPY_CACHE_GROUND)) continue;
            BlockPos top = ground.above();
            if (!level.isEmptyBlock(top.above()) || !this.crow.isSafeSpot(top) || !this.canSee(level, top)) continue;
            if (caches.anyNear(ground, 2.0) || this.crow.shouldAvoidCachingNear(ground)) continue;
            // Somewhere nobody's standing around to watch, if there is such a place.
            if (!this.anyoneNear(level, top)) return ground;
            if (fallback == null) fallback = ground;
        }
        return fallback;
    }

    /** The first solid ground under her flight height in that column (not a heightmap: those see roofs and caves wrong). */
    @Nullable
    private BlockPos groundBelow(ServerLevel level, int x, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, this.crow.getBlockY() + 3, z);
        if (!level.isEmptyBlock(pos)) return null;                     // starting inside something: not a spot she can fly to
        for (int i = 0; i < 12; i++) {
            pos.move(0, -1, 0);
            if (!level.isEmptyBlock(pos)) return level.getBlockState(pos).blocksMotion() ? pos.immutable() : null;
        }
        return null;
    }

    /** A clear line from her eyes to the spot, so she isn't trying to fly through a wall to get there. */
    private boolean canSee(ServerLevel level, BlockPos top) {
        Vec3 target = Vec3.atCenterOf(top);
        return level.clip(new ClipContext(this.crow.getEyePosition(), target, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this.crow))
                .getType() == HitResult.Type.MISS;
    }

    private boolean anyoneNear(ServerLevel level, BlockPos pos) {
        for (Player player : level.players()) {
            if (!player.isSpectator() && !this.crow.isOwnedBy(player) && player.distanceToSqr(pos.getBottomCenter()) < 8 * 8) return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticks < 400 && this.ground != null && this.crow.wantsToCache() && this.crow.isHoldingSomething()
                && this.crow.level().getBlockState(this.ground).is(ModTags.CROW_HARPY_CACHE_GROUND)
                && this.crow.level().isEmptyBlock(this.ground.above());
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.digTicks = 0;
    }

    @Override
    public void stop() {
        if (this.crow.getAction() == AbstractHarpy.Action.CACHING) this.crow.setAction(AbstractHarpy.Action.NONE);
        this.ground = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.ticks++;
        if (!GroundVisit.arrive(this.crow, this.ground.above(), this.speed, this.ticks)) return;
        this.crow.getLookControl().setLookAt(this.ground.getX() + 0.5, this.ground.getY() + 1.0, this.ground.getZ() + 0.5);
        this.crow.setAction(AbstractHarpy.Action.CACHING);
        GroundVisit.scratch(this.crow, this.ground, ++this.digTicks);
        if (this.digTicks >= DIG_TICKS) this.crow.buryHeldItem(this.ground);
    }

    /** Shared by caching and digging a cache back up: getting down to a spot on the ground, and scratching at it. */
    static final class GroundVisit {
        private GroundVisit() {}

        /** Steers her onto the block; true once she stands on it. */
        static boolean arrive(CrowHarpy crow, BlockPos spot, double speed, int ticks) {
            double dx = crow.getX() - (spot.getX() + 0.5), dz = crow.getZ() - (spot.getZ() + 0.5);
            if (dx * dx + dz * dz > 0.6 * 0.6 || crow.getY() > spot.getY() + 1.0) {
                if (ticks % 10 == 1) crow.getNavigation().moveTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, speed);
                crow.getMoveControl().setWantedPosition(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, speed);
                return false;
            }
            crow.getNavigation().stop();
            return true;
        }

        /** Dirt flying and scratching sounds. */
        static void scratch(CrowHarpy crow, BlockPos ground, int digTicks) {
            if (digTicks % 8 == 1 && crow.level() instanceof ServerLevel level) {
                BlockState state = level.getBlockState(ground);
                crow.playSound(state.getSoundType(level, ground, crow).getHitSound(), 0.8F, 1.1F);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), ground.getX() + 0.5, ground.getY() + 1.05,
                        ground.getZ() + 0.5, 6, 0.2, 0.05, 0.2, 0.05);
            }
        }
    }
}
