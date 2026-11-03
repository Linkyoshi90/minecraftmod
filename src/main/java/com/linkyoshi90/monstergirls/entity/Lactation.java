package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.block.MilkingMachineBlockEntity;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Minotaur;
import com.linkyoshi90.monstergirls.item.FamilyMilk;
import com.linkyoshi90.monstergirls.registry.ModParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * Who has milk just now: a minotaur cow whose milk is ready, and anyone in the last third of expecting (grown and full size, as for the
 * machine). They go looking for a milking machine (SeekMilkingMachineGoal), and with the milkDrips option on they drip until milked.
 */
public final class Lactation {
    /** How far along she has to be: the last of the three stages. */
    public static final float LAST_STAGE = 2.0F / 3.0F;
    private static final int DRIP_INTERVAL = 7, DROPS = 2;
    /** A lying girl's chest, above the ground she lies on. */
    static final double LYING_CHEST = 0.45;

    private Lactation() {}

    public static boolean lastStage(Bonded bonded) {
        return bonded.getGestation().isCarrying() && bonded.getGestation().progress() >= LAST_STAGE;
    }

    /** Whether she has milk to give at this moment. */
    public static boolean isLactating(LivingEntity creature) {
        if (!(creature instanceof Bonded bonded) || creature.isBaby() || creature.getBbHeight() < MilkingMachineBlockEntity.MIN_HEIGHT) return false;
        if (FamilyMilk.of(creature) == null || !MilkingMachineBlockEntity.isReady(creature, creature.level().getGameTime())) return false;
        return lastStage(bonded) || creature instanceof Minotaur cow && !cow.isMale();
    }

    /** Late in expecting, unmilked, and the option is on. A cow's own bell is on top of this (Minotaur.isDripping). */
    public static boolean isDripping(LivingEntity creature) {
        return MonsterGirlsConfig.milkDrips() && creature instanceof Bonded bonded && lastStage(bonded) && isLactating(creature);
    }

    /** Where her chest is: height above her feet and distance ahead of them, by pose. Lying (in bed) she is tipped onto her back, head behind her. */
    public static double[] chest(LivingEntity creature) {
        float height = creature.getBbHeight();
        if (creature instanceof AbstractMonsterGirl girl) {
            AbstractMonsterGirl.Action action = girl.getAction();
            if (action == AbstractMonsterGirl.Action.LYING) return new double[] {LYING_CHEST, -height * 0.65};
            if (action == AbstractMonsterGirl.Action.SITTING) return new double[] {height * 0.42, creature.getBbWidth() * 0.3};
        }
        if (creature.isSleeping()) return new double[] {LYING_CHEST, 0.0};   // vanilla sleep lays her along the bed about her middle
        return new double[] {height * 0.6, creature.getBbWidth() * 0.3};
    }

    public static void drip(ServerLevel level, LivingEntity creature) {
        double yaw = Math.toRadians(creature.yBodyRot);
        double[] chest = chest(creature);
        double forward = chest[1];
        for (int i = 0; i < DROPS; i++) {
            double side = i == 0 ? 0.1 : -0.1;
            level.sendParticles(ModParticles.MILK_DRIP.get(), creature.getX() - Math.sin(yaw) * forward + Math.cos(yaw) * side, creature.getY() + chest[0],
                    creature.getZ() + Math.cos(yaw) * forward + Math.sin(yaw) * side, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    /** For everyone but the minotaurs, who run their own. */
    public static void tick(ServerLevel level, LivingEntity creature) {
        if (creature.tickCount % DRIP_INTERVAL == 0 && !(creature instanceof Minotaur) && isDripping(creature)) drip(level, creature);
    }
}
