package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.entity.ai.ListenToMusicGoal;
import com.linkyoshi90.monstergirls.entity.ai.OwlPounceGoal;
import com.linkyoshi90.monstergirls.entity.ai.RoostGoal;
import com.linkyoshi90.monstergirls.entity.ai.StalkPlayerGoal;
import com.linkyoshi90.monstergirls.registry.ModSounds;
import com.linkyoshi90.monstergirls.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nullable;

/**
 * A patient night hunter of the forests. By day she roosts high up and sleeps (a player walking right up to her
 * wakes her, and she hoots at them). At night she hunts pigs, sheep, cows, chickens, rabbits and frogs, and now
 * and then quietly stalks a player, perching at a distance to stare and hoot. She is mostly harmless to people.
 * Meat, raw or cooked, lures her out even by day; feed her enough and she leaves your livestock alone.
 * Comes in five real-world species, chosen by biome (see {@link Variant}).
 */
public class OwlHarpy extends AbstractHarpy {
    /** Affection at which she spares a player's livestock (anything within 32 blocks of them). */
    public static final int FRIENDLY = 5;

    private static final EntityDataAccessor<Byte> DATA_VARIANT =
            SynchedEntityData.defineId(OwlHarpy.class, EntityDataSerializers.BYTE);

    /**
     * Species, each with its own texture from tools/gen_owl_harpy.py (the first is the default owl_harpy.png).
     * Saved by name, synced by ordinal: append new ones at the end.
     */
    public enum Variant {
        GREAT_HORNED("great_horned"), BARN("barn"), SNOWY("snowy"), TAWNY("tawny"), BURROWING("burrowing");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return GREAT_HORNED;
        }

        /** Snowy owls in the cold, burrowing owls in dry open country, barn owls on farmland plains; tawny or great horned in the woods. */
        public static Variant forBiome(Holder<Biome> biome, RandomSource random) {
            if (biome.is(Tags.Biomes.IS_SNOWY) || biome.is(Tags.Biomes.IS_COLD_OVERWORLD) && !biome.is(Tags.Biomes.IS_FOREST)) return SNOWY;
            if (biome.is(Tags.Biomes.IS_SAVANNA) || biome.is(Tags.Biomes.IS_DESERT) || biome.is(Tags.Biomes.IS_BADLANDS)) return BURROWING;
            if (biome.is(Tags.Biomes.IS_PLAINS)) return BARN;
            if (biome.is(Tags.Biomes.IS_TAIGA)) return GREAT_HORNED;
            return random.nextInt(5) < 2 ? TAWNY : GREAT_HORNED;
        }
    }

    private record VariantGroupData(Variant variant) implements SpawnGroupData {}

    /** Ticks until the second, lower "hoo" of a hoot. */
    private int hootEcho;

    public OwlHarpy(EntityType<? extends OwlHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FLYING_SPEED, 0.5)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    public static boolean checkOwlHarpySpawnRules(EntityType<OwlHarpy> type, ServerLevelAccessor level,
                                                  MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(ModTags.OWL_HARPY_SPAWNABLE_ON);
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

    /** Texture suffix for the renderer: "" for the default species. */
    public String getVariantSuffix() {
        return this.getVariant().ordinal() == 0 ? "" : "_" + this.getVariant().getSerializedName();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData groupData) {
        // One species per flock / area: the first spawn picks for the biome, the rest of the group follows.
        Variant variant;
        if (groupData instanceof VariantGroupData group) {
            variant = group.variant();
        } else {
            variant = Variant.forBiome(level.getBiome(this.blockPosition()), this.random);
            groupData = new VariantGroupData(variant);
        }
        if (!this.hasVariantFromData()) this.setVariant(variant);
        return super.finalizeSpawn(level, difficulty, spawnType, groupData);
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
    protected String species() {
        return "owl_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case HOOTING -> "hoot";
            case SLEEPING -> "sleep";
            case POUNCING -> "pounce";
            default -> null;
        };
    }

    @Override
    protected boolean actionShowsInFlight(Action action) {
        return action == Action.POUNCING; // talons-first dive
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new OwlPounceGoal(this, 1.4));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.1, this::isEdible, false));
        this.goalSelector.addGoal(3, new RoostGoal(this));
        // At night a jukebox draws her in: she perches close by, listens without a sound, and hoots once it's over.
        this.goalSelector.addGoal(4, new ListenToMusicGoal(this, 1.0, 4.0, Action.NONE, h -> !h.level().isDay() && h.getTarget() == null,
                (h, jukebox, t) -> {}, (h, jukebox, t) -> this.hoot()));
        this.goalSelector.addGoal(4, new StalkPlayerGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomFlyingGoal(this, 0.9));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Animal.class, 20, true, false, this::isPrey));
    }

    /** Night-time quarry: livestock and small animals, but never named pets or animals her friends look after. */
    public boolean isPrey(LivingEntity entity) {
        if (this.level().isDay() || !entity.getType().is(ModTags.OWL_HARPY_PREY) || entity.hasCustomName()) return false;
        for (Player player : this.level().players()) {
            if (this.getAffection(player) >= FRIENDLY && player.distanceToSqr(entity) < 32 * 32) return false;
        }
        return true;
    }

    public boolean isSleeping() {
        return this.getAction() == Action.SLEEPING;
    }

    /** A double "hoo-hoo" with the hoot animation. */
    /** Woken at her roost by music: an irritable hoot, and she remembers it (for tests and the roost goal). */
    public void wokenByMusic() {
        this.wokenByMusicCount++;
        this.setAction(Action.NONE);
        this.hoot();
    }

    public int getWokenByMusicCount() {
        return this.wokenByMusicCount;
    }

    public void hoot() {
        this.playSound(ModSounds.OWL_HOOT.get(), 1.4F, ModSounds.wobble(this));
        this.hootEcho = 12;
        this.startTimedAction(Action.HOOTING, 60);
    }

    private int wokenByMusicCount;

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.hootEcho > 0 && --this.hootEcho == 0) {
            this.playSound(ModSounds.OWL_HOOT.get(), 1.4F, 0.9F);
        }
    }

    @Override
    public boolean isEdible(ItemStack stack) {
        return stack.is(ModTags.OWL_HARPY_FOOD);
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return false;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!this.isEdible(stack)) return super.mobInteract(player, hand);
        if (this.level() instanceof ServerLevel) {
            stack.consume(1, player);
            this.heal(3.0F);
            this.addAffection(player, 1);
            this.setAction(Action.NONE);
            this.playSound(this.getEatSound(), 1.0F, this.getVoicePitch());
            if (this.getAffection(player) == FRIENDLY) {
                player.displayClientMessage(Component.translatable("entity.monstergirls.owl_harpy.friendly", this.getDisplayName()), true);
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    protected void onPetted(Player player) {
        if (this.isSleeping()) this.setAction(Action.NONE);
        this.hoot();
    }

    @Override
    public Component describeBond(Player player) {
        int affection = this.getAffection(player);
        String level = affection >= FRIENDLY ? "friendly" : affection > 0 ? "curious" : "watching";
        return Component.translatable("bond.monstergirls.owl." + level, affection);
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity entity) {
        this.heal(4.0F); // a meal
        return super.killedEntity(level, entity);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isSleeping()) this.setAction(Action.NONE); // rudely woken
        return super.hurt(source, amount);
    }

    // --- sounds: owls fly silently and mostly keep quiet -------------------------------------------------------

    @Override
    public int getAmbientSoundInterval() {
        return 600;
    }

    @Override
    public void playAmbientSound() {
        if (!this.isSleeping() && !this.level().isDay()) this.hoot();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.OWL_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.OWL_DEATH.get();
    }

    @Override
    protected SoundEvent getEatSound() {
        return ModSounds.OWL_EAT.get();
    }

    @Override
    public SoundEvent getChirpSound() {
        return ModSounds.OWL_HOOT.get();
    }

    @Override
    protected boolean isFlapping() {
        return false; // silent flight
    }

    /** A hatchling from a clutch: any of her species' colours. */
    @Override
    public void plainVariant() {
        this.setVariant(Variant.values()[0]);
    }

    @Override
    public void randomizeVariant() {
        Variant[] all = Variant.values();
        this.setVariant(all[this.random.nextInt(all.length)]);
    }

    /** {@inheritDoc} */
    @Override
    public ItemStack rollGift(Player player) {
        return HarpyGifts.owl(this.random);
    }

    @Override
    protected net.minecraft.world.item.ItemStack unfertilisedEgg() {
        return new net.minecraft.world.item.ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.OWL_EGG.get());
    }
}
