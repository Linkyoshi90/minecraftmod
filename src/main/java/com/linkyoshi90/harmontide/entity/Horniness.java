package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.network.ModPayloads;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * In her season, with the adult option on, a close grown friend asks to be taken standing. Yes holds the two of them
 * together for a short press ({@link #SCENE}). No, walking off, or letting the question sit, is a no. A grown daughter
 * still needs a condom, and that time does not conceive. The young, the men, and anyone of childlike height never ask.
 * The rolls stay off on the game-test server.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Horniness {
    /** How long she waits on the question, and how long the press lasts. */
    public static final int OFFER_TICKS = 400, SCENE = 160;
    /** Quiet after a no, and after she has had him. */
    public static final int COOLDOWN_NO = 3600, COOLDOWN_YES = 6000;
    private static final double ASK = 4.5, LEAVE = 8.0, GAP = 0.62, ROCK = 0.10;
    private static final String NEXT = "StandNext";
    private static final Map<UUID, Offer> OFFERS = new HashMap<>();
    private static final Map<UUID, UUID> ASKED = new HashMap<>();
    private static final Map<UUID, Scene> SCENES = new HashMap<>();

    private Horniness() {}

    public static boolean active(Mob mob) {
        return mob != null && (OFFERS.containsKey(mob.getUUID()) || SCENES.containsKey(mob.getUUID()));
    }

    @SubscribeEvent
    public static void onEntity(EntityTickEvent.Post event) {
        if (GameTestHooks.isGametestServer()) return;
        if (!(event.getEntity() instanceof Mob mob) || !(mob instanceof Bonded) || mob.tickCount % 100 != 0) return;
        if (!(mob.level() instanceof ServerLevel level) || !Nsfw.on() || !MatingSeason.inSeason(mob)) return;
        consider(level, mob);
    }

    @SubscribeEvent
    public static void onServer(ServerTickEvent.Post event) {
        tick(event.getServer());
    }

    /** One beat of every open question and every press. */
    public static void tick(MinecraftServer server) {
        if (!OFFERS.isEmpty()) {
            for (UUID id : new ArrayList<>(OFFERS.keySet())) {
                Offer offer = OFFERS.get(id);
                Mob mob = find(server, id);
                ServerPlayer player = offer == null ? null : player(server, offer.player);
                if (offer == null || mob == null || player == null || !mob.isAlive() || !player.isAlive() || mob.level() != player.level()) {
                    dropOffer(id, mob, player, false);
                    continue;
                }
                if (player.distanceTo(mob) > LEAVE || !ready(mob, player)) {
                    dropOffer(id, mob, player, true);
                    continue;
                }
                mob.getNavigation().stop();
                mob.getLookControl().setLookAt(player);
                if (++offer.ticks >= OFFER_TICKS) dropOffer(id, mob, player, true);
            }
        }
        if (SCENES.isEmpty()) return;
        for (UUID id : new ArrayList<>(SCENES.keySet())) {
            Scene scene = SCENES.get(id);
            Mob mob = find(server, id);
            ServerPlayer player = scene == null ? null : player(server, scene.player);
            if (scene == null || mob == null || player == null || !mob.isAlive() || !player.isAlive() || mob.level() != player.level()
                    || player.isSpectator() || player.isSleeping()) {
                end(mob, player, false);
                SCENES.remove(id);
                continue;
            }
            if (mob.getLastHurtByMob() == player && mob.hurtTime > 0) {
                end(mob, player, false);
                SCENES.remove(id);
                continue;
            }
            if (scene.ticks >= SCENE) {
                SCENES.remove(id);
                end(mob, player, true);
                continue;
            }
            pose(player.serverLevel(), mob, player, scene);
            scene.ticks++;
        }
    }

    /** She asks, if she is close enough and free to. True if the question opened. */
    public static boolean consider(ServerLevel level, Mob mob) {
        if (active(mob) || busy(mob) || level.getGameTime() < mob.getPersistentData().getLong(NEXT)) return false;
        ServerPlayer best = null;
        for (ServerPlayer player : level.players()) {
            if (!ready(mob, player) || player.distanceTo(mob) > ASK || ASKED.containsKey(player.getUUID())) continue;
            if (best == null || ((Bonded) mob).getAffection(player) > ((Bonded) mob).getAffection(best)) best = player;
        }
        if (best == null) return false;
        Offer offer = new Offer(best.getUUID());
        OFFERS.put(mob.getUUID(), offer);
        ASKED.put(best.getUUID(), mob.getUUID());
        mob.getNavigation().stop();
        mob.getPersistentData().putLong(NEXT, level.getGameTime() + OFFER_TICKS);
        if (best.connection != null && best.connection.hasChannel(ModPayloads.OpenStand.TYPE)) {
            PacketDistributor.sendToPlayer(best, new ModPayloads.OpenStand(mob.getId()));
        }
        return true;
    }

    public static void accept(ServerPlayer player, int entityId) {
        if (!(player.level().getEntity(entityId) instanceof Mob mob)) return;
        Offer offer = OFFERS.get(mob.getUUID());
        if (offer == null || !offer.player.equals(player.getUUID())) return;
        dropOffer(mob.getUUID(), mob, player, false);
        if (!(mob.level() instanceof ServerLevel level) || !ready(mob, player) || player.distanceTo(mob) > ASK) return;
        begin(level, mob, player);
    }

    public static void decline(ServerPlayer player, int entityId) {
        if (!(player.level().getEntity(entityId) instanceof Mob mob)) return;
        Offer offer = OFFERS.get(mob.getUUID());
        if (offer == null || !offer.player.equals(player.getUUID())) return;
        dropOffer(mob.getUUID(), mob, player, true);
    }

    private static void begin(ServerLevel level, Mob mob, ServerPlayer player) {
        Vec3 flat = new Vec3(mob.getX() - player.getX(), 0, mob.getZ() - player.getZ());
        if (flat.lengthSqr() < 0.04) flat = Vec3.directionFromRotation(0, player.getYRot());
        Vec3 out = flat.normalize();
        float girlYaw = (float) Math.toDegrees(Mth.atan2(out.x, -out.z));
        float hisYaw = (float) Math.toDegrees(Mth.atan2(-out.x, out.z));
        SCENES.put(mob.getUUID(), new Scene(player.getUUID(), new Vec3(player.getX(), player.getY(), player.getZ()), out, girlYaw, hisYaw));
        mob.getPersistentData().putLong(NEXT, level.getGameTime() + SCENE + COOLDOWN_YES);
        stand(mob);
        player.displayClientMessage(Component.translatable("message.harmontide.season.stand", mob.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.2, mob.getZ(), 6, 0.3, 0.2, 0.3, 0.0);
        if (mob instanceof Bonded bonded) bonded.blushFor(SCENE + 40);
    }

    private static void pose(ServerLevel level, Mob mob, ServerPlayer player, Scene scene) {
        double reach = GAP + Math.sin(scene.ticks * 0.5) * ROCK;
        Vec3 at = scene.anchor.add(scene.out.scale(reach));
        mob.noPhysics = true;
        mob.setDeltaMovement(Vec3.ZERO);
        mob.getNavigation().stop();
        mob.moveTo(at.x, scene.anchor.y, at.z, scene.girlYaw, 0.0F);
        mob.setYHeadRot(scene.girlYaw);
        mob.yBodyRot = mob.yBodyRotO = scene.girlYaw;
        stand(mob);
        player.teleportTo(level, scene.anchor.x, scene.anchor.y, scene.anchor.z, scene.hisYaw, 0.0F);
        player.setDeltaMovement(Vec3.ZERO);
        player.setYHeadRot(scene.hisYaw);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 3, false, false, false));
        if (scene.ticks % 20 == 0) {
            level.sendParticles(ParticleTypes.HEART, at.x, scene.anchor.y + mob.getBbHeight() * 0.6, at.z, 2, 0.2, 0.15, 0.2, 0.0);
        }
    }

    private static void end(Mob mob, @Nullable ServerPlayer player, boolean finished) {
        if (mob != null) {
            mob.noPhysics = false;
            mob.setDeltaMovement(Vec3.ZERO);
            if (Nsfw.standing(mob)) clearStand(mob);
        }
        if (player != null) {
            MobEffectInstance slow = player.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            if (slow != null && slow.getAmplifier() == 3 && slow.getDuration() <= 15) player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
        if (!finished || mob == null || player == null || !(mob.level() instanceof ServerLevel level)) return;
        player.displayClientMessage(Component.translatable("message.harmontide.season.done", mob.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        if (mob instanceof Bonded bonded) bonded.addAffection(player, 6);
        boolean daughter = Condom.grownOffspring(mob, player);
        if (daughter) {
            if (!Condom.take(player)) Condom.mention(level, mob, player);
            return;
        }
        if (!(mob instanceof Bonded bonded) || !bonded.getGestation().canConceive() || !Nsfw.bearsYoung(mob)) return;
        if (mob.getRandom().nextFloat() >= Missing.conceptionChance(mob, player)) return;
        bonded.getGestation().conceive(player.getUUID());
        player.displayClientMessage(Component.translatable("message.harmontide.happy_news", mob.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getY() + mob.getBbHeight() + 0.3, mob.getZ(), 5, 0.3, 0.2, 0.3, 0.0);
    }

    private static void dropOffer(UUID girl, @Nullable Mob mob, @Nullable ServerPlayer player, boolean tell) {
        Offer offer = OFFERS.remove(girl);
        if (offer != null) ASKED.remove(offer.player);
        if (mob != null && mob.level() instanceof ServerLevel level) mob.getPersistentData().putLong(NEXT, level.getGameTime() + COOLDOWN_NO);
        if (tell && player != null && mob != null) {
            player.displayClientMessage(Component.translatable("message.harmontide.season.pass", mob.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), true);
        }
    }

    private static boolean ready(Mob mob, Player player) {
        return player != null && !player.isSpectator() && player.isAlive() && !player.isSleeping() && !mob.isPassenger() && !player.isPassenger()
                && !HandMilking.isOpen(player) && Nsfw.close(mob, player) && MatingSeason.inSeason(mob) && !busy(mob);
    }

    private static boolean busy(Mob mob) {
        if (mob.isSleeping() || mob.getTarget() != null || mob.isPassenger()) return true;
        if (WildEncounter.active(mob) || Breastfeed.active(mob) || MorningOral.posing(mob) || MorningOral.pending(mob) || Group.active(mob) || Bathing.active(mob) || Dreaming.active(mob) || NpcSex.active(mob)) return true;
        if (Nsfw.kneeling(mob) || Nsfw.sittingBack(mob) || Nsfw.coupling(mob)) return true;
        if (mob instanceof AbstractMonsterGirl girl) {
            AbstractMonsterGirl.Action action = girl.getAction();
            return action == AbstractMonsterGirl.Action.FEED || action == AbstractMonsterGirl.Action.LABOR || action == AbstractMonsterGirl.Action.ORAL;
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

    private static void stand(Mob mob) {
        if (mob instanceof AbstractMonsterGirl girl) girl.setAction(AbstractMonsterGirl.Action.STAND);
        else if (mob instanceof AbstractHarpy harpy) harpy.setAction(AbstractHarpy.Action.STAND);
        else if (mob instanceof AbstractAnuran anuran) anuran.setAction(AbstractAnuran.Action.STAND);
    }

    private static void clearStand(Mob mob) {
        boolean sit = mob instanceof Bonded bonded && bonded.isOrderedToSit();
        if (mob instanceof AbstractMonsterGirl girl) girl.setAction(sit ? AbstractMonsterGirl.Action.SITTING : AbstractMonsterGirl.Action.NONE);
        else if (mob instanceof AbstractHarpy harpy) harpy.setAction(sit ? AbstractHarpy.Action.SITTING : AbstractHarpy.Action.NONE);
        else if (mob instanceof AbstractAnuran anuran) anuran.setAction(sit ? AbstractAnuran.Action.SITTING : AbstractAnuran.Action.NONE);
    }

    @Nullable
    private static Mob find(MinecraftServer server, UUID id) {
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(id) instanceof Mob mob) return mob;
        }
        return null;
    }

    @Nullable
    private static ServerPlayer player(MinecraftServer server, UUID id) {
        Player found = server.getPlayerList().getPlayer(id);
        return found instanceof ServerPlayer player ? player : null;
    }

    private static final class Offer {
        final UUID player;
        int ticks;

        Offer(UUID player) {
            this.player = player;
        }
    }

    private static final class Scene {
        final UUID player;
        final Vec3 anchor, out;
        final float girlYaw, hisYaw;
        int ticks;

        Scene(UUID player, Vec3 anchor, Vec3 out, float girlYaw, float hisYaw) {
            this.player = player;
            this.anchor = anchor;
            this.out = out;
            this.girlYaw = girlYaw;
            this.hisYaw = hisYaw;
        }
    }
}
