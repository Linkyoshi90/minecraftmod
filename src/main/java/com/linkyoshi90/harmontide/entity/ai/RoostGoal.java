package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.entity.OwlHarpy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Daytime: she flies up to the highest perch nearby (a treetop, usually) and sleeps there until dusk. Someone
 * walking right up to her wakes her; she hoots at them and dozes off again once they leave.
 */
public class RoostGoal extends Goal {
    private final OwlHarpy owl;
    @Nullable
    private BlockPos perch;
    private int ticks;
    /** Woken by a jukebox: she's off to roost somewhere out of earshot. */
    private boolean fleeingMusic;

    public RoostGoal(OwlHarpy owl) {
        this.owl = owl;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.owl.level().isDay() || this.owl.getTarget() != null || this.owl.isHoldingSomething()) return false;
        this.perch = this.findPerch();
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.owl.level().isDay() && this.owl.getTarget() == null;
    }

    @Override
    public void start() {
        this.ticks = 0;
    }

    @Override
    public void stop() {
        if (this.owl.isSleeping()) this.owl.setAction(AbstractHarpy.Action.NONE);
        this.perch = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.ticks++;
        if (this.ticks % 20 == 0) {
            BlockPos music = this.owl.getMusicNearby();
            if (music == null) {
                this.fleeingMusic = false;
            } else if (!this.fleeingMusic) {
                // Music by day! Up she gets with an irritable hoot, and off to sleep somewhere quieter.
                this.owl.wokenByMusic();
                this.owl.getLookControl().setLookAt(Vec3.atCenterOf(music));
                this.perch = this.findPerchAwayFrom(music);
                this.fleeingMusic = true;
                this.ticks = 1;                                    // a fresh time budget to get there
            }
        }
        if (this.perch != null && this.owl.distanceToSqr(Vec3.atBottomCenterOf(this.perch)) > 1.5 * 1.5 && this.ticks < 400) {
            // No way up there? Then the ground right here will have to do.
            if (this.owl.getNavigation().isDone()
                    && !this.owl.getNavigation().moveTo(this.perch.getX() + 0.5, this.perch.getY(), this.perch.getZ() + 0.5, 1.0)) {
                this.perch = null;
            }
            return;
        }
        this.owl.getNavigation().stop();
        Player close = this.owl.level().getNearestPlayer(this.owl, 2.5);
        if (close != null && !close.isSpectator() && !close.isShiftKeyDown()) {
            // Woken: open the eyes, turn and hoot at the intruder.
            if (this.owl.isSleeping()) {
                this.owl.setAction(AbstractHarpy.Action.NONE);
                this.owl.hoot();
            }
            this.owl.getLookControl().setLookAt(close);
            return;
        }
        if (this.owl.onGround() && this.owl.getAction() == AbstractHarpy.Action.NONE) this.owl.setAction(AbstractHarpy.Action.SLEEPING);
    }

    /** A perch (preferably high) on the far side from the music, out of earshot. */
    @Nullable
    private BlockPos findPerchAwayFrom(BlockPos music) {
        Vec3 away = this.owl.position().subtract(Vec3.atCenterOf(music)).multiply(1, 0, 1);
        away = away.lengthSqr() < 0.01 ? new Vec3(1, 0, 0) : away.normalize();
        BlockPos best = null;
        for (int i = 0; i < 16; i++) {
            double angle = (this.owl.getRandom().nextDouble() - 0.5) * 1.2;
            double dist = AbstractHarpy.MUSIC_RANGE + 4 + this.owl.getRandom().nextDouble() * 8;
            Vec3 dir = away.yRot((float) angle).scale(dist);
            BlockPos column = BlockPos.containing(music.getX() + 0.5 + dir.x, this.owl.getY(), music.getZ() + 0.5 + dir.z);
            BlockPos top = this.owl.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, column);
            if (Math.abs(top.getY() - this.owl.getBlockY()) > 24 || !this.owl.level().getFluidState(top.below()).isEmpty()) continue;
            if (best == null || top.getY() > best.getY()) best = top;
        }
        return best;
    }

    /** Highest surface within 10 blocks, at least 3 above her (a treetop or roof). */
    @Nullable
    private BlockPos findPerch() {
        BlockPos origin = this.owl.blockPosition();
        BlockPos best = null;
        for (int i = 0; i < 16; i++) {
            BlockPos column = origin.offset(this.owl.getRandom().nextInt(21) - 10, 0, this.owl.getRandom().nextInt(21) - 10);
            BlockPos top = this.owl.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, column);
            if (top.getY() - origin.getY() < 3 || !this.owl.level().getFluidState(top.below()).isEmpty()) continue;
            if (best == null || top.getY() > best.getY()) best = top;
        }
        return best;
    }
}
