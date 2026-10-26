package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.block.AnuranSpawnBlock;
import com.linkyoshi90.monstergirls.block.ParentageBlockEntity;
import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * An expecting frog or toad whose day is up looks for the nearest water source (a still pond with no current if
 * there is one, otherwise any source block with open air above) and lays her spawn on its surface. Without water
 * nearby she keeps looking, and keeps carrying.
 */
public class LaySpawnGoal extends Goal {
    public static final int SEARCH_RADIUS = 24;
    private static final double REACH_SQR = 6.25;
    private static final int GIVE_UP_TICKS = 600;

    private final AbstractAnuran mob;
    private int searchDelay;
    @Nullable
    private BlockPos waterPos;
    private int ticks;

    public LaySpawnGoal(AbstractAnuran mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    /** The nearest still water source within reach of {@code origin}, or null. */
    @Nullable
    public static BlockPos findStillWater(net.minecraft.world.level.LevelReader level, BlockPos origin, int radius) {
        return findWater(level, origin, radius, true);
    }

    /** The nearest water source with air above it (still ones only, if {@code still}), or null. */
    @Nullable
    public static BlockPos findWater(net.minecraft.world.level.LevelReader level, BlockPos origin, int radius, boolean still) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -4, -radius), origin.offset(radius, 4, radius))) {
            if (still ? !AnuranSpawnBlock.isStillWaterSource(level, pos) : !AnuranSpawnBlock.isOpenWaterSource(level, pos)) continue;
            double dist = pos.distSqr(origin);
            if (dist < bestDist) {
                bestDist = dist;
                best = pos.immutable();
            }
        }
        return best;
    }

    @Override
    public boolean canUse() {
        if (!this.mob.getGestation().isReady() || this.mob.isBaby()) return false;
        if (--this.searchDelay > 0) return false;
        this.searchDelay = 40;
        this.waterPos = findStillWater(this.mob.level(), this.mob.blockPosition(), SEARCH_RADIUS);
        if (this.waterPos == null) this.waterPos = findWater(this.mob.level(), this.mob.blockPosition(), SEARCH_RADIUS, false);
        return this.waterPos != null && AnuranSpawnBlock.Species.forEntityId(this.mob.getType().builtInRegistryHolder().key().location().getPath()) != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.waterPos != null && this.mob.getGestation().isReady() && this.ticks < GIVE_UP_TICKS;
    }

    @Override
    public void start() {
        this.ticks = 0;
    }

    @Override
    public void tick() {
        this.ticks++;
        double x = this.waterPos.getX() + 0.5;
        double z = this.waterPos.getZ() + 0.5;
        this.mob.getLookControl().setLookAt(x, this.waterPos.getY(), z);
        if (this.mob.distanceToSqr(x, this.waterPos.getY(), z) > REACH_SQR) {
            if (this.mob.getNavigation().isDone()) this.mob.getNavigation().moveTo(x, this.waterPos.getY(), z, 1.0);
            return;
        }
        this.lay();
    }

    private void lay() {
        BlockPos spawnPos = this.waterPos.above();
        AnuranSpawnBlock.Species species = AnuranSpawnBlock.Species.forEntityId(this.mob.getType().builtInRegistryHolder().key().location().getPath());
        if (species != null && AnuranSpawnBlock.isOpenWaterSource(this.mob.level(), this.waterPos)) {
            BlockState state = ModBlocks.ANURAN_SPAWN.get().defaultBlockState().setValue(AnuranSpawnBlock.SPECIES, species);
            if (this.mob.level().isEmptyBlock(spawnPos) && this.mob.level().setBlock(spawnPos, state, 3)) {
                ParentageBlockEntity.setFatherAt(this.mob.level(), spawnPos, this.mob.getGestation().getFather());
                ParentageBlockEntity.setPaintAt(this.mob.level(), spawnPos, this.mob.getPaint());
                this.mob.level().playSound(null, spawnPos, SoundEvents.FROG_LAY_SPAWN, SoundSource.NEUTRAL, 1.0F, 1.0F);
                this.mob.getGestation().clear();
            }
        }
        this.waterPos = null;
    }

    @Override
    public void stop() {
        this.waterPos = null;
    }
}
