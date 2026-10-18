package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Looks for a birdbath (water cauldron), a one-deep puddle of water or a patch of sand/loose dirt nearby,
 * goes there and splashes or dust-bathes for a few seconds. Then waits a couple of minutes before the next bath.
 */
public class BatheGoal extends MoveToBlockGoal {
    private static final int BATH_TICKS = 80; // two loops of animation.sparrow_harpy.bathe

    private final AbstractHarpy harpy;
    private int bathTicks;
    private int ticks;

    public BatheGoal(AbstractHarpy harpy, double speed) {
        super(harpy, speed, 12, 3);
        this.harpy = harpy;
    }

    @Override
    public boolean canUse() {
        return this.harpy.isBathReady() && this.harpy.getAction() == AbstractHarpy.Action.NONE && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        // Give up if she can't get into it within 20 s.
        return this.bathTicks < BATH_TICKS && (this.bathTicks > 0 || this.ticks < 400) && super.canContinueToUse();
    }

    @Override
    public void start() {
        super.start();
        this.bathTicks = 0;
        this.ticks = 0;
    }

    @Override
    public void stop() {
        if (this.bathTicks >= BATH_TICKS) this.harpy.onBathed(this.blockPos, this.harpy.level().getBlockState(this.blockPos));
        super.stop();
        this.harpy.setAction(AbstractHarpy.Action.NONE);
        // A proper bath lasts her a few minutes; if she was interrupted on the way (or mid-splash), try again soon.
        this.harpy.startBathCooldown(this.bathTicks >= BATH_TICKS ? 2400 + this.harpy.getRandom().nextInt(2400) : 100);
    }

    @Override
    public double acceptedDistance() {
        return 1.5; // she floats a little low in water
    }

    @Override
    public void tick() {
        super.tick();
        this.ticks++;
        if (!this.isInBath()) {
            this.harpy.setAction(AbstractHarpy.Action.NONE);
            // Pathfinding stops a block short of cauldrons and puddles; flutter the last bit directly.
            if (this.harpy.distanceToSqr(this.blockPos.getCenter()) < 3.5 * 3.5) {
                this.harpy.getMoveControl().setWantedPosition(this.blockPos.getX() + 0.5, this.blockPos.getY() + 1.3, this.blockPos.getZ() + 0.5, this.speedModifier);
            }
            return;
        }

        this.harpy.getNavigation().stop();
        this.harpy.setAction(AbstractHarpy.Action.BATHING);
        this.bathTicks++;

        if (!(this.harpy.level() instanceof ServerLevel server)) return;
        BlockState state = server.getBlockState(this.blockPos);
        boolean wet = state.is(Blocks.WATER_CAULDRON) || !state.getFluidState().isEmpty();
        double x = this.harpy.getX(), y = this.harpy.getY(), z = this.harpy.getZ();
        if (this.bathTicks % 4 == 0) {
            if (wet) {
                server.sendParticles(ParticleTypes.SPLASH, x, y + 0.4, z, 8, 0.3, 0.1, 0.3, 0.1);
            } else {
                server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), x, y + 0.1, z, 6, 0.3, 0.1, 0.3, 0.05);
            }
        }
        if (this.bathTicks % 10 == 0) {
            float pitch = 1.3F + this.harpy.getRandom().nextFloat() * 0.3F;
            this.harpy.playSound(wet ? SoundEvents.GENERIC_SPLASH : state.getSoundType(server, this.blockPos, this.harpy).getHitSound(), 0.4F, pitch);
        }
    }

    /** Sitting in the cauldron / puddle, or on top of the sand, rather than just next to it. */
    public boolean isInBath() {
        double dx = this.harpy.getX() - (this.blockPos.getX() + 0.5), dz = this.harpy.getZ() - (this.blockPos.getZ() + 0.5);
        double dy = this.harpy.getY() - this.blockPos.getY();
        return dx * dx + dz * dz < 0.45 * 0.45 && dy > -0.1 && dy < 1.6;
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        if (!level.isEmptyBlock(pos.above()) || !this.harpy.isSafeSpot(pos)) return false;
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.WATER_CAULDRON) || state.is(ModTags.SPARROW_HARPY_DUST_BATH)) return true;
        // Only shallow water: a one-block puddle with solid ground under it, no lakes.
        return state.is(Blocks.WATER) && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }
}
