package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.registry.ModItems;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;

/**
 * A sociable fruit-eater of the jungle canopy (HARMONTIDE_CONCEPTS.md, fourth wave). She talks by clacking her big
 * beak, and two toucans who meet now and then hold a clack-off: they face each other and clack back and forth, just
 * for the fun of it. Fruit wins her over, and friends get fruit back.
 */
public class ToucanHarpy extends AbstractHarpy {
    public static final int FRIEND = com.linkyoshi90.monstergirls.entity.Orders.AFFECTION;
    /** How long a clack-off lasts. */
    public static final int CLACK_OFF_TICKS = 120;
    private static final Set<Item> FRUIT = Set.of(Items.APPLE, Items.MELON_SLICE, Items.SWEET_BERRIES, Items.GLOW_BERRIES, Items.CHORUS_FRUIT,
            Items.COCOA_BEANS);

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(ToucanHarpy.class, EntityDataSerializers.BYTE);

    /** Texture variants (tools/gen_toucan_harpy.py). Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        TOCO("toco"), KEEL_BILLED("keel_billed"), CHANNEL_BILLED("channel_billed"), ARACARI("aracari"), EMERALD("emerald");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return TOCO;
        }
    }

    @Nullable
    private ToucanHarpy clackPartner;
    private int clackTicks;

    public ToucanHarpy(EntityType<? extends ToucanHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FLYING_SPEED, 0.5)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkToucanHarpySpawnRules(EntityType<ToucanHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
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
        this.setVariant(Variant.values()[this.random.nextInt(Variant.values().length)]);
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty,
                                                                   MobSpawnType spawnType, @Nullable net.minecraft.world.entity.SpawnGroupData group) {
        if (!this.hasVariantFromData()) this.randomizeVariant();
        return super.finalizeSpawn(level, difficulty, spawnType, group);
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

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(4, new ClackOffGoal());
        this.goalSelector.addGoal(6, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    // --- clacking --------------------------------------------------------------------------------------------------

    /** One clack of her beak: a hollow wooden knock (higher for a small bird). */
    public void clack() {
        this.startTimedAction(Action.CLACKING, 16);
        this.playSound(SoundEvents.BAMBOO_WOOD_BUTTON_CLICK_ON, 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
    }

    /** Her chatter is clacking. */
    @Override
    public void playAmbientSound() {
        super.playAmbientSound();
        if (!this.level().isClientSide) this.clack();
    }

    public boolean isClackingOff() {
        return this.clackPartner != null;
    }

    @Nullable
    public ToucanHarpy getClackPartner() {
        return this.clackPartner;
    }

    /** Two toucans face off and clack at each other for a while. */
    public void startClackOff(ToucanHarpy other) {
        this.clackPartner = other;
        other.clackPartner = this;
        this.clackTicks = other.clackTicks = CLACK_OFF_TICKS;
    }

    private boolean free() {
        return !this.isBaby() && !this.isOrderedToSit() && this.getTarget() == null && this.clackPartner == null
                && this.getOrders().command(this) == com.linkyoshi90.monstergirls.entity.Orders.Command.FREE_ROAM;
    }

    class ClackOffGoal extends Goal {
        ClackOffGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            ToucanHarpy t = ToucanHarpy.this;
            if (t.clackPartner != null) return true;                    // the other one started it
            if (!t.free() || t.tickCount % 20 != 0 || t.random.nextInt(30) != 0) return false;
            ToucanHarpy other = t.level().getEntitiesOfClass(ToucanHarpy.class, t.getBoundingBox().inflate(8), o -> o != t && o.free())
                    .stream().findFirst().orElse(null);
            if (other == null) return false;
            t.startClackOff(other);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            ToucanHarpy p = ToucanHarpy.this.clackPartner;
            return p != null && p.isAlive() && ToucanHarpy.this.clackTicks > 0 && ToucanHarpy.this.getTarget() == null;
        }

        @Override
        public void stop() {
            ToucanHarpy t = ToucanHarpy.this;
            if (t.clackPartner != null && t.clackPartner.clackPartner == t) t.clackPartner.clackPartner = null;
            t.clackPartner = null;
            t.clackTicks = 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            ToucanHarpy t = ToucanHarpy.this;
            ToucanHarpy p = t.clackPartner;
            if (p == null) return;
            t.clackTicks--;
            t.getLookControl().setLookAt(p, 30.0F, 30.0F);
            if (t.distanceToSqr(p) > 2.5 * 2.5) t.getNavigation().moveTo(p, 1.0);
            else t.getNavigation().stop();
            // they take turns: one clacks on the even beat, the other on the odd one
            boolean myTurn = (t.clackTicks / 10) % 2 == (t.getId() < p.getId() ? 0 : 1);
            if (t.clackTicks % 10 == 0 && myTurn) {
                t.clack();
                if (t.level() instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.NOTE, t.getX(), t.getY() + t.getBbHeight() + 0.3, t.getZ(), 0, t.random.nextDouble(), 0, 0, 1);
                }
            }
        }
    }

    // --- food, bond, gifts ---------------------------------------------------------------------------------------

    @Override
    public boolean isEdible(ItemStack stack) {
        return FRUIT.contains(stack.getItem());
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return false;
    }

    @Override
    public Component describeBond(Player player) {
        int a = this.getAffection(player);
        return Component.translatable("bond.monstergirls.toucan." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "shy"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 30 ? new ItemStack(Items.COCOA_BEANS, 3) : r < 60 ? new ItemStack(Items.MELON_SLICE, 2) : r < 85 ? new ItemStack(Items.GLOW_BERRIES, 2)
                : new ItemStack(Items.APPLE);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.TOUCAN_EGG.get());
    }

    @Override
    protected String species() {
        return "toucan_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case CLACKING -> "clack";
            case SINGING -> "sing";
            default -> null;
        };
    }

    @Override
    protected boolean actionShowsInFlight(Action action) {
        return action == Action.CLACKING || super.actionShowsInFlight(action);
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

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
