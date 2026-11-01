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
 * The ibisking (HARMONTIDE_CONCEPTS.md, twenty-eighth wave; model tools/gen_ibisking_harpy.py, the flamingo's rig): the great ibis of the temple scribes.
 * Befriended, she reads a sheet of paper for you and tells you where the nearest buried treasure lies.
 */
public class IbiskingHarpy extends AbstractHarpy {
    public static final int FRIEND = Orders.AFFECTION;
    private static final double GUARD = 16.0;

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(IbiskingHarpy.class, EntityDataSerializers.BYTE);

    /** Her colours (tools/gen_ibisking_harpy.py). Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        BLUE("blue"), IVORY("ivory"), INK("ink"), BRONZE("bronze"), JADE("jade");

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

    public IbiskingHarpy(EntityType<? extends IbiskingHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 18.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.FLYING_SPEED, 0.42)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkIbiskingHarpySpawnRules(EntityType<IbiskingHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
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

    // --- the records ---------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
    }

    /** Her friend: a player within 16 blocks of her she is friends with, or null. */
    @Nullable
    public Player friend() {
        for (Player p : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(GUARD), p -> p.isAlive() && !p.isSpectator())) {
            if (this.getAffection(p) >= FRIEND) return p;
        }
        return null;
    }

    /** She looks it up in her records: how far the nearest treasure is, or -1 when there is none she knows of. */
    public int consult(ServerLevel level, Player player) {
        BlockPos at = level.findNearestMapStructure(net.minecraft.tags.StructureTags.ON_TREASURE_MAPS, this.blockPosition(), 50, false);
        if (at == null) {
            player.displayClientMessage(Component.translatable("message.monstergirls.ibisking.none", this.getDisplayName()), true);
            return -1;
        }
        int dx = at.getX() - this.getBlockX(), dz = at.getZ() - this.getBlockZ();
        int distance = (int) Math.sqrt(dx * dx + dz * dz);
        String way = net.minecraft.core.Direction.getNearest((float) dx, 0.0F, (float) dz).getName();
        player.displayClientMessage(Component.translatable("message.monstergirls.ibisking.found", this.getDisplayName(), distance, way), true);
        return distance;
    }

    @Override
    protected net.minecraft.world.InteractionResult mobInteract(Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == net.minecraft.world.InteractionHand.MAIN_HAND && stack.is(Items.PAPER) && this.getAffection(player) >= FRIEND) {
            boolean client = this.level().isClientSide;
            if (this.level() instanceof ServerLevel level) {
                stack.shrink(1);
                this.consult(level, player);
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(client);
        }
        return super.mobInteract(player, hand);
    }

    // --- food, bond, gifts ---------------------------------------------------------------------------------------

    @Override
    public boolean isEdible(ItemStack stack) {
        return stack.has(net.minecraft.core.component.DataComponents.FOOD) || stack.is(ModTags.CROW_HARPY_FOOD);
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return stack.is(Items.PAPER) || stack.is(Items.INK_SAC);
    }

    @Override
    public Component describeBond(Player player) {
        int a = this.getAffection(player);
        return Component.translatable("bond.monstergirls.ibisking." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "shy"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 50 ? new ItemStack(Items.PAPER, 4) : r < 85 ? new ItemStack(Items.FEATHER, 3) : r < 97 ? new ItemStack(Items.INK_SAC, 3) : new ItemStack(Items.BOOK, 2);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.CROW_EGG.get());
    }

    @Override
    protected String species() {
        return "ibisking_harpy";
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
