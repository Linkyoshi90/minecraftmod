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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
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

/**
 * The coin magpie (HARMONTIDE_CONCEPTS.md, tenth wave; model tools/gen_coin_magpie_harpy.py, the crow's rig): a
 * Harmontide magpie who hoards whatever shines. Shinies she picks up go into her hoard instead of being dropped again;
 * pay her a gold nugget and she brings something back from it (or from her own finds when it is empty).
 */
public class CoinMagpieHarpy extends AbstractHarpy {
    public static final int FRIEND = Orders.AFFECTION;

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(CoinMagpieHarpy.class, EntityDataSerializers.BYTE);

    /** Her colours (tools/gen_coin_magpie_harpy.py). Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        COMMON("common"), AZURE("azure"), GREEN("green"), GOLD("gold"), WHITE("white");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return COMMON;
        }
    }

    private final SimpleContainer hoard = new SimpleContainer(9);

    public CoinMagpieHarpy(EntityType<? extends CoinMagpieHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.55)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkCoinMagpieHarpySpawnRules(EntityType<CoinMagpieHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
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
        this.setVariant(r < 50 ? Variant.COMMON : r < 72 ? Variant.AZURE : r < 90 ? Variant.GREEN : r < 97 ? Variant.GOLD : Variant.WHITE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    // --- the hoard -----------------------------------------------------------------------------------------------------

    public SimpleContainer getHoard() {
        return this.hoard;
    }

    /** A shiny she has carried long enough goes into her hoard (if there is room; otherwise she drops it). */
    @Override
    protected void onCarriedLongEnough() {
        ItemStack held = this.getMainHandItem();
        if (!held.isEmpty() && this.hoard.canAddItem(held)) {
            this.hoard.addItem(held.copy());
            this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.startTimedAction(Action.CACHING, 40);
        } else {
            super.onCarriedLongEnough();
        }
    }

    @Override
    protected int carryTicksFor(ItemStack stack) {
        return 200 + this.random.nextInt(200);
    }

    /** What a gold nugget buys: the oldest thing in her hoard, else one of her own finds. */
    public ItemStack payOut() {
        for (int i = 0; i < this.hoard.getContainerSize(); i++) if (!this.hoard.getItem(i).isEmpty()) return this.hoard.removeItemNoUpdate(i);
        int r = this.random.nextInt(100);
        return r < 35 ? new ItemStack(Items.IRON_NUGGET, 4) : r < 60 ? new ItemStack(Items.COPPER_INGOT, 2) : r < 80 ? new ItemStack(Items.LAPIS_LAZULI, 3)
                : r < 93 ? new ItemStack(Items.EMERALD) : r < 99 ? new ItemStack(Items.AMETHYST_SHARD, 2) : new ItemStack(Items.DIAMOND);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.MAIN_HAND && stack.is(Items.GOLD_NUGGET) && !this.isBaby()) {
            if (this.level() instanceof ServerLevel server) {
                stack.consume(1, player);
                ItemStack back = this.payOut();
                if (!player.getInventory().add(back)) this.spawnAtLocation(back);
                this.addAffection(player, 1);
                this.startTimedAction(Action.CAWING, 20);
                this.playSound(SoundEvents.PARROT_AMBIENT, 1.0F, 1.4F);
                server.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + this.getBbHeight(), this.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        for (ItemStack s : this.hoard.removeAllItems()) this.spawnAtLocation(s);
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
        return Component.translatable("bond.monstergirls.magpie." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "shy"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        return this.payOut();
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.MAGPIE_EGG.get());
    }

    @Override
    protected String species() {
        return "coin_magpie_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case BATHING -> "bathe";
            case CAWING, SINGING -> "caw";
            case CACHING -> "cache";
            case KNOCKING -> "knock";
            case PRANKING -> "prank";
            case SITTING -> "sit";
            case LYING, SLEEPING -> "lie";
            default -> null;
        };
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Variant", this.getVariant().getSerializedName());
        tag.put("Hoard", this.hoard.createTag(this.registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setVariant(Variant.byName(tag.getString("Variant")));
        this.hoard.fromTag(tag.getList("Hoard", 10), this.registryAccess());
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return com.linkyoshi90.monstergirls.registry.ModSounds.CROW_CAW.get();
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
