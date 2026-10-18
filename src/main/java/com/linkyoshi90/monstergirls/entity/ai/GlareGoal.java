package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.OstrichHarpy;
import com.linkyoshi90.monstergirls.registry.ModSounds;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * After she has kicked someone (or been calmed by a pat) she won't charge them again for a while, but if they
 * keep hanging around her territory she stands her ground and stares them down, now and then flaring her wings
 * with a hiss.
 */
public class GlareGoal extends Goal {
    private final OstrichHarpy ostrich;
    private Player subject;
    private int ticks;

    public GlareGoal(OstrichHarpy ostrich) {
        this.ostrich = ostrich;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean isGlareWorthy(Player player) {
        return this.ostrich.isSpared(player) && this.ostrich.isIntruder(player) && this.ostrich.hasLineOfSight(player);
    }

    @Override
    public boolean canUse() {
        if (this.ostrich.getTarget() != null || this.ostrich.isVehicle() || this.ostrich.tickCount % 5 != 0) return false;
        this.subject = this.ostrich.level().getNearestPlayer(this.ostrich.getX(), this.ostrich.getY(), this.ostrich.getZ(), 20.0,
                e -> e instanceof Player p && this.isGlareWorthy(p));
        return this.subject != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticks < 600 && this.ostrich.getTarget() == null && this.subject.isAlive() && this.ostrich.isSpared(this.subject)
                && this.ostrich.isIntruder(this.subject);
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.ostrich.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.subject = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.ostrich.getLookControl().setLookAt(this.subject, 30.0F, 30.0F);
        if (this.ticks++ % 100 == 40) {
            this.ostrich.startTimedAction(AbstractHarpy.Action.DISPLAYING, 20);
            this.ostrich.playSound(ModSounds.OSTRICH_HISS.get(), 0.7F, 0.9F);
        }
    }

    public boolean isGlaring() {
        return this.subject != null;
    }
}
