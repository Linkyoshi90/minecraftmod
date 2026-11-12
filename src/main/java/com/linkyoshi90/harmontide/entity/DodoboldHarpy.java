package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.SimpleMenuProvider;
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
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * The dodobold (HARMONTIDE_CONCEPTS.md, twelfth wave; model tools/gen_dodobold_harpy.py, the penguin's rig made small
 * and fat): a flightless Harmontide dodo in an ornate harness with two saddlebags. She waddles and never flies. A
 * friend's crouching empty hand opens her saddlebags (18 slots), which she carries wherever she goes and drops if she
 * dies. She lays big eggs.
 */
public class DodoboldHarpy extends AbstractHarpy {
    public static final int FRIEND = Orders.AFFECTION;
    public static final int PACK_SLOTS = 18;

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(DodoboldHarpy.class, EntityDataSerializers.BYTE);

    /** Her colours (tools/gen_dodobold_harpy.py). Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        BLUE("blue"), GREY("grey"), BROWN("brown"), WHITE("white"), TEAL("teal");

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

    /** Her saddlebags: still in reach only while she is alive and close. */
    private final SimpleContainer pack = new SimpleContainer(PACK_SLOTS) {
        @Override
        public boolean stillValid(Player player) {
            return DodoboldHarpy.this.isAlive() && player.distanceToSqr(DodoboldHarpy.this) < 8 * 8;
        }
    };

    public DodoboldHarpy(EntityType<? extends DodoboldHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 18.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.FLYING_SPEED, 0.2)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkDodoboldHarpySpawnRules(EntityType<DodoboldHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
                                                       RandomSource random) {
        return level.getBlockState(pos.below()).isSolid() && level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    protected boolean canFly() {
        return false;
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
        this.setVariant(r < 40 ? Variant.BLUE : r < 70 ? Variant.GREY : r < 85 ? Variant.BROWN : r < 95 ? Variant.TEAL : Variant.WHITE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.3));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    // --- the saddlebags -----------------------------------------------------------------------------------------------

    public SimpleContainer getPack() {
        return this.pack;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && player.isShiftKeyDown() && player.getMainHandItem().isEmpty() && !this.isBaby()
                && this.getAffection(player) >= FRIEND) {
            if (!this.level().isClientSide) {
                player.openMenu(new SimpleMenuProvider((id, inv, p) -> new ChestMenu(MenuType.GENERIC_9x2, id, inv, this.pack, 2), this.getDisplayName()));
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        for (ItemStack s : this.pack.removeAllItems()) this.spawnAtLocation(s);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return this.pack.isEmpty() && super.removeWhenFarAway(distance);
    }

    // --- food, bond, gifts ---------------------------------------------------------------------------------------

    @Override
    public boolean isEdible(ItemStack stack) {
        return stack.has(DataComponents.FOOD) || stack.is(Items.MELON_SEEDS) || stack.is(Items.WHEAT_SEEDS) || stack.is(Items.PUMPKIN_SEEDS);
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return stack.is(Items.APPLE) || stack.is(Items.SWEET_BERRIES) || stack.is(Items.MELON_SLICE);
    }

    @Override
    public Component describeBond(Player player) {
        int a = this.getAffection(player);
        return Component.translatable("bond.harmontide.dodo." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "shy"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 40 ? new ItemStack(Items.FEATHER, 2) : r < 70 ? new ItemStack(Items.MELON_SEEDS, 3) : r < 90 ? new ItemStack(Items.APPLE, 2)
                : new ItemStack(Items.GOLD_NUGGET, 3);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.DODO_EGG.get());
    }

    @Override
    protected String species() {
        return "dodobold_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case BATHING -> "bathe";
            case CAWING, SINGING -> "sing";
            default -> null;
        };
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Variant", this.getVariant().getSerializedName());
        tag.put("Pack", this.pack.createTag(this.registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setVariant(Variant.byName(tag.getString("Variant")));
        this.pack.fromTag(tag.getList("Pack", Tag.TAG_COMPOUND), this.registryAccess());
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
        return SoundEvents.PARROT_EAT;
    }
}
