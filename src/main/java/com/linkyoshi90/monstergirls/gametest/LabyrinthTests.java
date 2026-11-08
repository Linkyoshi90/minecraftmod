package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.world.LabyrinthStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayDeque;
import java.util.Deque;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;

/** The Minoan labyrinth (world/LabyrinthStructure). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class LabyrinthTests {
    /** Every cell can be reached from the entrance, and the heart is open. */
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "theMazeHasAWayThrough")
    public static void theMazeHasAWayThrough(GameTestHelper helper) {
        for (long seed = 0; seed < 20; seed++) {
            boolean[][] open = LabyrinthStructure.maze(RandomSource.create(seed));
            int n = open.length;
            boolean[][] seen = new boolean[n][n];
            Deque<int[]> q = new ArrayDeque<>();
            q.add(new int[]{1, 1});
            seen[1][1] = true;
            while (!q.isEmpty()) {
                int[] c = q.poll();
                for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                    int x = c[0] + d[0], z = c[1] + d[1];
                    if (x >= 0 && z >= 0 && x < n && z < n && open[x][z] && !seen[x][z]) {
                        seen[x][z] = true;
                        q.add(new int[]{x, z});
                    }
                }
            }
            for (int i = 0; i < LabyrinthStructure.CELLS; i++) {
                for (int j = 0; j < LabyrinthStructure.CELLS; j++) check(seen[1 + 2 * i][1 + 2 * j], "seed " + seed + ": every cell reachable");
            }
            check(!open[0][0] && !open[n - 1][n - 1] && open[n / 2][n / 2], "walled round, open at the heart");
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "theLabyrinthAndItsGuard")
    public static void theLabyrinthAndItsGuard(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(7, 16, 7));            // floating above the platform: the test world has no room below
        BlockPos chest = LabyrinthStructure.build(level, origin, origin.getY(), level.random, true);
        check(level.getBlockState(chest).is(Blocks.CHEST), "a chest at its heart");
        BlockPos shaft = origin.offset(-LabyrinthStructure.HALF + 1, 0, -LabyrinthStructure.HALF + 1);
        for (int y = chest.getY(); y <= origin.getY(); y++) check(level.getBlockState(new BlockPos(shaft.getX(), y, shaft.getZ())).is(Blocks.LADDER), "a ladder all the way down, at " + y);
        AbstractMonsterGirl guard = level.getEntitiesOfClass(AbstractMonsterGirl.class, new AABB(chest).inflate(3)).stream()
                .filter(g -> g.getSpecies() == GirlSpecies.MINOAN_MINOTAUR).findFirst().orElse(null);
        check(guard != null && guard.hasRestriction(), "and the Minoan minotaur who guards it");
        level.getEntitiesOfClass(Mob.class, new AABB(chest).inflate(10)).forEach(Mob::discard);
        for (BlockPos q : BlockPos.betweenClosed(origin.offset(-8, -LabyrinthStructure.DEPTH - 1, -8), origin.offset(8, 2, 8))) level.setBlock(q, Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.succeed();
    }
}
