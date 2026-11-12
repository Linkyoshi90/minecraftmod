package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.registry.ModItems;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Zhuque, the Vermilion Bird (HARMONTIDE_CONCEPTS.md, sixth wave): the south of the Chinese Four Symbols, the bird of
 * fire and summer. She wears the phoenix's shape in vermilion and gold, never burns, sets whatever she strikes alight,
 * and once befriended brings summer to the crops around her: they ripen as she passes.
 */
public class ZhuqueHarpy extends AbstractHarpy {
    public static final int FRIEND = Orders.AFFECTION;
    public static final double RIPEN_RANGE = 6.0;
    private static final Set<Item> FOOD = Set.of(Items.SWEET_BERRIES, Items.GLOW_BERRIES, Items.MELON_SLICE, Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS,
            Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.TORCHFLOWER_SEEDS);

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(ZhuqueHarpy.class, EntityDataSerializers.BYTE);

    /** Texture variants (tools/gen_zhuque_harpy.py). Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        VERMILION("vermilion"), DUSK("dusk"), GOLDEN("golden"), ROSE("rose"), JADE("jade");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return VERMILION;
        }
    }

    public ZhuqueHarpy(EntityType<? extends ZhuqueHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.55)
                .add(Attributes.ATTACK_DAMAGE, 5.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkZhuqueHarpySpawnRules(EntityType<ZhuqueHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
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
        this.setVariant(this.random.nextInt(3) == 0 ? Variant.values()[1 + this.random.nextInt(Variant.values().length - 1)] : Variant.VERMILION);
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
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    // --- summer and fire -------------------------------------------------------------------------------------------

    /** Whatever she strikes catches fire. */
    @Override
    public boolean doHurtTarget(Entity target) {
        this.startTimedAction(Action.STRIKING, 12);
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity l) l.igniteForSeconds(5);
        return hit;
    }

    /** Befriended, she ripens the crops she passes over. Returns how many grew. */
    public int ripenCrops(ServerLevel level) {
        int grown = 0;
        BlockPos o = this.blockPosition();
        for (BlockPos p : BlockPos.betweenClosed(o.offset(-6, -4, -6), o.offset(6, 1, 6))) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof CropBlock crop && !crop.isMaxAge(s) && this.random.nextInt(4) == 0) {
                level.setBlock(p, crop.getStateForAge(crop.getAge(s) + 1), 2);
                level.levelEvent(2005, p, 0);
                grown++;
            }
        }
        return grown;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel server && !this.isBaby() && this.tickCount % 60 == 0 && this.bestAffection() >= FRIEND) this.ripenCrops(server);
    }

    // --- food, bond, gifts ---------------------------------------------------------------------------------------

    @Override
    public boolean isEdible(ItemStack stack) {
        return FOOD.contains(stack.getItem());
    }

    /** The south of the Four Symbols. */
    @Override
    public void addAffection(Player player, int amount) {
        super.addAffection(player, amount);
        FourSymbols.onAffection(this, player, this.getAffection(player));
    }

    @Override
    public void setAffection(Player player, int amount) {
        super.setAffection(player, amount);
        FourSymbols.onAffection(this, player, amount);
    }

    @Override
    public void setAffection(java.util.UUID player, int amount) {
        super.setAffection(player, amount);
        FourSymbols.onAffection(this, this.level().getPlayerByUUID(player), amount);
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return false;
    }

    @Override
    public Component describeBond(Player player) {
        int a = this.getAffection(player);
        return Component.translatable("bond.harmontide.zhuque." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "shy"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 35 ? new ItemStack(Items.WHEAT, 4) : r < 65 ? new ItemStack(Items.TORCHFLOWER) : r < 90 ? new ItemStack(Items.BLAZE_POWDER, 2)
                : new ItemStack(Items.GOLD_INGOT);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.ZHUQUE_EGG.get());
    }

    @Override
    protected String species() {
        return "zhuque_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case STRIKING -> "strike";
            case CRYING -> "cry";
            default -> null;
        };
    }

    @Override
    protected boolean actionShowsInFlight(Action action) {
        return action == Action.STRIKING;
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
