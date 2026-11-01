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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

/**
 * The yatagarasu (HARMONTIDE_CONCEPTS.md, twenty-fifth wave; model tools/gen_yatagarasu_harpy.py, the crow's rig): the sun's crow of
 * Japan, who guided the first emperor. Black wings, a pale blue kimono. Befriended, at dawn she gives her friend (within 16 of her)
 * Regeneration and Speed for the first minutes of the day.
 */
public class YatagarasuHarpy extends AbstractHarpy {
    public static final int FRIEND = Orders.AFFECTION;
    private static final double GUARD = 16.0;

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(YatagarasuHarpy.class, EntityDataSerializers.BYTE);
    /** The last dawn she blessed (-1: never). */
    private long lastDawn = -1;

    /** Her colours (tools/gen_yatagarasu_harpy.py). Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        BLACK("black"), SUN("sun"), SHRINE("shrine"), NIGHT("night"), WHITE("white");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return BLACK;
        }
    }

    public YatagarasuHarpy(EntityType<? extends YatagarasuHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.45)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkYatagarasuHarpySpawnRules(EntityType<YatagarasuHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
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
        this.setVariant(r < 60 ? Variant.BLACK : r < 78 ? Variant.SUN : r < 90 ? Variant.SHRINE : r < 97 ? Variant.NIGHT : Variant.WHITE);
    }

    // --- the dawn ---------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel level && !this.isBaby() && this.tickCount % 100 == 0 && level.getDayTime() % 24000L < 2000L) this.dawn(level);
    }

    /** Her friend: a player within 16 blocks of her she is friends with, or null. */
    @Nullable
    public Player friend() {
        for (Player p : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(GUARD), p -> p.isAlive() && !p.isSpectator())) {
            if (this.getAffection(p) >= FRIEND) return p;
        }
        return null;
    }

    /** Once a day, Regeneration and Speed for three minutes. 1 when she blessed, 0 when she already has today, -1 without a friend. */
    public int dawn(ServerLevel level) {
        Player friend = this.friend();
        if (friend == null) return -1;
        long day = level.getDayTime() / 24000L;
        if (day == this.lastDawn) return 0;
        this.lastDawn = day;
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 200, 0));
        friend.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 3600, 0));
        this.getLookControl().setLookAt(friend);
        this.startTimedAction(Action.CAWING, 20);
        return 1;
    }

    // --- food, bond, gifts ---------------------------------------------------------------------------------------

    @Override
    public boolean isEdible(ItemStack stack) {
        return stack.has(net.minecraft.core.component.DataComponents.FOOD) || stack.is(ModTags.CROW_HARPY_FOOD);
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return stack.is(Items.GOLD_NUGGET) || stack.is(Items.SUNFLOWER);
    }

    @Override
    public Component describeBond(Player player) {
        int a = this.getAffection(player);
        return Component.translatable("bond.monstergirls.yatagarasu." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "shy"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 50 ? new ItemStack(Items.FEATHER, 3) : r < 80 ? new ItemStack(Items.GOLD_NUGGET, 4) : r < 95 ? new ItemStack(Items.SUNFLOWER) : new ItemStack(Items.GOLDEN_APPLE);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.CROW_EGG.get());
    }

    @Override
    protected String species() {
        return "yatagarasu_harpy";
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
        tag.putLong("LastDawn", this.lastDawn);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setVariant(Variant.byName(tag.getString("Variant")));
        this.lastDawn = tag.contains("LastDawn") ? tag.getLong("LastDawn") : -1;
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
