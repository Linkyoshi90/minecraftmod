package com.linkyoshi90.monstergirls;

import com.linkyoshi90.monstergirls.entity.girl.Arachne;
import com.linkyoshi90.monstergirls.entity.girl.Lamia;
import com.linkyoshi90.monstergirls.entity.girl.Merfolk;
import com.linkyoshi90.monstergirls.entity.girl.Nekomata;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/** World rules the later families bring along (see their field guides). */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class GirlEvents {
    private GirlEvents() {}

    /** Creepers keep away from nekomata, as they do from vanilla cats. */
    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Creeper creeper) {
            creeper.goalSelector.addGoal(3, new AvoidEntityGoal<>(creeper, Nekomata.class, 6.0F, 1.0, 1.2));
        }
    }

    /** The spider truce: spiders leave alone a player an arachne is devoted to. */
    @SubscribeEvent
    public static void onTarget(LivingChangeTargetEvent event) {
        if ((event.getEntity() instanceof Spider || event.getEntity() instanceof CaveSpider)
                && event.getNewAboutToBeSetTarget() instanceof Player player && Arachne.truceFor(player)) {
            event.setCanceled(true);
        }
    }

    /**
     * A devoted lamia beside her player takes half of the damage on her coils. A player who strikes something, or is
     * struck, rallies the lioness pride that loves them.
     */
    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;
        if (victim instanceof Player player) {
            Lamia lamia = Lamia.guardianOf(player);
            if (lamia != null && event.getAmount() > 1.0F && !event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                float half = event.getAmount() / 2;
                event.setAmount(half);
                lamia.hurt(lamia.damageSources().generic(), half * 0.5F);   // her scales soak up some of it
            }
            if (event.getSource().getEntity() instanceof LivingEntity attacker) Nekomata.rallyPride(player, attacker);
        } else if (event.getSource().getEntity() instanceof Player player) {
            Nekomata.rallyPride(player, victim);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Merfolk merfolk) Merfolk.onKill(merfolk, event.getEntity());
    }
}
