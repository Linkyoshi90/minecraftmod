package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.entity.ai.AnuranRestGoal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * The see-through stream frog: nearly invisible to mobs until they get close (the renderer fades her for players
 * too), calls with a high peep, and guards her patch of the bank. Too delicate to fight, she bolts for the water.
 */
public class GlassFrog extends Bullywug {
    /** Mobs notice her at a tenth of the usual distance, like a creature under invisibility. */
    private static final double CAMOUFLAGE = 0.1;
    /** Closer than this, her camouflage fails (mobs and the renderer alike). */
    public static final double REVEAL_DISTANCE = 4.0;

    public GlassFrog(EntityType<? extends GlassFrog> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return frogAttributes(8.0, 0.26, 1.0, 0.2);
    }

    @Override
    protected void addSpeciesGoals() {
        this.goalSelector.addGoal(5, new AnuranRestGoal<>(this, Action.GUARDING, 300, 100, 300, f -> waterNearby(f.level(), f.blockPosition(), 5)));
        this.goalSelector.addGoal(7, new AnuranRestGoal<>(this, Action.SITTING, 500, 200, 400, f -> true));
    }

    @Override
    protected void reactToAttacker(LivingEntity attacker, DamageSource source) {
        this.startFleeing(attacker);
    }

    @Override
    public double getVisibilityPercent(@Nullable Entity looker) {
        double visibility = super.getVisibilityPercent(looker);
        if (looker == null || looker.distanceToSqr(this) > REVEAL_DISTANCE * REVEAL_DISTANCE) visibility *= CAMOUFLAGE;
        return visibility;
    }

    @Override
    @Nullable
    protected Action ambientAction() {
        return Action.PEEPING;
    }

    @Override
    protected String species() {
        return "glass_frog";
    }
}
