package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.Lactation;
import com.linkyoshi90.harmontide.entity.Nsfw;
import com.linkyoshi90.harmontide.entity.Nursing;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Every three to six minutes a nursing mother with milk ready calls her baby over (within SEARCH), sits, and holds her to her chest for
 * Nursing.FEED_TICKS: the baby rides her, both in the NURSE action, hearts now and then. A joey stays in her pouch for it. With no baby
 * of her own to feed, a mother with milk fosters an orphan within FOSTER (Nursing.isOrphan): a calf, a wolf pup, a hatchling, any
 * species but a villager's child. With the adult option on, her top comes up for the feed and goes back down after.
 * The baby stays clothed.
 */
public class NurseGoal extends Goal {
    public static final int SEARCH = 16, FOSTER = 8, EVERY = 3600, PATIENCE = 400;
    public static final String NEXT = "NurseAt";
    private final AbstractMonsterGirl mother;
    @Nullable
    private net.minecraft.world.entity.Mob baby;
    private int patience, feeding;
    /** She was dressed when this feed started, so the end of it puts the top back. */
    private boolean opened;

    public NurseGoal(AbstractMonsterGirl mother) {
        this.mother = mother;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.mother.tickCount % 41 != 0 || !(this.mother.level() instanceof ServerLevel level) || this.mother.isBaby() || this.mother.isPassenger()) return false;
        if (!Lactation.isLactating(this.mother) || this.mother.getAction() != AbstractMonsterGirl.Action.NONE) return false;
        long next = this.mother.getPersistentData().getLong(NEXT);
        if (level.getGameTime() < next) return false;
        this.baby = null;
        for (AbstractMonsterGirl c : Nursing.loadedChildren(level, this.mother)) {
            boolean free = !c.isPassenger() || c.getVehicle() == this.mother;
            if (c.isBaby() && free && c.distanceToSqr(this.mother) < SEARCH * SEARCH) {
                this.baby = c;
                break;
            }
        }
        if (this.baby == null) {
            this.baby = level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, this.mother.getBoundingBox().inflate(FOSTER),
                    m -> !m.isPassenger() && Nursing.isOrphan(level, m)).stream().findFirst().orElse(null);
        }
        if (this.baby == null) this.mother.getPersistentData().putLong(NEXT, level.getGameTime() + 1200);
        return this.baby != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.baby != null && this.baby.isAlive() && this.baby.isBaby() && (this.feeding > 0 || this.patience > 0);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.patience = PATIENCE;
        this.feeding = 0;
    }

    @Override
    public void tick() {
        if (!(this.mother.level() instanceof ServerLevel level) || this.baby == null) return;
        this.mother.getLookControl().setLookAt(this.baby);
        if (this.feeding == 0) {
            this.patience--;
            if (this.baby.getVehicle() != this.mother && this.baby.distanceToSqr(this.mother) > 1.6 * 1.6) {
                if (this.mother.tickCount % 10 == 0) this.baby.getNavigation().moveTo(this.mother, 1.1);   // she comes to her mother
                return;
            }
            this.mother.getNavigation().stop();
            this.baby.getNavigation().stop();
            if (this.baby.getVehicle() != this.mother) this.baby.startRiding(this.mother, true);
            this.mother.setAction(AbstractMonsterGirl.Action.NURSE);
            if (this.baby instanceof AbstractMonsterGirl girl) girl.setAction(AbstractMonsterGirl.Action.NURSE);
            this.openTop();
            this.feeding = 1;
            return;
        }
        this.mother.getNavigation().stop();
        if (this.feeding % 40 == 0) {
            double[] chest = Lactation.chest(this.mother);
            level.sendParticles(ParticleTypes.HEART, this.mother.getX(), this.mother.getY() + chest[0] + 0.5, this.mother.getZ(), 1, 0.2, 0.1, 0.2, 0.0);
        }
        if (++this.feeding >= Nursing.FEED_TICKS) {
            Nursing.fed(level, this.mother, this.baby);
            this.baby = this.finish(this.baby);
        }
    }

    @Override
    public void stop() {
        if (this.baby != null) this.finish(this.baby);
        this.baby = null;
        this.feeding = 0;
        this.mother.getPersistentData().putLong(NEXT, this.mother.level().getGameTime() + EVERY + this.mother.getRandom().nextInt(EVERY));
    }

    /** Down from her arms (a joey stays in the pouch), and both back to what they were doing. */
    @Nullable
    private net.minecraft.world.entity.Mob finish(net.minecraft.world.entity.Mob baby) {
        if (baby instanceof AbstractMonsterGirl girl && girl.getAction() == AbstractMonsterGirl.Action.NURSE) girl.setAction(AbstractMonsterGirl.Action.NONE);
        if (this.mother.getAction() == AbstractMonsterGirl.Action.NURSE) this.mother.setAction(AbstractMonsterGirl.Action.NONE);
        if (this.opened) {
            this.mother.dressAfter(30);
            this.opened = false;
        }
        if (baby.getVehicle() == this.mother && !(this.mother instanceof com.linkyoshi90.harmontide.entity.girl.Marsupial)) baby.stopRiding();
        return null;
    }

    /** Her top up for the length of the feed. Off, she stays dressed, and a baby is never bared. */
    private void openTop() {
        if (!Nsfw.on()) return;
        if (!this.mother.hasFlag(AbstractMonsterGirl.FLAG_BARE)) this.opened = true;
        this.mother.bareFor(Nursing.FEED_TICKS);
    }
}
