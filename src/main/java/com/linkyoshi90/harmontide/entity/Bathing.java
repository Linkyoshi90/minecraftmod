package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
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
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A soak in a hot spring can become sex. She has to have been in the water with him a little while, and then only
 * sometimes. The pose is the standing one. Stepping out of the spring stops it. A grown daughter still needs a condom,
 * and that time does not conceive. The young, the men, and anyone of childlike height stay out. The rolls stay off
 * on the game-test server.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Bathing {
    public static final int SCENE = 160;
    /** Together in the water this long before it can turn. */
    private static final int WARM = 200;
    private static final int COOLDOWN = 6000;
    private static final int ODDS = 3;
    private static final double GAP = 0.62, ROCK = 0.10;
    private static final String WITH = "SpringWith", WITH_AT = "SpringWithAt", NEXT = "SpringNext";
    private static final Map<UUID, Scene> SCENES = new HashMap<>();

    private static final class Scene {
        final UUID player;
        final Vec3 anchor;
        final Vec3 out;
        final float girlYaw;
        final float hisYaw;
        int ticks;

        Scene(UUID player, Vec3 anchor, Vec3 out, float girlYaw, float hisYaw) {
            this.player = player;
            this.anchor = anchor;
            this.out = out;
            this.girlYaw = girlYaw;
            this.hisYaw = hisYaw;
        }
    }

    private Bathing() {}

    public static boolean active(Mob mob) {
        return mob != null && SCENES.containsKey(mob.getUUID());
    }

    @SubscribeEvent
    public static void onServer(ServerTickEvent.Post event) {
        tick(event.getServer());
    }

    /** One beat of every spring scene. */
    public static void tick(MinecraftServer server) {
        if (SCENES.isEmpty()) return;
        for (UUID id : new ArrayList<>(SCENES.keySet())) {
            Scene scene = SCENES.get(id);
            Mob mob = find(server, id);
            ServerPlayer player = scene == null ? null : player(server, scene.player);
            if (scene == null || mob == null || player == null || !mob.isAlive() || !player.isAlive()
                    || mob.level() != player.level() || player.isSpectator() || player.isSleeping()
                    || !HotSprings.inSpring(mob) || !HotSprings.inSpring(player)) {
                SCENES.remove(id);
                end(mob, player, false);
                continue;
            }
            if (mob.getLastHurtByMob() == player && mob.hurtTime > 0) {
                SCENES.remove(id);
                end(mob, player, false);
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

    /**
     * Called while she is already in the spring with him. After {@link #WARM} ticks together, one chance in three
     * every ten seconds. True if it turned into sex.
     */
    public static boolean consider(ServerLevel level, Mob girl, Player player) {
        if (GameTestHooks.isGametestServer()) return false;
        if (!(player instanceof ServerPlayer sleeper) || !ready(girl, sleeper) || taken(sleeper)) return false;
        long now = level.getGameTime();
        long last = girl.getPersistentData().getLong(WITH_AT);
        int with = now - last > 80 ? 0 : girl.getPersistentData().getInt(WITH);
        with += HotSprings.EVERY;
        girl.getPersistentData().putInt(WITH, with);
        girl.getPersistentData().putLong(WITH_AT, now);
        if (with < WARM || now < girl.getPersistentData().getLong(NEXT)) return false;
        if ((with - WARM) % WARM != 0) return false;
        if (girl.getRandom().nextInt(ODDS) != 0) return false;
        begin(level, girl, sleeper);
        return true;
    }

    /** Starts it without the wait or the roll. Still requires the spring, and that she is grown and close. */
    public static boolean start(ServerLevel level, Mob girl, ServerPlayer player) {
        if (!ready(girl, player) || taken(player)) return false;
        begin(level, girl, player);
        return true;
    }

    private static void begin(ServerLevel level, Mob mob, ServerPlayer player) {
        Vec3 flat = new Vec3(mob.getX() - player.getX(), 0, mob.getZ() - player.getZ());
        if (flat.lengthSqr() < 0.04) flat = Vec3.directionFromRotation(0, player.getYRot());
        Vec3 out = flat.normalize();
        float girlYaw = (float) Math.toDegrees(Mth.atan2(out.x, -out.z));
        float hisYaw = (float) Math.toDegrees(Mth.atan2(-out.x, out.z));
        SCENES.put(mob.getUUID(), new Scene(player.getUUID(), new Vec3(player.getX(), player.getY(), player.getZ()), out, girlYaw, hisYaw));
        mob.getPersistentData().putLong(NEXT, level.getGameTime() + SCENE + COOLDOWN);
        stand(mob);
        bare(mob);
        if (mob instanceof Bonded bonded) bonded.blushFor(SCENE + 40);
        player.displayClientMessage(Component.translatable("message.harmontide.spring.sex", mob.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY(), mob.getZ(), 5, 0.25, 0.15, 0.25, 0.0);
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
        bare(mob);
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
            cover(mob);
        }
        if (player != null) {
            MobEffectInstance slow = player.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            if (slow != null && slow.getAmplifier() == 3 && slow.getDuration() <= 15) player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
        if (!finished || mob == null || player == null || !(mob.level() instanceof ServerLevel level)) return;
        player.displayClientMessage(Component.translatable("message.harmontide.spring.done", mob.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        if (mob instanceof Bonded bonded) bonded.addAffection(player, 6);
        if (Condom.grownOffspring(mob, player)) {
            if (!Condom.take(player)) Condom.mention(level, mob, player);
            return;
        }
        if (!(mob instanceof Bonded bonded) || !bonded.getGestation().canConceive() || !Nsfw.bearsYoung(mob)) return;
        if (mob.getRandom().nextFloat() >= Missing.conceptionChance(mob, player)) return;
        bonded.getGestation().conceive(player.getUUID());
        player.displayClientMessage(Component.translatable("message.harmontide.happy_news", mob.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE), false);
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getY() + mob.getBbHeight() + 0.3, mob.getZ(), 5, 0.3, 0.2, 0.3, 0.0);
    }

    private static boolean ready(Mob mob, ServerPlayer player) {
        return player.isAlive() && !player.isSpectator() && !player.isSleeping() && !mob.isPassenger() && !player.isPassenger()
                && HotSprings.inSpring(mob) && HotSprings.inSpring(player) && mob.distanceToSqr(player) <= HotSprings.NEAR * HotSprings.NEAR
                && !HandMilking.isOpen(player) && Nsfw.close(mob, player) && !busy(mob);
    }

    private static boolean busy(Mob mob) {
        if (mob.isSleeping() || mob.getTarget() != null || mob.isPassenger() || active(mob)) return true;
        if (WildEncounter.active(mob) || Breastfeed.active(mob) || Horniness.active(mob) || Group.active(mob) || Dreaming.active(mob) || NpcSex.active(mob)) return true;
        if (MorningOral.posing(mob) || MorningOral.pending(mob)) return true;
        if (Nsfw.kneeling(mob) || Nsfw.sittingBack(mob) || Nsfw.coupling(mob) || Nsfw.standing(mob) || Nsfw.facing(mob)) return true;
        if (mob instanceof Bonded bonded && bonded.getGestation().getSharingWith() != null) return true;
        if (mob instanceof AbstractMonsterGirl girl) {
            AbstractMonsterGirl.Action action = girl.getAction();
            return action == AbstractMonsterGirl.Action.FEED || action == AbstractMonsterGirl.Action.LABOR
                    || action == AbstractMonsterGirl.Action.ORAL || action == AbstractMonsterGirl.Action.NURSE;
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

    private static boolean taken(ServerPlayer player) {
        UUID id = player.getUUID();
        for (Scene scene : SCENES.values()) if (id.equals(scene.player)) return true;
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

    /** Topless in the water. A bath towel comes off for the scene. */
    private static void bare(Mob mob) {
        if (mob instanceof AbstractMonsterGirl girl) {
            girl.bareFor(20);
            girl.setFlag(AbstractMonsterGirl.FLAG_TOWEL, false);
        } else if (mob instanceof AbstractHarpy harpy) harpy.bareFor(20);
        else if (mob instanceof AbstractAnuran anuran) anuran.bareFor(20);
    }

    /** Back in the towel if she has one, otherwise her top returns. */
    private static void cover(Mob mob) {
        if (mob instanceof AbstractMonsterGirl girl) {
            girl.dressAfter(0);
            girl.towelFor(400);
        } else if (mob instanceof AbstractHarpy harpy) harpy.dressAfter(40);
        else if (mob instanceof AbstractAnuran anuran) anuran.dressAfter(40);
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
        return server.getPlayerList().getPlayer(id);
    }
}
