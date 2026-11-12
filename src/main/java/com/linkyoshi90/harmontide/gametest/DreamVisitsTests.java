package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.DreamVisits;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Dream visits (entity/DreamVisits): a succubus friend's sweet dream, a wild one's feeding, and the tapir's ward. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class DreamVisitsTests {
    private static AbstractMonsterGirl still(GameTestHelper helper, GirlSpecies s, BlockPos at) {
        AbstractMonsterGirl g = spawn(helper, s, at);
        g.setNoAi(true);
        return g;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "succubusFriendSweetDream")
    public static void succubusFriendSweetDream(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        check(DreamVisits.dream(helper.getLevel(), p) == DreamVisits.Dream.NONE, "nobody to dream of");
        AbstractMonsterGirl cat = still(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(10, 2, 3));
        cat.addAffection(p, 60);
        check(DreamVisits.dream(helper.getLevel(), p) == DreamVisits.Dream.NONE, "only succubus kinds walk in dreams");
        AbstractMonsterGirl succubus = still(helper, GirlSpecies.SUCCUBUS_DEVIL, new BlockPos(6, 2, 6));
        succubus.addAffection(p, 60);
        p.giveExperiencePoints(100);
        check(DreamVisits.dream(helper.getLevel(), p) == DreamVisits.Dream.SWEET, "a sweet dream");
        check(p.hasEffect(MobEffects.LUCK) && succubus.getAffection(p) == 61 && p.totalExperience == 100, "a lucky morning, nothing taken");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "wildSuccubusFeeds")
    public static void wildSuccubusFeeds(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        AbstractMonsterGirl wild = still(helper, GirlSpecies.SUCCUBUS_DEVIL, new BlockPos(10, 2, 10));
        p.giveExperiencePoints(100);
        check(DreamVisits.dream(helper.getLevel(), p) == DreamVisits.Dream.FED, "she fed");
        check(p.totalExperience == 75 && wild.getAffection(p) == DreamVisits.FED, "a quarter taken, and she likes them a little");
        check(!helper.getLevel().getEntitiesOfClass(ItemEntity.class, p.getBoundingBox().inflate(2), i -> i.getItem().is(ModItems.HEARTFRUIT.get())).isEmpty(), "a heartfruit left by the bed");
        p.giveExperiencePoints(-p.totalExperience);
        check(DreamVisits.dream(helper.getLevel(), p) == DreamVisits.Dream.FED && p.totalExperience == 0, "nothing to take from the empty");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "tapirWardsDreams")
    public static void tapirWardsDreams(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        still(helper, GirlSpecies.SUCCUBUS_DEVIL, new BlockPos(10, 2, 10));
        still(helper, GirlSpecies.TAPIR_CENTAUR, new BlockPos(3, 2, 10));
        p.giveExperiencePoints(100);
        check(DreamVisits.dream(helper.getLevel(), p) == DreamVisits.Dream.WARDED && p.totalExperience == 100, "the tapir keeps her out");
        check(DreamVisits.taken(p) == 25, "a quarter");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
