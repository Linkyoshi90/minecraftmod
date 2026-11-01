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
 * The rookmourn (HARMONTIDE_CONCEPTS.md, eleventh wave; model tools/gen_rookmourn_harpy.py, the crow's rig): a
 * Harmontide rook in a plague doctor's hat and beaked mask. Every two seconds she cleanses poison, nausea, hunger and
 * wither from the players and girls around her; a golden apple sends her to cure the nearest zombie villager.
 */
public class RookmournHarpy extends AbstractHarpy {
    public static final int FRIEND = Orders.AFFECTION;

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(RookmournHarpy.class, EntityDataSerializers.BYTE);

    /** Her colours (tools/gen_rookmourn_harpy.py). Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        ROOK("rook"), HOODED("hooded"), JACKDAW("jackdaw"), MAGPIE("magpie"), CHOUGH("chough"), PIED("pied");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return ROOK;
        }
    }

    public RookmournHarpy(EntityType<? extends RookmournHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.55)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkRookmournHarpySpawnRules(EntityType<RookmournHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
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
        this.setVariant(r < 50 ? Variant.ROOK : r < 65 ? Variant.HOODED : r < 78 ? Variant.JACKDAW : r < 88 ? Variant.CHOUGH : r < 96 ? Variant.PIED : Variant.MAGPIE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    // --- the plague doctor ------------------------------------------------------------------------------------------

    private static final java.util.List<net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect>> AILMENTS = java.util.List.of(
            net.minecraft.world.effect.MobEffects.POISON, net.minecraft.world.effect.MobEffects.CONFUSION, net.minecraft.world.effect.MobEffects.HUNGER,
            net.minecraft.world.effect.MobEffects.WITHER);
    private static final java.lang.reflect.Method START_CONVERTING = net.neoforged.fml.util.ObfuscationReflectionHelper.findMethod(
            net.minecraft.world.entity.monster.ZombieVillager.class, "startConverting", java.util.UUID.class, int.class);

    /** Players and girls within 8 blocks lose their poison, nausea, hunger and wither. Returns how many she treated. */
    public int cleanse(ServerLevel level) {
        int n = 0;
        for (net.minecraft.world.entity.LivingEntity e : level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, this.getBoundingBox().inflate(8),
                e -> e.isAlive() && (e instanceof Player || e instanceof Bonded))) {
            boolean treated = false;
            for (var ailment : AILMENTS) treated |= e.removeEffect(ailment);
            if (treated) {
                n++;
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, e.getX(), e.getY() + e.getBbHeight(), e.getZ(), 5, 0.3, 0.3, 0.3, 0.0);
            }
        }
        if (n > 0) this.startTimedAction(Action.CAWING, 20);
        return n;
    }

    /** A golden apple: the nearest zombie villager within 16 blocks starts its cure (as if given the apple under weakness). */
    public boolean cure(ServerLevel level, Player player) {
        net.minecraft.world.entity.monster.ZombieVillager zv = null;
        for (var z : level.getEntitiesOfClass(net.minecraft.world.entity.monster.ZombieVillager.class, this.getBoundingBox().inflate(16), z -> !z.isConverting())) {
            if (zv == null || z.distanceToSqr(this) < zv.distanceToSqr(this)) zv = z;
        }
        if (zv == null) return false;
        try {
            START_CONVERTING.invoke(zv, player.getUUID(), 3600 + this.random.nextInt(2400));
        } catch (ReflectiveOperationException e) {
            return false;
        }
        this.getNavigation().moveTo(zv, 1.2);
        player.displayClientMessage(Component.translatable("message.monstergirls.rook_cure", this.getDisplayName()), true);
        return true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel level && this.tickCount % 40 == 0 && !this.isBaby()) this.cleanse(level);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.MAIN_HAND && stack.is(Items.GOLDEN_APPLE) && !this.isBaby()) {
            if (this.level() instanceof ServerLevel server && this.cure(server, player)) {
                stack.consume(1, player);
                this.addAffection(player, 3);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
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
        return Component.translatable("bond.monstergirls.rook." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "shy"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 35 ? new ItemStack(Items.FEATHER, 2) : r < 65 ? new ItemStack(Items.GLISTERING_MELON_SLICE) : r < 85 ? new ItemStack(Items.HONEY_BOTTLE)
                : r < 97 ? new ItemStack(Items.SPIDER_EYE, 2) : new ItemStack(Items.GOLDEN_CARROT);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.ROOK_EGG.get());
    }

    @Override
    protected String species() {
        return "rookmourn_harpy";
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
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setVariant(Variant.byName(tag.getString("Variant")));
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
