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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * The landharpy (HARMONTIDE_CONCEPTS.md, twenty-eighth wave; model tools/gen_landharpy_harpy.py, the ostrich's rig): a harpy who never bothered with the
 * sky. Very fast, and befriended her talon kick stuns the monsters near her friend.
 */
public class LandharpyHarpy extends AbstractHarpy {
    public static final int FRIEND = Orders.AFFECTION;
    private static final double GUARD = 16.0;

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(LandharpyHarpy.class, EntityDataSerializers.BYTE);
    private long nextKick;

    /** Her colours (tools/gen_landharpy_harpy.py). Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        WHITE("white"), BROWN("brown"), ASH("ash"), RED("red"), BLACK("black");

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

    public LandharpyHarpy(EntityType<? extends LandharpyHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 22.0)
                .add(Attributes.MOVEMENT_SPEED, 0.36)
                .add(Attributes.FLYING_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkLandharpyHarpySpawnRules(EntityType<LandharpyHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
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
        if (this.level() instanceof ServerLevel level && !this.isBaby() && this.tickCount % 10 == 0 && level.getGameTime() >= this.nextKick) this.stomp(level);
    }

    /** Her friend: a player within 16 blocks of her she is friends with, or null. */
    @Nullable
    public Player friend() {
        for (Player p : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(GUARD), p -> p.isAlive() && !p.isSpectator())) {
            if (this.getAffection(p) >= FRIEND) return p;
        }
        return null;
    }

    /** The nearest monster within 4 of her, near her friend, takes 6 and staggers. 1 on a hit, 0 with none near, -1 without a friend. */
    public int stomp(ServerLevel level) {
        Player friend = this.friend();
        if (friend == null) return -1;
        Monster near = null;
        for (Monster m : level.getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(4), Monster::isAlive)) {
            if (near == null || m.distanceToSqr(this) < near.distanceToSqr(this)) near = m;
        }
        if (near == null) return 0;
        near.hurt(this.damageSources().mobAttack(this), 6);
        near.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3));
        near.knockback(1.0, this.getX() - near.getX(), this.getZ() - near.getZ());
        this.startTimedAction(Action.KICKING, 12);
        this.nextKick = level.getGameTime() + 60;
        return 1;
    }

    // --- food, bond, gifts ---------------------------------------------------------------------------------------

    @Override
    public boolean isEdible(ItemStack stack) {
        return stack.has(net.minecraft.core.component.DataComponents.FOOD) || stack.is(ModTags.CROW_HARPY_FOOD);
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return stack.is(Items.WHEAT_SEEDS) || stack.is(Items.STRING);
    }

    @Override
    public Component describeBond(Player player) {
        int a = this.getAffection(player);
        return Component.translatable("bond.monstergirls.landharpy." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "shy"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 50 ? new ItemStack(Items.FEATHER, 4) : r < 85 ? new ItemStack(Items.WHEAT_SEEDS, 6) : r < 97 ? new ItemStack(Items.LEATHER, 2) : new ItemStack(Items.IRON_INGOT);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.CROW_EGG.get());
    }

    @Override
    protected String species() {
        return "landharpy_harpy";
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
