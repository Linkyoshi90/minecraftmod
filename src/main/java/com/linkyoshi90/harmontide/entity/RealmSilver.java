package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.registry.ModItems;
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
@EventBusSubscriber(modid = HarmontideMod.MODID)
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
        Dairy.award(player, "realm_silver");
        if (now - mob.getPersistentData().getLong(WARMED) >= WARM_EVERY) {
            mob.getPersistentData().putLong(WARMED, now);
            bonded.addAffection(player, WARMTH);
        }
        level.sendParticles(ParticleTypes.HEART, mob.getX(), mob.getEyeY() + 0.3, mob.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
        level.sendParticles(com.linkyoshi90.harmontide.block.RealmGrassBlock.HAZE, mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(), 6, 0.3, 0.3, 0.3, 0.0);
        mob.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.8F, 1.6F);
        return true;
    }

    /** Pieces of realm silver armour this one wears. */
    public static int pieces(net.minecraft.world.entity.LivingEntity e) {
        int n = 0;
        for (net.minecraft.world.item.ItemStack s : e.getArmorSlots()) {
            if (s.getItem() instanceof net.minecraft.world.item.ArmorItem a && a.getMaterial().is(com.linkyoshi90.harmontide.registry.ModArmorMaterials.REALM_SILVER.getKey())) n++;
        }
        return n;
    }

    /** Two pieces or more: a monster girl's blow lands soft (half). */
    public static final int SOFT = 2, UNSEEN = 4;

    public static float softened(net.minecraft.world.entity.LivingEntity target, net.minecraft.world.entity.Entity attacker, float amount) {
        return attacker instanceof Bonded && pieces(target) >= SOFT ? amount * 0.5F : amount;
    }

    /** A full set: wild monster girls (not her friends) don't pick its wearer to fight. */
    public static boolean unseen(net.minecraft.world.entity.LivingEntity target, Mob attacker) {
        return attacker instanceof Bonded b && target instanceof Player p && pieces(p) >= UNSEEN && b.getAffection(p) < Bonded.BED_AFFECTION;
    }

    @SubscribeEvent
    public static void onSoftBlow(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide || event.getSource().getEntity() == null) return;
        event.setAmount(softened(event.getEntity(), event.getSource().getEntity(), event.getAmount()));
    }

    @SubscribeEvent
    public static void onTarget(net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent event) {
        if (event.getEntity() instanceof Mob mob && event.getNewAboutToBeSetTarget() != null && unseen(event.getNewAboutToBeSetTarget(), mob)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onHit(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player) || event.getSource().getDirectEntity() != player
                || !player.getMainHandItem().is(ModItems.REALM_SILVER_SWORD.get()) || !(event.getEntity() instanceof Mob mob)
                || !(player.level() instanceof ServerLevel level)) return;
        if (spare(level, mob, player) || com.linkyoshi90.harmontide.entity.order.OrderSister.shake(level, mob, player)) event.setCanceled(true);
    }
}
