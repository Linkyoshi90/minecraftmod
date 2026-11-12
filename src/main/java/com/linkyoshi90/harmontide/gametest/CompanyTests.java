package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Company;
import com.linkyoshi90.harmontide.entity.HomeLife;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Company (entity.Company): good morning, and huddling in a thunderstorm. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class CompanyTests {
    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "goodMorningAndThunder")
    public static void goodMorningAndThunder(GameTestHelper helper) {
        var level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 4));
        AbstractMonsterGirl rabbit = spawn(helper, GirlSpecies.INABA_JACKALOPE, new BlockPos(5, 2, 4));
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLVERINE_CANID, new BlockPos(4, 2, 5));
        rabbit.setNoAi(true);
        wolf.setNoAi(true);
        check(Company.goodMorning(level, p) == 0 && !p.hasEffect(MobEffects.ABSORPTION), "nobody to say good morning");
        rabbit.addAffection(p, HomeLife.FRIEND);
        wolf.addAffection(p, HomeLife.FRIEND);
        check(Company.goodMorning(level, p) == 2 && p.hasEffect(MobEffects.ABSORPTION), "well rested");
        check(Company.timid(rabbit) && !Company.timid(wolf), "a rabbit jumps at thunder, a wolverine doesn't");
        check(Company.thunder(level, p) == 0, "no storm");
        int before = rabbit.getAffection(p);
        level.setWeatherParameters(0, 600, true, true);
        helper.runAfterDelay(110, () -> {                               // the storm takes a few seconds to build
            Company.thunder(level, p);                                  // (unless their own tick got there first)
            check(rabbit.getAffection(p) - before == Company.HUDDLE, "she huddles, and is closer for it");
            check(Company.thunder(level, p) == 0, "once a storm");
            level.setWeatherParameters(6000, 0, false, false);
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }
}
