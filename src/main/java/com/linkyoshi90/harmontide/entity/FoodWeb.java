package com.linkyoshi90.harmontide.entity;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.HearthBlockEntity;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The food web (2026-11-13). Every wild girl eats as her diet says, and so finds her place in the land she lives in. Her role comes from
 * what she will take from a hand ({@link #role}): meat makes her a {@link Role#HUNTER}, fish a {@link Role#FISHER}, carrion a
 * {@link Role#SCAVENGER}, greens a {@link Role#GRAZER}.
 * <ul>
 * <li>Hungry (once every {@link #MEAL} ticks), a hunter stalks and brings down a vanilla animal her size ({@link #prey}): rabbits and
 * chickens for the small, sheep, pigs and goats for most, cows too for the big. She eats the meat where it fell and leaves the remains
 * (hide, wool, feathers, a bone). A fisher takes fish and squid from the water near her.</li>
 * <li>A grazer crops the grass, ferns and flowers round her; where many graze together the turf is worn to bare dirt ({@link #OVERGRAZE}).</li>
 * <li>A scavenger goes to where things died ({@link #carrion}) and eats what was left lying there.</li>
 * <li><b>Populations.</b> Where there is too little to eat (fewer than {@link #SCARCE} animals near a hunter, no greens near a grazer)
 * she moves on, far off, to find better ground. And where the animals have been thinned out (by hunters or by players) and no hunter
 * is about, the herds come back over time: now and then a pair of the biome's own creatures turns up ({@link #regrow}).</li>
 * </ul>
 * Farm animals are safe: nothing tame, named, leashed or penned (a fence, wall or gate within {@link #PEN}), nothing in a village or
 * within {@link #HOME} blocks of a hearth. Befriended girls are fed by their friends and don't hunt. Off with the config option {@code foodWeb}.
 */
@EventBusSubscriber(modid = HarmontideMod.MODID)
public final class FoodWeb {
    public static final int MEAL = 12000, SEARCH = 24, SCARCE = 3, PEN = 3, HOME = 48, OVERGRAZE = 3, MOVE_ON = 64, CARRION_KEPT = 64,
            CARRION_TIME = 24000, REGROW_EVERY = 2400, REGROW_LOW = 3, REGROW_NEAR = 48, STRIKE_EVERY = 10;
    public static final float SMALL = 1.5F, LARGE = 2.2F, STRIKE = 6.0F;
    private static final String LAST_MEAL = "LastMeal";
    private static final Map<AbstractMonsterGirl, Role> ROLES = new WeakHashMap<>();
    private static final Map<ServerLevel, Deque<Carrion>> CARRION = new WeakHashMap<>();

    private FoodWeb() {}

    public enum Role {
        HUNTER, FISHER, SCAVENGER, GRAZER, NONE;

        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public record Carrion(BlockPos pos, long time) {}

    // --- roles -------------------------------------------------------------------------------------------------------

    /** Her place in the food web, from her diet. */
    public static Role role(AbstractMonsterGirl girl) {
        return ROLES.computeIfAbsent(girl, FoodWeb::roleOf);
    }

    /** From her concept's diet (registry/DietTable, tools/gen_diet_table.py), else from her clade. */
    private static Role roleOf(AbstractMonsterGirl g) {
        String r = com.linkyoshi90.harmontide.registry.DietTable.ROLE.get(g.getSpecies().id());
        if (r != null) return Role.valueOf(r);
        Clade c = Clade.of(g);
        if (c == null) return Role.NONE;
        return switch (c) {
            case FELIFORMIA, CANIDAE, ARCTOIDEA, DRAGON, ARCHOSAURIA, SQUAMATA, CHELICERATA -> Role.HUNTER;
            case ACTINOPTERYGII, WHIPPOMORPHA, MALACOSTRACA, MOLLUSCA, AMPHIBIA, TESTUDINES -> Role.FISHER;
            case UNDEAD, SLIME -> Role.SCAVENGER;
            case BOVIDAE, CERVIDAE, CAMELIDAE, PERISSODACTYLA, SUINA, LAGOMORPHA, RODENTIA, PAENUNGULATA, XENARTHRA, MARSUPIALIA, PRIMATES, HEXAPODA, AVES -> Role.GRAZER;
            default -> Role.NONE;                                          // plants, fae, devils, the made, riders: not part of it
        };
    }

    /** Wild, grown, free to roam: one who feeds herself. */
    public static boolean forages(AbstractMonsterGirl g) {
        return HarmontideConfig.foodWeb() && !g.isBaby() && !g.hasAnyAffection() && !g.isPassenger() && !g.isLeashed() && !g.isSleeping()
                && g.getAction() != AbstractMonsterGirl.Action.LYING && !g.isOrderedToSit() && role(g) != Role.NONE;
    }

    /** Hungry again {@link #MEAL} ticks after her last meal. One new to the world has eaten at some point in the last half day. */
    public static boolean hungry(AbstractMonsterGirl g) {
        var pd = g.getPersistentData();
        if (!pd.contains(LAST_MEAL)) pd.putLong(LAST_MEAL, g.level().getGameTime() - g.getRandom().nextInt(MEAL / 2));
        return g.level().getGameTime() - pd.getLong(LAST_MEAL) >= MEAL;
    }

    /** As if she hadn't eaten for a long while (tests, commands). */
    public static void starve(AbstractMonsterGirl g) {
        g.getPersistentData().putLong(LAST_MEAL, g.level().getGameTime() - MEAL);
    }

    /** She has eaten: full for a while. */
    public static void fed(ServerLevel level, AbstractMonsterGirl g) {
        g.getPersistentData().putLong(LAST_MEAL, level.getGameTime());
        g.heal(g.getMaxHealth());
        g.playSound(SoundEvents.GENERIC_EAT, 1.0F, 0.9F);
        Pollination.swallow(level, g);                                      // a fruit eater carries a seed away
    }

    // --- what is safe --------------------------------------------------------------------------------------------------

    /** Someone's livestock: tame, named, leashed, penned, in a village or by a hearth. Hunters leave it be. */
    public static boolean kept(ServerLevel level, LivingEntity a) {
        if (a instanceof OwnableEntity o && o.getOwnerUUID() != null || a.hasCustomName() || a instanceof Mob m && m.isLeashed() || a instanceof Bonded) return true;
        BlockPos at = a.blockPosition();
        if (level.isVillage(at)) return true;
        for (BlockPos h : HearthBlockEntity.loaded(level)) if (h.closerThan(at, HOME)) return true;
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-PEN, -1, -PEN), at.offset(PEN, 1, PEN))) {
            var b = level.getBlockState(p).getBlock();
            if (b instanceof FenceBlock || b instanceof FenceGateBlock || b instanceof WallBlock) return true;
        }
        return false;
    }

    /** What a hunter (or fisher) her size takes. */
    public static boolean prey(AbstractMonsterGirl hunter, LivingEntity a) {
        if (!a.isAlive() || a == hunter) return false;
        float size = hunter.getBbHeight();
        if (role(hunter) == Role.FISHER) return a instanceof AbstractFish || a instanceof Squid;
        if (role(hunter) != Role.HUNTER) return false;
        if (a instanceof AbstractFish && hunter.isInWater()) return true;
        if (a instanceof Rabbit || a instanceof Chicken) return true;
        if (size >= SMALL && (a instanceof Sheep || a instanceof Pig || a instanceof Goat)) return true;
        return size >= LARGE && a instanceof Cow;
    }

    @Nullable
    public static LivingEntity findPrey(ServerLevel level, AbstractMonsterGirl g) {
        LivingEntity best = null;
        for (LivingEntity a : level.getEntitiesOfClass(LivingEntity.class, g.getBoundingBox().inflate(SEARCH), a -> a instanceof Animal || a instanceof AbstractFish || a instanceof Squid)) {
            if (!prey(g, a) || kept(level, a)) continue;
            if (best == null || a.distanceToSqr(g) < best.distanceToSqr(g)) best = a;
        }
        return best;
    }

    /** How many wild prey animals are about: below {@link #SCARCE} the land is hunted out. */
    public static int preyAbout(ServerLevel level, AbstractMonsterGirl g) {
        return level.getEntitiesOfClass(LivingEntity.class, g.getBoundingBox().inflate(SEARCH * 1.5), a -> prey(g, a) && !kept(level, a)).size();
    }

    // --- hunting -----------------------------------------------------------------------------------------------------

    /** One blow at her prey (close enough). True once it is down and eaten. */
    public static boolean strike(ServerLevel level, AbstractMonsterGirl g, LivingEntity prey) {
        g.swing(InteractionHand.MAIN_HAND);
        g.getLookControl().setLookAt(prey);
        prey.hurt(g.damageSources().mobAttack(g), STRIKE);
        if (prey.isAlive()) return false;
        feast(level, g, prey.blockPosition());
        return true;
    }

    /** She eats from the kill where it fell (its carcass, entity/Carcass): her share comes off the bones and the rest is left for others. */
    public static void feast(ServerLevel level, AbstractMonsterGirl g, BlockPos at) {
        Carcass c = carcassNear(level, at, 3);
        if (c != null) c.bite(share(g));
        fed(level, g);
    }

    /** How much meat she eats at a meal: more the bigger she is. */
    public static int share(AbstractMonsterGirl g) {
        return g.getBbHeight() < SMALL ? 1 : g.getBbHeight() < LARGE ? 2 : 3;
    }

    /** The nearest carcass with meat on it, or null. */
    @Nullable
    public static Carcass carcassNear(ServerLevel level, BlockPos at, double range) {
        Carcass best = null;
        for (Carcass c : level.getEntitiesOfClass(Carcass.class, new AABB(at).inflate(range), c -> c.meat() > 0)) {
            if (best == null || c.distanceToSqr(at.getCenter()) < best.distanceToSqr(at.getCenter())) best = c;
        }
        return best;
    }

    // --- grazing -----------------------------------------------------------------------------------------------------

    public static boolean greens(BlockState s) {
        return s.is(Blocks.SHORT_GRASS) || s.is(Blocks.TALL_GRASS) || s.is(Blocks.FERN) || s.is(Blocks.LARGE_FERN) || s.is(BlockTags.SMALL_FLOWERS);
    }

    @Nullable
    public static BlockPos findGreens(ServerLevel level, AbstractMonsterGirl g) {
        RandomSource r = g.getRandom();
        BlockPos at = g.blockPosition();
        for (int i = 0; i < 24; i++) {
            BlockPos p = at.offset(r.nextInt(17) - 8, r.nextInt(5) - 2, r.nextInt(17) - 8);
            if (greens(level.getBlockState(p))) return p;
        }
        return null;
    }

    /** She crops the plant; where many graze, the turf below wears to dirt. True if she ate. */
    public static boolean graze(ServerLevel level, AbstractMonsterGirl g, BlockPos plant) {
        BlockState s = level.getBlockState(plant);
        if (!greens(s)) return false;
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, s), plant.getX() + 0.5, plant.getY() + 0.4, plant.getZ() + 0.5, 12, 0.3, 0.2, 0.3, 0.05);
        level.destroyBlock(plant, false);
        Pasture.note(level, plant, Pasture.Kind.CROPPED);                    // it grows back once the grazers move on
        int herd = level.getEntitiesOfClass(AbstractMonsterGirl.class, g.getBoundingBox().inflate(16), o -> role(o) == Role.GRAZER && forages(o)).size();
        BlockPos turf = plant.below();
        if (herd >= OVERGRAZE && level.getBlockState(turf).is(Blocks.GRASS_BLOCK) && g.getRandom().nextInt(3) == 0) {
            level.setBlockAndUpdate(turf, Blocks.DIRT.defaultBlockState());
            Pasture.note(level, turf, Pasture.Kind.WORN);
        }
        fed(level, g);
        return true;
    }

    // --- carrion -------------------------------------------------------------------------------------------------------

    public static void died(ServerLevel level, BlockPos at) {
        Deque<Carrion> list = CARRION.computeIfAbsent(level, l -> new ArrayDeque<>());
        list.addFirst(new Carrion(at.immutable(), level.getGameTime()));
        while (list.size() > CARRION_KEPT) list.removeLast();
    }

    /** The nearest fresh place where something died, or null. */
    @Nullable
    public static BlockPos carrion(ServerLevel level, BlockPos near, double range) {
        BlockPos best = null;
        for (Carrion c : CARRION.getOrDefault(level, new ArrayDeque<>())) {
            if (level.getGameTime() - c.time() > CARRION_TIME || !c.pos().closerThan(near, range)) continue;
            if (best == null || c.pos().distSqr(near) < best.distSqr(near)) best = c.pos();
        }
        return best;
    }

    public static boolean leftovers(ItemStack s) {
        return s.is(Items.ROTTEN_FLESH) || s.is(Items.BONE) || s.is(ItemTags.MEAT) || s.is(ItemTags.FISHES);
    }

    /** She eats what was left near her: off a carcass, or what lies about. True if there was something. */
    public static boolean scavenge(ServerLevel level, AbstractMonsterGirl g) {
        Carcass c = carcassNear(level, g.blockPosition(), 2.5);
        if (c != null && c.bite(share(g)) > 0) {
            fed(level, g);
            return true;
        }
        List<ItemEntity> found = level.getEntitiesOfClass(ItemEntity.class, g.getBoundingBox().inflate(2.5), i -> leftovers(i.getItem()));
        if (found.isEmpty()) return false;
        for (ItemEntity i : found) i.discard();
        fed(level, g);
        CARRION.getOrDefault(level, new ArrayDeque<>()).removeIf(k -> k.pos().closerThan(g.blockPosition(), 4));
        return true;
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity e = event.getEntity();
        if (e.level() instanceof ServerLevel level && !(e instanceof Player) && (e instanceof Animal || e instanceof net.minecraft.world.entity.monster.Enemy)) died(level, e.blockPosition());
    }

    // --- populations -------------------------------------------------------------------------------------------------

    /** Somewhere far off in a random direction, for a girl who is moving on. */
    public static BlockPos elsewhere(AbstractMonsterGirl g) {
        double a = g.getRandom().nextDouble() * Math.PI * 2;
        return g.blockPosition().offset((int) (Math.cos(a) * MOVE_ON), 0, (int) (Math.sin(a) * MOVE_ON));
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % REGROW_EVERY != 17 || !HarmontideConfig.foodWeb()) return;
        for (Player p : level.players()) regrow(level, p.blockPosition(), level.getRandom());
    }

    /**
     * The herds come back: at a random spot 32 to 64 blocks out, on open ground, where there are fewer than {@link #REGROW_LOW}
     * animals within {@link #REGROW_NEAR} and no wild hunter, a pair of the biome's own creatures turns up. What came, or empty.
     */
    public static List<Mob> regrow(ServerLevel level, BlockPos around, RandomSource r) {
        List<Mob> out = new ArrayList<>();
        double a = r.nextDouble() * Math.PI * 2, d = 32 + r.nextInt(33);
        int x = around.getX() + (int) (Math.cos(a) * d), z = around.getZ() + (int) (Math.sin(a) * d);
        if (!level.hasChunkAt(new BlockPos(x, 0, z))) return out;
        BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
        return regrowAt(level, ground, r);
    }

    public static List<Mob> regrowAt(ServerLevel level, BlockPos ground, RandomSource r) {
        List<Mob> out = new ArrayList<>();
        if (!level.getBlockState(ground.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON) || !level.getFluidState(ground).isEmpty() || level.isVillage(ground)) return out;
        AABB near = new AABB(ground).inflate(REGROW_NEAR);
        if (level.getEntitiesOfClass(Animal.class, near).size() >= REGROW_LOW) return out;
        if (!level.getEntitiesOfClass(AbstractMonsterGirl.class, near, g -> role(g) == Role.HUNTER && forages(g)).isEmpty()) return out;
        WeightedRandomList<MobSpawnSettings.SpawnerData> list = level.getBiome(ground).value().getMobSettings().getMobs(MobCategory.CREATURE);
        var pick = list.getRandom(r);
        if (pick.isEmpty()) return out;
        EntityType<?> type = pick.get().type;
        if (!type.getDescriptionId().startsWith("entity.minecraft.")) return out;    // the biome's own vanilla creatures, never our girls
        for (int i = 0; i < 2; i++) {
            if (!(type.create(level) instanceof Mob m)) continue;
            m.moveTo(ground.getX() + 0.5 + i, ground.getY(), ground.getZ() + 0.5, r.nextFloat() * 360.0F, 0.0F);
            m.finalizeSpawn(level, level.getCurrentDifficultyAt(ground), MobSpawnType.NATURAL, null);
            level.addFreshEntity(m);
            out.add(m);
        }
        return out;
    }

    // --- the goal ------------------------------------------------------------------------------------------------------

    /** Her foraging, whatever her role: find food, go to it, eat; or, finding none, move on. */
    public static final class ForageGoal extends Goal {
        private final AbstractMonsterGirl girl;
        @Nullable private LivingEntity prey;
        @Nullable private BlockPos spot;
        private int ticks, cooldown;
        private boolean movingOn, scavenging;

        public ForageGoal(AbstractMonsterGirl girl) {
            this.girl = girl;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (--this.cooldown > 0 || !(this.girl.level() instanceof ServerLevel level) || !forages(this.girl) || !hungry(this.girl) || this.girl.getTarget() != null) return false;
            this.cooldown = 100 + this.girl.getRandom().nextInt(100);
            this.prey = null;
            this.spot = null;
            this.movingOn = false;
            this.scavenging = false;
            switch (role(this.girl)) {
                case HUNTER, FISHER -> {
                    Carcass c = role(this.girl) == Role.HUNTER ? carcassNear(level, this.girl.blockPosition(), SEARCH / 2.0) : null;
                    if (c != null) {                                                // a free meal close by beats a chase
                        this.spot = c.blockPosition();
                        this.scavenging = true;
                        return true;
                    }
                    this.prey = findPrey(level, this.girl);
                    if (this.prey == null && !this.girl.hasRestriction() && preyAbout(level, this.girl) < SCARCE) {
                        this.spot = elsewhere(this.girl);
                        this.movingOn = true;
                    }
                }
                case GRAZER -> {
                    this.spot = findGreens(level, this.girl);
                    if (this.spot == null && !this.girl.hasRestriction() && this.girl.getRandom().nextInt(3) == 0) {
                        this.spot = elsewhere(this.girl);
                        this.movingOn = true;
                    }
                }
                case SCAVENGER -> {
                    Carcass c = carcassNear(level, this.girl.blockPosition(), SEARCH * 2);
                    this.spot = c != null ? c.blockPosition() : carrion(level, this.girl.blockPosition(), SEARCH * 2);
                }
                default -> {}
            }
            return this.prey != null || this.spot != null;
        }

        @Override
        public void start() {
            this.ticks = 0;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.ticks > 600 || this.girl.getTarget() != null || !forages(this.girl)) return false;
            if (this.prey != null) return this.prey.isAlive() && this.prey.distanceToSqr(this.girl) < SEARCH * SEARCH * 2;
            return this.spot != null && (this.movingOn || hungry(this.girl));
        }

        @Override
        public void tick() {
            this.ticks++;
            if (!(this.girl.level() instanceof ServerLevel level)) return;
            if (this.prey != null) {
                double reach = 1.8 + this.prey.getBbWidth();
                if (this.girl.distanceToSqr(this.prey) > reach * reach) {
                    if (this.ticks % 10 == 0) this.girl.getNavigation().moveTo(this.prey, 1.25);
                } else if (this.ticks % STRIKE_EVERY == 0 && strike(level, this.girl, this.prey)) {
                    this.prey = null;
                }
                return;
            }
            if (this.spot == null) return;
            if (this.girl.blockPosition().closerThan(this.spot, this.movingOn ? 6 : 2)) {
                switch (role(this.girl)) {
                    case GRAZER -> { if (!this.movingOn) graze(level, this.girl, this.spot); }
                    case SCAVENGER -> scavenge(level, this.girl);
                    case HUNTER -> { if (this.scavenging) scavenge(level, this.girl); }
                    default -> {}
                }
                this.spot = null;
            } else if (this.ticks % 20 == 0) {
                this.girl.getNavigation().moveTo(this.spot.getX() + 0.5, this.spot.getY(), this.spot.getZ() + 0.5, this.movingOn ? 1.0 : 1.1);
            }
        }

        @Override
        public void stop() {
            this.girl.getNavigation().stop();
            this.prey = null;
            this.spot = null;
        }
    }
}
