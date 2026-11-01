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
 * The bennu (HARMONTIDE_CONCEPTS.md, twenty-sixth wave; model tools/gen_bennu_harpy.py, the flamingo's rig): the sacred heron of Heliopolis, who
 * rose with the first sun. At sunrise she heals her friend whole, and once a day, whoever kills her, she gets up again.
 */
public class BennuHarpy extends AbstractHarpy {
    public static final int FRIEND = Orders.AFFECTION;
    private static final double GUARD = 16.0;

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(BennuHarpy.class, EntityDataSerializers.BYTE);
    /** The last sunrise she healed at, and the last day she got up again (-1: never). */
    private long lastSunrise = -1;
    private long lastRise = -1;

    /** Her colours (tools/gen_bennu_harpy.py). Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        BLUE("blue"), GREY("grey"), GOLD("gold"), WHITE("white"), EMBER("ember");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return BLUE;
        }
    }

    public BennuHarpy(EntityType<? extends BennuHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.42)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkBennuHarpySpawnRules(EntityType<BennuHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
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

    // --- the sunrise ---------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel level && !this.isBaby() && this.tickCount % 100 == 0 && level.getDayTime() % 24000L < 2000L) this.sunrise(level);
    }

    /** Her friend: a player within 16 blocks of her she is friends with, or null. */
    @Nullable
    public Player friend() {
        for (Player p : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(GUARD), p -> p.isAlive() && !p.isSpectator())) {
            if (this.getAffection(p) >= FRIEND) return p;
        }
        return null;
    }

    /** Once a day, all her friend's health back. 1 when she heals, 0 when she already has today, -1 without a friend. */
    public int sunrise(ServerLevel level) {
        Player friend = this.friend();
        if (friend == null) return -1;
        long day = level.getDayTime() / 24000L;
        if (day == this.lastSunrise) return 0;
        this.lastSunrise = day;
        friend.heal(friend.getMaxHealth());
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, friend.getX(), friend.getY() + 1.0, friend.getZ(), 12, 0.4, 0.5, 0.4, 0.02);
        this.getLookControl().setLookAt(friend);
        return 1;
    }

    /** Whether a blow that would kill her today lets her get up again instead (once a day). */
    public boolean rise(ServerLevel level) {
        long day = level.getDayTime() / 24000L;
        if (day == this.lastRise) return false;
        this.lastRise = day;
        this.setHealth(this.getMaxHealth());
        this.clearFire();
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME, this.getX(), this.getY() + 1.0, this.getZ(), 30, 0.4, 0.7, 0.4, 0.05);
        this.playSound(SoundEvents.TOTEM_USE, 0.6F, 1.3F);
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level() instanceof ServerLevel level && amount >= this.getHealth() && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY) && this.rise(level)) return false;
        return super.hurt(source, amount);
    }

    // --- food, bond, gifts ---------------------------------------------------------------------------------------

    @Override
    public boolean isEdible(ItemStack stack) {
        return stack.has(net.minecraft.core.component.DataComponents.FOOD) || stack.is(ModTags.CROW_HARPY_FOOD);
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return stack.is(Items.GOLD_NUGGET) || stack.is(Items.COD);
    }

    @Override
    public Component describeBond(Player player) {
        int a = this.getAffection(player);
        return Component.translatable("bond.monstergirls.bennu." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "shy"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 50 ? new ItemStack(Items.FEATHER, 3) : r < 80 ? new ItemStack(Items.COD, 2) : r < 95 ? new ItemStack(Items.GOLD_NUGGET, 4) : new ItemStack(Items.GOLDEN_CARROT, 2);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.CROW_EGG.get());
    }

    @Override
    protected String species() {
        return "bennu_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case SINGING -> "sing";
            case BATHING -> "bathe";
            case PERCHING -> "stand1";
            case DISPLAYING -> "march";
            default -> null;
        };
    }


    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Variant", this.getVariant().getSerializedName());
        tag.putLong("LastSunrise", this.lastSunrise);
        tag.putLong("LastRise", this.lastRise);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setVariant(Variant.byName(tag.getString("Variant")));
        this.lastSunrise = tag.contains("LastSunrise") ? tag.getLong("LastSunrise") : -1;
        this.lastRise = tag.contains("LastRise") ? tag.getLong("LastRise") : -1;
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
