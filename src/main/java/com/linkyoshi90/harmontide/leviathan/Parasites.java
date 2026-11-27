package com.linkyoshi90.harmontide.leviathan;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * What infests the Dream-Leviathan: zombies, skeletons and creepers the parasite has overgrown, with swollen tumours, split flesh and
 * glowing violet growths (their own textures and growths, client/ParasiteRenderers). They fight beside plain vanilla mobs that wandered
 * in. Each spreads the sickness its own way: the zombie's bite poisons, the skeleton's arrows weaken, and the creeper bursts into a
 * cloud of bile. Each drops a dream scale now and then (the horn that calls her back is made of them).
 */
public final class Parasites {
    /** The chance a parasite drops a dream scale. */
    public static final float SCALE_CHANCE = 0.25F;

    private Parasites() {
    }

    /** Marks the infested (stomach acid leaves them be). */
    public interface Infested {
    }

    private static void burst(LivingEntity mob) {
        if (mob.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.ITEM_SLIME, mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(), 16, 0.3, 0.4, 0.3, 0.1);
            level.sendParticles(ParticleTypes.WITCH, mob.getX(), mob.getY() + mob.getBbHeight() * 0.6, mob.getZ(), 8, 0.3, 0.4, 0.3, 0.05);
            if (mob.getRandom().nextFloat() < SCALE_CHANCE) mob.spawnAtLocation(new ItemStack(LeviathanRegistry.DREAM_SCALE.get()));
        }
    }

    public static class InfestedZombie extends Zombie implements Infested {
        public InfestedZombie(EntityType<? extends Zombie> type, Level level) {
            super(type, level);
            this.xpReward = 8;
        }

        public static AttributeSupplier.Builder createAttributes() {
            return Zombie.createAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.26).add(Attributes.ATTACK_DAMAGE, 4.0)
                    .add(Attributes.ARMOR, 3.0);
        }

        @Override
        protected boolean isSunSensitive() {
            return false;
        }

        @Override
        protected boolean convertsInWater() {
            return false;
        }

        @Override
        public boolean doHurtTarget(Entity target) {
            boolean hit = super.doHurtTarget(target);
            if (hit && target instanceof LivingEntity living) living.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0), this);
            return hit;
        }

        @Override
        public void die(DamageSource source) {
            super.die(source);
            burst(this);
        }
    }

    public static class InfestedSkeleton extends Skeleton implements Infested {
        public InfestedSkeleton(EntityType<? extends Skeleton> type, Level level) {
            super(type, level);
            this.xpReward = 8;
        }

        public static AttributeSupplier.Builder createAttributes() {
            return Skeleton.createAttributes().add(Attributes.MAX_HEALTH, 26.0).add(Attributes.ARMOR, 2.0);
        }

        @Override
        protected boolean isSunBurnTick() {
            return false;
        }

        @Override
        public boolean isFreezing() {
            return false;
        }

        @Override
        protected AbstractArrow getArrow(ItemStack arrow, float velocity, @Nullable ItemStack weapon) {
            AbstractArrow shot = super.getArrow(arrow, velocity, weapon);
            if (shot instanceof Arrow plain) plain.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
            return shot;
        }

        @Override
        public void die(DamageSource source) {
            super.die(source);
            burst(this);
        }
    }

    public static class InfestedCreeper extends Creeper implements Infested {
        public InfestedCreeper(EntityType<? extends Creeper> type, Level level) {
            super(type, level);
            this.xpReward = 8;
        }

        public static AttributeSupplier.Builder createAttributes() {
            return Creeper.createAttributes().add(Attributes.MAX_HEALTH, 26.0).add(Attributes.MOVEMENT_SPEED, 0.27);
        }

        /** Its burst leaves a cloud of bile behind (called from LeviathanEvents when it explodes). */
        public void spillBile() {
            AreaEffectCloud cloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
            cloud.setRadius(3.0F);
            cloud.setRadiusOnUse(-0.3F);
            cloud.setWaitTime(10);
            cloud.setDuration(140);
            cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
            cloud.setParticle(ParticleTypes.ITEM_SLIME);
            cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
            cloud.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0));
            this.level().addFreshEntity(cloud);
        }

        @Override
        public void die(DamageSource source) {
            super.die(source);
            burst(this);
        }
    }
}
