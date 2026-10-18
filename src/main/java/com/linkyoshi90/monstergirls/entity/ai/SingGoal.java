package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.SparrowHarpy;
import com.linkyoshi90.monstergirls.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Now and then during the day the sparrow flies up to the highest nearby spot (usually a roof or treetop)
 * and sings a little flute melody. Being fed triggers a song on the spot.
 */
public class SingGoal extends Goal {
    /** Major pentatonic, in note-block semitones (12 = pitch 1.0, 24 = pitch 2.0). */
    private static final int[] SCALE = {12, 14, 16, 19, 21, 24};
    private static final int SONG_TICKS = 80; // one loop of animation.sparrow_harpy.sing
    private static final int MAX_SEEK_TICKS = 160;

    private final SparrowHarpy harpy;
    private BlockPos perch;
    private int seekTicks;
    private int songTicks;
    private int note;

    public SingGoal(SparrowHarpy harpy) {
        this.harpy = harpy;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.harpy.getAction() != AbstractHarpy.Action.NONE) return false;
        if (this.harpy.consumeHappySong()) {
            this.perch = null;
            return true;
        }
        if (!this.harpy.level().isDay() || this.harpy.getRandom().nextInt(reducedTickDelay(2400)) != 0) return false;
        this.perch = this.findPerch();
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.isSeeking() || this.songTicks > 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.seekTicks = 0;
        this.songTicks = 0;
        this.note = 2 + this.harpy.getRandom().nextInt(2);
        if (this.perch != null) {
            this.harpy.getNavigation().moveTo(this.perch.getX() + 0.5, this.perch.getY(), this.perch.getZ() + 0.5, 1.0);
        } else {
            this.startSong();
        }
    }

    @Override
    public void stop() {
        this.perch = null;
        this.songTicks = 0;
        this.harpy.setAction(AbstractHarpy.Action.NONE);
    }

    @Override
    public void tick() {
        if (this.perch != null) {
            if (this.isSeeking()) {
                this.seekTicks++;
                if (this.harpy.getNavigation().isDone()) {
                    this.harpy.getNavigation().moveTo(this.perch.getX() + 0.5, this.perch.getY(), this.perch.getZ() + 0.5, 1.0);
                }
                return;
            }
            // Arrived (or gave up getting there): sing wherever she is now.
            this.perch = null;
            this.startSong();
        }

        this.songTicks--;
        this.harpy.getNavigation().stop();
        int beat = SONG_TICKS - this.songTicks;
        // Four phrases of four notes, each followed by a short breath.
        if (beat % 20 < 16 && beat % 4 == 0) this.playNote();
    }

    private boolean isSeeking() {
        return this.perch != null && this.seekTicks < MAX_SEEK_TICKS
                && this.harpy.distanceToSqr(Vec3.atBottomCenterOf(this.perch)) > 2.25;
    }

    private void startSong() {
        this.harpy.getNavigation().stop();
        this.harpy.setAction(AbstractHarpy.Action.SINGING);
        this.songTicks = SONG_TICKS;
    }

    private void playNote() {
        RandomSource random = this.harpy.getRandom();
        this.note = Mth.clamp(this.note + random.nextInt(5) - 2, 0, SCALE.length - 1);
        int semitone = SCALE[this.note];
        float pitch = (float) Math.pow(2.0, (semitone - 12) / 12.0);
        this.harpy.playSound(ModSounds.SPARROW_SING.get(), 0.8F, pitch);
        if (this.harpy.level() instanceof ServerLevel server) {
            // NOTE particles take their colour from the x speed when count is 0.
            server.sendParticles(ParticleTypes.NOTE, this.harpy.getX(), this.harpy.getEyeY() + 0.5, this.harpy.getZ(),
                    0, semitone / 24.0, 0.0, 0.0, 1.0);
        }
    }

    /** Highest reachable surface a few blocks above her within 8 blocks, or null if it's all flat. */
    private BlockPos findPerch() {
        Level level = this.harpy.level();
        RandomSource random = this.harpy.getRandom();
        BlockPos origin = this.harpy.blockPosition();
        BlockPos best = null;
        for (int i = 0; i < 12; i++) {
            BlockPos column = origin.offset(random.nextInt(17) - 8, 0, random.nextInt(17) - 8);
            BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, column);
            int rise = top.getY() - origin.getY();
            if (rise < 3 || rise > 16) continue;
            if (!level.getFluidState(top.below()).isEmpty()) continue;
            if (best == null || top.getY() > best.getY()) best = top;
        }
        return best;
    }
}
