package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.WeatherWays;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Weather ways (entity/WeatherWays). The weather itself is passed in, never set (it would leak into other tests). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class WeatherWayTests {
    private static final WeatherWays.Sky STORM = new WeatherWays.Sky(true, true, false, false, true);
    private static final WeatherWays.Sky RAIN = new WeatherWays.Sky(false, true, false, false, true);
    private static final WeatherWays.Sky SNOW = new WeatherWays.Sky(false, false, true, false, true);
    private static final WeatherWays.Sky CLEARING = new WeatherWays.Sky(false, false, false, true, true);

    private static AbstractMonsterGirl still(GameTestHelper helper, GirlSpecies s, BlockPos at) {
        AbstractMonsterGirl g = spawn(helper, s, at);
        g.setNoAi(true);
        return g;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "everyKindItsWeather")
    public static void everyKindItsWeather(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl sheep = still(helper, GirlSpecies.SHEEP_MINOTAUR, new BlockPos(2, 2, 2));
        AbstractMonsterGirl raiju = still(helper, GirlSpecies.RAIJU_CANID, new BlockPos(5, 2, 2));
        AbstractMonsterGirl slime = still(helper, GirlSpecies.VERDANT_SLIME, new BlockPos(8, 2, 2));
        AbstractMonsterGirl polar = still(helper, GirlSpecies.POLAR_BEAR_CANID, new BlockPos(11, 2, 2));
        AbstractMonsterGirl camel = still(helper, GirlSpecies.CAMEL_CENTAUR, new BlockPos(2, 2, 10));
        check(WeatherWays.shelters(sheep, STORM) && !WeatherWays.shelters(sheep, RAIN), "a sheep takes cover from a storm, not from rain");
        check(WeatherWays.way(raiju) == WeatherWays.Way.STORM_LOVER && !WeatherWays.shelters(raiju, STORM), "the raiju loves a storm");
        check("thrilled".equals(WeatherWays.feel(level, raiju, STORM)) && raiju.hasEffect(MobEffects.DAMAGE_BOOST), "thrilled by it");
        check(WeatherWays.way(slime) == WeatherWays.Way.RAIN_LOVER && "glistening".equals(WeatherWays.feel(level, slime, RAIN)), "a slime livens up in the rain");
        check(WeatherWays.way(polar) == WeatherWays.Way.SNOW_LOVER && "playing".equals(WeatherWays.feel(level, polar, SNOW)), "a polar bear plays in the snow");
        check(WeatherWays.way(camel) == WeatherWays.Way.WARM_LOVER && WeatherWays.shelters(camel, SNOW), "a camel shivers and takes cover from snow");
        check(!WeatherWays.shelters(sheep, new WeatherWays.Sky(true, true, false, false, false)), "already under a roof: no need");
        helper.succeed();
    }
}
