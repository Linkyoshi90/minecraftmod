package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.JukeboxSensing;
import com.linkyoshi90.monstergirls.entity.OstrichHarpy;
import com.linkyoshi90.monstergirls.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * A wild ostrich won't have that racket on her ground: a jukebox playing inside her territory gets the same treatment
 * as an intruder. She runs over, puffs up in her warning display with a hiss, and when it doesn't shut up, kicks it
 * so hard the disc flies out.
 */
public class KickJukeboxGoal extends Goal {
    private static final int WARN_TICKS = 40;
    private static final int KICK_AT = WARN_TICKS + 10;

    private final OstrichHarpy ostrich;
    private final double speed;
    @Nullable
    private BlockPos jukebox;
    private int ticks;
    private int closeTicks;

    public KickJukeboxGoal(OstrichHarpy ostrich, double speed) {
        this.ostrich = ostrich;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.ostrich.tickCount % 11 != 0 || this.ostrich.getLoyalPlayer() != null || this.ostrich.getTarget() != null) return false;
        BlockPos music = this.ostrich.getMusicNearby();
        Vec3 center = this.ostrich.getTerritoryCenter();
        if (music == null || center == null || Vec3.atCenterOf(music).distanceToSqr(center) > OstrichHarpy.TERRITORY * OstrichHarpy.TERRITORY) return false;
        this.jukebox = music;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.jukebox != null && this.ticks < 400 && this.ostrich.getTarget() == null && JukeboxSensing.isPlaying(this.ostrich.level(), this.jukebox);
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.closeTicks = 0;
    }

    @Override
    public void stop() {
        AbstractHarpy.Action action = this.ostrich.getAction();
        if (action == AbstractHarpy.Action.DISPLAYING || action == AbstractHarpy.Action.KICKING) this.ostrich.setAction(AbstractHarpy.Action.NONE);
        this.jukebox = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.ticks++;
        Vec3 box = Vec3.atBottomCenterOf(this.jukebox);
        this.ostrich.getLookControl().setLookAt(box.x, box.y + 0.5, box.z);
        if (this.ostrich.distanceToSqr(box) > 2.2 * 2.2) {
            if (this.ticks % 10 == 1) this.ostrich.getNavigation().moveTo(box.x, box.y, box.z, this.speed);
            return;
        }
        this.ostrich.getNavigation().stop();
        this.closeTicks++;
        if (this.closeTicks == 1) {
            this.ostrich.setAction(AbstractHarpy.Action.DISPLAYING);
            this.ostrich.playSound(ModSounds.OSTRICH_HISS.get(), 1.0F, 1.0F);
        } else if (this.closeTicks == WARN_TICKS) {
            this.ostrich.setAction(AbstractHarpy.Action.KICKING);
        } else if (this.closeTicks == KICK_AT) {
            this.ostrich.playSound(ModSounds.OSTRICH_KICK.get(), 1.2F, 0.9F);
            JukeboxBlockEntity entity = JukeboxSensing.jukeboxAt(this.ostrich.level(), this.jukebox);
            if (entity != null) entity.popOutTheItem(); // ...and the music stops; the goal ends with it
        }
    }
}
