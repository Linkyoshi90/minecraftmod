package com.linkyoshi90.harpies.gametest;

import com.linkyoshi90.harpies.HarpiesMod;
import com.linkyoshi90.harpies.block.RiflebirdNestBlock;
import com.linkyoshi90.harpies.entity.CrowCaches;
import com.linkyoshi90.harpies.entity.CrowHarpy;
import com.linkyoshi90.harpies.entity.RiflebirdHarpy;
import com.linkyoshi90.harpies.registry.ModBlocks;
import com.linkyoshi90.harpies.registry.ModEntities;
import com.linkyoshi90.harpies.registry.ModItems;
import com.linkyoshi90.harpies.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.harpies.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harpies.gametest.HarpyTestUtil.check;

/** Crow caches and riflebird nests. */
@GameTestHolder(HarpiesMod.MODID)
@PrefixGameTestTemplate(false)
public class CacheAndNestTests {

    private static boolean itemAround(GameTestHelper helper, BlockPos rel, double radius, net.minecraft.world.item.Item item) {
        AABB around = new AABB(helper.absolutePos(rel)).inflate(radius);
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, around).stream().anyMatch(i -> i.getItem().is(item));
    }

    // --- crow caches ------------------------------------------------------------------------------------------

    @GameTest(template = PLATFORM, timeoutTicks = 500, batch = "crowBuriesATrinket")
    public static void crowBuriesATrinket(GameTestHelper helper) {
        CrowHarpy crow = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(7, 2, 7));
        crow.takeIntoHands(new ItemStack(Items.GOLD_NUGGET));
        crow.startCaching();
        helper.succeedWhen(() -> {
            check(!crow.isHoldingSomething(), "still holding the nugget");
            List<CrowCaches.Cache> caches = CrowCaches.get(helper.getLevel()).of(crow.getUUID());
            check(caches.size() == 1, "expected one cache, got " + caches.size());
            CrowCaches.Cache cache = caches.get(0);
            check(cache.item().is(Items.GOLD_NUGGET), "buried the wrong thing");
            check(helper.getLevel().getBlockState(cache.pos()).is(ModTags.CROW_HARPY_CACHE_GROUND), "buried it in something that isn't soft ground");
            check(!cache.watched(), "nobody was around to see");
            check(!itemAround(helper, new BlockPos(7, 2, 7), 16, Items.GOLD_NUGGET), "the nugget is lying around instead of buried");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "diggingUpACacheFindsTheItem")
    public static void diggingUpACacheFindsTheItem(GameTestHelper helper) {
        CrowHarpy crow = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(7, 2, 7));
        crow.setNoAi(true);
        BlockPos ground = helper.absolutePos(new BlockPos(4, 1, 4));
        CrowCaches.get(helper.getLevel()).add(new CrowCaches.Cache(ground, new ItemStack(Items.EMERALD), crow.getUUID(), Long.MAX_VALUE, false));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 6));
        helper.runAtTickTime(5, () -> {
            player.gameMode.destroyBlock(ground);
            check(CrowCaches.get(helper.getLevel()).at(ground) == null, "the cache should be gone once dug up");
            check(itemAround(helper, new BlockPos(4, 1, 4), 2, Items.EMERALD), "digging should turn up the emerald");
            check(crow.holdsGrudgeAgainst(player), "she watched you rob her cache");
            HarpyTestUtil.remove(player);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 1400, batch = "crowMovesAWatchedCache")
    public static void crowMovesAWatchedCache(GameTestHelper helper) {
        CrowHarpy crow = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(7, 2, 7));
        BlockPos old = helper.absolutePos(new BlockPos(3, 1, 3));
        // Someone saw her bury it; they've gone now (no players in this test), so she comes back to move it.
        CrowCaches.get(helper.getLevel()).add(new CrowCaches.Cache(old, new ItemStack(Items.GOLD_INGOT), crow.getUUID(), Long.MAX_VALUE, true));
        helper.succeedWhen(() -> {
            List<CrowCaches.Cache> caches = CrowCaches.get(helper.getLevel()).of(crow.getUUID());
            check(caches.size() == 1 && !caches.get(0).pos().equals(old), "hasn't moved the cache yet");
            check(caches.get(0).item().is(Items.GOLD_INGOT), "moved the wrong thing");
            check(caches.get(0).pos().distSqr(old) >= 6 * 6, "the new hiding place is right next to the old one");
            check(!crow.isHoldingSomething(), "still carrying it");
        });
    }

    // --- riflebird nests --------------------------------------------------------------------------------------

    @GameTest(template = PLATFORM, timeoutTicks = 500, batch = "riflebirdLaysInANest")
    public static void riflebirdLaysInANest(GameTestHelper helper) {
        BlockPos nestRel = new BlockPos(7, 2, 12);
        helper.setBlock(nestRel, ModBlocks.RIFLEBIRD_NEST.get());
        RiflebirdHarpy bird = helper.spawn(ModEntities.RIFLEBIRD_HARPY.get(), new BlockPos(7, 2, 4));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 4));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.EMERALD));
        bird.interact(player, InteractionHand.MAIN_HAND);
        helper.succeedWhen(() -> {
            check(helper.getBlockState(nestRel).getValue(RiflebirdNestBlock.EGGS) == 1, "no egg in the nest yet");
            check(!itemAround(helper, nestRel, 16, ModItems.RIFLEBIRD_EGG.get()), "she laid on the ground despite the nest");
            check(helper.absolutePos(nestRel).equals(bird.getHomePos()), "the nest should be her home now");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "riflebirdAdoptsANest")
    public static void riflebirdAdoptsANest(GameTestHelper helper) {
        BlockPos nestRel = new BlockPos(3, 2, 3);
        helper.setBlock(nestRel, ModBlocks.RIFLEBIRD_NEST.get());
        RiflebirdHarpy bird = helper.spawn(ModEntities.RIFLEBIRD_HARPY.get(), new BlockPos(10, 2, 10));
        helper.succeedWhen(() -> check(helper.absolutePos(nestRel).equals(bird.getHomePos()), "hasn't adopted the nest"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "takingAnEggFromTheNest")
    public static void takingAnEggFromTheNest(GameTestHelper helper) {
        BlockPos nestRel = new BlockPos(7, 2, 7);
        helper.setBlock(nestRel, ModBlocks.RIFLEBIRD_NEST.get().defaultBlockState().setValue(RiflebirdNestBlock.EGGS, 2));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 9));
        helper.runAtTickTime(5, () -> {
            BlockPos abs = helper.absolutePos(nestRel);
            helper.getBlockState(nestRel).useWithoutItem(helper.getLevel(), player,
                    new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));
            check(helper.getBlockState(nestRel).getValue(RiflebirdNestBlock.EGGS) == 1, "taking one should leave one");
            check(player.getInventory().countItem(ModItems.RIFLEBIRD_EGG.get()) == 1, "the egg should be in your inventory");
            HarpyTestUtil.remove(player);
            helper.succeed();
        });
    }
}
