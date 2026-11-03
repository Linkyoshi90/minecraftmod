package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Friends miss the player they haven't seen. It is worked out from the last meeting when they meet again, so it holds for
 * anyone in a chunk nobody has loaded. For a day after that she is more eager (the bed, the odds of young, her gifts), and
 * she has lost a little friendship, never below {@link Bonded#BED_AFFECTION}. The closer the friend, the sooner it starts.
 */
public final class Missing {
    public static final int DAY = 24000;
    private static final String SEEN = "MissingSeen";
    private static final String BACK = "MissingBack";
    private static final String STAGE = "MissingStage";
    private static final String MET = "MissingMet", KEPT = "MissingKept";
    public static final int MONTH = 30;

    private Missing() {
    }

    /** Days apart before she misses them, at this friendship. Twice as many and she misses them badly. */
    public static int startDays(int affection) {
        int days = affection >= Bonded.FULL_AFFECTION ? 2 : affection >= 60 ? 3 : 4;
        return Math.max(1, Math.round(days * 100.0F / Math.max(1, MonsterGirlsConfig.missingRate())));
    }

    /** 0 not at all, 1 she misses them, 2 badly. */
    public static int stage(int affection, long daysApart) {
        if (affection < Bonded.BED_AFFECTION || MonsterGirlsConfig.missingRate() <= 0) return 0;
        int start = startDays(affection);
        return daysApart >= start * 2L ? 2 : daysApart >= start ? 1 : 0;
    }

    private static CompoundTag tag(Mob mob, String key) {
        CompoundTag data = mob.getPersistentData();
        if (!data.contains(key)) data.put(key, new CompoundTag());
        return data.getCompound(key);
    }

    /** A friend is near the player: settles how she has been, and starts the clock again. */
    public static void meet(Mob mob, Player player) {
        if (mob.isBaby() || !(mob.level() instanceof ServerLevel level)) return;
        Bonded bonded = (Bonded) mob;
        int affection = bonded.getAffection(player);
        if (affection < Bonded.BED_AFFECTION) return;
        String id = player.getUUID().toString();
        long now = level.getDayTime();
        CompoundTag seen = tag(mob, SEEN);
        if (seen.contains(id)) {
            long days = Math.max(0, now - seen.getLong(id)) / DAY;
            int stage = stage(affection, days);
            if (stage > 0) reunite(level, mob, player, stage, days);
        }
        seen.putLong(id, now);
        anniversary(level, mob, player, id, now / DAY);
        if (activeStage(mob, player) > 0 && mob.getRandom().nextInt(4) == 0) tears(level, mob);
    }

    /** Every month of days since they became close friends: three presents and a line in chat. Returns whether it was today. */
    public static boolean anniversary(ServerLevel level, Mob mob, Player player, String id, long day) {
        CompoundTag met = tag(mob, MET);
        if (!met.contains(id)) {
            met.putLong(id, day);
            return false;
        }
        long days = day - met.getLong(id);
        CompoundTag kept = tag(mob, KEPT);
        if (days <= 0 || days % MONTH != 0 || kept.getLong(id) == day) return false;
        kept.putLong(id, day);
        if (mob instanceof GiftGiver giver) for (int i = 0; i < 3; i++) mob.spawnAtLocation(giver.rollGift(player));
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getY() + mob.getBbHeight(), mob.getZ(), 10, 0.5, 0.3, 0.5, 0.0);
        player.displayClientMessage(Component.translatable("message.monstergirls.anniversary", mob.getDisplayName(), days), false);
        return true;
    }

    private static void reunite(ServerLevel level, Mob mob, Player player, int stage, long days) {
        Bonded bonded = (Bonded) mob;
        int affection = bonded.getAffection(player);
        int lost = (int) (days - startDays(affection) + 1);
        if (Keepsakes.wearsBracelet(mob, player)) lost /= 2;
        if (affection > Bonded.BED_AFFECTION) bonded.setAffection(player.getUUID(), Math.max(Bonded.BED_AFFECTION, affection - lost));
        String id = player.getUUID().toString();
        tag(mob, BACK).putLong(id, level.getDayTime() + DAY);
        tag(mob, STAGE).putInt(id, stage);
        if (mob instanceof GiftGiver giver) giver.setNextGiftTime(level.getGameTime());
        tears(level, mob);
        mob.playAmbientSound();
        player.displayClientMessage(Component.translatable("message.monstergirls.missed." + stage, mob.getDisplayName()), true);
        Journal.recordMissed(player, mob);
    }

    private static void tears(ServerLevel level, Mob mob) {
        level.sendParticles(ParticleTypes.FALLING_WATER, mob.getX(), mob.getEyeY(), mob.getZ(), 6, 0.2, 0.1, 0.2, 0.0);
    }

    /** How much she has missed them, for the day after they meet again: 0, 1 or 2. */
    public static int activeStage(Mob mob, @Nullable Player player) {
        if (player == null) return 0;
        String id = player.getUUID().toString();
        CompoundTag back = tag(mob, BACK);
        return back.contains(id) && mob.level().getDayTime() < back.getLong(id) ? tag(mob, STAGE).getInt(id) : 0;
    }

    /** What it adds to tonight's odds of her coming to bed. */
    public static float bedBonus(Mob mob, Player player) {
        return activeStage(mob, player) * 0.25F;
    }

    /** The odds of young after a shared night: the setting, rising to certain for one who missed them badly. */
    public static float conceptionChance(Mob mob, Player player) {
        float base = MonsterGirlsConfig.conceptionChance() / 100.0F;
        return base + (1.0F - base) * activeStage(mob, player) / 2.0F;
    }

    /** She went because you sent her (an expedition): nothing to forgive. */
    public static void forgive(Mob mob, UUID player) {
        tag(mob, SEEN).putLong(player.toString(), mob.level().getDayTime());
    }

    /** A line for /monstergirls bonds, or null. */
    @Nullable
    public static Component describe(Mob mob, Player player) {
        int stage = activeStage(mob, player);
        return stage == 0 ? null : Component.translatable("bond.monstergirls.missed." + stage);
    }
}
