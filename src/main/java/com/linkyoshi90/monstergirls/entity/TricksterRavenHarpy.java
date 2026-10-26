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
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The trickster raven (HARMONTIDE_CONCEPTS.md, tenth wave; model tools/gen_trickster_raven_harpy.py, the crow's rig):
 * a Harmontide raven and a born mimic. Now and then she plays a monster's voice beside a stranger for the fun of it;
 * once befriended she turns the trick on monsters: any that go for her friend hear her call from somewhere else and
 * wander off after it.
 */
public class TricksterRavenHarpy extends AbstractHarpy {
    public static final int FRIEND = Orders.AFFECTION;
    /** How far from her friend she sends a lured monster. */
    public static final double LURE_DISTANCE = 14.0;

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(TricksterRavenHarpy.class, EntityDataSerializers.BYTE);
    private static final List<SoundEvent> VOICES = List.of(SoundEvents.CREEPER_PRIMED, SoundEvents.ZOMBIE_AMBIENT, SoundEvents.SKELETON_AMBIENT,
            SoundEvents.SPIDER_AMBIENT, SoundEvents.ENDERMAN_STARE, SoundEvents.WITCH_CELEBRATE);

    /** Her colours (tools/gen_trickster_raven_harpy.py). Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        RAVEN("raven"), WHITE("white"), STORM("storm"), BROWN("brown"), SHEEN("sheen");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return RAVEN;
        }
    }

    private long nextPrank;
    private long nextLure;

    public TricksterRavenHarpy(EntityType<? extends TricksterRavenHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.55)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    public static boolean checkTricksterRavenHarpySpawnRules(EntityType<TricksterRavenHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
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
        this.setVariant(r < 55 ? Variant.RAVEN : r < 75 ? Variant.SHEEN : r < 88 ? Variant.STORM : r < 97 ? Variant.BROWN : Variant.WHITE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    // --- mimicry ---------------------------------------------------------------------------------------------------------

    /** A monster's voice, played where she is (a prank) or somewhere else (a lure). */
    public SoundEvent mimic(ServerLevel level, Vec3 at) {
        SoundEvent voice = VOICES.get(this.random.nextInt(VOICES.size()));
        level.playSound(null, at.x, at.y, at.z, voice, SoundSource.HOSTILE, 1.0F, 1.0F);
        return voice;
    }

    /** Every monster within 16 blocks that is after her friend is called away: it forgets them and goes looking 14 blocks off. */
    public int lure(ServerLevel level, Player friend) {
        int n = 0;
        for (Monster m : level.getEntitiesOfClass(Monster.class, friend.getBoundingBox().inflate(16), m -> m.isAlive() && m.getTarget() == friend)) {
            Vec3 away = m.position().subtract(friend.position()).multiply(1, 0, 1);
            if (away.lengthSqr() < 1.0E-4) away = new Vec3(1, 0, 0);
            Vec3 spot = friend.position().add(away.normalize().scale(LURE_DISTANCE));
            m.setTarget(null);
            m.getNavigation().moveTo(spot.x, spot.y, spot.z, 1.0);
            this.mimic(level, spot);
            n++;
        }
        if (n > 0) this.startTimedAction(Action.CAWING, 20);
        return n;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel level) || this.isBaby() || this.tickCount % 20 != 0) return;
        Player friend = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 24.0, p -> !p.isSpectator() && this.getAffection((Player) p) >= FRIEND);
        if (friend != null && level.getGameTime() >= this.nextLure && this.lure(level, friend) > 0) this.nextLure = level.getGameTime() + 100;
        if (level.getGameTime() >= this.nextPrank && this.random.nextInt(6) == 0) {
            Player stranger = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 10.0, p -> !p.isSpectator() && this.getAffection((Player) p) < FRIEND);
            if (stranger != null) {
                this.nextPrank = level.getGameTime() + 1200 + this.random.nextInt(1200);
                this.mimic(level, this.position());
                this.startTimedAction(Action.PRANKING, 60);
            }
        }
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
        return Component.translatable("bond.monstergirls.raven." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "shy"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 40 ? new ItemStack(Items.FEATHER, 2) : r < 65 ? new ItemStack(Items.BONE) : r < 85 ? new ItemStack(Items.GOLD_NUGGET, 2)
                : r < 97 ? new ItemStack(Items.FLINT, 2) : new ItemStack(Items.ECHO_SHARD);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.RAVEN_EGG.get());
    }

    @Override
    protected String species() {
        return "trickster_raven_harpy";
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

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.75F;
    }
}
