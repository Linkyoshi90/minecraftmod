package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * The company of the dead: while a zombie girl (or a drowned one) walks with a player, following them or close by their side,
 * the undead and creepers take that player for one of their own and leave them be. Hitting one breaks the spell for it.
 * (Her own kin, zombies, husks and drowned, already leave her friends alone near her: entity/girl/Kinship. This reaches
 * every kind of the dead, and creepers, as far as she follows.)
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class Shamblers {
    public static final Set<GirlSpecies> SHAMBLERS = Set.of(GirlSpecies.ZOMBIE_UNDEAD, GirlSpecies.DROWNED_UNDEAD);
    /** How far a following zombie girl may be, and how close one just keeping them company. */
    public static final double FOLLOWING = 32, BESIDE = 10;
    private static final String TOLD = "ShamblerToldAt";

    private Shamblers() {}

    /** The zombie girl walking with this player, or null. */
    @Nullable
    public static AbstractMonsterGirl companion(Player p) {
        if (!com.linkyoshi90.harmontide.HarmontideConfig.deadCompany()) return null;
        for (AbstractMonsterGirl g : p.level().getEntitiesOfClass(AbstractMonsterGirl.class, p.getBoundingBox().inflate(FOLLOWING),
                g -> g.isAlive() && SHAMBLERS.contains(g.getSpecies()) && !g.isBaby() && g.getAffection(p) >= Orders.AFFECTION)) {
            boolean following = Orders.current(g) == Orders.Command.FOLLOW && p.getUUID().equals(g.getOrders().commanderId());
            if (following || g.distanceToSqr(p) < BESIDE * BESIDE && !Orders.holdsPlace(g)) return g;
        }
        return null;
    }

    /** The dead and the creepers, not the realm's own girls. */
    public static boolean fooled(LivingEntity mob) {
        return !(mob instanceof AbstractMonsterGirl) && !(mob instanceof net.minecraft.world.entity.boss.wither.WitherBoss)   // (not the Wither)
                && (mob.getType().is(EntityTypeTags.UNDEAD) || mob instanceof Creeper);
    }

    @SubscribeEvent
    public static void onTarget(LivingChangeTargetEvent event) {
        if (!(event.getNewAboutToBeSetTarget() instanceof Player p) || !fooled(event.getEntity())) return;
        if (event.getEntity().getLastHurtByMob() == p) return;                 // it was struck: it knows better now
        if (companion(p) != null) event.setCanceled(true);
    }

    /** Every second: anything of the dead already after the player loses interest, and the player is told the first time. */
    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || p.tickCount % 20 != 11) return;
        boolean hunted = false;
        for (Mob m : p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(24), m -> m.getTarget() == p && fooled(m) && m.getLastHurtByMob() != p)) {
            hunted = true;
            break;
        }
        if (!hunted) return;
        AbstractMonsterGirl zombie = companion(p);
        if (zombie == null) return;
        for (Mob m : p.level().getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(24), m -> m.getTarget() == p && fooled(m) && m.getLastHurtByMob() != p)) {
            m.setTarget(null);
            if (m instanceof Creeper c) c.setSwellDir(-1);
        }
        long day = p.level().getDayTime() / 24000L;
        if (p.getPersistentData().getLong(TOLD) != day + 1) {
            p.getPersistentData().putLong(TOLD, day + 1);
            p.displayClientMessage(Component.translatable("message.harmontide.shambler_company", zombie.getDisplayName()), true);
        }
    }
}
