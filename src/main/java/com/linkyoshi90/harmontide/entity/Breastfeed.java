package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.network.ModPayloads;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * A feed at her chest. A mother already nurses her baby (NurseGoal). With the adult option on, a grown girl who has
 * milk of her own may offer a hungry player the same: she comes over, or he right-clicks her, she asks, and on yes
 * she holds them there until the hunger bar is full. Once he is hungry again she can do it again: the feed
 * does not use up her milk. Babies, childlike height, males and her own father are never offered it.
 * The days she is expecting or just gave birth do not count her out, which is when the milk is there.
 */
public final class Breastfeed {
    /** Half the hunger bar, or less. More than half and she does not ask. */
    public static final int HUNGRY = 10;
    /** How long the hold lasts, and how long she waits on the question. */
    public static final int FEED_TICKS = 80, OFFER_TICKS = 400;
    /** Taller than the player by a clear head: she picks them up. Shorter than their eyes: she climbs up. */
    public static final float TOWER = 2.15F, SMALL = 1.65F;
    private static final String NEXT = "BreastfeedNext";
    /** His mouth, above his feet. Her chest meets it. */
    private static final double MOUTH = 1.45;

    public enum Kind { TOWER, BEND, SMALL }

    private enum Phase { SEEK, OFFER, FEED }

    private static final class Session {
        final UUID player;
        Phase phase = Phase.SEEK;
        int ticks;
        boolean lifted;
        boolean hadNoGravity;

        Session(UUID player) {
            this.player = player;
        }
    }

    private static final Map<Mob, Session> SESSIONS = new WeakHashMap<>();

    private Breastfeed() {}

    /** How she has to move for her chest to meet his mouth. */
    public static Kind kind(Mob girl) {
        float height = girl.getBbHeight();
        if (height >= TOWER) return Kind.TOWER;
        if (height < SMALL) return Kind.SMALL;
        return Kind.BEND;
    }

    /**
     * Grown, not her father, has milk, and he is actually hungry. Pregnancy and the day after a birth do not lock
     * this: those are the days the milk is in. The option itself is separate.
     */
    public static boolean canOffer(Mob girl, Player player) {
        if (!HandMilking.interested(girl, player) || !Lactation.isLactating(girl) || WildEncounter.active(girl)) return false;
        if (player.isSpectator() || !player.isAlive() || girl.getTarget() != null) return false;
        return player.getFoodData().getFoodLevel() <= HUNGRY;
    }

    /**
     * An empty-hand right-click can ask. True on the client as well, so the pat does not play in its place.
     * The server still checks she has milk before the question actually opens.
     */
    public static boolean clickWouldAsk(Mob girl, Player player) {
        if (player.isSpectator() || !player.isAlive() || girl.getTarget() != null) return false;
        if (player.getFoodData().getFoodLevel() > HUNGRY) return false;
        return HandMilking.interested(girl, player);
    }

    /** She asks now, because he clicked her. A feed already going is left to finish. */
    public static boolean ask(Mob girl, ServerPlayer player) {
        Session session = SESSIONS.get(girl);
        if (session != null && !session.player.equals(player.getUUID())) return false;
        if (session != null && session.phase == Phase.FEED) return true;
        if (!canOffer(girl, player)) return false;
        if (session == null) {
            session = new Session(player.getUUID());
            SESSIONS.put(girl, session);
        }
        session.phase = Phase.OFFER;
        session.ticks = 0;
        girl.getNavigation().stop();
        girl.setDeltaMovement(Vec3.ZERO);
        face(girl, player.getX(), player.getZ());
        if (player.connection != null && player.connection.hasChannel(ModPayloads.OpenFeed.TYPE)) {
            PacketDistributor.sendToPlayer(player, new ModPayloads.OpenFeed(girl.getId()));
        }
        return true;
    }

    public static boolean active(Mob girl) {
        return SESSIONS.containsKey(girl);
    }

    /** Time to walk over, if the roll says so and no baby of hers is waiting. */
    public static boolean wants(Mob girl) {
        if (active(girl) || WildEncounter.active(girl) || girl.isBaby() || girl.getTarget() != null || !idle(girl)) return false;
        if (girl.tickCount % 40 != 0 || !(girl.level() instanceof ServerLevel level)) return false;
        long now = level.getGameTime();
        if (now < girl.getPersistentData().getLong(NEXT)) return false;
        if (babyWaiting(level, girl)) return false;
        Player hungry = nearest(level, girl);
        if (hungry == null) return false;
        if (girl.getRandom().nextInt(3) != 0) {
            girl.getPersistentData().putLong(NEXT, now + 800);
            return false;
        }
        SESSIONS.put(girl, new Session(hungry.getUUID()));
        return true;
    }

    public static void tick(Mob girl) {
        Session session = SESSIONS.get(girl);
        if (!(girl.level() instanceof ServerLevel level) || session == null) return;
        Player player = level.getPlayerByUUID(session.player);
        if (!(player instanceof ServerPlayer server) || !server.isAlive()) {
            end(girl, false);
            return;
        }
        girl.getLookControl().setLookAt(server);
        switch (session.phase) {
            case SEEK -> seek(girl, server, session);
            case OFFER -> offer(girl, server, session);
            case FEED -> feed(level, girl, server, session);
        }
    }

    /** She asked, and he said yes. Ignored unless she is still waiting on him. */
    public static void accept(ServerPlayer player, int entityId) {
        if (!(player.level().getEntity(entityId) instanceof Mob girl)) return;
        Session session = SESSIONS.get(girl);
        if (session == null || session.phase != Phase.OFFER || !session.player.equals(player.getUUID())) return;
        if (!canOffer(girl, player) || player.distanceToSqr(girl) > 36.0) {
            end(girl, false);
            return;
        }
        session.phase = Phase.FEED;
        session.ticks = 0;
    }

    /** She asked, and he said no. */
    public static void decline(ServerPlayer player, int entityId) {
        if (!(player.level().getEntity(entityId) instanceof Mob girl)) return;
        Session session = SESSIONS.get(girl);
        if (session == null || !session.player.equals(player.getUUID())) return;
        end(girl, false);
    }

    /** Start the hold at once. The question's yes, and the game test, both come through here. */
    public static boolean startFeed(Mob girl, ServerPlayer player) {
        if (!canOffer(girl, player)) return false;
        Session session = SESSIONS.computeIfAbsent(girl, g -> new Session(player.getUUID()));
        if (!session.player.equals(player.getUUID())) return false;
        session.phase = Phase.FEED;
        session.ticks = 0;
        return true;
    }

    /** The goal stopped, or she was interrupted. A finished meal has already ended itself. */
    public static void stop(Mob girl) {
        if (SESSIONS.containsKey(girl)) end(girl, false);
    }

    private static void seek(Mob girl, ServerPlayer player, Session session) {
        if (!canOffer(girl, player)) {
            end(girl, false);
            return;
        }
        if (player.distanceToSqr(girl) > 2.2 * 2.2) {
            if (girl.tickCount % 10 == 0) girl.getNavigation().moveTo(player, 1.0);
            return;
        }
        girl.getNavigation().stop();
        girl.setDeltaMovement(Vec3.ZERO);
        face(girl, player.getX(), player.getZ());
        if (player.connection != null && player.connection.hasChannel(ModPayloads.OpenFeed.TYPE)) {
            PacketDistributor.sendToPlayer(player, new ModPayloads.OpenFeed(girl.getId()));
            session.phase = Phase.OFFER;
            session.ticks = 0;
        } else {
            end(girl, false);
        }
    }

    private static void offer(Mob girl, ServerPlayer player, Session session) {
        girl.getNavigation().stop();
        girl.setDeltaMovement(Vec3.ZERO);
        face(girl, player.getX(), player.getZ());
        if (player.distanceToSqr(girl) > 10.0 * 10.0 || ++session.ticks > OFFER_TICKS) end(girl, false);
    }

    private static void feed(ServerLevel level, Mob girl, ServerPlayer player, Session session) {
        if (!player.isAlive() || player.distanceToSqr(girl) > 12.0 * 12.0) {
            end(girl, false);
            return;
        }
        pose(girl, true);
        bare(girl, 15);
        if (girl instanceof Bonded bonded && session.ticks == 0) bonded.blushFor(FEED_TICKS);
        place(girl, player, session);
        if (session.ticks % 20 == 0) {
            double[] chest = Lactation.chest(girl);
            level.sendParticles(ParticleTypes.HEART, girl.getX(), girl.getY() + chest[0], girl.getZ(), 1, 0.15, 0.08, 0.15, 0.0);
        }
        if (++session.ticks >= FEED_TICKS) {
            player.getFoodData().eat(20, 20.0F);
            Lactation.relieve(level, girl, player);
            level.sendParticles(ParticleTypes.HEART, player.getX(), player.getEyeY(), player.getZ(), 4, 0.2, 0.15, 0.2, 0.0);
            end(girl, true);
        }
    }

    /** Tower: he comes up to her chest. Bend: she folds over him where he stands. Small: she jumps or hovers up to his mouth. */
    private static void place(Mob girl, ServerPlayer player, Session session) {
        girl.getNavigation().stop();
        girl.setDeltaMovement(Vec3.ZERO);
        double[] chest = Lactation.chest(girl);
        switch (kind(girl)) {
            case TOWER -> {
                face(girl, player.getX(), player.getZ());
                float yaw = girl.getYRot();
                double forward = chest[1] + 0.35;
                double x = girl.getX() - Mth.sin(yaw * Mth.DEG_TO_RAD) * forward;
                double z = girl.getZ() + Mth.cos(yaw * Mth.DEG_TO_RAD) * forward;
                double y = girl.getY() + chest[0] - MOUTH;
                player.teleportTo(x, y, z);
                player.setYRot(yaw + 180.0F);
                player.setXRot(-22.0F);
                player.setDeltaMovement(Vec3.ZERO);
                player.fallDistance = 0.0F;
            }
            case BEND -> {
                float yaw = player.getYRot();
                double x = player.getX() - Mth.sin(yaw * Mth.DEG_TO_RAD) * 0.65;
                double z = player.getZ() + Mth.cos(yaw * Mth.DEG_TO_RAD) * 0.65;
                girl.moveTo(x, player.getY(), z, yaw + 180.0F, 28.0F);
                girl.setYBodyRot(yaw + 180.0F);
                girl.setYHeadRot(yaw + 180.0F);
            }
            case SMALL -> {
                float yaw = player.getYRot();
                double x = player.getX() - Mth.sin(yaw * Mth.DEG_TO_RAD) * 0.4;
                double z = player.getZ() + Mth.cos(yaw * Mth.DEG_TO_RAD) * 0.4;
                double y = player.getY() + MOUTH - chest[0];
                if (!session.lifted) {
                    session.hadNoGravity = girl.isNoGravity();
                    session.lifted = true;
                }
                girl.setNoGravity(true);
                girl.moveTo(x, y, z, yaw + 180.0F, 8.0F);
                girl.setYBodyRot(yaw + 180.0F);
                girl.setYHeadRot(yaw + 180.0F);
                girl.setDeltaMovement(Vec3.ZERO);
                girl.fallDistance = 0.0F;
            }
        }
    }

    private static void end(Mob girl, boolean fed) {
        Session session = SESSIONS.remove(girl);
        if (session == null) return;
        long wait = fed ? 0L : 3600L;
        if (girl.level() instanceof ServerLevel level) girl.getPersistentData().putLong(NEXT, level.getGameTime() + wait);
        if (session.lifted) girl.setNoGravity(session.hadNoGravity);
        pose(girl, false);
        dress(girl);
    }

    private static boolean idle(Mob girl) {
        if (girl instanceof AbstractMonsterGirl grown) return grown.getAction() == AbstractMonsterGirl.Action.NONE;
        if (girl instanceof AbstractHarpy harpy) return harpy.getAction() == AbstractHarpy.Action.NONE;
        if (girl instanceof AbstractAnuran anuran) return anuran.getAction() == AbstractAnuran.Action.NONE;
        return false;
    }

    /** Her own baby is close enough to nurse, so the player waits. */
    private static boolean babyWaiting(ServerLevel level, Mob girl) {
        if (!Nursing.isNursing(girl)) return false;
        for (AbstractMonsterGirl child : Nursing.loadedChildren(level, girl)) {
            if (child.isBaby() && child.distanceToSqr(girl) < 16.0 * 16.0) return true;
        }
        return false;
    }

    @Nullable
    private static Player nearest(ServerLevel level, Mob girl) {
        Player best = null;
        double bestDist = 12.0 * 12.0;
        for (Player player : level.players()) {
            if (!canOffer(girl, player)) continue;
            double dist = player.distanceToSqr(girl);
            if (dist < bestDist) {
                bestDist = dist;
                best = player;
            }
        }
        return best;
    }

    private static void face(Mob girl, double x, double z) {
        double dx = x - girl.getX();
        double dz = z - girl.getZ();
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        girl.setYRot(yaw);
        girl.setYBodyRot(yaw);
        girl.setYHeadRot(yaw);
    }

    private static void pose(Mob girl, boolean on) {
        if (girl instanceof AbstractMonsterGirl grown) {
            if (on) grown.setAction(AbstractMonsterGirl.Action.FEED);
            else if (grown.getAction() == AbstractMonsterGirl.Action.FEED) grown.setAction(AbstractMonsterGirl.Action.NONE);
        } else if (girl instanceof AbstractHarpy harpy) {
            if (on) harpy.setAction(AbstractHarpy.Action.FEED);
            else if (harpy.getAction() == AbstractHarpy.Action.FEED) harpy.setAction(AbstractHarpy.Action.NONE);
        } else if (girl instanceof AbstractAnuran anuran) {
            if (on) anuran.setAction(AbstractAnuran.Action.FEED);
            else if (anuran.getAction() == AbstractAnuran.Action.FEED) anuran.setAction(AbstractAnuran.Action.NONE);
        }
    }

    private static void bare(Mob girl, int ticks) {
        if (girl instanceof AbstractMonsterGirl grown) grown.bareFor(ticks);
        else if (girl instanceof AbstractHarpy harpy) harpy.bareFor(ticks);
        else if (girl instanceof AbstractAnuran anuran) anuran.bareFor(ticks);
    }

    private static void dress(Mob girl) {
        if (girl instanceof AbstractMonsterGirl grown) grown.dressAfter(30);
        else if (girl instanceof AbstractHarpy harpy) harpy.dressAfter(30);
        else if (girl instanceof AbstractAnuran anuran) anuran.dressAfter(30);
    }
}
