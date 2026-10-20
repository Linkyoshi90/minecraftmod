package com.linkyoshi90.monstergirls.entity.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;

/**
 * Swims in three dimensions in the water (pitching up and down toward her target, so she dives instead of bobbing on
 * the surface) and walks with the plain land control otherwise, so she still hops up steps on shore.
 */
public class AmphibiousMoveControl extends SmoothSwimmingMoveControl {
    private final MoveControl land;

    public AmphibiousMoveControl(Mob mob) {
        this(mob, 0.5F);
    }

    /** inWaterSpeed scales her walking speed while swimming (1.0 keeps the pace vanilla water travel gives her). */
    public AmphibiousMoveControl(Mob mob, float inWaterSpeed) {
        super(mob, 85, 10, inWaterSpeed, 1.0F, false);
        this.land = new MoveControl(mob);
    }

    @Override
    public void tick() {
        if (this.mob.isInWater()) {
            super.tick();
            return;
        }
        if (this.operation == Operation.MOVE_TO) {        // the navigation re-sends it every tick, so hand it over
            this.land.setWantedPosition(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
            this.operation = Operation.WAIT;
        }
        this.land.tick();
    }
}
