package com.linkyoshi90.harpies.entity.ai;

import com.linkyoshi90.harpies.entity.AlbatrossHarpy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * The albatross's whole day: soar in wide, slow circles high above the ground; every so often look for a clearing
 * and glide down to rest there a while; then take off again. Grounded somewhere cramped or hilly, she leaves at once.
 */
public class AlbatrossFlightGoal extends Goal {
    private enum Phase { SOARING, LANDING, RESTING }

    private final AlbatrossHarpy albatross;
    private Phase phase = Phase.SOARING;
    private Vec3 circleCenter;
    private double radius;
    private double angle;
    private int phaseTicks;
    private int phaseLength;
    @Nullable
    private BlockPos landing;

    public AlbatrossFlightGoal(AlbatrossHarpy albatross) {
        this.albatross = albatross;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        boolean groundedInClearing = this.albatross.onGround() && AlbatrossHarpy.isClearing(this.albatross.level(), this.albatross.blockPosition());
        if (groundedInClearing) this.rest();
        else this.soar();
    }

    @Override
    public void stop() {
        this.landing = null;
    }

    private void soar() {
        this.phase = Phase.SOARING;
        this.phaseTicks = 0;
        this.phaseLength = 600 + this.albatross.getRandom().nextInt(1200);
        this.circleCenter = this.albatross.position();
        this.radius = 12 + this.albatross.getRandom().nextInt(12);
        this.angle = this.albatross.getRandom().nextDouble() * Math.PI * 2;
    }

    private void rest() {
        this.phase = Phase.RESTING;
        this.phaseTicks = 0;
        this.phaseLength = 400 + this.albatross.getRandom().nextInt(800);
        this.albatross.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.phaseTicks++;
        switch (this.phase) {
            case SOARING -> this.tickSoaring();
            case LANDING -> this.tickLanding();
            case RESTING -> {
                if (this.phaseTicks > this.phaseLength) this.soar();
            }
        }
    }

    private void tickSoaring() {
        // Drift the circle a little so she slowly wanders along the coast.
        this.angle += 0.6 / this.radius;
        this.circleCenter = this.circleCenter.add(Math.cos(this.angle * 0.2) * 0.02, 0, Math.sin(this.angle * 0.2) * 0.02);
        double groundY = this.albatross.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
                BlockPos.containing(this.circleCenter)).getY();
        double y = groundY + 16 + Math.sin(this.angle * 0.5) * 4;
        this.albatross.getMoveControl().setWantedPosition(this.circleCenter.x + Math.cos(this.angle) * this.radius, y,
                this.circleCenter.z + Math.sin(this.angle) * this.radius, 1.0);
        if (this.phaseTicks > this.phaseLength && this.phaseTicks % 40 == 0) {
            this.landing = this.findClearing();
            if (this.landing != null) {
                this.phase = Phase.LANDING;
                this.phaseTicks = 0;
            }
        }
    }

    private void tickLanding() {
        Vec3 spot = Vec3.atBottomCenterOf(this.landing);
        if (this.albatross.onGround() && this.albatross.distanceToSqr(spot) < 2.0 * 2.0) {
            this.rest();
            return;
        }
        if (this.phaseTicks > 600) {
            this.soar(); // couldn't get down there
            return;
        }
        // Long shallow glide: aim a little above the spot until close, then drop onto it.
        double above = Math.min(6.0, Math.sqrt(this.albatross.distanceToSqr(spot.x, this.albatross.getY(), spot.z)) * 0.3);
        this.albatross.getMoveControl().setWantedPosition(spot.x, spot.y + above, spot.z, 1.0);
    }

    @Nullable
    private BlockPos findClearing() {
        for (int i = 0; i < 12; i++) {
            BlockPos column = this.albatross.blockPosition().offset(this.albatross.getRandom().nextInt(65) - 32, 0,
                    this.albatross.getRandom().nextInt(65) - 32);
            BlockPos ground = this.albatross.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
            if (AlbatrossHarpy.isClearing(this.albatross.level(), ground)) return ground;
        }
        return null;
    }

    public boolean isResting() {
        return this.phase == Phase.RESTING;
    }
}
