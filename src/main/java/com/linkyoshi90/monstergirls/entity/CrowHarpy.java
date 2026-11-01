package com.linkyoshi90.monstergirls.entity;

import net.minecraft.world.item.JukeboxSong;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.nbt.StringTag;
import com.linkyoshi90.monstergirls.entity.ai.StealDiscGoal;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.ai.AvoidKnownTrapsGoal;
import com.linkyoshi90.monstergirls.entity.ai.BatheGoal;
import com.linkyoshi90.monstergirls.entity.ai.BringToOwnerGoal;
import com.linkyoshi90.monstergirls.entity.ai.CacheItemGoal;
import com.linkyoshi90.monstergirls.entity.ai.CreeperPrankGoal;
import com.linkyoshi90.monstergirls.entity.ai.FollowFriendlyMobGoal;
import com.linkyoshi90.monstergirls.entity.ai.FollowLeaderGoal;
import com.linkyoshi90.monstergirls.entity.ai.KnockOnWoodGoal;
import com.linkyoshi90.monstergirls.entity.ai.OpenDoorsGoal;
import com.linkyoshi90.monstergirls.entity.ai.PickOnMobGoal;
import com.linkyoshi90.monstergirls.entity.ai.PointOutTreasureGoal;
import com.linkyoshi90.monstergirls.entity.ai.RetrieveCacheGoal;
import com.linkyoshi90.monstergirls.entity.ai.ReturnHomeGoal;
import com.linkyoshi90.monstergirls.entity.ai.ScavengeFarmGoal;
import com.linkyoshi90.monstergirls.entity.ai.SeekItemGoal;
import com.linkyoshi90.monstergirls.entity.ai.SnatchGoal;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import com.linkyoshi90.monstergirls.registry.ModItems;
import com.linkyoshi90.monstergirls.registry.ModSounds;
import com.linkyoshi90.monstergirls.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Clever, mischievous woodland/farm harpy. Imitates the mobs around her, knocks on wood, opens doors, pickpockets
 * players and drops the loot further down the road, pranks people with a perfect creeper hiss, gathers in murders
 * to raid crops (unless a scarecrow pumpkin stands guard), remembers traps and grudges (and tells her murder),
 * and trades: give her something shiny and she gives you a little gift back and never steals from you again.
 * Murders sometimes include a hatchling; feed one and it imprints on you: it follows you (sneak + empty hand to
 * make it stay or follow), brings you the shiny things it finds, gives you a gift every day, and grows up after
 * a Minecraft day without forgetting who raised it. A loyal crow also sniffs out unopened treasure chests near
 * her owner and hops up and down on the ground above them. Now and then a crow picks on some other mob: a peck,
 * a cackle, and off she flies. And like real corvids, a wild crow caches: instead of dropping a carried trinket she
 * may bury it in soft ground nearby (dig the block up to find it). If she noticed someone watching, she comes back
 * once they've gone and hides it somewhere else; otherwise she digs it up again after a day or two.
 */
public class CrowHarpy extends AbstractHarpy {
    public static final ResourceKey<LootTable> GIFT_LOOT = ResourceKey.create(Registries.LOOT_TABLE, MonsterGirlsMod.id("gameplay/crow_harpy_gift"));
    private static final Method GET_AMBIENT_SOUND = ObfuscationReflectionHelper.findMethod(Mob.class, "getAmbientSound");
    private static final int MAX_TRAPS = 8;
    private static final long GRUDGE_TICKS = 24000L * 3;
    private static final ResourceLocation HATCHLING_SCALE = MonsterGirlsMod.id("hatchling");
    private static final int GROW_UP_TICKS = 24000;
    /** One in this many crows of a spawning murder (after the first) is a hatchling. */
    private static final int HATCHLING_RARITY = 4;
    /** How many things one crow keeps buried at a time. */
    public static final int MAX_CACHES = 4;
    /** How long she looks for a hiding spot before she gives up and just drops the item. */
    private static final int CACHE_SEARCH_TICKS = 600;

    private final Set<UUID> friends = new HashSet<>();
    private final Map<UUID, Long> grudges = new HashMap<>();
    private final List<BlockPos> knownTraps = new ArrayList<>();
    /** Treasure chests she has already shown her owner. */
    private final Set<Long> shownChests = new HashSet<>();
    /** Where a murder-mate called her to raid a field, and until when the call stands. */
    @Nullable
    private BlockPos scavengeCall;
    private long scavengeCallUntil;
    private int giftTicks;
    @Nullable
    private UUID giftFor;
    @Nullable
    private UUID owner;
    private boolean following = true;
    private int hatchlingAge;
    private long nextOwnerGiftTime;
    private boolean wantsToCache;
    /** Records she has heard on a jukebox and can now "sing" (play back a stretch of) herself. */
    private final List<ResourceLocation> knownSongs = new ArrayList<>();
    private int mimicTicks;
    @Nullable
    private SoundEvent mimicSound;
    private long cacheDeadline;
    /** Where she just moved a cache away from (someone saw it there), so the new spot isn't right next to it. */
    @Nullable
    private BlockPos movedCacheFrom;

    private static final EntityDataAccessor<Byte> DATA_VARIANT =
            SynchedEntityData.defineId(CrowHarpy.class, EntityDataSerializers.BYTE);

    /**
     * Corvid species, each with its own texture from tools/gen_crow_harpy.py (the first is the default crow_harpy.png).
     * Saved by name, synced by ordinal: append new ones at the end.
     */
    public enum Variant {
        CARRION("carrion"), HOODED("hooded"), JACKDAW("jackdaw"), MAGPIE("magpie"), CHOUGH("chough"), PIED("pied");

        private final String name;

        Variant(String name) {
            this.name = name;
        }

        public String getSerializedName() {
            return this.name;
        }

        public static Variant byName(String name) {
            for (Variant v : values()) if (v.name.equals(name)) return v;
            return CARRION;
        }

        /**
         * Hooded crows in the cold north, jackdaws round farms and plains, magpies in woodland, plain carrion crows
         * elsewhere; a murder in any biome now and then turns out to be choughs (red legs) or pied crows.
         */
        public static Variant forBiome(Holder<Biome> biome, RandomSource random) {
            int rare = random.nextInt(12);
            if (rare == 0) return CHOUGH;
            if (rare == 1) return PIED;
            if (biome.is(BiomeTags.IS_MOUNTAIN) || biome.is(BiomeTags.IS_HILL)) return CHOUGH;
            if (biome.is(Tags.Biomes.IS_SAVANNA) || biome.is(Tags.Biomes.IS_DESERT) || biome.is(Tags.Biomes.IS_BADLANDS)
                    || biome.is(BiomeTags.IS_JUNGLE)) return PIED;
            if (biome.is(Tags.Biomes.IS_SNOWY) || biome.is(BiomeTags.IS_TAIGA)) return random.nextInt(3) == 0 ? CARRION : HOODED;
            if (biome.is(Tags.Biomes.IS_PLAINS)) return random.nextInt(3) == 0 ? CARRION : JACKDAW;
            if (biome.is(BiomeTags.IS_FOREST)) return random.nextInt(3) == 0 ? CARRION : MAGPIE;
            return CARRION;
        }
    }

    /** One species per murder: the first crow of a spawning group picks for the biome, the rest follow. */
    private record MurderData(Variant variant) implements SpawnGroupData {}

    public CrowHarpy(EntityType<? extends CrowHarpy> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FLYING_SPEED, 0.45)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    public static boolean checkCrowHarpySpawnRules(EntityType<CrowHarpy> type, ServerLevelAccessor level,
                                                   MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(ModTags.CROW_HARPY_SPAWNABLE_ON) && level.getRawBrightness(pos, 0) > 8;
    }

    @Override
    protected String species() {
        return "crow_harpy";
    }

    @Override
    protected String actionAnimation(Action action) {
        return switch (action) {
            case BATHING -> "bathe";
            case CAWING -> "caw";
            case KNOCKING -> "knock";
            case PRANKING -> "prank";
            case CACHING -> "cache";
            default -> null;
        };
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new OpenDoorsGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.3));
        this.goalSelector.addGoal(1, new AvoidKnownTrapsGoal(this, 1.2));
        // Crows never forget a face: keep away from anyone who hurt her or her murder.
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 10.0F, 1.0, 1.3, this::holdsGrudgeAgainst));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Creeper.class, 8.0F, 1.0, 1.3));
        this.goalSelector.addGoal(3, new BringToOwnerGoal(this, 1.2));
        this.goalSelector.addGoal(3, new CacheItemGoal(this, 1.1));
        this.goalSelector.addGoal(3, new SnatchGoal(this, 1.3, SnatchGoal.From.INVENTORY, 2400, 6000,
                p -> this.owner == null && !this.isHatchling() && !this.isFriend(p) && !this.holdsGrudgeAgainst(p)));
        this.goalSelector.addGoal(3, new CreeperPrankGoal(this, 0.9));
        this.goalSelector.addGoal(3, new StealDiscGoal(this, 1.2));
        this.goalSelector.addGoal(3, new PointOutTreasureGoal(this, 1.1));
        this.goalSelector.addGoal(4, new PickOnMobGoal(this, 1.2));
        this.goalSelector.addGoal(4, new SeekItemGoal(this, 1.1, 10.0));
        this.goalSelector.addGoal(4, new RetrieveCacheGoal(this, 1.1));
        this.goalSelector.addGoal(4, new ReturnHomeGoal(this, 1.0, 12.0));
        // Below item seeking, so a loyal crow on her owner's "leash" can still fetch things a little further out.
        this.goalSelector.addGoal(5, new FollowLeaderGoal(this, () -> this.following ? this.getOwner() : null, 1.2, 10.0F, 3.0F));
        this.goalSelector.addGoal(5, new ScavengeFarmGoal(this, 1.0));
        this.goalSelector.addGoal(5, new BatheGoal(this, 1.0));
        this.goalSelector.addGoal(6, new KnockOnWoodGoal(this, 1.0));
        this.goalSelector.addGoal(7, new FollowFriendlyMobGoal(this, 1.0, 4.0F, 16.0F, (self, other) -> other.getType() == self.getType()));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    @Override
    protected boolean opensDoors() {
        return true; // OpenDoorsGoal does the actual opening
    }

    @Override
    public boolean isEdible(ItemStack stack) {
        // Crows eat anything: any food at all, plus seeds and crops.
        return stack.has(DataComponents.FOOD) || stack.is(ModTags.CROW_HARPY_FOOD);
    }

    @Override
    public boolean likesToCarry(ItemStack stack) {
        return stack.is(ModTags.CROW_HARPY_SHINIES);
    }

    @Override
    public boolean wantsItem(ItemStack stack) {
        return super.wantsItem(stack) && this.giftTicks <= 0;
    }

    // --- hatchlings and owners -------------------------------------------------------------------------------

    public boolean isHatchling() {
        AttributeInstance scale = this.getAttribute(Attributes.SCALE);
        return scale != null && scale.hasModifier(HATCHLING_SCALE);
    }

    /** Hatchlings are the same crow at a bit over half size (SCALE also shrinks the hitbox and the rendered model). */
    public void setHatchling(boolean hatchling) {
        AttributeInstance scale = this.getAttribute(Attributes.SCALE);
        if (scale == null) return;
        if (hatchling) {
            scale.addOrReplacePermanentModifier(new AttributeModifier(HATCHLING_SCALE, -0.45, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            this.hatchlingAge = 0;
        } else {
            scale.removeModifier(HATCHLING_SCALE);
        }
    }

    @Nullable
    public Player getOwner() {
        return this.owner == null ? null : this.level().getPlayerByUUID(this.owner);
    }

    public boolean isOwnedBy(Player player) {
        return player.getUUID().equals(this.owner);
    }

    public boolean isFollowingOwner() {
        return this.owner != null && this.following;
    }

    /** Imprints on whoever feeds her as a hatchling. */
    public void imprintOn(Player player) {
        this.owner = player.getUUID();
        this.following = true;
        this.friends.add(player.getUUID());
        this.grudges.remove(player.getUUID());
        this.setHomePos(null);
        this.setPersistenceRequired();
        this.nextOwnerGiftTime = this.level().getGameTime() + 24000;
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight() + 0.2, this.getZ(), 5, 0.3, 0.2, 0.3, 0.0);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
                                        @Nullable SpawnGroupData groupData) {
        boolean joinsMurder = groupData instanceof MurderData;
        Variant variant;
        if (groupData instanceof MurderData murder) {
            variant = murder.variant();
        } else {
            variant = Variant.forBiome(level.getBiome(this.blockPosition()), this.random);
            groupData = new MurderData(variant);
        }
        if (!this.hasVariantFromData()) this.setVariant(variant);
        if (joinsMurder && this.random.nextInt(HATCHLING_RARITY) == 0) {
            this.setHatchling(true);
        }
        return super.finalizeSpawn(level, difficulty, spawnType, groupData);
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

    // --- friends, grudges, traps ----------------------------------------------------------------------------

    public boolean isFriend(Player player) {
        return this.friends.contains(player.getUUID());
    }

    public boolean holdsGrudgeAgainst(LivingEntity entity) {
        Long until = this.grudges.get(entity.getUUID());
        return until != null && until > this.level().getGameTime();
    }

    public void addGrudge(UUID who) {
        this.grudges.put(who, this.level().getGameTime() + GRUDGE_TICKS);
        this.friends.remove(who);
    }

    public List<BlockPos> getKnownTraps() {
        return this.knownTraps;
    }

    public void rememberTrap(BlockPos pos) {
        BlockPos p = pos.immutable();
        if (this.isNearKnownTrap(p, 1.0)) return;
        this.knownTraps.add(p);
        if (this.knownTraps.size() > MAX_TRAPS) this.knownTraps.remove(0);
    }

    public boolean isNearKnownTrap(BlockPos pos, double radius) {
        for (BlockPos trap : this.knownTraps) {
            if (trap.distSqr(pos) <= radius * radius) return true;
        }
        return false;
    }

    @Override
    public boolean isSafeSpot(BlockPos pos) {
        return !this.isNearKnownTrap(pos, 2.5);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && this.level() instanceof ServerLevel server) {
            Entity attacker = source.getEntity();
            if (attacker instanceof Player player && !this.isOwnedBy(player)) {
                // ...and tells the rest of the murder.
                this.addGrudge(player.getUUID());
                for (CrowHarpy crow : server.getEntitiesOfClass(CrowHarpy.class, this.getBoundingBox().inflate(24.0))) {
                    crow.addGrudge(player.getUUID());
                }
                this.caw(40);
            } else if (attacker == null && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                // Hurt by the world itself (cactus, berry bush, fire, a dispenser's arrow...): that spot is a trap.
                this.rememberTrap(this.blockPosition());
            }
        }
        return hurt;
    }

    // --- murders --------------------------------------------------------------------------------------------

    /** Tells every crow nearby to come and raid this field too. */
    public void callMurder(BlockPos field) {
        for (CrowHarpy crow : this.level().getEntitiesOfClass(CrowHarpy.class, this.getBoundingBox().inflate(24.0))) {
            if (crow != this) {
                crow.scavengeCall = field.immutable();
                crow.scavengeCallUntil = this.level().getGameTime() + 400;
            }
        }
    }

    @Nullable
    public BlockPos takeScavengeCall() {
        BlockPos call = this.level().getGameTime() < this.scavengeCallUntil ? this.scavengeCall : null;
        this.scavengeCall = null;
        return call;
    }

    // --- caws, imitation, timed animations ---------------------------------------------------------------------

    public void caw(int ticks) {
        this.playSound(ModSounds.CROW_CAW.get(), 1.2F, ModSounds.wobble(this));
        this.startTimedAction(Action.CAWING, ticks);
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240; // vanilla's 80 made her caw (and play the caw animation) all the time
    }

    @Override
    protected void onPetted(Player player) {
        this.playSound(ModSounds.CROW_GIGGLE.get(), 0.8F, 1.2F);
        this.startTimedAction(Action.CAWING, 20);
    }

    @Override
    public Component describeBond(Player player) {
        int affection = this.getAffection(player);
        String level;
        if (this.isOwnedBy(player)) level = "companion";
        else if (this.holdsGrudgeAgainst(player)) level = "grudge";
        else if (this.isFriend(player)) level = "friend";
        else level = affection >= 3 ? "used_to" : "wary";
        return Component.translatable("bond.monstergirls.crow." + level, affection);
    }

    public boolean hasShownChest(BlockPos pos) {
        return this.shownChests.contains(pos.asLong());
    }

    public void markChestShown(BlockPos pos) {
        this.shownChests.add(pos.asLong());
    }

    public void laugh() {
        this.playSound(ModSounds.CROW_LAUGH.get(), 1.0F, ModSounds.wobble(this));
    }

    // --- music ------------------------------------------------------------------------------------------------

    private static final int MAX_SONGS = 6;

    public List<ResourceLocation> getKnownSongs() {
        return this.knownSongs;
    }

    /** Hearing a record once is enough for her to learn it. */
    private void learnSong(ResourceLocation song) {
        if (this.knownSongs.contains(song)) return;
        this.knownSongs.add(song);
        if (this.knownSongs.size() > MAX_SONGS) this.knownSongs.remove(0);
    }

    public boolean isMimickingSong() {
        return this.mimicTicks > 0;
    }

    /** Plays back a few seconds of a record she learned, eerily well, then breaks off. */
    public boolean mimicSong() {
        if (this.knownSongs.isEmpty() || this.mimicTicks > 0) return false;
        ResourceLocation id = this.knownSongs.get(this.random.nextInt(this.knownSongs.size()));
        var song = this.level().registryAccess().registryOrThrow(Registries.JUKEBOX_SONG).getHolder(ResourceKey.create(Registries.JUKEBOX_SONG, id));
        if (song.isEmpty()) return false;
        var sound = song.get().value().soundEvent();
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), sound, this.getSoundSource(), 1.0F, 1.0F);
        this.mimicSound = sound.value();
        this.mimicTicks = 80 + this.random.nextInt(60);
        this.startTimedAction(Action.CAWING, this.mimicTicks);
        return true;
    }

    /** Records play on and on; cut her rendition off for everyone in earshot. */
    private void stopMimic(ServerLevel level) {
        if (this.mimicSound == null) return;
        ClientboundStopSoundPacket stop = new ClientboundStopSoundPacket(this.mimicSound.getLocation(), this.getSoundSource());
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this) < 96 * 96) player.connection.send(stop);
        }
        this.mimicSound = null;
    }

    @Override
    public void playAmbientSound() {
        if (this.mimicTicks > 0) return;                        // mid-song
        if (this.random.nextInt(5) == 0 && this.getMusicNearby() == null && this.mimicSong()) return;
        if (this.random.nextInt(3) == 0 && this.imitateNearbyMob()) return;
        this.caw(30);
    }

    /** Copies the ambient sound of a random mob nearby, almost flawlessly. */
    private boolean imitateNearbyMob() {
        List<Mob> mobs = this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(16.0),
                m -> !(m instanceof AbstractHarpy) && m.isAlive() && !m.isSilent());
        if (mobs.isEmpty()) return false;
        Mob model = mobs.get(this.random.nextInt(mobs.size()));
        try {
            SoundEvent sound = (SoundEvent) GET_AMBIENT_SOUND.invoke(model);
            if (sound == null) return false;
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), sound, this.getSoundSource(), 1.0F,
                    0.95F + this.random.nextFloat() * 0.1F);
            this.startTimedAction(Action.CAWING, 20);
            return true;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) return;
        if (this.giftTicks > 0 && --this.giftTicks == 0) this.giveGift(server);
        if (this.wantsToCache && !this.isHoldingSomething()) this.wantsToCache = false; // dropped it (startled, say)
        if (this.mimicTicks > 0 && --this.mimicTicks == 0) this.stopMimic(server);
        if (this.tickCount % 40 == 0) {
            ResourceKey<JukeboxSong> song = JukeboxSensing.songAt(server, this.getMusicNearby());
            if (song != null) this.learnSong(song.location());
        }
        if (this.isHatchling() && ++this.hatchlingAge >= GROW_UP_TICKS) {
            this.setHatchling(false);
            this.caw(40);
        }
        // A loyal crow brings her owner a little something every day.
        Player owner = this.getOwner();
        if (owner != null && this.giftTicks <= 0 && server.getGameTime() >= this.nextOwnerGiftTime && this.distanceToSqr(owner) < 12 * 12) {
            this.nextOwnerGiftTime = server.getGameTime() + 24000;
            this.giftFor = owner.getUUID();
            this.giftTicks = 20;
        }
        // Mobbing: scold a grudge target she can see.
        if (this.tickCount % 50 == 0) {
            Player enemy = server.getNearestPlayer(this.getX(), this.getY(), this.getZ(), 16.0,
                    e -> e instanceof Player p && this.holdsGrudgeAgainst(p) && this.hasLineOfSight(p));
            if (enemy != null) {
                this.getLookControl().setLookAt(enemy);
                this.caw(30);
            }
        }
    }

    // --- caches -----------------------------------------------------------------------------------------------

    public boolean wantsToCache() {
        return this.wantsToCache;
    }

    /** Makes her go and bury what she's holding (CacheItemGoal); she gives up after a while if there's nowhere to dig. */
    public void startCaching() {
        this.wantsToCache = true;
        this.cacheDeadline = this.level().getGameTime() + CACHE_SEARCH_TICKS;
    }

    public boolean shouldAvoidCachingNear(BlockPos ground) {
        return this.movedCacheFrom != null && this.movedCacheFrom.distSqr(ground) < 6 * 6;
    }

    /** Anyone (but her owner) close by who can see her. */
    private boolean isBeingWatched() {
        for (Player player : this.level().players()) {
            if (!player.isSpectator() && !this.isOwnedBy(player) && this.distanceToSqr(player) < 16 * 16 && player.hasLineOfSight(this)) return true;
        }
        return false;
    }

    private boolean anyoneNear(BlockPos pos, double radius) {
        for (Player player : this.level().players()) {
            if (!player.isSpectator() && !this.isOwnedBy(player) && player.distanceToSqr(pos.getBottomCenter()) < radius * radius) return true;
        }
        return false;
    }

    /** Puts what she holds into the ground block, remembering whether someone saw her do it. */
    public void buryHeldItem(BlockPos ground) {
        if (!(this.level() instanceof ServerLevel level) || !this.isHoldingSomething()) return;
        long retrieveAt = level.getGameTime() + 24000 + this.random.nextInt(24000);
        CrowCaches.get(level).add(new CrowCaches.Cache(ground.immutable(), this.getMainHandItem().copy(), this.getUUID(), retrieveAt,
                this.isBeingWatched()));
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.wantsToCache = false;
        this.movedCacheFrom = null;
        BlockState state = level.getBlockState(ground);
        this.playSound(state.getSoundType(level, ground, this).getPlaceSound(), 0.8F, 1.0F);
    }

    /** Her own cache that wants seeing to: someone watched her bury it and has gone now, or it's been there long enough. */
    @Nullable
    public CrowCaches.Cache findDueCache() {
        if (!(this.level() instanceof ServerLevel level)) return null;
        for (CrowCaches.Cache cache : CrowCaches.get(level).of(this.getUUID())) {
            if (cache.pos().distSqr(this.blockPosition()) > 48 * 48) continue;
            if (cache.watched() ? !this.anyoneNear(cache.pos(), 20.0) : level.getGameTime() >= cache.retrieveAt()) return cache;
        }
        return null;
    }

    /** Takes the item back out of the ground; a cache someone saw gets hidden again right away, somewhere else. */
    public void digUpCache(CrowCaches.Cache cache) {
        if (!(this.level() instanceof ServerLevel level)) return;
        CrowCaches.get(level).remove(cache.pos());
        this.takeIntoHands(cache.item().copy());
        if (cache.watched()) {
            this.movedCacheFrom = cache.pos();
            this.startCaching();
        }
    }

    public int countCaches() {
        return this.level() instanceof ServerLevel level ? CrowCaches.get(level).of(this.getUUID()).size() : 0;
    }

    @Override
    protected void onCarriedLongEnough() {
        if (this.owner != null && this.likesToCarry(this.getMainHandItem())) return; // BringToOwnerGoal delivers it
        if (this.wantsToCache) {
            if (this.level().getGameTime() < this.cacheDeadline) return;           // CacheItemGoal is on it
            this.wantsToCache = false;                                             // nowhere to dig: just leave it
        } else if (this.owner == null && !this.isHatchling() && this.random.nextBoolean() && this.countCaches() < MAX_CACHES) {
            this.startCaching();                                                   // this one is a keeper
            return;
        }
        // Down the road: drop the loot with a giggle, then forget about it.
        this.dropHeldItem();
        this.playSound(ModSounds.CROW_GIGGLE.get(), 1.0F, ModSounds.wobble(this));
        this.startTimedAction(Action.PRANKING, 60);
    }

    @Override
    protected int carryTicksFor(ItemStack stack) {
        return 200 + this.random.nextInt(400);
    }

    // --- trading --------------------------------------------------------------------------------------------

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.isOwnedBy(player) && player.isShiftKeyDown() && stack.isEmpty()) {
            if (!this.level().isClientSide) {
                this.following = !this.following;
                this.setHomePos(this.following ? null : this.blockPosition());
                player.displayClientMessage(Component.translatable(this.following ? "entity.monstergirls.crow_harpy.follow" : "entity.monstergirls.crow_harpy.stay",
                        this.getDisplayName()), true);
                this.caw(20);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (stack.is(ModItems.CROW_HARPY_SPAWN_EGG.get())) {
            // Like vanilla baby animals: a spawn egg used on a crow hatches a hatchling.
            if (this.level() instanceof ServerLevel server) {
                CrowHarpy chick = ModEntities.CROW_HARPY.get().create(server);
                if (chick != null) {
                    chick.setHatchling(true);
                    chick.setVariant(this.getVariant());        // same species as the crow it hatched from
                    chick.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                    server.addFreshEntity(chick);
                    stack.consume(1, player);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.isHatchling() && this.owner == null && this.isEdible(stack)) {
            if (!this.level().isClientSide) {
                stack.consume(1, player);
                this.playSound(this.getEatSound(), 1.0F, 1.2F);
                this.imprintOn(player);
                player.displayClientMessage(Component.translatable("entity.monstergirls.crow_harpy.imprinted", this.getDisplayName()), true);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        boolean shiny = this.likesToCarry(stack), food = this.isEdible(stack);
        if (!shiny && !food || this.giftTicks > 0) return super.mobInteract(player, hand);
        if (!this.level().isClientSide) {
            stack.consume(1, player);
            this.playSound(this.getEatSound(), 1.0F, ModSounds.wobble(this));
            if (shiny) {
                // A fair trade makes a friend: she'll bring something back and won't pickpocket you any more.
                this.friends.add(player.getUUID());
                this.grudges.remove(player.getUUID());
                this.addAffection(player, 2);
                this.giftFor = player.getUUID();
                this.giftTicks = 40;
                this.startTimedAction(Action.PRANKING, 40);
            } else {
                this.heal(3.0F);
                this.addAffection(player, 1);
                this.caw(20);
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    private void giveGift(ServerLevel level) {
        Player player = this.giftFor == null ? null : level.getPlayerByUUID(this.giftFor);
        this.giftFor = null;
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, this.position())
                .withParameter(LootContextParams.THIS_ENTITY, this)
                .create(LootContextParamSets.GIFT);
        Vec3 toward = player == null ? Vec3.ZERO : player.position().subtract(this.position()).normalize().scale(0.25);
        for (ItemStack gift : level.getServer().reloadableRegistries().getLootTable(GIFT_LOOT).getRandomItems(params)) {
            ItemEntity item = new ItemEntity(level, this.getX(), this.getEyeY() - 0.3, this.getZ(), gift);
            item.setDeltaMovement(toward.x, 0.2, toward.z);
            item.setDefaultPickUpDelay();
            level.addFreshEntity(item);
        }
        this.caw(30);
    }

    // --- save -----------------------------------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ListTag friendList = new ListTag();
        this.friends.forEach(uuid -> friendList.add(NbtUtils.createUUID(uuid)));
        tag.put("Friends", friendList);
        CompoundTag grudgeTag = new CompoundTag();
        this.grudges.forEach((uuid, until) -> grudgeTag.putLong(uuid.toString(), until));
        tag.put("Grudges", grudgeTag);
        ListTag traps = new ListTag();
        this.knownTraps.forEach(p -> traps.add(LongTag.valueOf(p.asLong())));
        tag.put("KnownTraps", traps);
        tag.putLongArray("ShownChests", this.shownChests.stream().mapToLong(Long::longValue).toArray());
        if (this.owner != null) tag.putUUID("Owner", this.owner);
        tag.putBoolean("Following", this.following);
        tag.putInt("HatchlingAge", this.hatchlingAge);
        tag.putLong("NextOwnerGift", this.nextOwnerGiftTime);
        tag.putString("Variant", this.getVariant().getSerializedName());
        tag.putBoolean("WantsToCache", this.wantsToCache);
        ListTag songs = new ListTag();
        this.knownSongs.forEach(song -> songs.add(StringTag.valueOf(song.toString())));
        tag.put("KnownSongs", songs);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.friends.clear();
        for (Tag t : tag.getList("Friends", Tag.TAG_INT_ARRAY)) this.friends.add(NbtUtils.loadUUID(t));
        this.grudges.clear();
        CompoundTag grudgeTag = tag.getCompound("Grudges");
        for (String key : grudgeTag.getAllKeys()) this.grudges.put(UUID.fromString(key), grudgeTag.getLong(key));
        this.knownTraps.clear();
        for (Tag t : tag.getList("KnownTraps", Tag.TAG_LONG)) this.knownTraps.add(BlockPos.of(((LongTag) t).getAsLong()));
        this.shownChests.clear();
        for (long chest : tag.getLongArray("ShownChests")) this.shownChests.add(chest);
        this.owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        this.following = !tag.contains("Following") || tag.getBoolean("Following");
        this.hatchlingAge = tag.getInt("HatchlingAge");
        this.nextOwnerGiftTime = tag.getLong("NextOwnerGift");
        this.setVariant(Variant.byName(tag.getString("Variant")));
        if (tag.getBoolean("WantsToCache")) this.startCaching();
        this.knownSongs.clear();
        for (Tag t : tag.getList("KnownSongs", Tag.TAG_STRING)) {
            ResourceLocation song = ResourceLocation.tryParse(t.getAsString());
            if (song != null) this.knownSongs.add(song);
        }
    }

    // --- sounds ---------------------------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.CROW_CAW.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.CROW_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.CROW_DEATH.get();
    }

    @Override
    protected SoundEvent getEatSound() {
        return ModSounds.CROW_EAT.get();
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.NEUTRAL;
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

    /** Hatched from her father's clutch: she is that player's companion from the first day, as if she had imprinted on them. */
    @Override
    protected void onBornTo(UUID father) {
        this.owner = father;
        this.following = true;
        this.friends.add(father);
        this.setHomePos(null);
        this.nextOwnerGiftTime = this.level().getGameTime() + 24000;
    }

    /** {@inheritDoc} */
    @Override
    public ItemStack rollGift(Player player) {
        return HarpyGifts.crow(this.random);
    }

    /** A crow puts her finds away: into the nearest chest (or barrel, or hopper) that already holds the same thing. */
    @Override
    public boolean storesGiftsAway() {
        return true;
    }

    @Override
    protected net.minecraft.world.item.ItemStack unfertilisedEgg() {
        return new net.minecraft.world.item.ItemStack(com.linkyoshi90.monstergirls.registry.ModItems.CROW_EGG.get());
    }
}
