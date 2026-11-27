package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Minotaur;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Befriended girls in season sometimes run to each other. Two on two legs grind hips together. Two taurs, one wears
 * a strap and takes the other from behind. A girl may instead catch an adult villager. Two adult male minotaurs may
 * take each other from behind, and that does not make a calf. The young, anyone of childlike height, and baby villagers
 * stay out. None of this conceives, and nobody is sent away for it. The rolls stay off on the game-test server.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class NpcSex {
    private static final int SCENE = 160, APPROACH = 300, COOLDOWN = 6000;
    private static final int GIRL_ODDS = 4, VILLAGER_ODDS = 8, BULL_ODDS = 6;
    private static final double RANGE = 16, VILLAGE = 12, CLOSE = 2.8, LEAVE = 24;
    private static final String NEXT = "NpcNext";
    private static final Map<UUID, Pair> BY_MOB = new HashMap<>();

    private enum Kind { TRIBE, MOUNT, VILLAGER, BULLS }
    private enum Phase { APPROACH, POSE }

    private static final class Pair {
        final UUID lead;
        @Nullable final UUID other;
        @Nullable final UUID villager;
        final Kind kind;
        final boolean quad;
        Phase phase = Phase.APPROACH;
        int ticks;
        boolean told;
        boolean frozeVillager;
        double ax, ay, az;
        float ayaw;

        Pair(UUID lead, @Nullable UUID other, @Nullable UUID villager, Kind kind, boolean quad) {
            this.lead = lead;
            this.other = other;
            this.villager = villager;
            this.kind = kind;
            this.quad = quad;
        }
    }

    private NpcSex() {}

    public static boolean active(Mob mob) {
        return mob != null && BY_MOB.containsKey(mob.getUUID());
    }

    /** She is still walking over. Both of a pair run. A villager does not. */
    public static boolean approaching(Mob mob) {
        Pair pair = mob == null ? null : BY_MOB.get(mob.getUUID());
        return pair != null && pair.phase == Phase.APPROACH;
    }

    public static void steer(Mob mob) {
        Pair pair = BY_MOB.get(mob.getUUID());
        if (pair == null || pair.phase != Phase.APPROACH || !(mob.level() instanceof ServerLevel level)) return;
        UUID targetId = mob.getUUID().equals(pair.lead) ? (pair.villager != null ? pair.villager : pair.other) : pair.lead;
        Mob target = find(level.getServer(), targetId);
        if (target != null) mob.getNavigation().moveTo(target, 1.15);
    }

    @SubscribeEvent
    public static void onEntity(EntityTickEvent.Post event) {
        if (GameTestHooks.isGametestServer()) return;
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level) || !Nsfw.on()) return;
        if (!active(mob) && orphan(mob)) clearToIdle(mob);
        if (mob.tickCount % 100 != 0 || !(mob instanceof Bonded)) return;
        consider(level, mob);
    }

    @SubscribeEvent
    public static void onServer(ServerTickEvent.Post event) {
        tick(event.getServer());
    }

    /** One beat of every pairing still going. */
    public static void tick(MinecraftServer server) {
        if (BY_MOB.isEmpty()) return;
        for (UUID id : new ArrayList<>(BY_MOB.keySet())) {
            Pair pair = BY_MOB.get(id);
            if (pair == null || !pair.lead.equals(id)) continue;
            if (!Nsfw.on()) {
                end(server, pair, false);
                continue;
            }
            Mob lead = find(server, pair.lead);
            if (lead == null || !lead.isAlive() || hurt(lead)) {
                end(server, pair, false);
                continue;
            }
            if (pair.phase == Phase.APPROACH) stepApproach(server, lead, pair);
            else stepPose(server, lead, pair);
        }
    }

    /** She looks for a partner, a villager, or another bull. True if she set off. */
    public static boolean consider(ServerLevel level, Mob mob) {
        if (active(mob) || busy(mob) || level.getGameTime() < mob.getPersistentData().getLong(NEXT)) return false;
        if (mob instanceof Minotaur bull && bullReady(bull)) return considerBull(level, bull);
        if (!girlReady(level, mob)) return false;
        if (mob.getRandom().nextInt(VILLAGER_ODDS) == 0 && considerVillager(level, mob)) return true;
        if (mob.getRandom().nextInt(GIRL_ODDS) != 0) return false;
        Mob partner = nearestGirl(level, mob);
        if (partner == null) return false;
        boolean quad = Nsfw.quadruped(mob) && Nsfw.quadruped(partner);
        begin(mob, partner, null, quad ? Kind.MOUNT : Kind.TRIBE, quad);
        return true;
    }

    private static boolean considerBull(ServerLevel level, Minotaur bull) {
        if (bull.getRandom().nextInt(BULL_ODDS) != 0) return false;
        Minotaur partner = null;
        double best = RANGE * RANGE;
        for (Minotaur other : level.getEntitiesOfClass(Minotaur.class, bull.getBoundingBox().inflate(RANGE), o -> o != bull && bullReady(o) && !active(o) && !busy(o))) {
            double dist = other.distanceToSqr(bull);
            if (dist < best) {
                best = dist;
                partner = other;
            }
        }
        if (partner == null) return false;
        begin(bull, partner, null, Kind.BULLS, false);
        return true;
    }

    private static boolean considerVillager(ServerLevel level, Mob mob) {
        Villager villager = null;
        double best = VILLAGE * VILLAGE;
        for (Villager other : level.getEntitiesOfClass(Villager.class, mob.getBoundingBox().inflate(VILLAGE), NpcSex::villagerReady)) {
            double dist = other.distanceToSqr(mob);
            if (dist < best) {
                best = dist;
                villager = other;
            }
        }
        if (villager == null) return false;
        begin(mob, null, villager, Kind.VILLAGER, false);
        return true;
    }

    private static void begin(Mob lead, @Nullable Mob other, @Nullable Villager villager, Kind kind, boolean quad) {
        Pair pair = new Pair(lead.getUUID(), other == null ? null : other.getUUID(), villager == null ? null : villager.getUUID(), kind, quad);
        BY_MOB.put(lead.getUUID(), pair);
        if (other != null) BY_MOB.put(other.getUUID(), pair);
        lead.getNavigation().stop();
        if (other != null) other.getNavigation().stop();
    }

    private static void stepApproach(MinecraftServer server, Mob lead, Pair pair) {
        pair.ticks++;
        Mob other = partner(server, pair);
        if (other == null || !other.isAlive() || hurt(other) || lead.distanceTo(other) > LEAVE || pair.ticks > APPROACH) {
            end(server, pair, false);
            return;
        }
        if (lead.distanceTo(other) <= CLOSE) startPose(server, lead, other, pair);
    }

    private static void startPose(MinecraftServer server, Mob lead, Mob other, Pair pair) {
        pair.phase = Phase.POSE;
        pair.ticks = 0;
        pair.ax = other.getX();
        pair.ay = other.getY();
        pair.az = other.getZ();
        pair.ayaw = other.getYRot();
        if (pair.kind == Kind.VILLAGER) {
            pair.frozeVillager = !other.isNoAi();
            if (pair.frozeVillager) other.setNoAi(true);
        }
        bare(lead);
        if (pair.kind != Kind.VILLAGER) bare(other);
        if (lead instanceof Bonded bonded) bonded.blushFor(SCENE + 40);
        if (other instanceof Bonded bonded) bonded.blushFor(SCENE + 40);
        if (!pair.told) {
            pair.told = true;
            tell(lead, other, pair);
        }
        if (lead.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.HEART, lead.getX(), lead.getEyeY() + 0.2, lead.getZ(), 6, 0.3, 0.2, 0.3, 0.0);
        }
    }

    private static void stepPose(MinecraftServer server, Mob lead, Pair pair) {
        Mob other = partner(server, pair);
        if (other == null || !other.isAlive() || hurt(other) || lead.level() != other.level()) {
            end(server, pair, false);
            return;
        }
        if (pair.ticks >= SCENE) {
            end(server, pair, true);
            return;
        }
        place(lead, other, pair);
        pair.ticks++;
    }

    private static void place(Mob lead, Mob other, Pair pair) {
        float pulse = Mth.sin(pair.ticks * 0.48F) * (pair.quad ? 0.12F : 0.07F);
        lead.noPhysics = true;
        lead.setDeltaMovement(Vec3.ZERO);
        lead.getNavigation().stop();
        if (pair.kind == Kind.VILLAGER) {
            float rad = pair.ayaw * Mth.DEG_TO_RAD;
            double gap = 0.74 + pulse;
            double x = pair.ax - Mth.sin(rad) * gap;
            double z = pair.az + Mth.cos(rad) * gap;
            lead.moveTo(x, pair.ay, z, pair.ayaw + 180.0F, 0.0F);
            lead.setYBodyRot(pair.ayaw + 180.0F);
            lead.setYHeadRot(pair.ayaw + 180.0F);
            stand(lead);
            hold(other, pair.ax, pair.ay, pair.az, pair.ayaw);
            bare(lead);
            return;
        }
        other.noPhysics = true;
        other.setDeltaMovement(Vec3.ZERO);
        other.getNavigation().stop();
        if (pair.kind == Kind.TRIBE) {
            double dx = lead.getX() - pair.ax;
            double dz = lead.getZ() - pair.az;
            if (dx * dx + dz * dz < 0.04) {
                dx = -Mth.sin(pair.ayaw * Mth.DEG_TO_RAD);
                dz = Mth.cos(pair.ayaw * Mth.DEG_TO_RAD);
            }
            double len = Math.sqrt(dx * dx + dz * dz);
            dx /= len;
            dz /= len;
            double gap = 0.46 + pulse;
            float leadYaw = yawToward(-dx, -dz);
            float otherYaw = yawToward(dx, dz);
            lead.moveTo(pair.ax + dx * gap, pair.ay, pair.az + dz * gap, leadYaw, 0.0F);
            other.moveTo(pair.ax - dx * gap, pair.ay, pair.az - dz * gap, otherYaw, 0.0F);
            face(lead, leadYaw);
            face(other, otherYaw);
            tribe(lead);
            tribe(other);
        } else {
            float rad = pair.ayaw * Mth.DEG_TO_RAD;
            double gap = (pair.quad ? 1.65 : 0.92) + pulse;
            lead.moveTo(pair.ax + Mth.sin(rad) * gap, pair.ay, pair.az - Mth.cos(rad) * gap, pair.ayaw, 0.0F);
            face(lead, pair.ayaw);
            hold(other, pair.ax, pair.ay, pair.az, pair.ayaw);
            strap(lead);
            taken(other);
        }
        bare(lead);
        bare(other);
        if (pair.ticks % 20 == 0 && lead.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.HEART, pair.ax, pair.ay + lead.getBbHeight() * 0.55, pair.az, 2, 0.2, 0.15, 0.2, 0.0);
        }
    }

    private static void end(MinecraftServer server, Pair pair, boolean finished) {
        BY_MOB.remove(pair.lead);
        if (pair.other != null) BY_MOB.remove(pair.other);
        Mob lead = find(server, pair.lead);
        Mob other = partner(server, pair);
        long when = server.overworld().getGameTime() + (finished ? COOLDOWN : COOLDOWN / 5);
        release(lead, pair, when);
        if (pair.kind != Kind.VILLAGER) release(other, pair, when);
        else if (other != null) {
            other.setDeltaMovement(Vec3.ZERO);
            if (pair.frozeVillager) other.setNoAi(false);
        }
    }

    private static void release(@Nullable Mob mob, Pair pair, long when) {
        if (mob == null) return;
        mob.noPhysics = false;
        mob.setDeltaMovement(Vec3.ZERO);
        if (posed(mob, pair)) clearToIdle(mob);
        cover(mob);
        if (mob.level() instanceof ServerLevel level) mob.getPersistentData().putLong(NEXT, when);
    }

    private static boolean posed(Mob mob, Pair pair) {
        return switch (pair.kind) {
            case TRIBE -> Nsfw.tribing(mob);
            case MOUNT, BULLS -> Nsfw.mounting(mob) || Nsfw.presented(mob);
            case VILLAGER -> Nsfw.standing(mob);
        };
    }

    @Nullable
    private static Mob partner(MinecraftServer server, Pair pair) {
        if (pair.villager != null) return find(server, pair.villager);
        return find(server, pair.other);
    }

    @Nullable
    private static Mob nearestGirl(ServerLevel level, Mob mob) {
        Mob best = null;
        double bestDist = RANGE * RANGE;
        for (Mob other : level.getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(RANGE), o -> o != mob && girlReady(level, o) && !active(o) && sameStyle(mob, o))) {
            double dist = other.distanceToSqr(mob);
            if (dist < bestDist) {
                bestDist = dist;
                best = other;
            }
        }
        return best;
    }

    /** Two taurs, two on two legs, or two of the same serpent, mermaid, spider or slime. */
    private static boolean sameStyle(Mob a, Mob b) {
        if (Nsfw.quadruped(a) && Nsfw.quadruped(b)) return true;
        if (Nsfw.bipedal(a) && Nsfw.bipedal(b)) return true;
        if (!(a instanceof AbstractMonsterGirl ga) || !(b instanceof AbstractMonsterGirl gb)) return false;
        if (ga.getSpecies().family() != gb.getSpecies().family()) return false;
        GirlSpecies.Family family = ga.getSpecies().family();
        return family == GirlSpecies.Family.LAMIA || family == GirlSpecies.Family.MERFOLK
                || family == GirlSpecies.Family.ARACHNE || family == GirlSpecies.Family.SLIME;
    }

    private static boolean girlReady(ServerLevel level, Mob mob) {
        if (!(mob instanceof Bonded bonded) || !bonded.canShareBed() || bonded.isOrderedToSit()) return false;
        if (!MatingSeason.inSeason(mob) || !befriended(level, bonded)) return false;
        if (level.getGameTime() < mob.getPersistentData().getLong(NEXT)) return false;
        return mob.isAlive() && !mob.isPassenger() && !mob.isSleeping() && mob.getTarget() == null
                && !mob.getPersistentData().getBoolean(Dreaming.COPY) && !busy(mob);
    }

    private static boolean bullReady(Minotaur bull) {
        if (!(bull.level() instanceof ServerLevel level) || level.getGameTime() < bull.getPersistentData().getLong(NEXT)) return false;
        return bull.isAlive() && bull.isMale() && !bull.isBaby() && bull.getSpecies().height() >= Bonded.CHILDLIKE_HEIGHT
                && !bull.isPassenger() && !bull.isSleeping() && bull.getTarget() == null && !bull.isOrderedToSit()
                && !bull.getPersistentData().getBoolean(Dreaming.COPY);
    }

    private static boolean villagerReady(Villager villager) {
        return villager.isAlive() && !villager.isBaby() && !villager.isSleeping() && !villager.isPassenger();
    }

    private static boolean befriended(ServerLevel level, Bonded bonded) {
        for (ServerPlayer player : level.players()) {
            if (bonded.getAffection(player) >= Orders.AFFECTION) return true;
        }
        return false;
    }

    private static boolean busy(Mob mob) {
        if (WildEncounter.active(mob) || Breastfeed.active(mob) || Horniness.active(mob) || Group.active(mob)
                || Bathing.active(mob) || Dreaming.active(mob) || MorningOral.posing(mob) || MorningOral.pending(mob)) return true;
        if (Nsfw.kneeling(mob) || Nsfw.sittingBack(mob) || Nsfw.coupling(mob) || Nsfw.facing(mob)) return true;
        if (mob instanceof AbstractMonsterGirl girl) {
            AbstractMonsterGirl.Action action = girl.getAction();
            return action == AbstractMonsterGirl.Action.FEED || action == AbstractMonsterGirl.Action.LABOR
                    || action == AbstractMonsterGirl.Action.ORAL || action == AbstractMonsterGirl.Action.MILK;
        }
        if (mob instanceof AbstractHarpy harpy) {
            AbstractHarpy.Action action = harpy.getAction();
            return action == AbstractHarpy.Action.FEED || action == AbstractHarpy.Action.ORAL || action == AbstractHarpy.Action.LAYING;
        }
        if (mob instanceof AbstractAnuran anuran) {
            AbstractAnuran.Action action = anuran.getAction();
            return action == AbstractAnuran.Action.FEED || action == AbstractAnuran.Action.ORAL || action == AbstractAnuran.Action.LAYING;
        }
        return false;
    }

    private static boolean hurt(Mob mob) {
        return mob.getLastHurtByMob() instanceof ServerPlayer && mob.hurtTime > 0;
    }

    private static boolean orphan(Mob mob) {
        return Nsfw.tribing(mob) || Nsfw.mounting(mob) || Nsfw.presented(mob);
    }

    private static void tell(Mob lead, Mob other, Pair pair) {
        if (!(lead.level() instanceof ServerLevel level)) return;
        String key = switch (pair.kind) {
            case TRIBE -> "message.harmontide.npc.tribe";
            case MOUNT -> "message.harmontide.npc.mount";
            case BULLS -> "message.harmontide.npc.bulls";
            case VILLAGER -> "message.harmontide.npc.villager";
        };
        Component line = pair.kind == Kind.VILLAGER
                ? Component.translatable(key, lead.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE)
                : Component.translatable(key, lead.getDisplayName(), other.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE);
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(lead) <= 32 * 32) player.displayClientMessage(line, false);
        }
    }

    private static void hold(Mob mob, double x, double y, double z, float yaw) {
        mob.setDeltaMovement(Vec3.ZERO);
        mob.moveTo(x, y, z, yaw, 0.0F);
        face(mob, yaw);
    }

    private static void face(Mob mob, float yaw) {
        mob.setYRot(yaw);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
    }

    private static float yawToward(double dx, double dz) {
        return (float) (Mth.atan2(-dx, dz) * Mth.RAD_TO_DEG);
    }

    private static void stand(Mob mob) {
        if (mob instanceof AbstractMonsterGirl girl) girl.setAction(AbstractMonsterGirl.Action.STAND);
        else if (mob instanceof AbstractHarpy harpy) harpy.setAction(AbstractHarpy.Action.STAND);
        else if (mob instanceof AbstractAnuran anuran) anuran.setAction(AbstractAnuran.Action.STAND);
    }

    private static void tribe(Mob mob) {
        if (mob instanceof AbstractMonsterGirl girl) girl.setAction(AbstractMonsterGirl.Action.TRIBE);
        else if (mob instanceof AbstractHarpy harpy) harpy.setAction(AbstractHarpy.Action.TRIBE);
        else if (mob instanceof AbstractAnuran anuran) anuran.setAction(AbstractAnuran.Action.TRIBE);
    }

    private static void strap(Mob mob) {
        if (mob instanceof AbstractMonsterGirl girl) girl.setAction(AbstractMonsterGirl.Action.STRAP);
        else if (mob instanceof AbstractHarpy harpy) harpy.setAction(AbstractHarpy.Action.STRAP);
        else if (mob instanceof AbstractAnuran anuran) anuran.setAction(AbstractAnuran.Action.STRAP);
    }

    private static void taken(Mob mob) {
        if (mob instanceof AbstractMonsterGirl girl) girl.setAction(AbstractMonsterGirl.Action.TAKEN);
        else if (mob instanceof AbstractHarpy harpy) harpy.setAction(AbstractHarpy.Action.TAKEN);
        else if (mob instanceof AbstractAnuran anuran) anuran.setAction(AbstractAnuran.Action.TAKEN);
    }

    private static void clearToIdle(Mob mob) {
        boolean sit = mob instanceof Bonded bonded && bonded.isOrderedToSit();
        if (mob instanceof AbstractMonsterGirl girl) girl.setAction(sit ? AbstractMonsterGirl.Action.SITTING : AbstractMonsterGirl.Action.NONE);
        else if (mob instanceof AbstractHarpy harpy) harpy.setAction(sit ? AbstractHarpy.Action.SITTING : AbstractHarpy.Action.NONE);
        else if (mob instanceof AbstractAnuran anuran) anuran.setAction(sit ? AbstractAnuran.Action.SITTING : AbstractAnuran.Action.NONE);
    }

    private static void bare(Mob mob) {
        if (mob instanceof AbstractMonsterGirl girl) girl.bareFor(20);
        else if (mob instanceof AbstractHarpy harpy) harpy.bareFor(20);
        else if (mob instanceof AbstractAnuran anuran) anuran.bareFor(20);
    }

    private static void cover(Mob mob) {
        if (mob instanceof AbstractMonsterGirl girl) girl.dressAfter(30);
        else if (mob instanceof AbstractHarpy harpy) harpy.dressAfter(30);
        else if (mob instanceof AbstractAnuran anuran) anuran.dressAfter(30);
    }

    @Nullable
    private static Mob find(MinecraftServer server, @Nullable UUID id) {
        if (id == null) return null;
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(id) instanceof Mob mob) return mob;
        }
        return null;
    }
}
