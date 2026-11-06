package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Company;
import com.linkyoshi90.monstergirls.entity.HomeLife;
import com.linkyoshi90.monstergirls.entity.NightWatch;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The night watch and remembrance (entity.NightWatch), and a warm friend (entity.Company). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class WatchTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aPoppyInHerMemory")
    public static void aPoppyInHerMemory(GameTestHelper helper) {
        var level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.INABA_JACKALOPE, new BlockPos(5, 2, 5));
        girl.setNoAi(true);
        check(NightWatch.remember(level, girl) == 0, "a stranger: no one to remember her");
        girl.addAffection(p, HomeLife.FRIEND);
        check(NightWatch.remember(level, girl) == 1, "remembered");
        check(NightWatch.remembered(p).size() == 1, "in the journal");
        check(!level.getEntitiesOfClass(ItemEntity.class, girl.getBoundingBox().inflate(2), e -> e.getItem().is(Items.POPPY)).isEmpty(), "a poppy where she fell");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aWarmFriend")
    public static void aWarmFriend(GameTestHelper helper) {
        var level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        AbstractMonsterGirl rabbit = spawn(helper, GirlSpecies.INABA_JACKALOPE, new BlockPos(3, 2, 2));
        rabbit.setNoAi(true);
        rabbit.addAffection(p, HomeLife.FRIEND);
        p.setTicksFrozen(100);
        check(Company.warm(level, p) == null && p.getTicksFrozen() == 100, "a moon rabbit is no hearth");
        AbstractMonsterGirl lava = spawn(helper, GirlSpecies.VOLCANO_JACKALOPE, new BlockPos(2, 2, 3));
        lava.setNoAi(true);
        lava.addAffection(p, HomeLife.FRIEND);
        check(Company.warm(level, p) == lava && p.getTicksFrozen() == 0, "a volcano rabbit is");
        lava.discard();
        AbstractMonsterGirl bear = spawn(helper, GirlSpecies.POLAR_BEAR_CANID, new BlockPos(3, 2, 3));
        bear.setNoAi(true);
        bear.addAffection(p, HomeLife.FRIEND);
        p.setTicksFrozen(100);
        check(Company.warm(level, p) == bear && p.getTicksFrozen() == 0, "and so is a polar bear's fur");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "sureFootedBesideAHoofedFriend")
    public static void sureFootedBesideAHoofedFriend(GameTestHelper helper) {
        var level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        var step = p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT);
        double base = step.getValue();
        AbstractMonsterGirl rabbit = spawn(helper, GirlSpecies.INABA_JACKALOPE, new BlockPos(4, 2, 2));
        rabbit.setNoAi(true);
        rabbit.addAffection(p, HomeLife.FRIEND);
        check(Company.sureFooted(level, p) == null && step.getValue() == base, "a rabbit has no hooves");
        AbstractMonsterGirl deer = spawn(helper, GirlSpecies.REINDEER_CENTAUR, new BlockPos(5, 2, 5));
        deer.setNoAi(true);
        check(Company.sureFooted(level, p) == null, "not with a stranger");
        deer.addAffection(p, HomeLife.FRIEND);
        check(Company.sureFooted(level, p) == deer && step.getValue() >= 1.0, "a whole block's step beside her, step=" + step.getValue());
        check(Company.sureFooted(level, p) == deer && step.getValue() == base + Company.STEP, "the step is lent once, not stacked");
        deer.discard();
        check(Company.sureFooted(level, p) == null && step.getValue() == base, "and it goes with her");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "slimesCushionAFallConstructsSteadyAnvils")
    public static void slimesCushionAFallConstructsSteadyAnvils(GameTestHelper helper) {
        var level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        AbstractMonsterGirl slime = spawn(helper, GirlSpecies.VERDANT_SLIME, new BlockPos(4, 2, 2));
        slime.setNoAi(true);
        AbstractMonsterGirl golem = spawn(helper, GirlSpecies.TIN_SQUIRREL_CONSTRUCT, new BlockPos(2, 2, 5));
        golem.setNoAi(true);
        check(Company.cushion(level, p, 10.0F) == null, "a stranger lets you fall");
        check(Company.steadyAnvil(level, p) == null, "a stranger leaves the anvil be");
        slime.addAffection(p, HomeLife.FRIEND);
        golem.addAffection(p, HomeLife.FRIEND);
        check(Company.cushion(level, p, 2.0F) == null, "a hop needs no cushion");
        check(Company.cushion(level, p, 10.0F) == slime, "a real fall: she breaks it");
        check(Company.steadyAnvil(level, p) == golem, "a construct friend keeps the anvil steady");
        check(Company.steadyAnvil(level, p) != slime, "a slime is no smith");
        slime.discard();
        golem.discard();
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aGuardCallsOut")
    public static void aGuardCallsOut(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos post = helper.absolutePos(new BlockPos(5, 2, 5));
        check(NightWatch.intruder(level, post) == null, "all quiet");
        helper.spawn(EntityType.ZOMBIE, new BlockPos(7, 2, 7)).setNoAi(true);
        check(NightWatch.intruder(level, post) != null, "a zombie near her post");
        helper.succeed();
    }
}
