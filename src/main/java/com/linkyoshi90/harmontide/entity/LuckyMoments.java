package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;
import javax.annotation.Nullable;

/**
 * Lucky moments, the slapstick of Monmusu: played for laughs, clothed, and never past a blush.
 * <ul>
 * <li>Peeking: looking at a girl who is bathing (in the soaking tub in her towel, a harpy at her bath, or standing still in water) for
 * {@link #PEEK_TICKS} ticks, she notices. Someone not yet close shrieks and splashes the player (Slowness, a push, one friendship lost);
 * a close friend sinks a little lower, blushes and teases instead. Each girl only once every {@link #PEEK_WAIT} ticks.</li>
 * <li>Trips: a friend walking right beside a player now and then stumbles into them, one chance in {@link #TRIP_CHANCE} every ten seconds
 * (in {@link #CLUMSY_CHANCE} for harpies, centaurs, lamias and slimes). Both end up flustered; a close friend takes her time getting up.</li>
 * </ul>
 * Only grown, full-size girls, never one and her own father, never constructs; behind the luckyMoments option.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class LuckyMoments {
    public static final int PEEK_TICKS = 40, PEEK_EVERY = 5, PEEK_WAIT = 2400, LINES = 4;
    public static final int TRIP_EVERY = 199, TRIP_CHANCE = 40, CLUMSY_CHANCE = 15;
    public static final double PEEK_RANGE = 12, TRIP_RANGE = 2.5;
    /** What happened when she caught them looking. */
    public static final int NOTHING = 0, SPLASHED = 1, TEASED = 2;
    private static final String PEEK_AT = "PeekAt", PEEK_FOR = "PeekFor", PEEKED = "PeekedUntil";

    private LuckyMoments() {
    }

    /** Someone these moments can happen with: grown, full size, not his daughter, not a construct. */
    public static boolean eligible(Mob mob, Player player) {
        if (!HarmontideConfig.luckyMoments() || !Missing.canLong(mob) || mob.isSleeping()) return false;
        if (player.getUUID().equals(((Bonded) mob).getFather())) return false;
        return !(mob instanceof AbstractMonsterGirl g && g.getSpecies().family() == GirlSpecies.Family.CONSTRUCT);
    }

    /** In the bath: the tub's towel, a harpy's bath, or standing still waist-deep in water (not one who lives in the water). */
    public static boolean bathing(Mob mob) {
        if (mob instanceof AbstractMonsterGirl g && g.hasFlag(AbstractMonsterGirl.FLAG_TOWEL)) return true;
        if (mob instanceof AbstractHarpy h && h.getAction() == AbstractHarpy.Action.BATHING) return true;
        if (!(mob instanceof AbstractMonsterGirl g)) return false;
        GirlSpecies.Family f = g.getSpecies().family();
        if (f == GirlSpecies.Family.MERFOLK || f == GirlSpecies.Family.MOLLUSK || f == GirlSpecies.Family.CRUSTACEAN) return false;
        return mob.isInWater() && mob.getFluidHeight(FluidTags.WATER) >= 0.5 && mob.getDeltaMovement().horizontalDistanceSqr() < 0.0025;
    }

    private static boolean lookingAt(Player player, Mob mob) {
        Vec3 eye = player.getEyePosition(), to = mob.position().add(0, mob.getBbHeight() * 0.6, 0).subtract(eye);
        double dist = to.length();
        return dist <= PEEK_RANGE && dist > 0.01 && player.getViewVector(1.0F).dot(to.scale(1.0 / dist)) > 0.985 && player.hasLineOfSight(mob);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player p = event.getEntity();
        if (p.tickCount % PEEK_EVERY == 0 && p.level() instanceof ServerLevel level && !p.isSpectator()) peek(level, p);
    }

    /** Once every PEEK_EVERY ticks: who the player is watching in her bath, and whether she noticed. What happened. */
    public static int peek(ServerLevel level, Player player) {
        CompoundTag pd = player.getPersistentData();
        Mob seen = null;
        for (Mob m : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(PEEK_RANGE), m -> m instanceof Bonded && m.isAlive())) {
            if (bathing(m) && eligible(m, player) && level.getGameTime() >= m.getPersistentData().getLong(PEEKED) && lookingAt(player, m)) {
                seen = m;
                break;
            }
        }
        if (seen == null) {
            pd.remove(PEEK_AT);
            pd.putInt(PEEK_FOR, 0);
            return NOTHING;
        }
        UUID id = seen.getUUID();
        int watched = pd.hasUUID(PEEK_AT) && pd.getUUID(PEEK_AT).equals(id) ? pd.getInt(PEEK_FOR) + PEEK_EVERY : PEEK_EVERY;
        pd.putUUID(PEEK_AT, id);
        pd.putInt(PEEK_FOR, watched);
        if (watched < PEEK_TICKS) return NOTHING;
        pd.putInt(PEEK_FOR, 0);
        return caught(level, seen, player);
    }

    /** She caught them looking. SPLASHED or TEASED. */
    public static int caught(ServerLevel level, Mob mob, Player player) {
        mob.getPersistentData().putLong(PEEKED, level.getGameTime() + PEEK_WAIT);
        mob.getLookControl().setLookAt(player);
        Bonded b = (Bonded) mob;
        b.blushFor(160);
        if (Flirting.canFlirt(mob, player)) {
            say(player, "peek_close", mob);
            level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.3, mob.getZ(), 3, 0.25, 0.15, 0.25, 0.0);
            return TEASED;
        }
        say(player, "peek_caught", mob);
        mob.playSound(SoundEvents.GENERIC_SPLASH, 1.0F, 1.3F);
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_SPLASH, SoundSource.NEUTRAL, 0.8F, 1.2F);
        level.sendParticles(ParticleTypes.SPLASH, player.getX(), player.getEyeY(), player.getZ(), 30, 0.3, 0.3, 0.3, 0.2);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
        push(player, mob, 0.4);
        if (b.getAffection(player) >= 1) b.addAffection(player, -1);
        return SPLASHED;
    }

    // --- trips -----------------------------------------------------------------------------------------------------

    public static boolean clumsy(Mob mob) {
        if (mob instanceof AbstractHarpy) return true;
        if (!(mob instanceof AbstractMonsterGirl g)) return false;
        GirlSpecies.Family f = g.getSpecies().family();
        return f == GirlSpecies.Family.CENTAUR || f == GirlSpecies.Family.LAMIA || f == GirlSpecies.Family.SLIME;
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        Entity e = event.getEntity();
        if (e.tickCount % TRIP_EVERY != 0 || !(e instanceof Mob mob) || !(e instanceof Bonded) || !(e.level() instanceof ServerLevel level)) return;
        if (!mob.onGround() || mob.isPassenger() || mob.isVehicle() || mob.getTarget() != null || mob.getDeltaMovement().horizontalDistanceSqr() < 0.001) return;
        if (mob.getRandom().nextInt(clumsy(mob) ? CLUMSY_CHANCE : TRIP_CHANCE) != 0) return;
        Player near = level.getNearestPlayer(mob, TRIP_RANGE);
        if (near != null && !near.isSpectator()) trip(level, mob, near);
    }

    /** She stumbles into the player. False if she isn't a friend who could. */
    public static boolean trip(ServerLevel level, Mob mob, Player player) {
        Bonded b = (Bonded) mob;
        if (!eligible(mob, player) || b.getAffection(player) < HomeLife.FRIEND) return false;
        boolean close = Flirting.canFlirt(mob, player);
        say(player, close ? "trip_close" : "trip", mob);
        b.blushFor(close ? 200 : 120);
        mob.playSound(SoundEvents.WOOL_FALL, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.POOF, player.getX(), player.getY() + 0.4, player.getZ(), 6, 0.3, 0.1, 0.3, 0.02);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1));
        push(player, mob, 0.25);
        if (mob instanceof AbstractMonsterGirl g) g.startTimedAction(AbstractMonsterGirl.Action.SITTING, close ? 80 : 30);
        else if (mob instanceof AbstractHarpy h) h.startTimedAction(AbstractHarpy.Action.SITTING, close ? 80 : 30);
        if (close) {
            level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.3, mob.getZ(), 3, 0.25, 0.15, 0.25, 0.0);
            b.addAffection(player, 1);
        }
        return true;
    }

    private static void push(Player player, @Nullable Mob from, double strength) {
        if (from == null) return;
        player.knockback(strength, from.getX() - player.getX(), from.getZ() - player.getZ());
        player.hurtMarked = true;
    }

    private static void say(Player player, String key, Mob mob) {
        player.displayClientMessage(Component.translatable("message.harmontide." + key + "." + mob.getRandom().nextInt(LINES), mob.getDisplayName()), true);
    }
}
