package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.entity.ai.BuildNestGoal;
import com.linkyoshi90.harmontide.entity.ai.GiftGoal;
import com.linkyoshi90.harmontide.entity.ai.ShareBedGoal;
import com.linkyoshi90.harmontide.entity.ai.SitWhenOrderedGoal;
import com.linkyoshi90.harmontide.registry.ModTags;
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
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * What every harpy shares: parrot-style walking/fluttering flight (unless the species is flightless), a synced
 * "what am I doing" action that drives the GeckoLib animation (set by goals, or for a fixed time), a remembered
 * home, bath cooldown, carrying one small item in her hands (eating it if it's food, otherwise dropping it
 * somewhere later), and her affection for each player (raised by feeding and petting; see /harmontide bonds).
 */
public abstract class AbstractHarpy extends PathfinderMob implements GeoEntity, FlyingAnimal, Bonded, GiftGiver, Painted, WeddingVeil, Voices.Voiced {
    private static final EntityDataAccessor<Boolean> DATA_BLUSHING = SynchedEntityData.defineId(AbstractHarpy.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_BARE = SynchedEntityData.defineId(AbstractHarpy.class, EntityDataSerializers.BOOLEAN);
    private int blushTicks, bareTicks;
    private static final EntityDataAccessor<Byte> DATA_ACTION =
            SynchedEntityData.defineId(AbstractHarpy.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<String> DATA_FIDGET = SynchedEntityData.defineId(AbstractHarpy.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_BABY =
            SynchedEntityData.defineId(AbstractHarpy.class, EntityDataSerializers.BOOLEAN);
    /** How far along her pregnancy is, 0-100, for the renderer to round out her belly. */
    private static final EntityDataAccessor<Byte> DATA_BELLY =
            SynchedEntityData.defineId(AbstractHarpy.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DATA_MOONSTRUCK =
            SynchedEntityData.defineId(AbstractHarpy.class, EntityDataSerializers.BOOLEAN);
    /** Her wedding look (WeddingVeil), on the day and on anniversaries. */
    private static final EntityDataAccessor<Boolean> DATA_VEILED = SynchedEntityData.defineId(AbstractHarpy.class, EntityDataSerializers.BOOLEAN);
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

    public enum Action { NONE, SINGING, BATHING, CAWING, KNOCKING, PRANKING, DISPLAYING, KICKING, BROODING, HOOTING, SLEEPING, POUNCING, BEGGING, DANCING, STRIKING, CRYING, CACHING, SITTING, LYING, NESTING, DRINKING, CLACKING, PERCHING, FIDGETING, KNEEL, LAYING, FEED, ORAL }

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
    private final com.linkyoshi90.harmontide.entity.Orders orders = new com.linkyoshi90.harmontide.entity.Orders();
    private long nextGiftTime = -1;

    protected AbstractHarpy(EntityType<? extends AbstractHarpy> type, Level level) {
        super(type, level);
        if (this.canFly()) this.moveControl = new FlyingMoveControl(this, 10, false);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.COCOA, -1.0F);
        this.setCanPickUpLoot(true);
        this.eggTime = EGG_INTERVAL + this.random.nextInt(EGG_INTERVAL);
        this.goalSelector.addGoal(-1, new SitWhenOrderedGoal<>(this));
        this.goalSelector.addGoal(1, new com.linkyoshi90.harmontide.entity.ai.StayNearGoal<>(this));
        this.goalSelector.addGoal(3, new com.linkyoshi90.harmontide.entity.girl.FarmhandGoal<>(this));
        this.goalSelector.addGoal(5, new com.linkyoshi90.harmontide.entity.girl.Chores.ChoresGoal<>(this));
        this.targetSelector.addGoal(1, new com.linkyoshi90.harmontide.entity.ai.StanceTargetGoal<>(this));
        this.goalSelector.addGoal(2, new BuildNestGoal(this));
        this.goalSelector.addGoal(2, new LooseEggGoal());
        this.goalSelector.addGoal(4, new com.linkyoshi90.harmontide.entity.ai.BreastfeedGoal(this));
        this.goalSelector.addGoal(2, new com.linkyoshi90.harmontide.entity.ai.FetchBallGoal<>(this));
        this.goalSelector.addGoal(3, new com.linkyoshi90.harmontide.entity.ai.SeekMilkingMachineGoal<>(this));
        this.goalSelector.addGoal(4, new com.linkyoshi90.harmontide.entity.ai.FridgeGoal<>(this));
        this.goalSelector.addGoal(3, new ShareBedGoal<>(this));
        this.goalSelector.addGoal(3, new GiftGoal<>(this));
        if (this.floatsOnWater()) {                                        // asked from the constructor: species constants only
            this.setPathfindingMalus(PathType.WATER, 0.0F);
            this.setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
        }
        if (this.towsSwimmers()) this.goalSelector.addGoal(6, new com.linkyoshi90.harmontide.entity.ai.PaddleGoal(this));
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

    /** Back to her species' default colours. */
    public void plainVariant() {
    }

    /** Animation name for an action, or null to fall back to idle/walk/fly. */
    @Nullable
    protected abstract String actionAnimation(Action action);

    /** Munching, when she eats something. */
    protected abstract SoundEvent getEatSound();

    /** Her own voice (entity/Voices): one of her slots' vanilla sounds is played as her species' event. */
    @Override
    public void playSound(SoundEvent sound, float volume, float pitch) {
        super.playSound(Voices.voice(this, sound), volume, pitch);
    }

    @Override
    @javax.annotation.Nullable
    public SoundEvent slotSound(String slot) {
        return switch (slot) {
            case "ambient" -> this.getAmbientSound();
            case "hurt" -> this.getHurtSound(this.damageSources().generic());
            case "death" -> this.getDeathSound();
            default -> this.getEatSound();
        };
    }

    /** A short call; used when begging, snatching and the like. Her normal ambient sound by default. */
    public SoundEvent getChirpSound() {
        return this.getAmbientSound();
    }

    /** One line for /harmontide bonds: how she feels about this player (e.g. "trusts you (7)"). */
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
        builder.define(DATA_FIDGET, "");
        builder.define(DATA_BLUSHING, false);
        builder.define(DATA_BARE, false);
        builder.define(DATA_BABY, false);
        builder.define(DATA_BELLY, (byte) 0);
        builder.define(DATA_MOONSTRUCK, false);
        builder.define(DATA_VEILED, false);
        builder.define(DATA_PAINT, 0);
        builder.define(DATA_CARRY, CARRY_NONE);
        builder.define(DATA_LIFT, 0);
    }

    public Action getAction() {
        return Action.values()[this.entityData.get(DATA_ACTION)];
    }

    public void setAction(Action action) {
        this.entityData.set(DATA_ACTION, (byte) action.ordinal());
    }

    /** The fidget clip she is playing (entity/girl/Fidgets). */
    public String getFidget() {
        return this.entityData.get(DATA_FIDGET);
    }

    /** Starts a fidget clip for so many ticks (only from standing idle). True if it started. */
    public boolean fidget(String name, int ticks) {
        if (this.getAction() != Action.NONE) return false;
        this.entityData.set(DATA_FIDGET, name);
        this.startTimedAction(Action.FIDGETING, ticks);
        return this.getAction() == Action.FIDGETING;
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
        int before = this.getAffection(player);
        this.affection.merge(player.getUUID(), amount, Integer::sum);
        Milestones.after(this, player, before, this.getAffection(player));
    }

    public void setAffection(Player player, int amount) {
        this.affection.put(player.getUUID(), amount);
    }

    @Override
    public void setAffection(UUID player, int amount) {
        this.affection.put(player, amount);
    }

    /** Whether any player at all has won some of her affection. */
    /** The highest affection she has for anyone. */
    public int bestAffection() {
        return this.affection.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    public boolean hasAnyAffection() {
        return this.affection.values().stream().anyMatch(a -> a > 0);
    }

    public void forgetAffection(Player player) {
        this.affection.remove(player.getUUID());
    }

    /** Empty main hand, not sneaking: a pat on the head. Species add their own reaction in {@link #onPetted}. */
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult paint = Painted.interact(this, this, player, hand);
        if (paint != null) return paint;
        if (player.getItemInHand(hand).is(Items.BUCKET) && com.linkyoshi90.harmontide.entity.HandMilking.interested(this, player)) {
            if (this.level() instanceof ServerLevel server && player instanceof net.minecraft.server.level.ServerPlayer sp) {
                com.linkyoshi90.harmontide.entity.HandMilking.tryBegin(server, this, sp, hand);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (hand == InteractionHand.MAIN_HAND && player.getMainHandItem().is(Items.STICK)) {
            if (!this.level().isClientSide) Orders.onStick(this, this.orders, player);
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
        if (hand == InteractionHand.MAIN_HAND && player.getMainHandItem().isEmpty() && !this.isBaby()) {
            InteractionResult lift = this.liftInteract(player);                 // a tow through the water, a lift through the air
            if (lift != null) return lift;
        }
        if (hand == InteractionHand.MAIN_HAND && player.getMainHandItem().isEmpty() && this.isBaby() && player.isShiftKeyDown() && Traversal.on()
                && this.getAffection(player) >= Orders.AFFECTION && !player.isVehicle() && !this.isPassenger()) {   // a hatchling up onto the shoulder
            if (!this.level().isClientSide && this.startRiding(player, true)) Traversal.award(player, "shoulder_friend");
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (hand == InteractionHand.MAIN_HAND && player.getMainHandItem().isEmpty() && this.level() instanceof ServerLevel rescue && MilkRescue.accept(rescue, this, player)) {
            return InteractionResult.SUCCESS;                           // a cup of her milk, offered when they were poisoned
        }
        if (hand == InteractionHand.MAIN_HAND && player.getMainHandItem().isEmpty() && !player.isShiftKeyDown()
                && com.linkyoshi90.harmontide.entity.Breastfeed.clickWouldAsk(this, player)) {
            if (!(this.level() instanceof ServerLevel) || player instanceof net.minecraft.server.level.ServerPlayer sp
                    && com.linkyoshi90.harmontide.entity.Breastfeed.ask(this, sp)) {
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
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
            Flirting.onPet(server, this, player);
            Lactation.onPet(this, player);
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
        if (this.isVeiled()) tag.putBoolean("Veiled", true);                   // kept, so her wedding figurine wears it
        tag.putBoolean("Waxed", this.waxed);
        if (this.homePos != null) tag.putLong("Home", this.homePos.asLong());
        CompoundTag affectionTag = new CompoundTag();
        this.affection.forEach((uuid, a) -> affectionTag.putInt(uuid.toString(), a));
        tag.put("Affection", affectionTag);
        tag.putBoolean("Baby", this.isBaby());
        tag.putInt("GrowTicks", this.growTicks);
        this.gestation.save(tag);
        if (this.father != null) tag.putUUID("Father", this.father);
        tag.putBoolean("OrderedToSit", this.orderedToSit);
        if (this.getPaint() > 0) tag.putString("Paint", this.getPaintName());
        tag.put("Orders", this.orders.save());
        tag.putLong("NextGift", this.nextGiftTime);
        if (this.roost != null) tag.putLong("Roost", this.roost.asLong());
        tag.putInt("EggTime", this.eggTime);
        tag.putInt("RoostDropTime", this.roostDropTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setVeiled(tag.getBoolean("Veiled"));
        Injury.reapply(this);
        this.waxed = tag.getBoolean("Waxed");
        this.homePos = tag.contains("Home") ? BlockPos.of(tag.getLong("Home")) : null;
        if (tag.contains("Variant")) this.variantFromData = true;
        this.entityData.set(DATA_BABY, tag.getBoolean("Baby"));
        this.growTicks = tag.getInt("GrowTicks");
        this.gestation.load(tag);
        this.father = tag.hasUUID("Father") ? tag.getUUID("Father") : null;
        this.orderedToSit = tag.getBoolean("OrderedToSit");
        if (tag.contains("Paint")) this.setPaint(com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl.PAINTS.indexOf(tag.getString("Paint")) + 1);
        this.orders.load(tag.getCompound("Orders"));
        this.nextGiftTime = tag.contains("NextGift") ? tag.getLong("NextGift") : -1;
        this.affection.clear();
        CompoundTag affectionTag = tag.getCompound("Affection");
        for (String key : affectionTag.getAllKeys()) this.affection.put(UUID.fromString(key), affectionTag.getInt(key));
        this.roost = tag.contains("Roost") ? BlockPos.of(tag.getLong("Roost")) : null;
        if (tag.contains("EggTime")) this.eggTime = tag.getInt("EggTime");
        if (tag.contains("RoostDropTime")) this.roostDropTime = tag.getInt("RoostDropTime");
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
        if (itemEntity.getOwner() instanceof Player && this.isEdible(single)) Dairy.ate(this, single);   // thrown to her: as good as from the hand
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

    private boolean livened;

    /**
     * A species that set up no wandering of its own gets the usual: she panics when hurt, strolls (or flies) about, looks at
     * people and around her. Checked once, on her first tick, when every constructor has added its goals.
     */
    private void liven() {
        this.livened = true;
        for (var g : this.goalSelector.getAvailableGoals()) if (g.getGoal() instanceof net.minecraft.world.entity.ai.goal.RandomStrollGoal) return;
        this.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.PanicGoal(this, 1.25));
        this.goalSelector.addGoal(8, this.canFly() ? new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal(this, 1.0)
                : new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, 0.9));
        this.goalSelector.addGoal(9, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
    }

    // --- getting about together (entity/Traversal): a lift, a catch, a tow --------------------------------------------

    /** What she is doing with the player hanging on: nothing, flying with them under her, or towing them through the water. */
    public static final byte CARRY_NONE = 0, CARRY_FLIGHT = 1, CARRY_TOW = 2;
    private static final EntityDataAccessor<Byte> DATA_CARRY = SynchedEntityData.defineId(AbstractHarpy.class, EntityDataSerializers.BYTE);
    /** Flight left in her wings while she carries someone (ticks; at 0 she can only sink). */
    private static final EntityDataAccessor<Integer> DATA_LIFT = SynchedEntityData.defineId(AbstractHarpy.class, EntityDataSerializers.INT);
    private int carryGrace;
    private long nextCarry;

    /** How well she carries a grown person: not at all (the tiny), a glide down (most), or real flight (the big birds). */
    public enum Lift { NONE, GLIDE, STRONG }

    public Lift lift() {
        if (!this.canFly() || this.isBaby()) return Lift.NONE;
        return Traversal.liftOf(this.species());
    }

    public byte carryMode() {
        return this.entityData.get(DATA_CARRY);
    }

    public int liftLeft() {
        return this.entityData.get(DATA_LIFT);
    }

    /** Ducks and swans paddle on the water and tow a swimming friend. */
    protected boolean towsSwimmers() {
        return false;
    }

    /** Water birds sit on the water instead of sinking: ducks, swans, gulls, pelicans, albatrosses. */
    protected boolean floatsOnWater() {
        return this.towsSwimmers();
    }

    /** Afloat she bobs up to the surface and stays there, riding a little low. */
    protected void bob() {
        if (!this.isInWater()) return;
        double depth = this.getFluidHeight(net.minecraft.tags.FluidTags.WATER);
        Vec3 v = this.getDeltaMovement();
        if (depth > 0.35) this.setDeltaMovement(v.x * 0.9, Math.min(v.y + 0.06, 0.12), v.z * 0.9);
        else if (v.y < 0) this.setDeltaMovement(v.x, v.y * 0.3, v.z);
    }

    /** A hatchling rides her friend's shoulder like a parrot: a crouching empty hand lifts her up (entity/Traversal). */
    @Override
    public Vec3 getVehicleAttachmentPoint(net.minecraft.world.entity.Entity vehicle) {
        if (vehicle instanceof Player p) return Traversal.shoulderSeat(p);
        return super.getVehicleAttachmentPoint(vehicle);
    }

    private int perchCrouch;

    @Override
    public void rideTick() {
        super.rideTick();
        if (!(this.getVehicle() instanceof Player p)) {
            this.perchCrouch = 0;
            return;
        }
        this.perchCrouch = p.isShiftKeyDown() ? this.perchCrouch + 1 : 0;
        this.yBodyRot = p.yBodyRot;
        this.setYRot(p.yBodyRot);
        if (!this.level().isClientSide && (this.perchCrouch > Traversal.CROUCH_OFF || !this.isBaby() || p.isSleeping() || !p.isAlive())) this.stopRiding();
    }

    /** An empty hand: in the water a tow (water birds, friend), crouching a lift into the air (bonded). Null to carry on. */
    @Nullable
    private InteractionResult liftInteract(Player player) {
        boolean client = this.level().isClientSide;
        if (!Traversal.on()) return null;
        if (!player.isShiftKeyDown() && this.towsSwimmers() && player.isInWater() && this.isInWater() && this.getAffection(player) >= Orders.AFFECTION) {
            if (!client) this.tow(player);
            return InteractionResult.sidedSuccess(client);
        }
        if (player.isShiftKeyDown() && this.lift() != Lift.NONE && this.getAffection(player) >= BED_AFFECTION) {
            if (!client && !this.pickUp(player, false)) {
                player.displayClientMessage(Component.translatable("message.harmontide.carry_rest", this.getDisplayName()), true);
            }
            return InteractionResult.sidedSuccess(client);
        }
        return null;
    }

    /** Whether she is free to take someone up (or catch them) now. */
    public boolean canCarry() {
        return Traversal.on() && this.lift() != Lift.NONE && !this.isVehicle() && !this.isPassenger() && this.isAlive() && this.level().getGameTime() >= this.nextCarry;
    }

    /**
     * Up she goes with the player hanging from her talons: steered by where they look (forward to fly, back to slow), for as
     * long as {@link Traversal#liftTicks} allows; then she sinks and sets them down. A catch picks up someone falling past her.
     */
    public boolean pickUp(Player p, boolean caught) {
        if (!this.canCarry() || p.isPassenger() || p.isSpectator() || p.isFallFlying()) return false;
        if (caught) {
            Vec3 over = p.position().add(0, Traversal.HANG + 0.1, 0);
            if (!this.level().noCollision(this, this.getBoundingBox().move(over.subtract(this.position())))) return false;
            this.setPos(over);
            if (this.level() instanceof ServerLevel server) server.sendParticles(ParticleTypes.CLOUD, over.x, over.y, over.z, 8, 0.4, 0.2, 0.4, 0.02);
        } else if (this.level().noCollision(this, this.getBoundingBox().move(0, Traversal.HANG, 0))) {
            this.setPos(this.getX(), this.getY() + Traversal.HANG, this.getZ());   // she rises a body's height first: their feet clear the ground
        }
        this.orderedToSit = false;
        this.getNavigation().stop();
        this.entityData.set(DATA_CARRY, CARRY_FLIGHT);
        this.entityData.set(DATA_LIFT, Traversal.liftTicks(this.lift(), this.getAffection(p)));
        this.carryGrace = 20;
        this.setDeltaMovement(Vec3.ZERO);
        p.resetFallDistance();
        if (!p.startRiding(this, true)) {
            this.entityData.set(DATA_CARRY, CARRY_NONE);
            return false;
        }
        this.playSound(SoundEvents.PHANTOM_FLAP, 1.0F, 1.2F);
        if (!caught) Traversal.award(p, "carried_aloft");
        return true;
    }

    /** A swimming friend takes hold of her and she paddles them along. */
    public boolean tow(Player p) {
        if (!Traversal.on() || !this.towsSwimmers() || this.isVehicle() || this.isPassenger() || p.isPassenger() || this.isBaby()) return false;
        this.orderedToSit = false;
        this.getNavigation().stop();
        this.entityData.set(DATA_CARRY, CARRY_TOW);
        if (!p.startRiding(this, true)) {
            this.entityData.set(DATA_CARRY, CARRY_NONE);
            return false;
        }
        Traversal.award(p, "ferried");
        return true;
    }

    /** She lets go: a flight ends in a slow, safe drop (and a rest for her wings). */
    public void dropCarried(@Nullable Player p) {
        byte was = this.carryMode();
        if (was == CARRY_NONE) return;
        this.entityData.set(DATA_CARRY, CARRY_NONE);
        this.entityData.set(DATA_LIFT, 0);
        this.nextCarry = this.level().getGameTime() + (was == CARRY_FLIGHT ? Traversal.CARRY_REST : 20);
        if (p == null) return;
        if (p.getVehicle() == this) p.stopRiding();
        p.resetFallDistance();
        if (was == CARRY_FLIGHT) p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING, 60, 0, true, false));
    }

    /** Server, every tick: wings tiring, set down once their feet touch ground or water, the tow over once she is ashore. */
    private void tickCarry(ServerLevel level) {
        byte mode = this.carryMode();
        if (mode == CARRY_NONE) return;
        if (!(this.getFirstPassenger() instanceof Player p)) {
            this.dropCarried(null);
            return;
        }
        if (mode == CARRY_FLIGHT) {
            int left = this.liftLeft();
            if (left > 0) {
                this.entityData.set(DATA_LIFT, left - 1);
                if (left == 61) p.displayClientMessage(Component.translatable("message.harmontide.carry_tiring", this.getDisplayName()), true);
            }
            p.resetFallDistance();
            if (this.carryGrace > 0) this.carryGrace--;
            else if (p.isInWater() || this.isInWater() || !level.noCollision(p, p.getBoundingBox().move(0, -0.2, 0))) this.dropCarried(p);
        } else if (!this.isInWater() && (this.onGround() || !p.isInWater())) {
            this.dropCarried(p);                                            // ashore
        }
    }

    @Override
    protected void removePassenger(net.minecraft.world.entity.Entity passenger) {
        super.removePassenger(passenger);
        if (!this.level().isClientSide && passenger instanceof Player p) this.dropCarried(p);   // they let go themselves (crouched)
    }

    @Nullable
    @Override
    public net.minecraft.world.entity.LivingEntity getControllingPassenger() {
        if (this.carryMode() != CARRY_NONE && this.getFirstPassenger() instanceof Player p) return p;
        return super.getControllingPassenger();
    }

    /** Carried: hanging from her talons, their shoulders at her feet. Towed: holding on behind her. */
    @Override
    protected Vec3 getPassengerAttachmentPoint(net.minecraft.world.entity.Entity passenger, EntityDimensions dims, float scale) {
        byte mode = this.carryMode();
        if (mode == CARRY_FLIGHT) return new Vec3(0, 0.6 - Traversal.HANG, 0);
        if (mode == CARRY_TOW) return new Vec3(0, -0.35, -(dims.width() * 0.5 + 0.45)).yRot(-this.yBodyRot * net.minecraft.util.Mth.DEG_TO_RAD);
        return super.getPassengerAttachmentPoint(passenger, dims, scale);
    }

    /** Someone hanging from her or holding on behind is not sitting. */
    @Override
    public boolean shouldRiderSit() {
        return this.carryMode() == CARRY_NONE && super.shouldRiderSit();
    }

    @Override
    public boolean canBeRiddenUnderFluidType(net.neoforged.neoforge.fluids.FluidType type, net.minecraft.world.entity.Entity rider) {
        return this.carryMode() == CARRY_TOW || super.canBeRiddenUnderFluidType(type, rider);
    }

    /** Steered by whoever she carries (their client moves her). */
    @Override
    public void travel(Vec3 input) {
        byte mode = this.carryMode();
        if (mode != CARRY_NONE && this.getControllingPassenger() instanceof Player p) {
            this.setYRot(p.getYRot());
            this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
            Vec3 v = mode == CARRY_FLIGHT ? Traversal.flightStroke(this.getDeltaMovement(), p, this.lift(), this.liftLeft())
                    : Traversal.towStroke(this.getDeltaMovement(), p, this.getYRot(), this.isInWater() ? this.getFluidHeight(net.minecraft.tags.FluidTags.WATER) : 0, this.towSpeed());
            if (mode == CARRY_TOW && this.horizontalCollision) v = v.add(0, 0.25, 0);   // up onto the bank
            this.setNoGravity(mode == CARRY_FLIGHT);
            this.setDeltaMovement(v);
            this.move(net.minecraft.world.entity.MoverType.SELF, v);
            return;
        }
        super.travel(input);
    }

    /** How fast she tows a swimmer (walk, sprint). */
    protected float towSpeed() {
        return 0.36F;
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

    @Override
    public int getGrowTicks() {
        return this.growTicks;
    }

    @Override
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
        return !this.isBaby() && this.getType().getHeight() >= CHILDLIKE_HEIGHT && this.gestation.canConceive();
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
    public boolean isVeiled() {
        return this.entityData.get(DATA_VEILED);
    }

    @Override
    public void setVeiled(boolean veiled) {
        this.entityData.set(DATA_VEILED, veiled);
    }

    @Override
    public boolean isMoonstruck() {
        return this.entityData.get(DATA_MOONSTRUCK);
    }

    @Override
    public void setMoonstruck(boolean moonstruck) {
        this.entityData.set(DATA_MOONSTRUCK, moonstruck);
    }

    @Override
    public void settleInBed() {
        this.setAction(Action.LYING);
    }

    @Override
    public void leaveBed() {
        if (this.getAction() == Action.LYING || this.getAction() == Action.KNEEL) this.setAction(Action.NONE);
    }

    // --- sitting on command --------------------------------------------------------------------------------------

    // --- stick orders --------------------------------------------------------------------------------------------

    @Override
    public com.linkyoshi90.harmontide.entity.Orders getOrders() {
        return this.orders;
    }

    /** Her stance has the last word on whom she fights (and companions never fight each other). */
    @Override
    public void setTarget(@Nullable net.minecraft.world.entity.LivingEntity target) {
        super.setTarget(Injury.isInjured(this) ? null : this.orders == null || this.orders.allows(this, target) ? target : null);
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
    public void blushFor(int ticks) {
        this.blushTicks = Math.max(this.blushTicks, ticks);
        this.entityData.set(DATA_BLUSHING, true);
    }

    public boolean isBlushing() {
        return this.entityData.get(DATA_BLUSHING);
    }

    /** Topless texture for a bucket milking (client/Topless). */
    public void bareFor(int ticks) {
        this.bareTicks = Math.max(this.bareTicks, ticks);
        this.entityData.set(DATA_BARE, true);
    }

    public void dressAfter(int ticks) {
        this.bareTicks = Math.max(0, ticks);
        this.entityData.set(DATA_BARE, ticks > 0);
    }

    public boolean isBare() {
        return this.entityData.get(DATA_BARE);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.blushTicks > 0 && --this.blushTicks == 0) this.entityData.set(DATA_BLUSHING, false);
        if (!this.level().isClientSide && this.bareTicks > 0 && --this.bareTicks == 0) this.entityData.set(DATA_BARE, false);
        if (this.level() instanceof ServerLevel hurt) Injury.tick(hurt, this);
        if (this.level() instanceof ServerLevel milky) com.linkyoshi90.harmontide.entity.Lactation.tick(milky, this);
        if (this.level() instanceof ServerLevel) {
            Painted.tickCopper(this, this);
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
        if (this.floatsOnWater() && this.isControlledByLocalInstance() && this.getControllingPassenger() == null) this.bob();   // afloat: she rides the surface
        if (this.level() instanceof ServerLevel server) {
            if (!this.livened) this.liven();
            if (this.tickCount % 40 == 13 && Instincts.OWLS.contains(this.species())) Instincts.owlEyes(server, this);   // her eyes for her friend at night
            this.tickCarry(server);
            this.tickHeldItem(server);
            if (this.timedActionTicks > 0 && --this.timedActionTicks == 0) this.setAction(Action.NONE);
            this.tickLaying(server);
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
        return !this.hasAnyAffection() && !this.isSettled() && !this.orderedToSit && !this.gestation.isCarrying() && this.roost == null;
    }

    /** Whether she has made a home somewhere a player gave her (and so shouldn't despawn). */
    protected boolean isSettled() {
        return false;
    }

    // --- laying and roosting ------------------------------------------------------------------------------------

    /** Like a hen: an unfertilised egg every five to ten minutes. */
    public static final int EGG_INTERVAL = 6000;
    /** A roosting sparrow or gull sheds a feather, a twig or a flint into her hopper every two and a half to five minutes. */
    public static final int ROOST_DROP_INTERVAL = 3000;
    /** How long a laid egg waits for her to get back to her roost before it is simply dropped. */
    private static final int EGG_HOLD = 600;
    /** One prone push, the same length as a fertilised egg joining the nest. No nest is built. */
    private static final int LOOSE_PUSH_TICKS = 14;

    @Nullable
    private BlockPos roost;
    private boolean roosting;
    private int eggTime = EGG_INTERVAL;
    private int roostDropTime = ROOST_DROP_INTERVAL;
    /** -1 idle. Otherwise ticks already spent prone over the loose egg. Not saved: a reload starts the push again. */
    private int loosePush = -1;

    /** Her unfertilised egg (empty: she does not lay any). */
    protected ItemStack unfertilisedEgg() {
        return ItemStack.EMPTY;
    }

    /** Whether she settles into the bowl of a hopper to roost (see {@link com.linkyoshi90.harmontide.entity.ai.RoostInHopperGoal}). */
    public boolean roostsInHoppers() {
        return false;
    }

    /** What she sheds into her roost now and then (feathers and nesting bits). */
    protected ItemStack roostDrop() {
        return ItemStack.EMPTY;
    }

    /** The hopper she has claimed as her roost, or null. */
    @Nullable
    public BlockPos getRoost() {
        return this.roost;
    }

    public void setRoost(@Nullable BlockPos roost) {
        this.roost = roost == null ? null : roost.immutable();
    }

    /** Settled on top of her roost right now. */
    public boolean isRoosting() {
        return this.roosting;
    }

    public void setRoosting(boolean roosting) {
        if (roosting && !this.roosting && this.getAction() == Action.NONE) this.setAction(Action.SITTING);
        if (!roosting && this.roosting && this.getAction() == Action.SITTING && !this.isOrderedToSit()) this.setAction(Action.NONE);
        this.roosting = roosting;
    }

    public void setEggTime(int ticks) {
        this.eggTime = ticks;
    }

    public void setRoostDropTime(int ticks) {
        this.roostDropTime = ticks;
    }

    /**
     * The hen's clock: every five to ten minutes a grown harpy lays an unfertilised egg, into her hopper if she roosts in
     * one (she waits a little to get back to it) or at her feet. She lies prone and pushes it, the same pose as a
     * fertilised lay, and builds no nest. A roosting sparrow or gull also sheds into her hopper, and that stays instant.
     */
    private void tickLaying(ServerLevel level) {
        if (this.isBaby()) {
            if (this.loosePush >= 0) this.endLoosePush();
            return;
        }
        ItemStack egg = this.unfertilisedEgg();
        if (egg.isEmpty()) {
            if (this.loosePush >= 0) this.endLoosePush();
        } else if (this.loosePush >= 0) {
            this.pushLooseEgg(level, egg);
        } else if (--this.eggTime <= 0) {
            boolean waiting = this.roost != null && !this.roosting && this.eggTime > -EGG_HOLD;
            if (!waiting) {
                if (this.fertilisedLayBusy()) this.eggTime = 0;   // keep the egg until the clutch is finished
                else {
                    this.loosePush = 0;
                    this.pushLooseEgg(level, egg);
                }
            }
        }
        if (this.roosting && this.roost != null) {
            if (--this.roostDropTime <= 0) {
                this.roostDropTime = this.nextRoostDrop(level);
                ItemStack drop = this.roostDrop();
                if (!drop.isEmpty()) this.deliver(level, drop);
            }
        }
    }

    /** A fertilised clutch already owns the laying pose, or she is still carrying it. */
    private boolean fertilisedLayBusy() {
        Action action = this.getAction();
        return action == Action.NESTING || action == Action.LAYING || this.gestation.isCarrying();
    }

    /** Prone, held still, then the same loose egg she has always laid. */
    private void pushLooseEgg(ServerLevel level, ItemStack egg) {
        this.setAction(Action.LAYING);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.plantForLooseEgg();
        if (++this.loosePush < LOOSE_PUSH_TICKS) return;
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, egg.copy()),
                this.getX(), this.getY() + 0.3, this.getZ(), 3, 0.12, 0.04, 0.12, 0.02);
        this.deliver(level, egg);
        level.playSound(null, this.blockPosition(), SoundEvents.CHICKEN_EGG, SoundSource.NEUTRAL, 0.8F, 1.3F);
        this.eggTime = EGG_INTERVAL + this.random.nextInt(EGG_INTERVAL);
        this.endLoosePush();
    }

    private void endLoosePush() {
        this.loosePush = -1;
        if (this.getAction() == Action.LAYING) {
            this.setAction(this.roosting || this.orderedToSit ? Action.SITTING : Action.NONE);
        }
    }

    /** Onto the block under her, so a push caught in the air is still on her stomach. A roost is left where it is. */
    private void plantForLooseEgg() {
        if (this.roosting || this.onGround()) return;
        Level level = this.level();
        int x = this.getBlockX();
        int z = this.getBlockZ();
        int min = level.getMinBuildHeight();
        for (int y = this.getBlockY(); y > min && this.getBlockY() - y < 16; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                this.moveTo(this.getX(), y + 1.0, this.getZ(), this.getYRot(), 0.0F);
                this.setDeltaMovement(Vec3.ZERO);
                this.fallDistance = 0.0F;
                return;
            }
        }
    }

    /**
     * Stops her wandering for the loose-egg push. Registered beside the nest goal, so a fertilised clutch
     * (added first, same priority) is never pushed aside. Stays out of the way while she is already roosting.
     */
    private final class LooseEggGoal extends Goal {
        private LooseEggGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return AbstractHarpy.this.loosePush >= 0 && !AbstractHarpy.this.roosting;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            AbstractHarpy.this.getNavigation().stop();
            AbstractHarpy.this.setDeltaMovement(Vec3.ZERO);
            AbstractHarpy.this.plantForLooseEgg();
        }
    }

    /** Within this of her hopper, a fletching table has her shed half as often again (an arrow farm's bonus, 2026-11-10). */
    public static final int FLETCHING_REACH = 4;

    /** Ticks until she next sheds into her hopper: five to ten minutes, two thirds of that with a fletching table near the hopper. */
    public int nextRoostDrop(ServerLevel level) {
        int t = ROOST_DROP_INTERVAL + this.random.nextInt(ROOST_DROP_INTERVAL);
        return this.roost != null && fletchingNear(level, this.roost) ? t * 2 / 3 : t;
    }

    public static boolean fletchingNear(net.minecraft.world.level.Level level, BlockPos at) {
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-FLETCHING_REACH, -2, -FLETCHING_REACH), at.offset(FLETCHING_REACH, 2, FLETCHING_REACH))) {
            if (level.getBlockState(p).is(net.minecraft.world.level.block.Blocks.FLETCHING_TABLE)) return true;
        }
        return false;
    }

    /** Into her hopper while she roosts on it, otherwise on the ground at her feet. */
    private void deliver(ServerLevel level, ItemStack stack) {
        if (this.roosting && this.roost != null && level.getBlockEntity(this.roost) instanceof HopperBlockEntity hopper) {
            stack = HopperBlockEntity.addItem(null, hopper, stack, Direction.UP);
        }
        if (!stack.isEmpty()) this.spawnAtLocation(stack);
    }

    // --- animation ----------------------------------------------------------------------------------------------

    protected RawAnimation anim(String name) {
        return ANIMATIONS.computeIfAbsent("animation." + this.modelName() + "." + name, n -> RawAnimation.begin().thenLoop(n));
    }

    private RawAnimation animOnce(String name) {
        return ANIMATIONS.computeIfAbsent("once:animation." + this.modelName() + "." + name, n -> RawAnimation.begin().thenPlayAndHold(n.substring("once:".length())));
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
        if (this.getAction() == Action.KNEEL || this.getAction() == Action.LAYING || this.getAction() == Action.FEED
                || this.getAction() == Action.ORAL) return state.setAndContinue(this.anim("idle"));
        // Once airborne the wings take over; actions (caw, sing, knock...) only show on the ground or perched.
        if (flying && !this.actionShowsInFlight(this.getAction())) return state.setAndContinue(this.anim(this.flyingAnimation()));
        if (this.getAction() == Action.FIDGETING && !this.getFidget().isEmpty() && !state.isMoving() && !flying) {
            return state.setAndContinue(this.animOnce("fidget_" + this.getFidget()));   // an idle fidget (entity/girl/Fidgets)
        }
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
        if (this.floatsOnWater() && this.isInWater()) return state.setAndContinue(this.anim("sit"));   // afloat, legs folded under her
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

    // --- Harmontide paints (as for the girls, see AbstractMonsterGirl.PAINTS) --------------------------------------

    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> DATA_PAINT =
            net.minecraft.network.syncher.SynchedEntityData.defineId(AbstractHarpy.class, net.minecraft.network.syncher.EntityDataSerializers.INT);

    /** 0 unpainted, else 1 + an index into AbstractMonsterGirl.PAINTS. */
    public int getPaint() {
        return this.entityData.get(DATA_PAINT);
    }

    public void setPaint(int paint) {
        this.entityData.set(DATA_PAINT, (paint < 0 || paint > com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl.PAINTS.size() ? 0 : paint));
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
        return p == 0 ? null : com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl.PAINTS.get(p - 1);
    }

    /** One wild one in 64 by default (never a hatchling: she takes her mother's) is painted. */
    protected void rollPaint(MobSpawnType spawnType) {
        if (this.getPaint() == 0 && spawnType != MobSpawnType.BREEDING && !this.hasVariantFromData()
                && com.linkyoshi90.harmontide.HarmontideConfig.rollsPaint(this.random)) {
            this.setPaint(Painted.roll(this.random));
        }
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty,
                                                                   MobSpawnType spawnType, @Nullable net.minecraft.world.entity.SpawnGroupData group) {
        this.rollPaint(spawnType);
        Injury.roll(this, spawnType);
        if (!this.hasVariantFromData() && spawnType != MobSpawnType.BREEDING && !com.linkyoshi90.harmontide.HarmontideConfig.keepsBiomeVariant(this.random)) this.plainVariant();
        return super.finalizeSpawn(level, difficulty, spawnType, group);
    }
}
