package com.linkyoshi90.monstergirls.compat;

import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.Vodyanoy;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** What the info overlays (JadePlugin) show about a girl; kept free of any other mod's classes. */
public final class BondInfo {
    /** The friendship milestones, in order: friend, shares your bed, devoted, full. */
    public static final int[] MILESTONES = {Orders.AFFECTION, Bonded.BED_AFFECTION, 60, Bonded.FULL_AFFECTION};

    private BondInfo() {}

    /** The next milestone above this affection (the last one once she is there). */
    public static int nextMilestone(int affection) {
        for (int m : MILESTONES) if (affection < m) return m;
        return MILESTONES[MILESTONES.length - 1];
    }

    /** Whether she could be expecting at all: grown, and not a bull, the leshy, the vodyanoy or a construct. */
    public static boolean canCarry(Entity e) {
        if (e instanceof LivingEntity l && l.isBaby()) return false;
        if (e instanceof Vodyanoy) return false;
        if (e instanceof AbstractMonsterGirl g) {
            return !g.hasFlag(AbstractMonsterGirl.FLAG_MALE) && g.getSpecies() != GirlSpecies.LESHY_FLORA
                    && g.getSpecies().family() != GirlSpecies.Family.CONSTRUCT;
        }
        return true;
    }
}
