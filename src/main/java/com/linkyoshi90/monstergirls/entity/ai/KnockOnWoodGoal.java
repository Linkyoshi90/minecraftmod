package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Finds a spot next to something wooden (a tree trunk, planks, a door), stands there and raps on it a few
 * times with her knuckles, head cocked to listen.
 */
public class KnockOnWoodGoal extends MoveToBlockGoal {
    private static final int KNOCK_TICKS = 64;       // two loops of animation.crow_harpy.knock (1.6 s)
    private static final int[] RAPS = {4, 12, 20};   // when the fist lands within one loop

    private final AbstractHarpy harpy;
    private long nextKnockTime;
    private int knockTicks;
    @Nullable
    private BlockPos wood;

    public KnockOnWoodGoal(AbstractHarpy harpy, double speed) {
        super(harpy, speed, 10, 2);
        this.harpy = harpy;
    }

    private static boolean isWood(BlockState state) {
        return state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS) || state.is(BlockTags.WOODEN_DOORS);
    }

    @Nullable
    private static BlockPos woodNextTo(LevelReader level, BlockPos standing) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (isWood(level.getBlockState(standing.relative(dir)))) return standing.relative(dir);
        }
        return null;
    }

    @Override
    public boolean canUse() {
        return this.harpy.level().getGameTime() >= this.nextKnockTime && this.harpy.getAction() == AbstractHarpy.Action.NONE
                && !this.harpy.isHoldingSomething() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return this.knockTicks < KNOCK_TICKS && super.canContinueToUse();
    }

    @Override
    public void start() {
        super.start();
        this.knockTicks = 0;
    }

    @Override
    public void stop() {
        super.stop();
        if (this.harpy.getAction() == AbstractHarpy.Action.KNOCKING) this.harpy.setAction(AbstractHarpy.Action.NONE);
        this.nextKnockTime = this.harpy.level().getGameTime() + 1200 + this.harpy.getRandom().nextInt(2400);
        this.wood = null;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isReachedTarget()) return;
        if (this.wood == null) this.wood = woodNextTo(this.harpy.level(), this.blockPos.above());
        if (this.wood == null) return;
        this.harpy.getNavigation().stop();
        this.harpy.getLookControl().setLookAt(this.wood.getX() + 0.5, this.harpy.getEyeY(), this.wood.getZ() + 0.5);
        this.harpy.setAction(AbstractHarpy.Action.KNOCKING);
        int beat = this.knockTicks++ % 32;
        for (int rap : RAPS) {
            if (beat == rap) {
                BlockState state = this.harpy.level().getBlockState(this.wood);
                this.harpy.playSound(state.getSoundType(this.harpy.level(), this.wood, this.harpy).getHitSound(), 1.0F, 0.8F);
            }
        }
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        BlockPos standing = pos.above();
        return level.getBlockState(pos).isFaceSturdy(level, pos, Direction.UP) && level.isEmptyBlock(standing)
                && level.isEmptyBlock(standing.above()) && woodNextTo(level, standing) != null && this.harpy.isSafeSpot(standing);
    }
}
