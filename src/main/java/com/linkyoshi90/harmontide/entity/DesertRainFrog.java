package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.entity.ai.AnuranRestGoal;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * The smallest anuran, a grumpy ball of a desert frog. She burrows into the sand until only her eyes show (a
 * player walking up gives her away with a squeak), licks dew off her poncho when she is thirsty, which makes her
 * far more drought-proof than other frogs, and screams when hurt.
 */
public class DesertRainFrog extends Bullywug {
    private static final int DRY_LIMIT_TICKS = 8000;

    public DesertRainFrog(EntityType<? extends DesertRainFrog> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return frogAttributes(8.0, 0.2, 1.0, 0.2);
    }

    @Override
    protected void addSpeciesGoals() {
        this.goalSelector.addGoal(4, new AnuranRestGoal<>(this, Action.SIPPING, 40, 60, 100, DesertRainFrog::isDry));
        this.goalSelector.addGoal(5, new AnuranRestGoal<>(this, Action.BURROWING, 300, 400, 1200,
                f -> f.level().getBlockState(f.blockPosition().below()).is(BlockTags.SAND) && f.level().getNearestPlayer(f, 3.0) == null));
        this.goalSelector.addGoal(7, new AnuranRestGoal<>(this, Action.SITTING, 500, 200, 400, f -> true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // Drinking dew: each tick of sipping takes the edge off her thirst.
        if (!this.level().isClientSide && this.getAction() == Action.SIPPING) {
            this.setDryTicks(Math.max(0, this.getDryTicks() - 40));
        }
    }

    /** The famous squeak, then she runs. */
    @Override
    protected void reactToAttacker(LivingEntity attacker, DamageSource source) {
        this.setAction(Action.NONE);
        this.startTimedAction(Action.SCREAMING, 40);
        this.startFleeing(attacker);
    }

    @Override
    @Nullable
    protected Action ambientAction() {
        return this.getAction() == Action.BURROWING ? null : Action.CROAKING;
    }

    @Override
    protected int dryLimitTicks() {
        return DRY_LIMIT_TICKS;
    }

    @Override
    protected String species() {
        return "desert_rain_frog";
    }
}
