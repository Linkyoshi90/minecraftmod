package com.linkyoshi90.monstergirls.entity;

import net.minecraft.sounds.SoundEvents;
import com.linkyoshi90.monstergirls.entity.ai.ListenToMusicGoal;
import com.linkyoshi90.monstergirls.entity.ai.AlbatrossFlightGoal;
import com.linkyoshi90.monstergirls.entity.ai.SeekItemGoal;
import com.linkyoshi90.monstergirls.entity.ai.SkittishGoal;
import com.linkyoshi90.monstergirls.entity.ai.SnatchGoal;
import com.linkyoshi90.monstergirls.registry.ModSounds;
import com.linkyoshi90.monstergirls.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Wanderer of coasts and open sky. She spends most of her time soaring in wide circles and only comes down where
 * there is room to land: a flat, open clearing. Skittish: walk up to her and she takes off, and a wild albatross
 * that knows nobody simply vanishes. She does swoop down to pinch food from people's packs, though. To earn her
 * trust, keep your distance and leave food out in a clearing; each treat she takes from you counts.
 */
public class AlbatrossHarpy extends AbstractHarpy {
    /** Affection at which she lets you come close (and stops robbing you). */
    public static final int TRUSTING = 3;

    private static final EntityDataAccessor<Byte> DATA_VARIANT =
            SynchedEntityData.defineId(AlbatrossHarpy.class, EntityDataSerializers.BYTE);

    /**
     * Species, each with its own texture from tools/gen_albatross_harpy.py (the first is the default albatross_harpy.png).
     * Saved by name, synced by ordinal: append new ones at the end.
     */
    public enum Variant {
        BLACK_BROWED("black_browed"), WANDERING("wandering"), LAYSAN("laysan"), SOOTY("sooty"),
        SHORT_TAILED("short_tailed"), GREY_HEADED("grey_headed");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return BLACK_BROWED;
        }

        /** Grey-headed on snowy shores, laysan on hot ones, sooty on rocky coasts; elsewhere a mix, led by black-browed. */
        public static Variant forBiome(Holder<Biome> biome, RandomSource random) {
            if (biome.is(Tags.Biomes.IS_SNOWY)) return GREY_HEADED;
            if (biome.is(Tags.Biomes.IS_HOT_OVERWORLD)) return LAYSAN;
            if (biome.is(Biomes.STONY_SHORE)) return SOOTY;
            if (biome.is(Biomes.WINDSWEPT_HILLS)) return random.nextBoolean() ? WANDERING : BLACK_BROWED;
            int roll = random.nextInt(10);
            if (roll < 4) return BLACK_BROWED;
            if (roll < 6) return WANDERING;
            if (roll == 6) return LAYSAN;
            if (roll == 7) return SHORT_TAILED;
            return roll == 8 ? GREY_HEADED : SOOTY;
        }
    }

    private record VariantGroupData(Variant variant) implements SpawnGroupData {}

    public AlbatrossHarpy(EntityType<? extends AlbatrossHarpy> type, Level level) {
        super(type, level);
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
        // One species per group: the first spawn picks for the biome, the rest follow.
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

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    public static boolean checkAlbatrossHarpySpawnRules(EntityType<AlbatrossHarpy> type, ServerLevelAccessor level,
                                                        MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(ModTags.ALBATROSS_HARPY_SPAWNABLE_ON) && level.getRawBrightness(pos, 0) > 8;
    }

    /** Room to land: a flat 5x5 patch of solid ground with three blocks of open air above it, and no water. */
    public static boolean isClearing(LevelReader level, BlockPos standing) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos p = standing.offset(dx, 0, dz);
                if (!level.getBlockState(p.below()).isFaceSturdy(level, p.below(), Direction.UP) || !level.getFluidState(p.below()).isEmpty()) return false;
                for (int dy = 0; dy < 3; dy++) {
                    if (!level.getBlockState(p.above(dy)).getCollisionShape(level, p.above(dy)).isEmpty()) return false;
                }
            }
        }
        return true;
    }

    @Override
    protected String species() {
        return "albatross_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case BATHING -> "bathe";
            case DANCING -> "dance";
            default -> null;
        };
    }

    /** Someone close enough that she won't dance (she's far too shy for an audience). */
    public boolean isWatched() {
        return this.level().getNearestPlayer(this.getX(), this.getY(), this.getZ(), 12.0, p -> !p.isSpectator()) != null;
    }

    /** Glides on stiff wings; only flaps while climbing. */
    @Override
    protected String flyingAnimation() {
        return this.getDeltaMovement().y > 0.08 ? "fly" : "soar";
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(2, new SkittishGoal(this));
        // Music and nobody about: she lands near the jukebox and does her courtship dance (sky-pointing, a bow,
        // head-fencing with bill-clap claps, a hop). Anyone coming close and she's gone (SkittishGoal).
        this.goalSelector.addGoal(3, new ListenToMusicGoal(this, 1.0, 5.0, Action.DANCING, h -> !this.isWatched(), (h, jukebox, t) -> {
            int beat = t % 64;                                           // one loop of animation.albatross_harpy.dance
            if (beat >= 34 && beat <= 44 && beat % 2 == 0) h.playSound(SoundEvents.WOODEN_BUTTON_CLICK_ON, 0.7F, 1.6F);
            if (beat == 8) h.playSound(ModSounds.ALBATROSS_AMBIENT.get(), 1.0F, 1.1F);
        }, null));
        this.goalSelector.addGoal(3, new SnatchGoal(this, 1.5, SnatchGoal.From.INVENTORY, 3600, 9000, p -> !this.isTrusting(p)));
        this.goalSelector.addGoal(4, new SeekItemGoal(this, 1.2, 16.0));
        this.goalSelector.addGoal(6, new AlbatrossFlightGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    public boolean isTrusting(Player player) {
        return this.getAffection(player) >= TRUSTING;
    }

    /** She only goes down for something where she could land. */
    @Override
    public boolean isSafeSpot(BlockPos pos) {
        return isClearing(this.level(), pos);
    }

    @Override
    public boolean isEdible(ItemStack stack) {
        return stack.has(DataComponents.FOOD) || stack.is(ItemTags.FISHES);
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return false;
    }

    /** Food left out by someone (thrown or dropped by a player) wins a little of her trust. */
    @Override
    protected void onPickedUp(ItemEntity item, ItemStack stack) {
        if (this.isEdible(stack) && item.getOwner() instanceof Player player) this.addAffection(player, stack.is(ItemTags.FISHES) ? 2 : 1);
    }

    @Override
    public Component describeBond(Player player) {
        int affection = this.getAffection(player);
        String level = affection >= TRUSTING ? "trusts" : affection > 0 ? "tolerates" : "distant";
        return Component.translatable("bond.monstergirls.albatross." + level, affection);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 400;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ALBATROSS_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.ALBATROSS_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.ALBATROSS_DEATH.get();
    }

    @Override
    protected SoundEvent getEatSound() {
        return ModSounds.ALBATROSS_EAT.get();
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
        return new net.minecraft.world.item.ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.ALBATROSS_EGG.get());
    }
}
