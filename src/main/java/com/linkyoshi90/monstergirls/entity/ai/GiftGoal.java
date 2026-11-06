package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.GiftGiver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * A monster girl who is very fond of someone ({@link Bonded#GIFT_AFFECTION}) now and then brings them a present of her
 * kind ({@link GiftGiver#rollGift}) and slips it straight into their inventory. A species that
 * {@link GiftGiver#storesGiftsAway() stores gifts away} (the crow) instead drops it into the nearest chest,
 * barrel or hopper that already holds that kind of item, if there is one close by.
 */
public class GiftGoal<T extends PathfinderMob & Bonded & GiftGiver> extends Goal {
    public static final double PLAYER_RANGE = 16.0;
    public static final int CONTAINER_RANGE = 12;
    private static final double REACH_SQR = 2.5 * 2.5;
    private static final int GIVE_UP_TICKS = 400;
    private static final int RETRY_TICKS = 200;

    private final T harpy;
    @Nullable
    private Player player;
    @Nullable
    private BlockPos container;
    private ItemStack gift = ItemStack.EMPTY;
    private int ticks;

    public GiftGoal(T harpy) {
        this.harpy = harpy;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.harpy.tickCount % 41 != 0 || !this.harpy.isGiftDue() || this.harpy.isBaby() || com.linkyoshi90.monstergirls.entity.Orders.holdsPlace(this.harpy)
                || this.harpy.getTarget() != null) return false;
        Player best = null;
        for (Player p : this.harpy.level().players()) {
            if (p.isSpectator() || p.isSleeping() || this.harpy.getAffection(p) < Bonded.GIFT_AFFECTION) continue;
            if (p.distanceToSqr(this.harpy) > PLAYER_RANGE * PLAYER_RANGE) continue;
            if (best == null || p.distanceToSqr(this.harpy) < best.distanceToSqr(this.harpy)) best = p;
        }
        if (best == null) return false;
        ItemStack gift = this.harpy instanceof com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl girl && girl.isHealer()
                && girl.getRandom().nextInt(5) == 0 ? new ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.HEALING_BALM.get()) : this.harpy.rollGift(best);
        if (!gift.isEmpty() && this.harpy.getRandom().nextInt(com.linkyoshi90.monstergirls.entity.Journal.FLOWER_GIFT) == 0) {   // she knows they press flowers
            ItemStack flower = com.linkyoshi90.monstergirls.entity.Journal.missingFlower(best, this.harpy.getRandom());
            if (!flower.isEmpty()) gift = flower;
        }
        if (gift.isEmpty()) {
            this.harpy.scheduleNextGift();
            return false;
        }
        this.player = best;
        this.gift = gift;
        this.container = this.harpy.storesGiftsAway() ? findContainerWith(this.harpy.level(), this.harpy.blockPosition(), gift) : null;
        return true;
    }

    /** The closest chest, barrel or hopper nearby that already holds this kind of item and has room for more. */
    @Nullable
    public static BlockPos findContainerWith(Level level, BlockPos origin, ItemStack stack) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-CONTAINER_RANGE, -4, -CONTAINER_RANGE), origin.offset(CONTAINER_RANGE, 4, CONTAINER_RANGE))) {
            var be = level.getBlockEntity(pos);
            if (!(be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity || be instanceof HopperBlockEntity)) continue;
            Container container = HopperBlockEntity.getContainerAt(level, pos);
            if (container == null || !container.hasAnyMatching(s -> ItemStack.isSameItemSameComponents(s, stack)) || !hasRoom(container, stack)) continue;
            double dist = pos.distSqr(origin);
            if (dist < bestDist) {
                bestDist = dist;
                best = pos.immutable();
            }
        }
        return best;
    }

    private static boolean hasRoom(Container container, ItemStack stack) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack slot = container.getItem(i);
            if (slot.isEmpty() || ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() < slot.getMaxStackSize()) return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.gift.isEmpty() && this.player != null && this.player.isAlive() && this.ticks < GIVE_UP_TICKS
                && this.harpy.getTarget() == null && !this.harpy.isOrderedToSit();
    }

    @Override
    public void start() {
        this.ticks = 0;
    }

    @Override
    public void stop() {
        // interrupted on the way (a dance, a scare...): she tries again shortly
        if (!this.gift.isEmpty()) this.harpy.setNextGiftTime(this.harpy.level().getGameTime() + RETRY_TICKS);
        this.gift = ItemStack.EMPTY;
        this.player = null;
        this.container = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        this.ticks++;
        Vec3 target = this.container != null ? Vec3.atCenterOf(this.container).add(0.0, 0.8, 0.0) : this.player.position();
        this.harpy.getLookControl().setLookAt(target.x, target.y, target.z);
        if (this.harpy.distanceToSqr(target) > REACH_SQR) {
            if (this.ticks % 10 == 1) this.harpy.getNavigation().moveTo(target.x, target.y, target.z, 1.1);
            return;
        }
        this.harpy.getNavigation().stop();
        this.deliver();
    }

    private void deliver() {
        if (!(this.harpy.level() instanceof ServerLevel level)) return;
        ItemStack rest = this.gift.copy();
        if (this.container != null) {
            Container container = HopperBlockEntity.getContainerAt(level, this.container);
            if (container != null) rest = HopperBlockEntity.addItem(null, container, rest, Direction.UP);
        }
        if (!rest.isEmpty() && this.player.distanceToSqr(this.harpy) < 4.0 * 4.0) {
            this.player.getInventory().add(rest);   // what doesn't fit stays in the stack
        }
        if (!rest.isEmpty()) this.harpy.spawnAtLocation(rest);
        Vec3 at = this.container != null ? Vec3.atCenterOf(this.container) : this.player.position();
        level.sendParticles(ParticleTypes.HEART, at.x, at.y + 1.0, at.z, 3, 0.3, 0.2, 0.3, 0.0);
        this.harpy.playSound(this.harpy.getChirpSound(), 1.0F, this.harpy.getVoicePitch() * 1.1F);
        this.gift = ItemStack.EMPTY;
        this.harpy.scheduleNextGift();
        if (com.linkyoshi90.monstergirls.entity.Missing.activeStage(this.harpy, this.player) > 0) this.harpy.setNextGiftTime(this.harpy.level().getGameTime() + 1200);   // she has a lot saved up
    }
}
