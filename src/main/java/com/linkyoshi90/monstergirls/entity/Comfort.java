package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;

/**
 * Looking out for their friend. Comfort: when a player is badly hurt (under a third of their health) the nearest friend within
 * 16 rushes to their side and, once there, steadies them (regeneration and a moment's resistance), at most every two minutes.
 * The forecast: a minute before rain comes, a friend near them sniffs the air and says so; frogs croak about it.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Comfort {
    public static final double CALL = 16, REACH = 3;
    public static final long COOLDOWN = 2400, FORECAST = 1200;
    public static final String NEXT = "ComfortAt", FORECAST_DAY = "ForecastFor";

    private Comfort() {
    }

    public static boolean hurt(ServerPlayer p) {
        return p.getHealth() < p.getMaxHealth() / 3 && !p.isCreative() && !p.isSpectator() && p.isAlive();
    }

    @Nullable
    private static Mob nearestFriend(ServerLevel level, ServerPlayer p, double range) {
        Mob best = null;
        double bestDist = range * range;
        for (Mob m : HomeLife.friendsOf(level, p, p.getBoundingBox().inflate(range))) {
            if (m.isPassenger() || m.isSleeping() || m.getTarget() != null && m.getTarget() != p.getLastHurtByMob()) continue;
            double d = m.distanceToSqr(p);
            if (d < bestDist) {
                bestDist = d;
                best = m;
            }
        }
        return best;
    }

    /** Once a second for a hurt player: the nearest friend comes, and steadies them when she is there. Who steadied them, or null. */
    @Nullable
    public static Mob check(ServerLevel level, ServerPlayer p) {
        if (!MonsterGirlsConfig.comfort() || !hurt(p)) return null;
        CompoundTag data = p.getData(ModAttachments.JOURNAL);
        if (level.getGameTime() < data.getLong(NEXT)) return null;
        Mob friend = nearestFriend(level, p, CALL);
        if (friend == null) return null;
        if (friend.distanceToSqr(p) > REACH * REACH) {
            friend.getNavigation().moveTo(p, 1.4);
            return null;
        }
        data.putLong(NEXT, level.getGameTime() + COOLDOWN);
        p.setData(ModAttachments.JOURNAL, data);
        friend.getNavigation().stop();
        friend.getLookControl().setLookAt(p);
        p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1));
        p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0));
        level.sendParticles(ParticleTypes.HEART, p.getX(), p.getY() + p.getBbHeight() + 0.2, p.getZ(), 5, 0.4, 0.2, 0.4, 0);
        level.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.0F, 1.2F);
        p.displayClientMessage(Component.translatable("message.monstergirls.comfort", friend.getDisplayName()), true);
        return friend;
    }

    /** Ticks until rain (0 while it rains, or when the weather is held). */
    public static int untilRain(ServerLevel level) {
        if (level.isRaining() || !level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_WEATHER_CYCLE)
                || !(level.getLevelData() instanceof ServerLevelData d) || d.getClearWeatherTime() > 0) return 0;
        return d.getRainTime();
    }

    /** Every ten seconds: rain within a minute, and a friend near says so (once a spell of fine weather). Who said it, or null. */
    @Nullable
    public static Mob forecast(ServerLevel level, ServerPlayer p) {
        if (!MonsterGirlsConfig.comfort() || !level.dimensionType().natural()) return null;
        int t = untilRain(level);
        if (t <= 0 || t > FORECAST) return null;
        long spell = level.getGameTime() + t;                                     // which rain this is
        CompoundTag data = p.getData(ModAttachments.JOURNAL);
        if (Math.abs(data.getLong(FORECAST_DAY) - spell) < 200) return null;
        Mob friend = nearestFriend(level, p, 24);
        if (friend == null) return null;
        data.putLong(FORECAST_DAY, spell);
        p.setData(ModAttachments.JOURNAL, data);
        boolean frog = friend instanceof AbstractAnuran;
        if (frog) level.playSound(null, friend.blockPosition(), SoundEvents.FROG_AMBIENT, SoundSource.NEUTRAL, 1.6F, 0.8F);
        p.displayClientMessage(Component.translatable(frog ? "message.monstergirls.forecast.frog" : "message.monstergirls.forecast", friend.getDisplayName()), false);
        return friend;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || !(p.level() instanceof ServerLevel level)) return;
        if (p.tickCount % 20 == 0) check(level, p);
        if (p.tickCount % 200 == 7) forecast(level, p);
    }
}
