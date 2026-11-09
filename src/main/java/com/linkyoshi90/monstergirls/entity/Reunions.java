package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.HearthBlockEntity;
import com.linkyoshi90.monstergirls.entity.girl.Festivals;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Family reunions (2026-11-10). On Reunion Day ({@link #DAY} of the 24-day calendar; the festivals keep theirs) every grown child of a
 * player comes home: at dusk, once they stand by their own hearth, each daughter who lives further off than {@link #HOME} (married into a
 * village, off on her own) arrives with her household: her villager sweetheart and her little ones, the player's grandchildren. A
 * daughter out of loaded range is fetched from where she was last seen (FriendLog), her chunk held open for a moment by a self-expiring
 * ticket. Then the reunion: hearts and fireworks, everyone {@link #CLOSER} closer, the grandchildren run to their grandparent. The next
 * morning everyone who came from afar goes back where they were (their own chunk held open for the trip). Same dimension only.
 * Off with the reunions option.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Reunions {
    public static final int DAY = 11, CLOSER = 6, FETCH_TICKS = 200;
    public static final double HOME = 32, ARRIVE = 5, HOUSEHOLD = 24, HEARTH = 8;
    static final String TOLD = "ReunionTold", DONE = "ReunionDone", ORIGIN = "VisitOrigin", ORIGIN_DIM = "VisitDim", BACK_AFTER = "VisitUntil";
    public static final String BORN_OF = "BornOf";
    /** Who is still being fetched, per player: their UUID, and when to give up. */
    private static final Map<UUID, Map<UUID, Long>> FETCHING = new HashMap<>();

    private Reunions() {
    }

    public static boolean isReunionDay(long dayTime) {
        return Math.floorMod(dayTime / 24000L, (long) Festivals.CYCLE) == DAY;
    }

    public static int daysUntil(long dayTime) {
        return (int) Math.floorMod(DAY - Math.floorMod(dayTime / 24000L, (long) Festivals.CYCLE), (long) Festivals.CYCLE);
    }

    /** The UUIDs of a player's grown-up-or-not children (not grandchildren), from the family journal. */
    public static List<UUID> children(Player player) {
        List<UUID> out = new ArrayList<>();
        for (Tag t : Marriage.childList(player)) {
            CompoundTag k = (CompoundTag) t;
            if (k.getBoolean("Grand")) continue;
            try {
                out.add(UUID.fromString(k.getString("Id")));
            } catch (IllegalArgumentException ignored) {
                // a hand-edited journal: skip it
            }
        }
        return out;
    }

    /** A grown child of this player, wherever she is loaded in this level, or null. */
    @Nullable
    static Mob find(ServerLevel level, UUID id) {
        return level.getEntity(id) instanceof Mob m && m.isAlive() ? m : null;
    }

    /** Her household near her: her villager sweetheart and her own little ones. */
    public static List<Mob> household(ServerLevel level, Mob daughter) {
        List<Mob> out = new ArrayList<>();
        UUID sweet = Sweethearts.sweetheart(daughter);
        if (sweet != null && level.getEntity(sweet) instanceof Villager v && v.isAlive()) out.add(v);
        for (Mob m : level.getEntitiesOfClass(Mob.class, daughter.getBoundingBox().inflate(HOUSEHOLD), m -> m.isAlive() && m.isBaby())) {
            UUID mother = m.getPersistentData().hasUUID(BORN_OF) ? m.getPersistentData().getUUID(BORN_OF) : Nursing.motherOf(m);
            if (daughter.getUUID().equals(mother)) out.add(m);
        }
        return out;
    }

    /** She (and whoever comes with her) arrives by the hearth, remembering where to go back to afterwards. */
    static void arrive(ServerLevel level, Mob who, BlockPos hearth) {
        CompoundTag pd = who.getPersistentData();
        if (!pd.contains(ORIGIN)) {
            pd.putLong(ORIGIN, who.blockPosition().asLong());
            pd.putString(ORIGIN_DIM, level.dimension().location().toString());
        }
        pd.putLong(BACK_AFTER, (level.getDayTime() / 24000L + 1) * 24000L + 1000L);   // the next morning
        double a = who.getRandom().nextDouble() * Math.PI * 2, r = 2.0 + who.getRandom().nextDouble() * (ARRIVE - 2.0);
        int x = hearth.getX() + (int) Math.round(Math.cos(a) * r), z = hearth.getZ() + (int) Math.round(Math.sin(a) * r);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (Math.abs(y - hearth.getY()) > 4) y = hearth.getY();               // a roof or a pit: by the fire, then
        who.teleportTo(x + 0.5, y, z + 0.5);
        who.getNavigation().stop();
        level.sendParticles(ParticleTypes.HEART, who.getX(), who.getEyeY() + 0.3, who.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
    }

    /** A daughter living away comes home with her household. How many arrived (0 if she lives close by already). */
    public static int bringHome(ServerLevel level, Mob daughter, BlockPos hearth) {
        if (daughter.distanceToSqr(hearth.getCenter()) <= HOME * HOME) return 0;
        List<Mob> coming = household(level, daughter);
        arrive(level, daughter, hearth);
        for (Mob m : coming) arrive(level, m, hearth);
        return 1 + coming.size();
    }

    /**
     * Reunion time, at their own hearth: everyone living away who is loaded comes home now; the rest are fetched over the next few
     * seconds (fetchTick). How many came straight away.
     */
    public static int gather(ServerLevel level, ServerPlayer player, BlockPos hearth) {
        int came = 0;
        Map<UUID, Long> fetch = FETCHING.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
        for (UUID id : children(player)) {
            Mob d = find(level, id);
            if (d != null) {
                if (!d.isBaby()) came += bringHome(level, d, hearth);
                continue;
            }
            for (FriendLog.Seen s : FriendLog.get(level.getServer()).of(player)) {   // out of range: open her chunk for a moment
                if (!s.mob().equals(id) || !s.dimension().equals(level.dimension().location().toString())) continue;
                level.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(s.pos()), 3, s.pos());
                fetch.put(id, level.getGameTime() + FETCH_TICKS);
            }
        }
        return came;
    }

    /** Every second while someone is being fetched: those now loaded come home; those not found in time are given up on. */
    public static int fetchTick(ServerLevel level, ServerPlayer player, BlockPos hearth) {
        Map<UUID, Long> fetch = FETCHING.get(player.getUUID());
        if (fetch == null || fetch.isEmpty()) return 0;
        int came = 0;
        for (var it = fetch.entrySet().iterator(); it.hasNext(); ) {
            var e = it.next();
            Mob d = find(level, e.getKey());
            if (d != null) {
                if (!d.isBaby()) came += bringHome(level, d, hearth);
                it.remove();
            } else if (level.getGameTime() > e.getValue()) {
                it.remove();
            }
        }
        if (came > 0) celebrate(level, player, hearth, false);
        return came;
    }

    /** The reunion: the family by the hearth, hearts and fireworks, everyone closer; grandchildren run to their grandparent. How many. */
    public static int celebrate(ServerLevel level, ServerPlayer player, BlockPos hearth, boolean firstTime) {
        int n = 0;
        for (Mob m : level.getEntitiesOfClass(Mob.class, new AABB(hearth).inflate(HOME), Mob::isAlive)) {
            boolean family = m.getPersistentData().contains(ORIGIN)                                   // visitors, a sweetheart too
                    || m instanceof Bonded b && (player.getUUID().equals(b.getFather()) || Marriage.married(m, player));
            if (!family) continue;
            n++;
            if (firstTime && m instanceof Bonded b) b.addAffection(player, CLOSER);
            if (m.isBaby()) m.getNavigation().moveTo(player, 1.1);           // grandchildren and little ones run to them
            level.sendParticles(ParticleTypes.HEART, m.getX(), m.getEyeY() + 0.3, m.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
        }
        if (firstTime) {
            level.sendParticles(ParticleTypes.FIREWORK, hearth.getX() + 0.5, hearth.getY() + 3, hearth.getZ() + 0.5, 60, 1.5, 1.0, 1.5, 0.08);
            level.playSound(null, hearth, SoundEvents.FIREWORK_ROCKET_TWINKLE, SoundSource.NEUTRAL, 1.0F, 1.0F);
            player.displayClientMessage(Component.translatable("message.monstergirls.reunion.together", n).withStyle(ChatFormatting.GOLD), false);
            if (n > 0) Dairy.award(player, "family_reunion");
        } else {
            player.displayClientMessage(Component.translatable("message.monstergirls.reunion.arrived").withStyle(ChatFormatting.LIGHT_PURPLE), true);
        }
        return n;
    }

    /** The morning after: a visitor goes back to where she came from (her chunk held open for the trip). Whether she went. */
    public static boolean goBack(ServerLevel level, Mob who) {
        CompoundTag pd = who.getPersistentData();
        if (!pd.contains(ORIGIN)) return false;
        BlockPos origin = BlockPos.of(pd.getLong(ORIGIN));
        if (!level.dimension().location().toString().equals(pd.getString(ORIGIN_DIM))) {
            pd.remove(ORIGIN);
            pd.remove(ORIGIN_DIM);
            pd.remove(BACK_AFTER);
            return false;
        }
        if (!level.isLoaded(origin)) {                                       // open her own chunk first; she sets off once it is
            level.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(origin), 3, origin);
            return false;
        }
        pd.remove(ORIGIN);
        pd.remove(ORIGIN_DIM);
        pd.remove(BACK_AFTER);
        who.teleportTo(origin.getX() + 0.5, origin.getY(), origin.getZ() + 0.5);
        who.getNavigation().stop();
        return true;
    }

    @SubscribeEvent
    public static void onVisitorTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Mob m && m.tickCount % 100 == 47 && m.level() instanceof ServerLevel level && m.getPersistentData().contains(BACK_AFTER)
                && level.getDayTime() >= m.getPersistentData().getLong(BACK_AFTER)) {
            goBack(level, m);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || p.tickCount % 20 != 13 || !(p.level() instanceof ServerLevel level) || !MonsterGirlsConfig.reunions()) return;
        long time = level.getDayTime(), day = time / 24000L + 1;
        BlockPos hearth = ownHearth(level, p);
        if (hearth != null) fetchTick(level, p, hearth);
        if (!isReunionDay(time) || children(p).isEmpty()) return;
        CompoundTag journal = p.getData(ModAttachments.JOURNAL);
        if (journal.getLong(TOLD) != day) {
            journal.putLong(TOLD, day);
            p.setData(ModAttachments.JOURNAL, journal);
            p.displayClientMessage(Component.translatable("message.monstergirls.reunion.today").withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }
        long t = Math.floorMod(time, 24000L);
        if (t < Festivals.DUSK || t >= Festivals.LATE || journal.getLong(DONE) == day || hearth == null) return;
        journal.putLong(DONE, day);
        p.setData(ModAttachments.JOURNAL, journal);
        gather(level, p, hearth);
        celebrate(level, p, hearth, true);
    }

    /** Their own loaded hearth within HEARTH, or null. */
    @Nullable
    static BlockPos ownHearth(ServerLevel level, Player p) {
        for (BlockPos h : HearthBlockEntity.loaded(level)) {
            if (h.distToCenterSqr(p.position()) <= HEARTH * HEARTH && level.getBlockEntity(h) instanceof HearthBlockEntity be && p.getUUID().equals(be.owner())) return h;
        }
        return null;
    }

    /** For tests: who has been told, and is somebody visiting. */
    public static boolean visiting(Entity e) {
        return e.getPersistentData().contains(ORIGIN);
    }
}
