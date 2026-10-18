package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.CrowCaches;
import com.linkyoshi90.monstergirls.entity.CrowHarpy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Goes back to one of her caches and digs it up: right away once whoever saw her bury it has left (and then hides
 * it again somewhere else, like real corvids do), or anyway after a day or two, to carry her treasure around a bit.
 */
public class RetrieveCacheGoal extends Goal {
    private static final int DIG_TICKS = 30;

    private final CrowHarpy crow;
    private final double speed;
    @Nullable
    private CrowCaches.Cache cache;
    private int ticks;
    private int digTicks;

    public RetrieveCacheGoal(CrowHarpy crow, double speed) {
        this.crow = crow;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.crow.isHoldingSomething() || this.crow.getAction() != AbstractHarpy.Action.NONE) return false;
        if (this.crow.getRandom().nextInt(reducedTickDelay(100)) != 0) return false;
        this.cache = this.crow.findDueCache();
        return this.cache != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticks < 600 && this.cache != null && !this.crow.isHoldingSomething()
                && this.crow.level() instanceof ServerLevel level && CrowCaches.get(level).at(this.cache.pos()) == this.cache;
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.digTicks = 0;
    }

    @Override
    public void stop() {
        if (this.crow.getAction() == AbstractHarpy.Action.CACHING) this.crow.setAction(AbstractHarpy.Action.NONE);
        this.cache = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.ticks++;
        if (!CacheItemGoal.GroundVisit.arrive(this.crow, this.cache.pos().above(), this.speed, this.ticks)) return;
        this.crow.getLookControl().setLookAt(this.cache.pos().getX() + 0.5, this.cache.pos().getY() + 1.0, this.cache.pos().getZ() + 0.5);
        this.crow.setAction(AbstractHarpy.Action.CACHING);
        CacheItemGoal.GroundVisit.scratch(this.crow, this.cache.pos(), ++this.digTicks);
        if (this.digTicks >= DIG_TICKS) this.crow.digUpCache(this.cache);
    }
}
