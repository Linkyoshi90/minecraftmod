package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.entity.ai.AnuranRestGoal;
import com.linkyoshi90.harmontide.entity.ai.StompGoal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * The giant: slow, huge and almost silent. She does not fight or flee; when hurt she lifts a foot and stomps, and
 * the ground shakes: every nearby mob and player is knocked back.
 */
public class GoliathFrog extends Bullywug {
    private boolean stompWanted;

    public GoliathFrog(EntityType<? extends GoliathFrog> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return frogAttributes(30.0, 0.2, 1.0, 0.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    protected void addSpeciesGoals() {
        this.goalSelector.addGoal(4, new StompGoal(this));
        this.goalSelector.addGoal(5, new AnuranRestGoal<>(this, Action.LIFTING, 500, 100, 200, f -> f.getTarget() == null));
        this.goalSelector.addGoal(7, new AnuranRestGoal<>(this, Action.SITTING, 500, 200, 500, f -> waterNearby(f.level(), f.blockPosition(), 4)));
    }

    @Override
    protected void reactToAttacker(LivingEntity attacker, DamageSource source) {
        this.stompWanted = true;
    }

    public boolean isStompWanted() {
        return this.stompWanted;
    }

    public void clearStompWanted() {
        this.stompWanted = false;
    }

    @Override
    protected String species() {
        return "goliath_frog";
    }
}
