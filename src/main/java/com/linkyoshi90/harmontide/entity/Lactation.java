package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.block.MilkingMachineBlockEntity;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Minotaur;
import com.linkyoshi90.harmontide.item.FamilyMilk;
import com.linkyoshi90.harmontide.registry.ModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * Who has milk just now: a minotaur cow whose milk is ready, and anyone in the last third of expecting (grown and full size, as for the
 * machine). They go looking for a milking machine (SeekMilkingMachineGoal), and with the milkDrips option on they drip until milked.
 * With milkDiscomfort on, ten minutes of milk unmilked leaves her uncomfortably full: slowed, a grumpy puff now and then and a word to
 * her friends nearby (and to a friend who pets her), and she looks twice as far for a machine. A mother nursing her baby
 * (Nursing) has milk until the baby is weaned. Milking her (machine or bucket) is a relief, worth a little friendship.
 */
public final class Lactation {
    /** How far along she has to be: the last of the three stages. */
    public static final float LAST_STAGE = 2.0F / 3.0F;
    private static final int DRIP_INTERVAL = 7, DROPS = 2;
    /** A lying girl's chest, above the ground she lies on. */
    static final double LYING_CHEST = 0.45;
    /** Ticks of milk before she is full, how often she says so, and the friendship her relief is worth. */
    public static final int FULL_AFTER = 12000, ASK_EVERY = 2400, RELIEF = 2;
    static final String FULL_SINCE = "MilkFullSince";

    private Lactation() {}

    public static boolean lastStage(Bonded bonded) {
        return bonded.getGestation().isCarrying() && bonded.getGestation().progress() >= LAST_STAGE;
    }

    /** Whether she has milk to give at this moment. */
    public static boolean isLactating(LivingEntity creature) {
        if (!(creature instanceof Bonded bonded) || creature.isBaby() || creature.getBbHeight() < MilkingMachineBlockEntity.MIN_HEIGHT) return false;
        if (FamilyMilk.of(creature) == null || !MilkingMachineBlockEntity.isReady(creature, creature.level().getGameTime())) return false;
        return lastStage(bonded) || Nursing.isNursing(creature) || creature instanceof Minotaur cow && !cow.isMale();
    }

    /** Late in expecting, unmilked, and the option is on. A cow's own bell is on top of this (Minotaur.isDripping). */
    public static boolean isDripping(LivingEntity creature) {
        return HarmontideConfig.milkDrips() && creature instanceof Bonded bonded && (lastStage(bonded) || Nursing.isNursing(creature)) && isLactating(creature);
    }

    /** Where her chest is: height above her feet and distance ahead of them, by pose. Lying (in bed) she is tipped onto her back, head behind her. */
    public static double[] chest(LivingEntity creature) {
        float height = creature.getBbHeight();
        if (creature instanceof AbstractMonsterGirl girl) {
            AbstractMonsterGirl.Action action = girl.getAction();
            if (action == AbstractMonsterGirl.Action.LYING) return new double[] {LYING_CHEST, -height * 0.65};
            if (action == AbstractMonsterGirl.Action.SITTING || action == AbstractMonsterGirl.Action.NURSE) return new double[] {height * 0.42, creature.getBbWidth() * 0.3};
        }
        if (creature.isSleeping()) return new double[] {LYING_CHEST, 0.0};   // vanilla sleep lays her along the bed about her middle
        if (creature instanceof AbstractMonsterGirl girl) {                   // read from her model (tools/gen_chest_table.py): centaurs and giraffes carry it high
            float[] at = com.linkyoshi90.harmontide.registry.ChestTable.CHEST.get(girl.getSpecies().id());
            if (at != null) return new double[] {height * at[0], height * at[1] + 0.05};
        }
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

    /** Drips for everyone but the minotaurs, who run their own; how full she feels, for everyone. */
    public static void tick(ServerLevel level, LivingEntity creature) {
        if (creature.tickCount % DRIP_INTERVAL == 0 && !(creature instanceof Minotaur) && isDripping(creature)) drip(level, creature);
        if (creature.tickCount % 20 == 0) comfort(level, creature);
        Nursing.tick(level, creature);
        MilkRescue.tick(level, creature);
    }

    /** Uncomfortably full: lactating, and unmilked for FULL_AFTER. */
    public static boolean isFull(LivingEntity creature) {
        CompoundTag data = creature.getPersistentData();
        return HarmontideConfig.milkDiscomfort() && data.contains(FULL_SINCE) && isLactating(creature)
                && creature.level().getGameTime() - data.getLong(FULL_SINCE) >= FULL_AFTER;
    }

    /** Once a second: when her milk came in, and how she feels about it since. */
    public static void comfort(ServerLevel level, LivingEntity creature) {
        CompoundTag data = creature.getPersistentData();
        if (!HarmontideConfig.milkDiscomfort() || !isLactating(creature)) {
            data.remove(FULL_SINCE);
            return;
        }
        long now = level.getGameTime();
        if (!data.contains(FULL_SINCE)) {
            data.putLong(FULL_SINCE, now);
            return;
        }
        long full = now - data.getLong(FULL_SINCE) - FULL_AFTER;
        if (full < 0) return;
        creature.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, false, true));
        if (full % ASK_EVERY >= 20) return;
        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, creature.getX(), creature.getEyeY() + 0.4, creature.getZ(), 1, 0.1, 0.1, 0.1, 0.0);
        if (!(creature instanceof Bonded bonded)) return;
        for (Player p : level.getEntitiesOfClass(Player.class, creature.getBoundingBox().inflate(16), p -> bonded.getAffection(p) >= HomeLife.FRIEND)) {
            p.displayClientMessage(Component.translatable("message.harmontide.milk_full", creature.getDisplayName()), true);
        }
    }

    /** A pat from a friend while she is full: she says so. */
    public static void onPet(LivingEntity creature, Player player) {
        if (isFull(creature)) player.displayClientMessage(Component.translatable("message.harmontide.milk_full", creature.getDisplayName()), true);
    }

    /** She has just been milked: if she was full, a sigh of relief and a little friendship for whoever did it. */
    public static boolean relieve(ServerLevel level, LivingEntity creature, @Nullable Player friend) {
        boolean full = isFull(creature);
        creature.getPersistentData().remove(FULL_SINCE);
        if (!full) return false;
        creature.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        level.sendParticles(ParticleTypes.HEART, creature.getX(), creature.getEyeY() + 0.3, creature.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
        if (friend != null && creature instanceof Bonded bonded) {
            bonded.addAffection(friend, RELIEF);
            friend.displayClientMessage(Component.translatable("message.harmontide.milk_relief", creature.getDisplayName()), true);
        }
        return true;
    }
}
