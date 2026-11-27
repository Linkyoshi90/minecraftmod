package com.linkyoshi90.harmontide.leviathan;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;

/**
 * The Dream-Leviathan's calf, hatched from the egg she gives (leviathan/DreamEggItem): a little sky whale who floats after the player
 * who hatched her. While you are near her she wraps you in Absorption. She grows over {@link #GROWS_FOR} ticks (three days) from a
 * hand's span to the size of a horse, and no bigger. Crouch and use her to have her stay or follow. If she is ever killed she curls
 * back up into her egg, to be hatched again.
 */
public class DreamCalf extends PathfinderMob implements OwnableEntity {
    public static final int GROWS_FOR = 72000;
    public static final float SMALLEST = 0.35F;
    /** How near you must be for her Absorption, and how often she gives it. */
    public static final double AURA = 10.0;
    public static final int AURA_EVERY = 80;

    private static final EntityDataAccessor<Float> GROWTH = SynchedEntityData.defineId(DreamCalf.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> STAY = SynchedEntityData.defineId(DreamCalf.class, EntityDataSerializers.BOOLEAN);
    @Nullable
    private UUID owner;

    public DreamCalf(EntityType<? extends DreamCalf> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.setNoGravity(true);
        this.setPersistenceRequired();
        this.applyScale();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.FLYING_SPEED, 0.5)
                .add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(GROWTH, 0.0F);
        b.define(STAY, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FollowGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomFlyingGoal(this, 0.6) {
            @Override
            public boolean canUse() {
                return !DreamCalf.this.staying() && super.canUse();
            }
        });
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    public float growth() {
        return this.entityData.get(GROWTH);
    }

    public void setGrowth(float g) {
        this.entityData.set(GROWTH, Mth.clamp(g, 0, 1));
        this.applyScale();
    }

    private void applyScale() {
        var scale = this.getAttribute(Attributes.SCALE);
        if (scale != null) scale.setBaseValue(SMALLEST + (1 - SMALLEST) * this.growth());
    }

    public boolean staying() {
        return this.entityData.get(STAY);
    }

    public void setStaying(boolean stay) {
        this.entityData.set(STAY, stay);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.owner;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            if (this.random.nextInt(4) == 0) {
                this.level().addParticle(ParticleTypes.END_ROD, this.getX() + this.random.nextGaussian() * this.getBbWidth() * 0.5,
                        this.getY() + this.getBbHeight() * 0.5, this.getZ() + this.random.nextGaussian() * this.getBbWidth() * 0.5, 0, -0.01, 0);
            }
            return;
        }
        if (this.growth() < 1 && this.tickCount % 20 == 0) this.setGrowth(this.growth() + 20.0F / GROWS_FOR);
        if (this.tickCount % 40 == 0 && this.getHealth() < this.getMaxHealth()) this.heal(1.0F);
        if (this.tickCount % AURA_EVERY == 0 && this.getOwner() instanceof Player p && p.distanceTo(this) <= AURA && !p.hasEffect(MobEffects.ABSORPTION)) {
            p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, this.growth() >= 1 ? 1 : 0, true, true), this);
        }
    }

    @Override
    public void travel(Vec3 input) {
        if (this.isControlledByLocalInstance()) {
            this.moveRelative(this.getSpeed(), input);
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.88));
        }
        this.calculateEntityAnimation(false);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!player.getUUID().equals(this.owner)) return InteractionResult.PASS;
        if (!this.level().isClientSide && player.isCrouching()) {
            boolean stay = !this.staying();
            this.entityData.set(STAY, stay);
            this.getNavigation().stop();
            player.displayClientMessage(Component.translatable(stay ? "message.harmontide.dream_calf.stay" : "message.harmontide.dream_calf.follow"), true);
            return InteractionResult.SUCCESS;
        }
        if (!this.level().isClientSide && this.level() instanceof ServerLevel s) {
            s.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight(), this.getZ(), 3, 0.4, 0.2, 0.4, 0);
            this.playSound(SoundEvents.ALLAY_AMBIENT_WITH_ITEM, 1.0F, 0.5F);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() != null && source.getEntity().getUUID().equals(this.owner)) return false;
        return super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float distance, float mult, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
    }

    /** Killed, she curls back into her egg instead of being lost. */
    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) this.spawnAtLocation(new ItemStack(LeviathanRegistry.DREAM_EGG.get()));
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel level, DamageSource source) {
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM;
    }

    @Override
    public float getVoicePitch() {
        return 0.45F + 0.2F * (1 - this.growth());
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ALLAY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ALLAY_DEATH;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.owner != null) tag.putUUID("Owner", this.owner);
        tag.putFloat("Growth", this.growth());
        tag.putBoolean("Stay", this.staying());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        this.setGrowth(tag.getFloat("Growth"));
        this.entityData.set(STAY, tag.getBoolean("Stay"));
    }

    /** Floats after her owner, and comes straight to them if left far behind. */
    static final class FollowGoal extends Goal {
        private final DreamCalf calf;

        FollowGoal(DreamCalf calf) {
            this.calf = calf;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Nullable
        private LivingEntity owner() {
            return this.calf.getOwner() instanceof LivingEntity l && l.level() == this.calf.level() && !l.isSpectator() ? l : null;
        }

        @Override
        public boolean canUse() {
            LivingEntity o = this.owner();
            return !this.calf.staying() && o != null && this.calf.distanceTo(o) > 5.0;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity o = this.owner();
            return !this.calf.staying() && o != null && this.calf.distanceTo(o) > 2.5;
        }

        @Override
        public void tick() {
            LivingEntity o = this.owner();
            if (o == null) return;
            this.calf.getLookControl().setLookAt(o, 10, this.calf.getMaxHeadXRot());
            if (this.calf.distanceTo(o) > 24) {
                Vec3 to = o.position().add(o.getLookAngle().scale(-2)).add(0, 1.5, 0);
                this.calf.moveTo(to.x, to.y, to.z, this.calf.getYRot(), 0);
                this.calf.getNavigation().stop();
                return;
            }
            if (this.calf.tickCount % 10 == 0) this.calf.getNavigation().moveTo(o.getX(), o.getY() + 1.6, o.getZ(), 1.4);
        }

        @Override
        public void stop() {
            this.calf.getNavigation().stop();
        }
    }

    /** The calf's owner as an entity, if loaded. */
    @Nullable
    @Override
    public LivingEntity getOwner() {
        if (this.owner == null) return null;
        Entity e = this.level() instanceof ServerLevel s ? s.getEntity(this.owner) : this.level().getPlayerByUUID(this.owner);
        return e instanceof LivingEntity l ? l : null;
    }
}
