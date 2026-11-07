package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Kinship;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * What a player has told a monster girl with a stick (harpies, anurans and every girl family alike).
 *
 * <ul>
 *   <li>Right-click: where to be. Sit, patrol (roam around the spot where the order was given), follow (stay
 *   around the player) or free roam. Anyone can make her sit and get up again; the rest needs her to be fond of
 *   you ({@link #AFFECTION}).</li>
 *   <li>Sneak + right-click: when to fight. Hostile (goes for hostile mobs), neutral (defends herself and you),
 *   self-defence (only hits back) or peaceful (never fights). Until someone sets one she follows her instincts.</li>
 * </ul>
 * Companions (two who are both fond of the same player) never hurt or target each other, and never their player.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public final class Orders {
    public static final int AFFECTION = 10;
    public static final double PATROL_RADIUS = 10.0;
    public static final double FOLLOW_RADIUS = 7.0;

    public enum Command { FREE_ROAM, SIT, PATROL, FOLLOW }

    public enum Stance { NATURAL, HOSTILE, NEUTRAL, SELF_DEFENCE, PEACEFUL }

    private Command command = Command.FREE_ROAM;
    private Stance stance = Stance.NATURAL;
    @Nullable
    private UUID commander;
    @Nullable
    private BlockPos anchor;

    /** Where she is told to be; SIT follows the mob's own sitting flag (other things can make her get up). */
    public <T extends PathfinderMob & Bonded> Command command(T mob) {
        if (mob.isOrderedToSit()) return Command.SIT;
        return this.command == Command.SIT ? Command.FREE_ROAM : this.command;
    }

    /** The stored order as it is (for mobs whose sitting is not a Bonded flag: befriended vanilla mobs). */
    public Command rawCommand() {
        return this.command;
    }

    public Stance stance() {
        return this.stance;
    }

    @Nullable
    public BlockPos anchor() {
        return this.anchor;
    }

    @Nullable
    public UUID commanderId() {
        return this.commander;
    }

    @Nullable
    public Player commander(Entity mob) {
        return this.commander == null ? null : mob.level().getPlayerByUUID(this.commander);
    }

    /** The centre she keeps near (the patrol spot or her player), or null when she is free (or sitting). */
    @Nullable
    public <T extends PathfinderMob & Bonded> Vec3 centre(T mob) {
        return switch (this.command(mob)) {
            case PATROL -> this.anchor == null ? null : Vec3.atBottomCenterOf(this.anchor);
            case FOLLOW -> {
                Player p = this.commander(mob);
                if (p != null && !p.isSpectator() && p.level() == mob.level()) {
                    this.anchor = null;
                    yield p.position();
                }
                if (this.anchor == null) this.anchor = mob.blockPosition();      // her player is away (logged out, another dimension): she waits here
                yield Vec3.atBottomCenterOf(this.anchor);
            }
            default -> null;
        };
    }

    public void set(Command command, @Nullable Player by, @Nullable BlockPos anchor) {
        this.command = command;
        if (by != null) this.commander = by.getUUID();
        this.anchor = anchor;
    }

    public void setStance(Stance stance, @Nullable Player by) {
        this.stance = stance;
        if (by != null) this.commander = by.getUUID();
    }

    /** Told to sit or patrol: she stays put, so no wandering off after her player, her flock, home, presents or bed. */
    public static boolean holdsPlace(Entity e) {
        if (!(e instanceof Bonded b)) return false;
        Command c = b.getOrders().command;
        return b.isOrderedToSit() || c == Command.PATROL;
    }

    /** Under any order but free roam: her own following (flock, home, family, her player) gives way to the order. */
    public static boolean commanded(Entity e) {
        return holdsPlace(e) || e instanceof Bonded b && b.getOrders().command == Command.FOLLOW;
    }

    /** Her order as it stands, for any Bonded (the generic {@link #command} needs a PathfinderMob type). */
    public static Command current(Bonded b) {
        if (b.isOrderedToSit()) return Command.SIT;
        Command c = b.getOrders().command;
        return c == Command.SIT ? Command.FREE_ROAM : c;
    }

    // --- the whistle, sneaking: orders for everyone at once ------------------------------------------------------

    /** Everyone within reach who takes this player's orders (grown friends). */
    public static java.util.List<net.minecraft.world.entity.Mob> squad(net.minecraft.server.level.ServerLevel level, Player player, double reach) {
        java.util.List<net.minecraft.world.entity.Mob> out = HomeLife.friendsOf(level, player, player.getBoundingBox().inflate(reach));
        out.removeIf(m -> !(m instanceof PathfinderMob) || m.distanceToSqr(player) > reach * reach);
        return out;
    }

    /** What a group order does next: everyone stays put, or, once they all sit already, everyone follows. */
    public static Command nextGroupOrder(java.util.List<net.minecraft.world.entity.Mob> squad) {
        return !squad.isEmpty() && squad.stream().allMatch(m -> ((Bonded) m).isOrderedToSit()) ? Command.FOLLOW : Command.SIT;
    }

    /** Gives the whole squad one order (sit, or follow this player). */
    public static void orderAll(java.util.List<net.minecraft.world.entity.Mob> squad, Command order, Player player) {
        for (net.minecraft.world.entity.Mob m : squad) {
            Bonded b = (Bonded) m;
            b.getOrders().set(order, player, null);
            b.setOrderedToSit(order == Command.SIT);
            m.getNavigation().stop();
        }
    }

    // --- the stick ----------------------------------------------------------------------------------------------

    /** Right-click with a stick (server side): the next order, or the next stance when sneaking. */
    public static <T extends PathfinderMob & Bonded> InteractionResult onStick(T mob, Orders orders, Player player) {
        boolean fond = mob.getAffection(player) >= AFFECTION;
        if (player.isShiftKeyDown()) {
            if (!fond) {
                player.displayClientMessage(Component.translatable("message.monstergirls.order.refused", mob.getDisplayName()), true);
                return InteractionResult.SUCCESS;
            }
            Stance next = switch (orders.stance) {
                case NATURAL, PEACEFUL -> Stance.HOSTILE;
                case HOSTILE -> Stance.NEUTRAL;
                case NEUTRAL -> Stance.SELF_DEFENCE;
                case SELF_DEFENCE -> Stance.PEACEFUL;
            };
            orders.setStance(next, player);
            if (!orders.allows(mob, mob.getTarget())) mob.setTarget(null);
            player.displayClientMessage(Component.translatable("message.monstergirls.stance." + next.name().toLowerCase(), mob.getDisplayName()), true);
            return InteractionResult.SUCCESS;
        }
        Command now = orders.command(mob);
        Command next = switch (now) {
            case FREE_ROAM -> Command.SIT;
            case SIT -> fond ? Command.PATROL : Command.FREE_ROAM;   // a stranger can only make her sit and get up
            case PATROL -> Command.FOLLOW;
            case FOLLOW -> Command.FREE_ROAM;
        };
        orders.set(next, player, next == Command.PATROL ? mob.blockPosition() : null);
        mob.setOrderedToSit(next == Command.SIT);
        String key = switch (next) {
            case SIT -> "message.monstergirls.sits";
            case PATROL -> com.linkyoshi90.monstergirls.entity.girl.FarmhandGoal.hasFarmland(mob.level(), mob.blockPosition())
                    && !(mob instanceof com.linkyoshi90.monstergirls.entity.girl.Merfolk) ? "message.monstergirls.order.patrol_farm"
                    : mob.level() instanceof net.minecraft.server.level.ServerLevel sl && com.linkyoshi90.monstergirls.entity.girl.Chores.hasWork(sl, mob, mob.blockPosition())
                    ? "message.monstergirls.order.patrol_chores" : "message.monstergirls.order.patrol";
            case FOLLOW -> "message.monstergirls.order.follow";
            case FREE_ROAM -> now == Command.SIT ? "message.monstergirls.stands" : "message.monstergirls.order.free";
        };
        player.displayClientMessage(Component.translatable(key, mob.getDisplayName()), true);
        return InteractionResult.SUCCESS;
    }

    // --- fighting -----------------------------------------------------------------------------------------------

    /** Two monster girls (or a girl and a player, or a vanilla friend) on the same side: both fond of one player. */
    public static boolean companions(Entity a, @Nullable Entity b) {
        if (b == null || a == b || !(a instanceof Bonded ba)) return false;
        if (b instanceof Player p) return ba.getAffection(p) >= AFFECTION;
        UUID friend = Kinship.friendOf(b);
        for (Player p : a.level().players()) {
            if (ba.getAffection(p) < AFFECTION) continue;
            if (b instanceof Bonded bb && bb.getAffection(p) >= AFFECTION) return true;
            if (p.getUUID().equals(friend)) return true;
        }
        return false;
    }

    /** Whether her current stance lets her fight this one (as a target she picks herself, or one handed to her). */
    public boolean allows(LivingEntity mob, @Nullable LivingEntity target) {
        if (target == null) return true;
        if (companions(mob, target)) return false;
        if (this.stance == Stance.NATURAL) return true;
        return this.wants(mob, target);
    }

    /** What she goes for under her stance. Hostile mobs, wild girls out for her player, and whoever hurts her or her player. */
    public boolean wants(LivingEntity mob, LivingEntity target) {
        if (this.stance == Stance.NATURAL || this.stance == Stance.PEACEFUL || !target.isAlive() || companions(mob, target)) return false;
        boolean hitMe = target == mob.getLastHurtByMob();
        if (this.stance == Stance.SELF_DEFENCE) return hitMe;
        Player p = this.commander(mob);
        boolean hitPlayer = p != null && target == p.getLastHurtByMob();
        if (this.stance == Stance.NEUTRAL) return hitMe || hitPlayer;
        boolean hostile = target instanceof Enemy || (p != null && target instanceof AbstractMonsterGirl g && g.isHostileTo(p));
        return hitMe || hitPlayer || hostile;
    }

    /**
     * No friendly fire: nothing a companion does hurts another companion, and her stray shots (a fireball) never hit
     * her player. Her deliberate pokes at the player (a swat, a clumsy kick) still land, as before.
     */
    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (attacker == null || attacker.level().isClientSide || !companions(attacker, event.getEntity())) return;
        if (!(event.getEntity() instanceof Player) || event.getSource().getDirectEntity() != attacker) event.setCanceled(true);
    }

    /** A player's own explosions (TNT they lit, a firework, a bed in the Nether) spare their friends. */
    @SubscribeEvent
    public static void onFriendlyBlast(LivingIncomingDamageEvent event) {
        if (event.getSource().getEntity() instanceof Player p && !p.level().isClientSide && event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)
                && event.getEntity() instanceof Bonded b && b.getAffection(p) >= AFFECTION) event.setCanceled(true);
    }

    /** A player's arrows and tridents fly past their friends, on to whatever is behind them. Snowballs and potions still land. */
    @SubscribeEvent
    public static void onFriendlyArrow(net.neoforged.neoforge.event.entity.ProjectileImpactEvent event) {
        if (event.getRayTraceResult() instanceof net.minecraft.world.phys.EntityHitResult hit
                && event.getProjectile() instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow
                && arrow.getOwner() instanceof Player p && !p.level().isClientSide
                && hit.getEntity() instanceof Bonded b && b.getAffection(p) >= AFFECTION) event.setCanceled(true);
    }

    // --- saving -------------------------------------------------------------------------------------------------

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Command", this.command.name());
        tag.putString("Stance", this.stance.name());
        if (this.commander != null) tag.putUUID("Commander", this.commander);
        if (this.anchor != null) tag.put("Anchor", NbtUtils.writeBlockPos(this.anchor));
        return tag;
    }

    public void load(CompoundTag tag) {
        try {
            if (tag.contains("Command")) this.command = Command.valueOf(tag.getString("Command"));
            if (tag.contains("Stance")) this.stance = Stance.valueOf(tag.getString("Stance"));
        } catch (IllegalArgumentException ignored) {
            // an order from a newer version: keep the defaults
        }
        this.commander = tag.hasUUID("Commander") ? tag.getUUID("Commander") : null;
        this.anchor = NbtUtils.readBlockPos(tag, "Anchor").orElse(null);
    }
}
