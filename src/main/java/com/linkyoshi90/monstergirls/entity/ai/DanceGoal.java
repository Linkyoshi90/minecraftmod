package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.RiflebirdHarpy;
import com.linkyoshi90.monstergirls.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.List;

/**
 * The riflebird's display: she picks an audience (a player, more eagerly one she likes, or another riflebird),
 * plants herself a few blocks in front of them and dances to her own little pop groove - a funky bassline,
 * hi-hats and a bright melody on note-block sounds. Since 2026-10-30 a tense, driving 80s pop groove in a minor key, a nod to the
 * viral videos of birds dancing to one (an original pattern, not anyone's song).
 */
public class DanceGoal extends Goal {
    /** 32-step loop at 3 ticks per step: a pulsing bass (semitones, -1 = rest), off-beat hats, short stabs of melody. */
    private static final int[] BASS = {0, -1, 0, -1, 0, -1, 0, 3, 5, -1, 5, -1, 3, -1, 0, -1,
            0, -1, 0, -1, 0, -1, 0, 3, 7, -1, 7, -1, 5, 3, -1, -1};
    private static final boolean[] HAT = {false, true, false, true, false, true, true, true, false, true, false, true, false, true, true, true,
            false, true, false, true, false, true, true, true, false, true, false, true, false, true, true, false};
    private static final int[] MELODY = {-1, -1, 12, 12, -1, 15, -1, -1, 17, -1, 15, -1, 12, -1, -1, -1,
            -1, -1, 12, 12, -1, 15, -1, 17, 19, -1, 17, 15, -1, 12, -1, -1};
    private static final int STEP_TICKS = 3;

    private final RiflebirdHarpy bird;
    private LivingEntity audience;
    private int ticks;
    private int duration;
    private long nextTime;

    public DanceGoal(RiflebirdHarpy bird) {
        this.bird = bird;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.bird.level().getGameTime() < this.nextTime || this.bird.isHoldingSomething()) return false;
        Player player = this.bird.level().getNearestPlayer(this.bird, 10.0);
        boolean fond = player != null && this.bird.getAffection(player) > 0;
        // Shows off far more readily for someone who has given her gifts.
        if (this.bird.getRandom().nextInt(reducedTickDelay(fond ? 300 : 1500)) != 0) return false;
        if (player != null && !player.isSpectator()) {
            this.audience = player;
        } else {
            List<RiflebirdHarpy> others = this.bird.level().getEntitiesOfClass(RiflebirdHarpy.class, this.bird.getBoundingBox().inflate(10.0), b -> b != this.bird);
            this.audience = others.isEmpty() ? null : others.get(0);
        }
        return this.audience != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticks < this.duration && this.audience.isAlive() && this.bird.distanceToSqr(this.audience) < 14 * 14;
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.duration = 160 + this.bird.getRandom().nextInt(160);
    }

    @Override
    public void stop() {
        if (this.bird.getAction() == AbstractHarpy.Action.DANCING) this.bird.setAction(AbstractHarpy.Action.NONE);
        this.audience = null;
        this.nextTime = this.bird.level().getGameTime() + 600 + this.bird.getRandom().nextInt(1200);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.ticks++;
        this.bird.getLookControl().setLookAt(this.audience, 30.0F, 30.0F);
        double dist = Math.sqrt(this.bird.distanceToSqr(this.audience));
        if (dist > 4.0 || !this.bird.onGround()) {
            this.bird.setAction(AbstractHarpy.Action.NONE);
            if (this.ticks % 10 == 1) this.bird.getNavigation().moveTo(this.audience, 1.0);
            return;
        }
        this.bird.getNavigation().stop();
        this.bird.setAction(AbstractHarpy.Action.DANCING);
        if (this.ticks % STEP_TICKS == 0) this.playStep(this.ticks / STEP_TICKS % BASS.length);
    }

    private void playStep(int step) {
        if (BASS[step] >= 0) this.note(ModSounds.RIFLEBIRD_BASS.get(), BASS[step], 0.9F);
        if (HAT[step]) this.bird.playSound(ModSounds.RIFLEBIRD_HAT.get(), 0.4F, step % 4 == 0 ? 1.0F : 1.3F);
        if (MELODY[step] >= 0) {
            this.note(ModSounds.RIFLEBIRD_SING.get(), MELODY[step], 0.8F);
            if (this.bird.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.NOTE, this.bird.getX(), this.bird.getEyeY() + 0.6, this.bird.getZ(), 0, MELODY[step] / 24.0, 0, 0, 1);
            }
        }
    }

    private void note(SoundEvent sound, int semitone, float volume) {
        this.bird.playSound(sound, volume, (float) Math.pow(2.0, (semitone - 12) / 12.0));
    }
}
