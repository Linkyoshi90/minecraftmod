package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Where each player last saw each of their friends: kept every few seconds while she is loaded and when her chunk unloads,
 * forgotten when she dies. /monstergirls friends and the friend compass fall back on it for friends out of the loaded world.
 * Lives with the overworld's saved data.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class FriendLog extends SavedData {
    private static final String NAME = "monstergirls_friend_log";
    private static final int EVERY = 100;
    private static final long FORGET_AFTER = 100L * 24000L;

    /** One sighting: her name then, the dimension, the spot and the overworld game time. */
    public record Seen(UUID mob, String name, String dimension, BlockPos pos, long time) {}

    private final Map<UUID, Map<UUID, Seen>> seen = new HashMap<>();

    public static FriendLog get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(FriendLog::new, FriendLog::load, null), NAME);
    }

    public void note(Player player, Mob mob) {
        this.seen.computeIfAbsent(player.getUUID(), k -> new HashMap<>()).put(mob.getUUID(), new Seen(mob.getUUID(), mob.getDisplayName().getString(),
                mob.level().dimension().location().toString(), mob.blockPosition(), mob.getServer().overworld().getGameTime()));
        this.setDirty();
    }

    public void forget(UUID mob) {
        for (Map<UUID, Seen> m : this.seen.values()) if (m.remove(mob) != null) this.setDirty();
    }

    /** Everyone this player has seen, most recent first. */
    public List<Seen> of(Player player) {
        List<Seen> out = new ArrayList<>(this.seen.getOrDefault(player.getUUID(), Map.of()).values());
        out.sort(Comparator.comparingLong(Seen::time).reversed());
        return out;
    }

    /** "just now", "12 min ago" or "3 days ago". */
    public static Component ago(MinecraftServer server, Seen s) {
        long t = server.overworld().getGameTime() - s.time();
        if (t < 1200) return Component.translatable("message.monstergirls.seen_now");
        if (t < 24000) return Component.translatable("message.monstergirls.seen_minutes", t / 1200);
        return Component.translatable("message.monstergirls.seen_days", t / 24000);
    }

    // --- keeping it ---------------------------------------------------------------------------------------------

    /** Notes every loaded friend of this player. */
    public static void update(ServerPlayer player) {
        FriendLog log = get(player.server);
        for (ServerLevel level : player.server.getAllLevels()) {
            for (Mob m : HomeLife.allFriends(level, player)) log.note(player, m);
        }
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer p && p.tickCount % EVERY == 0) update(p);
    }

    /** Her chunk unloads (or she goes to another dimension): the spot she was last at. */
    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof Mob m) || !(m instanceof Bonded b) || !(event.getLevel() instanceof ServerLevel level) || m.getHealth() <= 0
                || m.getRemovalReason() != null && m.getRemovalReason().shouldDestroy()) return;      // unloaded or changing dimension, not killed or discarded
        FriendLog log = null;
        for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
            if (b.getAffection(p) < Orders.AFFECTION) continue;
            if (log == null) log = get(level.getServer());
            log.note(p, m);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        if (!event.isCanceled() && event.getEntity() instanceof Bonded && event.getEntity().level() instanceof ServerLevel level) {
            get(level.getServer()).forget(event.getEntity().getUUID());
        }
    }

    // --- saving -------------------------------------------------------------------------------------------------

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        long now = this.seen.values().stream().flatMap(m -> m.values().stream()).mapToLong(Seen::time).max().orElse(0);
        for (Map.Entry<UUID, Map<UUID, Seen>> player : this.seen.entrySet()) {
            CompoundTag friends = new CompoundTag();
            for (Seen s : player.getValue().values()) {
                if (now - s.time() > FORGET_AFTER) continue;                     // a hundred days unseen: let it go

                CompoundTag e = new CompoundTag();
                e.putString("Name", s.name());
                e.putString("Dim", s.dimension());
                e.putLong("Pos", s.pos().asLong());
                e.putLong("Time", s.time());
                friends.put(s.mob().toString(), e);
            }
            tag.put(player.getKey().toString(), friends);
        }
        return tag;
    }

    private static FriendLog load(CompoundTag tag, HolderLookup.Provider registries) {
        FriendLog log = new FriendLog();
        for (String p : tag.getAllKeys()) {
            CompoundTag friends = tag.getCompound(p);
            Map<UUID, Seen> m = new HashMap<>();
            for (String id : friends.getAllKeys()) {
                CompoundTag e = friends.getCompound(id);
                try {
                    UUID mob = UUID.fromString(id);
                    m.put(mob, new Seen(mob, e.getString("Name"), e.getString("Dim"), BlockPos.of(e.getLong("Pos")), e.getLong("Time")));
                } catch (IllegalArgumentException ignored) {
                    // a damaged entry: skip it
                }
            }
            try {
                log.seen.put(UUID.fromString(p), m);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return log;
    }
}
