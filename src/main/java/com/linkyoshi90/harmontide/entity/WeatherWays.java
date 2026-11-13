package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.SleepGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Weather ways (2026-11-13): each kind meets the weather in her own way, on top of what was there (cats waiting out the rain, frogs
 * loving it, reptiles basking on clear mornings, colds: see the journal's "rain" page).
 * <ul>
 * <li>{@link Way#STORM_LOVER} (the raiju, the sylph, dragons): a thunderstorm thrills her (Speed, Strength, sparks).</li>
 * <li>{@link Way#RAIN_LOVER} (frogs and newts, slimes, snails and the fungus kinds): out in the rain she livens up (Speed) and is up and
 * about whatever her usual hours.</li>
 * <li>{@link Way#DRIER} (birds, insects, cats): when the sun comes out after rain ({@link #AFTER_RAIN} ticks), she suns or grooms herself
 * dry in the open, a wisp of steam rising off her.</li>
 * <li>{@link Way#SNOW_LOVER} (the cold kinds): falling snow puts her in high spirits (she laughs, Speed).</li>
 * <li>{@link Way#WARM_LOVER} (the sun and heat kinds) shivers in falling snow and, like everyone else ({@link Way#ORDINARY}) in a
 * thunderstorm, takes cover ({@link ShelterGoal}: somewhere roofed near by) and huddles there till it passes.</li>
 * </ul>
 * Wild girls and befriended ones roaming free take cover; a friend following you or sitting on your order stays. Config {@code weatherWays}.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class WeatherWays {
    public static final int PULSE = 100, AFTER_RAIN = 2400, HUDDLE = 200;
    private static final Map<Level, Long> LAST_RAIN = new WeakHashMap<>();

    private WeatherWays() {}

    public enum Way { ORDINARY, STORM_LOVER, RAIN_LOVER, DRIER, SNOW_LOVER, WARM_LOVER }

    /** The weather where she stands: thunder, rain, snow falling on her (none under a roof), the sun back after rain. */
    public record Sky(boolean thunder, boolean rain, boolean snow, boolean afterRain, boolean open) {}

    public static Way way(AbstractMonsterGirl g) {
        GirlSpecies s = g.getSpecies();
        if (s == GirlSpecies.RAIJU_CANID || s == GirlSpecies.SYLPH_ELF || s.family() == GirlSpecies.Family.DRAGON) return Way.STORM_LOVER;
        Clade c = Clade.of(g);
        if (c == Clade.AMPHIBIA || s.family() == GirlSpecies.Family.SLIME || Decomposers.FUNGI.contains(s) || s == GirlSpecies.AME_ONNA_UNDEAD
                || s.family() == GirlSpecies.Family.MOLLUSK && !g.isInWater()) return Way.RAIN_LOVER;
        var needs = Microhabitats.needs(g);
        if (needs.contains(Microhabitats.Need.COLD)) return Way.SNOW_LOVER;
        if (needs.contains(Microhabitats.Need.HOT) || needs.contains(Microhabitats.Need.SUN)) return Way.WARM_LOVER;
        if (c == Clade.AVES || c == Clade.HEXAPODA || c == Clade.FELIFORMIA || s.family() == GirlSpecies.Family.NEKOMATA) return Way.DRIER;
        return Way.ORDINARY;
    }

    public static Sky sky(AbstractMonsterGirl g) {
        Level level = g.level();
        BlockPos at = g.blockPosition();
        boolean open = level.canSeeSky(at.above());
        boolean wet = level.getLevelData().isRaining();
        Biome.Precipitation p = level.getBiome(at).value().getPrecipitationAt(at);
        boolean after = !wet && LAST_RAIN.containsKey(level) && level.getGameTime() - LAST_RAIN.get(level) < AFTER_RAIN && level.isDay();
        return new Sky(level.getLevelData().isThundering(), wet && open && p == Biome.Precipitation.RAIN, wet && open && p == Biome.Precipitation.SNOW, after, open);
    }

    /** Whether she takes cover from this weather. */
    public static boolean shelters(AbstractMonsterGirl g, Sky sky) {
        Way w = way(g);
        if (!sky.open() || g.isInWater() || w == Way.STORM_LOVER || w == Way.RAIN_LOVER) return false;
        return sky.thunder() || w == Way.WARM_LOVER && sky.snow();
    }

    /** Free to go and take cover: wild, or roaming free; not following, not sitting on an order, not a baby with her mother. */
    public static boolean free(AbstractMonsterGirl g) {
        return HarmontideConfig.weatherWays() && !g.isPassenger() && !g.isLeashed() && !g.isOrderedToSit() && Orders.current(g) != Orders.Command.FOLLOW;
    }

    /** Rain-lovers are up and about in the rain, whatever their usual hours (AbstractMonsterGirl#restingHours asks this). */
    public static boolean awakeInRain(AbstractMonsterGirl g) {
        return HarmontideConfig.weatherWays() && way(g) == Way.RAIN_LOVER && g.level().getLevelData().isRaining();
    }

    /** One round of her weather ways (not the taking cover, which is the ShelterGoal's). What she did, or null. */
    @Nullable
    public static String feel(ServerLevel level, AbstractMonsterGirl g, Sky sky) {
        if (!HarmontideConfig.weatherWays() || g.isBaby()) return null;
        Way w = way(g);
        if (w == Way.STORM_LOVER && sky.thunder() && sky.open()) {
            g.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, PULSE + 20, 0, true, false, true));
            g.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, PULSE + 20, 0, true, false, true));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, g.getX(), g.getEyeY(), g.getZ(), 6, 0.4, 0.4, 0.4, 0.05);
            return "thrilled";
        }
        if (w == Way.RAIN_LOVER && sky.rain()) {
            g.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, PULSE + 20, 0, true, false, true));
            level.sendParticles(ParticleTypes.SPLASH, g.getX(), g.getEyeY(), g.getZ(), 6, 0.3, 0.3, 0.3, 0.0);
            return "glistening";
        }
        if (w == Way.SNOW_LOVER && sky.snow()) {
            g.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, PULSE + 20, 0, true, false, true));
            level.sendParticles(ParticleTypes.SNOWFLAKE, g.getX(), g.getEyeY(), g.getZ(), 8, 0.4, 0.4, 0.4, 0.02);
            g.startTimedAction(AbstractMonsterGirl.Action.LAUGH, 30);
            return "playing";
        }
        if (w == Way.WARM_LOVER && sky.snow()) {
            level.sendParticles(ParticleTypes.SNOWFLAKE, g.getX(), g.getEyeY(), g.getZ(), 3, 0.2, 0.1, 0.2, 0.0);
            return "shivering";
        }
        if (w == Way.DRIER && sky.afterRain() && sky.open()) {
            g.startTimedAction(Clade.of(g) == Clade.FELIFORMIA || g.getSpecies().family() == GirlSpecies.Family.NEKOMATA ? AbstractMonsterGirl.Action.GROOM : AbstractMonsterGirl.Action.BASK, 80);
            level.sendParticles(ParticleTypes.CLOUD, g.getX(), g.getEyeY() + 0.2, g.getZ(), 3, 0.2, 0.2, 0.2, 0.01);
            return "drying";
        }
        return null;
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!event.getLevel().isClientSide && event.getLevel().getLevelData().isRaining()) LAST_RAIN.put(event.getLevel(), event.getLevel().getGameTime());
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof AbstractMonsterGirl g && g.tickCount % PULSE == 29 && g.level() instanceof ServerLevel level) feel(level, g, sky(g));
    }

    /** Taking cover from a storm (or, for the warmth-lovers, from falling snow), then huddling till it passes. */
    public static final class ShelterGoal extends Goal {
        private final AbstractMonsterGirl girl;
        @Nullable private BlockPos to;
        private int cooldown, ticks;

        public ShelterGoal(AbstractMonsterGirl girl) {
            this.girl = girl;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (--this.cooldown > 0) return false;
            this.cooldown = 40;
            if (this.girl.isBaby() || !free(this.girl) || !shelters(this.girl, sky(this.girl))) return false;
            this.to = SleepGoal.cover(this.girl);
            return this.to != null;
        }

        @Override
        public void start() {
            this.ticks = 0;
            this.girl.getNavigation().moveTo(this.to.getX() + 0.5, this.to.getY(), this.to.getZ() + 0.5, 1.2);
        }

        @Override
        public boolean canContinueToUse() {
            return this.ticks < 400 && free(this.girl);
        }

        @Override
        public void tick() {
            if (++this.ticks % 20 != 0) return;
            if (this.to != null && this.girl.blockPosition().closerThan(this.to, 1.5)) {
                this.to = null;
                this.girl.getNavigation().stop();
                this.girl.startTimedAction(AbstractMonsterGirl.Action.SITTING, HUDDLE);   // huddled up till it passes
            } else if (this.to != null && this.girl.getNavigation().isDone()) {
                this.girl.getNavigation().moveTo(this.to.getX() + 0.5, this.to.getY(), this.to.getZ() + 0.5, 1.2);
            }
        }
    }
}
