package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.CrowHarpy;
import com.linkyoshi90.monstergirls.entity.JukeboxSensing;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * A wild crow eyeing a jukebox that plays with nobody standing by: she hops onto it, fishes the shiny disc out
 * (the music stops mid-song), cackles and flies off with it. Then it's a trinket like any other: carried around,
 * dropped somewhere down the road, or buried in one of her caches.
 */
public class StealDiscGoal extends Goal {
    private static final double GUARD_RADIUS = 6.0;
    private static final int FISH_TICKS = 30;

    private final CrowHarpy crow;
    private final double speed;
    @Nullable
    private BlockPos jukebox;
    private int ticks;
    private int onTop;

    public StealDiscGoal(CrowHarpy crow, double speed) {
        this.crow = crow;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.crow.getOwner() != null || this.crow.isHatchling() || this.crow.isHoldingSomething() || this.crow.getAction() != AbstractHarpy.Action.NONE) return false;
        if (this.crow.getRandom().nextInt(reducedTickDelay(200)) != 0) return false;
        BlockPos music = this.crow.getMusicNearby();
        if (music == null || this.guarded(music) || !this.crow.level().isEmptyBlock(music.above())) return false;
        this.jukebox = music;
        return true;
    }

    /** Anyone standing close enough to catch her at it. */
    private boolean guarded(BlockPos pos) {
        for (Player player : this.crow.level().players()) {
            if (!player.isSpectator() && player.distanceToSqr(pos.getBottomCenter()) < GUARD_RADIUS * GUARD_RADIUS) return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.jukebox != null && this.ticks < 400 && !this.crow.isHoldingSomething() && !this.guarded(this.jukebox)
                && JukeboxSensing.isPlaying(this.crow.level(), this.jukebox);
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.onTop = 0;
    }

    @Override
    public void stop() {
        if (this.crow.getAction() == AbstractHarpy.Action.PRANKING) this.crow.setAction(AbstractHarpy.Action.NONE);
        this.jukebox = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.ticks++;
        BlockPos top = this.jukebox.above();
        double dx = this.crow.getX() - (top.getX() + 0.5), dz = this.crow.getZ() - (top.getZ() + 0.5);
        if (dx * dx + dz * dz > 0.6 * 0.6 || this.crow.getY() > top.getY() + 1.0) {
            if (this.ticks % 10 == 1) this.crow.getNavigation().moveTo(top.getX() + 0.5, top.getY(), top.getZ() + 0.5, this.speed);
            this.crow.getMoveControl().setWantedPosition(top.getX() + 0.5, top.getY(), top.getZ() + 0.5, this.speed);
            return;
        }
        this.crow.getNavigation().stop();
        this.crow.setAction(AbstractHarpy.Action.PRANKING);
        if (++this.onTop < FISH_TICKS) return;
        JukeboxBlockEntity entity = JukeboxSensing.jukeboxAt(this.crow.level(), this.jukebox);
        if (entity == null) return;
        ItemStack disc = entity.splitTheItem(1); // stops the song
        if (disc.isEmpty()) return;
        this.crow.takeIntoHands(disc);
        this.crow.laugh();
    }
}
