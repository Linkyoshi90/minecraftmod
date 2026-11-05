package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Daily visits: a friend a player comes near ({@link #NEAR}) on a new day is glad to see them (one friendship a day), and counts the
 * days in a row. Every {@link #WEEK} days in a row she says so and is {@link #WEEKLY} closer. A day missed starts the count over.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Visits {
    public static final double NEAR = 8;
    public static final int WEEK = 7, WEEKLY = 5;
    public static final String KEY = "Visits";

    private Visits() {
    }

    public static long today(ServerLevel level) {
        return level.getDayTime() / 24000L;
    }

    private static CompoundTag of(Mob mob, Player p) {
        return mob.getPersistentData().getCompound(KEY).getCompound(p.getStringUUID());
    }

    /** Days in a row this player has come to see her (counting today, if they have). */
    public static int streak(Mob mob, Player p) {
        return of(mob, p).getInt("Streak");
    }

    /** A visit: the day counted (once a day). The streak now, or 0 when today was already counted. */
    public static int visit(ServerLevel level, Mob mob, Player p) {
        long today = today(level);
        CompoundTag all = mob.getPersistentData().getCompound(KEY);
        CompoundTag v = all.getCompound(p.getStringUUID());
        if (v.contains("Day") && v.getLong("Day") == today) return 0;
        int streak = v.contains("Day") && v.getLong("Day") == today - 1 ? v.getInt("Streak") + 1 : 1;
        v.putLong("Day", today);
        v.putInt("Streak", streak);
        all.put(p.getStringUUID(), v);
        mob.getPersistentData().put(KEY, all);
        Bonded b = (Bonded) mob;
        b.addAffection(p, 1);
        if (streak % WEEK == 0) {
            b.addAffection(p, WEEKLY);
            level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.4, mob.getZ(), 5, 0.3, 0.2, 0.3, 0);
            p.displayClientMessage(Component.translatable("message.monstergirls.visits", mob.getDisplayName(), streak).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }
        return streak;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || !(p.level() instanceof ServerLevel level) || p.tickCount % 200 != 83 || p.isSpectator()) return;
        for (Mob m : HomeLife.friendsOf(level, p, p.getBoundingBox().inflate(NEAR))) visit(level, m, p);
    }
}
