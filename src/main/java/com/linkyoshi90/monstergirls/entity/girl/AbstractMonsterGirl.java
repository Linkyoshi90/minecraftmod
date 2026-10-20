package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.Gestation;
import com.linkyoshi90.monstergirls.entity.GiftGiver;
import com.linkyoshi90.monstergirls.entity.ai.GiftGoal;
import com.linkyoshi90.monstergirls.entity.ai.ShareBedGoal;
import com.linkyoshi90.monstergirls.entity.ai.SitWhenOrderedGoal;
import com.linkyoshi90.monstergirls.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
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
 * What every monster girl of the families after the harpies and anurans shares (the field guides, volumes III to
 * IX): her species ({@link GirlSpecies}), affection per player with the mod-wide tiers, feeding and petting, sitting
 * on command, sharing a bed, a day-long pregnancy with a rounding belly, young that grow up in five days and are
 * devoted to their father, colour variants, presents, and a "true form" that awakens after ten days of full
 * devotion (the nekomata's second tail, the kitsune's nine tails, the dragon koi). Each family class adds its own
 * bond action, signature behaviours and way of bearing young.
 */
public abstract class AbstractMonsterGirl extends PathfinderMob implements GeoEntity, Bonded, GiftGiver {
    private static final EntityDataAccessor<Byte> DATA_ACTION = SynchedEntityData.defineId(AbstractMonsterGirl.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DATA_YOUNG = SynchedEntityData.defineId(AbstractMonsterGirl.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(AbstractMonsterGirl.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_BELLY = SynchedEntityData.defineId(AbstractMonsterGirl.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_FLAGS = SynchedEntityData.defineId(AbstractMonsterGirl.class, EntityDataSerializers.BYTE);
    private static final Map<String, RawAnimation> ANIMATIONS = new ConcurrentHashMap<>();

    /** Synced flags (bits of DATA_FLAGS). */
    public static final int FLAG_TRUE_FORM = 1, FLAG_HIDDEN = 2, FLAG_DECOY = 4, FLAG_HALF = 8, FLAG_CARRYING = 16, FLAG_SADDLED = 32, FLAG_CHEST = 64;

    /** Five in-game days from newborn to grown up. */
    public static final int GROW_TICKS = 5 * 24000;
    public static final float YOUNG_SCALE = 0.45F;
    /** Days at full devotion before her true form awakens. */
    public static final long TRUE_FORM_TICKS = 10L * 24000L;
    private static final int GIFT_INTERVAL = 6000;
    private static final int FOOD_COOLDOWN = 600;
    private static final int PET_COOLDOWN = 1200;

    /** What she is doing; every family's animation file has the animations its actions name. */
    public enum Action {
        NONE(null, false), SITTING("sit", false), LYING("lie", false), POUNCE("pounce", true), GROOM("groom", false), HISS("hiss", false),
        DIG("dig", false), LAUGH("laugh", false), REAR("rear", false), COIL("coil", false), BASK("bask", false), LEAP("leap", true),
        SPIN("spin", false), KICK("kick", true), GRAZE("graze", false), SING("sing", false), ABSORB("absorb", true), SPLIT("split", true);

        public final String animation;
        public final boolean once;

        Action(String animation, boolean once) {
            this.animation = animation;
            this.once = once;
        }
    }

    protected final GirlSpecies species;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final Map<UUID, Integer> affection = new HashMap<>();
    private final Gestation gestation = new Gestation();
    @Nullable
    private UUID father;
    private boolean orderedToSit;
    private long nextGiftTime = -1;
    private int growTicks;
    private int timedActionTicks;
    private long nextPetTime;
    private long nextFoodTime;
    private long devotedSince = NOT_DEVOTED;
    private static final long NOT_DEVOTED = Long.MIN_VALUE;
    private boolean variantFromData;
    @Nullable
    private BlockPos lightPos;

    protected AbstractMonsterGirl(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level);
        this.species = species;
        this.goalSelector.addGoal(-1, new SitWhenOrderedGoal<>(this));
        this.goalSelector.addGoal(3, new ShareBedGoal<>(this));
        this.goalSelector.addGoal(3, new GiftGoal<>(this));
        this.goalSelector.addGoal(5, new FollowEntityGoal(this, this::mother, 1.1, 6.0F, 2.5F));
    }

    /** A young one stays near the closest grown woman of her species. */
    @Nullable
    protected net.minecraft.world.entity.LivingEntity mother() {
        if (!this.isBaby()) return null;
        return this.nearest(AbstractMonsterGirl.class, 16, m -> m.getType() == this.getType() && !m.isBaby());
    }

    public static AttributeSupplier.Builder girlAttributes(double health, double speed, double damage) {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ATTACK_DAMAGE, damage)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    public GirlSpecies getSpecies() {
        return this.species;
    }

    // --- synced data ---------------------------------------------------------------------------------------------

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ACTION, (byte) 0);
        builder.define(DATA_YOUNG, false);
        builder.define(DATA_VARIANT, (byte) 0);
        builder.define(DATA_BELLY, (byte) 0);
        builder.define(DATA_FLAGS, (byte) 0);
    }

    public Action getAction() {
        return Action.values()[this.entityData.get(DATA_ACTION)];
    }

    public void setAction(Action action) {
        this.entityData.set(DATA_ACTION, (byte) action.ordinal());
    }

    /** Plays an action for a while (unless another one is already playing), then goes back to NONE. */
    public void startTimedAction(Action action, int ticks) {
        if (this.getAction() != Action.NONE && this.getAction() != Action.SITTING && this.timedActionTicks <= 0) return;
        this.setAction(action);
        this.timedActionTicks = ticks;
    }

    public boolean hasFlag(int flag) {
        return (this.entityData.get(DATA_FLAGS) & flag) != 0;
    }

    public void setFlag(int flag, boolean on) {
        byte b = this.entityData.get(DATA_FLAGS);
        this.entityData.set(DATA_FLAGS, (byte) (on ? b | flag : b & ~flag));
    }

    // --- variant -------------------------------------------------------------------------------------------------

    public int getVariantIndex() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariantIndex(int index) {
        this.entityData.set(DATA_VARIANT, (byte) Math.max(0, Math.min(this.species.variants().size() - 1, index)));
    }

    public String getVariantName() {
        return this.species.variants().get(this.getVariantIndex());
    }

    public void setVariant(String name) {
        int i = this.species.variants().indexOf(name);
        this.setVariantIndex(Math.max(0, i));
    }

    /** "" for the default colours, otherwise "_variant" (textures/entity/&lt;species&gt;_&lt;variant&gt;.png). */
    public String getVariantSuffix() {
        int i = this.getVariantIndex();
        return i == 0 ? "" : "_" + this.species.variants().get(i);
    }

    /** Wild spawns: the default colours half the time, the four others share the rest. */
    protected int rollWildVariant(RandomSource random) {
        return random.nextBoolean() ? 0 : 1 + random.nextInt(this.species.variants().size() - 1);
    }

    public void randomizeVariant() {
        this.setVariantIndex(this.random.nextInt(this.species.variants().size()));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData groupData) {
        if (!this.variantFromData) this.setVariantIndex(this.rollWildVariant(this.random));
        return super.finalizeSpawn(level, difficulty, spawnType, groupData);
    }

    // --- relationships -------------------------------------------------------------------------------------------

    @Override
    public int getAffection(Player player) {
        return this.affection.getOrDefault(player.getUUID(), 0);
    }

    public int getAffection(UUID player) {
        return this.affection.getOrDefault(player, 0);
    }

    @Override
    public void addAffection(Player player, int amount) {
        this.affection.merge(player.getUUID(), amount, (a, b) -> Math.max(0, Math.min(FULL_AFFECTION, a + b)));
        this.affection.computeIfPresent(player.getUUID(), (k, v) -> Math.max(0, Math.min(FULL_AFFECTION, v)));
    }

    @Override
    public void setAffection(UUID player, int amount) {
        this.affection.put(player, Math.max(0, Math.min(FULL_AFFECTION, amount)));
    }

    public boolean hasAnyAffection() {
        return this.affection.values().stream().anyMatch(a -> a > 0);
    }

    /** The highest affection she has for anyone. */
    public int bestAffection() {
        return this.affection.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    /** The nearest player within range she likes at least this much, or null. */
    @Nullable
    public Player bondedPlayer(int minAffection, double range) {
        Player best = null;
        for (Player p : this.level().players()) {
            if (p.isSpectator() || this.getAffection(p) < minAffection || p.distanceToSqr(this) > range * range) continue;
            if (best == null || p.distanceToSqr(this) < best.distanceToSqr(this)) best = p;
        }
        return best;
    }

    @Override
    public Component describeBond(Player player) {
        int a = this.getAffection(player);
        String key = a >= FULL_AFFECTION ? "full" : a >= 60 ? "devoted" : a >= BED_AFFECTION ? "high" : a >= 10 ? "fond" : a >= 3 ? "curious" : "wild";
        return Component.translatable("bond.monstergirls.girl." + key, a);
    }

    @Override
    public Gestation getGestation() {
        return this.gestation;
    }

    @Override
    public boolean canShareBed() {
        return !this.isBaby() && this.gestation.canConceive() && !this.hasFlag(FLAG_DECOY) && !this.hasFlag(FLAG_HALF);
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
        if (father != null) this.setPersistenceRequired();
    }

    @Override
    public void settleInBed() {
        this.setAction(Action.LYING);
    }

    @Override
    public void leaveBed() {
        if (this.getAction() == Action.LYING) this.setAction(Action.NONE);
    }

    /** Called when the player whose bed she shared wakes up in the morning (a family's bed perk). */
    public void onNightShared(ServerPlayer player) {
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

    public boolean isTrueForm() {
        return this.hasFlag(FLAG_TRUE_FORM);
    }

    /** Whether this species awakens a true form after ten days at full devotion. */
    protected boolean hasTrueForm() {
        return false;
    }

    protected void onTrueFormAwakened(ServerLevel level) {
        Player p = this.bondedPlayer(FULL_AFFECTION, 64);
        if (p != null) p.displayClientMessage(Component.translatable("message.monstergirls.true_form", this.getDisplayName()), true);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1, this.getZ(), 30, 0.5, 0.8, 0.5, 0.02);
    }

    // --- interaction ---------------------------------------------------------------------------------------------

    /** Food she takes from a hand (any meat is always welcome too). */
    public abstract boolean isFood(ItemStack stack);

    /** The family's own interactions (a brush, a saddle...): a result, or null to carry on with the shared ones. */
    @Nullable
    protected InteractionResult familyInteract(Player player, InteractionHand hand, ItemStack stack) {
        return null;
    }

    /** How much a pat is worth to her (0 = she does not want it now). */
    protected int petValue(Player player) {
        return 1;
    }

    protected int foodValue(Player player, ItemStack stack) {
        return stack.is(ModTags.MEAT) ? 2 : 1;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) return super.mobInteract(player, hand);
        if (stack.is(Items.STICK)) {
            if (!this.level().isClientSide) {
                this.setOrderedToSit(!this.orderedToSit);
                player.displayClientMessage(Component.translatable(this.orderedToSit ? "message.monstergirls.sits" : "message.monstergirls.stands",
                        this.getDisplayName()), true);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        InteractionResult family = this.familyInteract(player, hand, stack);
        if (family != null) return family;
        if (!this.isBaby() && (this.isFood(stack) || stack.is(ModTags.MEAT))) {
            boolean hurt = this.getHealth() < this.getMaxHealth();
            boolean welcome = this.level().getGameTime() >= this.nextFoodTime;
            if (hurt || welcome) {
                if (this.level() instanceof ServerLevel server) {
                    int value = this.foodValue(player, stack);
                    stack.consume(1, player);
                    if (hurt) this.heal(4.0F);
                    if (welcome) {
                        this.nextFoodTime = server.getGameTime() + FOOD_COOLDOWN;
                        this.addAffection(player, value);
                    }
                    this.playSound(this.getEatSound(), 1.0F, this.getVoicePitch());
                    this.hearts(server, 2);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        if (stack.isEmpty() && !player.isShiftKeyDown()) {
            if (this.level() instanceof ServerLevel server) {
                int value = this.petValue(player);
                if (value > 0) {
                    if (server.getGameTime() >= this.nextPetTime) {
                        this.nextPetTime = server.getGameTime() + PET_COOLDOWN;
                        this.addAffection(player, value);
                    }
                    this.getLookControl().setLookAt(player);
                    this.hearts(server, 2);
                    this.playSound(this.getChirpSound(), 1.0F, this.getVoicePitch() * 1.1F);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    public void hearts(ServerLevel level, int count) {
        level.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight() + 0.2, this.getZ(), count, 0.3, 0.2, 0.3, 0.0);
    }

    /** How much a hit from a player costs (most families: 3; the slimes lose half). */
    protected void onHurtByPlayer(Player player) {
        this.addAffection(player, -3);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && source.getEntity() instanceof Player player) this.onHurtByPlayer(player);
        return hurt;
    }

    // --- presents ------------------------------------------------------------------------------------------------

    @Override
    public ItemStack rollGift(Player player) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isGiftDue() {
        if (this.nextGiftTime < 0) this.scheduleNextGift();
        return this.level().getGameTime() >= this.nextGiftTime;
    }

    @Override
    public void scheduleNextGift() {
        this.nextGiftTime = this.level().getGameTime() + GIFT_INTERVAL + this.random.nextInt(GIFT_INTERVAL);
    }

    @Override
    public void setNextGiftTime(long time) {
        this.nextGiftTime = time;
    }

    // --- sounds --------------------------------------------------------------------------------------------------

    /** Munching, when she eats from a hand. */
    public abstract SoundEvent getEatSound();

    @Override
    public SoundEvent getChirpSound() {
        SoundEvent s = this.getAmbientSound();
        return s != null ? s : this.getEatSound();
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * (this.isBaby() ? 1.5F : 1.2F);
    }

    // --- young ---------------------------------------------------------------------------------------------------

    @Override
    public boolean isBaby() {
        return this.entityData.get(DATA_YOUNG);
    }

    @Override
    public void setBaby(boolean baby) {
        this.entityData.set(DATA_YOUNG, baby);
        this.growTicks = 0;
    }

    public void setGrowTicks(int ticks) {
        this.growTicks = ticks;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_YOUNG.equals(key) || DATA_FLAGS.equals(key)) this.refreshDimensions();
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        EntityDimensions d = super.getDefaultDimensions(pose);
        float s = this.sizeScale();
        return s == 1.0F ? d : d.scale(s);
    }

    /** Her size relative to a grown adult (the young, a split slime). */
    public float sizeScale() {
        return this.isBaby() ? YOUNG_SCALE : this.hasFlag(FLAG_HALF) ? 0.6F : 1.0F;
    }

    /** How many young she bears at once. */
    protected int litterSize() {
        return 1;
    }

    /**
     * Her time has come (the gestation day is over). Returns true when the young (or the eggs) are out. The default is
     * live young right beside her; egg-laying families override.
     */
    protected boolean giveBirth(ServerLevel level, @Nullable UUID father) {
        int n = this.litterSize();
        for (int i = 0; i < n; i++) this.spawnYoung(level, this.blockPosition(), father);
        return true;
    }

    /** One newborn of her species at a spot: small, a random colour of her species, devoted to her father. */
    @Nullable
    public AbstractMonsterGirl spawnYoung(ServerLevel level, BlockPos pos, @Nullable UUID father) {
        if (!(this.getType().create(level) instanceof AbstractMonsterGirl young)) return null;
        young.moveTo(pos.getX() + 0.5 + (this.random.nextDouble() - 0.5), pos.getY(), pos.getZ() + 0.5 + (this.random.nextDouble() - 0.5),
                this.random.nextFloat() * 360.0F, 0.0F);
        young.setBaby(true);
        young.randomizeVariant();
        level.addFreshEntity(young);
        young.bornTo(father);
        return young;
    }

    public static AbstractMonsterGirl youngOf(ServerLevel level, EntityType<?> type, BlockPos pos, @Nullable UUID father) {
        if (!(type.create(level) instanceof AbstractMonsterGirl young)) return null;
        young.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
        young.setBaby(true);
        young.randomizeVariant();
        level.addFreshEntity(young);
        young.bornTo(father);
        return young;
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) return;
        this.gestation.tick();
        if (this.tickCount % 10 == 0) this.entityData.set(DATA_BELLY, (byte) Math.round(this.bellyProgress() * 100));
        if (this.isBaby() && ++this.growTicks >= GROW_TICKS) this.setBaby(false);
        if (this.timedActionTicks > 0 && --this.timedActionTicks == 0) this.setAction(this.orderedToSit ? Action.SITTING : Action.NONE);
        if (this.orderedToSit && this.getAction() == Action.NONE) this.setAction(Action.SITTING);
        if (this.gestation.isReady() && this.tickCount % 40 == 0 && this.giveBirth(server, this.gestation.getFather())) {
            this.gestation.clear();
            server.playSound(null, this.blockPosition(), net.minecraft.sounds.SoundEvents.CHICKEN_EGG, net.minecraft.sounds.SoundSource.NEUTRAL, 0.8F, 1.3F);
        }
        if (this.hasTrueForm() && !this.isTrueForm() && this.tickCount % 100 == 0) {
            if (this.bestAffection() >= FULL_AFFECTION) {
                if (this.devotedSince == NOT_DEVOTED) this.devotedSince = server.getGameTime();
                else if (server.getGameTime() - this.devotedSince >= TRUE_FORM_TICKS) {
                    this.setFlag(FLAG_TRUE_FORM, true);
                    this.onTrueFormAwakened(server);
                }
            } else {
                this.devotedSince = NOT_DEVOTED;
            }
        }
    }

    /** 0..1: how round her middle is (expecting; the lamia also when digesting). */
    protected float bellyProgress() {
        return this.gestation.progress();
    }

    public float getBellySwell() {
        return this.entityData.get(DATA_BELLY) / 100.0F;
    }

    public void setDevotedSince(long time) {
        this.devotedSince = time;
    }

    // --- light (foxfire, a glowing slime) -------------------------------------------------------------------------

    /** Keeps a vanilla light block at her feet (or removes it with level 0); she moves it along as she walks. */
    protected void glow(ServerLevel level, int lightLevel) {
        BlockPos here = this.blockPosition();
        if (lightLevel <= 0) {
            this.clearLight();
            return;
        }
        if (here.equals(this.lightPos)) return;
        this.clearLight();
        if (level.getBlockState(here).isAir()) {
            level.setBlock(here, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, lightLevel), 3);
            this.lightPos = here;
        }
    }

    protected void clearLight() {
        if (this.lightPos != null && this.level().getBlockState(this.lightPos).is(Blocks.LIGHT)) {
            this.level().setBlock(this.lightPos, Blocks.AIR.defaultBlockState(), 3);
        }
        this.lightPos = null;
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide) this.clearLight();
        super.remove(reason);
    }

    // --- persistence ---------------------------------------------------------------------------------------------

    @Override
    public boolean removeWhenFarAway(double distance) {
        return !this.hasAnyAffection() && !this.orderedToSit && this.father == null && !this.gestation.isCarrying() && !this.isSettled();
    }

    /** A family's own reasons to stay (a den, a herd she was led to...). */
    protected boolean isSettled() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        CompoundTag a = new CompoundTag();
        this.affection.forEach((uuid, v) -> a.putInt(uuid.toString(), v));
        tag.put("Affection", a);
        this.gestation.save(tag);
        if (this.father != null) tag.putUUID("Father", this.father);
        tag.putBoolean("OrderedToSit", this.orderedToSit);
        tag.putLong("NextGift", this.nextGiftTime);
        tag.putBoolean("Young", this.isBaby());
        tag.putInt("GrowTicks", this.growTicks);
        tag.putString("Variant", this.getVariantName());
        tag.putByte("Flags", this.entityData.get(DATA_FLAGS));
        tag.putLong("DevotedSince", this.devotedSince);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.affection.clear();
        CompoundTag a = tag.getCompound("Affection");
        for (String k : a.getAllKeys()) this.affection.put(UUID.fromString(k), a.getInt(k));
        this.gestation.load(tag);
        this.father = tag.hasUUID("Father") ? tag.getUUID("Father") : null;
        this.orderedToSit = tag.getBoolean("OrderedToSit");
        this.nextGiftTime = tag.contains("NextGift") ? tag.getLong("NextGift") : -1;
        this.entityData.set(DATA_YOUNG, tag.getBoolean("Young"));
        this.growTicks = tag.getInt("GrowTicks");
        if (tag.contains("Variant")) {
            this.variantFromData = true;
            this.setVariant(tag.getString("Variant"));
        }
        this.entityData.set(DATA_FLAGS, (byte) (tag.getByte("Flags") & (FLAG_TRUE_FORM | FLAG_SADDLED | FLAG_CHEST | FLAG_HALF)));
        this.devotedSince = tag.contains("DevotedSince") ? tag.getLong("DevotedSince") : NOT_DEVOTED;
    }

    // --- animation -----------------------------------------------------------------------------------------------

    protected RawAnimation anim(String name, boolean once) {
        String key = (once ? "once:" : "") + this.species.id() + "." + name;
        return ANIMATIONS.computeIfAbsent(key, k -> once ? RawAnimation.begin().thenPlayAndHold("animation." + this.species.id() + "." + name)
                : RawAnimation.begin().thenLoop("animation." + this.species.id() + "." + name));
    }

    /** Whether her animation file has a swim animation (the merfolk). */
    protected boolean hasSwimAnimation() {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, this::animate));
    }

    private PlayState animate(AnimationState<AbstractMonsterGirl> state) {
        Action action = this.getAction();
        if (action.animation != null) return state.setAndContinue(this.anim(action.animation, action.once));
        if (this.isInWater() && this.hasSwimAnimation()) return state.setAndContinue(this.anim(state.isMoving() ? "swim" : "idle", false));
        if (state.isMoving()) {
            boolean fast = this.isSprinting() || this.isAggressive() || this.getDeltaMovement().horizontalDistanceSqr() > 0.02;
            return state.setAndContinue(this.anim(fast ? "run" : "walk", false));
        }
        return state.setAndContinue(this.anim("idle", false));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    // --- helpers for families ------------------------------------------------------------------------------------

    /** The nearest entity of a class within a radius that passes the test, or null. */
    @Nullable
    public <E extends net.minecraft.world.entity.Entity> E nearest(Class<E> cls, double radius, java.util.function.Predicate<E> test) {
        E best = null;
        for (E e : this.level().getEntitiesOfClass(cls, this.getBoundingBox().inflate(radius), e -> e != this && test.test(e))) {
            if (best == null || e.distanceToSqr(this) < best.distanceToSqr(this)) best = e;
        }
        return best;
    }

    /** Whether a spot can hold an egg block (air above something solid). */
    protected static boolean canHoldBlock(LevelAccessor level, BlockPos pos) {
        return level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP);
    }
}
