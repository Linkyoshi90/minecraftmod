package com.linkyoshi90.harmontide.entity.girl;

import com.linkyoshi90.harmontide.entity.Bonded;
import com.linkyoshi90.harmontide.entity.Gestation;
import com.linkyoshi90.harmontide.entity.GiftGiver;
import com.linkyoshi90.harmontide.entity.ai.GiftGoal;
import com.linkyoshi90.harmontide.entity.ai.ShareBedGoal;
import com.linkyoshi90.harmontide.entity.ai.SitWhenOrderedGoal;
import com.linkyoshi90.harmontide.registry.ModTags;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.phys.Vec3;
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
public abstract class AbstractMonsterGirl extends PathfinderMob implements GeoEntity, Bonded, GiftGiver, com.linkyoshi90.harmontide.entity.Painted,
        com.linkyoshi90.harmontide.entity.Voices.Voiced {
    private static final EntityDataAccessor<Byte> DATA_ACTION = SynchedEntityData.defineId(AbstractMonsterGirl.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DATA_YOUNG = SynchedEntityData.defineId(AbstractMonsterGirl.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> DATA_VARIANT = SynchedEntityData.defineId(AbstractMonsterGirl.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_BELLY = SynchedEntityData.defineId(AbstractMonsterGirl.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_PAINT = SynchedEntityData.defineId(AbstractMonsterGirl.class, EntityDataSerializers.INT);
    /** The Harmontide paints (textures/entity/&lt;model&gt;_paint_&lt;name&gt;.png, tools/gen_paints.py); 0 is unpainted. */
    public static final java.util.List<String> PAINTS = java.util.List.of("blue", "red", "green", "yellow", "purple", "black", "inverted",
            "chocolate", "icecream", "strawberry", "jello", "milk",           // treats: drips, patterns, see-through jello
            "gold", "marble", "galaxy", "ghost", "honey", "rainbow",           // finishes: statues, a night sky, a see-through ghost (append only: synced by index)
            "copper", "copper_exposed", "copper_weathered", "copper_oxidized",   // copper weathers stage by stage (Painted)
            "glow", "ink", "magma", "pumpkin", "candy_cane",                   // glow and magma glow in the dark; pumpkin and candy cane are seasonal
            "frost", "sakura", "stained_glass", "sepia",                       // stained glass is see-through
            "opal", "chrome", "aurora", "camo",
            "plush", "pixel", "origami", "gummy",                              // gummy is see-through
            "synthwave", "cookie", "cloud", "valentine",                       // valentine is seasonal, February only
            "porcelain", "denim", "moss", "easter",                            // easter is seasonal, April only
            "tartan", "watercolour", "hologram", "fireworks",                  // hologram is see-through; fireworks is seasonal, January only
            "mosaic", "chalk", "leopard", "autumn",                            // autumn is seasonal, November only
            "checkered", "sunset", "thunder", "burlap",                        // thunder's bolts glow
            "neon", "zebra", "terrazzo", "clover",                           // neon glows; clover is seasonal, March only
            "polka", "circuit", "giraffe", "watermelon",                     // circuit glows; watermelon is seasonal, July only
            "cow", "rust", "lava_lamp", "sunflower",                         // lava lamp's blobs glow; sunflower is seasonal, August only
            "comic", "blueprint", "koi", "lemonade",                         // lemonade is seasonal, June only
            "tie_dye", "jade", "lace", "bluebell",                           // bluebell is seasonal, May only
            "obsidian", "patchwork", "sandstone", "acorn",                   // acorn is seasonal, September only
            "prismarine", "amethyst", "sculk", "mushroom",                   // Minecraft's blocks, tiled; sculk's specks glow
            "lapis", "bookshelf", "glazed", "redstone_lamp",                 // the lamp's light glows
            "netherite", "purpur", "crying_obsidian", "shroomlight",         // the tears and the shroomlight's light glow
            "end_stone", "hay_bale", "ancient_debris", "sea_lantern",        // the sea lantern glows
            "redstone", "glowstone", "cherry", "terracotta",                 // redstone's bright lines and the glowstone glow
            "tnt", "warped", "dripstone", "brain_coral", "crimson", "bone", "bamboo", "sponge", "soul_sand", "nether_wart", "mycelium", "basalt", "blue_ice", "chorus", "calcite", "tuff", "mud_brick", "deepslate", "oak", "gilded");
    /** Homage variants (the look of a favourite character): a wild one rolls them only rarely. */
    public static final java.util.Set<String> HOMAGES = java.util.Set.of("gunman", "blossom", "lost", "stitched", "nurse", "mummy", "ranger", "outback",
            "sun_wolf", "charm_fox", "fight_panda", "storm_tiger", "boots_cat", "red_tide", "shadow_ranger", "bridge_keeper",
            "jewel_thief", "ink_kid", "pink_dairy", "sun_goddess",                            // POP_CULTURE_VARIANTS.md
            "octo_agent", "ninja_chameleon", "shell_trooper", "town_clerk", "scythe_green", "leaf_sprout", "mud_fin", "trident_hood",
            "innkeeper", "prankster", "soul_keeper", "sage_princess", "green_hero", "tree_guard", "snow_wraith", "headless_rider", "death_envoy",
            "bake_sale", "twin_tails", "ember_tail", "spike_king", "lawn_zombie", "drop_smile", "puffball", "hoodie_squirrel", "flight_cap",
            "lamp_friend", "tiny_jumper", "kaiju_moth", "pocket_bot", "nine_cream", "shopkeeper", "shy_ghost", "night_heir",
            "detective_croc", "bandicoot", "needle_runner", "angel_guard", "music_box", "star_song", "tactical_eye",
            "blue_blur", "royal_flame", "backpack_bear", "aura_guardian", "gold_ninja", "night_heiress", "copy_blob",
            "spark_mouse", "green_dino", "doctor_deer", "purple_flame", "night_wing", "forest_keeper", "sleepy_giant", "first_fox",
            "needle_knight", "spore_retainer", "lasagna_cat", "sly_fox", "carrot_bunny", "web_hero", "blue_heeler", "swamp_king",
            "sea_witch", "calypso_crab", "moon_warrior", "frozen_queen", "pride_queen", "wish_dragon", "night_warden", "ice_fox");
    /** Homage looks with a model of their own (geo, animations and texture &lt;species&gt;_&lt;variant&gt;), not just a recolour. */
    /** Looks a girl comes to by being fed, never met in the wild. */
    private static final java.util.Set<String> EARNED = java.util.Set.of("cured", "sated");
    public static final java.util.Set<String> OWN_MODELS = java.util.Set.of("mud_fin", "trident_hood", "tactical_eye");
    /** One wild girl in this many is painted, like a shiny. */
    private static final EntityDataAccessor<Integer> DATA_FLAGS = SynchedEntityData.defineId(AbstractMonsterGirl.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> DATA_TAILS = SynchedEntityData.defineId(AbstractMonsterGirl.class, EntityDataSerializers.BYTE);
    /** What she is wearing over her own clothes (entity/Wardrobe.Outfit ordinal + 1; 0 for nothing). */
    private static final EntityDataAccessor<Byte> DATA_OUTFIT = SynchedEntityData.defineId(AbstractMonsterGirl.class, EntityDataSerializers.BYTE);
    /** What she is aiming (GirlArms.RangedGoal), for the client's aiming pose (client.AimPose). */
    private static final EntityDataAccessor<Byte> DATA_AIM = SynchedEntityData.defineId(AbstractMonsterGirl.class, EntityDataSerializers.BYTE);
    public static final int AIM_NONE = 0, AIM_BOW = 1, AIM_CROSSBOW = 2, AIM_GUN = 3;
    /** The entity event of a shot she fires: the client's recoil and bow release. */
    public static final byte EVENT_SHOT = 101;
    private int lastShotTick = Integer.MIN_VALUE / 2;
    private static final Map<String, RawAnimation> ANIMATIONS = new ConcurrentHashMap<>();

    /** Synced flags (bits of DATA_FLAGS). */
    private int blushTicks, towelTicks, harnessTicks;
    public static final int FLAG_TRUE_FORM = 1, FLAG_HIDDEN = 2, FLAG_DECOY = 4, FLAG_HALF = 8, FLAG_CARRYING = 16, FLAG_SADDLED = 32, FLAG_CHEST = 64,
            FLAG_CALM = 128, FLAG_SHELLS = 256, FLAG_SMILE = 512, FLAG_MALE = 1024, FLAG_RETRACTED = 2048, FLAG_LIT = 4096, FLAG_MASKED = 8192,
            FLAG_MERCHANT = 16384, FLAG_TOWING = 32768, FLAG_MOONSTRUCK = 65536, FLAG_INJURED = 131072, FLAG_WAXED = 262144,
            FLAG_TAILLESS = 524288, FLAG_UNWRAP_A = 1048576, FLAG_UNWRAP_B = 2097152, FLAG_SHORN = 4194304, FLAG_BLINDFOLD = 8388608,
            FLAG_BLUSHING = 16777216, FLAG_HARNESSED = 33554432, FLAG_PRUNED = 67108864, FLAG_TOWEL = 134217728, FLAG_VEIL = 268435456,
            FLAG_HEADLESS = 536870912;
    /** The flags that are saved (the others are moment-to-moment state). */
    private static final int SAVED_FLAGS = FLAG_TRUE_FORM | FLAG_SADDLED | FLAG_CHEST | FLAG_HALF | FLAG_CALM | FLAG_SHELLS | FLAG_MALE | FLAG_MASKED | FLAG_MERCHANT
            | FLAG_INJURED | FLAG_WAXED | FLAG_TAILLESS | FLAG_UNWRAP_A | FLAG_UNWRAP_B | FLAG_SHORN;
    private static final net.minecraft.resources.ResourceLocation INJURED_SLOW = com.linkyoshi90.harmontide.HarmontideMod.id("injured");

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
        SPIN("spin", false), KICK("kick", true), GRAZE("graze", false), SING("sing", false), ABSORB("absorb", true), SPLIT("split", true),
        STALK("stalk", false), BOUNCE("bounce", false), PANT("pant", false), CHARGE("charge", false), MILK("milk", false), GRAB("grab", false), INK("ink", true), RETRACT("retract", true), LASH("lash", true), SHOOT("shoot", true), TRADE("trade", true), DRINK("drink", false), ROLL("roll", false), CALL("call", true), STING("sting", true), BREATHE("breathe", true), CHARM("charm", false), HOP("hop", false), EAT("eat", false), HEAL("heal", true), POLLINATE("pollinate", false), SCREAM("scream", true), PUFF("puff", true), PLANT("plant", true),
        HOWL("howl", true), FISH("fish", false), SNEEZE("sneeze", true), JUDGE("judge", true), CAST("cast", true), BITE("bite", true),
        POINT("point", true), SOAK("soak", false), SCAMPER("scamper", false),
        PINCH("pinch", true), CURL("curl", false), SLAM("slam", true), CLING("cling", false),
        ROOST("roost", false), PRAY("pray", false), STRIKE("strike", true), SNAP("snap", true), PERCH("perch", false),
        HANG("hang", false), RUNDOWN("rundown", false), WATCH("watch", true),
        PLAY("play", false), DANCE("dance", false),
        /** Sitting with her baby held to her chest (or, for the baby, held there): the sit animation with the cradle pose over it. */
        NURSE("sit", false);

        public final String animation;
        public final boolean once;

        Action(String animation, boolean once) {
            this.animation = animation;
            this.once = once;
        }
    }

    protected final GirlSpecies species;
    /** The hitboxes of a rearing serpent's body; none on anyone else. */
    private final SerpentSegment[] segments;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final Map<UUID, Integer> affection = new HashMap<>();
    private final Gestation gestation = new Gestation();
    @Nullable
    private UUID father;
    private boolean orderedToSit;
    private final com.linkyoshi90.harmontide.entity.Orders orders = new com.linkyoshi90.harmontide.entity.Orders();
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
    /** A TaCZ gun's ammunition, given by her friend (GirlArms); TaCZ reloads from it through her item-handler capability. */
    private final net.minecraft.world.SimpleContainer ammoPouch = new net.minecraft.world.SimpleContainer(9);
    /** Her home (Settlement): the hearth she lives at, since when, and the hut she builds near it. */
    @Nullable
    private BlockPos homeHearth;
    private long settledSince;
    @Nullable
    private BlockPos hutOrigin;
    private net.minecraft.core.Direction hutDoor = net.minecraft.core.Direction.NORTH;
    private int hutStep;
    /** Called home by the hearth's bell (HearthBlockEntity.bell): till this game time she comes to the hearth and stays near it. Not saved. */
    private long calledHomeUntil = -1L;

    protected AbstractMonsterGirl(EntityType<? extends AbstractMonsterGirl> type, Level level, GirlSpecies species) {
        super(type, level);
        this.species = species;
        this.segments = SerpentSegment.of(this, species);
        if (this.segments.length > 0) this.setId(ENTITY_COUNTER.getAndAdd(this.segments.length + 1) + 1);
        this.goalSelector.addGoal(-1, new SitWhenOrderedGoal<>(this));
        this.goalSelector.addGoal(1, new com.linkyoshi90.harmontide.entity.ai.StayNearGoal<>(this));
        this.goalSelector.addGoal(2, new com.linkyoshi90.harmontide.entity.ai.FetchBallGoal<>(this));
        this.goalSelector.addGoal(3, new com.linkyoshi90.harmontide.entity.ai.SeekMilkingMachineGoal<>(this));
        this.goalSelector.addGoal(4, new com.linkyoshi90.harmontide.entity.ai.FridgeGoal<>(this));
        this.goalSelector.addGoal(3, new com.linkyoshi90.harmontide.entity.ai.NurseGoal(this));
        this.goalSelector.addGoal(3, new FarmhandGoal<>(this));
        this.goalSelector.addGoal(5, new Chores.ChoresGoal<>(this));
        this.goalSelector.addGoal(7, new SleepGoal(this));
        if (species.clade() == com.linkyoshi90.harmontide.entity.Clade.FELIFORMIA) this.goalSelector.addGoal(6, new RainMoods.ShelterGoal(this));   // cats hate the rain
        this.targetSelector.addGoal(1, new com.linkyoshi90.harmontide.entity.ai.StanceTargetGoal<>(this));
        this.goalSelector.addGoal(3, new ShareBedGoal<>(this));
        this.goalSelector.addGoal(3, new GiftGoal<>(this));
        this.goalSelector.addGoal(5, new FollowEntityGoal(this, this::mother, 1.1, 6.0F, 2.5F));
        this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                p -> p instanceof Player pl && this.isHostileTo(pl)));
        if (GirlArms.isFighter(species) || species == GirlSpecies.CYCLOPS_DEVIL) {   // a weapon from her friend: handed over or thrown her way
            this.goalSelector.addGoal(0, new GirlArms.FetchGoal(this));
            this.goalSelector.addGoal(1, new GirlArms.RangedGoal(this));
            this.goalSelector.addGoal(2, new GirlArms.SpotterGoal(this));
        }
        this.goalSelector.addGoal(5, new Festivals.GatherGoal(this));
        this.goalSelector.addGoal(6, new Settlement.HomeGoal(this));
        if (this.canFly()) {                                // the winged and the floating: fly instead of walking
            this.useFlight(true);
            this.goalSelector.addGoal(8, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal(this, 1.0));
        }
    }

    /** Flying movement and navigation, or walking ones (a flier who cannot fly: a broken wing). */
    private void useFlight(boolean fly) {
        if (fly) {
            this.moveControl = new net.minecraft.world.entity.ai.control.FlyingMoveControl(this, 20, true);
            net.minecraft.world.entity.ai.navigation.FlyingPathNavigation nav = new net.minecraft.world.entity.ai.navigation.FlyingPathNavigation(this, this.level());
            nav.setCanOpenDoors(false);
            nav.setCanFloat(true);
            this.navigation = nav;
        } else {
            this.moveControl = new net.minecraft.world.entity.ai.control.MoveControl(this);
            this.navigation = new net.minecraft.world.entity.ai.navigation.GroundPathNavigation(this, this.level());
            this.setNoGravity(false);
        }
    }

    // --- nursing (Nursing, NurseGoal) -------------------------------------------------------------------------------

    /** Her baby (or a foster child), held to her chest while she nurses. */
    public boolean nursingRider(Entity passenger) {
        return passenger instanceof LivingEntity baby && baby.isBaby() && this.getAction() == Action.NURSE;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, net.minecraft.world.entity.EntityDimensions dims, float scale) {
        if (this.nursingRider(passenger)) {                             // her middle at her mother's chest, just in front of it
            double[] chest = com.linkyoshi90.harmontide.entity.Lactation.chest(this);
            return new Vec3(0.0, chest[0] - passenger.getBbHeight() * 0.55, chest[1] + passenger.getBbWidth() * 0.5);
        }
        return super.getPassengerAttachmentPoint(passenger, dims, scale);
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction move) {
        super.positionRider(passenger, move);
        if (this.nursingRider(passenger) && passenger instanceof LivingEntity baby) {   // facing her mother
            float yaw = this.yBodyRot + 180.0F;
            baby.setYRot(yaw);
            baby.yBodyRot = yaw;
            baby.yHeadRot = yaw;
        }
    }

    /** A baby carried about never steers. */
    @Override
    public boolean canControlVehicle() {
        return !this.isBaby() && super.canControlVehicle();
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
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.FLYING_SPEED, Math.max(0.4, speed * 1.4));
    }

    /**
     * Whether she flies (the species must only look at {@link #species}: this is asked from the constructor). Fliers get
     * flying movement and navigation, never take fall damage and play "fly" while airborne.
     */
    public boolean canFly() {
        return false;
    }

    /** Whether her animation file has a "fly" animation (a floating will-o'-wisp has none: she just idles in the air). */
    protected boolean hasFlyAnimation() {
        return this.canFly();
    }

    /** Gliding down on wings that cannot fly (the wolpertinger): she plays her "fly" animation while she does. */
    protected boolean isGliding() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return !this.canFly() && super.causeFallDamage(distance, multiplier, source);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new com.linkyoshi90.harmontide.entity.Herds.AlarmGoal(this));        // the herd's alarm (entity/Herds)
        this.goalSelector.addGoal(7, new com.linkyoshi90.harmontide.entity.FoodWeb.ForageGoal(this));
        this.goalSelector.addGoal(7, new com.linkyoshi90.harmontide.entity.Herds.KeepTogetherGoal(this));
        this.goalSelector.addGoal(8, new com.linkyoshi90.harmontide.entity.Territories.PatrolGoal(this));
        this.goalSelector.addGoal(7, new com.linkyoshi90.harmontide.entity.Pollination.VisitGoal(this));   // flower to flower (entity/Pollination)   // marking her range (entity/Territories)   // wild, she feeds herself (entity/FoodWeb)
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
        builder.define(DATA_FLAGS, 0);
        builder.define(DATA_PAINT, 0);
        builder.define(DATA_TAILS, (byte) 1);
        builder.define(DATA_OUTFIT, (byte) 0);
        builder.define(DATA_AIM, (byte) AIM_NONE);
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

    /** The kind whose look she wears (the doppelganger, entity/girl/Doppelganger), or "" for her own. */
    public String disguise() {
        return "";
    }

    public int getOutfit() {
        return this.entityData.get(DATA_OUTFIT);
    }

    public void setOutfit(int outfit) {
        this.entityData.set(DATA_OUTFIT, (byte) outfit);
    }

    public boolean hasFlag(int flag) {
        return (this.entityData.get(DATA_FLAGS) & flag) != 0;
    }

    public void setFlag(int flag, boolean on) {
        int b = this.entityData.get(DATA_FLAGS);
        this.entityData.set(DATA_FLAGS, on ? b | flag : b & ~flag);
    }

    /** How many tails she was born with (a few kitsune and nekomata have split tails from the start). */
    public int getTails() {
        return this.entityData.get(DATA_TAILS);
    }

    public void setTails(int tails) {
        this.entityData.set(DATA_TAILS, (byte) Math.max(1, Math.min(9, tails)));
    }

    /** The tail count a wild one of her species is born with (most: one). */
    protected int rollTails(RandomSource random) {
        return 1;
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

    /** A bath towel round her for a while (client/TowelLayer). */
    public void towelFor(int ticks) {
        this.towelTicks = Math.max(this.towelTicks, ticks);
        this.setFlag(FLAG_TOWEL, true);
    }

    /** Pink cheeks for a while (client/GirlRenderer.BlushLayer). */
    public void blushFor(int ticks) {
        this.blushTicks = Math.max(this.blushTicks, ticks);
        this.setFlag(FLAG_BLUSHING, true);
    }

    /** How far behind her waist a rider sits, in blocks: her model is drawn this far forward so she turns about her rider (GirlRenderer). */
    public double seatBack() {
        return 0.0;
    }

    /** The milking harness for a while (client/MilkingHarnessLayer); minotaur cows run their own. */
    public void harnessFor(int ticks) {
        this.harnessTicks = Math.max(this.harnessTicks, ticks);
        this.setFlag(FLAG_HARNESSED, true);
    }

    /** The vanity mirror: her next colour, leaving out the ones she has to earn. */
    public void nextVariant() {
        int n = this.species.variants().size();
        for (int i = 1; i < n; i++) {
            int v = (this.getVariantIndex() + i) % n;
            if (!EARNED.contains(this.species.variants().get(v))) {
                this.setVariantIndex(v);
                return;
            }
        }
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

    /** Wild spawns: the default colours half the time (see the variant rate), the four others share the rest. */
    protected int rollWildVariant(RandomSource random) {
        int v = random.nextDouble() >= com.linkyoshi90.harmontide.HarmontideConfig.variantChance(0.5) ? 0 : 1 + random.nextInt(this.species.variants().size() - 1);
        if (EARNED.contains(this.species.variants().get(v))) return 0;
        return HOMAGES.contains(this.species.variants().get(v)) && random.nextInt(8) != 0 ? 0 : v;   // homages are rare
    }

    public void randomizeVariant() {
        this.setVariantIndex(this.random.nextInt(this.species.variants().size()));
        if (EARNED.contains(this.getVariantName())) this.setVariantIndex(0);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData groupData) {
        if (!this.variantFromData && this.getPaint() == 0 && spawnType != MobSpawnType.BREEDING && com.linkyoshi90.harmontide.HarmontideConfig.rollsPaint(this.random)) {   // a summoned paint stays
            this.setPaint(com.linkyoshi90.harmontide.entity.Painted.roll(this.random));   // a painted one: rare, like a shiny
        }
        if (!this.variantFromData) {
            this.setVariantIndex(this.rollWildVariant(this.random));
            this.setTails(this.rollTails(this.random));
        }
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, groupData);
        if ((spawnType == MobSpawnType.NATURAL || spawnType == MobSpawnType.CHUNK_GENERATION) && !this.isBaby() && this.random.nextInt(this.injuredRarity()) == 0) {
            this.setInjured(true);                                       // a wild one, hurt: the healing balm encounter
            this.setHealth(this.getMaxHealth() * 0.3F);
        }
        return data;
    }

    // --- injured (the balm encounter) ------------------------------------------------------------------------------

    /** One wild girl in this many is found injured. */
    protected int injuredRarity() {
        return 50;
    }

    /** How much friendship mending her wound is worth. */
    protected int mendAffection() {
        return 40;
    }

    public boolean isInjured() {
        return this.hasFlag(FLAG_INJURED);
    }

    /** Injured: slow, she cannot fight, and a flier cannot fly (a broken wing). */
    public void setInjured(boolean injured) {
        this.setFlag(FLAG_INJURED, injured);
        this.applyInjury();
    }

    private void applyInjury() {
        boolean injured = this.isInjured();
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(INJURED_SLOW);
            if (injured) speed.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(INJURED_SLOW, -0.5,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        if (this.canFly()) this.useFlight(!injured);
        if (injured) this.setTarget(null);
    }

    /** A healing balm on her wound: she is whole again, and she will not forget who did it. */
    public void mend(ServerLevel level, Player player) {
        this.setInjured(false);
        this.addAffection(player, this.mendAffection());
        this.setLastHurtByMob(null);
        level.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + this.getBbHeight(), this.getZ(), 12, 0.5, 0.4, 0.5, 0.0);
        player.displayClientMessage(Component.translatable("message.harmontide.mended", this.getDisplayName()), true);
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
        int before = this.getAffection(player);
        this.affection.merge(player.getUUID(), amount, (a, b) -> Math.max(0, Math.min(FULL_AFFECTION, a + b)));
        this.affection.computeIfPresent(player.getUUID(), (k, v) -> Math.max(0, Math.min(FULL_AFFECTION, v)));
        com.linkyoshi90.harmontide.entity.FourSymbols.onAffection(this, player, this.getAffection(player));
        com.linkyoshi90.harmontide.entity.Milestones.after(this, player, before, this.getAffection(player));
    }

    @Override
    public void setAffection(UUID player, int amount) {
        this.affection.put(player, Math.max(0, Math.min(FULL_AFFECTION, amount)));
        com.linkyoshi90.harmontide.entity.FourSymbols.onAffection(this, this.level().getPlayerByUUID(player), this.getAffection(player));
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
        return Component.translatable("bond.harmontide.girl." + key, a);
    }

    @Override
    public Gestation getGestation() {
        return this.gestation;
    }

    /** A girl monsterized by the Demon Realm joins any sleeper's bed, friend or not, but never her father's (entity/Monsterization). */
    @Override
    public boolean trustsEnoughForBed(Player player) {
        if (com.linkyoshi90.harmontide.entity.Monsterization.monsterized(this)) return !player.getUUID().equals(this.getFather());
        return com.linkyoshi90.harmontide.entity.Bonded.super.trustsEnoughForBed(player);
    }

    @Override
    public float bedChanceFor(Player player) {
        return Math.max(com.linkyoshi90.harmontide.entity.Bonded.super.bedChanceFor(player), com.linkyoshi90.harmontide.entity.Monsterization.eagerness(this, player));
    }

    @Override
    public boolean canShareBed() {
        return !this.isBaby() && this.species.height() >= CHILDLIKE_HEIGHT && this.gestation.canConceive() && !this.hasFlag(FLAG_DECOY) && !this.hasFlag(FLAG_HALF);
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
    public boolean isMoonstruck() {
        return this.hasFlag(FLAG_MOONSTRUCK);
    }

    @Override
    public void setMoonstruck(boolean moonstruck) {
        this.setFlag(FLAG_MOONSTRUCK, moonstruck);
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

    // --- stick orders --------------------------------------------------------------------------------------------

    @Override
    public com.linkyoshi90.harmontide.entity.Orders getOrders() {
        return this.orders;
    }

    /** Her stance has the last word on whom she fights (and companions never fight each other). */
    @Override
    public void setTarget(@Nullable net.minecraft.world.entity.LivingEntity target) {
        boolean allowed = (this.orders == null || this.orders.allows(this, target)) && !this.isInjured();
        super.setTarget(allowed ? target : null);
    }

    /** A score to settle: past her orders and her friendships (the Fury's guilty, the Charro Negro's debtor). */
    protected void settleScoreWith(net.minecraft.world.entity.LivingEntity target) {
        super.setTarget(target);
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
        if (p != null) p.displayClientMessage(Component.translatable("message.harmontide.true_form", this.getDisplayName()), true);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1, this.getZ(), 30, 0.5, 0.8, 0.5, 0.02);
    }

    // --- wild hostility ----------------------------------------------------------------------------------------

    /** Whether a wild one of her species attacks players on sight until someone calms her with food (meat). */
    protected boolean wildHostile() {
        return false;
    }

    /** What calms a hostile one down (any meat; the zombie wants rotten flesh). */
    protected boolean calmsHer(ItemStack stack) {
        return stack.is(ModTags.MEAT);
    }

    public boolean isCalm() {
        return this.hasFlag(FLAG_CALM);
    }

    /** Whether she would attack this player on sight right now. */
    public boolean isHostileTo(Player player) {
        return this.wildHostile() && !this.isCalm() && this.getAffection(player) <= 0 && this.couldAttack(player);
    }

    /** What every wild hostility needs: a grown, wild, unhurt girl, and a player in survival on a difficulty above peaceful. */
    protected boolean couldAttack(Player player) {
        return !this.isBaby() && this.father == null && !this.isInjured()
                && !player.isCreative() && !player.isSpectator() && this.level().getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL;
    }

    /** The first bite of the right food: she stops seeing you as prey. More food from then on builds friendship. */
    protected void calmDown(ServerLevel level, Player player) {
        this.setFlag(FLAG_CALM, true);
        if (this.getTarget() instanceof Player) this.setTarget(null);
        this.setLastHurtByMob(null);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + this.getBbHeight(), this.getZ(), 8, 0.4, 0.3, 0.4, 0.0);
        player.displayClientMessage(Component.translatable("message.harmontide.calmed", this.getDisplayName()), true);
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
        this.wokenUntil = this.level().getGameTime() + 600;               // a hand wakes her (for half a minute)
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) return super.mobInteract(player, hand);
        InteractionResult paint = com.linkyoshi90.harmontide.entity.Painted.interact(this, this, player, hand);
        if (paint != null) return paint;
        InteractionResult favour = Favours.interact(this, player, stack);    // what she asked them for
        if (favour != null) return favour;
        InteractionResult soup = Colds.interact(this, player, stack);      // a warm soup for a cold
        if (soup != null) return soup;
        if (stack.is(Items.STICK)) {
            if (!this.takesOrders()) {                                     // the chupacabra answers to nobody
                if (!this.level().isClientSide) player.displayClientMessage(Component.translatable("message.harmontide.ignores_orders", this.getDisplayName()), true);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            if (!this.level().isClientSide) com.linkyoshi90.harmontide.entity.Orders.onStick(this, this.orders, player);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.wildHostile() && !this.isCalm() && !this.isBaby() && this.calmsHer(stack)) {
            if (this.level() instanceof ServerLevel server) {
                stack.consume(1, player);
                this.playSound(this.getEatSound(), 1.0F, this.getVoicePitch());
                this.calmDown(server, player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (stack.is(Items.MAP) && !this.isBaby() && this.getAffection(player) >= com.linkyoshi90.harmontide.entity.Orders.AFFECTION) {   // off on an expedition
            if (!this.level().isClientSide) com.linkyoshi90.harmontide.entity.Expeditions.send(this, player, stack);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        InteractionResult arms = GirlArms.interact(this, player, stack);
        if (arms != null) return arms;
        if (Mining.isMiner(this) && !this.isBaby() && this.getAffection(player) >= com.linkyoshi90.harmontide.entity.Orders.AFFECTION) {   // a pickaxe, then what to dig for
            boolean pickaxe = this.getMainHandItem().getItem() instanceof net.minecraft.world.item.PickaxeItem;
            if (stack.getItem() instanceof net.minecraft.world.item.PickaxeItem) {
                if (this.level() instanceof ServerLevel s) Mining.takePickaxe(this, player, stack);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            if (pickaxe && Mining.vein(stack) != null) {
                if (this.level() instanceof ServerLevel s) Mining.send(s, this, player, stack);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            if (pickaxe && stack.isEmpty() && player.isShiftKeyDown() && this.getPersistentData().getBoolean(Mining.GIVEN)) {   // the pickaxe back
                if (this.level() instanceof ServerLevel) Mining.givePickaxeBack(this, player);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        InteractionResult errand = Foraging.interact(this, player, stack);   // a crouched sample: off to gather it
        if (errand != null) return errand;
        InteractionResult family = this.familyInteract(player, hand, stack);
        if (family != null) return family;
        if (this.isBaby() && (this.isFood(stack) || stack.is(ModTags.MEAT) || stack.has(net.minecraft.core.component.DataComponents.FOOD))
                && this.level() instanceof ServerLevel weaning && com.linkyoshi90.harmontide.entity.Nursing.wean(weaning, this, player)) {   // her first solid food
            stack.consume(1, player);
            this.playSound(this.getEatSound(), 1.0F, this.getVoicePitch());
            this.hearts(weaning, 3);
            return InteractionResult.SUCCESS;
        }
        if (!this.isBaby() && (this.isFood(stack) || stack.is(ModTags.MEAT) || stack.is(Treats.favourite(this)))) {
            boolean hurt = this.getHealth() < this.getMaxHealth();
            boolean welcome = this.level().getGameTime() >= this.nextFoodTime;
            if (hurt || welcome) {
                if (this.level() instanceof ServerLevel server) {
                    int value = welcome ? Treats.value(server, this, player, stack, this.foodValue(player, stack)) : 0;   // her favourite, her birthday
                    com.linkyoshi90.harmontide.entity.Dairy.ate(this, stack);
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
        if (stack.isEmpty() && this.level() instanceof ServerLevel rescue && com.linkyoshi90.harmontide.entity.MilkRescue.accept(rescue, this, player)) {
            return InteractionResult.SUCCESS;                           // a cup of her milk, offered when they were poisoned
        }
        if (stack.isEmpty() && !player.isShiftKeyDown()) return this.pet(player);
        if (stack.isEmpty() && this.level() instanceof ServerLevel game && HideAndSeek.start(game, this, player)) return InteractionResult.SUCCESS;   // crouching: let's play
        return super.mobInteract(player, hand);
    }

    /** A pat from an empty hand. */
    protected InteractionResult pet(Player player) {
        if (this.level() instanceof ServerLevel server) {
            int value = this.petValue(player);
            if (value > 0) {
                if (server.getGameTime() >= this.nextPetTime) {
                    this.nextPetTime = server.getGameTime() + PET_COOLDOWN;
                    this.addAffection(player, value);
                }
                this.getLookControl().setLookAt(player);
                this.hearts(server, 2);
                com.linkyoshi90.harmontide.entity.Flirting.onPet(server, this, player);
                com.linkyoshi90.harmontide.entity.Lactation.onPet(this, player);
                this.playSound(this.getChirpSound(), 1.0F, this.getVoicePitch() * 1.1F);
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
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
        if (hurt) this.wokenUntil = this.level().getGameTime() + 600;
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

    /** Her own voice (entity/Voices): one of her slots' vanilla sounds is played as her species' event. */
    @Override
    public void playSound(SoundEvent sound, float volume, float pitch) {
        super.playSound(com.linkyoshi90.harmontide.entity.Voices.voice(this, sound), volume, pitch);
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
        return this.isBaby() ? YOUNG_SCALE : this.hasFlag(FLAG_HALF) ? 0.6F : this.hasFlag(FLAG_RETRACTED) && this.species == GirlSpecies.PUFFERFISH_MERMAID ? 1.4F : 1.0F;
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
        if (this.random.nextBoolean() && !EARNED.contains(this.getVariantName())) young.setVariantIndex(this.getVariantIndex());   // she takes after her mother
        young.setPaint(this.getPaint());                                  // a painted mother has painted daughters
        young.setPersistenceRequired();                                   // born here, she stays here: no despawning
        level.addFreshEntity(young);
        young.getPersistentData().putUUID(com.linkyoshi90.harmontide.entity.Reunions.BORN_OF, this.getUUID());   // whose she is (households, reunions)
        young.bornTo(father);
        if (father != null && level.getServer().getPlayerList().getPlayer(father) instanceof Player parent) {
            com.linkyoshi90.harmontide.entity.Marriage.motherOf(parent, young, this);   // the family journal knows her mother
        }
        if (this.random.nextBoolean()) com.linkyoshi90.harmontide.entity.Hobbies.setHobby(young, com.linkyoshi90.harmontide.entity.Hobbies.hobby(this));   // like mother, like daughter
        com.linkyoshi90.harmontide.entity.Nursing.born(this, young);   // she nurses her newborn
        return young;
    }

    public static AbstractMonsterGirl youngOf(ServerLevel level, EntityType<?> type, BlockPos pos, @Nullable UUID father, int paint) {
        AbstractMonsterGirl young = youngOf(level, type, pos, father);
        if (young != null) young.setPaint(paint);
        return young;
    }

    public static AbstractMonsterGirl youngOf(ServerLevel level, EntityType<?> type, BlockPos pos, @Nullable UUID father) {
        if (!(type.create(level) instanceof AbstractMonsterGirl young)) return null;
        young.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
        young.setBaby(true);
        young.randomizeVariant();
        young.setPersistenceRequired();
        level.addFreshEntity(young);
        young.bornTo(father);
        return young;
    }

    // --- tick ----------------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.blushTicks > 0 && --this.blushTicks == 0) this.setFlag(FLAG_BLUSHING, false);
        if (this.level() instanceof ServerLevel party && this.tickCount % 101 == 0) com.linkyoshi90.harmontide.entity.HomeLife.dance(party, this, com.linkyoshi90.harmontide.entity.JukeboxSensing.findPlaying(party, this.blockPosition(), 16) != null
                || com.linkyoshi90.harmontide.entity.DemonRealm.sabbath(this));
        if (!this.level().isClientSide && this.towelTicks > 0 && --this.towelTicks == 0) this.setFlag(FLAG_TOWEL, false);
        if (!this.level().isClientSide && this.harnessTicks > 0 && --this.harnessTicks == 0) this.setFlag(FLAG_HARNESSED, false);
        if (this.level() instanceof ServerLevel milky) com.linkyoshi90.harmontide.entity.Lactation.tick(milky, this);
        SerpentSegment.follow(this, this.segments);
        if (this.level() instanceof ServerLevel healerLevel && this.tickCount % 600 == 0) this.tendCompanions(healerLevel);
        if (!(this.level() instanceof ServerLevel server)) return;
        com.linkyoshi90.harmontide.entity.Painted.tickCopper(this, this);
        if (this.tickCount % 100 == 0) Settlement.tick(server, this);
        if (this.tickCount % 100 == 50) Treats.tick(server, this);
        if (this.tickCount % 100 == 25) Colds.tick(server, this);
        if (this.tickCount % 600 == 300) GreenThumb.tend(server, this);
        if (this.tickCount % 10 == 3) HideAndSeek.tick(server, this);
        if (this.tickCount % 20 == 9 && Mining.holdsHaul(this)) Mining.handOver(server, this);
        if (this.tickCount % 97 == 0) Rivalries.squabble(server, this);
        if (this.tickCount % 100 == 75) RainMoods.tick(server, this);
        if (this.tickCount % 100 == 80) Basking.tick(server, this);
        if (this.tickCount % 200 == 120) Habits.tick(server, this);
        if (this.tickCount % 100 == 90) Habits.groom(server, this);
        if (this.tickCount % 20 == 11) Habits.douse(server, this);
        if (this.tickCount % 100 == 95) Habits.beachcomb(server, this);
        if (this.tickCount % 200 == 170) Habits.fairyRing(server, this, false);
        if (this.tickCount % 40 == 23) Habits.thump(server, this);
        if (this.hutOrigin != null && this.tickCount % 10 == 0) Settlement.buildStep(server, this);
        this.gestation.tick();
        if (this.tickCount % 10 == 0) this.entityData.set(DATA_BELLY, (byte) Math.round(this.bellyProgress() * 100));
        if (this.isBaby() && ++this.growTicks >= GROW_TICKS) {
            this.setBaby(false);
            Roles.grownUp(server, this);                                  // she picks her calling
        }
        if (this.tickCount % Roles.COOK_EVERY == 0) Roles.cook(server, this);
        if (this.isInjured() && this.tickCount % 60 == 0) {
            server.sendParticles(ParticleTypes.DAMAGE_INDICATOR, this.getX(), this.getY() + this.getBbHeight() * 0.6, this.getZ(), 2, 0.3, 0.2, 0.3, 0.0);
        }
        if (this.timedActionTicks > 0 && --this.timedActionTicks == 0) this.setAction(this.orderedToSit ? Action.SITTING : Action.NONE);
        if (this.orderedToSit && this.getAction() == Action.NONE) this.setAction(Action.SITTING);
        if (this.tickCount % 100 == 65 && this.gestation.isCarrying()) Cravings.tick(server, this);
        if (this.tickCount % Cradle.EVERY == 55 && this.isBaby()) Cradle.tick(server, this, server.isNight());
        if (this.tickCount % 60 == 37 && this.isBaby()) GrowingUp.tick(server, this);
        if (this.gestation.isReady() && this.tickCount % 40 == 0 && this.giveBirth(server, this.gestation.getFather())) {
            boolean twins = Cravings.twins(this);
            if (twins) this.giveBirth(server, this.gestation.getFather());   // her craving was answered: twins
            com.linkyoshi90.harmontide.entity.Marriage.announceBirth(server, this, this.gestation.getFather(), twins);
            this.gestation.clear();
            if (com.linkyoshi90.harmontide.entity.Monsterization.monsterized(this)) this.gestation.halveCooldown();   // eager again sooner
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

    // --- a serpent's body ------------------------------------------------------------------------------------------

    @Override
    public boolean isMultipartEntity() {
        return this.segments.length > 0;
    }

    @Nullable
    @Override
    public net.neoforged.neoforge.entity.PartEntity<?>[] getParts() {
        return this.segments;
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        if (this.segments != null) for (int i = 0; i < this.segments.length; i++) this.segments[i].setId(id + i + 1);
    }

    @Override
    public net.minecraft.world.phys.AABB getBoundingBoxForCulling() {                // her tail is drawn well outside her own box
        return this.segments.length > 0 ? super.getBoundingBoxForCulling().inflate(3.5) : super.getBoundingBoxForCulling();
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
        boolean water = level.getBlockState(here).is(Blocks.WATER) && level.getFluidState(here).isSource();
        if (level.getBlockState(here).isAir() || water) {
            level.setBlock(here, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, lightLevel).setValue(LightBlock.WATERLOGGED, water), 3);
            this.lightPos = here;
        }
    }

    protected void clearLight() {
        var light = this.lightPos == null ? null : this.level().getBlockState(this.lightPos);
        if (light != null && light.is(Blocks.LIGHT)) {
            this.level().setBlock(this.lightPos, light.getValue(LightBlock.WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
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
        tag.put("Orders", this.orders.save());
        tag.putLong("NextGift", this.nextGiftTime);
        tag.putBoolean("Young", this.isBaby());
        tag.putInt("GrowTicks", this.growTicks);
        tag.putString("Variant", this.getVariantName());
        tag.putInt("Flags", this.entityData.get(DATA_FLAGS) & SAVED_FLAGS);
        tag.putByte("Tails", (byte) this.getTails());
        if (this.getPaint() > 0) tag.putString("Paint", this.getPaintName());
        tag.putLong("DevotedSince", this.devotedSince);
        if (!this.ammoPouch.isEmpty()) tag.put("AmmoPouch", this.ammoPouch.createTag(this.registryAccess()));
        if (this.homeHearth != null) {
            tag.put("HomeHearth", net.minecraft.nbt.NbtUtils.writeBlockPos(this.homeHearth));
            tag.putLong("SettledSince", this.settledSince);
        }
        if (this.hutOrigin != null) {
            tag.put("HutOrigin", net.minecraft.nbt.NbtUtils.writeBlockPos(this.hutOrigin));
            tag.putString("HutDoor", this.hutDoor.getName());
            tag.putInt("HutStep", this.hutStep);
        }
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
        this.orders.load(tag.getCompound("Orders"));
        this.nextGiftTime = tag.contains("NextGift") ? tag.getLong("NextGift") : -1;
        this.entityData.set(DATA_YOUNG, tag.getBoolean("Young"));
        this.growTicks = tag.getInt("GrowTicks");
        if (tag.contains("Variant")) {
            this.variantFromData = true;
            this.setVariant(tag.getString("Variant"));
        }
        this.entityData.set(DATA_FLAGS, tag.getInt("Flags") & SAVED_FLAGS);
        if (this.isInjured()) this.applyInjury();
        if (tag.contains("Tails")) this.setTails(tag.getByte("Tails"));
        if (tag.contains("Paint")) this.setPaint(PAINTS.indexOf(tag.getString("Paint")) + 1);
        this.devotedSince = tag.contains("DevotedSince") ? tag.getLong("DevotedSince") : NOT_DEVOTED;
        this.ammoPouch.clearContent();
        if (tag.contains("AmmoPouch")) this.ammoPouch.fromTag(tag.getList("AmmoPouch", net.minecraft.nbt.Tag.TAG_COMPOUND), this.registryAccess());
        this.homeHearth = net.minecraft.nbt.NbtUtils.readBlockPos(tag, "HomeHearth").orElse(null);
        this.settledSince = tag.getLong("SettledSince");
        this.hutOrigin = net.minecraft.nbt.NbtUtils.readBlockPos(tag, "HutOrigin").orElse(null);
        net.minecraft.core.Direction d = net.minecraft.core.Direction.byName(tag.getString("HutDoor"));
        this.hutDoor = d == null || d.getAxis().isVertical() ? net.minecraft.core.Direction.NORTH : d;
        this.hutStep = tag.getInt("HutStep");
    }

    // --- aiming a weapon (GirlArms) --------------------------------------------------------------------------------

    public int getAim() {
        return this.entityData.get(DATA_AIM);
    }

    public void setAim(int aim) {
        if (this.getAim() != aim) this.entityData.set(DATA_AIM, (byte) aim);
    }

    /** She fired: everyone watching sees the recoil (or the bowstring released). */
    public void announceShot() {
        this.lastShotTick = this.tickCount;
        this.level().broadcastEntityEvent(this, EVENT_SHOT);
    }

    public int lastShotTick() {
        return this.lastShotTick;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_SHOT) this.lastShotTick = this.tickCount;
        else super.handleEntityEvent(id);
    }

    // --- her home at a hearth (Settlement) --------------------------------------------------------------------------

    @Nullable
    public BlockPos homeHearth() {
        return this.homeHearth;
    }

    public long settledSince() {
        return this.settledSince;
    }

    public void callHome(long until) {
        this.calledHomeUntil = until;
    }

    /** The hearth's bell called her and she is still answering it. */
    public boolean calledHome() {
        return this.homeHearth != null && this.level().getGameTime() < this.calledHomeUntil;
    }

    void settleAt(BlockPos hearth, long time) {
        this.homeHearth = hearth.immutable();
        this.settledSince = time;
        this.hutOrigin = null;
        this.hutStep = 0;
    }

    /** Her hearth is gone (or no longer has her): she roams free again. Her hut, if she built one, stays standing. */
    public void leaveHearth() {
        this.homeHearth = null;
        this.hutOrigin = null;
        this.hutStep = 0;
    }

    @Nullable
    public BlockPos hutOrigin() {
        return this.hutOrigin;
    }

    public net.minecraft.core.Direction hutDoor() {
        return this.hutDoor;
    }

    public int hutStep() {
        return this.hutStep;
    }

    void setHutStep(int step) {
        this.hutStep = step;
    }

    public void planHutAt(BlockPos origin, net.minecraft.core.Direction door) {
        this.hutOrigin = origin.immutable();
        this.hutDoor = door;
        this.hutStep = 0;
    }

    public boolean hutDone() {
        return this.hutOrigin != null && this.hutStep >= Settlement.plan(this).size();
    }

    public net.minecraft.world.SimpleContainer ammoPouch() {
        return this.ammoPouch;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, net.minecraft.world.damagesource.DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        for (ItemStack s : this.ammoPouch.removeAllItems()) this.spawnAtLocation(s);   // her friend's ammunition is not lost with her
    }

    // --- animation -----------------------------------------------------------------------------------------------

    protected RawAnimation anim(String name, boolean once) {
        String model = OWN_MODELS.contains(this.getVariantName()) ? this.species.id() + "_" + this.getVariantName() : this.species.id();   // an own model's file is named for it
        String key = (once ? "once:" : "") + model + "." + name;
        return ANIMATIONS.computeIfAbsent(key, k -> once ? RawAnimation.begin().thenPlayAndHold("animation." + model + "." + name)
                : RawAnimation.begin().thenLoop("animation." + model + "." + name));
    }

    /** Whether her animation file has a swim animation (the merfolk). */
    protected boolean hasSwimAnimation() {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 4, this::animate));
    }

    /** Whether her animation file has a seated "ride" pose for when she is on a mount (the Riders). */
    protected boolean hasRideAnimation() {
        return false;
    }

    private PlayState animate(AnimationState<AbstractMonsterGirl> state) {
        if (this.isDeadOrDying() && this.hasDeathAnimation()) return state.setAndContinue(this.anim("death", true));
        if (this.isPassenger() && this.hasRideAnimation()) return state.setAndContinue(this.anim("ride", false));
        Action action = this.getAction();
        if (action.animation != null) return state.setAndContinue(this.anim(action.animation, action.once));
        if (this.isInWater() && this.hasSwimAnimation()) return state.setAndContinue(this.anim(state.isMoving() ? "swim" : "idle", false));
        if ((this.canFly() && this.hasFlyAnimation() || this.isGliding()) && !this.onGround() && !this.isInWater()) return state.setAndContinue(this.anim("fly", false));
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

    // --- healers ---------------------------------------------------------------------------------------------------

    /** Healers (flora, the verdant and honey slimes, the snail, the fairies, the axolotl) bring healing balms and tend companions. */
    public boolean isHealer() {
        return false;
    }

    /**
     * A healer at high friendship tends her player's hurt companions within 8 blocks (other girls, befriended vanilla
     * mobs) once every 30 seconds: the worst hurt gets 4 health and a little regeneration.
     */
    public void tendCompanions(ServerLevel level) {
        if (!this.isHealer() || this.isBaby() || this.bondedPlayer(BED_AFFECTION, 16) == null) return;
        net.minecraft.world.entity.LivingEntity worst = null;
        for (var e : level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, this.getBoundingBox().inflate(8),
                e -> e != this && e.isAlive() && !(e instanceof Player) && e.getHealth() < e.getMaxHealth() && com.linkyoshi90.harmontide.entity.Orders.companions(this, e))) {
            if (worst == null || e.getHealth() / e.getMaxHealth() < worst.getHealth() / worst.getMaxHealth()) worst = e;
        }
        if (worst == null) return;
        worst.heal(4.0F);
        worst.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 100, 0));
        level.sendParticles(ParticleTypes.HEART, worst.getX(), worst.getY() + worst.getBbHeight(), worst.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
        this.getLookControl().setLookAt(worst);
    }

    // --- sleep ----------------------------------------------------------------------------------------------------

    private long wokenUntil;

    /** Until when she stays awake (after a hit or a hand), see SleepGoal. */
    public long getWokenUntil() {
        return this.wokenUntil;
    }

    /** Her daily rhythm (2026-11-13): when she is up and about, and when she sleeps. */
    public enum Rhythm {
        DIURNAL, NOCTURNAL, CREPUSCULAR, CATHEMERAL;

        public String key() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    /** By day for most; the listed night kinds by night; otherwise as her concept says (registry/RhythmTable, tools/gen_diet_table.py). */
    public Rhythm rhythm() {
        if (this.listedNocturnal()) return Rhythm.NOCTURNAL;
        String r = com.linkyoshi90.harmontide.registry.RhythmTable.RHYTHM.get(this.species.id());
        return r == null ? Rhythm.DIURNAL : Rhythm.valueOf(r);
    }

    /** Up at night, asleep by day. */
    public boolean isNocturnal() {
        return this.rhythm() == Rhythm.NOCTURNAL;
    }

    /**
     * Her resting hours: the night for day kinds, the day for night kinds, midday and the dead of night for the dawn-and-dusk kinds
     * (up 21000 to 4000 and 9000 to 15000), and one nap in four for the round-the-clock nappers (bouts of 1500 ticks, her own phase).
     */
    public boolean restingHours() {
        if (this.level().dimensionType().hasFixedTime()) return false;      // no day and night down there
        long t = Math.floorMod(this.level().getDayTime(), 24000L);
        boolean night = t >= 13000 && t < 23000;                            // by the clock, not the sky (a storm isn't night)
        return switch (this.rhythm()) {
            case DIURNAL -> night;
            case NOCTURNAL -> !night;
            case CREPUSCULAR -> t >= 4000 && t < 9000 || t >= 15000 && t < 21000;
            case CATHEMERAL -> Math.floorMod(this.level().getDayTime() / 1500L + (this.getUUID().getLeastSignificantBits() & 3L), 4L) == 0;
        };
    }

    /** The species known to be up at night: the undead, foxes, moths and mosquitoes, the night elf, the succubus, the jiang shi and the jack o'lantern, the tapir. */
    private boolean listedNocturnal() {
        return switch (this.species) {
            case NIGHT_ELF, MOTH_BUG, MOSQUITO_BUG, SUCCUBUS_DEVIL, JIANG_SHI_DEVIL, JACK_O_LANTERN_DEVIL, TAPIR_CENTAUR, OCELOT_NEKOMATA, ANUBIS_CANID,
                 CHUPACABRA_LIZARDFOLK, FIREFLY_BUG, CAVE_BAT, ONI_DEVIL, GARGOYLE_CONSTRUCT, SLOTHREN_ARMOURED, BROWNIE_ELF, GREMLIN_DEVIL -> true;
            default -> this.species.family() == GirlSpecies.Family.UNDEAD || this.species.family() == GirlSpecies.Family.KITSUNE;
        };
    }

    // --- Harmontide paints -----------------------------------------------------------------------------------------

    /** 0 unpainted, else 1 + an index into PAINTS. */
    public int getPaint() {
        return this.entityData.get(DATA_PAINT);
    }

    public void setPaint(int paint) {
        this.entityData.set(DATA_PAINT, (paint < 0 || paint > PAINTS.size() ? 0 : paint));
    }

    @Nullable
    public String getPaintName() {
        int p = this.getPaint();
        return p == 0 ? null : PAINTS.get(p - 1);
    }

    @Override
    public boolean isWaxed() {
        return this.hasFlag(FLAG_WAXED);
    }

    @Override
    public void setWaxed(boolean waxed) {
        this.setFlag(FLAG_WAXED, waxed);
    }

    // --- dying -----------------------------------------------------------------------------------------------------

    /** Whether a stick gives her orders (the chupacabra takes none). */
    protected boolean takesOrders() {
        return true;
    }

    /** Whether she has her own "death" animation (the golem breaks apart); the renderer then does not tip her over. */
    public boolean hasDeathAnimation() {
        return false;
    }
}
