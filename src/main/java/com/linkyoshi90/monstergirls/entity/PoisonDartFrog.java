package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.entity.ai.AnuranRestGoal;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.level.Level;

/**
 * The rainforest hunter: poisonous to the touch, so any mob that attacks her in melee gets Poison II. She uses it
 * only defensively, warns the curious by flaunting her colours, and climbs on her sticky pads.
 */
public class PoisonDartFrog extends Bullywug {
    /** Eight seconds of Poison II. */
    public static final int POISON_TICKS = 160;

    public PoisonDartFrog(EntityType<? extends PoisonDartFrog> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return frogAttributes(12.0, 0.27, 1.0, 0.3);
    }

    @Override
    protected void addSpeciesGoals() {
        // The aposematic display, when someone walks up to look.
        this.goalSelector.addGoal(5, new AnuranRestGoal<>(this, Action.WARNING, 120, 60, 120, f -> f.level().getNearestPlayer(f, 6.0) != null));
        this.goalSelector.addGoal(7, new AnuranRestGoal<>(this, Action.SITTING, 500, 200, 400, f -> true));
    }

    /** She doesn't flee or fight: the poison does the talking. */
    @Override
    protected void reactToAttacker(LivingEntity attacker, DamageSource source) {
        if (source.getDirectEntity() instanceof LivingEntity direct) {
            direct.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_TICKS, 1), this);
        }
        this.setAction(Action.NONE);
        this.startTimedAction(Action.WARNING, 60);
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
    protected String species() {
        return "poison_dart_frog";
    }
}
