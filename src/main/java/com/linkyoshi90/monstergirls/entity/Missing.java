package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;

/**
 * Friends miss the player they haven't seen. It is worked out from the last meeting when they meet again, so it holds for
 * anyone in a chunk nobody has loaded. For a day after that she is more eager (the bed, the odds of young, her gifts), and
 * she has lost a little friendship, never below {@link Bonded#BED_AFFECTION}. The closer the friend, the sooner it starts.
 * Days apart are the player's own (clock): time passes for it only while they are online, as the world's day does then (a night
 * slept through counts), so a week away from a server that others kept running costs nothing. missingCountsOffline puts it back
 * on the world's days.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Missing {
    public static final int DAY = 24000;
    private static final String SEEN = "MissingSeen";
    private static final String BACK = "MissingBack";
    private static final String STAGE = "MissingStage";
    private static final String MET = "MissingMetOn", KEPT = "MissingKeptOn";   // in the player's days (MissingMet was the world's)
    private static final String CLOCK = "Clock", CLOCK_LAST = "ClockLast";
    public static final int MONTH = 30;

    private Missing() {
    }

    /** The player's own time: the world's day time while they have been online, in ticks. */
    public static long clock(Player player) {
        if (MonsterGirlsConfig.missingCountsOffline()) return player.level().getDayTime();
        return player.getData(ModAttachments.JOURNAL).getLong(CLOCK);
    }

    /** Back on the server: the time they were away is not theirs. */
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        loggedIn(event.getEntity());
    }

    public static void loggedIn(Player player) {
        CompoundTag data = player.getData(ModAttachments.JOURNAL);
        data.putLong(CLOCK_LAST, player.level().getDayTime());
        player.setData(ModAttachments.JOURNAL, data);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().tickCount % 20 == 0 && !event.getEntity().level().isClientSide) advance(event.getEntity());
    }

    /** The world's time since they last looked, while they are here; a jump back, or of more than a day (a command), counts as a second. */
    public static void advance(Player player) {
        CompoundTag data = player.getData(ModAttachments.JOURNAL);
        long now = player.level().getDayTime(), last = data.contains(CLOCK_LAST) ? data.getLong(CLOCK_LAST) : now;
        long delta = now - last;
        if (delta < 0 || delta > DAY) delta = 20;
        data.putLong(CLOCK, data.getLong(CLOCK) + delta);
        data.putLong(CLOCK_LAST, now);
        player.setData(ModAttachments.JOURNAL, data);
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
        long now = clock(player);
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
    public static void forgive(Mob mob, Player player) {
        tag(mob, SEEN).putLong(player.getUUID().toString(), clock(player));
    }

    /** A line for /monstergirls bonds, or null. */
    @Nullable
    public static Component describe(Mob mob, Player player) {
        int stage = activeStage(mob, player);
        return stage == 0 ? null : Component.translatable("bond.monstergirls.missed." + stage);
    }
}
