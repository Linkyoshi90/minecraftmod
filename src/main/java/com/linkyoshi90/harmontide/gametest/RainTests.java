package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.RainMoods;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Rain moods (entity.girl.RainMoods): cats look for a roof, amphibians splash about with a friend. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class RainTests {

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "catsHateTheRainFrogsLoveIt")
    public static void catsHateTheRainFrogsLoveIt(GameTestHelper helper) {
        var level = helper.getLevel();                                  // (rain never falls on the test platforms: splash is told it does)
        for (int x = 4; x <= 8; x++) for (int z = 4; z <= 8; z++) helper.setBlock(new BlockPos(x, 4, z), Blocks.STONE);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        AbstractMonsterGirl cat = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(3, 2, 3));
        AbstractMonsterGirl newt = spawn(helper, GirlSpecies.SALAMANDER_LIZARDFOLK, new BlockPos(2, 2, 1));
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(4, 2, 1));
        newt.addAffection(p, Orders.AFFECTION);
        check(RainMoods.hatesRain(cat) && !RainMoods.hatesRain(wolf) && RainMoods.lovesRain(newt), "cats hate it, salamanders love it, wolves don't mind");
        check(RainMoods.tick(level, newt) == null, "no rain, no splashing");
        int before = newt.getAffection(p);
        check(RainMoods.splash(level, newt, true) == p && newt.getAffection(p) == before + 2, "the salamander splashes about with her friend");
        check(RainMoods.splash(level, newt, true) == null && RainMoods.splash(level, wolf, true) == null, "once a shower, and only for rain-lovers");
        helper.runAfterDelay(20, () -> {                                // the light under the new roof settles first (canSeeSky reads it)
            BlockPos roof = null;
            for (int i = 0; i < 400 && roof == null; i++) roof = RainMoods.shelter(level, cat);
            check(roof != null && !level.canSeeSky(roof), "a cat finds the roof");
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "reptilesBaskInTheMorningSun")
    public static void reptilesBaskInTheMorningSun(GameTestHelper helper) {
        var level = helper.getLevel();
        level.setWeatherParameters(6000, 0, false, false);
        helper.setDayTime(1000);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        AbstractMonsterGirl iguana = spawn(helper, GirlSpecies.IGUANA_LIZARDFOLK, new BlockPos(3, 2, 3));
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(6, 2, 6));
        iguana.addAffection(p, Orders.AFFECTION);
        int before = iguana.getAffection(p);
        helper.runAfterDelay(2, () -> {                                 // isDay follows the new time a tick later
        check(com.linkyoshi90.harmontide.entity.girl.Basking.coldBlooded(iguana) && !com.linkyoshi90.harmontide.entity.girl.Basking.coldBlooded(wolf), "the iguana is cold-blooded, the wolf isn't");
        check(com.linkyoshi90.harmontide.entity.girl.Basking.sunny(level, iguana), "a clear morning under open sky");
        check(com.linkyoshi90.harmontide.entity.girl.Basking.bask(level, iguana), "she lies in the sun");
        check(iguana.getAction() == AbstractMonsterGirl.Action.BASK && iguana.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED), "and warms up");
        check(iguana.getAffection(p) > before, "her friend kept her company");
        check(!com.linkyoshi90.harmontide.entity.girl.Basking.tick(level, iguana), "once a day");
        HarpyTestUtil.remove(p);
        helper.succeed();
        });
    }
}
