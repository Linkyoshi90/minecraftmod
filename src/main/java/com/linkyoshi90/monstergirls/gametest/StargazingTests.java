package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.HomeLife;
import com.linkyoshi90.monstergirls.entity.Stargazing;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Treats;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Stargazing (entity.Stargazing): friends sit with a still player on a clear night, and a shooting star tells a wish. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class StargazingTests {
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aShootingStarTellsAWish")
    public static void aShootingStarTellsAWish(GameTestHelper helper) {
        var level = helper.getLevel();
        level.setWeatherParameters(6000, 0, false, false);
        helper.setDayTime(18000);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 4));
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.INABA_JACKALOPE, new BlockPos(5, 2, 4));
        girl.setNoAi(true);
        girl.addAffection(p, HomeLife.FRIEND);
        helper.runAfterDelay(2, () -> {
            check(Stargazing.clearNight(level), "a clear night");
            Stargazing.step(level, p);
            int still = Stargazing.step(level, p);
            if (level.canSeeSky(p.blockPosition().above())) check(still == 1, "standing still: " + still);
            p.teleportTo(p.getX() + 1, p.getY(), p.getZ());
            check(Stargazing.step(level, p) == 0, "moving starts it over");
            check(!Treats.knows(p, girl), "her favourite is a secret");
            int before = girl.getAffection(p);
            check(Stargazing.shootingStar(level, p) == 1, "she sees it with them");
            check(girl.getAffection(p) - before == Stargazing.WISH, "closer for it");
            check(Treats.knows(p, girl), "and she whispers her wish");
            check(Stargazing.seen(p).size() == 1, "a constellation on the chart");
            for (int i = 1; i < Stargazing.CONSTELLATIONS.size(); i++) Stargazing.chart(p);
            check(Stargazing.chart(p) == null && Stargazing.seen(p).size() == Stargazing.CONSTELLATIONS.size(), "all twelve");
            check(Stargazing.seenTonight(level, p) && Stargazing.step(level, p) == 0, "one star a night");
            helper.setDayTime(1000);   // back to morning: later tests share this world's clock
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }
}
