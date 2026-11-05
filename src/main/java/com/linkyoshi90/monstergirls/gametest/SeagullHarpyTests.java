package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.SeagullHarpy;
import com.linkyoshi90.monstergirls.entity.ai.FollowBoatGoal;
import com.linkyoshi90.monstergirls.entity.ai.SirenSongGoal;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;

/** Run with: ./gradlew runGameTestServer. Floor is at relative y=1. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class SeagullHarpyTests {
    private static final BlockPos ROCK = new BlockPos(7, 3, 7);

    /** Floods the platform one block deep, optionally with a two-block stone rock standing in the middle. */
    static void buildSea(GameTestHelper helper, boolean rock) {
        for (int x = 0; x < 15; x++) {
            for (int z = 0; z < 15; z++) helper.setBlock(new BlockPos(x, 2, z), Blocks.WATER);
        }
        if (rock) {
            helper.setBlock(ROCK.below(), Blocks.STONE);
            helper.setBlock(ROCK, Blocks.STONE);
        }
    }

    /** A boat floating on the water at this column (spawned on the surface, not inside the water block). */
    static Boat mannedBoat(GameTestHelper helper, BlockPos pos, ServerPlayer sailor) {
        Boat boat = helper.spawn(EntityType.BOAT, pos.above());
        sailor.startRiding(boat, true);
        return boat;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 8000, batch = "seagullBegsFromPeopleOnLand")
    public static void begsFromPeopleOnLand(GameTestHelper helper) {
        SeagullHarpy gull = helper.spawn(ModEntities.SEAGULL_HARPY.get(), new BlockPos(3, 2, 3));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(10, 2, 10));
        helper.succeedWhen(() -> {
            check(gull.getAction() == AbstractHarpy.Action.BEGGING, "not begging yet");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "seagullFollowsBoats")
    public static void followsBoats(GameTestHelper helper) {
        buildSea(helper, false);
        SeagullHarpy gull = helper.spawn(ModEntities.SEAGULL_HARPY.get(), new BlockPos(2, 3, 12));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(12, 3, 2));
        Boat boat = mannedBoat(helper, new BlockPos(12, 2, 2), player);
        helper.succeedWhen(() -> {
            boolean following = gull.goalSelector.getAvailableGoals().stream()
                    .anyMatch(g -> g.isRunning() && g.getGoal() instanceof FollowBoatGoal f && f.getBoat() == boat);
            check(following && gull.distanceToSqr(boat) < 6.0 * 6.0, "not wheeling over the boat");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 5000, batch = "seagullSirenSongLuresBoats")
    public static void sirenSongLuresBoats(GameTestHelper helper) {
        buildSea(helper, true);
        SeagullHarpy gull = helper.spawn(ModEntities.SEAGULL_HARPY.get(), ROCK.above());
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 3, 2));
        Boat boat = mannedBoat(helper, new BlockPos(2, 2, 2), player);
        helper.succeedWhen(() -> {
            boolean pulled = gull.goalSelector.getAvailableGoals().stream()
                    .anyMatch(g -> g.getGoal() instanceof SirenSongGoal song && song.getPulls() > 0);
            check(pulled && boat.isAlive(), "the song hasn't drawn the boat toward the rock");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 5000, batch = "seagullWrecksBoatsAtTheRock")
    public static void wrecksBoatsAtTheRock(GameTestHelper helper) {
        buildSea(helper, true);
        SeagullHarpy gull = helper.spawn(ModEntities.SEAGULL_HARPY.get(), ROCK.above());
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 3, 9));
        Boat boat = mannedBoat(helper, new BlockPos(7, 2, 8), player);
        helper.succeedWhen(() -> {
            check(boat.isRemoved(), "the boat that reached the rock hasn't been wrecked");
            check(player.getVehicle() == null, "the crew should end up in the water");
            HarpyTestUtil.remove(player);
        });
    }
}
