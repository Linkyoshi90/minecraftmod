package com.linkyoshi90.harmontide.entity.ai;

import com.linkyoshi90.harmontide.block.HarpyEggBlock;
import com.linkyoshi90.harmontide.block.ParentageBlockEntity;
import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * An expecting harpy whose time has come picks a spot (her home if it will do, otherwise a dry, solid spot close
 * by), weaves a nest there and lays a clutch of two to four fertilised eggs in it. The nest becomes her home, so she
 * broods on it at night. If she can't get to her spot for a long while she builds wherever she stands.
 */
public class BuildNestGoal extends Goal {
    public static final int BUILD_TICKS = 100;
    private static final int GIVE_UP_TICKS = 1200;
    private static final int SEARCH_RADIUS = 6;

    private static final int PUSH_TICKS = 14;

    private final AbstractHarpy harpy;
    @Nullable
    private BlockPos spot;
    private int ticks;
    private int built;
    /** How many eggs this clutch will hold, once she has started pushing them out. 0 while she is still weaving. */
    private int eggsWanted;
    private int eggsLaid;
    private int pushTicks;

    public BuildNestGoal(AbstractHarpy harpy) {
        this.harpy = harpy;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!this.harpy.getGestation().isReady() || this.harpy.isBaby() || this.harpy.isOrderedToSit() || this.harpy.tickCount % 19 != 0) return false;
        BlockPos home = this.harpy.getHomePos();
        this.spot = home != null && canNestAt(this.harpy.level(), home) ? home : findSpot(this.harpy.level(), this.harpy.blockPosition(), SEARCH_RADIUS);
        return this.spot != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.spot != null && this.harpy.getGestation().isReady() && !this.harpy.isOrderedToSit();
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.built = 0;
        this.eggsWanted = 0;
        this.eggsLaid = 0;
        this.pushTicks = 0;
    }

    @Override
    public void stop() {
        if (this.harpy.getAction() == AbstractHarpy.Action.NESTING || this.harpy.getAction() == AbstractHarpy.Action.LAYING) {
            this.harpy.setAction(AbstractHarpy.Action.NONE);
        }
        this.spot = null;
        this.eggsWanted = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    /** Room for the nest, on something solid, out of the water. */
    public static boolean canNestAt(Level level, BlockPos pos) {
        BlockState nest = ModBlocks.HARPY_EGG.get().defaultBlockState();
        return level.isEmptyBlock(pos) && level.getFluidState(pos).isEmpty() && nest.canSurvive(level, pos);
    }

    /** The closest place to nest around {@code origin} (she may be flying, so this looks a fair way down). */
    @Nullable
    public static BlockPos findSpot(Level level, BlockPos origin, int radius) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -8, -radius), origin.offset(radius, 2, radius))) {
            if (!canNestAt(level, pos)) continue;
            double dist = pos.distSqr(origin);
            if (dist < bestDist) {
                bestDist = dist;
                best = pos.immutable();
            }
        }
        return best;
    }

    @Override
    public void tick() {
        if (this.spot == null) return;                       // spot lost (taken, or the clutch is laid): the goal stops on its next check
        this.ticks++;
        if (this.eggsWanted > 0) {
            this.pushEgg();
            return;
        }
        if (this.ticks > GIVE_UP_TICKS) {
            BlockPos here = findSpot(this.harpy.level(), this.harpy.blockPosition(), 2);
            if (here != null) this.spot = here;
            this.beginClutch();
            return;
        }
        Vec3 centre = Vec3.atBottomCenterOf(this.spot);
        double dx = this.harpy.getX() - centre.x, dz = this.harpy.getZ() - centre.z;
        if (dx * dx + dz * dz > 0.8 * 0.8 || this.harpy.getY() > this.spot.getY() + 1.0) {
            if (this.ticks % 10 == 1) this.harpy.getNavigation().moveTo(centre.x, centre.y, centre.z, 1.0);
            this.harpy.getMoveControl().setWantedPosition(centre.x, centre.y + 0.2, centre.z, 1.0);
            return;
        }
        this.harpy.getNavigation().stop();
        if (!canNestAt(this.harpy.level(), this.spot)) {   // someone put something there: find another spot
            this.spot = findSpot(this.harpy.level(), this.harpy.blockPosition(), SEARCH_RADIUS);
            return;
        }
        this.harpy.setAction(AbstractHarpy.Action.NESTING);
        if (this.harpy.level() instanceof ServerLevel server && this.built % 10 == 0) {
            ItemStack twig = new ItemStack(this.harpy.getRandom().nextBoolean() ? Items.STICK : Items.WHEAT);
            server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, twig), centre.x, centre.y + 0.2, centre.z, 4, 0.3, 0.05, 0.3, 0.02);
            server.playSound(null, this.spot, SoundEvents.GRASS_PLACE, SoundSource.NEUTRAL, 0.6F, 1.2F);
        }
        if (++this.built >= BUILD_TICKS) this.beginClutch();
    }

    /** The nest, with the first egg. The rest are pushed out one at a time. */
    private void beginClutch() {
        BlockPos pos = this.spot;
        Level level = this.harpy.level();
        HarpyEggBlock.Species species = HarpyEggBlock.Species.forEntityId(this.harpy.speciesId());
        if (pos == null || species == null || !canNestAt(level, pos)) {
            this.spot = null;
            return;
        }
        BlockState nest = ModBlocks.HARPY_EGG.get().defaultBlockState().setValue(HarpyEggBlock.SPECIES, species).setValue(HarpyEggBlock.EGGS, 1);
        if (!level.setBlock(pos, nest, 3)) {
            this.spot = null;
            return;
        }
        int eggs = com.linkyoshi90.harmontide.entity.girl.Cravings.twins(this.harpy) ? HarpyEggBlock.MAX_EGGS   // her craving answered: a full clutch
                : 2 + this.harpy.getRandom().nextInt(HarpyEggBlock.MAX_EGGS - 1);
        ParentageBlockEntity.setFatherAt(level, pos, this.harpy.getGestation().getFather());
        ParentageBlockEntity.setPaintAt(level, pos, this.harpy.getPaint());
        this.harpy.setHomePos(pos);
        this.eggsWanted = Math.max(1, eggs);
        this.eggsLaid = 1;
        this.pushTicks = 0;
        level.playSound(null, pos, SoundEvents.TURTLE_EGG_CRACK, SoundSource.NEUTRAL, 0.7F, 1.3F);
        this.harpy.setAction(AbstractHarpy.Action.LAYING);
        if (this.eggsLaid >= this.eggsWanted) this.finishClutch();
    }

    /** Still over the nest, prone, until the next egg joins the clutch. */
    private void pushEgg() {
        if (this.spot == null) return;
        this.harpy.setAction(AbstractHarpy.Action.LAYING);
        this.harpy.getNavigation().stop();
        this.harpy.setDeltaMovement(Vec3.ZERO);
        Vec3 centre = Vec3.atBottomCenterOf(this.spot);
        this.harpy.moveTo(centre.x, this.spot.getY(), centre.z, this.harpy.getYRot(), 0.0F);
        if (++this.pushTicks < PUSH_TICKS) return;
        this.pushTicks = 0;
        this.eggsLaid++;
        Level level = this.harpy.level();
        BlockState state = level.getBlockState(this.spot);
        if (!state.is(ModBlocks.HARPY_EGG.get())) {
            this.finishClutch();
            return;
        }
        int shown = Math.min(Math.max(this.eggsLaid, 1), HarpyEggBlock.MAX_EGGS);
        level.setBlock(this.spot, state.setValue(HarpyEggBlock.EGGS, shown), 3);
        level.playSound(null, this.spot, SoundEvents.TURTLE_EGG_CRACK, SoundSource.NEUTRAL, 0.7F, 1.2F);
        if (level instanceof ServerLevel server) {
            server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.EGG)),
                    centre.x, centre.y + 0.25, centre.z, 3, 0.12, 0.04, 0.12, 0.02);
        }
        if (this.eggsLaid >= this.eggsWanted) this.finishClutch();
    }

    private void finishClutch() {
        this.harpy.getGestation().clear();
        if (this.harpy.getAction() == AbstractHarpy.Action.LAYING || this.harpy.getAction() == AbstractHarpy.Action.NESTING) {
            this.harpy.setAction(AbstractHarpy.Action.NONE);
        }
        this.spot = null;
        this.eggsWanted = 0;
    }
}
