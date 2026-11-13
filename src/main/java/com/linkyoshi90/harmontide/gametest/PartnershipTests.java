package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Herds;
import com.linkyoshi90.harmontide.entity.Partnerships;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Partnerships between kinds (entity/Partnerships). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class PartnershipTests {
    private static AbstractMonsterGirl still(GameTestHelper helper, GirlSpecies s, BlockPos at) {
        AbstractMonsterGirl g = spawn(helper, s, at);
        g.setNoAi(true);
        return g;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "partnersHelpEachOther")
    public static void partnersHelpEachOther(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl elephant = still(helper, GirlSpecies.ELEPHANT_CENTAUR, new BlockPos(3, 2, 3));
        AbstractMonsterGirl mouse = still(helper, GirlSpecies.HOUSE_MOUSE_RODENT, new BlockPos(5, 2, 3));
        check(Partnerships.offers(mouse) == Partnerships.Pair.TICK_PICKER, "a little rodent picks a big grazer clean");
        check(Partnerships.pulse(level, mouse) == 1 && elephant.hasEffect(MobEffects.REGENERATION) && mouse.hasEffect(MobEffects.DAMAGE_RESISTANCE), "both do better for it");
        AbstractMonsterGirl crab = still(helper, GirlSpecies.HERMIT_CRAB_CRUSTACEAN, new BlockPos(10, 2, 3));
        still(helper, GirlSpecies.ANEMONE_MOLLUSK, new BlockPos(11, 2, 4));
        check(Partnerships.pulse(level, crab) == 1 && crab.hasEffect(MobEffects.DAMAGE_RESISTANCE), "the hermit crab shelters by the anemone");
        AbstractMonsterGirl ant = still(helper, GirlSpecies.ANT_BUG, new BlockPos(3, 2, 10));
        still(helper, GirlSpecies.ALRAUNE_FLORA, new BlockPos(5, 2, 11));
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(7, 2, 12));
        z.setNoAi(true);
        check(Partnerships.pulse(level, ant) == 1 && ant.getTarget() == z, "the ant goes for the monster by her plant");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "scavengersAndMixedHerds")
    public static void scavengersAndMixedHerds(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl hyena = still(helper, GirlSpecies.HYENA_CANID, new BlockPos(3, 2, 3));
        AbstractMonsterGirl wolf = still(helper, GirlSpecies.WOLF_CANID, new BlockPos(5, 2, 3));
        check(Partnerships.trails(hyena) == wolf, "a hyena trails a wild hunter");
        check(Partnerships.trailSpot(hyena, wolf) != null, "keeping her distance: too close now");
        AbstractMonsterGirl sheep = still(helper, GirlSpecies.SHEEP_MINOTAUR, new BlockPos(10, 2, 10));
        AbstractMonsterGirl elephant = still(helper, GirlSpecies.ELEPHANT_CENTAUR, new BlockPos(12, 2, 12));
        Herds.alarm(level, sheep, sheep.position().add(5, 0, 0));
        check(Herds.alarmed(elephant), "another kind heeds the sheep's alarm");
        helper.succeed();
    }
}
