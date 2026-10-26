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
 * The peaflare (HARMONTIDE_CONCEPTS.md, thirteenth wave; model tools/gen_peaflare_harpy.py, the crow's rig with a peacock's
 * crest and train): when monsters come within 8 blocks of her friend she raises her train into its great fan
 * ("display") and dazzles them, slow and weak, at most every ten seconds.
 */
public class PeaflareHarpy extends AbstractHarpy {
    public static final int FRIEND = Orders.AFFECTION;

    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(PeaflareHarpy.class, EntityDataSerializers.BYTE);

    /** Her colours (tools/gen_peaflare_harpy.py). Saved by name, synced by ordinal: append new ones at the end. */
    public enum Variant {
        BLUE("blue"), WHITE("white"), GREEN("green"), GOLD("gold"), VIOLET("violet");

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

    public PeaflareHarpy(EntityType<? extends PeaflareHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.55)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkPeaflareHarpySpawnRules(EntityType<PeaflareHarpy> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos,
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
        this.setVariant(r < 60 ? Variant.BLUE : r < 75 ? Variant.GREEN : r < 85 ? Variant.WHITE : r < 95 ? Variant.GOLD : Variant.VIOLET);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    // --- the display -------------------------------------------------------------------------------------------------

    private long nextDisplay;

    /** Monsters within 8 blocks of her friend: up goes her fan, and they are dazzled. Returns how many she dazzled. */
    public int dazzle(ServerLevel level) {
        if (level.getGameTime() < this.nextDisplay) return 0;
        Player friend = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 24.0, p -> !p.isSpectator() && this.getAffection((Player) p) >= FRIEND);
        if (friend == null) return 0;
        java.util.List<net.minecraft.world.entity.monster.Monster> near = level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class,
                friend.getBoundingBox().inflate(8));
        if (near.isEmpty()) return 0;
        for (var m : near) {
            m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            m.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 100, 0));
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, m.getX(), m.getEyeY(), m.getZ(), 6, 0.3, 0.3, 0.3, 0.02);
        }
        this.nextDisplay = level.getGameTime() + 200;
        this.getLookControl().setLookAt(near.get(0));
        this.startTimedAction(Action.DISPLAYING, 40);
        this.playSound(SoundEvents.PARROT_AMBIENT, 1.2F, 0.7F);
        return near.size();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel level && this.tickCount % 10 == 0 && !this.isBaby()) this.dazzle(level);
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
        return Component.translatable("bond.monstergirls.peafowl." + (a >= FRIEND ? "friend" : a > 0 ? "curious" : "shy"), a);
    }

    @Override
    public ItemStack rollGift(Player player) {
        int r = this.random.nextInt(100);
        return r < 45 ? new ItemStack(Items.FEATHER, 3) : r < 75 ? new ItemStack(Items.WHEAT_SEEDS, 4) : r < 95 ? new ItemStack(Items.LAPIS_LAZULI, 2)
                : new ItemStack(Items.EMERALD);
    }

    @Override
    protected ItemStack unfertilisedEgg() {
        return new ItemStack(ModItems.PEAFOWL_EGG.get());
    }

    @Override
    protected String species() {
        return "peaflare_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case BATHING -> "bathe";
            case DISPLAYING -> "display";
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
