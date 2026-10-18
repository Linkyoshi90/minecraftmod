package com.linkyoshi90.harpies.entity.ai;

import com.linkyoshi90.harpies.entity.AbstractHarpy;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * Swoops in on a nearby player, takes one item and makes off with it.
 * HAND: only what they're holding out (a sparrow grabbing the bread you're dangling at her).
 * INVENTORY: anything from their pack, preferring things she likes (a crow's pickpocketing).
 */
public class SnatchGoal extends Goal {
    public enum From { HAND, INVENTORY }

    private final AbstractHarpy harpy;
    private final double speed;
    private final From from;
    private final int chance;
    private final int minCooldown;
    private final Predicate<Player> victimFilter;
    private long nextSnatchTime;
    private Player victim;
    private int ticks;
    private boolean fleeing;

    /**
     * @param chance       on average one attempt per this many ticks while a victim is around
     * @param minCooldown  ticks after a snatch before she tries again (up to twice this)
     * @param victimFilter extra condition on who she'll rob (e.g. not her friends)
     */
    public SnatchGoal(AbstractHarpy harpy, double speed, From from, int chance, int minCooldown, Predicate<Player> victimFilter) {
        this.harpy = harpy;
        this.speed = speed;
        this.from = from;
        this.chance = chance;
        this.minCooldown = minCooldown;
        this.victimFilter = victimFilter;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean isVictim(Player player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator() && this.victimFilter.test(player)
                && this.pickLoot(player) >= 0;
    }

    /** Inventory slot to take from, or -1. */
    private int pickLoot(Player player) {
        if (this.from == From.HAND) {
            int slot = player.getInventory().selected;
            return this.harpy.wantsItem(player.getInventory().getItem(slot)) ? slot : -1;
        }
        List<Integer> liked = new ArrayList<>(), any = new ArrayList<>();
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().items.get(i);
            if (stack.isEmpty()) continue;
            any.add(i);
            if (this.harpy.wantsItem(stack)) liked.add(i);
        }
        List<Integer> pool = liked.isEmpty() ? any : liked;
        return pool.isEmpty() ? -1 : pool.get(this.harpy.getRandom().nextInt(pool.size()));
    }

    @Override
    public boolean canUse() {
        if (this.harpy.isHoldingSomething() || this.harpy.level().getGameTime() < this.nextSnatchTime) return false;
        if (this.harpy.getRandom().nextInt(reducedTickDelay(this.chance)) != 0) return false;
        if (!EventHooks.canEntityGrief(this.harpy.level(), this.harpy)) return false;
        this.victim = this.harpy.level().getNearestPlayer(this.harpy.getX(), this.harpy.getY(), this.harpy.getZ(), 10.0,
                e -> e instanceof Player p && this.isVictim(p));
        return this.victim != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.ticks > 240) return false;
        if (this.fleeing) return !this.harpy.getNavigation().isDone();
        return this.isVictim(this.victim) && this.harpy.distanceToSqr(this.victim) < 16 * 16;
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.fleeing = false;
        this.harpy.getNavigation().moveTo(this.victim, this.speed);
    }

    @Override
    public void stop() {
        this.victim = null;
        this.harpy.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.ticks++;
        if (this.fleeing) return;
        this.harpy.getLookControl().setLookAt(this.victim, 30.0F, 30.0F);
        if (this.harpy.distanceToSqr(this.victim) > 2.0 * 2.0) {
            if (this.ticks % 10 == 0) this.harpy.getNavigation().moveTo(this.victim, this.speed);
            return;
        }
        int slot = this.pickLoot(this.victim);
        if (slot < 0) return;
        this.harpy.takeIntoHands(this.victim.getInventory().removeItem(slot, 1));
        this.harpy.playSound(this.harpy.getChirpSound(), 1.0F, this.harpy.getVoicePitch());
        this.nextSnatchTime = this.harpy.level().getGameTime() + this.minCooldown + this.harpy.getRandom().nextInt(this.minCooldown + 1);
        this.fleeing = true;
        Vec3 away = DefaultRandomPos.getPosAway(this.harpy, 16, 7, this.victim.position());
        if (away == null) {
            away = this.harpy.position().add(this.harpy.position().subtract(this.victim.position()).normalize().scale(12)).add(0, 3, 0);
        }
        this.harpy.getNavigation().moveTo(away.x, away.y, away.z, this.speed * 1.4);
    }
}
