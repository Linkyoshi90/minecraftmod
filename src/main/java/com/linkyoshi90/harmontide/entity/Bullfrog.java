package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.entity.ai.AnuranRestGoal;
import com.linkyoshi90.harmontide.entity.ai.BellowGoal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * The pond boss: broad, stern, bossy and loud. Where the bullywug backs down, the bullfrog never does: she answers
 * every attack with a shove, bellows at rival bullfrogs, and beats her chest.
 */
public class Bullfrog extends Bullywug {
    public Bullfrog(EntityType<? extends Bullfrog> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return frogAttributes(30.0, 0.22, 5.0, 2.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.4);
    }

    @Override
    protected void addSpeciesGoals() {
        this.goalSelector.addGoal(5, new BellowGoal(this));
        this.goalSelector.addGoal(7, new AnuranRestGoal<>(this, Action.SITTING, 500, 200, 500, b -> waterNearby(b.level(), b.blockPosition(), 4)));
        this.goalSelector.addGoal(7, new AnuranRestGoal<>(this, Action.POUNDING, 400, 60, 120, b -> b.getTarget() == null));
    }

    /** She never flees: whoever hurt her, or hurt one of her kind nearby, gets a fight. */
    @Override
    protected void reactToAttacker(LivingEntity attacker, DamageSource source) {
        for (Bullywug b : this.nearbyPack()) b.retaliateAgainst(attacker);
        this.retaliateAgainst(attacker);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && this.getAction() == Action.NONE) this.startTimedAction(Action.SHOVING, 12);
        return hit;
    }

    @Override
    protected String species() {
        return "bullfrog";
    }
}
