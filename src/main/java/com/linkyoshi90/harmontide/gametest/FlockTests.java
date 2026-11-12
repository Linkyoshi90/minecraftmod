package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Bonded;
import com.linkyoshi90.harmontide.entity.Flocks;
import com.linkyoshi90.harmontide.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;

/** Passing flocks (entity/Flocks): when they come, and that they fly on unless one has made a friend. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class FlockTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "flocksComeEveryEightDays")
    public static void flocksComeEveryEightDays(GameTestHelper helper) {
        int days = 0;
        for (long d = 0; d < Flocks.EVERY * 4; d++) if (Flocks.flockDay(d * 24000L + 500L)) days++;
        check(days == 4, "one flock day in eight: " + days);
        check(Flocks.northward(Flocks.ON * 24000L) != Flocks.northward((Flocks.ON + Flocks.EVERY) * 24000L), "north and south by turns");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aFlockRestsAndFliesOn")
    public static void aFlockRestsAndFliesOn(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos centre = helper.absolutePos(new BlockPos(7, 2, 7));
        List<Mob> flock = Flocks.land(level, centre, ModEntities.SWAN_HARPY.get(), 4, 1, 2);
        check(flock.size() >= 2 && flock.stream().allMatch(Flocks::resting), "a flock comes down to rest: " + flock.size());
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 7));
        Mob friend = flock.get(0);
        ((Bonded) friend).addAffection(p, 1);
        flock.forEach(Flocks::dueNow);
        check(!Flocks.flyOn(level, friend) && friend.isAlive() && !Flocks.resting(friend), "the one who made a friend stays");
        for (int i = 1; i < flock.size(); i++) check(Flocks.flyOn(level, flock.get(i)) && flock.get(i).isRemoved(), "the rest fly on");
        friend.discard();
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
