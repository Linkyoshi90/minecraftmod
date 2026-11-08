package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.network.ModPayloads;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The rose moon: about one night in {@link #ONE_IN} (never the first) the moon rises rose-violet and the Demon Realm stirs:
 * monsterization goes twice as fast, possession spreads and the realm calls its own out four times as often, and the grass
 * spreads as under a full moon. Which nights is fixed by the world seed and the day, so everyone agrees. Players are told at
 * moonrise and their sky turns rose (client/RealmAtmosphere). Behind the roseMoon option.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class RoseMoon {
    public static final int ONE_IN = 10, STIR = 4;
    private static final String SEEN = "RoseMoonSeen";

    private RoseMoon() {}

    /** Whether day `day` of a world with this seed ends in a rose moon. */
    public static boolean roseDay(long seed, long day) {
        if (day <= 0) return false;
        long h = seed ^ (day * 0x9E3779B97F4A7C15L);
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        return Math.floorMod(h, ONE_IN) == 0;
    }

    /** A rose moon in this level's sky right now (the overworld, at night). */
    public static boolean tonight(Level level) {
        if (!(level instanceof ServerLevel server) || level.dimension() != Level.OVERWORLD || !MonsterGirlsConfig.roseMoon()) return false;
        if (net.neoforged.neoforge.gametest.GameTestHooks.isGametestServer()) return false;   // the test world's clock must not double a test's counts
        long time = level.getDayTime() % 24000L;
        return time >= 12500L && time < 23500L && roseDay(server.getSeed(), level.getDayTime() / 24000L);
    }

    /** How much faster the realm works tonight: STIR on a rose moon, else 1. */
    public static int stir(Level level) {
        return tonight(level) ? STIR : 1;
    }

    /** Each player learns when a rose moon rises (a line in chat, and their sky turns rose) and when it sets. */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().tickCount % 40 != 21 || !(event.getEntity() instanceof ServerPlayer player)) return;
        boolean rose = tonight(player.level());
        if (rose == player.getPersistentData().getBoolean(SEEN)) return;
        player.getPersistentData().putBoolean(SEEN, rose);
        PacketDistributor.sendToPlayer(player, new ModPayloads.RoseMoonSky(rose));
        if (rose) player.displayClientMessage(Component.translatable("message.monstergirls.rose_moon").withStyle(ChatFormatting.LIGHT_PURPLE), false);
    }
}
