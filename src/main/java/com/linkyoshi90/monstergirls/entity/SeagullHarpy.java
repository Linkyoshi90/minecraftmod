package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.entity.ai.ListenToMusicGoal;
import com.linkyoshi90.monstergirls.entity.ai.BatheGoal;
import com.linkyoshi90.monstergirls.entity.ai.BegForFoodGoal;
import com.linkyoshi90.monstergirls.entity.ai.FollowBoatGoal;
import com.linkyoshi90.monstergirls.entity.ai.FollowFriendlyMobGoal;
import com.linkyoshi90.monstergirls.entity.ai.SeekItemGoal;
import com.linkyoshi90.monstergirls.entity.ai.SirenSongGoal;
import com.linkyoshi90.monstergirls.entity.ai.SnatchGoal;
import com.linkyoshi90.monstergirls.registry.ModSounds;
import com.linkyoshi90.monstergirls.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nullable;

/**
 * Bold, noisy coastal harpy that lives in flocks. She follows boats, walks up to people on the beach begging for
 * fish, swoops in to pinch food from their packs, and out at sea gathers on lonely rocks to sing: an alluring song
 * that draws boats in towards the rocks until they wreck. Feed her (fish most of all) and she stops robbing and
 * luring you. Comes in six real-world gull species, chosen by the coast's climate (see {@link Variant}).
 */
public class SeagullHarpy extends AbstractHarpy {
    /** Affection at which she leaves your pockets and your boat alone. */
    public static final int FRIENDLY = 5;

    private static final EntityDataAccessor<Byte> DATA_VARIANT =
            SynchedEntityData.defineId(SeagullHarpy.class, EntityDataSerializers.BYTE);

    /**
     * Species, each with its own texture from tools/gen_seagull_harpy.py (the first is the default seagull_harpy.png).
     * Saved by name, synced by ordinal: append new ones at the end.
     */
    public enum Variant {
        YELLOW_LEGGED("yellow_legged"), HERRING("herring"), BLACK_HEADED("black_headed"), LAUGHING("laughing"),
        KITTIWAKE("kittiwake"), GREAT_BLACK_BACKED("great_black_backed");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return YELLOW_LEGGED;
        }

        /** Kittiwakes and great black-backs on cold coasts, laughing gulls on warm ones, a mix of the rest in between. */
        public static Variant forBiome(Holder<Biome> biome, RandomSource random) {
            float temperature = biome.value().getBaseTemperature();
            if (temperature < 0.3F) return random.nextInt(5) < 3 ? KITTIWAKE : GREAT_BLACK_BACKED;
            if (temperature >= 0.9F) return random.nextBoolean() ? LAUGHING : YELLOW_LEGGED;
            return switch (random.nextInt(6)) {
                case 0, 1 -> HERRING;
                case 2, 3 -> BLACK_HEADED;
                case 4 -> YELLOW_LEGGED;
                default -> GREAT_BLACK_BACKED;
            };
        }
    }

    private record VariantGroupData(Variant variant) implements SpawnGroupData {}

    public SeagullHarpy(EntityType<? extends SeagullHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FLYING_SPEED, 0.45)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    public static boolean checkSeagullHarpySpawnRules(EntityType<SeagullHarpy> type, ServerLevelAccessor level,
                                                      MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(ModTags.SEAGULL_HARPY_SPAWNABLE_ON) && level.getRawBrightness(pos, 0) > 8;
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
        return "seagull_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case SINGING -> "sing";
            case BEGGING -> "beg";
            case BATHING -> "bathe";
            default -> null;
        };
    }

    /** The siren's minor scale, for joining in with a jukebox. */
    private static final int[] CHORUS = {9, 11, 12, 14, 16, 17, 19, 21};

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Cat.class, 6.0F, 1.0, 1.3));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Ocelot.class, 6.0F, 1.0, 1.3));
        // Music! The flock crowds round the jukebox and joins in, loudly, with their eerie siren harmonies.
        this.goalSelector.addGoal(3, new ListenToMusicGoal(this, 1.1, 3.0, Action.SINGING, h -> true, (h, jukebox, t) -> {
            if (t % 6 == 0 && t % 48 < 36) ListenToMusicGoal.note(h, ModSounds.SEAGULL_SING.get(), CHORUS[h.getRandom().nextInt(CHORUS.length)], 1.2F);
        }, null));
        this.goalSelector.addGoal(3, new SnatchGoal(this, 1.3, SnatchGoal.From.INVENTORY, 3000, 6000, p -> !this.isFriendly(p)));
        // Above jukebox music: a siren at her rock keeps luring sailors, whatever is playing on shore.
        this.goalSelector.addGoal(2, new SirenSongGoal(this));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.1, this::isEdible, false));
        this.goalSelector.addGoal(4, new FollowBoatGoal(this));
        this.goalSelector.addGoal(4, new com.linkyoshi90.monstergirls.entity.ai.RoostInHopperGoal(this));
        this.goalSelector.addGoal(5, new BegForFoodGoal(this));
        this.goalSelector.addGoal(5, new SeekItemGoal(this, 1.1, 10.0));
        this.goalSelector.addGoal(6, new BatheGoal(this, 1.0));
        this.goalSelector.addGoal(7, new FollowFriendlyMobGoal(this, 1.0, 3.0F, 14.0F, (self, other) -> other.getType() == self.getType()));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    public boolean isFriendly(Player player) {
        return this.getAffection(player) >= FRIENDLY;
    }

    /** Gulls eat anything; fish is what they really want. */
    @Override
    public boolean isEdible(ItemStack stack) {
        return stack.has(DataComponents.FOOD) || this.isFavouriteFood(stack);
    }

    public boolean isFavouriteFood(ItemStack stack) {
        return stack.is(ItemTags.FISHES);
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
            boolean fish = this.isFavouriteFood(stack);
            stack.consume(1, player);
            this.heal(2.0F);
            this.addAffection(player, fish ? 2 : 1);
            this.playSound(this.getEatSound(), 1.0F, this.getVoicePitch());
            this.startTimedAction(Action.BEGGING, 24); // a happy squawk-and-bounce
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public Component describeBond(Player player) {
        int affection = this.getAffection(player);
        String level = affection >= FRIENDLY ? "friendly" : affection > 0 ? "greets" : "eyes";
        return Component.translatable("bond.monstergirls.seagull." + level, affection);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.getAction() == Action.SINGING ? null : ModSounds.SEAGULL_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120; // noisy
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.SEAGULL_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.SEAGULL_DEATH.get();
    }

    @Override
    protected SoundEvent getEatSound() {
        return ModSounds.SEAGULL_EAT.get();
    }

    @Override
    public SoundEvent getChirpSound() {
        return ModSounds.SEAGULL_AMBIENT.get();
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
        return HarpyGifts.sea(this.random);
    }

    @Override
    protected net.minecraft.world.item.ItemStack unfertilisedEgg() {
        return new net.minecraft.world.item.ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.GULL_EGG.get());
    }

    /** Gulls roost in hoppers too (see {@link com.linkyoshi90.monstergirls.entity.ai.RoostInHopperGoal}). */
    @Override
    public boolean roostsInHoppers() {
        return true;
    }

    /** A moulted feather or a flint from the beach: with a sparrow's twigs, everything a fletcher needs. */
    @Override
    protected net.minecraft.world.item.ItemStack roostDrop() {
        return new net.minecraft.world.item.ItemStack(this.random.nextBoolean() ? net.minecraft.world.item.Items.FEATHER : net.minecraft.world.item.Items.FLINT);
    }
}
