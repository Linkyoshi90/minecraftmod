package com.linkyoshi90.monstergirls.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.JukeboxSongs;
import com.linkyoshi90.monstergirls.entity.ai.ListenToMusicGoal;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.ai.FollowLeaderGoal;
import com.linkyoshi90.monstergirls.entity.ai.PhoenixAttackGoal;
import com.linkyoshi90.monstergirls.registry.ModItems;
import com.linkyoshi90.monstergirls.registry.ModSounds;
import com.linkyoshi90.monstergirls.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Fierce, fire-proof flier of the Nether. A wild phoenix attacks anyone who comes close: fire bolts from range,
 * talons up close. Slain, she burns to ashes; ashes left by a fire hatch into a tiny phoenix that imprints on the
 * first creature she sees - even someone who just killed her. A phoenix imprinted on a player follows and
 * defends them, sheds healing tears (Regeneration) when they're badly hurt, and knocks ghast fireballs back
 * where they came from. One imprinted on a mob follows and defends that mob instead. Chicks grow up in two days.
 */
public class PhoenixHarpy extends AbstractHarpy {
    private static final ResourceLocation HATCHLING_SCALE = MonsterGirlsMod.id("phoenix_hatchling");
    private static final int GROW_UP_TICKS = 48000;
    private static final int TEAR_COOLDOWN = 2400;

    @Nullable
    private UUID imprintedOn;
    private int hatchlingAge;
    private long nextTearTime;

    private static final EntityDataAccessor<Byte> DATA_VARIANT =
            SynchedEntityData.defineId(PhoenixHarpy.class, EntityDataSerializers.BYTE);

    /**
     * The colour of her flame, each with its own texture and glow mask from tools/gen_phoenix_harpy.py (the first is the
     * default phoenix_harpy.png). Saved by name, synced by ordinal: append new ones at the end.
     */
    public enum Variant {
        FLAME("flame"), AZURE("azure"), VERDANT("verdant"), SOLAR("solar"), EMBER("ember");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return FLAME;
        }

        /**
         * A blue flame in the soul sand valley, a green one in the warped forest, ash and ember on the basalt deltas,
         * crimson flame in the crimson forest; anywhere else (nether wastes, or ashes hatched in the overworld) mostly
         * flame, now and then a sun-bright solar or a smouldering ember phoenix.
         */
        public static Variant forBiome(Holder<Biome> biome, RandomSource random) {
            if (biome.is(Biomes.SOUL_SAND_VALLEY)) return AZURE;
            if (biome.is(Biomes.WARPED_FOREST)) return VERDANT;
            if (biome.is(Biomes.BASALT_DELTAS)) return EMBER;
            if (biome.is(Biomes.CRIMSON_FOREST)) return FLAME;
            int roll = random.nextInt(8);
            return roll == 0 ? SOLAR : roll == 1 ? EMBER : FLAME;
        }
    }

    private record VariantGroupData(Variant variant) implements SpawnGroupData {}

    public PhoenixHarpy(EntityType<? extends PhoenixHarpy> type, Level level) {
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

    /** Texture suffix for the renderer: "" for the default flame. */
    public String getVariantSuffix() {
        return this.getVariant().ordinal() == 0 ? "" : "_" + this.getVariant().getSerializedName();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData groupData) {
        // One flame per group: the first spawn picks for the biome, the rest follow.
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
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FLYING_SPEED, 0.55)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    public static boolean checkPhoenixHarpySpawnRules(EntityType<PhoenixHarpy> type, ServerLevelAccessor level,
                                                      MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(ModTags.PHOENIX_HARPY_SPAWNABLE_ON);
    }

    @Override
    protected String species() {
        return "phoenix_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case STRIKING -> "strike";
            case CRYING -> "cry";
            default -> null;
        };
    }

    @Override
    protected boolean actionShowsInFlight(Action action) {
        return action == Action.STRIKING;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PhoenixAttackGoal(this));
        // Music soothes the fierce phoenix: she drifts over to listen. To Pigstep she dances, striking sparks.
        this.goalSelector.addGoal(2, new ListenToMusicGoal(this, 1.0, 3.5, Action.NONE, h -> h.getTarget() == null,
                (h, jukebox, t) -> this.listenTo(jukebox, t), null));
        this.goalSelector.addGoal(3, new FollowLeaderGoal(this, () -> this.getImprintedPlayer(), 1.2, 10.0F, 3.0F));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return !PhoenixHarpy.this.isImprintedOn(PhoenixHarpy.this.getLastHurtByMob()) && super.canUse();
            }
        });
        // Wild phoenixes go for anyone who comes close (but never whoever they imprinted on).
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                p -> this.imprintedOn == null && !this.isHatchling() && !this.isSoothed() && this.distanceToSqr(p) < 12 * 12));
    }

    // --- imprinting & hatchlings ------------------------------------------------------------------------------

    /** A jukebox playing within earshot calms her: no picking fights while the music lasts (she still hits back). */
    public boolean isSoothed() {
        return this.getMusicNearby() != null;
    }

    private void listenTo(BlockPos jukebox, int ticks) {
        if (!(this.level() instanceof ServerLevel server)) return;
        if (JukeboxSongs.PIGSTEP.equals(JukeboxSensing.songAt(server, jukebox))) {
            // A fire dance: a talon strike on every other beat, with a ring of flame.
            if (ticks % 20 == 0) {
                this.startTimedAction(Action.STRIKING, 12);
                server.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.0, this.getZ(), 24, 0.8, 0.4, 0.8, 0.02);
                this.playSound(SoundEvents.BLAZE_SHOOT, 0.4F, 1.4F);
            }
        } else if (ticks % 40 == 0) {
            server.sendParticles(ParticleTypes.SMALL_FLAME, this.getX(), this.getY() + 1.6, this.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
        }
    }

    public boolean isImprintedOn(@Nullable Entity entity) {
        return entity != null && entity.getUUID().equals(this.imprintedOn);
    }

    @Nullable
    public UUID getImprintedOn() {
        return this.imprintedOn;
    }

    @Nullable
    public Entity getImprinted() {
        return this.imprintedOn != null && this.level() instanceof ServerLevel server ? server.getEntity(this.imprintedOn) : null;
    }

    @Nullable
    public Player getImprintedPlayer() {
        return this.getImprinted() instanceof Player player ? player : null;
    }

    public void imprintOn(Entity entity) {
        this.imprintedOn = entity.getUUID();
        this.setTarget(null);
        this.setPersistenceRequired();
        if (entity instanceof Player player) {
            this.setAffection(player, 10);
            player.displayClientMessage(Component.translatable("entity.monstergirls.phoenix_harpy.imprinted", this.getDisplayName()), true);
        }
    }

    public boolean isHatchling() {
        AttributeInstance scale = this.getAttribute(Attributes.SCALE);
        return scale != null && scale.hasModifier(HATCHLING_SCALE);
    }

    public void setHatchling(boolean hatchling) {
        AttributeInstance scale = this.getAttribute(Attributes.SCALE);
        if (scale == null) return;
        if (hatchling) {
            scale.addOrReplacePermanentModifier(new AttributeModifier(HATCHLING_SCALE, -0.55, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            this.hatchlingAge = 0;
        } else {
            scale.removeModifier(HATCHLING_SCALE);
        }
    }

    /** Reborn from ashes: a chick that imprints on the nearest creature in sight (whoever that may be). */
    public static PhoenixHarpy hatchFromAshes(ServerLevel level, Vec3 pos) {
        PhoenixHarpy chick = com.linkyoshi90.monstergirls.registry.ModEntities.PHOENIX_HARPY.get().create(level);
        if (chick == null) return null;
        chick.moveTo(pos.x, pos.y, pos.z, level.random.nextFloat() * 360.0F, 0.0F);
        chick.setHatchling(true);
        chick.setVariant(Variant.forBiome(level.getBiome(BlockPos.containing(pos)), level.random));   // reborn in the flame of this place
        level.addFreshEntity(chick);
        level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y + 0.4, pos.z, 40, 0.4, 0.5, 0.4, 0.05);
        level.sendParticles(ParticleTypes.LAVA, pos.x, pos.y + 0.4, pos.z, 8, 0.3, 0.3, 0.3, 0.0);
        chick.playSound(ModSounds.PHOENIX_REBIRTH.get(), 1.5F, 1.0F);
        List<LivingEntity> seen = level.getEntitiesOfClass(LivingEntity.class, chick.getBoundingBox().inflate(16.0),
                e -> e != chick && e.isAlive() && !e.isSpectator() && chick.hasLineOfSight(e));
        seen.stream().min((a, b) -> Double.compare(a.distanceToSqr(chick), b.distanceToSqr(chick))).ifPresent(chick::imprintOn);
        return chick;
    }

    // --- friendship -------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.isHatchling() && ++this.hatchlingAge >= GROW_UP_TICKS) this.setHatchling(false);
        // Soothed by music: a wild phoenix lets go of whoever she was after (unless they're the one hitting her).
        if (this.imprintedOn == null && this.getTarget() instanceof Player target && target != this.getLastHurtByMob()
                && this.tickCount % 20 == 0 && this.isSoothed()) {
            this.setTarget(null);
        }
        if (this.random.nextInt(4) == 0) {
            server.sendParticles(ParticleTypes.FLAME, this.getRandomX(0.6), this.getY() + this.getBbHeight() * (0.4 + 0.6 * this.random.nextDouble()),
                    this.getRandomZ(0.6), 1, 0, 0.02, 0, 0.0);
        }
        Entity friend = this.getImprinted();
        if (friend instanceof LivingEntity buddy && buddy.isAlive() && this.distanceToSqr(buddy) < 24 * 24) {
            this.defend(buddy);
            if (buddy instanceof Player player) {
                this.deflectFireballsAt(server, player);
                this.cryIfNeeded(server, player);
                this.healIfHurt(server, player);
            }
        }
    }

    /** Whoever hurts her friend, or whatever her player friend attacks, is her enemy too. */
    private void defend(LivingEntity friend) {
        if (this.getTarget() != null || this.isHatchling()) return;
        LivingEntity attacker = friend.getLastHurtByMob();
        if (attacker != null && attacker != this && attacker.isAlive() && friend.tickCount - friend.getLastHurtByMobTimestamp() < 100) {
            this.setTarget(attacker);
        } else if (friend instanceof Player player && player.getLastHurtMob() != null && player.getLastHurtMob() != this
                && player.getLastHurtMob().isAlive() && player.tickCount - player.getLastHurtMobTimestamp() < 100) {
            this.setTarget(player.getLastHurtMob());
        }
    }

    /** Any ghast fireball heading for her friend gets swatted back at whoever fired it. */
    private void deflectFireballsAt(ServerLevel level, Player friend) {
        for (LargeFireball fireball : level.getEntitiesOfClass(LargeFireball.class, friend.getBoundingBox().inflate(12.0))) {
            if (fireball.getOwner() == this || fireball.getOwner() == friend) continue;
            Vec3 toFriend = friend.position().subtract(fireball.position());
            if (fireball.getDeltaMovement().dot(toFriend) <= 0) continue;              // not coming our way
            Entity shooter = fireball.getOwner();
            Vec3 aimAt = shooter != null ? shooter.getEyePosition() : fireball.position().subtract(toFriend);
            Vec3 dir = aimAt.subtract(fireball.position());
            this.setYRot((float) (Mth.atan2(dir.z, dir.x) * Mth.RAD_TO_DEG) - 90.0F);
            this.setXRot((float) -(Mth.atan2(dir.y, dir.horizontalDistance()) * Mth.RAD_TO_DEG));
            fireball.deflect(ProjectileDeflection.AIM_DEFLECT, this, this, true);
            this.startTimedAction(Action.STRIKING, 16);
            level.sendParticles(ParticleTypes.FLAME, fireball.getX(), fireball.getY(), fireball.getZ(), 20, 0.3, 0.3, 0.3, 0.05);
        }
    }

    /** Healing tears when her friend is badly hurt. */
    private void cryIfNeeded(ServerLevel level, Player friend) {
        if (level.getGameTime() < this.nextTearTime || friend.getHealth() > friend.getMaxHealth() * 0.5F || this.distanceToSqr(friend) > 8 * 8) return;
        this.nextTearTime = level.getGameTime() + TEAR_COOLDOWN;
        this.startTimedAction(Action.CRYING, 40);
        friend.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1), this);
        level.sendParticles(ParticleTypes.FALLING_WATER, this.getX(), this.getEyeY(), this.getZ(), 12, 0.2, 0.1, 0.2, 0.0);
        level.sendParticles(ParticleTypes.END_ROD, friend.getX(), friend.getY() + 1.0, friend.getZ(), 10, 0.4, 0.6, 0.4, 0.02);
        this.playSound(ModSounds.PHOENIX_CRY.get(), 1.0F, 1.0F);
    }

    // --- death: she leaves her ashes ----------------------------------------------------------------------------

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        var ashes = this.spawnAtLocation(new ItemStack(ModItems.PHOENIX_ASHES.get()));
        if (ashes != null) ashes.setExtendedLifetime();
    }

    @Override
    public boolean isEdible(ItemStack stack) {
        return false;
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return false;
    }

    @Override
    public boolean wantsToPickUp(ItemStack stack) {
        return false;
    }

    @Override
    public Component describeBond(Player player) {
        String level = this.isImprintedOn(player) ? "imprinted" : this.imprintedOn != null ? "devoted_elsewhere" : "hostile";
        return Component.translatable("bond.monstergirls.phoenix." + level, this.getAffection(player));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.imprintedOn != null) tag.putUUID("ImprintedOn", this.imprintedOn);
        tag.putInt("HatchlingAge", this.hatchlingAge);
        tag.putLong("NextTears", this.nextTearTime);
        tag.putString("Variant", this.getVariant().getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.imprintedOn = tag.hasUUID("ImprintedOn") ? tag.getUUID("ImprintedOn") : null;
        this.hatchlingAge = tag.getInt("HatchlingAge");
        this.nextTearTime = tag.getLong("NextTears");
        this.setVariant(Variant.byName(tag.getString("Variant")));
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return this.imprintedOn == null && !this.isHatchling();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.PHOENIX_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.PHOENIX_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.PHOENIX_DEATH.get();
    }

    @Override
    protected SoundEvent getEatSound() {
        return ModSounds.PHOENIX_AMBIENT.get();
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

    /** Hatched from her father's clutch: imprinted on that player from the first moment. */
    @Override
    protected void onBornTo(UUID father) {
        this.imprintedOn = father;
        this.setTarget(null);
    }

    /** Her warmth mends the player she imprinted on: half a heart every two seconds while they are close and hurt. */
    private void healIfHurt(ServerLevel level, Player player) {
        if (this.isHatchling() || this.tickCount % HEAL_INTERVAL != 0 || player.getHealth() >= player.getMaxHealth()
                || this.distanceToSqr(player) > HEAL_RANGE * HEAL_RANGE) return;
        player.heal(1.0F);
        level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + player.getBbHeight() + 0.2, player.getZ(), 1, 0.2, 0.1, 0.2, 0.0);
        level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 4, 0.3, 0.4, 0.3, 0.01);
    }

    public static final int HEAL_INTERVAL = 41;
    public static final double HEAL_RANGE = 8.0;

    @Override
    protected net.minecraft.world.item.ItemStack unfertilisedEgg() {
        return new net.minecraft.world.item.ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.EMBER_EGG.get());
    }
}
