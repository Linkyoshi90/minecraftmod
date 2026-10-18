package com.linkyoshi90.monstergirls.entity.ai;

import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.SeagullHarpy;
import com.linkyoshi90.monstergirls.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Out at sea a gull settles on a lonely rock (a solid block standing out of open water) and sings: a lilting,
 * alluring harp melody. Boats with people in them within 24 blocks are slowly drawn toward the rock; one that
 * reaches it is wrecked, spilling its crew into the sea. Players she is fond of are not lured.
 */
public class SirenSongGoal extends Goal {
    /** Natural minor scale in note-block semitones: wistful rather than cheerful. */
    private static final int[] SCALE = {9, 11, 12, 14, 16, 17, 19, 21};
    private static final double LURE_RANGE = 24.0;
    private static final double WRECK_DISTANCE = 1.8;
    private static final double PULL = 0.04;

    private final SeagullHarpy gull;
    private BlockPos rock;
    private int ticks;
    private int duration;
    private int note;
    private long nextTime;
    private int pulls;

    public SirenSongGoal(SeagullHarpy gull) {
        this.gull = gull;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    /** A solid block with open air above, mostly surrounded by water at its own level. */
    public static boolean isSeaRock(Level level, BlockPos pos) {
        if (!level.getBlockState(pos).isFaceSturdy(level, pos, Direction.UP) || !level.isEmptyBlock(pos.above())
                || !level.isEmptyBlock(pos.above(2))) {
            return false;
        }
        // Walk down to the waterline beside the rock, then check how much open water surrounds it there.
        int waterY = pos.getY();
        while (waterY > pos.getY() - 4 && !level.getFluidState(new BlockPos(pos.getX() + 1, waterY, pos.getZ())).is(FluidTags.WATER)
                && !level.getFluidState(new BlockPos(pos.getX() - 1, waterY, pos.getZ())).is(FluidTags.WATER)) {
            waterY--;
        }
        int water = 0, total = 0;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) continue;
                total++;
                if (level.getFluidState(new BlockPos(pos.getX() + dx, waterY, pos.getZ() + dz)).is(FluidTags.WATER)) water++;
            }
        }
        return water >= total * 0.7;
    }

    @Nullable
    private BlockPos findRock() {
        BlockPos origin = this.gull.blockPosition();
        return BlockPos.findClosestMatch(origin, 24, 8, p -> isSeaRock(this.gull.level(), p)).map(BlockPos::immutable).orElse(null);
    }

    @Override
    public boolean canUse() {
        if (this.gull.level().getGameTime() < this.nextTime || this.gull.isHoldingSomething()) return false;
        if (this.gull.getRandom().nextInt(reducedTickDelay(800)) != 0) return false;
        this.rock = this.findRock();
        return this.rock != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticks < this.duration;
    }

    @Override
    public void start() {
        this.ticks = 0;
        this.duration = 600 + this.gull.getRandom().nextInt(600);
        this.note = 3;
    }

    @Override
    public void stop() {
        if (this.gull.getAction() == AbstractHarpy.Action.SINGING) this.gull.setAction(AbstractHarpy.Action.NONE);
        this.rock = null;
        this.nextTime = this.gull.level().getGameTime() + 2400 + this.gull.getRandom().nextInt(2400);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private Vec3 perch() {
        return Vec3.atBottomCenterOf(this.rock.above());
    }

    @Override
    public void tick() {
        this.ticks++;
        if (this.gull.distanceToSqr(this.perch()) > 1.2 * 1.2) {
            this.gull.setAction(AbstractHarpy.Action.NONE);
            Vec3 p = this.perch();
            if (this.ticks % 20 == 1) this.gull.getNavigation().moveTo(p.x, p.y, p.z, 1.2);
            if (this.gull.distanceToSqr(p) < 4 * 4) this.gull.getMoveControl().setWantedPosition(p.x, p.y, p.z, 1.0);
            return;
        }
        this.gull.getNavigation().stop();
        this.gull.setAction(AbstractHarpy.Action.SINGING);
        if (this.ticks % 6 == 0 && this.ticks % 48 < 36) this.playNote();
        this.lureBoats();
    }

    private void playNote() {
        this.note = Mth.clamp(this.note + this.gull.getRandom().nextInt(5) - 2, 0, SCALE.length - 1);
        int semitone = SCALE[this.note];
        this.gull.playSound(ModSounds.SEAGULL_SING.get(), 1.6F, (float) Math.pow(2.0, (semitone - 12) / 12.0));
        if (this.gull.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.NOTE, this.gull.getX(), this.gull.getEyeY() + 0.5, this.gull.getZ(), 0, semitone / 24.0, 0, 0, 1);
        }
    }

    /** Draw manned boats toward the rock; wreck any that reach it. */
    private void lureBoats() {
        Vec3 rockCenter = Vec3.atCenterOf(this.rock);
        for (Boat boat : this.gull.level().getEntitiesOfClass(Boat.class, this.gull.getBoundingBox().inflate(LURE_RANGE))) {
            Player sailor = boat.getPassengers().stream().filter(p -> p instanceof Player).map(p -> (Player) p).findFirst().orElse(null);
            if (sailor == null || sailor.isCreative() || this.gull.isFriendly(sailor)) continue;
            Vec3 toRock = rockCenter.subtract(boat.position()).multiply(1, 0, 1);
            if (toRock.length() < WRECK_DISTANCE) {
                this.wreck(boat);
                continue;
            }
            // A steered boat is simulated by its driver's client (the server zeroes its velocity every tick), so the
            // pull is sent straight to that client as a velocity update, like a gust of wind or an explosion would be.
            if (this.ticks % 5 == 0 && sailor instanceof ServerPlayer driver) {
                driver.connection.send(new ClientboundSetEntityMotionPacket(boat.getId(), toRock.normalize().scale(PULL * 5)));
                this.pulls++;
            }
        }
    }

    private void wreck(Boat boat) {
        if (!(this.gull.level() instanceof ServerLevel server)) return;
        server.sendParticles(ParticleTypes.SPLASH, boat.getX(), boat.getY() + 0.5, boat.getZ(), 30, 0.8, 0.3, 0.8, 0.2);
        boat.playSound(net.minecraft.sounds.SoundEvents.WOOD_BREAK, 1.2F, 0.8F);
        for (Entity passenger : boat.getPassengers()) passenger.stopRiding();
        // Splinters, like a boat that crashed too hard.
        boat.spawnAtLocation(boat.getVariant().getPlanks(), 3);
        boat.spawnAtLocation(Items.STICK.getDefaultInstance().copyWithCount(2));
        boat.discard();
    }

    /** For tests: how many times she has tugged a boat toward her rock. */
    public int getPulls() {
        return this.pulls;
    }

    @Nullable
    public BlockPos getRock() {
        return this.rock;
    }
}
