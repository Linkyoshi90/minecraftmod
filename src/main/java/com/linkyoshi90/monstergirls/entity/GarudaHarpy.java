package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.registry.ModItems;
import com.linkyoshi90.monstergirls.registry.ModTags;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * Garuda (HARMONTIDE_CONCEPTS.md, twenty-sixth wave; model tools/gen_garuda_harpy.py, the crow's rig): the king of birds in the Hindu and Buddhist
 * epics, the enemy of every serpent. Poison does not hold on her, she cleanses it from her friend, and she strikes down anything crawling and serpentine on sight.
 */
public class GarudaHarpy extends AbstractHarpy {
    public static final int FRIEND = Orders.AFFECTION;
    private static final double GUARD = 16.0;

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(GarudaHarpy.class, EntityDataSerializers.BYTE);

    /** Her colours (tools/gen_garuda_harpy.py). Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        SUN("sun"), CRIMSON("crimson"), EMERALD("emerald"), WHITE("white"), NIGHT("night");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return SUN;
        }
    }

    public GarudaHarpy(EntityType<? extends GarudaHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.46)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkGarudaHarpySpawnRules(EntityType<GarudaHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
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
        this.setVariant(r < 60 ? Variant.values()[0] : r < 78 ? Variant.values()[1] : r < 90 ? Variant.values()[2] : r < 97 ? Variant.values()[3] : Variant.values()[4]);
    }

    // --- the serpents ---------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel level && !this.isBaby() && this.tickCount % 20 == 0) this.serpentBane(level);
    }

    /** Her friend: a player within 16 blocks of her she is friends with, or null. */
    @Nullable
    public Player friend() {
        for (Player p : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(GUARD), p -> p.isAlive() && !p.isSpectator())) {
            if (this.getAffection(p) >= FRIEND) return p;
        }
        return null;
    }

    /** Silverfish, endermites and wild hostile lamias: what counts as a serpent to her. */
    private static boolean serpent(LivingEntity e) {
        if (e instanceof net.minecraft.world.entity.monster.Silverfish || e instanceof net.minecraft.world.entity.monster.Endermite) return true;
        return e instanceof com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl g && g.getSpecies().family() == com.linkyoshi90.monstergirls.entity.girl.GirlSpecies.Family.LAMIA
                && g.getType().getCategory() == net.minecraft.world.entity.MobCategory.MONSTER && g.bondedPlayer(1, 64) == null;
    }

    /** Clears Poison from her friend, and strikes the nearest serpent within 12 of her for 10. Returns how many she struck (0 or 1). */
    public int serpentBane(ServerLevel level) {
        Player friend = this.friend();
        if (friend != null) friend.removeEffect(MobEffects.POISON);
        LivingEntity prey = null;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(12), e -> e.isAlive() && serpent(e))) {
            if (prey == null || e.distanceToSqr(this) < prey.distanceToSqr(this)) prey = e;
        }
        if (prey == null) return 0;
        prey.hurt(level.damageSources().mobAttack(this), 10.0F);
        this.getLookControl().setLookAt(prey);
        this.startTimedAction(Action.CAWING, 12);
        return 1;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return effect.getEffect() != MobEffects.POISON && super.canBeAffected(effect);
    }

    // --- food, bond, gifts ---------------------------------------------------------------------------------------

    @Override
    public boolean isEdible(ItemStack stack) {
        return stack.has(net.minecraft.core.component.DataComponents.FOOD) || stack.is(ModTags.CROW_HARPY_FOOD);
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return stack.is(Items.GOLD_NUGGET) || stack.is(Items.GOLD_INGOT);
    }

    @Override
    public Component describeBond(Player player) {
        int a = this.getAffection(player);
        return Component.translatable("bond.monstergirls.garuda." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "shy"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 50 ? new ItemStack(Items.FEATHER, 3) : r < 80 ? new ItemStack(Items.GOLD_NUGGET, 6) : r < 95 ? new ItemStack(Items.MILK_BUCKET) : new ItemStack(Items.GOLD_INGOT, 2);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.CROW_EGG.get());
    }

    @Override
    protected String species() {
        return "garuda_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case BATHING -> "bathe";
            case CAWING, SINGING -> "caw";
            case CACHING -> "cache";
            case KNOCKING -> "knock";
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
        return SoundEvents.PARROT_AMBIENT;
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
