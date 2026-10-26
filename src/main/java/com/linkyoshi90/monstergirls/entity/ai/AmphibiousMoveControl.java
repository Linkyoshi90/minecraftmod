package com.linkyoshi90.monstergirls.entity.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;

/**
 * Swims in three dimensions in the water (pitching up and down toward her target, so she dives instead of bobbing on
 * the surface) and walks with the plain land control otherwise, so she still hops up steps on shore. In the water she
 * turns before she goes: with the target off to the side she barely moves forward, so she doesn't orbit it.
 */
public class AmphibiousMoveControl extends SmoothSwimmingMoveControl {
    private final MoveControl land;

    public AmphibiousMoveControl(Mob mob) {
        this(mob, 0.5F);
    }

    /** inWaterSpeed scales her walking speed while swimming (1.0 keeps the pace vanilla water travel gives her). */
    public AmphibiousMoveControl(Mob mob, float inWaterSpeed) {
        super(mob, 85, 20, inWaterSpeed, 1.0F, false);
        this.land = new MoveControl(mob);
    }

    @Override
    public void tick() {
        if (this.mob.isInWater()) {
            super.tick();
            if (this.operation == Operation.MOVE_TO) {
                float want = (float) (Mth.atan2(this.wantedZ - this.mob.getZ(), this.wantedX - this.mob.getX()) * Mth.RAD_TO_DEG) - 90.0F;
                float off = Math.abs(Mth.wrapDegrees(want - this.mob.getYRot()));
                if (off > 45.0F) {
                    float k = off > 90.0F ? 0.1F : 0.4F;
                    this.mob.setSpeed(this.mob.getSpeed() * k);
                    this.mob.setZza(this.mob.zza * k);
                    this.mob.setYya(this.mob.yya * k);
                }
            }
            return;
        }
        if (this.operation == Operation.MOVE_TO) {        // the navigation re-sends it every tick, so hand it over
            this.land.setWantedPosition(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
            this.operation = Operation.WAIT;
        }
        this.land.tick();
    }
}
