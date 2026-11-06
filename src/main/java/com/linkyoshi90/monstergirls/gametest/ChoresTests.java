package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.FridgeBlockEntity;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Chores;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.function.Predicate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Chores (entity.girl.Chores): eggs gathered, honey taken calmly, sheep shorn, fish caught into the fridge. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class ChoresTests {
    private static boolean criterion(ServerPlayer p, String c) {
        var a = p.server.getAdvancements().get(MonsterGirlsMod.id(Chores.EVERY));
        return a != null && p.getAdvancements().getOrStartProgress(a).getCriterion(c).isDone();
    }

    private static int count(Container box, Predicate<ItemStack> what) {
        int n = 0;
        for (int i = 0; i < box.getContainerSize(); i++) if (what.test(box.getItem(i))) n += box.getItem(i).getCount();
        return n;
    }

    private static int count(Container box, Item item) {
        return count(box, s -> s.is(item));
    }

    private static AbstractMonsterGirl onPatrol(GameTestHelper helper, ServerPlayer player, BlockPos at) {
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.CATTLE_MINOTAUR, at);
        girl.setAffection(player.getUUID(), AbstractMonsterGirl.BED_AFFECTION);
        girl.getOrders().set(Orders.Command.PATROL, player, girl.blockPosition());
        return girl;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "gathersTheEggs")
    public static void gathersTheEggs(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        BlockPos chest = new BlockPos(5, 2, 9);
        helper.setBlock(chest, Blocks.CHEST);
        AbstractMonsterGirl girl = onPatrol(helper, player, new BlockPos(6, 2, 6));
        var level = helper.getLevel();
        Vec3 egg = helper.absoluteVec(new Vec3(9.5, 2.1, 4.5)), tossed = helper.absoluteVec(new Vec3(3.5, 2.1, 7.5));
        level.addFreshEntity(new ItemEntity(level, egg.x, egg.y, egg.z, new ItemStack(Items.EGG)));
        ItemEntity mine = new ItemEntity(level, tossed.x, tossed.y, tossed.z, new ItemStack(Items.FEATHER));
        mine.setThrower(player);
        level.addFreshEntity(mine);
        check(!Chores.produce(mine), "what a player threw is hers to take, not a chore");
        helper.succeedWhen(() -> {
            var box = (Container) helper.getBlockEntity(chest);
            check(count(box, Items.EGG) == 1 && Chores.tally(girl, Chores.Kind.EGGS) == 1, "she gathers the egg into the chest");
            check(count(box, Items.FEATHER) == 0 && criterion(player, "eggs"), "and leaves the thrown feather; her friend gets the credit");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "honeyWithoutAngryBees")
    public static void honeyWithoutAngryBees(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        BlockPos hive = new BlockPos(8, 2, 8), chest = new BlockPos(5, 2, 9);
        helper.setBlock(chest, Blocks.CHEST);
        var box = (Container) helper.getBlockEntity(chest);
        box.setItem(0, new ItemStack(Items.GLASS_BOTTLE));
        helper.setBlock(hive, Blocks.BEEHIVE.defaultBlockState().setValue(BeehiveBlock.HONEY_LEVEL, BeehiveBlock.MAX_HONEY_LEVELS));
        Bee bee = helper.spawn(EntityType.BEE, new BlockPos(9, 3, 9));
        AbstractMonsterGirl girl = onPatrol(helper, player, new BlockPos(6, 2, 6));
        var level = helper.getLevel();
        BlockPos anchor = girl.getOrders().anchor(), at = helper.absolutePos(hive);
        check(Chores.harvestHive(level, girl, anchor, at), "she takes the honey");
        check(count(box, Items.HONEY_BOTTLE) == 1 && count(box, Items.GLASS_BOTTLE) == 0, "into the bottle from the chest");
        check(level.getBlockState(at).getValue(BeehiveBlock.HONEY_LEVEL) == 0 && !Chores.harvestHive(level, girl, anchor, at), "the hive is empty now");
        helper.setBlock(hive, Blocks.BEEHIVE.defaultBlockState().setValue(BeehiveBlock.HONEY_LEVEL, BeehiveBlock.MAX_HONEY_LEVELS));
        check(Chores.harvestHive(level, girl, anchor, at) && count(box, Items.HONEYCOMB) == 3, "no bottles left: honeycomb");
        check(!bee.isAngry() && Chores.tally(girl, Chores.Kind.HONEY) == 2 && criterion(player, "honey"), "and the bees stay calm");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "shearsTheSheep")
    public static void shearsTheSheep(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        BlockPos chest = new BlockPos(5, 2, 9);
        helper.setBlock(chest, Blocks.CHEST);
        Sheep sheep = helper.spawn(EntityType.SHEEP, new BlockPos(8, 2, 8));
        AbstractMonsterGirl girl = onPatrol(helper, player, new BlockPos(6, 2, 6));
        var level = helper.getLevel();
        check(Chores.shear(level, girl, girl.getOrders().anchor(), sheep) && sheep.isSheared(), "she shears the sheep");
        check(count((Container) helper.getBlockEntity(chest), s -> s.is(ItemTags.WOOL)) >= 1, "and the wool goes in the chest");
        check(!Chores.shear(level, girl, girl.getOrders().anchor(), sheep) && criterion(player, "wool"), "once until it grows back");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "fishesIntoTheFridge")
    public static void fishesIntoTheFridge(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        for (int x = 7; x <= 10; x++) for (int z = 3; z <= 7; z++) helper.setBlock(new BlockPos(x, 2, z), Blocks.WATER);
        BlockPos fridge = new BlockPos(3, 2, 9), chest = new BlockPos(5, 2, 9);
        helper.setBlock(fridge, ModBlocks.FRIDGE.get());
        helper.setBlock(chest, Blocks.CHEST);
        AbstractMonsterGirl girl = onPatrol(helper, player, new BlockPos(5, 2, 5));
        var level = helper.getLevel();
        BlockPos anchor = girl.getOrders().anchor();
        Chores.Spot spot = Chores.fishingSpot(level, girl, anchor);
        check(spot != null && level.getFluidState(spot.water()).isSource() && !level.getFluidState(spot.stand()).isSource()
                && spot.stand().distManhattan(spot.water()) <= 2, "she finds a spot on the bank");
        var store = ((FridgeBlockEntity) helper.getBlockEntity(fridge)).store();
        for (int i = 0; i < 20 && count(store, FridgeBlockEntity::isFood) == 0; i++) Chores.catchFish(level, girl, anchor, spot.water());
        check(count(store, FridgeBlockEntity::isFood) > 0 && count(store, s -> !s.isEmpty() && !FridgeBlockEntity.isFood(s)) == 0, "the fish go in the fridge");
        check(Chores.tally(girl, Chores.Kind.FISH) > 0 && criterion(player, "fish"), "and count as her chores");
        check(Chores.catchTime(girl) >= Chores.CATCH_MIN, "a bite takes a while");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "picksTheBerries")
    public static void picksTheBerries(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        BlockPos chest = new BlockPos(5, 2, 9), bush = new BlockPos(9, 2, 4), vine = new BlockPos(3, 3, 3);
        helper.setBlock(chest, Blocks.CHEST);
        helper.setBlock(bush, Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(net.minecraft.world.level.block.SweetBerryBushBlock.AGE, 3));
        helper.setBlock(vine.above(), Blocks.STONE);
        helper.setBlock(vine, Blocks.CAVE_VINES.defaultBlockState().setValue(net.minecraft.world.level.block.CaveVines.BERRIES, true));
        AbstractMonsterGirl girl = onPatrol(helper, player, new BlockPos(6, 2, 6));
        var level = helper.getLevel();
        check(Chores.pick(level, girl, girl.getOrders().anchor(), helper.absolutePos(vine)), "she picks the glow berries");
        check(!Chores.ripe(level.getBlockState(helper.absolutePos(vine))) && level.getBlockState(helper.absolutePos(vine)).is(Blocks.CAVE_VINES), "the vine stays");
        helper.succeedWhen(() -> {
            var box = (Container) helper.getBlockEntity(chest);
            check(count(box, Items.SWEET_BERRIES) >= 2 && count(box, Items.GLOW_BERRIES) == 1, "she walks to the bush and picks it too, into the chest");
            check(level.getBlockState(helper.absolutePos(bush)).getValue(net.minecraft.world.level.block.SweetBerryBushBlock.AGE) == 1
                    && Chores.tally(girl, Chores.Kind.BERRIES) == 2, "the bush stays to fruit again");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "henLaysInTheNestingBox")
    public static void henLaysInTheNestingBox(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        BlockPos nest = new BlockPos(6, 2, 6);
        helper.setBlock(nest, com.linkyoshi90.monstergirls.registry.ModBlocks.NESTING_BOX.get());
        var box = (com.linkyoshi90.monstergirls.block.NestingBoxBlockEntity) helper.getBlockEntity(nest);
        var hen = helper.spawn(EntityType.CHICKEN, new BlockPos(8, 2, 7));
        var level = helper.getLevel();
        ItemEntity laid = new ItemEntity(level, hen.getX(), hen.getY(), hen.getZ(), new ItemStack(Items.EGG));
        level.addFreshEntity(laid);
        check(count(box.eggs(), Items.EGG) == 1 && !laid.isAlive(), "the hen's egg goes into the box, not on the ground");
        check(helper.getBlockState(nest).getValue(com.linkyoshi90.monstergirls.block.NestingBoxBlock.EGGS), "and shows in the hay");
        Vec3 far = helper.absoluteVec(new Vec3(4.5, 2.1, 9.5));
        ItemEntity mine = new ItemEntity(level, far.x, far.y, far.z, new ItemStack(Items.EGG));
        mine.setThrower(player);
        level.addFreshEntity(mine);
        check(mine.isAlive() && count(box.eggs(), Items.EGG) == 1, "an egg a player dropped stays where it fell");
        check(box.takeAll().stream().mapToInt(ItemStack::getCount).sum() == 1 && box.eggs().isEmpty(), "an empty hand takes them out");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }
}
