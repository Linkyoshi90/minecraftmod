package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.registry.ModItems;
import com.linkyoshi90.harmontide.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

/**
 * The condorune (HARMONTIDE_CONCEPTS.md, sixteenth wave; model tools/gen_condorune_harpy.py, the crow's rig in condor
 * colours with a white ruff): she rides the mountain updrafts, and when her friend falls more than six blocks within 16
 * of her she catches them: Slow Falling, and the fall so far forgiven.
 */
public class CondoruneHarpy extends AbstractHarpy {
    public static final int FRIEND = Orders.AFFECTION;
    /** How far her friend must have fallen before she swoops. */
    public static final float CATCH_FALL = 6.0F;

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(CondoruneHarpy.class, EntityDataSerializers.BYTE);

    /** Her colours (tools/gen_condorune_harpy.py). Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        ANDEAN("andean"), CALIFORNIA("california"), KING("king"), GRIFFON("griffon"), BEARDED("bearded");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return ANDEAN;
        }
    }

    public CondoruneHarpy(EntityType<? extends CondoruneHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 18.0)
                .add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.FLYING_SPEED, 0.7)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    public static boolean checkCondoruneHarpySpawnRules(EntityType<CondoruneHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
                                                        RandomSource random) {
        return level.getBlockState(pos.below()).isSolid() && level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, (byte) 0);
    }

    public Variant getVariant() {
        return Variant.values()[this.entityData.get(DATA_VARIANT)];
    }

    public void setVariant(Variant variant) {
        this.entityData.set(DATA_VARIANT, (byte) variant.ordinal());
    }

    public String getVariantSuffix() {
        return this.getVariant().ordinal() == 0 ? "" : "_" + this.getVariant().getSerializedName();
    }

    @Override
    public void plainVariant() {
        this.setVariant(Variant.values()[0]);
    }

    @Override
    public void randomizeVariant() {
        int r = this.random.nextInt(100);
        this.setVariant(r < 50 ? Variant.ANDEAN : r < 65 ? Variant.GRIFFON : r < 80 ? Variant.CALIFORNIA : r < 92 ? Variant.BEARDED : Variant.KING);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    // --- the catch -------------------------------------------------------------------------------------------------

    /** Her friend within 16 blocks, falling and already more than six blocks down: caught. The friend, or null. */
    @Nullable
    public Player catchFall(ServerLevel level) {
        Player friend = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 16.0,
                p -> !p.isSpectator() && this.getAffection((Player) p) >= FRIEND && p.fallDistance > CATCH_FALL && !p.onGround());
        if (friend == null || friend.hasEffect(MobEffects.SLOW_FALLING)) return null;
        friend.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, false, true));
        friend.resetFallDistance();
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, friend.getX(), friend.getY() + 1, friend.getZ(), 12, 0.5, 0.3, 0.5, 0.02);
        this.getLookControl().setLookAt(friend);
        this.startTimedAction(Action.CAWING, 30);
        this.playSound(SoundEvents.PHANTOM_FLAP, 1.0F, 0.6F);
        return friend;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel level && !this.isBaby()) this.catchFall(level);
    }

    // --- food, bond, gifts ---------------------------------------------------------------------------------------

    @Override
    public boolean isEdible(ItemStack stack) {
        return stack.has(DataComponents.FOOD);
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return stack.is(Items.BONE) || stack.is(Items.FEATHER);
    }

    @Override
    public Component describeBond(Player player) {
        int a = this.getAffection(player);
        return Component.translatable("bond.harmontide.condorune." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "shy"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 40 ? new ItemStack(Items.FEATHER, 4) : r < 70 ? new ItemStack(Items.BONE, 2) : r < 90 ? new ItemStack(Items.PHANTOM_MEMBRANE)
                : new ItemStack(Items.EMERALD);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.CROW_EGG.get());
    }

    @Override
    protected String species() {
        return "condorune_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case BATHING -> "bathe";
            case CAWING, SINGING -> "caw";
            case CACHING -> "cache";
            case KNOCKING -> "knock";
            case PRANKING -> "prank";
            case SITTING -> "sit";
            case LYING, SLEEPING -> "lie";
            default -> null;
        };
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Variant", this.getVariant().getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setVariant(Variant.byName(tag.getString("Variant")));
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CROW_CAW.get();
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.7F;                               // a deeper croak than the crow's
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PARROT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PARROT_DEATH;
    }

    @Override
    protected SoundEvent getEatSound() {
        return SoundEvents.PARROT_EAT;
    }
}
