package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.entity.ai.KickJukeboxGoal;
import com.linkyoshi90.harmontide.entity.ai.ListenToMusicGoal;
import com.linkyoshi90.harmontide.entity.ai.BroodGoal;
import com.linkyoshi90.harmontide.entity.ai.FollowLeaderGoal;
import com.linkyoshi90.harmontide.entity.ai.GlareGoal;
import com.linkyoshi90.harmontide.entity.ai.OstrichKickGoal;
import com.linkyoshi90.harmontide.entity.ai.ReturnHomeGoal;
import com.linkyoshi90.harmontide.entity.ai.TerritoryWarningGoal;
import com.linkyoshi90.harmontide.registry.ModSounds;
import com.linkyoshi90.harmontide.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Flightless, fast and proud. Every ostrich has a nest (where she spawned) and patrols the ground around it.
 * Anyone who wanders into her territory gets a warning display (wings spread, hissing); stay and she charges and
 * kicks, a few times at most and never to the death, then just glares at you until you leave. Hostile mobs in her
 * territory get kicked out, and at night she broods on the nest. Petting calms her down.
 * <p>
 * She eats anything, leafy greens most of all. Every treat (and the odd pat) builds affection: at 5 she trusts you
 * and lets you near, and the first to reach 12 gets a loyal companion. A loyal ostrich treats wherever you are as
 * her territory: she follows you (sneak + empty hand to make her stay), drives off anything hostile that comes
 * close, attacks whatever you attack, and can be saddled and ridden, sprinting faster than any horse.
 */
public class OstrichHarpy extends AbstractHarpy implements net.minecraft.world.entity.PlayerRideableJumping {
    private static final EntityDataAccessor<Byte> DATA_VARIANT =
            SynchedEntityData.defineId(OstrichHarpy.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DATA_SADDLED =
            SynchedEntityData.defineId(OstrichHarpy.class, EntityDataSerializers.BOOLEAN);
    /** Affection needed before she lets you near the nest, and to become loyal. */
    public static final int TRUSTED = 5;
    public static final int LOYAL = 12;
    /** Radius around the nest (or her companion) she defends, and how close a sneaking player may come. */
    public static final double TERRITORY = 10.0;
    public static final double TERRITORY_SNEAKING = 5.0;
    /** She kicks a trespasser at most this many times, then settles for glaring. */
    public static final int MAX_KICKS = 3;
    /** How long a kicked (or petted) player is spared further charges. */
    private static final int SPARE_TICKS = 2400;
    /** Past this distance from her territory's centre she gives up a chase. */
    private static final double CHASE_LIMIT = 22.0;
    /** Ridden: walking pace, and a sprint faster than the fastest horse (0.3375). */
    private static final float RIDDEN_WALK = 0.22F;
    private static final float RIDDEN_SPRINT = 0.5F;

    @Nullable
    private UUID loyalTo;
    private boolean following = true;
    private final Map<UUID, Integer> kicksTaken = new HashMap<>();
    private final Map<UUID, Long> sparedUntil = new HashMap<>();

    /** Colour variants; each has its own texture, generated by tools/gen_ostrich_harpy.py. Saved by name, synced by ordinal. */
    public enum Variant {
        OSTRICH("ostrich"), HEN("hen"), EMU("emu"), CASSOWARY("cassowary"), RHEA("rhea");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return OSTRICH;
        }

        /** Savanna ostriches (cock or hen), emus in the badlands, cassowaries in jungles, rheas on the plains. */
        public static Variant forBiome(Holder<Biome> biome, RandomSource random) {
            if (biome.is(Tags.Biomes.IS_JUNGLE)) return CASSOWARY;
            if (biome.is(Tags.Biomes.IS_BADLANDS)) return EMU;
            if (biome.is(Tags.Biomes.IS_SAVANNA) || biome.is(Tags.Biomes.IS_DESERT)) return random.nextBoolean() ? OSTRICH : HEN;
            return RHEA;
        }
    }

    public OstrichHarpy(EntityType<? extends OstrichHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.ATTACK_KNOCKBACK, 1.2)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4)
                .add(Attributes.STEP_HEIGHT, 1.1)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    public static boolean checkOstrichHarpySpawnRules(EntityType<OstrichHarpy> type, ServerLevelAccessor level,
                                                      MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(ModTags.OSTRICH_HARPY_SPAWNABLE_ON) && level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    protected boolean canFly() {
        return false;
    }

    @Override
    protected String species() {
        return "ostrich_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case DISPLAYING -> "display";
            case KICKING -> "kick";
            case BROODING -> "brood";
            default -> null;
        };
    }

    @Override
    protected String movingAnimation() {
        // Runs when charging (the aggressive flag is synced to clients) or simply moving fast.
        return this.isAggressive() || this.walkAnimation.speed() > 0.8F ? "run" : "walk";
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new OstrichKickGoal(this, 1.7));
        this.goalSelector.addGoal(2, new KickJukeboxGoal(this, 1.5));
        this.goalSelector.addGoal(2, new TerritoryWarningGoal(this));
        this.goalSelector.addGoal(2, new GlareGoal(this));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0, this::isEdible, false) {
            // Food only lures her within her patrol area; otherwise she'd be pulled back and forth between the
            // treat and the walk home to her nest.
            @Override
            public boolean canUse() {
                return super.canUse() && OstrichHarpy.this.isWithinTemptReach(this.player);
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && OstrichHarpy.this.isWithinTemptReach(this.player);
            }
        });
        // Her companion's music: she dances her courtship display to it, stamping the beat.
        this.goalSelector.addGoal(4, new ListenToMusicGoal(this, 1.2, 3.0, Action.DISPLAYING, h -> this.getLoyalPlayer() != null && h.getTarget() == null,
                (h, jukebox, t) -> {
                    if (t % 10 == 0) h.playSound(SoundEvents.CAMEL_STEP, 0.8F, 0.9F + h.getRandom().nextFloat() * 0.2F);
                }, null));
        this.goalSelector.addGoal(4, new FollowLeaderGoal(this, () -> this.following ? this.getLoyalPlayer() : null, 1.4, 10.0F, 3.0F));
        this.goalSelector.addGoal(5, new BroodGoal(this));
        this.goalSelector.addGoal(6, new ReturnHomeGoal(this, 1.1, 20.0));
        this.goalSelector.addGoal(7, new MoveTowardsRestrictionGoal(this, 1.0));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.9)); // patrols: strolls stay inside her restriction
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                // Her loyal companion can clumsily hit her without starting a fight (it costs affection instead).
                return !(OstrichHarpy.this.getLastHurtByMob() instanceof Player p && OstrichHarpy.this.isLoyalTo(p)) && super.canUse();
            }
        });
        // Any hostile mob inside her territory (around the nest, or around her companion) gets driven off.
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false,
                m -> m instanceof Enemy && this.isInTerritory(m)));
    }

    // --- diet -------------------------------------------------------------------------------------------------

    /** An omnivore: she'll eat any food, plus seeds and plants. */
    @Override
    public boolean isEdible(ItemStack stack) {
        return stack.has(DataComponents.FOOD) || stack.is(ModTags.OSTRICH_HARPY_FOOD) || this.isFavouriteFood(stack);
    }

    /** Leafy greens: worth double affection. */
    public boolean isFavouriteFood(ItemStack stack) {
        return stack.is(ModTags.OSTRICH_HARPY_FAVOURITES);
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return false;
    }

    @Override
    public boolean wantsItem(ItemStack stack) {
        return super.wantsItem(stack) && this.getTarget() == null;
    }

    // --- territory --------------------------------------------------------------------------------------------

    /** Her companion's position while she follows them, otherwise her nest. */
    @Nullable
    public Vec3 getTerritoryCenter() {
        Player companion = this.following ? this.getLoyalPlayer() : null;
        if (companion != null) return companion.position();
        BlockPos nest = this.getHomePos();
        return nest == null ? null : Vec3.atBottomCenterOf(nest);
    }

    public boolean isInTerritory(Entity entity) {
        Vec3 center = this.getTerritoryCenter();
        return center != null && entity.distanceToSqr(center) < TERRITORY * TERRITORY;
    }

    /** Whether this player is somewhere she objects to right now. */
    public boolean isIntruder(Player player) {
        Vec3 center = this.getTerritoryCenter();
        if (center == null || player.isCreative() || player.isSpectator() || !player.isAlive()) return false;
        if (this.isTrusted(player) || this.level().getDifficulty() == Difficulty.PEACEFUL) return false;
        double radius = player.isShiftKeyDown() ? TERRITORY_SNEAKING : TERRITORY;
        return player.distanceToSqr(center) < radius * radius;
    }

    /** A treat only draws her as far as her patrol area reaches (anywhere, if she's out following her companion). */
    public boolean isWithinTemptReach(@Nullable Player player) {
        BlockPos nest = this.getHomePos();
        if (player == null) return false;
        if (nest == null) return true;
        double reach = TERRITORY + 4;   // the same radius her patrol (restrictTo) uses
        return player.distanceToSqr(Vec3.atBottomCenterOf(nest)) < reach * reach;
    }

    /** Already kicked (or calmed by petting) recently: she glares instead of charging again. */
    public boolean isSpared(Player player) {
        return this.sparedUntil.getOrDefault(player.getUUID(), 0L) > this.level().getGameTime();
    }

    public void spare(Player player) {
        this.sparedUntil.put(player.getUUID(), this.level().getGameTime() + SPARE_TICKS);
        this.kicksTaken.remove(player.getUUID());
        if (this.getTarget() == player) this.setTarget(null);
    }

    /** Called by the kick goal; after a few kicks she stops and spares the player. */
    public void onKicked(Player player) {
        int kicks = this.kicksTaken.merge(player.getUUID(), 1, Integer::sum);
        if (kicks >= MAX_KICKS) this.spare(player);
    }

    @Override
    public void setHomePos(@Nullable BlockPos pos) {
        super.setHomePos(pos);
        // Patrol: random strolls stay within the territory.
        if (pos != null) this.restrictTo(pos, (int) TERRITORY + 4);
        else this.clearRestriction();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData groupData) {
        if (!this.hasVariantFromData()) this.setVariant(Variant.forBiome(level.getBiome(this.blockPosition()), this.random));
        this.setHomePos(this.blockPosition()); // her nest
        return super.finalizeSpawn(level, difficulty, spawnType, groupData);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;
        if (this.getHomePos() == null && this.loyalTo == null) this.setHomePos(this.blockPosition());
        // Give up a chase far from her territory, or as soon as the intruder is someone she trusts or has spared.
        LivingEntity target = this.getTarget();
        Vec3 center = this.getTerritoryCenter();
        if (target instanceof Player player && (this.isTrusted(player) || player.isCreative() || this.isSpared(player)
                || center == null || player.distanceToSqr(center) > CHASE_LIMIT * CHASE_LIMIT)) {
            this.setTarget(null);
        }
        Player companion = this.getLoyalPlayer();
        if (companion != null && this.getTarget() == null && this.distanceToSqr(companion) < 24 * 24) {
            // Defend her companion from whoever hurts them, and join in on whatever they attack.
            LivingEntity attacker = companion.getLastHurtByMob();
            LivingEntity attacked = companion.getLastHurtMob();
            if (attacker != null && attacker != this && attacker.isAlive() && companion.tickCount - companion.getLastHurtByMobTimestamp() < 100) {
                this.setTarget(attacker);
            } else if (attacked != null && attacked != this && attacked.isAlive() && companion.tickCount - companion.getLastHurtMobTimestamp() < 100) {
                this.setTarget(attacked);
            }
        }
    }

    // --- affection & loyalty ----------------------------------------------------------------------------------

    public boolean isTrusted(Player player) {
        return this.getAffection(player) >= TRUSTED;
    }

    public boolean isLoyalTo(Player player) {
        return player.getUUID().equals(this.loyalTo);
    }

    @Nullable
    public Player getLoyalPlayer() {
        return this.loyalTo == null ? null : this.level().getPlayerByUUID(this.loyalTo);
    }

    @Override
    public void addAffection(Player player, int amount) {
        int before = this.getAffection(player);
        super.addAffection(player, amount);
        int after = this.getAffection(player);
        String message = null;
        if (before < TRUSTED && after >= TRUSTED) message = "entity.harmontide.ostrich_harpy.trusted";
        if (after >= LOYAL && this.loyalTo == null) {
            this.loyalTo = player.getUUID();
            this.following = true;
            this.setHomePos(null);
            this.setPersistenceRequired();
            message = "entity.harmontide.ostrich_harpy.loyal";
        }
        if (message != null) player.displayClientMessage(Component.translatable(message, this.getDisplayName()), true);
    }

    @Override
    public Component describeBond(Player player) {
        int affection = this.getAffection(player);
        String level;
        if (this.isLoyalTo(player)) level = "companion";
        else if (this.getTarget() == player || this.isSpared(player)) level = "angry";
        else if (affection >= TRUSTED) level = "trusts";
        else level = affection > 0 ? "tolerates" : "wary";
        return Component.translatable("bond.harmontide.ostrich." + level, affection, LOYAL);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean emptyHand = stack.isEmpty();
        if (this.isLoyalTo(player)) {
            if (player.isShiftKeyDown() && emptyHand) {
                if (!this.level().isClientSide) {
                    this.following = !this.following;
                    // Staying makes the current spot her nest; following drops the nest.
                    this.setHomePos(this.following ? null : this.blockPosition());
                    player.displayClientMessage(Component.translatable(this.following ? "entity.harmontide.ostrich_harpy.follow"
                            : "entity.harmontide.ostrich_harpy.stay", this.getDisplayName()), true);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            if (stack.is(Items.SADDLE) && !this.isSaddled()) {
                if (!this.level().isClientSide) {
                    this.setSaddled(true);
                    stack.consume(1, player);
                    this.playSound(SoundEvents.HORSE_SADDLE, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            if (emptyHand && this.isSaddled() && !this.isVehicle() && hand == InteractionHand.MAIN_HAND) {
                if (!this.level().isClientSide) {
                    this.getNavigation().stop();
                    this.setTarget(null);
                    player.startRiding(this);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        // An offering of plant food at the edge of her territory makes peace, even mid-charge.
        if (stack.is(ModTags.HERBIVORE_FOOD) && (this.getTarget() == player || this.getAction() == Action.DISPLAYING)) {
            if (!this.level().isClientSide) {
                stack.consume(1, player);
                this.spare(player);
                if (this.getAction() == Action.DISPLAYING) this.setAction(Action.NONE);
                this.addAffection(player, 1);
                this.playSound(this.getEatSound(), 1.0F, this.getVoicePitch());
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        // Other food: she won't take it from someone she's busy chasing off.
        if (this.isEdible(stack) && this.getTarget() != player) {
            if (this.level() instanceof ServerLevel server) {
                boolean favourite = this.isFavouriteFood(stack);
                stack.consume(1, player);
                this.heal(2.0F);
                this.playSound(this.getEatSound(), 1.0F, this.getVoicePitch());
                this.addAffection(player, favourite ? 2 : 1);
                server.sendParticles(this.isTrusted(player) ? ParticleTypes.HEART : ParticleTypes.HAPPY_VILLAGER,
                        this.getX(), this.getY() + this.getBbHeight() + 0.2, this.getZ(), favourite ? 6 : 3, 0.3, 0.2, 0.3, 0.0);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand); // empty hand: petting
    }

    @Override
    protected void onPetted(Player player) {
        // A gentle hand calms her down: no more charging this player for a while.
        boolean wasAngry = this.getTarget() == player || this.getAction() == Action.DISPLAYING;
        this.spare(player);
        if (this.getAction() == Action.DISPLAYING) this.setAction(Action.NONE);
        this.playSound(ModSounds.OSTRICH_AMBIENT.get(), 0.6F, wasAngry ? 1.2F : 1.0F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && source.getEntity() instanceof Player player) {
            if (this.isLoyalTo(player)) {
                this.setAffection(player, Math.max(LOYAL, this.getAffection(player) - 1));
            } else {
                this.forgetAffection(player); // trust is slow to build and quick to lose
                this.sparedUntil.remove(player.getUUID());
            }
        }
        return hurt;
    }

    // --- riding -----------------------------------------------------------------------------------------------

    public boolean isSaddled() {
        return this.entityData.get(DATA_SADDLED);
    }

    public void setSaddled(boolean saddled) {
        this.entityData.set(DATA_SADDLED, saddled);
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return this.isSaddled() && this.getFirstPassenger() instanceof Player player ? player : super.getControllingPassenger();
    }

    @Override
    protected void tickRidden(Player player, Vec3 travelVector) {
        super.tickRidden(player, travelVector);
        Vec2 rotation = new Vec2(player.getXRot() * 0.5F, player.getYRot());
        this.setRot(rotation.y, rotation.x);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
        if (this.jumpPending > 0) {                                        // a running leap with her rider (entity/Traversal)
            if (this.onGround()) {
                Vec3 fwd = Vec3.directionFromRotation(0, this.getYRot()).scale(0.45 * this.jumpPending);
                Vec3 v = this.getDeltaMovement();
                this.setDeltaMovement(v.x + fwd.x, 0.42 + 0.45 * this.jumpPending, v.z + fwd.z);
                this.hasImpulse = true;
            }
            this.jumpPending = 0;
        }
    }

    private float jumpPending;

    @Override
    public void onPlayerJump(int power) {
        if (this.canJump()) this.jumpPending = power >= 90 ? 1.0F : 0.4F + 0.6F * power / 90.0F;
    }

    /** Saddled and ridden, she leaps like a horse (hold jump and let go). */
    @Override
    public boolean canJump() {
        return this.isSaddled() && this.isVehicle() && Traversal.on();
    }

    @Override
    public void handleStartJump(int power) {
        this.playSound(net.minecraft.sounds.SoundEvents.HORSE_JUMP, 0.5F, 1.4F);
    }

    @Override
    public void handleStopJump() {
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 travelVector) {
        float forward = player.zza <= 0.0F ? player.zza * 0.25F : player.zza;
        return new Vec3(player.xxa * 0.5F, 0.0, forward);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return player.isSprinting() ? RIDDEN_SPRINT : RIDDEN_WALK;
    }

    /** Lets the rider's sprint key work while mounted (like a camel). */
    @Override
    public boolean canSprint() {
        return true;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.isSaddled()) this.spawnAtLocation(Items.SADDLE);
    }

    // --- variant ----------------------------------------------------------------------------------------------

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, (byte) Variant.OSTRICH.ordinal());
        builder.define(DATA_SADDLED, false);
    }

    public Variant getVariant() {
        return Variant.values()[this.entityData.get(DATA_VARIANT)];
    }

    public void setVariant(Variant variant) {
        this.entityData.set(DATA_VARIANT, (byte) variant.ordinal());
    }

    // --- save -------------------------------------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Variant", this.getVariant().getSerializedName());
        if (this.loyalTo != null) tag.putUUID("LoyalTo", this.loyalTo);
        tag.putBoolean("Following", this.following);
        tag.putBoolean("Saddled", this.isSaddled());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setVariant(Variant.byName(tag.getString("Variant")));
        this.loyalTo = tag.hasUUID("LoyalTo") ? tag.getUUID("LoyalTo") : null;
        this.following = !tag.contains("Following") || tag.getBoolean("Following");
        this.setSaddled(tag.getBoolean("Saddled"));
        // The base class restores the nest; re-apply the patrol restriction around it.
        this.setHomePos(this.getHomePos());
    }

    // --- sounds -----------------------------------------------------------------------------------------------

    @Override
    public int getAmbientSoundInterval() {
        return 480; // a proud, mostly quiet bird: an occasional boom, not constant chatter
    }

    @Override
    protected float getSoundVolume() {
        return 0.7F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.getAction() == Action.BROODING ? null : ModSounds.OSTRICH_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.OSTRICH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.OSTRICH_DEATH.get();
    }

    @Override
    protected SoundEvent getEatSound() {
        return ModSounds.OSTRICH_EAT.get();
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

    /** Hatched from her father's clutch: loyal to that player from the start. */
    @Override
    protected void onBornTo(UUID father) {
        this.loyalTo = father;
        this.following = true;
        this.setHomePos(null);
    }

    @Override
    protected net.minecraft.world.item.ItemStack unfertilisedEgg() {
        return new net.minecraft.world.item.ItemStack(com.linkyoshi90.harmontide.registry.ModItems.OSTRICH_EGG.get());
    }
}
