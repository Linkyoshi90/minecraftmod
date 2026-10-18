package com.linkyoshi90.harpies.gametest;

import com.linkyoshi90.harpies.HarpiesMod;
import com.linkyoshi90.harpies.entity.AlbatrossHarpy;
import com.linkyoshi90.harpies.entity.ai.SkittishGoal;
import com.linkyoshi90.harpies.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harpies.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harpies.gametest.HarpyTestUtil.check;

/** Run with: ./gradlew runGameTestServer. Floor is at relative y=1. */
@GameTestHolder(HarpiesMod.MODID)
@PrefixGameTestTemplate(false)
public class AlbatrossHarpyTests {

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "albatrossVanishesWhenApproached")
    public static void vanishesWhenApproached(GameTestHelper helper) {
        // Spawned like a natural spawn: helper.spawn would mark her persistent, and persistent mobs never vanish.
        AlbatrossHarpy albatross = ModEntities.ALBATROSS_HARPY.get().spawn(helper.getLevel(), helper.absolutePos(new BlockPos(7, 2, 7)),
                MobSpawnType.NATURAL);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 12));
        helper.succeedWhen(() -> {
            check(albatross.isRemoved(), "a wild albatross should vanish when approached");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "albatrossLetsTrustedPeopleClose")
    public static void letsTrustedPeopleClose(GameTestHelper helper) {
        AlbatrossHarpy albatross = helper.spawn(ModEntities.ALBATROSS_HARPY.get(), new BlockPos(7, 2, 7));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 10));
        albatross.setAffection(player, AlbatrossHarpy.TRUSTING);
        helper.onEachTick(() -> check(albatross.goalSelector.getAvailableGoals().stream()
                .noneMatch(g -> g.isRunning() && g.getGoal() instanceof SkittishGoal), "fled from someone she trusts"));
        helper.runAtTickTime(300, () -> {
            check(!albatross.isRemoved(), "vanished despite trusting the player");
            HarpyTestUtil.remove(player);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 1500, batch = "albatrossTakesFoodLeftOutForHer")
    public static void takesFoodLeftOutForHer(GameTestHelper helper) {
        AlbatrossHarpy albatross = helper.spawn(ModEntities.ALBATROSS_HARPY.get(), new BlockPos(11, 2, 7));
        // Keeping a respectful distance, sneaking, as the field guide advises.
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        player.setShiftKeyDown(true);
        helper.onEachTick(() -> player.setShiftKeyDown(true));
        ItemEntity fish = helper.spawnItem(Items.COD, new BlockPos(11, 2, 11));
        fish.setThrower(player);
        helper.succeedWhen(() -> {
            check(albatross.getAffection(player) >= 2, "hasn't taken the fish yet");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, batch = "albatrossNeedsAClearingToLand")
    public static void needsAClearingToLand(GameTestHelper helper) {
        BlockPos open = helper.absolutePos(new BlockPos(4, 2, 4));
        BlockPos cramped = helper.absolutePos(new BlockPos(10, 2, 10));
        helper.setBlock(new BlockPos(11, 2, 11), Blocks.OAK_LOG);
        check(AlbatrossHarpy.isClearing(helper.getLevel(), open), "flat open ground should be a clearing");
        check(!AlbatrossHarpy.isClearing(helper.getLevel(), cramped), "a tree trunk next to the spot should spoil it");
        helper.succeed();
    }
}
