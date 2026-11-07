package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * Optional (config "friendsFaint", off by default): a harpy, frog or girl who counts anyone a friend faints instead of
 * dying. She keeps a quarter of her health, is shielded for a few seconds (resistance, fire resistance, water
 * breathing, regeneration) and, if she is on solid ground, sits down until a friend gets her up with a stick. Her
 * friends hear where she is. She cannot faint twice within {@link #RECOVERY} ticks, and nothing saves her from /kill
 * or the void.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Fainting {
    public static final long RECOVERY = 1200;
    private static final String KEY = "FaintedAt";

    private Fainting() {}

    /** Whether she fainted within the last {@link #RECOVERY} ticks. */
    public static boolean recovering(Mob mob) {
        return mob.getPersistentData().contains(KEY) && mob.level().getGameTime() - mob.getPersistentData().getLong(KEY) < RECOVERY;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (!MonsterGirlsConfig.friendsFaint() || !(event.getEntity() instanceof Mob mob) || !(mob instanceof Bonded bonded)
                || !(mob.level() instanceof ServerLevel level) || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || recovering(mob)) return;
        java.util.List<ServerPlayer> friends = level.getServer().getPlayerList().getPlayers().stream()
                .filter(p -> bonded.getAffection(p) >= Orders.AFFECTION).toList();
        if (friends.isEmpty()) return;
        event.setCanceled(true);
        mob.setHealth(Math.max(1.0F, mob.getMaxHealth() * 0.25F));
        mob.clearFire();
        mob.setTarget(null);
        mob.getNavigation().stop();
        mob.getPersistentData().putLong(KEY, level.getGameTime());
        mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4));
        mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
        mob.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 600, 0));
        mob.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 0));
        if (mob.onGround() && !mob.isInLiquid()) {                       // she stays put; lying in lava or water she still gets herself out
            bonded.getOrders().set(Orders.Command.SIT, null, null);
            bonded.setOrderedToSit(true);
        }
        level.sendParticles(ParticleTypes.CLOUD, mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(), 8, 0.3, 0.2, 0.3, 0.02);
        Component message = Component.translatable("message.monstergirls.fainted", mob.getDisplayName(),
                mob.getBlockX(), mob.getBlockY(), mob.getBlockZ()).withStyle(ChatFormatting.YELLOW);
        for (ServerPlayer p : friends) p.sendSystemMessage(message);
    }
}
