package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.registry.ModItems;
import com.linkyoshi90.monstergirls.registry.ModTags;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * The emberu (HARMONTIDE_CONCEPTS.md, nineteenth wave; model tools/gen_emberu_harpy.py, the ostrich's rig in a blue emu's
 * painted colours): a tall emu of the red plains. Befriended, every second she kicks away the monsters that come within
 * 3 blocks of her friend (who is within 16 of her).
 */
public class EmberuHarpy extends AbstractHarpy {
    public static final int FRIEND = Orders.AFFECTION;
    private static final double GUARD = 16.0;

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(EmberuHarpy.class, EntityDataSerializers.BYTE);

    /** Her colours (tools/gen_emberu_harpy.py). Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        BLUE("blue"), EMBER("ember"), OCHRE("ochre"), ASH("ash"), DUSK("dusk");

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

    public EmberuHarpy(EntityType<? extends EmberuHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkEmberuHarpySpawnRules(EntityType<EmberuHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
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
    public void randomizeVariant() {
        int r = this.random.nextInt(100);
        this.setVariant(r < 60 ? Variant.BLUE : r < 75 ? Variant.EMBER : r < 87 ? Variant.OCHRE : r < 95 ? Variant.ASH : Variant.DUSK);
    }

    @Override
    protected boolean canFly() {
        return false;                                    // an emu: she runs, she cannot fly
    }

    @Override
    protected String movingAnimation() {
        return this.walkAnimation.speed() > 0.8F ? "run" : "walk";
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(6, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    // --- the kick ---------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel level && !this.isBaby() && this.tickCount % 20 == 0) this.kickAway(level);
    }

    /** Her friend: a player within 16 blocks of her she is friends with, or null. */
    @Nullable
    public Player friend() {
        for (Player p : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(GUARD), p -> p.isAlive() && !p.isSpectator())) {
            if (this.getAffection(p) >= FRIEND) return p;
        }
        return null;
    }

    /** Monsters within 3 blocks of her friend are kicked away from them. How many, or -1 without a friend. */
    public int kickAway(ServerLevel level) {
        Player friend = this.isBaby() ? null : this.friend();
        if (friend == null) return -1;
        int n = 0;
        for (Monster m : level.getEntitiesOfClass(Monster.class, friend.getBoundingBox().inflate(3))) {
            Vec3 away = m.position().subtract(friend.position()).multiply(1, 0, 1);
            if (away.lengthSqr() < 1.0E-4) away = new Vec3(1, 0, 0);
            away = away.normalize();
            m.setDeltaMovement(away.x * 1.4, 0.4, away.z * 1.4);
            m.hurtMarked = true;
            m.hurt(level.damageSources().mobAttack(this), 3.0F);
            n++;
        }
        if (n > 0) {
            this.getLookControl().setLookAt(friend);
            this.startTimedAction(Action.KICKING, 12);
            this.playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0F, 1.1F);
        }
        return n;
    }

    // --- food, bond, gifts ---------------------------------------------------------------------------------------

    @Override
    public boolean isEdible(ItemStack stack) {
        return stack.has(DataComponents.FOOD) || stack.is(ModTags.CROW_HARPY_FOOD);
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return stack.is(ModTags.CROW_HARPY_SHINIES);
    }

    @Override
    public Component describeBond(Player player) {
        int a = this.getAffection(player);
        return Component.translatable("bond.monstergirls.emberu." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "shy"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 45 ? new ItemStack(Items.FEATHER, 3) : r < 75 ? new ItemStack(Items.SWEET_BERRIES, 4) : r < 95 ? new ItemStack(Items.WHEAT_SEEDS, 6)
                : new ItemStack(Items.RED_DYE, 2);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.OSTRICH_EGG.get());
    }

    @Override
    protected String species() {
        return "emberu_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case SINGING, CAWING, DISPLAYING -> "display";
            case KICKING -> "kick";
            case BROODING, NESTING -> "brood";
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
        return SoundEvents.CHICKEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.CHICKEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CHICKEN_DEATH;
    }

    @Override
    protected SoundEvent getEatSound() {
        return SoundEvents.GENERIC_EAT;
    }
}
