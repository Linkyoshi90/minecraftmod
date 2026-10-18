package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.CrowCaches;
import com.linkyoshi90.monstergirls.entity.CrowHarpy;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;

/** Run with: ./gradlew runGameTestServer. Floor is at relative y=1. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class CrowHarpyTests {

    private static List<ItemEntity> itemsAround(GameTestHelper helper, double radius) {
        AABB box = new AABB(helper.absolutePos(new BlockPos(7, 2, 7))).inflate(radius);
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, box);
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "tradingShiniesMakesAFriendAndEarnsAGift")
    public static void tradingShiniesMakesAFriendAndEarnsAGift(GameTestHelper helper) {
        CrowHarpy crow = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(7, 2, 7));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLD_NUGGET, 2));
        crow.interact(player, InteractionHand.MAIN_HAND);
        check(player.getMainHandItem().getCount() == 1, "the nugget should be taken");
        check(crow.isFriend(player), "a trade should make a friend");
        helper.succeedWhen(() -> check(!itemsAround(helper, 16).isEmpty(), "no gift yet"));
    }

    // She tries about once a minute while a victim is within 10 blocks, so give her time and keep the player central.
    @GameTest(template = PLATFORM, timeoutTicks = 24000, batch = "pickpocketsAndDropsTheLootDownTheRoad")
    public static void pickpocketsAndDropsTheLootDownTheRoad(GameTestHelper helper) {
        CrowHarpy crow = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(3, 2, 3));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 7));
        player.getInventory().setItem(12, new ItemStack(Items.GOLD_INGOT));
        player.getInventory().setItem(13, new ItemStack(Items.COBBLESTONE, 8));
        helper.succeedWhen(() -> {
            check(player.getInventory().countItem(Items.GOLD_INGOT) == 0, "hasn't stolen the gold (shinies first) yet");
            // Down the road she either drops it, or buries it (caching) to keep.
            check(itemsAround(helper, 32).stream().anyMatch(i -> i.getItem().is(Items.GOLD_INGOT))
                    || CrowCaches.get(helper.getLevel()).of(crow.getUUID()).stream().anyMatch(c -> c.item().is(Items.GOLD_INGOT)),
                    "hasn't dropped or buried the loot yet");
            check(player.getInventory().countItem(Items.COBBLESTONE) == 8, "should only take one thing");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 3100, batch = "neverRobsAFriend")
    public static void neverRobsAFriend(GameTestHelper helper) {
        CrowHarpy crow = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(3, 2, 3));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(10, 2, 10));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_NUGGET));
        crow.interact(player, InteractionHand.MAIN_HAND);
        player.getInventory().setItem(12, new ItemStack(Items.DIAMOND));
        helper.runAtTickTime(3000, () -> {
            check(player.getInventory().countItem(Items.DIAMOND) == 1, "robbed a friend");
            HarpyTestUtil.remove(player);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 8000, batch = "knocksOnWood")
    public static void knocksOnWood(GameTestHelper helper) {
        helper.setBlock(new BlockPos(10, 2, 10), Blocks.OAK_LOG);
        helper.setBlock(new BlockPos(10, 3, 10), Blocks.OAK_LOG);
        CrowHarpy crow = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(3, 2, 3));
        helper.succeedWhen(() -> {
            check(crow.getAction() == AbstractHarpy.Action.KNOCKING, "not knocking yet");
            check(crow.distanceToSqr(helper.absoluteVec(new BlockPos(10, 2, 10).getCenter())) < 2.5 * 2.5, "knocking but not at the log");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "remembersTrapsAndIgnoresBaitThere")
    public static void remembersTrapsAndIgnoresBaitThere(GameTestHelper helper) {
        CrowHarpy crow = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(7, 2, 7));
        crow.hurt(helper.getLevel().damageSources().cactus(), 1.0F);
        BlockPos trap = crow.blockPosition();
        check(!crow.isSafeSpot(trap), "the spot should be remembered as a trap");
        crow.setPos(helper.absoluteVec(new BlockPos(2, 2, 2).getBottomCenter()));
        ItemEntity bait = new ItemEntity(helper.getLevel(), trap.getX() + 0.5, trap.getY(), trap.getZ() + 0.5, new ItemStack(Items.GOLD_NUGGET));
        helper.getLevel().addFreshEntity(bait);
        helper.runAtTickTime(250, () -> {
            check(bait.isAlive() && !crow.isHoldingSomething(), "took the bait from a known trap");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, batch = "grudgesAreSharedWithTheMurder")
    public static void grudgesAreSharedWithTheMurder(GameTestHelper helper) {
        CrowHarpy victim = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(4, 2, 4));
        CrowHarpy witness = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(10, 2, 10));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        victim.hurt(helper.getLevel().damageSources().playerAttack(player), 1.0F);
        check(victim.holdsGrudgeAgainst(player), "victim should hold a grudge");
        check(witness.holdsGrudgeAgainst(player), "the murder should share the grudge");
        helper.succeed();
    }

    private static void plantField(GameTestHelper helper) {
        for (int x = 9; x <= 11; x++) {
            for (int z = 9; z <= 11; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.FARMLAND);
                helper.setBlock(new BlockPos(x, 2, z), ((CropBlock) Blocks.WHEAT).getStateForAge(7));
            }
        }
    }

    private static boolean anyCropPecked(GameTestHelper helper) {
        for (int x = 9; x <= 11; x++) {
            for (int z = 9; z <= 11; z++) {
                var state = helper.getBlockState(new BlockPos(x, 2, z));
                if (state.getBlock() instanceof CropBlock crop && crop.getAge(state) == 0) return true;
            }
        }
        return false;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 8000, batch = "murdersRaidFields")
    public static void murdersRaidFields(GameTestHelper helper) {
        helper.setDayTime(1000);
        plantField(helper);
        helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(3, 2, 3));
        helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(3, 2, 5));
        helper.succeedWhen(() -> check(anyCropPecked(helper), "no crop pecked yet"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 3100, batch = "scarecrowsProtectFields")
    public static void scarecrowsProtectFields(GameTestHelper helper) {
        helper.setDayTime(1000);
        plantField(helper);
        helper.setBlock(new BlockPos(12, 2, 12), Blocks.CARVED_PUMPKIN);
        helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(3, 2, 3));
        helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(3, 2, 5));
        helper.runAtTickTime(3000, () -> {
            check(!anyCropPecked(helper), "crows raided a field guarded by a scarecrow");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "opensDoorsOnHerWay")
    public static void opensDoorsOnHerWay(GameTestHelper helper) {
        // A wall across the whole platform (up to the ceiling) with one oak door in it.
        for (int z = 0; z < 15; z++) {
            for (int y = 2; y < 8; y++) helper.setBlock(new BlockPos(7, y, z), Blocks.STONE_BRICKS);
        }
        BlockPos door = new BlockPos(7, 2, 7);
        helper.setBlock(door, Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.EAST).setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        helper.setBlock(door.above(), Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.EAST).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        CrowHarpy crow = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(3, 2, 7));
        var goal = helper.absoluteVec(new BlockPos(11, 2, 7).getBottomCenter());
        boolean[] through = {false};
        helper.onEachTick(() -> {
            through[0] |= helper.relativeVec(crow.position()).x > 9.0;
            // Keep re-issuing the route so her own wandering doesn't win (she still has to open the door herself).
            if (!through[0] && (crow.getNavigation().isDone() || helper.getTick() % 60 == 0)) crow.getNavigation().moveTo(goal.x, goal.y, goal.z, 1.0);
        });
        helper.succeedWhen(() -> {
            check(through[0], "hasn't got through the door");
            check(!helper.getBlockState(door).getValue(DoorBlock.OPEN), "should close the door behind her");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "hatchlingImprintsOnWhoeverFeedsItAndFollows")
    public static void hatchlingImprintsOnWhoeverFeedsItAndFollows(GameTestHelper helper) {
        CrowHarpy chick = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(3, 2, 3));
        chick.setHatchling(true);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 3));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT_SEEDS));
        chick.interact(player, InteractionHand.MAIN_HAND);
        check(chick.isOwnedBy(player) && chick.isFollowingOwner(), "should imprint on the feeder");
        var far = helper.absoluteVec(new BlockPos(12, 2, 12).getBottomCenter());
        player.teleportTo(far.x, far.y, far.z);
        helper.succeedWhen(() -> {
            check(chick.getBbHeight() < 1.0F, "hatchling should be small, height=" + chick.getBbHeight());
            check(chick.distanceToSqr(player) < 4.0 * 4.0, "not following");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, batch = "hatchlingGrowsUpAfterADay")
    public static void hatchlingGrowsUpAfterADay(GameTestHelper helper) {
        CrowHarpy chick = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(7, 2, 7));
        chick.setHatchling(true);
        var tag = new net.minecraft.nbt.CompoundTag();
        chick.saveWithoutId(tag);
        tag.putInt("HatchlingAge", 23990);
        chick.load(tag);
        check(chick.isHatchling(), "the saved scale modifier should keep it a hatchling");
        helper.succeedWhen(() -> {
            check(!chick.isHatchling(), "hasn't grown up");
            check(chick.getBbHeight() > 1.5F, "adult should be full size");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 1200, batch = "loyalCrowBringsShiniesToHerOwner")
    public static void loyalCrowBringsShiniesToHerOwner(GameTestHelper helper) {
        CrowHarpy crow = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(7, 2, 7));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        crow.imprintOn(player);
        helper.spawnItem(Items.GOLD_NUGGET, new BlockPos(12, 2, 12));
        boolean[] carried = {false};
        helper.onEachTick(() -> {
            carried[0] |= crow.getMainHandItem().is(Items.GOLD_NUGGET);
        });
        helper.succeedWhen(() -> {
            check(carried[0], "never picked the nugget up");
            check(!crow.isHoldingSomething(), "still carrying it");
            check(helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(3.0))
                    .stream().anyMatch(i -> i.getItem().is(Items.GOLD_NUGGET)), "nugget not delivered to the owner");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, batch = "cawsAndImitates")
    public static void cawsAndImitates(GameTestHelper helper) {
        helper.spawn(EntityType.COW, new BlockPos(10, 2, 10));
        CrowHarpy crow = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(4, 2, 4));
        // Either a caw or an imitation of the cow; both play the caw animation.
        crow.playAmbientSound();
        check(crow.getAction() == AbstractHarpy.Action.CAWING, "should animate a caw");
        helper.succeedWhen(() -> check(crow.getAction() == AbstractHarpy.Action.NONE, "caw animation should end on its own"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 15000, batch = "crowPicksOnOtherMobs")
    public static void picksOnOtherMobs(GameTestHelper helper) {
        CrowHarpy crow = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(4, 2, 4));
        var cow = helper.spawn(EntityType.COW, new BlockPos(10, 2, 10));
        cow.setNoAi(true);
        helper.succeedWhen(() -> check(cow.getLastHurtByMob() == crow, "hasn't picked on the cow yet"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 4000, batch = "loyalCrowPointsOutTreasure")
    public static void loyalCrowPointsOutTreasure(GameTestHelper helper) {
        BlockPos chestPos = new BlockPos(11, 1, 11); // set into the floor, lid flush with the grass
        helper.setBlock(chestPos, Blocks.CHEST);
        if (helper.getLevel().getBlockEntity(helper.absolutePos(chestPos)) instanceof ChestBlockEntity chest) {
            chest.setLootTable(BuiltInLootTables.SIMPLE_DUNGEON, 1L);
        }
        CrowHarpy crow = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(3, 2, 3));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 4));
        crow.imprintOn(player);
        boolean[] bounced = {false};
        helper.onEachTick(() -> {
            var rel = helper.relativeVec(crow.position());
            boolean onMarker = Math.abs(rel.x - 11.5) < 0.8 && Math.abs(rel.z - 11.5) < 0.8;
            bounced[0] |= onMarker && !crow.onGround();
        });
        helper.succeedWhen(() -> {
            check(bounced[0], "hasn't bounced on the spot above the chest yet");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, batch = "crowEatsAnythingAndLikesPats")
    public static void eatsAnythingAndLikesPats(GameTestHelper helper) {
        CrowHarpy crow = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(7, 2, 7));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COOKED_COD));
        crow.interact(player, InteractionHand.MAIN_HAND);
        check(player.getMainHandItem().isEmpty(), "crows should eat any food");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        crow.interact(player, InteractionHand.MAIN_HAND);
        check(crow.getAffection(player) == 2, "feeding + petting should give 2 affection, got " + crow.getAffection(player));
        helper.succeed();
    }
}
