package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
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
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A flamingo of the warm shallows (HARMONTIDE_CONCEPTS.md, eighth wave). She stands on one leg, and now and then the
 * whole flock marches together. Her pink is her diet: every day without fish she fades a shade (pink, pale, white), and
 * fish bring it back a shade at a time.
 */
public class FlamingoHarpy extends AbstractHarpy {
    public static final int FRIEND = Orders.AFFECTION;
    /** A day without fish fades her a shade. */
    public static final long FADE_TICKS = 24000L;

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(FlamingoHarpy.class, EntityDataSerializers.BYTE);

    /** Her shades (tools/gen_flamingo_harpy.py), from her diet. Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        PINK("pink"), PALE("pale"), WHITE("white");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return PINK;
        }
    }

    private long lastFish = -1;

    public FlamingoHarpy(EntityType<? extends FlamingoHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FLYING_SPEED, 0.5)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkFlamingoHarpySpawnRules(EntityType<FlamingoHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
                                                       RandomSource random) {
        return (level.getBlockState(pos.below()).isSolid() || level.getFluidState(pos).isSource()) && level.getRawBrightness(pos, 0) > 8;
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
        this.setVariant(Variant.PINK);                      // a wild flamingo is well fed
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    // --- diet, one leg, the march ---------------------------------------------------------------------------------

    /** She ate: fish bring her colour back a shade. */
    @Override
    protected void onAte(ItemStack food) {
        super.onAte(food);
        if (food.is(ItemTags.FISHES)) this.eatFish();
    }

    public void eatFish() {
        this.lastFish = this.level().getGameTime();
        if (this.getVariant() != Variant.PINK) this.setVariant(Variant.values()[this.getVariant().ordinal() - 1]);
    }

    /** A day without fish: a shade paler. */
    public void fade() {
        if (this.getVariant() != Variant.WHITE) this.setVariant(Variant.values()[this.getVariant().ordinal() + 1]);
    }

    /** The flock's march: she and every flamingo around her (three at least) step out together. */
    public boolean startMarch(ServerLevel level) {
        List<FlamingoHarpy> flock = level.getEntitiesOfClass(FlamingoHarpy.class, this.getBoundingBox().inflate(8), f -> !f.isBaby() && f.getTarget() == null);
        if (flock.size() < 3) return false;
        for (FlamingoHarpy f : flock) f.startTimedAction(Action.DISPLAYING, 60);
        this.playSound(SoundEvents.PARROT_AMBIENT, 1.0F, 0.6F);
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server) || this.isBaby()) return;
        if (this.lastFish < 0) this.lastFish = server.getGameTime();
        if (server.getGameTime() - this.lastFish > FADE_TICKS) {
            this.fade();
            this.lastFish = server.getGameTime();
        }
        if (this.tickCount % 40 == 0 && this.getAction() == Action.NONE && this.getNavigation().isDone() && this.onGround()) {
            if (this.random.nextInt(30) == 0) this.startMarch(server);
            else if (this.random.nextInt(3) == 0) this.startTimedAction(Action.PERCHING, 160);     // on one leg
        }
    }

    // --- food, bond, gifts ---------------------------------------------------------------------------------------

    @Override
    public boolean isEdible(ItemStack stack) {
        return stack.is(ItemTags.FISHES);
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return false;
    }

    @Override
    public Component describeBond(Player player) {
        int a = this.getAffection(player);
        return Component.translatable("bond.monstergirls.flamingo." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "shy"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 40 ? new ItemStack(Items.FEATHER, 2) : r < 75 ? new ItemStack(Items.PINK_DYE, 2) : r < 95 ? new ItemStack(Items.SALMON) : new ItemStack(Items.PINK_TULIP);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.FLAMINGO_EGG.get());
    }

    @Override
    protected String species() {
        return "flamingo_harpy";
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
        tag.putLong("LastFish", this.lastFish);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setVariant(Variant.byName(tag.getString("Variant")));
        this.lastFish = tag.contains("LastFish") ? tag.getLong("LastFish") : -1;
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
