package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * A wild grown girl who is not yet close enough to share a bed may catch a player while the adult option is on.
 * She comes over, tackles him to the ground, and only then uses the same poses as the bed ({@link Nsfw#rock}).
 * Young, childlike height and the men stay out.
 * A grown daughter still needs a condom, and that does not conceive.
 * <p>
 * Once she is done she is fonder of him ({@link #AFFECTION}, enough that a hostile one stops hunting him), she may
 * conceive at the usual odds, and he is restored: a little food, a short regeneration, and two extra hearts for a while.
 * About one try in {@link #ODDS} every {@link #SCAN} ticks while she can see him inside {@link #RANGE}. After one
 * finishes, neither of them starts another for {@link #COOLDOWN} ticks. Striking her makes her let go, with none of that.
 */
public final class WildEncounter {
    public static final int RANGE = 12, SCAN = 200, ODDS = 8, APPROACH = 160, TACKLE = 20, SCENE = 160, COOLDOWN = 6000, AFFECTION = 20;
    /** Close enough to take him. The tackle lands him on this tick of {@link #TACKLE} (the pounce's landing). */
    private static final double CLOSE = 2.5;
    private static final int IMPACT = 11;
    private static final int FOOD = 6;
    private static final String NEXT = "WildNext", SAID = "WildCondomSaid";

    private static final Map<Mob, Session> SESSIONS = new WeakHashMap<>();
    private static ServerLevel lastLevel;
    private static long lastTime = Long.MIN_VALUE;

    private static final class Session {
        final UUID player;
        int ticks;
        /** She has reached him and the tackle has started. */
        boolean caught;
        /** He is on the ground, and the bed pose may play. */
        boolean sex;
        boolean grounded;
        double x, y, z;
        float yaw;

        Session(UUID player) {
            this.player = player;
        }
    }

    private WildEncounter() {}

    /** She is coming for him, or already on him. */
    public static boolean active(Mob mob) {
        return SESSIONS.containsKey(mob);
    }

    /** Once per game tick, from the player tick. The rolls stay off on the game-test server; {@link #start} still works. */
    public static void tick(ServerLevel level) {
        long now = level.getGameTime();
        if (level == lastLevel && now == lastTime) return;
        lastLevel = level;
        lastTime = now;
        step(level);
    }

    /** One beat. The game test calls this on its own. */
    public static void step(ServerLevel level) {
        List<Mob> mobs = new ArrayList<>(SESSIONS.keySet());
        for (Mob mob : mobs) {
            Session session = SESSIONS.get(mob);
            if (session == null) continue;
            Player found = level.getPlayerByUUID(session.player);
            if (!(found instanceof ServerPlayer player) || !player.isAlive() || !mob.isAlive() || mob.level() != level
                    || player.isSpectator() || player.isCreative() || player.isSleeping()) {
                end(level, mob, found instanceof ServerPlayer sp ? sp : null, false);
                continue;
            }
            if (mob.getLastHurtByMob() == player && mob.hurtTime > 0) {
                end(level, mob, player, false);
                continue;
            }
            if (!session.caught) {
                session.ticks++;
                if (player.distanceToSqr(mob) > 24.0 * 24.0 || session.ticks > APPROACH) {
                    end(level, mob, player, false);
                    continue;
                }
                if (player.distanceToSqr(mob) > CLOSE * CLOSE) {
                    mob.getLookControl().setLookAt(player);
                    if (mob.tickCount % 10 == 0) mob.getNavigation().moveTo(player, 1.15);
                    continue;
                }
                begin(level, mob, player, session);
            }
            if (!session.sex) {
                if (player.distanceToSqr(mob) > 24.0 * 24.0) {
                    end(level, mob, player, false);
                    continue;
                }
                session.ticks++;
                tackle(level, mob, player, session);
                if (session.ticks < TACKLE) continue;
                session.sex = true;
                session.ticks = 0;
                continue;
            }
            session.ticks++;
            mob.setTarget(null);
            mob.getNavigation().stop();
            hold(player, session);
            if (!Nsfw.rock(level, mob, player, player.position(), player.getYRot(), null)) {
                end(level, mob, player, false);
                continue;
            }
            if (session.ticks >= SCENE) end(level, mob, player, true);
        }
        if (!Nsfw.on() || net.neoforged.neoforge.gametest.GameTestHooks.isGametestServer()) return;
        if (level.getGameTime() % SCAN != 0) return;
        for (ServerPlayer player : level.players()) consider(level, player);
    }

    /**
     * She goes for this player. False if the option is off, she is not a grown partner, a condom is required and missing,
     * or either of them is already in the middle of one.
     */
    public static boolean start(ServerLevel level, Mob mob, ServerPlayer player) {
        if (!eligible(mob, player) || busy(mob) || playerBusy(player)) return false;
        SESSIONS.put(mob, new Session(player.getUUID()));
        mob.setTarget(null);
        mob.getNavigation().stop();
        return true;
    }

    private static void consider(ServerLevel level, ServerPlayer player) {
        if (player.isSpectator() || player.isCreative() || !player.isAlive() || player.isSleeping()) return;
        if (level.getGameTime() < player.getPersistentData().getLong(NEXT) || playerBusy(player)) return;
        Mob best = null;
        double bestDist = RANGE * (double) RANGE;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(RANGE), m -> m instanceof Bonded)) {
            if (SESSIONS.containsKey(mob) || level.getGameTime() < mob.getPersistentData().getLong(NEXT)) continue;
            if (!mob.hasLineOfSight(player) || !open(mob, player) || busy(mob)) continue;
            if (!Condom.allows(mob, player)) {
                if (Condom.grownOffspring(mob, player)) refuse(level, mob, player);
                continue;
            }
            double dist = mob.distanceToSqr(player);
            if (dist < bestDist) {
                bestDist = dist;
                best = mob;
            }
        }
        if (best != null && best.getRandom().nextInt(ODDS) == 0) start(level, best, player);
    }

    /** Grown, not yet a bed partner, and not his wife. The condom is separate. */
    private static boolean open(Mob mob, Player player) {
        if (!Nsfw.adult(mob, player) || player.isSleeping()) return false;
        if (!(mob instanceof Bonded bonded) || bonded.getAffection(player) >= Bonded.BED_AFFECTION) return false;
        if (Marriage.married(mob, player)) return false;
        if (mob.getTarget() != null && mob.getTarget() != player) return false;
        return !com.linkyoshi90.harmontide.entity.Orders.holdsPlace(mob);
    }

    private static boolean eligible(Mob mob, Player player) {
        return Nsfw.on() && player instanceof ServerPlayer && !player.isSpectator() && !player.isCreative() && player.isAlive()
                && open(mob, player) && Condom.allows(mob, player);
    }

    private static boolean busy(Mob mob) {
        if (MorningOral.pending(mob) || MorningOral.posing(mob) || Breastfeed.active(mob) || Encounters.of(mob) != null) return true;
        if (mob instanceof Bonded bonded && bonded.getGestation().getSharingWith() != null) return true;
        if (mob instanceof AbstractMonsterGirl girl) {
            if (Courtship.snaring(girl)) return true;
            AbstractMonsterGirl.Action action = girl.getAction();
            return action == AbstractMonsterGirl.Action.LABOR || action == AbstractMonsterGirl.Action.FEED
                    || action == AbstractMonsterGirl.Action.NURSE || action == AbstractMonsterGirl.Action.MILK
                    || action == AbstractMonsterGirl.Action.ORAL || action == AbstractMonsterGirl.Action.KNEEL
                    || action == AbstractMonsterGirl.Action.SITBACK || action == AbstractMonsterGirl.Action.COUPLE;
        }
        return false;
    }

    private static boolean playerBusy(Player player) {
        UUID id = player.getUUID();
        for (Session session : SESSIONS.values()) if (id.equals(session.player)) return true;
        return false;
    }

    private static void begin(ServerLevel level, Mob mob, ServerPlayer player, Session session) {
        session.caught = true;
        session.ticks = 0;
        double dx = player.getX() - mob.getX();
        double dz = player.getZ() - mob.getZ();
        session.yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        if (mob instanceof Bonded bonded) bonded.blushFor(TACKLE + SCENE + 40);
        player.displayClientMessage(Component.translatable("message.harmontide.wild_encounter", mob.getDisplayName())
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.2, mob.getZ(), 4, 0.2, 0.1, 0.2, 0.0);
    }

    /** She lunges. At {@link #IMPACT} he goes down, and the bed pose waits until the lunge is over. */
    private static void tackle(ServerLevel level, Mob mob, ServerPlayer player, Session session) {
        mob.setTarget(null);
        mob.getNavigation().stop();
        mob.setDeltaMovement(Vec3.ZERO);
        float rad = session.yaw * Mth.DEG_TO_RAD;
        double dist = session.grounded ? 0.15 : 0.8 * (1.0 - session.ticks / (double) TACKLE);
        double px = session.grounded ? session.x : player.getX();
        double py = session.grounded ? session.y : player.getY();
        double pz = session.grounded ? session.z : player.getZ();
        mob.moveTo(px + Mth.sin(rad) * dist, py, pz - Mth.cos(rad) * dist, session.yaw, 0.0F);
        mob.setYBodyRot(session.yaw);
        mob.setYHeadRot(session.yaw);
        if (mob instanceof AbstractMonsterGirl girl) girl.setAction(AbstractMonsterGirl.Action.POUNCE);
        if (!session.grounded && session.ticks >= IMPACT) pin(level, player, session);
        if (session.grounded) hold(player, session);
    }

    /** Flat on the ground, held on the spot she landed him. */
    private static void pin(ServerLevel level, ServerPlayer player, Session session) {
        player.stopRiding();
        session.x = player.getX();
        session.y = groundY(level, player);
        session.z = player.getZ();
        session.grounded = true;
        hold(player, session);
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_SMALL_FALL, SoundSource.PLAYERS, 0.8F, 0.9F);
        level.sendParticles(ParticleTypes.CLOUD, session.x, session.y + 0.1, session.z, 8, 0.3, 0.05, 0.3, 0.02);
    }

    private static void hold(ServerPlayer player, Session session) {
        player.setSwimming(true);
        player.setPose(Pose.SWIMMING);
        player.setSprinting(false);
        player.moveTo(session.x, session.y, session.z, player.getYRot(), player.getXRot());
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 6, false, false, true));
    }

    private static double groundY(ServerLevel level, Player player) {
        if (player.onGround()) return player.getY();
        int x = player.getBlockX();
        int z = player.getBlockZ();
        int min = Math.max(level.getMinBuildHeight(), player.getBlockY() - 8);
        for (int at = player.getBlockY(); at > min; at--) {
            if (level.getBlockState(new BlockPos(x, at - 1, z)).blocksMotion()
                    && !level.getBlockState(new BlockPos(x, at, z)).blocksMotion()) return at;
        }
        return player.getY();
    }

    private static void release(ServerPlayer player) {
        player.setSwimming(false);
        if (player.getPose() == Pose.SWIMMING) player.setPose(Pose.STANDING);
    }

    private static void refuse(ServerLevel level, Mob mob, ServerPlayer player) {
        long now = level.getGameTime();
        if (now < mob.getPersistentData().getLong(SAID)) return;
        mob.getPersistentData().putLong(SAID, now + 1200);
        player.displayClientMessage(Component.translatable("message.harmontide.no_condom", mob.getDisplayName())
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
    }

    private static void end(ServerLevel level, Mob mob, @Nullable ServerPlayer player, boolean reward) {
        Session session = SESSIONS.remove(mob);
        if (session == null) return;
        stand(mob);
        if (player != null) release(player);
        long wait = session.caught ? COOLDOWN : COOLDOWN / 5;
        long until = level.getGameTime() + wait;
        mob.getPersistentData().putLong(NEXT, until);
        if (player != null) player.getPersistentData().putLong(NEXT, until);
        if (!reward || player == null || !player.isAlive()) {
            if (player != null && session.caught) {
                player.displayClientMessage(Component.translatable("message.harmontide.wild_encounter.broke", mob.getDisplayName())
                        .withStyle(ChatFormatting.LIGHT_PURPLE), false);
            }
            return;
        }
        if (mob instanceof Bonded bonded) {
            bonded.addAffection(player, AFFECTION);
            bonded.blushFor(200);
        }
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 0));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 0));
        player.getFoodData().eat(FOOD, 0.6F);
        boolean daughter = Condom.grownOffspring(mob, player);
        if (daughter && !Condom.take(player)) Condom.mention(level, mob, player);
        else if (!daughter && mob.getRandom().nextFloat() < Missing.conceptionChance(mob, player)) {
            ((Bonded) mob).getGestation().conceive(player.getUUID());
            player.displayClientMessage(Component.translatable("message.harmontide.wild_encounter.expecting", mob.getDisplayName())
                    .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getY() + mob.getBbHeight() + 0.3, mob.getZ(), 6, 0.3, 0.2, 0.3, 0.0);
        player.displayClientMessage(Component.translatable("message.harmontide.wild_encounter.done", mob.getDisplayName())
                .withStyle(ChatFormatting.LIGHT_PURPLE), false);
    }

    /** Back on her feet. The bed pose would leave her lying down, and the pounce would leave her lunging. */
    private static void stand(Mob mob) {
        if (mob instanceof Bonded bonded) bonded.leaveBed();
        if (mob instanceof AbstractMonsterGirl girl && girl.getAction() == AbstractMonsterGirl.Action.POUNCE) {
            girl.setAction(AbstractMonsterGirl.Action.NONE);
        }
    }
}
