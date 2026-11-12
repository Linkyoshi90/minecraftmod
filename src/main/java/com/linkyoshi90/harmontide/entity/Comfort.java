package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.registry.ModAttachments;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;

/**
 * Looking out for their friend. Comfort: when a player is badly hurt (under a third of their health) the nearest friend within
 * 16 rushes to their side and, once there, steadies them (regeneration and a moment's resistance), at most every two minutes.
 * A snack: when a player is hungry (three drumsticks or fewer) the nearest friend brings them something of hers to eat, once a day.
 * The rain call: after three dry days, a frog friend near them croaks the rain in.
 * The forecast: a minute before rain comes, a friend near them sniffs the air and says so; frogs croak about it.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Comfort {
    public static final double CALL = 16, REACH = 3;
    public static final long COOLDOWN = 2400, FORECAST = 1200;
    public static final String NEXT = "ComfortAt", FORECAST_DAY = "ForecastFor", LUNCH = "LunchAt", DRY = "DryFrom";
    public static final long DRY_SPELL = 72000;
    public static final int HUNGRY = 6;
    public static final long LUNCH_WAIT = 24000;

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
        if (!HarmontideConfig.comfort() || !hurt(p)) return null;
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
        p.displayClientMessage(Component.translatable("message.harmontide.comfort", friend.getDisplayName()), true);
        return friend;
    }

    /** Every ten seconds for a hungry player: the nearest friend comes over and shares a snack. Who did, or null. */
    @Nullable
    public static Mob lunch(ServerLevel level, ServerPlayer p) {
        if (!HarmontideConfig.comfort() || p.getFoodData().getFoodLevel() > HUNGRY || p.isCreative() || p.isSpectator()) return null;
        CompoundTag data = p.getData(ModAttachments.JOURNAL);
        if (level.getGameTime() < data.getLong(LUNCH)) return null;
        Mob friend = nearestFriend(level, p, CALL);
        if (friend == null) return null;
        if (friend.distanceToSqr(p) > REACH * REACH) {
            friend.getNavigation().moveTo(p, 1.1);
            return null;
        }
        data.putLong(LUNCH, level.getGameTime() + LUNCH_WAIT);
        p.setData(ModAttachments.JOURNAL, data);
        ItemStack snack = friend instanceof com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl girl
                ? new ItemStack(net.minecraft.Util.getRandom(com.linkyoshi90.harmontide.entity.girl.Treats.TREATS.get(Letters.country(girl.getSpecies().family())), level.random))
                : new ItemStack(Items.BREAD);
        snack.setCount(Math.min(2, snack.getMaxStackSize()));                     // a bowl of stew is one bowl
        friend.getNavigation().stop();
        friend.getLookControl().setLookAt(p);
        if (!p.addItem(snack.copy())) p.drop(snack.copy(), false);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, p.getX(), p.getY() + 1.2, p.getZ(), 5, 0.3, 0.2, 0.3, 0);
        p.displayClientMessage(Component.translatable("message.harmontide.lunch", friend.getDisplayName(), snack.getHoverName()), false);
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
        if (!HarmontideConfig.comfort() || !level.dimensionType().natural()) return null;
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
        p.displayClientMessage(Component.translatable(frog ? "message.harmontide.forecast.frog" : "message.harmontide.forecast", friend.getDisplayName()), false);
        return friend;
    }

    /** Every ten seconds: the dry days counted, and after a dry spell a frog friend near croaks for rain. Who called it, or null. */
    @Nullable
    public static Mob rainCall(ServerLevel level, ServerPlayer p) {
        CompoundTag data = p.getData(ModAttachments.JOURNAL);
        long now = level.getGameTime();
        if (level.isRaining() || !data.contains(DRY)) {
            data.putLong(DRY, now);
            p.setData(ModAttachments.JOURNAL, data);
            return null;
        }
        if (!HarmontideConfig.comfort() || now - data.getLong(DRY) < DRY_SPELL || !level.dimensionType().natural()
                || !level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_WEATHER_CYCLE)) return null;
        Mob frog = null;
        for (Mob m : HomeLife.friendsOf(level, p, p.getBoundingBox().inflate(CALL))) if (m instanceof AbstractAnuran) frog = m;
        if (frog == null) return null;
        data.putLong(DRY, now);
        p.setData(ModAttachments.JOURNAL, data);
        level.setWeatherParameters(0, 6000 + level.random.nextInt(6000), true, false);
        level.playSound(null, frog.blockPosition(), SoundEvents.FROG_AMBIENT, SoundSource.NEUTRAL, 2.0F, 0.6F);
        p.displayClientMessage(Component.translatable("message.harmontide.rain_call", frog.getDisplayName()), false);
        return frog;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || !(p.level() instanceof ServerLevel level)) return;
        if (p.tickCount % 20 == 0) check(level, p);
        if (p.tickCount % 200 == 7) forecast(level, p);
        if (p.tickCount % 200 == 113) lunch(level, p);
        if (p.tickCount % 200 == 157) rainCall(level, p);
    }
}
