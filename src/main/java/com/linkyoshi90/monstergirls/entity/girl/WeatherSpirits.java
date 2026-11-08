package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.HomeLife;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;

/**
 * Weather spirits (2026-11-08): girls who come with the weather and, while wild, go with it.
 * <ul>
 * <li>The raiju comes only in a thunderstorm. Lightning never touches her. Befriended, in a storm she calls a bolt down on a
 * monster near her friend every {@link #CALL_EVERY} ticks.</li>
 * <li>The ame-onna comes only in the rain. Befriended, in the rain the crops round her grow, and a water bottle from her friend
 * brings a shower, once a day.</li>
 * <li>The sylph comes with any rain or storm, to the windswept hills. Befriended, her friend near her under the open sky has a
 * tailwind (Speed) and floats down instead of falling.</li>
 * </ul>
 * A wild one that came naturally with her weather leaves (in a gust) when it clears; a friend stays. The spawn gating lives in
 * {@link com.linkyoshi90.monstergirls.entity.SpawnConditions}.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class WeatherSpirits {
    public static final int CALL_EVERY = 200, CALL_REACH = 12, NEAR = 16, BOLT = 8, GROW_EVERY = 600, GROW_REACH = 6, SHOWER = 6000, FALL = 3;
    private static final String BORN = "WeatherBorn", SHOWER_DAY = "ShowerDay";

    private WeatherSpirits() {}

    public static boolean raiju(Mob m) {
        return m instanceof AbstractMonsterGirl g && g.getSpecies() == GirlSpecies.RAIJU_CANID;
    }

    public static boolean ameOnna(Mob m) {
        return m instanceof AbstractMonsterGirl g && g.getSpecies() == GirlSpecies.AME_ONNA_UNDEAD;
    }

    public static boolean sylph(Mob m) {
        return m instanceof AbstractMonsterGirl g && g.getSpecies() == GirlSpecies.SYLPH_ELF;
    }

    public static boolean spirit(Mob m) {
        return raiju(m) || ameOnna(m) || sylph(m);
    }

    @Nullable
    private static Player friend(AbstractMonsterGirl g) {
        return g.isBaby() ? null : g.bondedPlayer(HomeLife.FRIEND, NEAR);
    }

    /** Her weather: thunder for the raiju, rain for the ame-onna, any rain or storm for the sylph. */
    public static boolean herWeather(Mob m, boolean raining, boolean thundering) {
        if (raiju(m)) return thundering;
        return raining || thundering;
    }

    // --- the raiju ----------------------------------------------------------------------------------------------------

    /** In a storm she calls a bolt down on the nearest monster near her friend. The one struck, or null. */
    @Nullable
    public static Mob thunderCall(ServerLevel level, AbstractMonsterGirl g, boolean thundering) {
        Player f = friend(g);
        if (!thundering || f == null) return null;
        Mob target = null;
        double best = Double.MAX_VALUE;
        for (Mob m : level.getEntitiesOfClass(Mob.class, f.getBoundingBox().inflate(CALL_REACH), m -> m instanceof Enemy && !(m instanceof Bonded) && m.isAlive())) {
            double d = m.distanceToSqr(f);
            if (d < best) {
                best = d;
                target = m;
            }
        }
        if (target == null) return null;
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(target.position());
            bolt.setVisualOnly(true);                                        // the strike is hers to aim: no fires, no stray hits
            level.addFreshEntity(bolt);
        }
        target.hurt(level.damageSources().lightningBolt(), BOLT);
        target.igniteForSeconds(3);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, g.getX(), g.getY() + g.getBbHeight(), g.getZ(), 12, 0.4, 0.4, 0.4, 0.1);
        return target;
    }

    @SubscribeEvent
    public static void onStruck(EntityStruckByLightningEvent event) {
        if (event.getEntity() instanceof Mob m && raiju(m)) {
            event.setCanceled(true);                                         // lightning never touches her
            if (m.level() instanceof ServerLevel level) level.sendParticles(ParticleTypes.ELECTRIC_SPARK, m.getX(), m.getY() + 1, m.getZ(), 20, 0.4, 0.6, 0.4, 0.2);
        }
    }

    // --- the ame-onna -------------------------------------------------------------------------------------------------

    /** In the rain the crops round her grow a stage. How many grew. */
    public static int rainGrowth(ServerLevel level, AbstractMonsterGirl g, boolean raining) {
        if (!raining || friend(g) == null) return 0;
        int grew = 0;
        BlockPos c = g.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(c.offset(-GROW_REACH, -2, -GROW_REACH), c.offset(GROW_REACH, 2, GROW_REACH))) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof CropBlock crop && !crop.isMaxAge(s)) {
                level.setBlock(p, crop.getStateForAge(crop.getAge(s) + 1), 2);
                grew++;
            }
        }
        if (grew > 0) level.sendParticles(ParticleTypes.SPLASH, g.getX(), g.getY() + 0.5, g.getZ(), 20, GROW_REACH / 2.0, 0.3, GROW_REACH / 2.0, 0.0);
        return grew;
    }

    /** A water bottle from her friend: she brings a shower (once a day). True if it began to rain. */
    public static boolean shower(ServerLevel level, AbstractMonsterGirl g, Player p) {
        if (!ameOnna(g) || g.isBaby() || g.getAffection(p) < HomeLife.FRIEND) return false;
        long day = level.getDayTime() / 24000L;
        if (level.getLevelData().isRaining() || g.getPersistentData().getLong(SHOWER_DAY) == day + 1) {
            p.displayClientMessage(Component.translatable("message.monstergirls.ame_onna.not_now", g.getDisplayName()), true);
            return false;
        }
        g.getPersistentData().putLong(SHOWER_DAY, day + 1);
        level.setWeatherParameters(0, SHOWER, true, false);
        level.playSound(null, g.blockPosition(), SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.FALLING_WATER, g.getX(), g.getY() + g.getBbHeight() + 1, g.getZ(), 30, 1.0, 0.5, 1.0, 0.0);
        p.displayClientMessage(Component.translatable("message.monstergirls.ame_onna.shower", g.getDisplayName()).withStyle(ChatFormatting.AQUA), false);
        return true;
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (!(event.getTarget() instanceof AbstractMonsterGirl g) || !ameOnna(g) || !stack.is(Items.POTION)) return;
        PotionContents pc = stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
        if (pc == null || !pc.is(Potions.WATER) || !(event.getLevel() instanceof ServerLevel level)) return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        Player p = event.getEntity();
        if (shower(level, g, p)) p.setItemInHand(event.getHand(), ItemUtils.createFilledResult(stack, p, new ItemStack(Items.GLASS_BOTTLE)));
    }

    // --- the sylph ----------------------------------------------------------------------------------------------------

    /** Her friend near her under the open sky: a tailwind, and a soft landing. True if she helped. */
    public static boolean tailwind(ServerLevel level, AbstractMonsterGirl g) {
        Player f = friend(g);
        if (f == null || !level.canSeeSky(f.blockPosition().above())) return false;
        f.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, true, false));
        if (f.fallDistance > FALL) f.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 0, true, false));
        return true;
    }

    // --- coming and going ---------------------------------------------------------------------------------------------

    /** A wild one who came with the weather leaves when it clears (a friend stays). True if she left. */
    public static boolean leaves(ServerLevel level, AbstractMonsterGirl g, boolean raining, boolean thundering) {
        if (!g.getPersistentData().getBoolean(BORN)) return false;
        if (g.hasAnyAffection()) {
            g.getPersistentData().remove(BORN);
            return false;
        }
        if (herWeather(g, raining, thundering)) return false;
        level.sendParticles(ParticleTypes.CLOUD, g.getX(), g.getY() + 1, g.getZ(), 16, 0.4, 0.6, 0.4, 0.05);
        level.playSound(null, g.blockPosition(), SoundEvents.PHANTOM_FLAP, SoundSource.NEUTRAL, 0.8F, 1.4F);
        g.discard();
        return true;
    }

    public static void weatherBorn(Mob m) {
        m.getPersistentData().putBoolean(BORN, true);
    }

    public static boolean isWeatherBorn(Mob m) {
        return m.getPersistentData().getBoolean(BORN);
    }

    @SubscribeEvent
    public static void onSpawn(FinalizeSpawnEvent event) {
        if (event.getSpawnType() == MobSpawnType.NATURAL && spirit(event.getEntity())) weatherBorn(event.getEntity());
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof AbstractMonsterGirl g) || !(g.level() instanceof ServerLevel level) || !spirit(g)) return;
        boolean raining = level.isRaining(), thundering = level.isThundering();
        if (g.tickCount % 100 == 37 && leaves(level, g, raining, thundering)) return;
        if (raiju(g) && g.tickCount % CALL_EVERY == 0) thunderCall(level, g, thundering);
        if (ameOnna(g) && g.tickCount % GROW_EVERY == 0) rainGrowth(level, g, raining && level.isRainingAt(g.blockPosition().above(2)));
        if (sylph(g) && g.tickCount % 20 == 5) tailwind(level, g);
    }
}
