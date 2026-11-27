package com.linkyoshi90.harmontide.leviathan;

import com.linkyoshi90.harmontide.HarmontideMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Hooks the Dream-Leviathan event into the game: her story's clock, her insides, the blessing bar, deaths inside her. */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class LeviathanEvents {
    private LeviathanEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        LeviathanEvent.get(event.getServer().overworld()).tick(event.getServer().overworld());
        Interior.tick(event.getServer());
        Interior.tickHumps(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer p) {
            DreamHeart.tick(p);
            if (p.tickCount % 100 == 0) LeviathanNet.sendHeart(p);
        }
    }

    /** The blessing bar takes the hurt first (after armour and Absorption). */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer p)) return;
        // Pressed into the egg-cell, his head is inside the sphere. Zero damage ends the suffocation before it lands.
        if (Interior.humping(p) && event.getSource().is(DamageTypes.IN_WALL)) {
            event.setNewDamage(0);
            return;
        }
        event.setNewDamage(DreamHeart.absorb(p, event.getSource(), event.getNewDamage()));
    }

    /** Dying inside her: she puts you back outside, whole, with everything you carried. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && Interior.inside(p.level())) {
            event.setCanceled(true);
            p.setHealth(p.getMaxHealth());
            p.clearFire();
            p.removeAllEffects();
            p.getFoodData().setFoodLevel(Math.max(p.getFoodData().getFoodLevel(), 14));
            Interior.leave(p, "message.harmontide.leviathan.expelled");
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) LeviathanNet.sendHeart(p);
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) LeviathanNet.sendHeart(p);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer p) LeviathanNet.sendHeart(p);
    }

    /** Her insides can't be blown apart; an infested creeper leaves a cloud of bile where it burst. */
    @SubscribeEvent
    public static void onExplode(ExplosionEvent.Detonate event) {
        if (event.getExplosion().getDirectSourceEntity() instanceof Parasites.InfestedCreeper creeper) creeper.spillBile();
        if (Interior.inside(event.getLevel())) event.getAffectedBlocks().clear();
    }
}
