package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.ai.ShareBedGoal;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Sometimes a second grown girl joins a bed scene. One keeps the body's own ride ({@link Nsfw#rock}).
 * The other sits over his face. After one round they trade places and stay that way.
 * A mother and her grown daughter may share it, and they say so. A grown daughter still needs a condom
 * and does not conceive from being the guest. The young, the men, and anyone of childlike height stay out.
 * Nobody is jealous, and nobody is sent away. The rolls stay off on the game-test server.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Group {
    /** How long the first arrangement lasts before they trade. */
    public static final int ROUND = 160;
    private static final int ODDS = 3;
    private static final int APPROACH = 300;
    private static final double RANGE = 16.0;
    private static final double CLOSE = 2.5;
    private static final Map<UUID, Session> BY_PLAYER = new HashMap<>();
    private static final Map<UUID, UUID> BY_MOB = new HashMap<>();
    /** The night a sleeper already had his one roll, whether or not a second girl came. */
    private static final Map<UUID, Long> ROLLED = new HashMap<>();

    private static final class Session {
        final UUID rider;
        final UUID guest;
        final UUID player;
        final BlockPos bed;
        int approach;
        int ticks;
        boolean posed;
        boolean swapped;
        boolean kin;

        Session(UUID rider, UUID guest, UUID player, BlockPos bed) {
            this.rider = rider;
            this.guest = guest;
            this.player = player;
            this.bed = bed;
        }
    }

    private Group() {}

    /** She is the girl already in the bed, or the one who came to join her. */
    public static boolean active(Mob mob) {
        return mob != null && BY_MOB.containsKey(mob.getUUID());
    }

    /** The girl who came to join, not the one the bed already had. */
    public static boolean guest(Mob mob) {
        Session session = session(mob);
        return session != null && mob.getUUID().equals(session.guest);
    }

    /** She is still walking to the bed. */
    public static boolean approaching(Mob mob) {
        Session session = session(mob);
        return session != null && mob.getUUID().equals(session.guest) && !session.posed;
    }

    /** She is over his face this round, so the bed goal must not also ride her. */
    public static boolean onFace(Mob mob) {
        Session session = session(mob);
        if (session == null || !session.posed) return false;
        return session.swapped ? mob.getUUID().equals(session.rider) : mob.getUUID().equals(session.guest);
    }

    /**
     * The moon turn waits while a second girl is on her way, and through both rounds, so the trade is visible.
     * A night that is not a full moon is unchanged: they stay until he wakes.
     */
    public static boolean holding(Mob mob) {
        Session session = session(mob);
        return session != null && mob.getUUID().equals(session.rider) && session.ticks < ROUND * 2;
    }

    @SubscribeEvent
    public static void onEntity(EntityTickEvent.Post event) {
        if (GameTestHooks.isGametestServer()) return;
        if (!(event.getEntity() instanceof Mob mob) || !(mob instanceof Bonded) || mob.tickCount % 40 != 0) return;
        if (!(mob.level() instanceof ServerLevel level) || !Nsfw.on()) return;
        if (!Nsfw.kneeling(mob) && !Nsfw.sittingBack(mob) && !Nsfw.coupling(mob)) return;
        consider(level, mob);
    }

    @SubscribeEvent
    public static void onServer(ServerTickEvent.Post event) {
        tick(event.getServer());
    }

    /** One beat of every group still going. */
    public static void tick(MinecraftServer server) {
        if (BY_PLAYER.isEmpty()) return;
        for (UUID id : new ArrayList<>(BY_PLAYER.keySet())) {
            Session session = BY_PLAYER.get(id);
            if (session == null) continue;
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player == null || !player.isAlive() || player.isSpectator() || !player.isSleeping()
                    || !(player.level() instanceof ServerLevel level)) {
                end(server, session, session.posed);
                continue;
            }
            Mob rider = mob(level, session.rider);
            Mob guest = mob(level, session.guest);
            if (rider == null || guest == null || !rider.isAlive() || !guest.isAlive()) {
                end(server, session, session.posed);
                continue;
            }
            if (!(rider instanceof Bonded bonded) || !id.equals(bonded.getGestation().getSharingWith())) {
                end(server, session, session.posed);
                continue;
            }
            if (guest.getTarget() == player && guest.hurtTime > 0) {
                end(server, session, session.posed);
                continue;
            }
            if (!session.posed) {
                if (++session.approach > APPROACH || guest.distanceToSqr(player) > (RANGE + 8.0) * (RANGE + 8.0)) {
                    end(server, session, false);
                    continue;
                }
                if (guest.distanceToSqr(approachSpot(level, session.bed)) > CLOSE * CLOSE) continue;
                session.posed = true;
                session.kin = family(rider, guest);
                greet(level, rider, guest, player, session);
            }
            if (!session.swapped && !Nsfw.kneeling(rider) && !Nsfw.sittingBack(rider) && !Nsfw.coupling(rider)) {
                end(server, session, session.posed);
                continue;
            }
            if (!session.swapped && session.ticks >= ROUND) {
                session.swapped = true;
                guest.noPhysics = false;
                switched(player, rider, guest, session);
            }
            Mob face = session.swapped ? rider : guest;
            Mob hips = session.swapped ? guest : rider;
            hips.noPhysics = false;
            if (session.swapped) {
                Nsfw.rock(level, hips, player, ShareBedGoal.spotInBed(level, session.bed), ShareBedGoal.yawInBed(level, session.bed), session.bed);
            }
            placeFace(level, face, session.bed);
            session.ticks++;
        }
    }

    /** She walks toward the bed. The pose starts once she is beside it. */
    public static void steer(Mob guest) {
        if (!(guest.level() instanceof ServerLevel level)) return;
        Session session = session(guest);
        if (session == null || !guest.getUUID().equals(session.guest) || session.posed) return;
        Vec3 spot = approachSpot(level, session.bed);
        guest.getLookControl().setLookAt(spot.x, spot.y, spot.z);
        if (guest.distanceToSqr(spot) <= CLOSE * CLOSE) guest.getNavigation().stop();
        else if (guest.tickCount % 10 == 0) guest.getNavigation().moveTo(spot.x, spot.y, spot.z, 1.15);
    }

    /**
     * One roll a night, and only when a second grown girl is already close enough to join.
     * A mother or grown daughter of the girl in the bed is preferred. Young stay out.
     */
    public static void consider(ServerLevel level, Mob rider) {
        if (!(rider instanceof Bonded bonded)) return;
        UUID sharing = bonded.getGestation().getSharingWith();
        if (sharing == null || BY_PLAYER.containsKey(sharing)) return;
        if (!(level.getPlayerByUUID(sharing) instanceof ServerPlayer player) || !player.isSleeping() || player.isSpectator()) return;
        long night = (level.getDayTime() + 12000L) / 24000L;
        Long rolled = ROLLED.get(sharing);
        if (rolled != null && rolled == night) return;
        Mob guest = pick(level, rider, player);
        if (guest == null) return;
        ROLLED.put(sharing, night);
        if (rider.getRandom().nextInt(ODDS) != 0) return;
        open(rider, guest, player);
    }

    /**
     * Starts a group without the nightly roll. Still refuses the young, the men, a daughter with no condom,
     * and anyone who is not already close enough for the bed. The guest does not conceive.
     */
    public static boolean join(ServerLevel level, Mob rider, Mob guest, ServerPlayer player) {
        if (player.level() != level || !Nsfw.on() || BY_PLAYER.containsKey(player.getUUID()) || player.getSleepingPos().isEmpty()) return false;
        if (!(rider instanceof Bonded bonded) || !player.getUUID().equals(bonded.getGestation().getSharingWith())) return false;
        if (!player.isSleeping() || !welcome(guest, rider, player)) return false;
        open(rider, guest, player);
        return BY_PLAYER.containsKey(player.getUUID());
    }

    private static void open(Mob rider, Mob guest, ServerPlayer player) {
        BlockPos bed = player.getSleepingPos().orElse(null);
        if (bed == null) return;
        Session session = new Session(rider.getUUID(), guest.getUUID(), player.getUUID(), bed.immutable());
        BY_PLAYER.put(player.getUUID(), session);
        BY_MOB.put(rider.getUUID(), player.getUUID());
        BY_MOB.put(guest.getUUID(), player.getUUID());
        guest.setTarget(null);
        guest.getNavigation().stop();
    }

    @Nullable
    private static Mob pick(ServerLevel level, Mob rider, ServerPlayer player) {
        List<Mob> found = level.getEntitiesOfClass(Mob.class, rider.getBoundingBox().inflate(RANGE), mob -> welcome(mob, rider, player));
        Mob kin = null;
        double kinDist = Double.MAX_VALUE;
        Mob any = null;
        double anyDist = Double.MAX_VALUE;
        for (Mob mob : found) {
            double dist = mob.distanceToSqr(rider);
            if (dist < anyDist) {
                anyDist = dist;
                any = mob;
            }
            if (family(rider, mob) && dist < kinDist) {
                kinDist = dist;
                kin = mob;
            }
        }
        return kin != null ? kin : any;
    }

    /** Grown, close, and free. A daughter of his needs a condom in hand. Men and the young fail {@link Nsfw#close}. */
    private static boolean welcome(Mob mob, Mob rider, ServerPlayer player) {
        if (mob == rider || !mob.isAlive() || mob.isBaby() || mob.isPassenger() || mob.isSleeping()) return false;
        if (mob.getTarget() != null) return false;
        if (!(mob instanceof Bonded bonded) || bonded.isOrderedToSit()) return false;
        if (bonded.getGestation().getSharingWith() != null) return false;
        if (Orders.holdsPlace(mob) || active(mob)) return false;
        if (!Nsfw.close(mob, player)) return false;
        if (WildEncounter.active(mob) || Breastfeed.active(mob) || Horniness.active(mob) || Bathing.active(mob) || Dreaming.active(mob) || NpcSex.active(mob)) return false;
        if (MorningOral.posing(mob) || MorningOral.pending(mob)) return false;
        if (Nsfw.kneeling(mob) || Nsfw.sittingBack(mob) || Nsfw.coupling(mob) || Nsfw.standing(mob)) return false;
        return !working(mob);
    }

    private static boolean working(Mob mob) {
        if (mob instanceof AbstractMonsterGirl girl) {
            AbstractMonsterGirl.Action action = girl.getAction();
            return action == AbstractMonsterGirl.Action.LABOR || action == AbstractMonsterGirl.Action.FEED
                    || action == AbstractMonsterGirl.Action.NURSE || action == AbstractMonsterGirl.Action.ORAL
                    || action == AbstractMonsterGirl.Action.STAND || action == AbstractMonsterGirl.Action.FACE;
        }
        if (mob instanceof AbstractHarpy harpy) {
            AbstractHarpy.Action action = harpy.getAction();
            return action == AbstractHarpy.Action.FEED || action == AbstractHarpy.Action.ORAL
                    || action == AbstractHarpy.Action.LAYING || action == AbstractHarpy.Action.STAND
                    || action == AbstractHarpy.Action.FACE;
        }
        if (mob instanceof AbstractAnuran anuran) {
            AbstractAnuran.Action action = anuran.getAction();
            return action == AbstractAnuran.Action.FEED || action == AbstractAnuran.Action.ORAL
                    || action == AbstractAnuran.Action.LAYING || action == AbstractAnuran.Action.STAND
                    || action == AbstractAnuran.Action.FACE;
        }
        return false;
    }

    private static void greet(ServerLevel level, Mob rider, Mob guest, ServerPlayer player, Session session) {
        if (rider instanceof Bonded bonded) bonded.blushFor(ROUND * 2 + 40);
        if (guest instanceof Bonded bonded) {
            bonded.blushFor(ROUND * 2 + 40);
            bonded.addAffection(player, 4);
        }
        Mob mother = motherOf(rider, guest);
        MutableComponent line;
        if (mother == null) line = Component.translatable("message.harmontide.group.join", guest.getDisplayName(), rider.getDisplayName());
        else if (mother == rider) line = Component.translatable("message.harmontide.group.kin.daughter", rider.getDisplayName(), guest.getDisplayName());
        else line = Component.translatable("message.harmontide.group.kin.mother", guest.getDisplayName(), rider.getDisplayName());
        player.displayClientMessage(line.withStyle(ChatFormatting.LIGHT_PURPLE), false);
        level.sendParticles(ParticleTypes.HEART, guest.getX(), guest.getEyeY(), guest.getZ(), 5, 0.25, 0.15, 0.25, 0.0);
    }

    private static void switched(ServerPlayer player, Mob rider, Mob guest, Session session) {
        if (rider instanceof Bonded bonded) bonded.addAffection(player, 2);
        if (guest instanceof Bonded bonded) bonded.addAffection(player, 2);
        String key = session.kin ? "message.harmontide.group.kin.switch" : "message.harmontide.group.switch";
        player.displayClientMessage(Component.translatable(key, guest.getDisplayName(), rider.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
    }

    /** Beside the bed, where she can walk. The face itself is placed once she arrives. */
    private static Vec3 approachSpot(ServerLevel level, BlockPos bed) {
        BlockPos beside = ShareBedGoal.findSpotBeside(level, bed);
        if (beside != null) return Vec3.atBottomCenterOf(beside);
        return Vec3.atBottomCenterOf(bed);
    }

    /** Over the pillow, looking down the bed toward whoever is riding. */
    private static void placeFace(ServerLevel level, Mob mob, BlockPos bed) {
        BlockState state = level.getBlockState(bed);
        Direction facing = state.hasProperty(BedBlock.FACING) ? state.getValue(BedBlock.FACING) : Direction.NORTH;
        Direction foot = facing.getOpposite();
        Vec3 head = Vec3.atBottomCenterOf(bed);
        double x = head.x + foot.getStepX() * 0.15;
        double z = head.z + foot.getStepZ() * 0.15;
        double y = head.y + faceHeight(mob);
        float yaw = foot.toYRot();
        mob.noPhysics = true;
        mob.setDeltaMovement(Vec3.ZERO);
        mob.getNavigation().stop();
        mob.moveTo(x, y, z, yaw, 12.0F);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
        setFace(mob);
        if (mob.tickCount % 40 == 0 && mob instanceof Bonded bonded) bonded.blushFor(80);
    }

    private static double faceHeight(Mob mob) {
        if (Nsfw.quadruped(mob)) return 0.42;
        if (mob instanceof AbstractMonsterGirl girl) {
            GirlSpecies.Family family = girl.getSpecies().family();
            if (family == GirlSpecies.Family.LAMIA || family == GirlSpecies.Family.MERFOLK) return 0.48;
            if (family == GirlSpecies.Family.ARACHNE) return 0.55;
            if (family == GirlSpecies.Family.SLIME) return 0.40;
        }
        return 0.62;
    }

    private static void setFace(Mob mob) {
        if (mob instanceof AbstractMonsterGirl girl) girl.setAction(AbstractMonsterGirl.Action.FACE);
        else if (mob instanceof AbstractHarpy harpy) harpy.setAction(AbstractHarpy.Action.FACE);
        else if (mob instanceof AbstractAnuran anuran) anuran.setAction(AbstractAnuran.Action.FACE);
    }

    private static void end(MinecraftServer server, Session session, boolean tookPart) {
        BY_PLAYER.remove(session.player);
        BY_MOB.remove(session.rider);
        BY_MOB.remove(session.guest);
        ServerPlayer player = server.getPlayerList().getPlayer(session.player);
        Mob guest = null;
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(session.guest) instanceof Mob mob) {
                guest = mob;
                releaseGuest(mob);
            }
            if (level.getEntity(session.rider) instanceof Mob mob) releaseRider(mob);
        }
        // The guest never conceives. A grown daughter uses a condom only if she actually joined the scene.
        // The girl who was already in the bed is settled in the morning, the same way as any other night.
        if (!tookPart || player == null || guest == null || !(guest.level() instanceof ServerLevel level)) return;
        if (!Condom.grownOffspring(guest, player)) return;
        if (!Condom.take(player)) Condom.mention(level, guest, player);
    }

    private static void releaseGuest(Mob mob) {
        mob.noPhysics = false;
        if (Nsfw.facing(mob) || Nsfw.kneeling(mob) || Nsfw.sittingBack(mob) || Nsfw.coupling(mob)) idle(mob);
    }

    private static void releaseRider(Mob mob) {
        mob.noPhysics = false;
        if (Nsfw.facing(mob)) idle(mob);
    }

    private static void idle(Mob mob) {
        boolean sit = mob instanceof Bonded bonded && bonded.isOrderedToSit();
        if (mob instanceof AbstractMonsterGirl girl) girl.setAction(sit ? AbstractMonsterGirl.Action.SITTING : AbstractMonsterGirl.Action.NONE);
        else if (mob instanceof AbstractHarpy harpy) harpy.setAction(sit ? AbstractHarpy.Action.SITTING : AbstractHarpy.Action.NONE);
        else if (mob instanceof AbstractAnuran anuran) anuran.setAction(sit ? AbstractAnuran.Action.SITTING : AbstractAnuran.Action.NONE);
    }

    @Nullable
    private static Session session(Mob mob) {
        if (mob == null) return null;
        UUID player = BY_MOB.get(mob.getUUID());
        if (player == null) return null;
        Session session = BY_PLAYER.get(player);
        if (session == null) return null;
        if (!mob.getUUID().equals(session.rider) && !mob.getUUID().equals(session.guest)) return null;
        return session;
    }

    @Nullable
    private static Mob mob(ServerLevel level, UUID id) {
        return level.getEntity(id) instanceof Mob mob ? mob : null;
    }

    @Nullable
    private static Mob motherOf(Mob a, Mob b) {
        if (parentOf(a, b)) return a;
        if (parentOf(b, a)) return b;
        return null;
    }

    private static boolean family(Mob a, Mob b) {
        return parentOf(a, b) || parentOf(b, a);
    }

    /** She bore the younger one, by the birth link or by having nursed her. */
    private static boolean parentOf(Mob mother, Mob young) {
        UUID id = mother.getUUID();
        if (young.getPersistentData().hasUUID(Reunions.BORN_OF) && id.equals(young.getPersistentData().getUUID(Reunions.BORN_OF))) return true;
        UUID nursed = Nursing.motherOf(young);
        return id.equals(nursed);
    }
}
