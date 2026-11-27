package com.linkyoshi90.harmontide.entity.girl;

import com.linkyoshi90.harmontide.block.CheeseShelfBlockEntity;
import com.linkyoshi90.harmontide.entity.Dairy;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Work with the vanilla blocks around a home, each family to its own gift. Only a girl who is fond of someone (any affection)
 * works; a wild one minds her own business.
 *
 * <ul>
 *   <li><b>Dragons at the furnace.</b> A dragon goes to a furnace, blast furnace or smoker near her (any furnace-like block
 *   from other mods too) that has something in it to cook, and breathes on it: it burns without fuel and cooks twice as
 *   fast while she is there; she lights an unlit campfire too. Not the water dragons.</li>
 *   <li><b>Liches at the brewing stand.</b> A lich (or Koschei) keeps a stand with potions and an ingredient in it burning
 *   without blaze powder and brews twice as fast.</li>
 *   <li><b>Cheese.</b> Cattle minotaurs and holstaurs carry milk from a chest or barrel to a cheese shelf with room on it,
 *   and take the hard cheeses off the shelves to a chest that already holds cheese.</li>
 *   <li><b>Bees.</b> Bug girls (not the fairies, who build fountains) take the honey from full hives and nests without
 *   angering the bees, and calm the ones that are angry. Into a bottle from a nearby chest, else as honeycomb.</li>
 *   <li><b>Looms and fletching tables.</b> An arachne weaves the string in a chest by a loom into white wool (four to one); a
 *   wood or desert elf turns flint, sticks and feathers in a chest by a fletching table into arrows (four a set).</li>
 *   <li><b>Baking.</b> A holstaur bakes a cake (her own milk in it) from the sugar, egg and wheat in a chest by a crafting
 *   table; a brownie or a domovoi bakes bread from the wheat there.</li>
 *   <li><b>Stew.</b> The mooskuh fills the bowls in a chest near her with mushroom stew, one every couple of minutes.</li>
 *   <li><b>Cauldrons.</b> A slime girl fills an empty cauldron near her with what she is made of, once every five minutes:
 *   the undine with water, the frost slime with powder snow, the magma slime with lava.</li>
 *   <li><b>The beaver's axe.</b> Hand a beaver friend (10+) an axe: she fells the wild trees (logs with natural leaves on them,
 *   never anything built) around the spot where you gave it to her, one every few minutes, and puts the logs into the chest
 *   nearest that spot. Crouch with an empty hand to take the axe back.</li>
 *   <li><b>Forests.</b> Hand a rodent a stack of saplings and she plants them, a few blocks apart, all around the spot where
 *   you gave them to her.</li>
 * </ul>
 * The crab's mending at the smithing table is item/CrabMendRecipe; the zombie's company among the dead is entity/Traversal.
 */
public final class Jobs {
    /** How far from her a job may be, and how close she must stand to do it. */
    public static final int REACH = 12;
    public static final double HANDS = 2.6;
    /** Saplings a rodent plants at most this far from where she was given them, at least this far apart. */
    public static final int FOREST = 10, SPACING = 3;
    public static final String POUCH = "SaplingPouch", GROVE = "Grove", POUCH_FROM = "PouchFrom";
    /** The water dragons: they do not breathe fire. */
    public static final Set<GirlSpecies> COLD_DRAGONS = Set.of(GirlSpecies.LONG_DRAGON, GirlSpecies.JORMUNGANDR_DRAGON, GirlSpecies.KUN_DRAGON);
    public static final Set<GirlSpecies> FAIRIES = Set.of(GirlSpecies.FAIRY_BUG, GirlSpecies.FAIRY_QUEEN_BUG, GirlSpecies.PIXI_BUG);
    /** The sorceresses who know their way round a brewing stand. */
    public static final Set<GirlSpecies> BREWERS = Set.of(GirlSpecies.LICH_UNDEAD, GirlSpecies.KOSCHEI_UNDEAD);
    /** The archer elves, who fletch their own arrows. */
    public static final Set<GirlSpecies> FLETCHERS = Set.of(GirlSpecies.WOOD_ELF, GirlSpecies.DESERT_ELF);
    /** A workbench's makings must be in a chest this close to it. */
    public static final int CORNER = 6;
    public static final Set<GirlSpecies> DAIRY_COWS = Set.of(GirlSpecies.CATTLE_MINOTAUR, GirlSpecies.HOLSTAUR_MINOTAUR);

    private Jobs() {}

    /** Adds her family's job, if it has one (from the constructor: species only). */
    public static void install(AbstractMonsterGirl g) {
        GirlSpecies s = g.getSpecies();
        if (s.family() == GirlSpecies.Family.DRAGON && !COLD_DRAGONS.contains(s)) g.goalSelector.addGoal(4, new TendGoal(g, Jobs::fireToTend, Jobs::breatheOnFire));
        if (BREWERS.contains(s)) g.goalSelector.addGoal(4, new TendGoal(g, Jobs::standToTend, Jobs::brew));
        if (DAIRY_COWS.contains(s)) g.goalSelector.addGoal(4, new CheeseGoal(g));
        if (s.family() == GirlSpecies.Family.BUG && !FAIRIES.contains(s) && !s.id().contains("larva")) g.goalSelector.addGoal(4, new BeeGoal(g));
        if (s.family() == GirlSpecies.Family.RODENT) g.goalSelector.addGoal(4, new PlantGoal(g));
        if (s == GirlSpecies.BEAVER_RODENT) g.goalSelector.addGoal(4, new LumberGoal(g));
        if (s.family() == GirlSpecies.Family.ARACHNE) g.goalSelector.addGoal(4, new CraftGoal(g, Blocks.LOOM, WEAVE));
        if (FLETCHERS.contains(s)) g.goalSelector.addGoal(4, new CraftGoal(g, Blocks.FLETCHING_TABLE, FLETCH));
        if (s == GirlSpecies.MOOSKUH_MINOTAUR) g.goalSelector.addGoal(4, new StewGoal(g));
        if (s == GirlSpecies.HOLSTAUR_MINOTAUR) g.goalSelector.addGoal(4, new CraftGoal(g, Blocks.CRAFTING_TABLE, CAKE));
        if (BAKERS.contains(s)) g.goalSelector.addGoal(4, new CraftGoal(g, Blocks.CRAFTING_TABLE, BREAD));
        if (cauldronFill(s) != null) g.goalSelector.addGoal(4, new TendGoal(g, (l, at, r) -> emptyCauldron(l, g, at, r), Jobs::fillCauldron));
    }

    static boolean works(AbstractMonsterGirl g) {
        return com.linkyoshi90.harmontide.HarmontideConfig.blockJobs() && !g.isBaby() && g.hasAnyAffection() && !g.isOrderedToSit() && !g.isPassenger() && !g.isVehicle() && g.getTarget() == null
                && Orders.current(g) != Orders.Command.FOLLOW;
    }

    /** Containers (chests, barrels...) within reach of a spot, nearest first is not promised. */
    static Iterable<BlockPos> around(BlockPos at, int r) {
        return BlockPos.betweenClosed(at.offset(-r, -3, -r), at.offset(r, 3, r));
    }

    @Nullable
    static Container container(ServerLevel level, BlockPos p) {
        BlockEntity be = level.getBlockEntity(p);
        return be instanceof Container c && !(be instanceof AbstractFurnaceBlockEntity) && !(be instanceof net.minecraft.world.level.block.entity.HopperBlockEntity) ? c : null;
    }

    static boolean holds(Container c, Predicate<ItemStack> what) {
        for (int i = 0; i < c.getContainerSize(); i++) if (what.test(c.getItem(i))) return true;
        return false;
    }

    /** Puts a stack into a container (merging first); what did not fit. */
    static ItemStack put(Container c, ItemStack stack) {
        for (int i = 0; i < c.getContainerSize() && !stack.isEmpty(); i++) {
            ItemStack in = c.getItem(i);
            if (!in.isEmpty() && ItemStack.isSameItemSameComponents(in, stack) && in.getCount() < in.getMaxStackSize()) {
                int move = Math.min(stack.getCount(), in.getMaxStackSize() - in.getCount());
                in.grow(move);
                stack.shrink(move);
            }
        }
        for (int i = 0; i < c.getContainerSize() && !stack.isEmpty(); i++) {
            if (c.getItem(i).isEmpty() && c.canPlaceItem(i, stack)) {
                c.setItem(i, stack.copy());
                stack.setCount(0);
            }
        }
        c.setChanged();
        return stack;
    }

    // --- the errand: walk to a block, do a thing, walk to the next ------------------------------------------------------

    /** Close enough to work a block: within reach across, and not far above or below it (a flier hovers). */
    static boolean within(AbstractMonsterGirl g, BlockPos at) {
        double dx = g.getX() - (at.getX() + 0.5), dz = g.getZ() - (at.getZ() + 0.5), dy = g.getY() - at.getY();
        return dx * dx + dz * dz <= (HANDS + 0.4) * (HANDS + 0.4) && dy > -2.5 && dy < 3.0;
    }

    /** A short errand of steps (go there, do that). The job plans it; she walks it; whatever she carries is dropped if it fails. */
    abstract static class ErrandGoal extends Goal {
        protected final AbstractMonsterGirl girl;
        private final Deque<Step> steps = new ArrayDeque<>();
        protected ItemStack carried = ItemStack.EMPTY;
        private int timeout, cooldown;

        private record Step(BlockPos at, Predicate<ServerLevel> act) {}

        ErrandGoal(AbstractMonsterGirl girl) {
            this.girl = girl;
            this.cooldown = 40 + girl.getRandom().nextInt(100);
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        /** Plans the next errand with {@link #step}; false when there is nothing to do. */
        protected abstract boolean plan(ServerLevel level);

        protected int every() {
            return 100;
        }

        protected void step(BlockPos at, Predicate<ServerLevel> act) {
            this.steps.add(new Step(at.immutable(), act));
        }

        @Override
        public boolean canUse() {
            if (--this.cooldown > 0 || !(this.girl.level() instanceof ServerLevel level)) return false;
            this.cooldown = this.every();
            if (!works(this.girl)) return false;
            this.steps.clear();
            return this.plan(level) && !this.steps.isEmpty();
        }

        @Override
        public void start() {
            this.timeout = 600;
            this.walk();
        }

        private void walk() {
            Step s = this.steps.peek();
            if (s != null) this.girl.getNavigation().moveTo(s.at.getX() + 0.5, s.at.getY(), s.at.getZ() + 0.5, 1.0);
        }

        @Override
        public boolean canContinueToUse() {
            return !this.steps.isEmpty() && this.timeout > 0 && works(this.girl);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            this.timeout--;
            Step s = this.steps.peek();
            if (s == null || !(this.girl.level() instanceof ServerLevel level)) return;
            Vec3 c = Vec3.atCenterOf(s.at);
            this.girl.getLookControl().setLookAt(c.x, c.y, c.z);
            if (within(this.girl, s.at)) {
                this.girl.getNavigation().stop();
                this.girl.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                if (s.act.test(level)) {
                    this.steps.poll();
                    this.walk();
                } else {
                    this.steps.clear();
                }
            } else if (this.girl.getNavigation().isDone() && this.timeout % 20 == 0) {
                this.walk();
            }
        }

        @Override
        public void stop() {
            this.steps.clear();
            if (!this.carried.isEmpty()) {
                this.girl.spawnAtLocation(this.carried);
                this.carried = ItemStack.EMPTY;
            }
        }
    }

    // --- dragons at the furnace -------------------------------------------------------------------------------------

    /** The recipes this furnace cooks with. */
    static RecipeType<? extends net.minecraft.world.item.crafting.AbstractCookingRecipe> recipesOf(AbstractFurnaceBlockEntity f) {
        return f instanceof BlastFurnaceBlockEntity ? RecipeType.BLASTING : f instanceof SmokerBlockEntity ? RecipeType.SMOKING : RecipeType.SMELTING;
    }

    /** Something in it that it can cook. */
    public static boolean cooking(ServerLevel level, AbstractFurnaceBlockEntity f) {
        ItemStack in = f.getItem(0);
        return !in.isEmpty() && hasRecipe(level, recipesOf(f), in);
    }

    private static <T extends net.minecraft.world.item.crafting.AbstractCookingRecipe> boolean hasRecipe(ServerLevel level, RecipeType<T> type, ItemStack in) {
        return level.getRecipeManager().getRecipeFor(type, new SingleRecipeInput(in), level).isPresent();
    }

    /** A furnace near her with something to cook in it, the nearest, or null. */
    @Nullable
    public static BlockPos furnaceToTend(ServerLevel level, BlockPos at, int r) {
        BlockPos best = null;
        for (BlockPos p : around(at, r)) {
            if (level.getBlockEntity(p) instanceof AbstractFurnaceBlockEntity f && cooking(level, f) && (best == null || p.distSqr(at) < best.distSqr(at))) best = p.immutable();
        }
        return best;
    }

    /** Her breath on it (every second she stands by): it burns without fuel and cooks a second's worth more. Whether it was a furnace with work. */
    public static boolean breatheOn(ServerLevel level, AbstractMonsterGirl dragon, BlockPos p) {
        if (!(level.getBlockEntity(p) instanceof AbstractFurnaceBlockEntity f) || !cooking(level, f)) return false;
        if (f.litTime < 40) {
            f.litTime = 100;
            f.litDuration = Math.max(f.litDuration, 100);
            BlockState s = level.getBlockState(p);
            if (s.hasProperty(AbstractFurnaceBlock.LIT) && !s.getValue(AbstractFurnaceBlock.LIT)) level.setBlock(p, s.setValue(AbstractFurnaceBlock.LIT, true), 3);
        }
        if (f.cookingTotalTime > 0) f.cookingProgress = Math.min(f.cookingTotalTime - 1, f.cookingProgress + 20);
        f.setChanged();
        Vec3 mouth = dragon.getEyePosition();
        Vec3 to = Vec3.atCenterOf(p).subtract(mouth);
        for (int i = 1; i <= 4; i++) {
            Vec3 at = mouth.add(to.scale(i / 5.0));
            level.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 1, 0.05, 0.05, 0.05, 0.01);
        }
        if (dragon.getRandom().nextInt(4) == 0) level.playSound(null, p, SoundEvents.BLAZE_SHOOT, SoundSource.NEUTRAL, 0.3F, 1.4F);
        return true;
    }

    /** An unlit campfire near her, or null. */
    @Nullable
    public static BlockPos unlitCampfire(ServerLevel level, BlockPos at, int r) {
        for (BlockPos p : around(at, r)) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof net.minecraft.world.level.block.CampfireBlock && !s.getValue(net.minecraft.world.level.block.CampfireBlock.LIT)
                    && !s.getValue(net.minecraft.world.level.block.CampfireBlock.WATERLOGGED)) return p.immutable();
        }
        return null;
    }

    /** What a dragon would breathe on: a furnace with work in it, else an unlit campfire. */
    @Nullable
    public static BlockPos fireToTend(ServerLevel level, BlockPos at, int r) {
        BlockPos furnace = furnaceToTend(level, at, r);
        return furnace != null ? furnace : unlitCampfire(level, at, r);
    }

    /** Her breath on a furnace (see the furnace one), or on an unlit campfire: it catches. Whether there was anything to do. */
    public static boolean breatheOnFire(ServerLevel level, AbstractMonsterGirl dragon, BlockPos p) {
        BlockState s = level.getBlockState(p);
        if (s.getBlock() instanceof net.minecraft.world.level.block.CampfireBlock) {
            if (s.getValue(net.minecraft.world.level.block.CampfireBlock.LIT) || s.getValue(net.minecraft.world.level.block.CampfireBlock.WATERLOGGED)) return false;
            level.setBlock(p, s.setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true), 11);
            level.playSound(null, p, SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 0.6F, 1.2F);
            return false;                                                    // lit: done
        }
        return breatheOn(level, dragon, p);
    }

    // --- liches at the brewing stand ------------------------------------------------------------------------------------

    /** A brewing stand with potions and an ingredient in it near her, or null. */
    @Nullable
    public static BlockPos standToTend(ServerLevel level, BlockPos at, int r) {
        for (BlockPos p : around(at, r)) {
            if (level.getBlockEntity(p) instanceof net.minecraft.world.level.block.entity.BrewingStandBlockEntity b && brewing(b)) return p.immutable();
        }
        return null;
    }

    static boolean brewing(net.minecraft.world.level.block.entity.BrewingStandBlockEntity b) {
        if (b.getItem(3).isEmpty()) return false;
        for (int i = 0; i < 3; i++) if (!b.getItem(i).isEmpty()) return true;
        return false;
    }

    /** Her sorcery on a stand (every second): it never wants blaze powder and brews twice as fast. Whether it had work. */
    public static boolean brew(ServerLevel level, AbstractMonsterGirl lich, BlockPos p) {
        if (!(level.getBlockEntity(p) instanceof net.minecraft.world.level.block.entity.BrewingStandBlockEntity b) || !brewing(b)) return false;
        if (b.fuel <= 0) b.fuel = 20;
        if (b.brewTime > 20) b.brewTime -= 20;
        b.setChanged();
        level.sendParticles(ParticleTypes.WITCH, p.getX() + 0.5, p.getY() + 0.9, p.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0.0);
        return true;
    }

    /** Finds a block to work at near her. */
    @FunctionalInterface
    interface Finder {
        @Nullable
        BlockPos find(ServerLevel level, BlockPos at, int r);
    }

    /** Works a block, once a second; false when there is nothing (more) to do there. */
    @FunctionalInterface
    interface Work {
        boolean work(ServerLevel level, AbstractMonsterGirl girl, BlockPos p);
    }

    /** She walks to a block that wants her and stays by it, working it once a second, until it is done. */
    static final class TendGoal extends Goal {
        private final AbstractMonsterGirl girl;
        private final Finder finder;
        private final Work work;
        @Nullable
        private BlockPos at;
        private int cooldown;
        private boolean done;

        TendGoal(AbstractMonsterGirl girl, Finder finder, Work work) {
            this.girl = girl;
            this.finder = finder;
            this.work = work;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (--this.cooldown > 0 || !(this.girl.level() instanceof ServerLevel level) || !works(this.girl)) return false;
            this.cooldown = 60;
            this.at = this.finder.find(level, this.girl.blockPosition(), REACH);
            this.done = false;
            return this.at != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.at != null && !this.done && works(this.girl);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.at == null || !(this.girl.level() instanceof ServerLevel level)) return;
            Vec3 c = Vec3.atCenterOf(this.at);
            this.girl.getLookControl().setLookAt(c.x, c.y, c.z);
            if (!within(this.girl, this.at)) {
                if (this.girl.getNavigation().isDone()) this.girl.getNavigation().moveTo(c.x, c.y, c.z, 1.0);
                return;
            }
            this.girl.getNavigation().stop();
            if (this.girl.tickCount % 20 == 0 && !this.work.work(level, this.girl, this.at)) this.done = true;
        }

        @Override
        public void stop() {
            this.at = null;
        }
    }

    // --- cheese ------------------------------------------------------------------------------------------------------

    /** A cheese shelf near her with a hard cheese on it, or with room for milk. */
    @Nullable
    static BlockPos shelf(ServerLevel level, BlockPos at, Predicate<CheeseShelfBlockEntity> want) {
        for (BlockPos p : around(at, REACH)) if (level.getBlockEntity(p) instanceof CheeseShelfBlockEntity s && want.test(s)) return p.immutable();
        return null;
    }

    static boolean hasHard(CheeseShelfBlockEntity s) {
        for (int i = 0; i < CheeseShelfBlockEntity.CHEESES; i++) if (Dairy.hardness(s.store().getItem(i)) >= 2) return true;
        return false;
    }

    static boolean hasRoom(CheeseShelfBlockEntity s) {
        return s.count() < CheeseShelfBlockEntity.CHEESES && s.store().getItem(CheeseShelfBlockEntity.CHEESES).getCount() < 16;
    }

    /** A chest or barrel near a spot holding something this test likes, or null. */
    @Nullable
    static BlockPos containerWith(ServerLevel level, BlockPos at, Predicate<ItemStack> what) {
        for (BlockPos p : around(at, REACH)) {
            Container c = container(level, p);
            if (c != null && holds(c, what)) return p.immutable();
        }
        return null;
    }

    static final class CheeseGoal extends ErrandGoal {
        CheeseGoal(AbstractMonsterGirl girl) {
            super(girl);
        }

        @Override
        protected boolean plan(ServerLevel level) {
            BlockPos at = this.girl.blockPosition();
            BlockPos ripe = shelf(level, at, Jobs::hasHard);
            if (ripe != null) {
                BlockPos cellar = containerWith(level, ripe, s -> s.is(ModItems.CHEESE.get()));
                if (cellar != null) {
                    this.step(ripe, l -> {                                       // the hardest cheese off the shelf
                        if (!(l.getBlockEntity(ripe) instanceof CheeseShelfBlockEntity s) || !hasHard(s)) return false;
                        this.carried = s.takeRipest();
                        return !this.carried.isEmpty();
                    });
                    this.step(cellar, l -> {                                     // into the chest with the rest of the cheese
                        Container c = container(l, cellar);
                        if (c == null) return false;
                        this.carried = put(c, this.carried);
                        return this.carried.isEmpty();
                    });
                    return true;
                }
            }
            BlockPos empty = shelf(level, at, Jobs::hasRoom);
            if (empty == null) return false;
            BlockPos dairy = containerWith(level, empty, Dairy::isMilk);
            if (dairy == null) return false;
            this.step(dairy, l -> {                                              // a bucket of milk out of the chest
                Container c = container(l, dairy);
                if (c == null) return false;
                for (int i = 0; i < c.getContainerSize(); i++) {
                    if (Dairy.isMilk(c.getItem(i))) {
                        this.carried = c.removeItem(i, 1);
                        c.setChanged();
                        return true;
                    }
                }
                return false;
            });
            this.step(empty, l -> {                                              // set on the shelf: a soft cheese; the bucket goes back
                if (!(l.getBlockEntity(empty) instanceof CheeseShelfBlockEntity s) || !s.addMilk(this.carried)) return false;
                ItemStack bucket = this.carried.getCraftingRemainingItem();
                this.carried = ItemStack.EMPTY;
                Container c = container(l, dairy);
                if (!bucket.isEmpty() && (c == null || !put(c, bucket).isEmpty())) this.girl.spawnAtLocation(bucket);
                return true;
            });
            return true;
        }
    }

    // --- looms and fletching tables -----------------------------------------------------------------------------------

    /** What goes into one go at a workbench, and what comes out. */
    public record Recipe(java.util.Map<net.minecraft.world.item.Item, Integer> needs, ItemStack makes) {
        boolean ready(Container c) {
            for (var e : this.needs.entrySet()) if (count(c, e.getKey()) < e.getValue()) return false;
            return true;
        }
    }

    public static final Recipe WEAVE = new Recipe(java.util.Map.of(Items.STRING, 4), new ItemStack(Items.WHITE_WOOL));
    /** The holstaur's cake (her own milk in it) and the house spirits' bread. */
    public static final Recipe CAKE = new Recipe(java.util.Map.of(Items.SUGAR, 2, Items.EGG, 1, Items.WHEAT, 3), new ItemStack(Items.CAKE));
    public static final Recipe BREAD = new Recipe(java.util.Map.of(Items.WHEAT, 3), new ItemStack(Items.BREAD));
    public static final Set<GirlSpecies> BAKERS = Set.of(GirlSpecies.BROWNIE_ELF, GirlSpecies.DOMOVOI_ELF);
    public static final Recipe FLETCH = new Recipe(java.util.Map.of(Items.FLINT, 1, Items.STICK, 1, Items.FEATHER, 1), new ItemStack(Items.ARROW, 4));

    static int count(Container c, net.minecraft.world.item.Item item) {
        int n = 0;
        for (int i = 0; i < c.getContainerSize(); i++) if (c.getItem(i).is(item)) n += c.getItem(i).getCount();
        return n;
    }

    /** Takes this many of an item out of a container (it must hold them). */
    static void take(Container c, net.minecraft.world.item.Item item, int n) {
        for (int i = 0; i < c.getContainerSize() && n > 0; i++) {
            ItemStack s = c.getItem(i);
            if (!s.is(item)) continue;
            int t = Math.min(n, s.getCount());
            s.shrink(t);
            n -= t;
        }
        c.setChanged();
    }

    /** A workbench of this kind near her with a chest of its makings beside it: {bench, chest}, or null. */
    @Nullable
    public static BlockPos[] bench(ServerLevel level, BlockPos at, Block kind, Recipe recipe) {
        for (BlockPos p : around(at, REACH)) {
            if (!level.getBlockState(p).is(kind)) continue;
            for (BlockPos q : around(p, CORNER)) {
                Container c = container(level, q);
                if (c != null && recipe.ready(c)) return new BlockPos[] {p.immutable(), q.immutable()};
            }
        }
        return null;
    }

    /** One go: the makings out of the chest, worked at the bench, the result back into the chest. */
    static final class CraftGoal extends ErrandGoal {
        private final Block kind;
        private final Recipe recipe;

        CraftGoal(AbstractMonsterGirl girl, Block kind, Recipe recipe) {
            super(girl);
            this.kind = kind;
            this.recipe = recipe;
        }

        @Override
        protected int every() {
            return 200;
        }

        @Override
        protected boolean plan(ServerLevel level) {
            BlockPos[] found = bench(level, this.girl.blockPosition(), this.kind, this.recipe);
            if (found == null) return false;
            BlockPos table = found[0], chest = found[1];
            this.step(chest, l -> {
                Container c = container(l, chest);
                if (c == null || !this.recipe.ready(c)) return false;
                for (var e : this.recipe.needs().entrySet()) take(c, e.getKey(), e.getValue());
                return true;
            });
            this.step(table, l -> {
                this.carried = this.recipe.makes().copy();
                l.playSound(null, table, this.kind == Blocks.LOOM ? SoundEvents.UI_LOOM_TAKE_RESULT : this.kind == Blocks.FLETCHING_TABLE
                        ? SoundEvents.VILLAGER_WORK_FLETCHER : SoundEvents.VILLAGER_WORK_FARMER, SoundSource.NEUTRAL, 0.8F, 1.0F);
                l.sendParticles(ParticleTypes.HAPPY_VILLAGER, table.getX() + 0.5, table.getY() + 1.1, table.getZ() + 0.5, 3, 0.3, 0.1, 0.3, 0.0);
                return true;
            });
            this.step(chest, l -> {
                Container c = container(l, chest);
                if (c != null) this.carried = put(c, this.carried);
                return this.carried.isEmpty();
            });
            return true;
        }
    }

    // --- the mooskuh's stew --------------------------------------------------------------------------------------------

    static final class StewGoal extends ErrandGoal {
        StewGoal(AbstractMonsterGirl girl) {
            super(girl);
        }

        @Override
        protected int every() {
            return 1200;
        }

        @Override
        protected boolean plan(ServerLevel level) {
            BlockPos chest = containerWith(level, this.girl.blockPosition(), st -> st.is(Items.BOWL));
            if (chest == null) return false;
            this.step(chest, l -> {
                Container c = container(l, chest);
                if (c == null || count(c, Items.BOWL) < 1) return false;
                take(c, Items.BOWL, 1);
                ItemStack left = put(c, new ItemStack(Items.MUSHROOM_STEW));
                if (!left.isEmpty()) this.girl.spawnAtLocation(left);
                l.playSound(null, chest, SoundEvents.MOOSHROOM_MILK, SoundSource.NEUTRAL, 0.8F, 1.0F);
                return true;
            });
            return true;
        }
    }

    // --- slimes fill cauldrons ------------------------------------------------------------------------------------------

    public static final int CAULDRON_REST = 6000;

    /** The filled cauldron this slime makes, or null for a slime who fills none. */
    @Nullable
    public static BlockState cauldronFill(GirlSpecies s) {
        return switch (s) {
            case UNDINE_SLIME -> Blocks.WATER_CAULDRON.defaultBlockState().setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, 3);
            case FROST_SLIME -> Blocks.POWDER_SNOW_CAULDRON.defaultBlockState().setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, 3);
            case MAGMA_SLIME -> Blocks.LAVA_CAULDRON.defaultBlockState();
            default -> null;
        };
    }

    /** An empty cauldron near her, if she is rested, or null. */
    @Nullable
    static BlockPos emptyCauldron(ServerLevel level, AbstractMonsterGirl g, BlockPos at, int r) {
        if (level.getGameTime() < g.getPersistentData().getLong("CauldronRestUntil")) return null;
        for (BlockPos p : around(at, r)) if (level.getBlockState(p).is(Blocks.CAULDRON)) return p.immutable();
        return null;
    }

    /** She fills it, then rests. Always done after one go (false: nothing more to do there). */
    public static boolean fillCauldron(ServerLevel level, AbstractMonsterGirl g, BlockPos p) {
        BlockState fill = cauldronFill(g.getSpecies());
        if (fill == null || !level.getBlockState(p).is(Blocks.CAULDRON)) return false;
        level.setBlockAndUpdate(p, fill);
        g.getPersistentData().putLong("CauldronRestUntil", level.getGameTime() + CAULDRON_REST);
        level.playSound(null, p, fill.is(Blocks.LAVA_CAULDRON) ? SoundEvents.BUCKET_EMPTY_LAVA : fill.is(Blocks.POWDER_SNOW_CAULDRON)
                ? SoundEvents.BUCKET_EMPTY_POWDER_SNOW : SoundEvents.BUCKET_EMPTY, SoundSource.NEUTRAL, 0.8F, 1.0F);
        level.sendParticles(ParticleTypes.ITEM_SLIME, p.getX() + 0.5, p.getY() + 1.0, p.getZ() + 0.5, 8, 0.3, 0.2, 0.3, 0.05);
        return false;
    }

    // --- the beaver fells trees ---------------------------------------------------------------------------------------------

    public static final String AXE = "LumberAxe", WOODS = "LumberAt";
    public static final int FELL_MAX = 64;

    public static ItemStack axe(AbstractMonsterGirl g) {
        CompoundTag d = g.getPersistentData();
        return d.contains(AXE) ? ItemStack.parseOptional(g.registryAccess(), d.getCompound(AXE)) : ItemStack.EMPTY;
    }

    static void setAxe(AbstractMonsterGirl g, ItemStack s) {
        if (s.isEmpty()) {
            g.getPersistentData().remove(AXE);
            g.getPersistentData().remove(WOODS);
        } else {
            g.getPersistentData().put(AXE, s.save(g.registryAccess()));
        }
    }

    /** An axe handed to a beaver friend (she starts on the trees round here), or a crouching empty hand taking it back. */
    @Nullable
    public static InteractionResult beaverAxe(AbstractMonsterGirl g, Player player, ItemStack stack) {
        if (g.getSpecies() != GirlSpecies.BEAVER_RODENT || g.isBaby() || g.getAffection(player) < Orders.AFFECTION || !com.linkyoshi90.harmontide.HarmontideConfig.blockJobs()) return null;
        if (stack.is(ItemTags.AXES) && !player.isShiftKeyDown()) {
            if (!g.level().isClientSide) {
                ItemStack had = axe(g);
                if (!had.isEmpty()) player.getInventory().placeItemBackInInventory(had);
                setAxe(g, stack.split(1));
                g.getPersistentData().putLong(WOODS, g.blockPosition().asLong());
                player.displayClientMessage(Component.translatable("message.harmontide.beaver_axe", g.getDisplayName()), true);
            }
            return InteractionResult.sidedSuccess(g.level().isClientSide);
        }
        if (stack.isEmpty() && player.isShiftKeyDown() && !axe(g).isEmpty()) {
            if (!g.level().isClientSide) {
                player.getInventory().placeItemBackInInventory(axe(g));
                setAxe(g, ItemStack.EMPTY);
            }
            return InteractionResult.sidedSuccess(g.level().isClientSide);
        }
        return null;
    }

    /** Natural leaves (not placed by a player) touching this log. */
    static boolean leafy(ServerLevel level, BlockPos log) {
        for (BlockPos q : BlockPos.betweenClosed(log.offset(-1, -1, -1), log.offset(1, 1, 1))) {
            BlockState s = level.getBlockState(q);
            if (s.getBlock() instanceof net.minecraft.world.level.block.LeavesBlock && !s.getValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT)) return true;
        }
        return false;
    }

    /** The logs of one wild tree from its bottom log up (connected, at most {@link #FELL_MAX}), or empty if it is not a wild tree. */
    public static java.util.List<BlockPos> tree(ServerLevel level, BlockPos bottom) {
        java.util.List<BlockPos> logs = new java.util.ArrayList<>();
        if (!level.getBlockState(bottom).is(net.minecraft.tags.BlockTags.LOGS) || !level.getBlockState(bottom.below()).is(net.minecraft.tags.BlockTags.DIRT)) return logs;
        Deque<BlockPos> todo = new ArrayDeque<>(java.util.List.of(bottom));
        java.util.Set<BlockPos> seen = new java.util.HashSet<>();
        boolean leaves = false;
        while (!todo.isEmpty() && logs.size() < FELL_MAX) {
            BlockPos p = todo.poll();
            if (!seen.add(p) || !level.getBlockState(p).is(net.minecraft.tags.BlockTags.LOGS)) continue;
            logs.add(p);
            if (!leaves && leafy(level, p)) leaves = true;
            for (BlockPos q : BlockPos.betweenClosed(p.offset(-1, 0, -1), p.offset(1, 1, 1))) if (q.getY() >= bottom.getY()) todo.add(q.immutable());
        }
        if (!leaves) logs.clear();                                              // bare logs: somebody built that
        return logs;
    }

    /** The bottom log of a wild tree near the woods spot, or null. */
    @Nullable
    static BlockPos wildTree(ServerLevel level, BlockPos woods) {
        for (BlockPos p : BlockPos.betweenClosed(woods.offset(-REACH, -3, -REACH), woods.offset(REACH, 3, REACH))) {
            if (level.getBlockState(p).is(net.minecraft.tags.BlockTags.LOGS) && level.getBlockState(p.below()).is(net.minecraft.tags.BlockTags.DIRT)
                    && !tree(level, p.immutable()).isEmpty()) return p.immutable();
        }
        return null;
    }

    /** She fells it: every log of it, as items. The axe wears; at its end she stops. */
    public static java.util.List<ItemStack> fell(ServerLevel level, AbstractMonsterGirl beaver, BlockPos bottom) {
        java.util.List<ItemStack> wood = new java.util.ArrayList<>();
        ItemStack axe = axe(beaver);
        for (BlockPos p : tree(level, bottom)) {
            if (axe.isEmpty()) break;
            BlockState s = level.getBlockState(p);
            wood.add(new ItemStack(s.getBlock().asItem()));
            level.destroyBlock(p, false, beaver);
            axe.setDamageValue(axe.getDamageValue() + 1);
            if (axe.getDamageValue() >= axe.getMaxDamage()) axe = ItemStack.EMPTY;
        }
        setAxe(beaver, axe);
        if (axe.isEmpty()) beaver.playSound(SoundEvents.ITEM_BREAK, 1.0F, 1.0F);
        return wood;
    }

    static final class LumberGoal extends ErrandGoal {
        private final java.util.List<ItemStack> logs = new java.util.ArrayList<>();

        LumberGoal(AbstractMonsterGirl girl) {
            super(girl);
        }

        @Override
        protected int every() {
            return 1200;
        }

        @Override
        protected boolean plan(ServerLevel level) {
            CompoundTag d = this.girl.getPersistentData();
            if (axe(this.girl).isEmpty() || !d.contains(WOODS) || !level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return false;
            BlockPos woods = BlockPos.of(d.getLong(WOODS));
            BlockPos bottom = wildTree(level, woods);
            if (bottom == null) return false;
            this.step(bottom, l -> {
                this.logs.clear();
                this.logs.addAll(fell(l, this.girl, bottom));
                return !this.logs.isEmpty();
            });
            BlockPos store = containerWith(level, woods, st -> true);
            BlockPos chest = store != null ? store : woods;
            this.step(chest, l -> {
                Container c = container(l, chest);
                for (ItemStack w : this.logs) {
                    ItemStack left = c == null ? w : put(c, w);
                    if (!left.isEmpty()) this.girl.spawnAtLocation(left);
                }
                this.logs.clear();
                return true;
            });
            return true;
        }

        @Override
        public void stop() {
            super.stop();
            for (ItemStack w : this.logs) this.girl.spawnAtLocation(w);
            this.logs.clear();
        }
    }

    // --- bees --------------------------------------------------------------------------------------------------------

    static final class BeeGoal extends ErrandGoal {
        BeeGoal(AbstractMonsterGirl girl) {
            super(girl);
        }

        @Override
        protected int every() {
            return 160;
        }

        @Override
        protected boolean plan(ServerLevel level) {
            for (Bee b : level.getEntitiesOfClass(Bee.class, this.girl.getBoundingBox().inflate(REACH), Bee::isAngry)) b.stopBeingAngry();   // hush
            if (!level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return false;
            BlockPos hive = null;
            for (BlockPos p : around(this.girl.blockPosition(), REACH)) {
                if (Chores.fullHive(level.getBlockState(p))) {
                    hive = p.immutable();
                    break;
                }
            }
            if (hive == null) return false;
            BlockPos h = hive;
            this.step(h, l -> Chores.harvestHive(l, this.girl, h, h));
            return true;
        }
    }

    // --- rodents plant forests ------------------------------------------------------------------------------------------

    public static boolean isSapling(ItemStack s) {
        return s.is(ItemTags.SAPLINGS) && s.getItem() instanceof BlockItem;
    }

    public static ItemStack pouch(AbstractMonsterGirl g) {
        CompoundTag d = g.getPersistentData();
        return d.contains(POUCH) ? ItemStack.parseOptional(g.registryAccess(), d.getCompound(POUCH)) : ItemStack.EMPTY;
    }

    static void setPouch(AbstractMonsterGirl g, ItemStack s) {
        if (s.isEmpty()) g.getPersistentData().remove(POUCH);
        else g.getPersistentData().put(POUCH, s.save(g.registryAccess()));
    }

    @Nullable
    public static BlockPos grove(AbstractMonsterGirl g) {
        CompoundTag d = g.getPersistentData();
        return d.contains(GROVE) ? BlockPos.of(d.getLong(GROVE)) : null;
    }

    /** A friend (10+) hands a rodent saplings: she takes the stack (any she still had of another kind back to them) and plants them about here. */
    @Nullable
    public static InteractionResult giveSaplings(AbstractMonsterGirl g, Player player, ItemStack stack) {
        if (!com.linkyoshi90.harmontide.HarmontideConfig.blockJobs() || g.getSpecies().family() != GirlSpecies.Family.RODENT || !isSapling(stack) || g.isBaby() || player.isShiftKeyDown()
                || g.getAffection(player) < Orders.AFFECTION) return null;
        if (g.level().isClientSide) return InteractionResult.SUCCESS;
        ItemStack had = pouch(g);
        if (!had.isEmpty() && !ItemStack.isSameItemSameComponents(had, stack)) {
            player.getInventory().placeItemBackInInventory(had);
            had = ItemStack.EMPTY;
        }
        int take = Math.min(stack.getCount(), 64 - had.getCount());
        ItemStack in = stack.copyWithCount(had.getCount() + take);
        stack.shrink(take);
        setPouch(g, in);
        g.getPersistentData().putLong(GROVE, g.blockPosition().asLong());
        g.getPersistentData().putUUID(POUCH_FROM, player.getUUID());
        g.addAffection(player, 1);
        player.displayClientMessage(Component.translatable("message.harmontide.forest_given", g.getDisplayName(), in.getCount(), in.getHoverName()), true);
        return InteractionResult.SUCCESS;
    }

    /** Somewhere near the grove a sapling of this kind would grow, clear of trees and other saplings; or null. */
    @Nullable
    public static BlockPos plantingSpot(ServerLevel level, BlockPos grove, ItemStack sapling, net.minecraft.util.RandomSource random) {
        BlockState plant = ((BlockItem) sapling.getItem()).getBlock().defaultBlockState();
        for (int i = 0; i < 24; i++) {
            BlockPos p = grove.offset(random.nextInt(2 * FOREST + 1) - FOREST, 3, random.nextInt(2 * FOREST + 1) - FOREST);
            for (int dy = 0; dy < 7; dy++, p = p.below()) {
                if (!level.getBlockState(p).isAir() || level.getBlockState(p.below()).isAir()) continue;
                if (!plant.canSurvive(level, p) || level.getRawBrightness(p, 0) < 9) break;   // (daylight, whatever the hour)
                boolean crowded = false;
                for (BlockPos q : BlockPos.betweenClosed(p.offset(-SPACING + 1, -1, -SPACING + 1), p.offset(SPACING - 1, 3, SPACING - 1))) {
                    BlockState s = level.getBlockState(q);
                    if (s.is(net.minecraft.tags.BlockTags.SAPLINGS) || s.is(net.minecraft.tags.BlockTags.LOGS)) {
                        crowded = true;
                        break;
                    }
                }
                if (!crowded) return p.immutable();
                break;
            }
        }
        return null;
    }

    static final class PlantGoal extends ErrandGoal {
        PlantGoal(AbstractMonsterGirl girl) {
            super(girl);
        }

        @Override
        protected int every() {
            return 60;
        }

        @Override
        protected boolean plan(ServerLevel level) {
            ItemStack bag = pouch(this.girl);
            BlockPos grove = grove(this.girl);
            if (bag.isEmpty() || grove == null || !isSapling(bag) || !level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return false;
            BlockPos spot = plantingSpot(level, grove, bag, this.girl.getRandom());
            if (spot == null) return false;
            this.step(spot, l -> plant(l, this.girl, spot));
            return true;
        }
    }

    /** One sapling from her pouch into the ground here. Whether it took. */
    public static boolean plant(ServerLevel level, AbstractMonsterGirl g, BlockPos at) {
        ItemStack bag = pouch(g);
        if (bag.isEmpty() || !isSapling(bag) || !level.getBlockState(at).isAir()) return false;
        BlockState plant = ((BlockItem) bag.getItem()).getBlock().defaultBlockState();
        if (!plant.canSurvive(level, at)) return false;
        level.setBlock(at, plant, 3);
        level.playSound(null, at, SoundEvents.GRASS_PLACE, SoundSource.NEUTRAL, 0.8F, 1.1F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, at.getX() + 0.5, at.getY() + 0.4, at.getZ() + 0.5, 4, 0.3, 0.2, 0.3, 0.0);
        bag.shrink(1);
        setPouch(g, bag);
        if (bag.isEmpty() && g.getPersistentData().hasUUID(POUCH_FROM)) {
            Player from = level.getPlayerByUUID(g.getPersistentData().getUUID(POUCH_FROM));
            if (from != null && from.distanceToSqr(g) < 64 * 64) from.displayClientMessage(Component.translatable("message.harmontide.forest_done", g.getDisplayName()), true);
            g.getPersistentData().remove(GROVE);
            g.getPersistentData().remove(POUCH_FROM);
        }
        return true;
    }
}
