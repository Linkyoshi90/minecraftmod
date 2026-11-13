package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Daily rhythms (AbstractMonsterGirl#rhythm, #restingHours). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class RhythmTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "everyKindItsHours")
    public static void everyKindItsHours(GameTestHelper helper) {
        var level = helper.getLevel();
        AbstractMonsterGirl horse = spawn(helper, GirlSpecies.SHEEP_MINOTAUR, new BlockPos(3, 2, 3));
        AbstractMonsterGirl fox = spawn(helper, GirlSpecies.RED_KITSUNE, new BlockPos(6, 2, 3));
        AbstractMonsterGirl lioness = spawn(helper, GirlSpecies.LIONESS_NEKOMATA, new BlockPos(9, 2, 3));
        check(horse.rhythm() == AbstractMonsterGirl.Rhythm.DIURNAL && fox.rhythm() == AbstractMonsterGirl.Rhythm.NOCTURNAL, "a sheep by day, a fox by night: " + horse.rhythm());
        check(lioness.rhythm() == AbstractMonsterGirl.Rhythm.CREPUSCULAR, "lionesses hunt at dusk: " + lioness.rhythm());
        long saved = level.getDayTime();
        long base = saved / 24000L * 24000L;
        level.setDayTime(base + 6000);                                       // noon
        check(!horse.restingHours() && fox.restingHours() && lioness.restingHours(), "noon: the horse up, the fox and the lioness resting");
        level.setDayTime(base + 12500);                                      // dusk
        check(!lioness.restingHours(), "dusk: the lioness is up");
        level.setDayTime(base + 18000);                                      // midnight
        check(horse.restingHours() && !fox.restingHours() && lioness.restingHours(), "midnight: the horse asleep, the fox up, the lioness resting");
        level.setDayTime(saved);                                             // put back within the tick
        helper.succeed();
    }
}
