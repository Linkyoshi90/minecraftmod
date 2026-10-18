package com.linkyoshi90.monstergirls.entity;

import com.linkyoshi90.monstergirls.entity.ai.BuildNestGoal;
import com.linkyoshi90.monstergirls.entity.ai.GiftGoal;
import com.linkyoshi90.monstergirls.entity.ai.ShareBedGoal;
import com.linkyoshi90.monstergirls.entity.ai.SitWhenOrderedGoal;
import com.linkyoshi90.monstergirls.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
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
 * What every harpy shares: parrot-style walking/fluttering flight (unless the species is flightless), a synced
 * "what am I doing" action that drives the GeckoLib animation (set by goals, or for a fixed time), a remembered
 * home, bath cooldown, carrying one small item in her hands (eating it if it's food, otherwise dropping it
 * somewhere later), and her affection for each player (raised by feeding and petting; see /monstergirls bonds).
 */
public abstract class AbstractHarpy extends PathfinderMob implements GeoEntity, FlyingAnimal, Bonded {
    private static final EntityDataAccessor<Byte> DATA_ACTION =
            SynchedEntityData.defineId(AbstractHarpy.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DATA_BABY =
            SynchedEntityData.defineId(AbstractHarpy.class, EntityDataSerializers.BOOLEAN);
    /** How far along her pregnancy is, 0-100, for the renderer to round out her belly. */
    private static final EntityDataAccessor<Byte> DATA_BELLY =
            SynchedEntityData.defineId(AbstractHarpy.class, EntityDataSerializers.BYTE);
    /** Gifts for a favourite player come every five to ten minutes. */
    private static final int GIFT_INTERVAL = 6000;
    private static final Map<String, RawAnimation> ANIMATIONS = new ConcurrentHashMap<>();
    /** Five in-game days from hatchling to adult. */
    public static final int GROW_TICKS = 5 * 24000;
    public static final float BABY_SCALE = 0.5F;
    /** A gift of meat only counts this often per harpy. */
    private static final int MEAT_GIFT_COOLDOWN = 600;
    private static final int EAT_TICKS = 40;
    private static final Vec3i PICKUP_REACH = new Vec3i(1, 1, 1);
    /** Petting raises affection at most once per this many ticks (per harpy), so it can't be spammed. */
    private static final int PET_AFFECTION_COOLDOWN = 1200;

    /** Every species uses the subset its animation file has; see {@link #actionAnimation}. */
    /** How far away a harpy hears (and reacts to) a jukebox playing. */
    public static final int MUSIC_RANGE = 16;

    public enum Action { NONE, SINGING, BATHING, CAWING, KNOCKING, PRANKING, DISPLAYING, KICKING, BROODING, HOOTING, SLEEPING, POUNCING, BEGGING, DANCING, STRIKING, CRYING, CACHING, SITTING, LYING, NESTING }

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    @Nullable
    private BlockPos homePos;
    private long nextBathTime;
    private int heldTicks;
    private int carryLimit;
    private int pickupCooldown;
    private int timedActionTicks;
    private final Map<UUID, Integer> affection = new HashMap<>();
    private long nextPetAffectionTime;
    /** Set when loaded data (a save, or /summon NBT) named a variant: spawn setup must not re-roll it. */
    private boolean variantFromData;
    @Nullable
    private BlockPos musicPos;
    private long nextMusicCheck;
    private final Gestation gestation = new Gestation();
    private int growTicks;
    private long nextMeatGiftTime;
    @Nullable
    private UUID father;
    private boolean orderedToSit;
    private long nextGiftTime = -1;

    protected AbstractHarpy(EntityType<? extends AbstractHarpy> type, Level level) {
        super(type, level);
        if (this.canFly()) this.moveControl = new FlyingMoveControl(this, 10, false);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.COCOA, -1.0F);
        this.setCanPickUpLoot(true);
        this.goalSelector.addGoal(-1, new SitWhenOrderedGoal<>(this));
        this.goalSelector.addGoal(2, new BuildNestGoal(this));
        this.goalSelector.addGoal(3, new ShareBedGoal<>(this));
        this.goalSelector.addGoal(3, new GiftGoal(this));
    }

    /** Flightless species (the ostrich) walk with ground navigation, fall normally and have no "fly" animation. */
    protected boolean canFly() {
        return true;
    }

    /** e.g. "sparrow_harpy"; animations are looked up as animation.&lt;species&gt;.&lt;name&gt;. */
    protected abstract String species();

    /** Her species id ("sparrow_harpy"), for eggs and nests. */
    public String speciesId() {
        return this.species();
    }

    /** Picks any of her species' colour variants (a hatchling from a clutch). Species without variants keep theirs. */
    public void randomizeVariant() {
    }

    /** Animation name for an action, or null to fall back to idle/walk/fly. */
    @Nullable
    protected abstract String actionAnimation(Action action);

    /** Munching, when she eats something. */
    protected abstract SoundEvent getEatSound();

    /** A short call; used when begging, snatching and the like. Her normal ambient sound by default. */
    public SoundEvent getChirpSound() {
        return this.getAmbientSound();
    }

    /** One line for /monstergirls bonds: how she feels about this player (e.g. "trusts you (7)"). */
    public abstract Component describeBond(Player player);

    /** Food she eats out of her hands. */
    public abstract boolean isEdible(ItemStack stack);

    /** Non-food items she likes to pick up and carry around. */
    public abstract boolean likesToCarry(ItemStack stack);

    // --- action / home / bath ---------------------------------------------------------------------------------

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ACTION, (byte) Action.NONE.ordinal());
        builder.define(DATA_BABY, false);
        builder.define(DATA_BELLY, (byte) 0);
    }

    public Action getAction() {
        return Action.values()[this.entityData.get(DATA_ACTION)];
    }

    public void setAction(Action action) {
        this.entityData.set(DATA_ACTION, (byte) action.ordinal());
    }

    /** Plays an action's animation for a while (unless a goal is already animating her), then goes back to NONE. */
    public void startTimedAction(Action action, int ticks) {
        if (this.getAction() != Action.NONE && this.timedActionTicks <= 0) return;
        this.setAction(action);
        this.timedActionTicks = ticks;
    }

    /**
     * Whether her variant was given by data. Vanilla /summon loads the NBT and then still runs finalizeSpawn, so
     * species that pick a variant there must skip it when this is true.
     */
    protected boolean hasVariantFromData() {
        return this.variantFromData;
    }

    @Nullable
    public BlockPos getHomePos() {
        return this.homePos;
    }

    public void setHomePos(@Nullable BlockPos pos) {
        this.homePos = pos == null ? null : pos.immutable();
    }

    public boolean isBathReady() {
        return this.level().getGameTime() >= this.nextBathTime;
    }

    public void startBathCooldown(int ticks) {
        this.nextBathTime = this.level().getGameTime() + ticks;
    }

    /**
     * A jukebox playing a song within earshot ({@link #MUSIC_RANGE}), or null. The search runs at most every two
     * seconds; in between, a known jukebox that stops playing is dropped right away.
     */
    @Nullable
    public BlockPos getMusicNearby() {
        long now = this.level().getGameTime();
        if (now >= this.nextMusicCheck) {
            this.nextMusicCheck = now + 40;
            this.musicPos = JukeboxSensing.findPlaying(this.level(), this.blockPosition(), MUSIC_RANGE);
        } else if (this.musicPos != null && !JukeboxSensing.isPlaying(this.level(), this.musicPos)) {
            this.musicPos = null;
        }
        return this.musicPos;
    }

    /** Whether she's willing to go to this spot (crows refuse places where they got hurt before). */
    public boolean isSafeSpot(BlockPos pos) {
        return true;
    }

    /** Called by the bathe goal once she has finished a bath at {@code pos}. */
    public void onBathed(BlockPos pos, BlockState state) {
    }

    // --- affection & petting -----------------------------------------------------------------------------------

    public int getAffection(Player player) {
        return this.affection.getOrDefault(player.getUUID(), 0);
    }

    public void addAffection(Player player, int amount) {
        this.affection.merge(player.getUUID(), amount, Integer::sum);
    }

    public void setAffection(Player player, int amount) {
        this.affection.put(player.getUUID(), amount);
    }

    @Override
    public void setAffection(UUID player, int amount) {
        this.affection.put(player, amount);
    }

    /** Whether any player at all has won some of her affection. */
    public boolean hasAnyAffection() {
        return this.affection.values().stream().anyMatch(a -> a > 0);
    }

    public void forgetAffection(Player player) {
        this.affection.remove(player.getUUID());
    }

    /** Empty main hand, not sneaking: a pat on the head. Species add their own reaction in {@link #onPetted}. */
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && player.getMainHandItem().is(Items.STICK)) {
            if (!this.level().isClientSide) this.toggleSitting(player);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (hand == InteractionHand.MAIN_HAND && !this.isBaby() && player.getMainHandItem().is(ModTags.MEAT)
                && this.level().getGameTime() >= this.nextMeatGiftTime) {
            if (this.level() instanceof ServerLevel server) {
                if (!player.getAbilities().instabuild) player.getMainHandItem().shrink(1);
                this.nextMeatGiftTime = server.getGameTime() + MEAT_GIFT_COOLDOWN;
                this.addAffection(player, 2);
                this.heal(2.0F);
                this.playSound(this.getEatSound(), 1.0F, this.getVoicePitch());
                server.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight() + 0.2, this.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (hand != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty() || player.isShiftKeyDown()) {
            return super.mobInteract(player, hand);
        }
        if (this.level() instanceof ServerLevel server) {
            if (server.getGameTime() >= this.nextPetAffectionTime) {
                this.nextPetAffectionTime = server.getGameTime() + PET_AFFECTION_COOLDOWN;
                this.addAffection(player, 1);
            }
            this.getLookControl().setLookAt(player);
            server.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight() + 0.2, this.getZ(), 2, 0.3, 0.2, 0.3, 0.0);
            this.onPetted(player);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    /** Her reaction to being petted (server side). */
    protected void onPetted(Player player) {
        this.playSound(this.getChirpSound(), 1.0F, this.getVoicePitch() * 1.1F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.homePos != null) tag.putLong("Home", this.homePos.asLong());
        CompoundTag affectionTag = new CompoundTag();
        this.affection.forEach((uuid, a) -> affectionTag.putInt(uuid.toString(), a));
        tag.put("Affection", affectionTag);
        tag.putBoolean("Baby", this.isBaby());
        tag.putInt("GrowTicks", this.growTicks);
        this.gestation.save(tag);
        if (this.father != null) tag.putUUID("Father", this.father);
        tag.putBoolean("OrderedToSit", this.orderedToSit);
        tag.putLong("NextGift", this.nextGiftTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.homePos = tag.contains("Home") ? BlockPos.of(tag.getLong("Home")) : null;
        if (tag.contains("Variant")) this.variantFromData = true;
        this.entityData.set(DATA_BABY, tag.getBoolean("Baby"));
        this.growTicks = tag.getInt("GrowTicks");
        this.gestation.load(tag);
        this.father = tag.hasUUID("Father") ? tag.getUUID("Father") : null;
        this.orderedToSit = tag.getBoolean("OrderedToSit");
        this.nextGiftTime = tag.contains("NextGift") ? tag.getLong("NextGift") : -1;
        this.affection.clear();
        CompoundTag affectionTag = tag.getCompound("Affection");
        for (String key : affectionTag.getAllKeys()) this.affection.put(UUID.fromString(key), affectionTag.getInt(key));
    }

    // --- held item ----------------------------------------------------------------------------------------------

    public boolean isHoldingSomething() {
        return !this.getMainHandItem().isEmpty();
    }

    public boolean wantsItem(ItemStack stack) {
        return this.pickupCooldown <= 0 && !this.isHoldingSomething() && (this.isEdible(stack) || this.likesToCarry(stack));
    }

    @Override
    public boolean wantsToPickUp(ItemStack stack) {
        return this.wantsItem(stack);
    }

    @Override
    protected Vec3i getPickupReach() {
        return PICKUP_REACH; // vanilla has no vertical reach, and a flyer hovers a little above the item
    }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (!this.wantsItem(stack) || !this.isSafeSpot(itemEntity.blockPosition())) return;
        this.onItemPickup(itemEntity);
        this.take(itemEntity, 1);
        ItemStack single = stack.split(1);
        this.onPickedUp(itemEntity, single);
        this.takeIntoHands(single);
        if (stack.isEmpty()) itemEntity.discard();
        else itemEntity.setItem(stack);
    }

    /** Called when she picks something up off the ground (before it goes into her hands). */
    protected void onPickedUp(ItemEntity item, ItemStack stack) {
    }

    /** Puts a single item into her hands (from the ground, or snatched from someone). */
    public void takeIntoHands(ItemStack single) {
        this.setItemSlot(EquipmentSlot.MAINHAND, single);
        this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
        this.heldTicks = 0;
        this.carryLimit = this.carryTicksFor(single);
    }

    /** How long a non-food item is carried before it gets dropped (crows override to stash it on purpose). */
    protected int carryTicksFor(ItemStack stack) {
        return 1200 + this.random.nextInt(1200);
    }

    /** Drops whatever she holds at her feet; the item lasts longer on the ground than a normal drop. */
    public void dropHeldItem() {
        ItemStack held = this.getMainHandItem();
        if (held.isEmpty()) return;
        ItemEntity item = this.spawnAtLocation(held.copy());
        if (item != null) item.setExtendedLifetime();
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.pickupCooldown = 1200;
    }

    protected void onAte(ItemStack food) {
    }

    /** Hook for species that do something deliberate with a carried item once it has been held long enough. */
    protected void onCarriedLongEnough() {
        this.dropHeldItem();
    }

    private void tickHeldItem(ServerLevel level) {
        if (this.pickupCooldown > 0) this.pickupCooldown--;
        ItemStack held = this.getMainHandItem();
        if (held.isEmpty()) return;
        this.heldTicks++;
        if (this.isEdible(held)) {
            if (this.heldTicks % 8 == 0) {
                level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, held), this.getX(), this.getEyeY() - 0.2, this.getZ(),
                        3, 0.1, 0.05, 0.1, 0.05);
            }
            if (this.heldTicks >= EAT_TICKS) {
                this.playSound(this.getEatSound(), 1.0F, this.getVoicePitch());
                this.heal(2.0F);
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                this.onAte(held);
            }
        } else if (this.heldTicks >= this.carryLimit) {
            this.onCarriedLongEnough();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        // Startled: lets go of whatever she was carrying.
        if (hurt && !this.level().isClientSide && !this.isEdible(this.getMainHandItem())) this.dropHeldItem();
        return hurt;
    }

    // --- flight -------------------------------------------------------------------------------------------------

    /** Lets paths lead through closed wooden doors; the species then needs a goal that actually opens them. */
    protected boolean opensDoors() {
        return false;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        if (!this.canFly()) {
            PathNavigation ground = super.createNavigation(level);
            ground.setCanFloat(true);
            return ground;
        }
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(this.opensDoors());
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    // --- young, eggs, bed ---------------------------------------------------------------------------------------------

    @Override
    public boolean isBaby() {
        return this.entityData.get(DATA_BABY);
    }

    public void setBaby(boolean baby) {
        this.entityData.set(DATA_BABY, baby);
        this.growTicks = 0;
    }

    public void setGrowTicks(int ticks) {
        this.growTicks = ticks;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_BABY.equals(key)) this.refreshDimensions();
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        EntityDimensions dimensions = super.getDefaultDimensions(pose);
        return this.isBaby() ? dimensions.scale(BABY_SCALE) : dimensions;
    }

    @Override
    public Gestation getGestation() {
        return this.gestation;
    }

    @Override
    public boolean canShareBed() {
        return !this.isBaby() && this.gestation.canConceive();
    }

    /** How round her belly is, 0 (not expecting) to 1 (due); synced to the client. */
    public float getBellySwell() {
        return this.entityData.get(DATA_BELLY) / 100.0F;
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

    @Override
    public void bornTo(@Nullable UUID father) {
        Bonded.super.bornTo(father);
        if (father != null) {
            this.setPersistenceRequired();
            this.onBornTo(father);
        }
    }

    /** Species with their own kind of bond (a crow's owner, a phoenix's imprint, an ostrich's loyalty) set it here. */
    protected void onBornTo(UUID father) {
    }

    @Override
    public void settleInBed() {
        this.setAction(Action.LYING);
    }

    @Override
    public void leaveBed() {
        if (this.getAction() == Action.LYING) this.setAction(Action.NONE);
    }

    // --- sitting on command --------------------------------------------------------------------------------------

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

    /** A stick: sit down, or get up again. */
    private void toggleSitting(Player player) {
        this.setOrderedToSit(!this.orderedToSit);
        player.displayClientMessage(Component.translatable(this.orderedToSit ? "message.monstergirls.sits" : "message.monstergirls.stands",
                this.getDisplayName()), true);
    }

    // --- gifts -----------------------------------------------------------------------------------------------------

    /** A present for a favourite player, or empty if her species doesn't bring any (see {@link GiftGoal}). */
    public ItemStack rollGift(Player player) {
        return ItemStack.EMPTY;
    }

    /** Whether she drops her presents into a chest that holds the same thing, rather than handing them over. */
    public boolean storesGiftsAway() {
        return false;
    }

    public boolean isGiftDue() {
        if (this.nextGiftTime < 0) this.scheduleNextGift();   // the first one comes a while after she grows fond
        return this.level().getGameTime() >= this.nextGiftTime;
    }

    public void scheduleNextGift() {
        this.nextGiftTime = this.level().getGameTime() + GIFT_INTERVAL + this.random.nextInt(GIFT_INTERVAL);
    }

    public void setNextGiftTime(long time) {
        this.nextGiftTime = time;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel) {
            this.gestation.tick();
            if (this.isBaby() && ++this.growTicks >= GROW_TICKS) this.setBaby(false);
            if (this.tickCount % 10 == 0) this.entityData.set(DATA_BELLY, (byte) Math.round(this.gestation.progress() * 100));
            // the sitting pose comes back after anything else (a song, a caw...) took over for a moment
            if (this.orderedToSit && this.getAction() == Action.NONE) this.setAction(Action.SITTING);
        }
        // Flutter: glide down instead of dropping like a stone.
        Vec3 motion = this.getDeltaMovement();
        if (this.canFly() && !this.onGround() && motion.y < 0.0) {
            this.setDeltaMovement(motion.multiply(1.0, 0.6, 1.0));
        }
        if (this.level() instanceof ServerLevel server) {
            this.tickHeldItem(server);
            if (this.timedActionTicks > 0 && --this.timedActionTicks == 0) this.setAction(Action.NONE);
        }
    }

    @Override
    public boolean isFlying() {
        return this.canFly() && !this.onGround();
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return !this.canFly() && super.causeFallDamage(fallDistance, multiplier, source);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        if (!this.canFly()) super.checkFallDamage(y, onGround, state, pos);
    }

    /**
     * Wild harpies come and go like other wildlife (the field guide: only bonded or notable ones need to persist).
     * Anyone who has won some of her affection keeps her around; named, leashed or companion harpies are kept
     * by vanilla's persistence rules. Species add their own reasons to stay with {@link #isSettled()}.
     */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.hasAnyAffection() && !this.isSettled() && !this.orderedToSit && !this.gestation.isCarrying();
    }

    /** Whether she has made a home somewhere a player gave her (and so shouldn't despawn). */
    protected boolean isSettled() {
        return false;
    }

    // --- animation ----------------------------------------------------------------------------------------------

    protected RawAnimation anim(String name) {
        return ANIMATIONS.computeIfAbsent("animation." + this.modelName() + "." + name, n -> RawAnimation.begin().thenLoop(n));
    }

    /** What the model, animations and texture are called: the species, or species_hatchling while she is small. */
    public String modelName() {
        return this.isBaby() ? this.species() + "_hatchling" : this.species();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, this::animate));
    }

    private PlayState animate(AnimationState<AbstractHarpy> state) {
        boolean flying = this.isAirborne();
        if (this.isBaby()) {   // a hatchling only knows how to stand, toddle and flutter
            return state.setAndContinue(this.anim(flying ? "fly" : state.isMoving() ? "walk" : "idle"));
        }
        // Once airborne the wings take over; actions (caw, sing, knock...) only show on the ground or perched.
        if (flying && !this.actionShowsInFlight(this.getAction())) return state.setAndContinue(this.anim(this.flyingAnimation()));
        // every adult harpy model has these poses (tools/add_harpy_extras.py)
        switch (this.getAction()) {
            case SITTING, NESTING -> {
                return state.setAndContinue(this.anim("sit"));
            }
            case LYING -> {
                return state.setAndContinue(this.anim("lie"));
            }
            default -> {
            }
        }
        String action = this.actionAnimation(this.getAction());
        if (action != null) return state.setAndContinue(this.anim(action));
        return state.setAndContinue(this.anim(state.isMoving() ? this.movingAnimation() : "idle"));
    }

    /**
     * In the air, judged by probing for ground just under her feet. The onGround flag a client receives lags
     * behind (and flickers on short hops), which used to leave her walking on thin air for a moment.
     */
    public boolean isAirborne() {
        if (!this.canFly() || this.isInWater() || this.getVehicle() != null) return false;
        return this.level().noCollision(this, this.getBoundingBox().expandTowards(0.0, -0.3, 0.0));
    }

    /** Animation while airborne ("fly"; the albatross glides with "soar"). */
    protected String flyingAnimation() {
        return "fly";
    }

    /** Actions whose animation also plays in the air (bathing, an owl's pounce...); others give way to flight. */
    protected boolean actionShowsInFlight(Action action) {
        return action == Action.BATHING;
    }

    /** Animation while moving on the ground ("walk"; the ostrich switches to "run" at speed). */
    protected String movingAnimation() {
        return "walk";
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
