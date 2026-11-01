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
 * La Lechuza (HARMONTIDE_CONCEPTS.md, twenty-sixth wave; model tools/gen_lechuza_harpy.py, the owl's rig): the witch-owl of Mexico and South Texas, whose
 * cry at night warns of what is coming. Befriended, at night her cry shows her friend the monsters close to them and drives them back.
 */
public class LechuzaHarpy extends AbstractHarpy {
    public static final int FRIEND = Orders.AFFECTION;
    private static final double GUARD = 16.0;

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(LechuzaHarpy.class, EntityDataSerializers.BYTE);

    /** Her colours (tools/gen_lechuza_harpy.py). Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        BROWN("brown"), ASH("ash"), WHITE("white"), BLACK("black"), RED("red");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return BROWN;
        }
    }

    public LechuzaHarpy(EntityType<? extends LechuzaHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FLYING_SPEED, 0.42)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkLechuzaHarpySpawnRules(EntityType<LechuzaHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
                                                       RandomSource random) {
        return level.getBlockState(pos.below()).isSolid();
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

    // --- the cry ---------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel level && !this.isBaby() && this.tickCount % 60 == 0) this.cry(level, !level.isDay());
    }

    /** Her friend: a player within 16 blocks of her she is friends with, or null. */
    @Nullable
    public Player friend() {
        for (Player p : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(GUARD), p -> p.isAlive() && !p.isSpectator())) {
            if (this.getAffection(p) >= FRIEND) return p;
        }
        return null;
    }

    /** At night: monsters within 8 of her friend glow, slow and are thrown back. Returns how many; 0 by day; -1 without a friend. */
    public int cry(ServerLevel level, boolean night) {
        Player friend = this.friend();
        if (friend == null) return -1;
        if (!night) return 0;
        int n = 0;
        for (Monster m : level.getEntitiesOfClass(Monster.class, friend.getBoundingBox().inflate(8))) {
            Vec3 away = m.position().subtract(friend.position()).multiply(1, 0, 1);
            away = (away.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : away).normalize();
            m.setDeltaMovement(away.x * 1.0, 0.3, away.z * 1.0);
            m.hurtMarked = true;
            m.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0));
            m.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            n++;
        }
        if (n > 0) this.playSound(SoundEvents.PARROT_IMITATE_PHANTOM, 1.0F, 0.7F);
        return n;
    }

    // --- food, bond, gifts ---------------------------------------------------------------------------------------

    @Override
    public boolean isEdible(ItemStack stack) {
        return stack.has(net.minecraft.core.component.DataComponents.FOOD) || stack.is(ModTags.CROW_HARPY_FOOD);
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return stack.is(Items.RABBIT_FOOT) || stack.is(Items.BONE);
    }

    @Override
    public Component describeBond(Player player) {
        int a = this.getAffection(player);
        return Component.translatable("bond.monstergirls.lechuza." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "shy"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 50 ? new ItemStack(Items.FEATHER, 3) : r < 80 ? new ItemStack(Items.BONE, 2) : r < 95 ? new ItemStack(Items.RABBIT_FOOT) : new ItemStack(Items.ENDER_PEARL);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.CROW_EGG.get());
    }

    @Override
    protected String species() {
        return "lechuza_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case HOOTING -> "hoot";
            case SLEEPING -> "sleep";
            case POUNCING -> "pounce";
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
