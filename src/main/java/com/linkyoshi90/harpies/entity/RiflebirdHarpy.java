package com.linkyoshi90.harpies.entity;

import com.linkyoshi90.harpies.entity.ai.ListenToMusicGoal;
import com.linkyoshi90.harpies.entity.ai.DanceGoal;
import com.linkyoshi90.harpies.block.RiflebirdNestBlock;
import com.linkyoshi90.harpies.entity.ai.FollowFriendlyMobGoal;
import com.linkyoshi90.harpies.entity.ai.LayInNestGoal;
import com.linkyoshi90.harpies.entity.ai.ReturnHomeGoal;
import com.linkyoshi90.harpies.entity.ai.SeekItemGoal;
import com.linkyoshi90.harpies.registry.ModItems;
import com.linkyoshi90.harpies.registry.ModSounds;
import com.linkyoshi90.harpies.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.biome.Biome;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Showy little rainforest harpy. She dances (the riflebird's round wing-fan display) to her own pop grooves,
 * for other riflebirds and, more and more often, for players she likes. Gifts win her over: a diamond or emerald
 * counts for a lot, any food a little. Once she's fond of you she sometimes lays an unfertilised egg for you,
 * and doesn't mind at all when you take it. Set out a riflebird nest and she'll adopt it as her home: she lays
 * her eggs in it (for you, and now and then all on her own) instead of on the bare ground.
 */
public class RiflebirdHarpy extends AbstractHarpy {
    /** Affection at which she starts laying eggs for you. */
    public static final int FOND = 3;
    private static final int EGG_COOLDOWN = 24000;
    /** How far she looks (sideways; up to 8 below her, as she is often flying) for a nest to adopt or lay in. */
    private static final int NEST_RANGE = 12;
    /** How long she spends getting to a nest before she lays the egg wherever she is. */
    private static final int NEST_DEADLINE = 600;
    /** A nesting bird lays on her own about once in this many checks (every 200 ticks) once her cooldown is over. */
    private static final int OWN_EGG_CHANCE = 300;

    private long nextEggTime;
    /** Dancing for an audience to a jukebox wins her over a little, at most this often. */
    private static final int MUSIC_AFFECTION_COOLDOWN = 6000;
    /** How long into a dance her audience has to stay before it counts. */
    public static final int MUSIC_AFFECTION_AFTER = 200;
    private long nextMusicAffectionTime;
    private int eggDelay;
    private boolean eggDue;
    private long eggDeadline;
    /** The nest she's taking her egg to (found once, when the egg came due). */
    @Nullable
    private BlockPos eggNest;

    private static final EntityDataAccessor<Byte> DATA_VARIANT =
            SynchedEntityData.defineId(RiflebirdHarpy.class, EntityDataSerializers.BYTE);

    /**
     * Birds-of-paradise colourings, each with its own texture from tools/gen_riflebird_harpy.py (the first is the
     * default riflebird_harpy.png, the magnificent riflebird). Saved by name, synced by ordinal: append new ones at the end.
     */
    public enum Variant {
        MAGNIFICENT("magnificent"), VICTORIA("victoria"), PARADISE("paradise"), HEN("hen"), KING("king"), WILSONS("wilsons");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return MAGNIFICENT;
        }

        /** A jungle flock: mostly the three black riflebirds, a few brown hens, and the odd flashy king or Wilson's. */
        public static Variant forBiome(Holder<Biome> biome, RandomSource random) {
            int roll = random.nextInt(24);
            if (roll < 8) return MAGNIFICENT;
            if (roll < 13) return VICTORIA;
            if (roll < 17) return PARADISE;
            if (roll < 21) return HEN;
            return roll < 23 ? KING : WILSONS;
        }
    }

    private record VariantGroupData(Variant variant) implements SpawnGroupData {}

    public RiflebirdHarpy(EntityType<? extends RiflebirdHarpy> type, Level level) {
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
        // One look per flock: the first spawn rolls, the rest of the group follows.
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

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FLYING_SPEED, 0.42)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkRiflebirdHarpySpawnRules(EntityType<RiflebirdHarpy> type, ServerLevelAccessor level,
                                                        MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(ModTags.RIFLEBIRD_HARPY_SPAWNABLE_ON);
    }

    @Override
    protected String species() {
        return "riflebird_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case DANCING -> "dance";
            case SINGING -> "sing";
            default -> null;
        };
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.3));
        this.goalSelector.addGoal(2, new LayInNestGoal(this, 1.0));
        // A real song to dance to: she dances to the jukebox instead of humming her own groove.
        this.goalSelector.addGoal(3, new ListenToMusicGoal(this, 1.0, 2.0, Action.DANCING, h -> true,
                (h, jukebox, t) -> this.danceToMusic(t), null));
        this.goalSelector.addGoal(3, new DanceGoal(this));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, s -> this.isGift(s), false));
        this.goalSelector.addGoal(5, new SeekItemGoal(this, 1.0, 8.0));
        this.goalSelector.addGoal(6, new ReturnHomeGoal(this, 1.0, 16.0));
        this.goalSelector.addGoal(6, new FollowFriendlyMobGoal(this, 1.0, 3.0F, 12.0F, (self, other) -> other.getType() == self.getType()));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    /** Diamonds, emeralds, or any food at all. */
    public boolean isGift(ItemStack stack) {
        return this.isPrecious(stack) || this.isEdible(stack);
    }

    public boolean isPrecious(ItemStack stack) {
        return stack.is(ModTags.RIFLEBIRD_HARPY_PRECIOUS);
    }

    @Override
    public boolean isEdible(ItemStack stack) {
        return stack.has(DataComponents.FOOD) && !stack.is(ModItems.RIFLEBIRD_EGG.get());
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return false;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!this.isGift(stack)) return super.mobInteract(player, hand);
        if (this.level() instanceof ServerLevel server) {
            boolean precious = this.isPrecious(stack);
            stack.consume(1, player);
            this.heal(2.0F);
            this.addAffection(player, precious ? 3 : 1);
            this.playSound(this.getEatSound(), 1.0F, this.getVoicePitch());
            server.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight() + 0.2, this.getZ(), precious ? 6 : 2, 0.3, 0.2, 0.3, 0.0);
            // A happy bird, fond of the giver, may lay an egg for them before long.
            if (this.getAffection(player) >= FOND && server.getGameTime() >= this.nextEggTime && this.random.nextInt(precious ? 1 : 3) == 0) {
                this.eggDelay = 100;
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.eggDelay > 0 && --this.eggDelay == 0) this.eggIsDue(server);
        // Couldn't get to a nest in time: she lays it where she is.
        if (this.eggDue && server.getGameTime() >= this.eggDeadline) this.layEgg(server);
        if (this.tickCount % 200 == 0) this.checkNest(server);
    }

    /** Keeps her home pointed at a nest: forgets one that's gone, adopts one nearby, and now and then lays in it. */
    private void checkNest(ServerLevel level) {
        BlockPos home = this.getHomePos();
        if (home != null && !this.isNest(home)) this.setHomePos(home = null);
        if (home == null) {
            BlockPos nest = this.findNest();
            if (nest != null) this.setHomePos(nest);
        } else if (!this.eggDue && level.getGameTime() >= this.nextEggTime && this.isNestWithRoom(home)
                && this.random.nextInt(OWN_EGG_CHANCE) == 0) {
            this.eggIsDue(level);
        }
    }

    /** Off to a nest to lay it (LayInNestGoal), or straight onto the ground if there's no nest around. */
    private void eggIsDue(ServerLevel level) {
        this.eggNest = this.findNest();
        if (this.eggNest == null) {
            this.layEgg(level);
            return;
        }
        this.eggDue = true;
        this.eggDeadline = level.getGameTime() + NEST_DEADLINE;
    }

    public boolean isEggDue() {
        return this.eggDue;
    }

    /** Where she means to lay her egg: the nest she picked, or (if that one filled up meanwhile) another one nearby. */
    @Nullable
    public BlockPos getEggNest() {
        boolean stale = this.eggNest == null || !this.isNestWithRoom(this.eggNest);
        // A full rescan, so not every tick (odd interval: this runs from a goal check, polled every other tick).
        if (stale && this.tickCount % 21 == 0) this.eggNest = this.findNest();
        return this.eggNest;
    }

    @Override
    protected boolean isSettled() {
        BlockPos home = this.getHomePos();
        return home != null && this.isNest(home);
    }

    private boolean isNest(BlockPos pos) {
        return this.level().getBlockState(pos).is(ModTags.RIFLEBIRD_HARPY_NESTS);
    }

    public boolean isNestWithRoom(BlockPos pos) {
        BlockState state = this.level().getBlockState(pos);
        return state.is(ModTags.RIFLEBIRD_HARPY_NESTS) && RiflebirdNestBlock.hasRoom(state);
    }

    /** Her own nest if it has room, otherwise the closest nest nearby with room. */
    @Nullable
    public BlockPos findNest() {
        BlockPos home = this.getHomePos();
        if (home != null && this.isNestWithRoom(home)) return home;
        BlockPos best = null;
        for (BlockPos pos : BlockPos.betweenClosed(this.blockPosition().offset(-NEST_RANGE, -8, -NEST_RANGE),
                this.blockPosition().offset(NEST_RANGE, 4, NEST_RANGE))) {
            if (this.isNestWithRoom(pos) && (best == null || pos.distSqr(this.blockPosition()) < best.distSqr(this.blockPosition()))) {
                best = pos.immutable();
            }
        }
        return best;
    }

    /** Settled in the nest: lays the egg into it, and the nest becomes her home. */
    public void layEggInNest(BlockPos nest) {
        if (!(this.level() instanceof ServerLevel level) || !this.isNestWithRoom(nest)) return;
        RiflebirdNestBlock.addEgg(level, nest, level.getBlockState(nest));
        this.setHomePos(nest);
        this.laid(level);
    }

    private void layEgg(ServerLevel level) {
        ItemEntity egg = this.spawnAtLocation(new ItemStack(ModItems.RIFLEBIRD_EGG.get()));
        if (egg != null) egg.setExtendedLifetime();
        this.laid(level);
    }

    private void laid(ServerLevel level) {
        this.eggDue = false;
        this.eggNest = null;
        this.nextEggTime = level.getGameTime() + EGG_COOLDOWN;
        this.playSound(ModSounds.RIFLEBIRD_LAY.get(), 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 0.3, this.getZ(), 6, 0.3, 0.2, 0.3, 0.0);
    }

    /** While she dances to a jukebox: music notes, and (once in a while) a little more fondness for her audience. */
    private void danceToMusic(int ticks) {
        if (!(this.level() instanceof ServerLevel server)) return;
        if (ticks % 10 == 0) {
            server.sendParticles(ParticleTypes.NOTE, this.getX(), this.getEyeY() + 0.6, this.getZ(), 0, this.random.nextInt(24) / 24.0, 0, 0, 1);
        }
        if (ticks == MUSIC_AFFECTION_AFTER && server.getGameTime() >= this.nextMusicAffectionTime) {
            this.nextMusicAffectionTime = server.getGameTime() + MUSIC_AFFECTION_COOLDOWN;
            for (Player player : server.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(10.0), p -> !p.isSpectator())) {
                this.addAffection(player, 1);
                server.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight() + 0.2, this.getZ(), 2, 0.3, 0.2, 0.3, 0.0);
            }
        }
    }

    @Override
    protected void onPetted(Player player) {
        super.onPetted(player);
        if (this.onGround()) this.startTimedAction(Action.DANCING, 40); // a happy little shimmy
    }

    @Override
    public Component describeBond(Player player) {
        int affection = this.getAffection(player);
        String level = affection >= FOND ? "fond" : affection > 0 ? "shows_off" : "ignores";
        return Component.translatable("bond.harpies.riflebird." + level, affection);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("NextEgg", this.nextEggTime);
        tag.putBoolean("EggDue", this.eggDue);
        tag.putLong("EggDeadline", this.eggDeadline);
        tag.putString("Variant", this.getVariant().getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.nextEggTime = tag.getLong("NextEgg");
        this.eggDue = tag.getBoolean("EggDue");
        this.eggDeadline = tag.getLong("EggDeadline");
        this.eggNest = null; // looked up again on the next check
        this.setVariant(Variant.byName(tag.getString("Variant")));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.getAction() == Action.DANCING ? null : ModSounds.RIFLEBIRD_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.RIFLEBIRD_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.RIFLEBIRD_DEATH.get();
    }

    @Override
    protected SoundEvent getEatSound() {
        return ModSounds.RIFLEBIRD_EAT.get();
    }
}
