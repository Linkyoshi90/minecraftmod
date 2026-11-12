package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.entity.ai.AnuranRestGoal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * The canopy scout: light and quick, with the highest hop of the family. She climbs walls and trees on her sticky
 * pads, glides down instead of falling, and sleeps through the day with her eyes shut until someone gets close.
 */
public class TreeFrog extends Bullywug {
    public TreeFrog(EntityType<? extends TreeFrog> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return frogAttributes(10.0, 0.3, 1.0, 0.3).add(Attributes.JUMP_STRENGTH, 0.8);
    }

    @Override
    protected void addSpeciesGoals() {
        this.goalSelector.addGoal(5, new AnuranRestGoal<>(this, Action.SLEEPING, 300, 400, 1200,
                f -> f.level().isDay() && !f.level().isRaining() && f.level().getNearestPlayer(f, 4.0) == null));
        this.goalSelector.addGoal(7, new AnuranRestGoal<>(this, Action.SITTING, 500, 200, 400, f -> true));
    }

    @Override
    protected void reactToAttacker(LivingEntity attacker, DamageSource source) {
        this.startFleeing(attacker); // too light to fight
    }

    @Override
    protected boolean canClimbWalls() {
        return true;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // Glide: spread the leaf cape and drift down instead of dropping.
        Vec3 motion = this.getDeltaMovement();
        if (!this.onGround() && !this.isInWater() && !this.onClimbable() && motion.y < -0.1) {
            this.setDeltaMovement(motion.multiply(1.0, 0.7, 1.0));
            this.fallDistance = 0.0F;
        }
    }

    @Override
    @Nullable
    protected String specialMovementAnimation() {
        if (this.isClimbingWall()) return "climb";
        return !this.onGround() && !this.isInWater() && this.getDeltaMovement().y < -0.1 ? "glide" : null;
    }

    @Override
    protected String species() {
        return "tree_frog";
    }
}
