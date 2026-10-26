package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.entity.ai.AmphibiousMoveControl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Merfolk;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * A flightless seabird of snow and ice (HARMONTIDE_CONCEPTS.md, fourth wave). Penguins live in big colonies, waddle
 * on land and fly underwater. They hunt fish, and a befriended penguin shares her catch: every fish she takes, one
 * comes to her player. Wild orca mermaids hunt penguins; wild penguins go for koi mermaids. Now and then one walks
 * off alone, away from the colony and the sea, and keeps walking (Werner Herzog's penguin) until someone befriends her.
 */
public class PenguinHarpy extends AbstractHarpy {
    public static final int FRIEND = com.linkyoshi90.monstergirls.entity.Orders.AFFECTION;
    /** One penguin in this many walks off alone. */
    public static final int WANDERER_RARITY = 12;

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(PenguinHarpy.class, EntityDataSerializers.BYTE);

    /** Texture variants (tools/gen_penguin_harpy.py). Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        EMPEROR("emperor"), ADELIE("adelie"), GENTOO("gentoo"), CHINSTRAP("chinstrap"), ROCKHOPPER("rockhopper");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return EMPEROR;
        }
    }

    private record ColonyData(Variant variant) implements net.minecraft.world.entity.SpawnGroupData {}

    private boolean wanderer;
    /** The way the wanderer is walking (degrees). */
    private float heading;

    public PenguinHarpy(EntityType<? extends PenguinHarpy> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.moveControl = new AmphibiousMoveControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.18)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkPenguinHarpySpawnRules(EntityType<PenguinHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
                                                      RandomSource random) {
        return level.getBlockState(pos.below()).isSolid() && level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    protected boolean canFly() {
        return false;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new com.linkyoshi90.monstergirls.entity.ai.SwimmerNavigation(this, level);
    }

    /** She flies underwater: quick there, slow on land. */
    @Override
    public void travel(Vec3 input) {
        if (this.isControlledByLocalInstance() && this.isInWater()) {
            this.moveRelative(this.getSpeed() * 1.8F, input);
            this.move(MoverType.SELF, this.getDeltaMovement());
            Vec3 v = this.getDeltaMovement().scale(0.9);
            if (this.horizontalCollision && this.isFree(v.x, 0.6, v.z)) v = new Vec3(v.x, 0.3, v.z);   // hop out onto the ice
            this.setDeltaMovement(v);
            return;
        }
        super.travel(input);
    }

    @Override
    protected String movingAnimation() {
        return this.isInWater() ? "swim" : "walk";
    }

    // --- variant, colony, the wanderer ------------------------------------------------------------------------------

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

    /** One species to a colony; and now and then one of them is a wanderer. */
    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty,
                                                                   MobSpawnType spawnType, @Nullable net.minecraft.world.entity.SpawnGroupData group) {
        Variant variant;
        if (group instanceof ColonyData colony) {
            variant = colony.variant();
        } else {
            variant = Variant.values()[this.random.nextInt(Variant.values().length)];
            group = new ColonyData(variant);
        }
        if (!this.hasVariantFromData()) this.setVariant(variant);
        if ((spawnType == MobSpawnType.NATURAL || spawnType == MobSpawnType.CHUNK_GENERATION) && this.random.nextInt(WANDERER_RARITY) == 0) {
            this.setWanderer(true);
        }
        return super.finalizeSpawn(level, difficulty, spawnType, group);
    }

    public boolean isWanderer() {
        return this.wanderer;
    }

    public void setWanderer(boolean wanderer) {
        this.wanderer = wanderer;
        this.heading = this.random.nextFloat() * 360.0F;
    }

    public boolean isBefriended() {
        return this.bestAffection() >= FRIEND;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Variant", this.getVariant().getSerializedName());
        tag.putBoolean("Wanderer", this.wanderer);
        tag.putFloat("Heading", this.heading);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setVariant(Variant.byName(tag.getString("Variant")));
        this.wanderer = tag.getBoolean("Wanderer");
        this.heading = tag.getFloat("Heading");
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.3));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.4, true));
        this.goalSelector.addGoal(4, new WanderOffGoal());
        this.goalSelector.addGoal(5, new com.linkyoshi90.monstergirls.entity.girl.FollowEntityGoal(this, this::colonyMate, 1.0, 10.0F, 4.0F));
        this.goalSelector.addGoal(6, new RandomSwimmingGoal(this, 1.0, 20));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.LivingEntity.class, 20, true, false,
                f -> (f instanceof AbstractFish || com.linkyoshi90.monstergirls.compat.AlexsMobs.isFish(f)) && f.isInWater() && this.distanceToSqr(f) < 12 * 12));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Merfolk.class, 10, true, false,
                m -> !this.isBefriended() && ((Merfolk) m).getSpecies() == GirlSpecies.KOI_MERMAID));
    }

    /** The nearest penguin of the colony (the wanderer keeps to herself). */
    @Nullable
    private LivingEntity colonyMate() {
        if (this.wanderer && !this.hasAnyAffection()) return null;
        PenguinHarpy best = null;
        for (PenguinHarpy p : this.level().getEntitiesOfClass(PenguinHarpy.class, this.getBoundingBox().inflate(24), p -> p != this && !p.isWanderer())) {
            if (best == null || p.distanceToSqr(this) < best.distanceToSqr(this)) best = p;
        }
        return best;
    }

    /** The lone penguin: she picks a direction and walks, steadily, away from everything. Befriend her and she stops. */
    class WanderOffGoal extends Goal {
        WanderOffGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            PenguinHarpy p = PenguinHarpy.this;
            return p.wanderer && !p.hasAnyAffection() && !p.isOrderedToSit() && p.getTarget() == null && p.tickCount % 20 == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return PenguinHarpy.this.wanderer && !PenguinHarpy.this.hasAnyAffection() && PenguinHarpy.this.getTarget() == null
                    && !PenguinHarpy.this.getNavigation().isDone();
        }

        @Override
        public void start() {
            PenguinHarpy p = PenguinHarpy.this;
            Vec3 ahead = p.position().add(Vec3.directionFromRotation(0, p.heading).scale(16));
            if (!p.getNavigation().moveTo(ahead.x, ahead.y, ahead.z, 0.8)) p.heading += 30.0F;   // blocked: veer a little
        }
    }

    /** Every fish she takes, a befriended penguin brings one to her player. */
    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity victim) {
        boolean result = super.killedEntity(level, victim);
        if (victim instanceof AbstractFish || com.linkyoshi90.monstergirls.compat.AlexsMobs.isFish(victim)) {
            Player friend = this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), 32.0, p -> this.getAffection((Player) p) >= FRIEND);
            if (friend != null) {
                ItemStack catchOf = !(victim instanceof AbstractFish) ? com.linkyoshi90.monstergirls.compat.AlexsMobs.catchOf(victim)
                        : new ItemStack(victim instanceof Salmon ? Items.SALMON : victim instanceof TropicalFish ? Items.TROPICAL_FISH : Items.COD);
                ItemEntity item = new ItemEntity(level, friend.getX(), friend.getY() + 0.5, friend.getZ(), catchOf);
                item.setNoPickUpDelay();
                level.addFreshEntity(item);
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight(), this.getZ(), 3, 0.3, 0.2, 0.3, 0);
            } else {
                this.heal(2.0F);
            }
        }
        return result;
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
        return Component.translatable("bond.monstergirls.penguin." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "wary"), a);
    }

    /** Fish, or a pebble: the gift a penguin brings the one she loves. */
    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 40 ? new ItemStack(Items.COD, 2) : r < 70 ? new ItemStack(Items.SALMON) : new ItemStack(Items.FLINT);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.PENGUIN_EGG.get());
    }

    @Override
    protected String species() {
        return "penguin_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return action == Action.SINGING ? "sing" : null;
    }

    // --- sounds: a bray, like the jackass penguin's ------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.DONKEY_AMBIENT;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 1.6F;
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
        return SoundEvents.GENERIC_EAT;
    }
}
