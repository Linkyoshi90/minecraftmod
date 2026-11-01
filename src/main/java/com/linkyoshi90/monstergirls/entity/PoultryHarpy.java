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
 * The poultry harpy (HARMONTIDE_CONCEPTS.md, twenty-eighth wave; model tools/gen_poultry_harpy.py, the crow's rig with a hen's figure): the farmyard's own
 * harpy, in a work dress and apron. Befriended, she keeps the crops around her growing and lays eggs for you.
 */
public class PoultryHarpy extends AbstractHarpy {
    public static final int FRIEND = Orders.AFFECTION;
    private static final double GUARD = 16.0;

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(PoultryHarpy.class, EntityDataSerializers.BYTE);
    private long nextEgg;

    /** Her colours (tools/gen_poultry_harpy.py). Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        WHITE("white"), BLUE("blue"), BROWN("brown"), BLACK("black"), SPECKLED("speckled");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return WHITE;
        }
    }

    public PoultryHarpy(EntityType<? extends PoultryHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.34)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkPoultryHarpySpawnRules(EntityType<PoultryHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
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

    // --- the farm ---------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel level && !this.isBaby() && this.tickCount % 200 == 0) this.tend(level);
        if (this.level() instanceof ServerLevel level && !this.isBaby() && this.tickCount % 100 == 0 && level.getGameTime() >= this.nextEgg) this.layEgg(level);
    }

    /** Her friend: a player within 16 blocks of her she is friends with, or null. */
    @Nullable
    public Player friend() {
        for (Player p : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(GUARD), p -> p.isAlive() && !p.isSpectator())) {
            if (this.getAffection(p) >= FRIEND) return p;
        }
        return null;
    }

    /** One unripe crop within 6 of her grows a stage. 1 when she tends one, 0 with nothing to tend, -1 without a friend. */
    public int tend(ServerLevel level) {
        if (this.friend() == null) return -1;
        for (BlockPos pos : BlockPos.betweenClosed(this.blockPosition().offset(-6, -2, -6), this.blockPosition().offset(6, 2, 6))) {
            if (level.getBlockState(pos).getBlock() instanceof net.minecraft.world.level.block.CropBlock crop && !crop.isMaxAge(level.getBlockState(pos))
                    && net.minecraft.world.item.BoneMealItem.growCrop(new ItemStack(Items.BONE_MEAL), level, pos.immutable())) {
                return 1;
            }
        }
        return 0;
    }

    /** An egg at her feet, about every ten minutes. 1 when she lays, -1 without a friend. */
    public int layEgg(ServerLevel level) {
        if (this.friend() == null) return -1;
        this.spawnAtLocation(Items.EGG);
        this.playSound(SoundEvents.CHICKEN_EGG, 1.0F, 1.0F);
        this.nextEgg = level.getGameTime() + 12000;
        return 1;
    }

    // --- food, bond, gifts ---------------------------------------------------------------------------------------

    @Override
    public boolean isEdible(ItemStack stack) {
        return stack.has(net.minecraft.core.component.DataComponents.FOOD) || stack.is(ModTags.CROW_HARPY_FOOD);
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return stack.is(Items.WHEAT_SEEDS) || stack.is(Items.EGG);
    }

    @Override
    public Component describeBond(Player player) {
        int a = this.getAffection(player);
        return Component.translatable("bond.monstergirls.poultry." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "shy"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 50 ? new ItemStack(Items.EGG, 3) : r < 85 ? new ItemStack(Items.FEATHER, 4) : r < 97 ? new ItemStack(Items.BREAD, 2) : new ItemStack(Items.CAKE);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.CROW_EGG.get());
    }

    @Override
    protected String species() {
        return "poultry_harpy";
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
