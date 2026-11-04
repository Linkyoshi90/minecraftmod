package com.linkyoshi90.monstergirls.entity.girl;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.FridgeBlockEntity;
import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.Orders;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

/**
 * Chores (after the farmhand, FarmhandGoal): a friend on patrol (harpies and anurans too) keeps the place around her patrol spot.
 * She gathers the eggs, feathers and wool lying about, takes the honey from a full hive without angering the bees (into a glass
 * bottle if there is one in a chest, else as honeycomb), shears woolly sheep, and when there is nothing else to do she fishes from
 * the shore (a swimmer from the water), a catch every minute or two. Food goes into a fridge in the area, the rest into a chest
 * or barrel, or at her feet. Shearing and honey obey mobGriefing; gathering and fishing don't need it. Each friend keeps a tally,
 * and her commander earns two advancements.
 */
public final class Chores {
    public enum Kind {EGGS, HONEY, WOOL, FISH}

    public static final String TALLY = "Chores", FIRST = "first_chore", EVERY = "every_chore";
    public static final int CATCH_MIN = 900, CATCH_SPREAD = 1500;
    /** How much open water a fishing spot needs around it (sources in the five by five). */
    public static final int OPEN_WATER = 8;

    private Chores() {
    }

    /** What lies about a farm that she picks up: eggs, feathers, wool, honeycomb, hides and scutes. Not anything a player threw. */
    public static boolean produce(ItemEntity item) {
        ItemStack s = item.getItem();
        return item.isAlive() && item.getOwner() == null && (s.is(Items.EGG) || s.is(Items.FEATHER) || s.is(ItemTags.WOOL) || s.is(Items.HONEYCOMB)
                || s.is(Items.RABBIT_HIDE) || s.is(Items.TURTLE_SCUTE) || s.is(Items.ARMADILLO_SCUTE));
    }

    /** Her patrol area. */
    public static AABB area(BlockPos anchor) {
        double r = Orders.PATROL_RADIUS;
        return new AABB(anchor).inflate(r, 3, r);
    }

    // --- putting things away --------------------------------------------------------------------------------------

    /** Food into a fridge in the area, the rest (or what the fridge has no room for) into a chest or barrel. What is left over. */
    public static ItemStack store(ServerLevel level, BlockPos anchor, ItemStack stack) {
        int r = (int) Orders.PATROL_RADIUS;
        if (FridgeBlockEntity.isFood(stack)) {
            for (BlockPos p : BlockPos.betweenClosed(anchor.offset(-r, -3, -r), anchor.offset(r, 3, r))) {
                if (level.getBlockEntity(p) instanceof FridgeBlockEntity f) stack = HopperBlockEntity.addItem(null, f.store(), stack, null);
                if (stack.isEmpty()) return stack;
            }
        }
        for (BlockPos p : BlockPos.betweenClosed(anchor.offset(-r, -3, -r), anchor.offset(r, 3, r))) {
            if (level.getBlockEntity(p) instanceof Container box) stack = HopperBlockEntity.addItem(null, box, stack, null);
            if (stack.isEmpty()) return stack;
        }
        return stack;
    }

    private static void putAway(ServerLevel level, Mob mob, BlockPos anchor, ItemStack stack) {
        ItemStack left = store(level, anchor, stack);
        if (!left.isEmpty()) mob.spawnAtLocation(left);
    }

    /** Takes a glass bottle from a chest in the area. Whether there was one. */
    private static boolean takeBottle(ServerLevel level, BlockPos anchor) {
        int r = (int) Orders.PATROL_RADIUS;
        for (BlockPos p : BlockPos.betweenClosed(anchor.offset(-r, -3, -r), anchor.offset(r, 3, r))) {
            if (!(level.getBlockEntity(p) instanceof Container box)) continue;
            for (int i = 0; i < box.getContainerSize(); i++) {
                if (box.getItem(i).is(Items.GLASS_BOTTLE)) {
                    box.removeItem(i, 1);
                    return true;
                }
            }
        }
        return false;
    }

    /** One chore done: her tally, and her commander's advancements. */
    static void done(Mob mob, Kind kind) {
        var tag = mob.getPersistentData().getCompound(TALLY);
        String k = kind.name().toLowerCase(java.util.Locale.ROOT);
        tag.putInt(k, tag.getInt(k) + 1);
        mob.getPersistentData().put(TALLY, tag);
        if (mob instanceof Bonded b && b.getOrders().commander(mob) instanceof ServerPlayer sp) {
            award(sp, FIRST, "done");
            award(sp, EVERY, k);
        }
    }

    /** How many of a chore she has done. */
    public static int tally(Mob mob, Kind kind) {
        return mob.getPersistentData().getCompound(TALLY).getInt(kind.name().toLowerCase(java.util.Locale.ROOT));
    }

    private static void award(ServerPlayer sp, String id, String criterion) {
        AdvancementHolder a = sp.server.getAdvancements().get(MonsterGirlsMod.id(id));
        if (a != null) sp.getAdvancements().award(a, criterion);
    }

    // --- the chores -------------------------------------------------------------------------------------------------

    /** She picks up something lying about and puts it away. Whether she did. */
    public static boolean gather(ServerLevel level, Mob mob, BlockPos anchor, ItemEntity item) {
        if (!produce(item)) return false;
        ItemStack stack = item.getItem().copy();
        mob.take(item, stack.getCount());
        item.discard();
        putAway(level, mob, anchor, stack);
        done(mob, Kind.EGGS);
        return true;
    }

    public static boolean fullHive(BlockState s) {
        return s.getBlock() instanceof BeehiveBlock && s.getValue(BeehiveBlock.HONEY_LEVEL) >= BeehiveBlock.MAX_HONEY_LEVELS;
    }

    /** She takes the honey from a full hive, calmly: the bees stay inside and stay calm. Whether she did. */
    public static boolean harvestHive(ServerLevel level, Mob mob, BlockPos anchor, BlockPos hive) {
        BlockState s = level.getBlockState(hive);
        if (!fullHive(s)) return false;
        boolean bottle = takeBottle(level, anchor);
        level.playSound(null, hive, bottle ? SoundEvents.BOTTLE_FILL : SoundEvents.BEEHIVE_SHEAR, SoundSource.NEUTRAL, 1.0F, 1.0F);
        ((BeehiveBlock) s.getBlock()).resetHoneyLevel(level, s, hive);
        level.gameEvent(mob, GameEvent.SHEAR, hive);
        mob.swing(InteractionHand.MAIN_HAND);
        putAway(level, mob, anchor, bottle ? new ItemStack(Items.HONEY_BOTTLE) : new ItemStack(Items.HONEYCOMB, 3));
        done(mob, Kind.HONEY);
        return true;
    }

    /** She shears a woolly sheep and puts the wool away. Whether she did. */
    public static boolean shear(ServerLevel level, Mob mob, BlockPos anchor, Sheep sheep) {
        if (!sheep.isAlive() || !sheep.readyForShearing()) return false;
        sheep.shear(SoundSource.NEUTRAL);
        sheep.gameEvent(GameEvent.SHEAR, mob);
        mob.swing(InteractionHand.MAIN_HAND);
        for (ItemEntity wool : level.getEntitiesOfClass(ItemEntity.class, sheep.getBoundingBox().inflate(2.0), Chores::produce)) {
            ItemStack stack = wool.getItem().copy();
            wool.discard();
            putAway(level, mob, anchor, stack);
        }
        done(mob, Kind.WOOL);
        return true;
    }

    /** Where she fishes from: a dry spot beside open water (or, for a swimmer, the water itself), and the water. */
    public record Spot(BlockPos stand, BlockPos water) {
    }

    private static boolean openWater(ServerLevel level, BlockPos p) {
        if (!level.getFluidState(p).is(FluidTags.WATER) || !level.getFluidState(p).isSource() || !level.getBlockState(p.above()).isAir()) return false;
        int n = 0;
        for (BlockPos q : BlockPos.betweenClosed(p.offset(-2, 0, -2), p.offset(2, 0, 2))) {
            if (level.getFluidState(q).is(FluidTags.WATER) && level.getFluidState(q).isSource()) n++;
        }
        return n >= OPEN_WATER;
    }

    private static boolean standable(ServerLevel level, BlockPos p) {
        BlockState s = level.getBlockState(p), above = level.getBlockState(p.above());
        return level.getBlockState(p.below()).isFaceSturdy(level, p.below(), Direction.UP) && s.canBeReplaced() && s.getFluidState().isEmpty()
                && above.canBeReplaced() && above.getFluidState().isEmpty();
    }

    /** The nearest fishing spot in her patrol area, or null. */
    @Nullable
    public static Spot fishingSpot(ServerLevel level, Mob mob, BlockPos anchor) {
        boolean swimmer = mob instanceof Bonded b && b.needsWater();
        int r = (int) Orders.PATROL_RADIUS;
        Spot best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.betweenClosed(anchor.offset(-r, -3, -r), anchor.offset(r, 2, r))) {
            if (!openWater(level, p)) continue;
            BlockPos w = p.immutable();
            if (swimmer) {
                double d = w.distSqr(mob.blockPosition());
                if (d < bestDist) {
                    bestDist = d;
                    best = new Spot(w, w);
                }
                continue;
            }
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos stand = w.relative(dir);
                for (int dy = 0; dy <= 1; dy++) {                        // the bank, or a block up from the water
                    BlockPos s = stand.above(dy);
                    if (!standable(level, s)) continue;
                    double d = s.distSqr(mob.blockPosition());
                    if (d < bestDist) {
                        bestDist = d;
                        best = new Spot(s, w);
                    }
                }
            }
        }
        return best;
    }

    /** How long a bite takes: a minute or two, half that for a swimmer. */
    public static int catchTime(Mob mob) {
        int t = CATCH_MIN + mob.getRandom().nextInt(CATCH_SPREAD);
        return mob instanceof Bonded b && b.needsWater() ? t / 2 : t;
    }

    /** A catch from the water: the fishing loot (fish and junk; treasure only comes to a real hook in open water), put away. */
    public static List<ItemStack> catchFish(ServerLevel level, Mob mob, BlockPos anchor, BlockPos water) {
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(water))
                .withParameter(LootContextParams.TOOL, new ItemStack(Items.FISHING_ROD))
                .withParameter(LootContextParams.THIS_ENTITY, mob)
                .create(LootContextParamSets.FISHING);
        List<ItemStack> loot = level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING).getRandomItems(params);
        Vec3 at = Vec3.atCenterOf(water).add(0, 0.5, 0);
        level.sendParticles(ParticleTypes.SPLASH, at.x, at.y, at.z, 12, 0.3, 0.1, 0.3, 0.1);
        level.playSound(null, water, SoundEvents.FISHING_BOBBER_SPLASH, SoundSource.NEUTRAL, 0.6F, 1.0F);
        mob.swing(InteractionHand.MAIN_HAND);
        for (ItemStack s : loot) putAway(level, mob, anchor, s.copy());
        done(mob, Kind.FISH);
        return loot;
    }

    /** Whether there are chores around a patrol spot (the patrol order says she will do them). */
    public static boolean hasWork(ServerLevel level, Mob mob, BlockPos anchor) {
        if (!MonsterGirlsConfig.chores()) return false;
        AABB box = area(anchor);
        if (!level.getEntitiesOfClass(net.minecraft.world.entity.animal.Chicken.class, box).isEmpty()
                || !level.getEntitiesOfClass(Sheep.class, box).isEmpty()) return true;
        int r = (int) Orders.PATROL_RADIUS;
        for (BlockPos p : BlockPos.betweenClosed(anchor.offset(-r, -3, -r), anchor.offset(r, 3, r))) {
            if (level.getBlockState(p).getBlock() instanceof BeehiveBlock) return true;
        }
        return fishingSpot(level, mob, anchor) != null;
    }

    /** The chores, as a goal: the next one at hand (eggs, then honey, then wool), or fishing when there are none. */
    public static class ChoresGoal<T extends PathfinderMob & Bonded> extends Goal {
        private final T girl;
        @Nullable
        private Kind kind;
        @Nullable
        private BlockPos at;
        @Nullable
        private BlockPos water;
        @Nullable
        private net.minecraft.world.entity.Entity target;
        private int cooldown, ticks, bite;

        public ChoresGoal(T girl) {
            this.girl = girl;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        /** On duty: patrolling for a player she is fond of (who is in this world), grown, not fighting. */
        public boolean onDuty() {
            Orders o = this.girl.getOrders();
            Player boss = o.commander(this.girl);
            return MonsterGirlsConfig.chores() && o.command(this.girl) == Orders.Command.PATROL && o.anchor() != null && boss != null
                    && this.girl.getAffection(boss) >= Orders.AFFECTION && !this.girl.isBaby() && this.girl.getTarget() == null
                    && !this.girl.isPassenger() && this.girl.level() instanceof ServerLevel;
        }

        private boolean griefs(ServerLevel level) {
            return net.neoforged.neoforge.event.EventHooks.canEntityGrief(level, this.girl);
        }

        @Override
        public boolean canUse() {
            if (--this.cooldown > 0 || !this.onDuty() || !(this.girl.level() instanceof ServerLevel level)) return false;
            this.cooldown = 60;
            BlockPos anchor = this.girl.getOrders().anchor();
            AABB box = area(anchor);
            boolean swimmer = this.girl.needsWater();
            this.target = null;
            this.at = null;
            this.water = null;
            if (!swimmer) {
                ItemEntity item = this.nearest(level.getEntitiesOfClass(ItemEntity.class, box, Chores::produce));
                if (item != null) return this.choose(Kind.EGGS, item, item.blockPosition());
                if (this.griefs(level)) {
                    BlockPos hive = null;
                    double best = Double.MAX_VALUE;
                    int r = (int) Orders.PATROL_RADIUS;
                    for (BlockPos p : BlockPos.betweenClosed(anchor.offset(-r, -3, -r), anchor.offset(r, 3, r))) {
                        if (!fullHive(level.getBlockState(p))) continue;
                        double d = p.distSqr(this.girl.blockPosition());
                        if (d < best) {
                            best = d;
                            hive = p.immutable();
                        }
                    }
                    if (hive != null) return this.choose(Kind.HONEY, null, hive);
                    Sheep sheep = this.nearest(level.getEntitiesOfClass(Sheep.class, box, s -> s.isAlive() && s.readyForShearing()));
                    if (sheep != null) return this.choose(Kind.WOOL, sheep, sheep.blockPosition());
                }
            }
            if (!level.isDay()) return false;                              // she fishes by day
            Spot spot = fishingSpot(level, this.girl, anchor);
            if (spot == null) return false;
            this.water = spot.water();
            return this.choose(Kind.FISH, null, spot.stand());
        }

        @Nullable
        private <E extends net.minecraft.world.entity.Entity> E nearest(List<E> list) {
            E best = null;
            double bestDist = Double.MAX_VALUE;
            for (E e : list) {
                double d = e.distanceToSqr(this.girl);
                if (d < bestDist) {
                    bestDist = d;
                    best = e;
                }
            }
            return best;
        }

        private boolean choose(Kind kind, @Nullable net.minecraft.world.entity.Entity target, BlockPos at) {
            this.kind = kind;
            this.target = target;
            this.at = at;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.kind == null || this.at == null || !this.onDuty()) return false;
            if (this.target != null && !this.target.isAlive()) return false;
            return this.kind == Kind.FISH ? this.ticks < this.bite + 400 && this.girl.level().isDay() : this.ticks < 300;
        }

        @Override
        public void start() {
            this.ticks = 0;
            this.bite = -1;
        }

        @Override
        public void stop() {
            this.kind = null;
            this.target = null;
            this.at = null;
            this.water = null;
            this.girl.getNavigation().stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.kind == null || this.at == null || !(this.girl.level() instanceof ServerLevel level)) return;   // done: the goal ends at the next check
            this.ticks++;
            BlockPos dest = this.target != null ? this.target.blockPosition() : this.at;
            BlockPos look = this.kind == Kind.FISH && this.water != null ? this.water : dest;
            this.girl.getLookControl().setLookAt(look.getX() + 0.5, look.getY() + 0.5, look.getZ() + 0.5);
            double reach = this.kind == Kind.FISH ? 1.2 : this.kind == Kind.EGGS ? 1.6 : 2.5;
            if (this.girl.distanceToSqr(Vec3.atBottomCenterOf(dest)) > reach * reach) {
                if (this.ticks % 10 == 1) this.girl.getNavigation().moveTo(dest.getX() + 0.5, dest.getY(), dest.getZ() + 0.5, 1.0);
                if (this.bite > 0) this.bite++;                            // walked off: the wait starts over when she is back
                return;
            }
            this.girl.getNavigation().stop();
            BlockPos anchor = this.girl.getOrders().anchor();
            if (anchor == null) return;
            switch (this.kind) {
                case EGGS -> {
                    if (this.target instanceof ItemEntity item) gather(level, this.girl, anchor, item);
                    this.kind = null;
                }
                case HONEY -> {
                    harvestHive(level, this.girl, anchor, this.at);
                    this.kind = null;
                }
                case WOOL -> {
                    if (this.target instanceof Sheep sheep) shear(level, this.girl, anchor, sheep);
                    this.kind = null;
                }
                case FISH -> {
                    if (this.bite < 0) this.bite = this.ticks + catchTime(this.girl);
                    if (this.water != null && this.ticks % 40 == 0) {
                        Vec3 w = Vec3.atCenterOf(this.water).add(0, 0.5, 0);
                        level.sendParticles(ParticleTypes.FISHING, w.x, w.y, w.z, 1, 0.1, 0, 0.1, 0);   // her line in the water
                    }
                    if (this.ticks >= this.bite && this.water != null) {
                        catchFish(level, this.girl, anchor, this.water);
                        this.kind = null;
                        this.cooldown = 1;
                    }
                }
            }
        }
    }
}
