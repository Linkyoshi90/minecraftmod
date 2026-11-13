package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Fidgets;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Idle fidgets (entity/girl/Fidgets, tools/gen_fidgets.py). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class FidgetTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "everyKindHerOwnFidget")
    public static void everyKindHerOwnFidget(GameTestHelper helper) {
        for (GirlSpecies s : GirlSpecies.values()) {
            var f = Fidgets.of(s);
            check(f.containsKey("signature"), s.id() + " has a fidget of her own");
            check(f.size() >= 3, s.id() + " has a few to choose from: " + f.keySet());
        }
        check(Fidgets.of(GirlSpecies.SERVAL_NEKOMATA).containsKey("knead") && Fidgets.of(GirlSpecies.HORSE_CENTAUR).containsKey("stamp"), "and her family's");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "sheFidgetsWhenIdle")
    public static void sheFidgetsWhenIdle(GameTestHelper helper) {
        AbstractMonsterGirl horse = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(5, 2, 5));
        horse.setNoAi(true);
        helper.runAfterDelay(5, () -> {
            String n = Fidgets.fidget(horse, horse.getRandom());
            check(n != null && horse.getAction() == AbstractMonsterGirl.Action.FIDGET && horse.getFidget().equals(n), "idle, she fidgets: " + n);
            check(Fidgets.fidget(horse, horse.getRandom()) == null, "one at a time");
            AbstractMonsterGirl cat = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(10, 2, 10));
            check(Fidgets.PLAYFUL.contains(cat.getSpecies().family()) && !Fidgets.PLAYFUL.contains(horse.getSpecies().family()), "cats get the zoomies, centaurs don't");
            helper.succeed();
        });
    }
}
