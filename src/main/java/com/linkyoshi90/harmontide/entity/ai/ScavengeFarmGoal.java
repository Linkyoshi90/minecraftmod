package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.entity.CrowHarpy;
import com.linkyoshi90.harmontide.registry.ModSounds;
import com.linkyoshi90.harmontide.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

import java.util.EnumSet;

/**
 * Murders gather on farms: a crow finds a field (or answers another crow's call to one), hops around between
 * the crops and now and then pecks a ripe one down to a seedling. A scarecrow, any carved pumpkin or jack
 * o'lantern near the field, keeps them all away.
 */
public class ScavengeFarmGoal extends Goal {
    private static final int SCARECROW_RANGE = 8;

    private final CrowHarpy crow;
    private final double speed;
    private BlockPos field;
    private BlockPos hopTarget;
    private int ticks;
    private int duration;
    private long nextRaidTime;

    public ScavengeFarmGoal(CrowHarpy crow, double speed) {
        this.crow = crow;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public static boolean isGuardedByScarecrow(Level level, BlockPos crop) {
        return BlockPos.findClosestMatch(crop, SCARECROW_RANGE, 4, p -> level.getBlockState(p).is(ModTags.CROW_HARPY_SCARECROWS)).isPresent();
    }

    private boolean isRaidableCrop(BlockPos pos) {
        Level level = this.crow.level();
        return level.getBlockState(pos).is(BlockTags.CROPS) && this.crow.isSafeSpot(pos);
    }

    @Override
    public boolean canUse() {
        if (!this.crow.level().isDay() || this.crow.isHoldingSomething()) return false;
        BlockPos called = this.crow.takeScavengeCall();
        if (called != null) {
            this.field = called;
        } else {
            if (this.crow.level().getGameTime() < this.nextRaidTime || this.crow.getRandom().nextInt(reducedTickDelay(800)) != 0) return false;
            this.field = BlockPos.findClosestMatch(this.crow.blockPosition(), 20, 6, this::isRaidableCrop).orElse(null);
            if (this.field == null) return false;
        }
        if (isGuardedByScarecrow(this.crow.level(), this.field)) {
            this.field = null;
            this.nextRaidTime = this.crow.level().getGameTime() + 2400;
            return false;
        }
        if (called == null) this.crow.callMurder(this.field);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticks < this.duration && this.crow.level().isDay() && !this.crow.isHoldingSomething();
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.duration = 600 + this.crow.getRandom().nextInt(600);
        this.hopTarget = this.field;
        this.crow.caw(30);
        this.moveTo(this.hopTarget);
    }

    @Override
    public void stop() {
        this.field = null;
        this.hopTarget = null;
        this.nextRaidTime = this.crow.level().getGameTime() + 2400 + this.crow.getRandom().nextInt(2400);
        this.crow.getNavigation().stop();
    }

    private void moveTo(BlockPos pos) {
        this.crow.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, this.speed);
    }

    @Override
    public void tick() {
        this.ticks++;
        if (this.crow.distanceToSqr(Vec3.atBottomCenterOf(this.hopTarget)) > 1.5 * 1.5) {
            if (this.crow.getNavigation().isDone()) this.moveTo(this.hopTarget);
            return;
        }
        this.crow.getLookControl().setLookAt(Vec3.atCenterOf(this.hopTarget));
        if (this.ticks % 50 != 0) return;
        this.peck(this.hopTarget);
        // Hop to another crop nearby.
        BlockPos next = this.hopTarget.offset(this.crow.getRandom().nextInt(7) - 3, 0, this.crow.getRandom().nextInt(7) - 3);
        if (this.isRaidableCrop(next)) {
            this.hopTarget = next;
            this.moveTo(next);
        }
    }

    private void peck(BlockPos pos) {
        Level level = this.crow.level();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CropBlock crop) || !crop.isMaxAge(state)) return;
        if (this.crow.getRandom().nextInt(3) != 0 || !EventHooks.canEntityGrief(level, this.crow)) return;
        level.setBlock(pos, crop.getStateForAge(0), 2);
        this.crow.heal(1.0F);
        this.crow.playSound(ModSounds.CROW_EAT.get(), 1.0F, ModSounds.wobble(this.crow));
        if (level instanceof ServerLevel server) {
            server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5,
                    10, 0.25, 0.2, 0.25, 0.05);
        }
        this.crow.startTimedAction(AbstractHarpy.Action.CAWING, 20);
    }

    /** For tests: the field this crow is currently raiding, if any. */
    public BlockPos getField() {
        return this.field;
    }
}
