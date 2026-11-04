package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.entity.Orders;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

/**
 * A girl (harpies and anurans too) told to patrol (stick) by someone she is fond of works the fields in her patrol area:
 * she harvests ripe crops, replants them from the harvest (like a farmer villager) and puts the rest into a chest or
 * barrel in the area (or leaves it at her feet). Flora girls also give growing crops a nudge now and then. Obeys
 * mobGriefing, as farmer villagers do. Merfolk stay in the water.
 */
public class FarmhandGoal<T extends net.minecraft.world.entity.PathfinderMob & com.linkyoshi90.monstergirls.entity.Bonded> extends Goal {
    private final T girl;
    @Nullable
    private BlockPos crop;
    private int cooldown;
    private int ticks;

    public FarmhandGoal(T girl) {
        this.girl = girl;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    /** On duty: patrolling for a player she is fond of (who is in this world), grown, not a merfolk. */
    public boolean onDuty() {
        Orders o = this.girl.getOrders();
        net.minecraft.world.entity.player.Player boss = o.commander(this.girl);
        return o.command(this.girl) == Orders.Command.PATROL && o.anchor() != null && boss != null && this.girl.getAffection(boss) >= Orders.AFFECTION
                && !this.girl.isBaby() && !(this.girl instanceof Merfolk) && this.girl.getTarget() == null
                && this.girl.level() instanceof ServerLevel level && net.neoforged.neoforge.event.EventHooks.canEntityGrief(level, this.girl);
    }

    /** Whether there is farmland around a patrol spot (the patrol order says she will tend it). */
    public static boolean hasFarmland(net.minecraft.world.level.Level level, BlockPos anchor) {
        int r = (int) Orders.PATROL_RADIUS;
        for (BlockPos p : BlockPos.betweenClosed(anchor.offset(-r, -3, -r), anchor.offset(r, 2, r))) {
            if (level.getBlockState(p).getBlock() instanceof net.minecraft.world.level.block.FarmBlock) return true;
        }
        return false;
    }

    private static boolean ripe(BlockState s) {
        return s.getBlock() instanceof CropBlock c && c.isMaxAge(s);
    }

    @Override
    public boolean canUse() {
        if (--this.cooldown > 0 || !this.onDuty()) return false;
        this.cooldown = 40;
        BlockPos anchor = this.girl.getOrders().anchor();
        int r = (int) Orders.PATROL_RADIUS;
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.betweenClosed(anchor.offset(-r, -2, -r), anchor.offset(r, 2, r))) {
            BlockState s = this.girl.level().getBlockState(p);
            if (!ripe(s)) {
                if ((this.girl instanceof Flora || Roles.is(this.girl, Roles.Role.FARMHAND)) && s.getBlock() instanceof CropBlock c
                        && this.girl.getRandom().nextInt(400) == 0 && this.girl.level() instanceof ServerLevel level) {
                    c.performBonemeal(level, level.random, p, s);           // a flora girl's touch: growing crops get a nudge
                }
                continue;
            }
            double d = p.distSqr(this.girl.blockPosition());
            if (d < bestDist) {
                bestDist = d;
                best = p.immutable();
            }
        }
        this.crop = best;
        return best != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.crop != null && this.ticks < 300 && this.onDuty() && ripe(this.girl.level().getBlockState(this.crop));
    }

    @Override
    public void start() {
        this.ticks = 0;
    }

    @Override
    public void stop() {
        this.crop = null;
        this.girl.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.ticks++;
        if (this.crop == null) return;
        this.girl.getLookControl().setLookAt(this.crop.getX() + 0.5, this.crop.getY() + 0.5, this.crop.getZ() + 0.5);
        if (this.girl.distanceToSqr(this.crop.getX() + 0.5, this.crop.getY(), this.crop.getZ() + 0.5) > 2.5 * 2.5) {
            if (this.ticks % 10 == 1) this.girl.getNavigation().moveTo(this.crop.getX() + 0.5, this.crop.getY(), this.crop.getZ() + 0.5, 1.0);
            return;
        }
        if (this.girl.level() instanceof ServerLevel level) this.harvest(level, this.crop);
        this.crop = null;
    }

    /** Pick the crop, replant it from its own seed, and store the rest. */
    public void harvest(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CropBlock crop) || !crop.isMaxAge(state)) return;
        List<ItemStack> drops = Block.getDrops(state, level, pos, null, this.girl, ItemStack.EMPTY);
        ItemStack seed = state.getBlock().getCloneItemStack(level, pos, state);
        boolean replant = false;
        for (ItemStack d : drops) {
            if (!replant && ItemStack.isSameItem(d, seed)) {
                d.shrink(1);
                replant = true;
            }
        }
        level.levelEvent(2001, pos, Block.getId(state));                    // the break particles and sound
        if (replant) level.setBlockAndUpdate(pos, crop.getStateForAge(0));
        else level.removeBlock(pos, false);
        level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.NEUTRAL, 0.8F, 1.0F);
        for (ItemStack d : drops) {
            if (d.isEmpty()) continue;
            ItemStack left = this.storeAway(level, d);
            if (!left.isEmpty()) this.girl.spawnAtLocation(left);
        }
    }

    /** Into a fridge if it is food, else a chest or barrel (any container) in her patrol area, if there is room (Chores.store). */
    private ItemStack storeAway(ServerLevel level, ItemStack stack) {
        return Chores.store(level, this.girl.getOrders().anchor(), stack);
    }
}
