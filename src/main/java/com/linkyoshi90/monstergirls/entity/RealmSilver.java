package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Realm silver, after the demon realm silver of Monster Girl Encyclopedia: a blade of it cuts the spirit, not the flesh. Struck
 * with a realm silver sword, a monster girl (or harpy, or frog girl) takes no wound: she stops fighting, blushes, and warms to
 * the one who held it ({@link #WARMTH}, at most once every {@link #WARM_EVERY} ticks). Anything else it cuts as an iron sword.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class RealmSilver {
    public static final int WARMTH = 1, WARM_EVERY = 100, BLUSH = 60;
    private static final String WARMED = "RealmSilverWarmed";

    private RealmSilver() {}

    /** A blow of a realm silver sword at this creature. True if it spared her (so the blow should do nothing else). */
    public static boolean spare(ServerLevel level, Mob mob, Player player) {
        if (!(mob instanceof Bonded bonded)) return false;
        mob.setTarget(null);
        mob.setLastHurtByMob(null);
        bonded.blushFor(BLUSH);
        long now = level.getGameTime();
        if (now - mob.getPersistentData().getLong(WARMED) >= WARM_EVERY) {
            mob.getPersistentData().putLong(WARMED, now);
            bonded.addAffection(player, WARMTH);
        }
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.3, mob.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
        level.sendParticles(com.linkyoshi90.monstergirls.block.RealmGrassBlock.HAZE, mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(), 6, 0.3, 0.3, 0.3, 0.0);
        mob.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.8F, 1.6F);
        return true;
    }

    @SubscribeEvent
    public static void onHit(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player) || event.getSource().getDirectEntity() != player
                || !player.getMainHandItem().is(ModItems.REALM_SILVER_SWORD.get()) || !(event.getEntity() instanceof Mob mob)
                || !(player.level() instanceof ServerLevel level)) return;
        if (spare(level, mob, player)) event.setCanceled(true);
    }
}
