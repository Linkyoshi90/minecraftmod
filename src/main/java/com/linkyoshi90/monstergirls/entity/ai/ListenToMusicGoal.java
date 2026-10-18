package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.JukeboxSensing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

/**
 * Hearing a jukebox play, she goes over to it (to her own spot a few blocks away, so a crowd spreads out around it),
 * turns to face it and does her species' thing for as long as the song lasts: the {@link Beat} runs every tick, and
 * {@code action} is the animation she shows meanwhile. {@code onEnd} runs if she was still there when the song ended.
 */
public class ListenToMusicGoal extends Goal {
    @FunctionalInterface
    public interface Beat {
        void tick(AbstractHarpy harpy, BlockPos jukebox, int ticks);
    }

    private final AbstractHarpy harpy;
    private final double speed;
    private final double distance;
    private final AbstractHarpy.Action action;
    private final Predicate<AbstractHarpy> when;
    private final Beat beat;
    @Nullable
    private final Beat onEnd;
    @Nullable
    private BlockPos jukebox;
    private Vec3 spot;
    private int ticks;
    private int listened;

    public ListenToMusicGoal(AbstractHarpy harpy, double speed, double distance, AbstractHarpy.Action action,
                             Predicate<AbstractHarpy> when, Beat beat, @Nullable Beat onEnd) {
        this.harpy = harpy;
        this.speed = speed;
        this.distance = distance;
        this.action = action;
        this.when = when;
        this.beat = beat;
        this.onEnd = onEnd;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        // Odd interval: goals are only polled every other tick, so an even one would never fire for half of all mobs.
        if (this.harpy.tickCount % 11 != 0 || this.harpy.isHoldingSomething() || this.harpy.getAction() != AbstractHarpy.Action.NONE) return false;
        if (!this.when.test(this.harpy)) return false;
        this.jukebox = this.harpy.getMusicNearby();
        return this.jukebox != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.jukebox != null && this.ticks < 20 * 60 * 8 && this.when.test(this.harpy) && !this.harpy.isHoldingSomething()
                && JukeboxSensing.isPlaying(this.harpy.level(), this.jukebox);
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.listened = 0;
        double angle = this.harpy.getRandom().nextDouble() * Math.PI * 2.0;
        this.spot = Vec3.atBottomCenterOf(this.jukebox).add(Math.cos(angle) * this.distance, 0.0, Math.sin(angle) * this.distance);
    }

    @Override
    public void stop() {
        if (this.jukebox != null && this.listened > 0 && this.onEnd != null && !JukeboxSensing.isPlaying(this.harpy.level(), this.jukebox)) {
            this.onEnd.tick(this.harpy, this.jukebox, this.listened);
        }
        if (this.harpy.getAction() == this.action) this.harpy.setAction(AbstractHarpy.Action.NONE);
        this.jukebox = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /** Whether she has made it over to the music and is listening (for tests). */
    public boolean isListening() {
        return this.jukebox != null && this.listened > 0;
    }

    /** Sings (or plays) one note along with the music, with a note particle over her head. */
    public static void note(AbstractHarpy harpy, SoundEvent sound, int semitone, float volume) {
        harpy.playSound(sound, volume, (float) Math.pow(2.0, (semitone - 12) / 12.0));
        if (harpy.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.NOTE, harpy.getX(), harpy.getEyeY() + 0.5, harpy.getZ(), 0, semitone / 24.0, 0.0, 0.0, 1.0);
        }
    }

    @Override
    public void tick() {
        this.ticks++;
        double dx = this.harpy.getX() - this.spot.x, dz = this.harpy.getZ() - this.spot.z;
        boolean there = dx * dx + dz * dz < 1.2 * 1.2 && Math.abs(this.harpy.getY() - this.spot.y) < 2.0;
        if (!there && this.ticks < 300) {
            if (this.harpy.getAction() == this.action) this.harpy.setAction(AbstractHarpy.Action.NONE);
            if (this.ticks % 10 == 1) this.harpy.getNavigation().moveTo(this.spot.x, this.spot.y, this.spot.z, this.speed);
            if (this.harpy.isAirborne()) this.harpy.getMoveControl().setWantedPosition(this.spot.x, this.spot.y, this.spot.z, this.speed);
            return;
        }
        // Close enough (or as close as she could get): settle down and enjoy it.
        this.harpy.getNavigation().stop();
        this.harpy.getLookControl().setLookAt(this.jukebox.getX() + 0.5, this.jukebox.getY() + 0.8, this.jukebox.getZ() + 0.5);
        if (this.action != AbstractHarpy.Action.NONE && this.harpy.getAction() == AbstractHarpy.Action.NONE) this.harpy.setAction(this.action);
        this.beat.tick(this.harpy, this.jukebox, ++this.listened);
    }
}
