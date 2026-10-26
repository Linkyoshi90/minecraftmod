package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.entity.ai.AmphibiousMoveControl;
import com.linkyoshi90.monstergirls.entity.ai.LaySpawnGoal;
import com.linkyoshi90.monstergirls.entity.ai.ShareBedGoal;
import com.linkyoshi90.monstergirls.entity.ai.SitWhenOrderedGoal;
import com.linkyoshi90.monstergirls.entity.ai.TadpoleGoal;
import com.linkyoshi90.monstergirls.registry.ModSounds;
import com.linkyoshi90.monstergirls.registry.ModTags;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.PathType;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * What the frog and toad families share (see anuran_field_guide.html): an amphibious body, a synced "what am I
 * doing" action that drives the GeckoLib animation, and the need for water. Any anuran left dry for too long slowly
 * takes damage (frogs dry out fast, toads slowly); water or rain resets the clock.
 */
public abstract class AbstractAnuran extends PathfinderMob implements GeoEntity, Bonded, Painted {
    private static final EntityDataAccessor<Byte> DATA_ACTION =
            SynchedEntityData.defineId(AbstractAnuran.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_STAGE =
            SynchedEntityData.defineId(AbstractAnuran.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DATA_CLIMBING =
            SynchedEntityData.defineId(AbstractAnuran.class, EntityDataSerializers.BOOLEAN);
    /** Index into the species' colour variants ({@link AnuranVariants}); 0 = the default colours. */
    private static final EntityDataAccessor<Byte> DATA_VARIANT =
            SynchedEntityData.defineId(AbstractAnuran.class, EntityDataSerializers.BYTE);
    /** How far along her pregnancy is, 0-100, for the renderer to round out her belly. */
    private static final EntityDataAccessor<Byte> DATA_BELLY =
            SynchedEntityData.defineId(AbstractAnuran.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DATA_MOONSTRUCK =
            SynchedEntityData.defineId(AbstractAnuran.class, EntityDataSerializers.BOOLEAN);
    private static final Map<String, RawAnimation> ANIMATIONS = new ConcurrentHashMap<>();
    /** Dry skin hurts this often once the limit is passed. */
    private static final int DRY_DAMAGE_INTERVAL = 40;

    /** Every species uses the subset its animation file has; see {@link #actionAnimation}. */
    public enum Action {
        NONE(null, false), CROAKING("croak", false), TONGUE("tongue", false), SITTING("sit", false), BELLOWING("bellow", false),
        PUFFING("puff", false), DIGGING("dig", false), POUNDING("pound", false), SHOVING("shove", false), SLEEPING("sleep", false),
        WARNING("warn", false), PEEPING("peep", false), GUARDING("guard", false), STOMPING("stomp", false), LIFTING("lift", false),
        SCREAMING("scream", false), BURROWING("burrow", true), SIPPING("sip", false), UNKEN("unken", false), TRILLING("trill", false),
        GULPING("gulp", false), SQUIRTING("squirt", false), CHORUS("chorus", false), SWEEPING("sweep", false),
        BROODING("brood", false), BIRTH("birth", true), PULLING("pull", true);

        /** The animation.&lt;species&gt;.&lt;name&gt; this action plays, and whether it plays once and holds its last pose. */
        public final String animation;
        public final boolean once;

        Action(String animation, boolean once) {
            this.animation = animation;
            this.once = once;
        }
    }

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int dryTicks;
    private int timedActionTicks;
    private final Map<UUID, Integer> affection = new HashMap<>();
    private final Gestation gestation = new Gestation();
    private long nextGiftTime;
    private long nextPetTime;
    private int growTicks;
    /** Set when loaded data (a save, or /summon NBT) named a variant: spawn setup must not re-roll it. */
    private boolean variantFromData;
    @Nullable
    private UUID father;
    private boolean orderedToSit;
    private final com.linkyoshi90.monstergirls.entity.Orders orders = new com.linkyoshi90.monstergirls.entity.Orders();
    private boolean inBed;
    /** Fed a slimeball: in the mood, and a vanilla frog in love will do (see {@link #courtFrog}). */
    private int frogLove;
    /** The father of spawn sired by a vanilla frog (a third of its tadpoles hatch as vanilla tadpoles). */
    public static final UUID FROG_FATHER = new UUID(0L, 0x46524F47L);

    /** Five in-game days from hatching to adult: the first two and a half as a tadpole, the rest as a froglet. */
    public static final int GROW_TICKS = 5 * 24000;
    public static final int TADPOLE_TICKS = GROW_TICKS / 2;
    public static final float BABY_SCALE = 0.4F;
    /** A tadpole out of water dries out within half a minute. */
    private static final int TADPOLE_DRY_LIMIT = 600;

    /** Life stages. The Surinam toad's young skip the tadpole and keep the adult model at a small scale. */
    public static final byte ADULT = 0;
    public static final byte FROGLET = 1;
    public static final byte TADPOLE = 2;
    private static final int GIFT_COOLDOWN = 600;
    private static final int PET_COOLDOWN = 1200;

    protected AbstractAnuran(EntityType<? extends AbstractAnuran> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, this.waterPathMalus());
        this.moveControl = new AmphibiousMoveControl(this, 1.0F); // swims up and down instead of walking the bottom
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
        this.goalSelector.addGoal(-1, new TadpoleGoal(this)); // above everything, FloatGoal included
        this.goalSelector.addGoal(-1, new SitWhenOrderedGoal<>(this));
        this.goalSelector.addGoal(1, new com.linkyoshi90.monstergirls.entity.ai.StayNearGoal<>(this));
        this.goalSelector.addGoal(3, new com.linkyoshi90.monstergirls.entity.girl.FarmhandGoal<>(this));
        this.targetSelector.addGoal(1, new com.linkyoshi90.monstergirls.entity.ai.StanceTargetGoal<>(this));
        this.goalSelector.addGoal(2, new LaySpawnGoal(this));
        this.goalSelector.addGoal(3, new ShareBedGoal<>(this));
    }

    /** e.g. "bullywug"; animations are looked up as animation.&lt;species&gt;.&lt;name&gt;. */
    protected abstract String species();

    /** The action played by an ambient call, or null for none (a buried toad stays quiet). */
    @Nullable
    protected Action ambientAction() {
        return Action.CROAKING;
    }

    /** A movement animation that beats the idle/walk/swim choice (gliding, climbing), or null. */
    @Nullable
    protected String specialMovementAnimation() {
        return this.isClimbingWall() ? "climb" : null;
    }

    protected String idleAnimation() {
        return "idle";
    }

    /** Species that climb walls like spiders (tree frog, poison dart frog). */
    protected boolean canClimbWalls() {
        return false;
    }

    /** How many ticks she can stay dry before her skin starts to suffer. */
    protected abstract int dryLimitTicks();

    /** Food she accepts from a hand. */
    public abstract boolean isFood(ItemStack stack);

    public SoundEvent getEatSound() {
        return ModSounds.anuran(this.species()).eat().get();
    }

    public SoundEvent getTongueSound() {
        return ModSounds.anuran(this.species()).tongue().get();
    }

    public SoundEvent getBellowSound() {
        return ModSounds.anuran(this.species()).bellow().get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.anuran(this.species()).ambient().get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.anuran(this.species()).hurt().get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.anuran(this.species()).death().get();
    }

    @Override
    public void playAmbientSound() {
        Action action = this.ambientAction();
        if (action == null) return;
        super.playAmbientSound();
        if (this.getAction() == Action.NONE && !this.isInWater()) this.startTimedAction(action, 30);
    }

    /** Path cost of water: 0 for a swimmer, higher for a land lover. */
    protected float waterPathMalus() {
        return 0.0F;
    }

    // --- action ---------------------------------------------------------------------------------------------------

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ACTION, (byte) Action.NONE.ordinal());
        builder.define(DATA_CLIMBING, false);
        builder.define(DATA_STAGE, ADULT);
        builder.define(DATA_VARIANT, (byte) 0);
        builder.define(DATA_BELLY, (byte) 0);
        builder.define(DATA_MOONSTRUCK, false);
        builder.define(DATA_PAINT, (byte) 0);
    }

    // --- colour variant -------------------------------------------------------------------------------------------

    /** This species' colour variants, or null if it has none (see {@link AnuranVariants}). */
    @Nullable
    private AnuranVariants.Set variants() {
        return AnuranVariants.forSpecies(this.species());
    }

    /** The variant's name ("golden"), or "" for a species without variants. */
    public String getVariantName() {
        AnuranVariants.Set set = this.variants();
        return set == null ? "" : set.nameAt(this.entityData.get(DATA_VARIANT));
    }

    /** The texture suffix: "" for the default colours, otherwise "_golden" (textures/entity/bullywug_golden.png). */
    public String getVariantSuffix() {
        AnuranVariants.Set set = this.variants();
        int index = this.entityData.get(DATA_VARIANT);
        return set == null || index <= 0 || index >= set.names().size() ? "" : "_" + set.names().get(index);
    }

    public void setVariant(String name) {
        AnuranVariants.Set set = this.variants();
        this.entityData.set(DATA_VARIANT, (byte) (set == null ? 0 : set.indexOf(name)));
    }

    /** Picks this species' variant for a biome (with the rare rolls); species without variants stay as they are. */
    public void rollVariant(Holder<Biome> biome) {
        AnuranVariants.Set set = this.variants();
        if (set != null) this.entityData.set(DATA_VARIANT, (byte) set.pick(biome, this.random));
    }

    /** Whether the variant came from loaded data. Vanilla /summon loads the NBT and then still runs finalizeSpawn. */
    protected boolean hasVariantFromData() {
        return this.variantFromData;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData groupData) {
        this.rollPaint(spawnType);
        if (!this.variantFromData) this.rollVariant(level.getBiome(this.blockPosition()));
        return super.finalizeSpawn(level, difficulty, spawnType, groupData);
    }

    public Action getAction() {
        return Action.values()[this.entityData.get(DATA_ACTION)];
    }

    public void setAction(Action action) {
        this.entityData.set(DATA_ACTION, (byte) action.ordinal());
    }

    /** Plays an action's animation for a while (unless something else is animating her), then goes back to NONE. */
    public void startTimedAction(Action action, int ticks) {
        if (this.getAction() != Action.NONE && this.timedActionTicks <= 0) return;
        this.setAction(action);
        this.timedActionTicks = ticks;
    }

    public boolean isClimbingWall() {
        return this.entityData.get(DATA_CLIMBING);
    }

    @Override
    public boolean onClimbable() {
        return this.isClimbingWall();
    }

    // --- spawning -------------------------------------------------------------------------------------------------

    private static boolean onSpawnableGround(EntityType<? extends AbstractAnuran> type, LevelAccessor level, BlockPos pos) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        TagKey<Block> tag = TagKey.create(Registries.BLOCK, key.withPath(path -> path + "_spawnable_on"));
        return level.getBlockState(pos.below()).is(tag);
    }

    /** Ground from the species own tag (data/monstergirls/tags/block/&lt;species&gt;_spawnable_on.json). */
    public static boolean checkSpawnRules(EntityType<? extends AbstractAnuran> type, ServerLevelAccessor level, MobSpawnType spawnType,
                                          BlockPos pos, RandomSource random) {
        return onSpawnableGround(type, level, pos);
    }

    /** Same, and also within a few blocks of water: the frogs of ponds and streams. */
    public static boolean checkWaterSideSpawnRules(EntityType<? extends AbstractAnuran> type, ServerLevelAccessor level, MobSpawnType spawnType,
                                                   BlockPos pos, RandomSource random) {
        return onSpawnableGround(type, level, pos) && waterNearby(level, pos, 6);
    }

    // --- water ----------------------------------------------------------------------------------------------------

    public boolean isWet() {
        return this.isInWaterOrRain();
    }

    public int getDryTicks() {
        return this.dryTicks;
    }

    public void setDryTicks(int ticks) {
        this.dryTicks = ticks;
    }

    /** Dry enough that she should start looking for water (half-way to hurting). */
    public boolean isDry() {
        return this.dryTicks > this.effectiveDryLimit() / 2;
    }

    public boolean isDriedOut() {
        return this.dryTicks > this.effectiveDryLimit();
    }

    private int effectiveDryLimit() {
        if (this.isTadpole()) return TADPOLE_DRY_LIMIT;
        return this.isBaby() ? this.dryLimitTicks() / 2 : this.dryLimitTicks();
    }

    /** The closest water block within the radius, or null. */
    @Nullable
    public static BlockPos findWater(LevelReader level, BlockPos origin, int radius) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -3, -radius), origin.offset(radius, 3, radius))) {
            if (!level.getFluidState(pos).is(FluidTags.WATER)) continue;
            double dist = pos.distSqr(origin);
            if (dist < bestDist) {
                bestDist = dist;
                best = pos.immutable();
            }
        }
        return best;
    }

    /**
     * The nearest water she can actually get to. The nearest by distance can lie below a ledge or past a drop, and
     * heading for it strands her at the edge; this tries the nearest few ponds and takes the first she has a path to.
     */
    @Nullable
    public static BlockPos findReachableWater(PathfinderMob mob, int radius) {
        BlockPos origin = mob.blockPosition();
        java.util.List<BlockPos> water = new java.util.ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -3, -radius), origin.offset(radius, 3, radius))) {
            if (mob.level().getFluidState(pos).is(FluidTags.WATER)) water.add(pos.immutable());
        }
        water.sort(java.util.Comparator.comparingDouble(p -> p.distSqr(origin)));
        java.util.List<BlockPos> tried = new java.util.ArrayList<>();
        for (BlockPos pos : water) {
            if (tried.size() >= 6) break;
            if (tried.stream().anyMatch(t -> t.distSqr(pos) <= 9)) continue; // one try per pond, not per block
            tried.add(pos);
            net.minecraft.world.level.pathfinder.Path path = mob.getNavigation().createPath(pos, 0);
            if (path != null && path.canReach()) return pos;
        }
        return null;
    }

    public static boolean waterNearby(LevelReader level, BlockPos origin, int radius) {
        return findWater(level, origin, radius) != null;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new AmphibiousPathNavigation(this, level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel) {
            Painted.tickCopper(this, this);
            if (this.isWet()) this.dryTicks = 0;
            else this.dryTicks++;
            if (this.isDriedOut() && this.tickCount % DRY_DAMAGE_INTERVAL == 0) {
                this.hurt(this.damageSources().dryOut(), 1.0F);
            }
            if (this.timedActionTicks > 0 && --this.timedActionTicks == 0) this.setAction(Action.NONE);
            this.entityData.set(DATA_CLIMBING, this.canClimbWalls() && this.horizontalCollision && !this.isInWater());
            this.gestation.tick();
            if (this.tickCount % 10 == 0) this.entityData.set(DATA_BELLY, (byte) Math.round(this.gestation.progress() * 100));
            if (this.orderedToSit && this.getAction() == Action.NONE) this.setAction(Action.SITTING);
            if (this.frogLove > 0 && --this.frogLove % 10 == 0) this.courtFrog((ServerLevel) this.level());
            if (this.tickCount % 40 == 0 && !this.isBaby()) this.gatherFrogs((ServerLevel) this.level());
            if (this.isBaby()) {
                this.growTicks++;
                if (this.growTicks >= GROW_TICKS) this.setBaby(false);
                else if (this.isTadpole() && this.growTicks >= TADPOLE_TICKS) this.setStage(FROGLET);
            }
        }
    }

    // --- food -----------------------------------------------------------------------------------------------------

    /**
     * Empty hand: a pat. Food she likes, or any meat, is a gift that builds affection (and heals her if she is hurt);
     * a gift only counts once every half minute.
     */
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult paint = Painted.interact(this, this, player, hand);
        if (paint != null) return paint;
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.MAIN_HAND && stack.is(Items.STICK) && !this.isTadpole()) {
            if (!this.level().isClientSide) Orders.onStick(this, this.orders, player);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (hand == InteractionHand.MAIN_HAND && stack.isEmpty() && !player.isShiftKeyDown()) {
            if (this.level() instanceof ServerLevel server) {
                if (server.getGameTime() >= this.nextPetTime) {
                    this.nextPetTime = server.getGameTime() + PET_COOLDOWN;
                    this.addAffection(player, 1);
                }
                this.getLookControl().setLookAt(player);
                server.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight() + 0.2, this.getZ(), 2, 0.3, 0.2, 0.3, 0.0);
                this.playSound(this.getAmbientSound(), 1.0F, this.getVoicePitch() * 1.1F);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (hand == InteractionHand.MAIN_HAND && stack.is(Items.SLIME_BALL) && !this.isBaby() && this.canShareBed() && this.frogLove <= 0) {
            if (this.level() instanceof ServerLevel server) {                // in the mood: a vanilla frog in love will do
                if (!player.getAbilities().instabuild) stack.shrink(1);
                this.frogLove = 600;
                server.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight() + 0.2, this.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        boolean meat = stack.is(ModTags.MEAT);
        if (!this.isBaby() && (meat || this.isFood(stack))) {
            boolean hurt = this.getHealth() < this.getMaxHealth();
            boolean giftWelcome = this.level().getGameTime() >= this.nextGiftTime;
            if (hurt || giftWelcome) {
                if (this.level() instanceof ServerLevel server) {
                    if (!player.getAbilities().instabuild) stack.shrink(1);
                    if (hurt) this.heal(4.0F);
                    if (giftWelcome) {
                        this.nextGiftTime = server.getGameTime() + GIFT_COOLDOWN;
                        this.addAffection(player, meat ? 2 : 1);
                    }
                    this.playSound(this.getEatSound(), 1.0F, this.getVoicePitch());
                    this.startTimedAction(Action.TONGUE, 14);
                    server.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight() + 0.2, this.getZ(), 2, 0.3, 0.2, 0.3, 0.0);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }

    // --- frog kin ----------------------------------------------------------------------------------------------------

    public int getFrogLove() {
        return this.frogLove;
    }

    /** She walks up to a vanilla frog in love (fed a slimeball) within 8 blocks, and she is expecting frogspawn. */
    public void courtFrog(ServerLevel level) {
        if (!this.gestation.canConceive()) return;
        net.minecraft.world.entity.animal.frog.Frog frog = null;
        for (var f : level.getEntitiesOfClass(net.minecraft.world.entity.animal.frog.Frog.class, this.getBoundingBox().inflate(8), f -> f.isInLove() && !f.isBaby())) {
            if (frog == null || f.distanceToSqr(this) < frog.distanceToSqr(this)) frog = f;
        }
        if (frog == null) return;
        if (this.distanceToSqr(frog) > 2.5 * 2.5) {
            this.getNavigation().moveTo(frog, 1.0);
            return;
        }
        this.gestation.conceive(FROG_FATHER);
        this.frogLove = 0;
        frog.resetLove();
        level.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight() + 0.2, this.getZ(), 6, 0.4, 0.3, 0.4, 0.0);
    }

    /** Her kin: vanilla frogs within 16 blocks hop over and keep near her. */
    public void gatherFrogs(ServerLevel level) {
        for (var f : level.getEntitiesOfClass(net.minecraft.world.entity.animal.frog.Frog.class, this.getBoundingBox().inflate(16),
                f -> !f.isLeashed() && f.distanceToSqr(this) > 5 * 5)) {
            f.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.WALK_TARGET,
                    new net.minecraft.world.entity.ai.memory.WalkTarget(this, 1.0F, 3));
        }
    }

    // --- relationships and young ------------------------------------------------------------------------------------

    @Override
    public int getAffection(Player player) {
        return this.affection.getOrDefault(player.getUUID(), 0);
    }

    @Override
    public void addAffection(Player player, int amount) {
        this.affection.merge(player.getUUID(), amount, Integer::sum);
    }

    @Override
    public void setAffection(UUID player, int amount) {
        this.affection.put(player, amount);
    }

    @Nullable
    @Override
    public UUID getFather() {
        return this.father;
    }

    @Override
    public void setFather(@Nullable UUID father) {
        this.father = father;
    }

    /** How round her belly is, 0 (not expecting) to 1 (due); synced to the client. */
    public float getBellySwell() {
        return this.entityData.get(DATA_BELLY) / 100.0F;
    }

    // --- stick orders --------------------------------------------------------------------------------------------

    @Override
    public com.linkyoshi90.monstergirls.entity.Orders getOrders() {
        return this.orders;
    }

    /** Her stance has the last word on whom she fights (and companions never fight each other). */
    @Override
    public void setTarget(@Nullable net.minecraft.world.entity.LivingEntity target) {
        super.setTarget(this.orders == null || this.orders.allows(this, target) ? target : null);
    }

    @Override
    public boolean isOrderedToSit() {
        return this.orderedToSit;
    }

    @Override
    public void setOrderedToSit(boolean sit) {
        this.orderedToSit = sit;
        if (sit) {
            this.getNavigation().stop();
            this.setTarget(null);
            this.setAction(Action.SITTING);
        } else if (this.getAction() == Action.SITTING) {
            this.setAction(Action.NONE);
        }
    }

    @Override
    public Component describeBond(Player player) {
        int a = this.getAffection(player);
        String key = a >= BED_AFFECTION ? "devoted" : a >= 10 ? "fond" : a >= 3 ? "curious" : "wary";
        return Component.translatable("bond.monstergirls.anuran." + key, a);
    }

    @Override
    public Gestation getGestation() {
        return this.gestation;
    }

    /** The Surinam toad lives in the water and carries her young on her back instead. */
    protected boolean isAquaticOnly() {
        return false;
    }

    @Override
    public boolean canShareBed() {
        return !this.isBaby() && !this.isAquaticOnly() && this.gestation.canConceive();
    }

    @Override
    public boolean isMoonstruck() {
        return this.entityData.get(DATA_MOONSTRUCK);
    }

    @Override
    public void setMoonstruck(boolean moonstruck) {
        this.entityData.set(DATA_MOONSTRUCK, moonstruck);
    }

    /** In the bed she sits snug beside the sleeper (every anuran model has a "sit" pose). */
    @Override
    public void settleInBed() {
        this.inBed = true;
        this.setAction(Action.SITTING);
    }

    /** Settled in or beside the bed for the night (no other rest takes her away from it). */
    public boolean isInBed() {
        return this.inBed;
    }

    @Override
    public void leaveBed() {
        this.inBed = false;
        if (this.getAction() == Action.SITTING && !this.orderedToSit) this.setAction(Action.NONE);
    }

    @Override
    public boolean isBaby() {
        return this.entityData.get(DATA_STAGE) != ADULT;
    }

    public boolean isTadpole() {
        return this.entityData.get(DATA_STAGE) == TADPOLE;
    }

    public byte getStage() {
        return this.entityData.get(DATA_STAGE);
    }

    public void setStage(byte stage) {
        this.entityData.set(DATA_STAGE, stage);
    }

    /** A froglet (or, for the Surinam toad, a toadling); false grows her up. */
    public void setBaby(boolean baby) {
        this.setStage(baby ? FROGLET : ADULT);
        this.growTicks = 0;
    }

    /** Hatches as a tadpole: it swims in the water until it grows legs. */
    public void setTadpole() {
        this.setStage(this.hasYoungModels() ? TADPOLE : FROGLET);
        this.growTicks = 0;
    }

    /** Whether this species has tadpole and froglet models (everyone but the Surinam toad). */
    public boolean hasYoungModels() {
        return true;
    }

    /** What the model, animations and texture are called at this stage: bullywug, bullywug_tadpole, bullywug_froglet. */
    public String modelName() {
        if (!this.hasYoungModels()) return this.species();
        return switch (this.getStage()) {
            case TADPOLE -> this.species() + "_tadpole";
            case FROGLET -> this.species() + "_froglet";
            default -> this.species();
        };
    }

    @Override
    protected int decreaseAirSupply(int currentAir) {
        return this.isTadpole() ? currentAir : super.decreaseAirSupply(currentAir); // gills
    }

    public void setGrowTicks(int ticks) {
        this.growTicks = ticks;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_STAGE.equals(key)) this.refreshDimensions();
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        EntityDimensions dimensions = super.getDefaultDimensions(pose);
        if (this.isTadpole()) return EntityDimensions.scalable(0.4F, 0.35F).withEyeHeight(0.25F);
        return this.isBaby() ? dimensions.scale(BABY_SCALE) : dimensions;
    }

    /** Wild pond and forest dwellers stay put rather than blinking out of existence when you walk away. */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Waxed", this.waxed);
        tag.putInt("DryTicks", this.dryTicks);
        tag.putByte("Stage", this.getStage());
        tag.putInt("GrowTicks", this.growTicks);
        if (this.variants() != null) tag.putString("Variant", this.getVariantName());
        this.gestation.save(tag);
        if (this.father != null) tag.putUUID("Father", this.father);
        tag.putBoolean("OrderedToSit", this.orderedToSit);
        if (this.getPaint() > 0) tag.putString("Paint", this.getPaintName());
        tag.put("Orders", this.orders.save());
        CompoundTag affectionTag = new CompoundTag();
        this.affection.forEach((uuid, a) -> affectionTag.putInt(uuid.toString(), a));
        tag.put("Affection", affectionTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.waxed = tag.getBoolean("Waxed");
        this.dryTicks = tag.getInt("DryTicks");
        this.entityData.set(DATA_STAGE, tag.contains("Stage") ? tag.getByte("Stage") : (tag.getBoolean("Baby") ? FROGLET : ADULT));
        this.growTicks = tag.getInt("GrowTicks");
        if (tag.contains("Variant")) {
            this.variantFromData = true;
            this.setVariant(tag.getString("Variant"));
        }
        this.gestation.load(tag);
        this.father = tag.hasUUID("Father") ? tag.getUUID("Father") : null;
        this.orderedToSit = tag.getBoolean("OrderedToSit");
        if (tag.contains("Paint")) this.setPaint(com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl.PAINTS.indexOf(tag.getString("Paint")) + 1);
        this.orders.load(tag.getCompound("Orders"));
        this.affection.clear();
        CompoundTag affectionTag = tag.getCompound("Affection");
        for (String key : affectionTag.getAllKeys()) this.affection.put(UUID.fromString(key), affectionTag.getInt(key));
    }

    // --- animation ------------------------------------------------------------------------------------------------

    protected RawAnimation anim(String name) {
        return ANIMATIONS.computeIfAbsent("animation." + this.modelName() + "." + name, n -> RawAnimation.begin().thenLoop(n));
    }

    private RawAnimation animOnce(String name) {
        return ANIMATIONS.computeIfAbsent("once:animation." + this.modelName() + "." + name,
                n -> RawAnimation.begin().thenPlayAndHold(n.substring("once:".length())));
    }

    /** Animation while moving on land ("walk"; frogs switch to "hop" when running or fighting). */
    protected String movingAnimation() {
        return "walk";
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, this::animate));
    }

    private PlayState animate(AnimationState<AbstractAnuran> state) {
        if (this.isBaby() && this.hasYoungModels()) {
            // tadpoles and froglets only know the basic moves
            if (this.isTadpole()) return state.setAndContinue(this.anim(state.isMoving() || this.isInWater() && !this.onGround() ? "swim" : "idle"));
            if (this.isInWater()) return state.setAndContinue(this.anim("swim"));
            return state.setAndContinue(this.anim(state.isMoving() ? (this.isAggressive() ? "hop" : this.movingAnimation().equals("hop") ? "hop" : "walk") : "idle"));
        }
        Action action = this.getAction();
        if (action.animation != null) return state.setAndContinue(action.once ? this.animOnce(action.animation) : this.anim(action.animation));
        String special = this.specialMovementAnimation();
        if (special != null) return state.setAndContinue(this.anim(special));
        if (this.isInWater()) return state.setAndContinue(this.anim("swim"));
        return state.setAndContinue(this.anim(state.isMoving() ? this.movingAnimation() : this.idleAnimation()));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    // --- Harmontide paints (as for the girls, see AbstractMonsterGirl.PAINTS) --------------------------------------

    private static final net.minecraft.network.syncher.EntityDataAccessor<Byte> DATA_PAINT =
            net.minecraft.network.syncher.SynchedEntityData.defineId(AbstractAnuran.class, net.minecraft.network.syncher.EntityDataSerializers.BYTE);

    /** 0 unpainted, else 1 + an index into AbstractMonsterGirl.PAINTS. */
    public int getPaint() {
        return this.entityData.get(DATA_PAINT);
    }

    public void setPaint(int paint) {
        this.entityData.set(DATA_PAINT, (byte) (paint < 0 || paint > com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl.PAINTS.size() ? 0 : paint));
    }

    /** Copper paint stops weathering once waxed (Painted). */
    private boolean waxed;

    @Override
    public boolean isWaxed() {
        return this.waxed;
    }

    @Override
    public void setWaxed(boolean waxed) {
        this.waxed = waxed;
    }

    @Override
    @Nullable
    public String getPaintName() {
        int p = this.getPaint();
        return p == 0 ? null : com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl.PAINTS.get(p - 1);
    }

    /** One wild one in 64 (never a hatchling: she takes her mother's) is painted. */
    protected void rollPaint(MobSpawnType spawnType) {
        if (spawnType != MobSpawnType.BREEDING && !this.hasVariantFromData()
                && this.random.nextInt(com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl.PAINT_RARITY) == 0) {
            this.setPaint(Painted.roll(this.random));
        }
    }
}
