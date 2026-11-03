package com.linkyoshi90.monstergirls.item;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** What the girls' gear does while worn: the winged cap slows a fall and softens the landing. */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class GearEvents {
    /** The fastest a winged cap lets you fall (vanilla terminal speed is about -3.9). */
    public static final double CAP_FALL_SPEED = -0.3;

    private GearEvents() {}

    public static boolean wearsCap(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.WINGED_CAP.get());
    }

    /** Whether the cap's wings are beating right now (falling, not sneaking, not gliding or flying). The renderer flaps them while this holds. */
    public static boolean capFlapping(Player player) {
        return wearsCap(player) && !player.onGround() && !player.isInWater() && !player.isFallFlying() && !player.getAbilities().flying
                && !player.isShiftKeyDown() && player.getDeltaMovement().y < -0.1;
    }

    /**
     * A healing balm on a befriended girl (fond of you) or a vanilla friend of yours: 10 health back and some
     * regeneration. A wild one will not let you near enough, unless she is injured: then the balm mends her wound (see
     * AbstractMonsterGirl#mend).
     */
    @SubscribeEvent
    public static void onBalm(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND || !stack.is(ModItems.HEALING_BALM.get())
                || !(event.getTarget() instanceof net.minecraft.world.entity.LivingEntity target)) return;
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        if (!(event.getLevel() instanceof net.minecraft.server.level.ServerLevel level)) return;
        Player p = event.getEntity();
        if (target instanceof com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl girl && girl.isInjured()) {   // a wounded wild one lets you
            applyBalm(level, target);
            girl.mend(level, p);
            stack.consume(1, p);
            return;
        }
        if (target instanceof net.minecraft.world.entity.Mob hurt && com.linkyoshi90.monstergirls.entity.Injury.isInjured(hurt)) {   // a wounded harpy or frog
            applyBalm(level, target);
            com.linkyoshi90.monstergirls.entity.Injury.mend(level, hurt, p);
            stack.consume(1, p);
            return;
        }
        if (!befriended(target, p)) {
            p.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.balm_refused", target.getDisplayName()), true);
            return;
        }
        if (target.getHealth() >= target.getMaxHealth()) {
            p.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.monstergirls.balm_unhurt", target.getDisplayName()), true);
            return;
        }
        applyBalm(level, target);
        stack.consume(1, p);
    }

    /** The friendship charm (creative): any girl is devoted to you at once. */
    @SubscribeEvent
    public static void onCharm(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND || !event.getItemStack().is(ModItems.FRIENDSHIP_CHARM.get())
                || !(event.getTarget() instanceof com.linkyoshi90.monstergirls.entity.Bonded girl)) return;
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        if (!(event.getLevel() instanceof net.minecraft.server.level.ServerLevel level)) return;
        girl.setAffection(event.getEntity().getUUID(), com.linkyoshi90.monstergirls.entity.Bonded.FULL_AFFECTION);
        var t = event.getTarget();
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART, t.getX(), t.getY() + t.getBbHeight(), t.getZ(), 10, 0.4, 0.3, 0.4, 0.0);
    }

    public static boolean befriended(net.minecraft.world.entity.LivingEntity target, Player p) {
        return target instanceof com.linkyoshi90.monstergirls.entity.Bonded b && b.getAffection(p) >= com.linkyoshi90.monstergirls.entity.Orders.AFFECTION
                || p.getUUID().equals(com.linkyoshi90.monstergirls.entity.girl.Kinship.friendOf(target));
    }

    public static void applyBalm(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target) {
        target.heal(10.0F);
        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 200, 1));
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART, target.getX(), target.getY() + target.getBbHeight(), target.getZ(), 5, 0.3, 0.2, 0.3, 0.0);
        level.playSound(null, target, net.minecraft.sounds.SoundEvents.HONEY_DRINK, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.2F);
    }

    /** Runs on both sides: the client moves its own player. */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player p = event.getEntity();
        if (!capFlapping(p)) return;
        Vec3 v = p.getDeltaMovement();
        if (v.y < CAP_FALL_SPEED) p.setDeltaMovement(v.x, CAP_FALL_SPEED, v.z);
    }

    /** A capped landing hurts a fifth as much (sneaking folds the wings: full damage). */
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player p && wearsCap(p) && !p.isShiftKeyDown()) event.setDamageMultiplier(event.getDamageMultiplier() * 0.2F);
    }
}
