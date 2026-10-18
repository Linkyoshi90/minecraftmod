package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.entity.ai.AnuranRestGoal;
import com.linkyoshi90.monstergirls.entity.ai.SwallowItemGoal;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * The big venomous glutton. Whatever hits her gets Poison I and Nausea I (so does anyone close when she squirts);
 * she swallows dropped items and small mobs whole, and they are gone for good.
 */
public class CaneToad extends MucusToad {
    public static final int VENOM_TICKS = 200;
    private static final double SQUIRT_RADIUS = 2.5;
    /** Small enough to swallow. */
    private static final float MAX_PREY_SIZE = 0.7F;

    public CaneToad(EntityType<? extends CaneToad> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return toadAttributes(18.0, 0.18);
    }

    @Override
    protected void addSpeciesGoals() {
        this.goalSelector.addGoal(3, new SwallowItemGoal(this));
        this.goalSelector.addGoal(5, new AnuranRestGoal<>(this, Action.SITTING, 500, 200, 500, t -> true));
    }

    /** Small monsters and rabbits that haven't been given a name. */
    @Override
    public boolean isPest(LivingEntity entity) {
        if (entity == this || entity.hasCustomName() || (entity instanceof Mob mob && mob.isLeashed())) return false;
        if (!(entity instanceof Monster || entity instanceof Rabbit)) return false;
        return entity.getBbWidth() <= MAX_PREY_SIZE && entity.getBbHeight() <= MAX_PREY_SIZE;
    }

    @Override
    public Action huntAction() {
        return Action.GULPING;
    }

    /** Gone for good: no drops, no experience. */
    @Override
    public void swallow(LivingEntity prey) {
        prey.discard();
    }

    @Override
    protected Action defenceAction() {
        return Action.SQUIRTING;
    }

    @Override
    protected void secrete(LivingEntity attacker) {
        this.venom(attacker);
        for (LivingEntity e : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(SQUIRT_RADIUS),
                x -> x != this && x != attacker && !(x instanceof AbstractAnuran))) {
            this.venom(e);
        }
    }

    private void venom(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.POISON, VENOM_TICKS, 0), this);
        target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, VENOM_TICKS, 0), this);
    }

    @Override
    protected String species() {
        return "cane_toad";
    }
}
