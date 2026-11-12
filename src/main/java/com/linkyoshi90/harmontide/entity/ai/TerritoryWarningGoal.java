package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.entity.OstrichHarpy;
import com.linkyoshi90.harmontide.registry.ModSounds;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * Someone has walked into her territory: she plants herself, spreads her wings and hisses at them for a couple
 * of seconds. If they have backed off by then, that's the end of it; if not, she charges.
 */
public class TerritoryWarningGoal extends Goal {
    private static final int DISPLAY_TICKS = 50;

    private final OstrichHarpy ostrich;
    private Player intruder;
    private int ticks;

    public TerritoryWarningGoal(OstrichHarpy ostrich) {
        this.ostrich = ostrich;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.ostrich.getTarget() != null || this.ostrich.isVehicle() || this.ostrich.tickCount % 5 != 0) return false;
        this.intruder = this.ostrich.level().getNearestPlayer(this.ostrich.getX(), this.ostrich.getY(), this.ostrich.getZ(), 20.0,
                e -> e instanceof Player p && this.ostrich.isIntruder(p) && !this.ostrich.isSpared(p) && this.ostrich.hasLineOfSight(p));
        return this.intruder != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticks < DISPLAY_TICKS && this.intruder.isAlive() && this.ostrich.getTarget() == null && !this.ostrich.isSpared(this.intruder);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.ostrich.getNavigation().stop();
        this.ostrich.setAction(AbstractHarpy.Action.DISPLAYING);
    }

    @Override
    public void stop() {
        if (this.ostrich.getAction() == AbstractHarpy.Action.DISPLAYING) this.ostrich.setAction(AbstractHarpy.Action.NONE);
        // Fair warning was given: anyone still inside gets charged.
        if (this.ticks >= DISPLAY_TICKS && this.ostrich.isIntruder(this.intruder) && !this.ostrich.isSpared(this.intruder)) {
            this.ostrich.setTarget(this.intruder);
        }
        this.intruder = null;
    }

    @Override
    public void tick() {
        this.ostrich.getLookControl().setLookAt(this.intruder, 30.0F, 30.0F);
        if (this.ticks++ % 20 == 0) this.ostrich.playSound(ModSounds.OSTRICH_HISS.get(), 1.2F, ModSounds.wobble(this.ostrich));
    }
}
