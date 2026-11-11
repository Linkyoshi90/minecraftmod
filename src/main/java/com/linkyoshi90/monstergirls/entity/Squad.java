package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlArms;
import com.linkyoshi90.monstergirls.entity.order.Order;
import com.linkyoshi90.monstergirls.entity.order.OrderSister;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * The squad (2026-11-11): up to {@link #SIZE} fighter friends a player has enlisted with a goat horn (crouch and use it on her;
 * again to let her go). The whistle calls every friend; the squad answers to the horn, and to trouble:
 * <ul>
 *   <li>Rally: their leader blows a goat horn. Squad members within {@link #HEAR} run to them, and for {@link #RALLY} ticks go for
 *   every monster within {@link #GUARD} of them.</li>
 *   <li>Bodyguard: a mob strikes their leader. Squad members within {@link #HEAR} (not ordered to sit) turn on it.</li>
 *   <li>Call to arms: the Order raids their leader's hearth. Squad members within {@link #MARCH} march home to defend it.</li>
 * </ul>
 * Only fighters (GirlArms.isFighter) who are their leader's friends ({@link Orders#AFFECTION}). Config "squad".
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Squad {
    public static final int SIZE = 4, HEAR = 96, GUARD = 16, MARCH = 160, RALLY = 2400;
    private static final String LEADER = "SquadLeader", RALLIED = "SquadRallyUntil";

    private Squad() {}

    @Nullable
    public static UUID leader(Mob mob) {
        return mob.getPersistentData().hasUUID(LEADER) ? mob.getPersistentData().getUUID(LEADER) : null;
    }

    public static boolean inSquadOf(Mob mob, Player p) {
        return p.getUUID().equals(leader(mob));
    }

    /** The loaded members of this player's squad within range of them. */
    public static List<AbstractMonsterGirl> members(ServerLevel level, Player p, double range) {
        return level.getEntitiesOfClass(AbstractMonsterGirl.class, p.getBoundingBox().inflate(range), g -> g.isAlive() && inSquadOf(g, p));
    }

    /** How many are enlisted to this player (all of them, near or far: kept in the journal). */
    public static int size(Player p) {
        return p.getData(com.linkyoshi90.monstergirls.registry.ModAttachments.JOURNAL).getList(LEADER, net.minecraft.nbt.Tag.TAG_STRING).size();
    }

    private static void roster(Player p, UUID girl, boolean add) {
        var journal = p.getData(com.linkyoshi90.monstergirls.registry.ModAttachments.JOURNAL);
        var list = journal.getList(LEADER, net.minecraft.nbt.Tag.TAG_STRING);
        list.removeIf(t -> t.getAsString().equals(girl.toString()));
        if (add) list.add(net.minecraft.nbt.StringTag.valueOf(girl.toString()));
        journal.put(LEADER, list);
        p.setData(com.linkyoshi90.monstergirls.registry.ModAttachments.JOURNAL, journal);
    }

    // --- enlisting ----------------------------------------------------------------------------------------------------

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        Player p = event.getEntity();
        if (!(event.getTarget() instanceof AbstractMonsterGirl girl) || !p.isShiftKeyDown() || event.getHand() != InteractionHand.MAIN_HAND
                || !event.getItemStack().is(Items.GOAT_HORN) || !MonsterGirlsConfig.squad()) return;
        if (girl.level() instanceof ServerLevel level) {
            if (inSquadOf(girl, p)) dismiss(level, girl, p);
            else enlist(level, girl, p);
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(girl.level().isClientSide));
    }

    /** She joins the player's squad. Whether she did (a grown fighter, their friend, in nobody's squad, and room for her). */
    public static boolean enlist(ServerLevel level, AbstractMonsterGirl girl, Player p) {
        String why = girl.isBaby() || !GirlArms.isFighter(girl) ? "not_fighter" : girl.getAffection(p) < Orders.AFFECTION ? "not_friend"
                : leader(girl) != null ? "taken" : size(p) >= SIZE ? "full" : null;
        if (why != null) {
            p.displayClientMessage(Component.translatable("message.monstergirls.squad." + why, girl.getDisplayName(), SIZE), true);
            return false;
        }
        girl.getPersistentData().putUUID(LEADER, p.getUUID());
        roster(p, girl.getUUID(), true);
        level.sendParticles(ParticleTypes.CRIT, girl.getX(), girl.getY() + girl.getBbHeight(), girl.getZ(), 12, 0.4, 0.3, 0.4, 0.1);
        p.displayClientMessage(Component.translatable("message.monstergirls.squad.enlisted", girl.getDisplayName(), size(p), SIZE).withStyle(ChatFormatting.GOLD), false);
        return true;
    }

    public static boolean dismiss(ServerLevel level, AbstractMonsterGirl girl, Player p) {
        if (!inSquadOf(girl, p)) return false;
        girl.getPersistentData().remove(LEADER);
        girl.getPersistentData().remove(RALLIED);
        roster(p, girl.getUUID(), false);
        p.displayClientMessage(Component.translatable("message.monstergirls.squad.dismissed", girl.getDisplayName()), true);
        return true;
    }

    // --- the horn -----------------------------------------------------------------------------------------------------

    @SubscribeEvent
    public static void onHorn(PlayerInteractEvent.RightClickItem event) {
        Player p = event.getEntity();
        if (!event.getItemStack().is(Items.GOAT_HORN) || p.isShiftKeyDown() || p.getCooldowns().isOnCooldown(Items.GOAT_HORN)
                || !(p.level() instanceof ServerLevel level) || !MonsterGirlsConfig.squad()) return;
        int n = rally(level, p);
        if (n > 0) p.displayClientMessage(Component.translatable("message.monstergirls.squad.rally", n), true);
    }

    /** The horn sounds: the squad within HEAR runs to their leader, ready to fight. How many answered. */
    public static int rally(ServerLevel level, Player p) {
        List<AbstractMonsterGirl> squad = members(level, p, HEAR);
        for (AbstractMonsterGirl g : squad) {
            if (g.isOrderedToSit()) g.setOrderedToSit(false);
            g.getPersistentData().putLong(RALLIED, level.getGameTime() + RALLY);
            g.getNavigation().moveTo(p, 1.4);
        }
        return squad.size();
    }

    public static boolean rallied(Mob mob) {
        return mob.level().getGameTime() < mob.getPersistentData().getLong(RALLIED);
    }

    // --- trouble ------------------------------------------------------------------------------------------------------

    /** A mob strikes the leader: the squad near turns on it. */
    @SubscribeEvent
    public static void onLeaderHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player p) || !(p.level() instanceof ServerLevel level) || !MonsterGirlsConfig.squad()) return;
        if (event.getSource().getEntity() instanceof Mob attacker && !(attacker instanceof AbstractMonsterGirl g && inSquadOf(g, p))) defend(level, p, attacker);
    }

    /** The squad near the leader goes for whoever struck them. How many did. */
    public static int defend(ServerLevel level, Player p, LivingEntity attacker) {
        int n = 0;
        for (AbstractMonsterGirl g : members(level, p, HEAR)) {
            if (g.isOrderedToSit() || g == attacker) continue;
            g.setTarget(attacker);
            if (g.getTarget() == attacker) n++;
        }
        return n;
    }

    @SubscribeEvent
    public static void onTick(EntityTickEvent.Post event) {
        if (event.getEntity().tickCount % 20 != 13 || !(event.getEntity() instanceof AbstractMonsterGirl g) || !(g.level() instanceof ServerLevel level)) return;
        UUID id = leader(g);
        if (id == null) return;
        Player p = level.getPlayerByUUID(id);
        if (p == null) return;
        duty(level, g, p);
    }

    /** Once a second: rallied, she takes on the monsters round her leader; with a raid on her leader's hearth, she marches home. What she is doing. */
    public static String duty(ServerLevel level, AbstractMonsterGirl g, Player p) {
        if (!MonsterGirlsConfig.squad()) return "off";
        for (Order.Raid raid : Order.raids(level).all()) {               // a crusade at home comes first
            if (!raid.player().equals(p.getUUID()) || g.blockPosition().distSqr(raid.hearth()) > (double) MARCH * MARCH) continue;
            if (g.isOrderedToSit()) g.setOrderedToSit(false);
            OrderSister foe = null;
            for (OrderSister s : Order.standing(level, raid)) if (foe == null || s.distanceToSqr(g) < foe.distanceToSqr(g)) foe = s;
            if (foe != null && foe.distanceToSqr(g) < GUARD * GUARD) g.setTarget(foe);
            else if (g.blockPosition().distSqr(raid.hearth()) > 8 * 8) g.getNavigation().moveTo(raid.hearth().getX() + 0.5, raid.hearth().getY(), raid.hearth().getZ() + 0.5, 1.3);
            return "defending";
        }
        if (rallied(g)) {
            if (g.getTarget() == null || !g.getTarget().isAlive()) {
                Mob foe = null;
                for (Mob m : level.getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(GUARD), m -> m instanceof Enemy && m.isAlive() && !(m instanceof Bonded))) {
                    if (foe == null || m.distanceToSqr(p) < foe.distanceToSqr(p)) foe = m;
                }
                if (foe != null) g.setTarget(foe);
                else if (g.distanceToSqr(p) > 6 * 6) g.getNavigation().moveTo(p, 1.3);
            }
            return "rallied";
        }
        return "idle";
    }

    /** The hearth a raid is on for this player, or null (for tests). */
    @Nullable
    public static BlockPos raidedHearth(ServerLevel level, Player p) {
        for (Order.Raid raid : Order.raids(level).all()) if (raid.player().equals(p.getUUID())) return raid.hearth();
        return null;
    }
}
