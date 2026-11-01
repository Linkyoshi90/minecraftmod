package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.registry.ModItems;
import com.linkyoshi90.monstergirls.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

import javax.annotation.Nullable;

/**
 * The mirror crane (HARMONTIDE_CONCEPTS.md, eighteenth wave; model tools/gen_mirror_crane_harpy.py, the flamingo's rig in
 * a blue and white crane's colours): her polished feathers turn arrows aside. One arrow (or any other projectile) in
 * three that would hit her friend within 16 blocks of her glances off and flies back the way it came.
 */
@EventBusSubscriber(modid = MonsterGirlsMod.MODID)
public class MirrorCraneHarpy extends AbstractHarpy {
    public static final int FRIEND = Orders.AFFECTION;
    private static final double GUARD = 16.0;

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(MirrorCraneHarpy.class, EntityDataSerializers.BYTE);

    /** Her colours (tools/gen_mirror_crane_harpy.py). Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        BLUE("blue"), WHITE("white"), GREY("grey"), VIOLET("violet"), GOLD("gold");

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

    public MirrorCraneHarpy(EntityType<? extends MirrorCraneHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.55)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkMirrorCraneHarpySpawnRules(EntityType<MirrorCraneHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
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
        this.setVariant(r < 60 ? Variant.BLUE : r < 75 ? Variant.WHITE : r < 87 ? Variant.GREY : r < 95 ? Variant.VIOLET : Variant.GOLD);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    // --- the mirror feathers -----------------------------------------------------------------------------------------

    /** A befriended mirror crane within 16 blocks of this player, if there is one. */
    @Nullable
    public static MirrorCraneHarpy guardOf(Player player) {
        for (MirrorCraneHarpy c : player.level().getEntitiesOfClass(MirrorCraneHarpy.class, player.getBoundingBox().inflate(GUARD),
                c -> !c.isBaby() && c.isAlive() && c.getAffection(player) >= FRIEND)) {
            return c;
        }
        return null;
    }

    /** Turns a projectile aside from her friend: it flies back the way it came. False when it is her friend's own. */
    public boolean deflect(Projectile shot, Player friend) {
        if (shot.getOwner() == friend) return false;
        shot.setDeltaMovement(shot.getDeltaMovement().scale(-0.4));
        shot.hurtMarked = true;
        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.END_ROD, shot.getX(), shot.getY(), shot.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
            level.playSound(null, shot.blockPosition(), SoundEvents.SHIELD_BLOCK, this.getSoundSource(), 1.0F, 1.4F);
        }
        this.getLookControl().setLookAt(friend);
        this.startTimedAction(Action.DISPLAYING, 20);
        return true;
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof Player friend) || friend.level().isClientSide) return;
        MirrorCraneHarpy crane = guardOf(friend);
        if (crane != null && crane.random.nextInt(3) == 0 && crane.deflect(event.getProjectile(), friend)) event.setCanceled(true);
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
        return Component.translatable("bond.monstergirls.mirror_crane." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "shy"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 45 ? new ItemStack(Items.FEATHER, 3) : r < 75 ? new ItemStack(Items.COD, 2) : r < 95 ? new ItemStack(Items.ARROW, 4)
                : new ItemStack(Items.GOLD_NUGGET, 4);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.FLAMINGO_EGG.get());
    }

    @Override
    protected String species() {
        return "mirror_crane_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case SINGING, CAWING -> "sing";
            case BATHING -> "bathe";
            case PERCHING -> "stand1";
            case DISPLAYING -> "march";
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
