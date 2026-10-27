package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.registry.ModItems;
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
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * The pumpkingull (HARMONTIDE_CONCEPTS.md, sixteenth wave; model tools/gen_pumpkingull_harpy.py, the seagull's rig in
 * sky blue and white, dressed in a pumpkin): endermen near her friend take no offence at being looked at. Every second,
 * an enderman within 16 blocks of her that has her friend in its sights calms down and looks away.
 */
public class PumpkingullHarpy extends AbstractHarpy {
    public static final int FRIEND = Orders.AFFECTION;

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(PumpkingullHarpy.class, EntityDataSerializers.BYTE);

    /** Her colours (tools/gen_pumpkingull_harpy.py). Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        PUMPKIN("pumpkin"), GHOST("ghost"), BLACK("black"), HARVEST("harvest"), CINDER("cinder"), ORANGE("orange");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return PUMPKIN;
        }
    }

    public PumpkingullHarpy(EntityType<? extends PumpkingullHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkPumpkingullHarpySpawnRules(EntityType<PumpkingullHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
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
        this.setVariant(r < 55 ? Variant.PUMPKIN : r < 70 ? Variant.HARVEST : r < 82 ? Variant.CINDER : r < 92 ? Variant.BLACK : Variant.GHOST);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    // --- the pumpkin -----------------------------------------------------------------------------------------------

    /** Endermen within 16 blocks of her that are after her friend lose interest. Returns how many she calmed. */
    public int calmEndermen(ServerLevel level) {
        Player friend = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 24.0, p -> !p.isSpectator() && this.getAffection((Player) p) >= FRIEND);
        if (friend == null) return 0;
        int n = 0;
        for (EnderMan e : level.getEntitiesOfClass(EnderMan.class, this.getBoundingBox().inflate(16), e -> e.getTarget() == friend)) {
            e.setTarget(null);
            e.stopBeingAngry();
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SMALL_FLAME, e.getX(), e.getEyeY() + 0.3, e.getZ(), 4, 0.2, 0.2, 0.2, 0.0);
            n++;
        }
        if (n > 0) {
            this.startTimedAction(Action.SINGING, 30);
            this.playSound(SoundEvents.ENDERMAN_AMBIENT, 1.0F, 1.2F);
        }
        return n;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel level && this.tickCount % 20 == 0 && !this.isBaby()) this.calmEndermen(level);
    }

    // --- food, bond, gifts ---------------------------------------------------------------------------------------

    @Override
    public boolean isEdible(ItemStack stack) {
        return stack.has(DataComponents.FOOD) || stack.is(Items.PUMPKIN_SEEDS);
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return stack.is(Items.PUMPKIN_SEEDS) || stack.is(ItemTags.FISHES);
    }

    @Override
    public Component describeBond(Player player) {
        int a = this.getAffection(player);
        return Component.translatable("bond.monstergirls.pumpkingull." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "shy"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 40 ? new ItemStack(Items.PUMPKIN_SEEDS, 4) : r < 65 ? new ItemStack(Items.FEATHER, 3) : r < 85 ? new ItemStack(Items.PUMPKIN_PIE)
                : r < 97 ? new ItemStack(Items.CARVED_PUMPKIN) : new ItemStack(Items.ENDER_PEARL);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.GULL_EGG.get());
    }

    @Override
    protected String species() {
        return "pumpkingull_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case BATHING -> "bathe";
            case SINGING, CAWING -> "sing";
            case BEGGING -> "beg";
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
        return com.linkyoshi90.monstergirls.registry.ModSounds.SEAGULL_AMBIENT.get();
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
