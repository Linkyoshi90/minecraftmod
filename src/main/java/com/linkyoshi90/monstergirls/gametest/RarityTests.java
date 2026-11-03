package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Journal;
import com.linkyoshi90.monstergirls.entity.Rarity;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.HashSet;
import java.util.Set;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Rarity tiers (entity.Rarity): the lists name real kinds, a legend comes once to a region, and meeting one is an advancement. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class RarityTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "tiersNameRealKinds")
    public static void tiersNameRealKinds(GameTestHelper helper) {
        Set<String> seen = new HashSet<>();
        for (String id : Rarity.RARE) check(BuiltInRegistries.ENTITY_TYPE.containsKey(MonsterGirlsMod.id(id)) && seen.add(id), "rare: " + id);
        for (String id : Rarity.LEGENDARY) check(BuiltInRegistries.ENTITY_TYPE.containsKey(MonsterGirlsMod.id(id)) && seen.add(id), "legendary: " + id);
        check(Rarity.of("wolf_canid") == Rarity.Tier.COMMON && Rarity.of("unicorn_centaur") == Rarity.Tier.RARE
                && Rarity.of("bahamut_dragon") == Rarity.Tier.LEGENDARY, "the tiers");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "oneLegendToARegion")
    public static void oneLegendToARegion(GameTestHelper helper) {
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(1, 2, 1));
        AbstractMonsterGirl first = spawn(helper, GirlSpecies.BAHAMUT_DRAGON, new BlockPos(2, 2, 2));
        AbstractMonsterGirl second = spawn(helper, GirlSpecies.BAHAMUT_DRAGON, new BlockPos(3, 2, 3));
        check(Rarity.spawnFactor(first) < Rarity.spawnFactor(wolf), "a legend is rarer than a common kind");
        var level = helper.getLevel();
        Rarity.Regions.get(level).free("bahamut_dragon", Rarity.region(first));   // whatever an earlier run left
        check(Rarity.claim(level, first) && first.isPersistenceRequired(), "the first comes, and stays");
        check(!Rarity.claim(level, second), "not a second in the same region");
        first.kill();
        check(Rarity.claim(level, second), "one who dies unbefriended leaves room for another");
        Rarity.Regions.get(level).free("bahamut_dragon", Rarity.region(second));
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "meetingALegend")
    public static void meetingALegend(GameTestHelper helper) {
        AbstractMonsterGirl legend = spawn(helper, GirlSpecies.KUN_DRAGON, new BlockPos(2, 2, 2));
        legend.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        var advancements = p.server.getAdvancements();
        AdvancementHolder meet = advancements.get(MonsterGirlsMod.id(Rarity.MEET)), friend = advancements.get(MonsterGirlsMod.id(Rarity.BEFRIEND)),
                every = advancements.get(MonsterGirlsMod.id(Rarity.EVERY));
        check(meet != null && friend != null && every != null, "the advancements are there");
        Journal.record(p, legend, false);
        check(p.getAdvancements().getOrStartProgress(meet).isDone() && !p.getAdvancements().getOrStartProgress(friend).isDone(), "met, not yet a friend");
        Journal.record(p, legend, true);
        var progress = p.getAdvancements().getOrStartProgress(every);
        check(p.getAdvancements().getOrStartProgress(friend).isDone() && progress.getCriterion("kun_dragon").isDone() && !progress.isDone(), "one legend of many");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "eachAtHerMoment")
    public static void eachAtHerMoment(GameTestHelper helper) {
        for (String id : com.linkyoshi90.monstergirls.entity.SpawnConditions.all().keySet()) {
            check(BuiltInRegistries.ENTITY_TYPE.containsKey(MonsterGirlsMod.id(id)), "a real kind: " + id);
        }
        check(com.linkyoshi90.monstergirls.entity.SpawnConditions.of("moon_rabbit_jackalope") == com.linkyoshi90.monstergirls.entity.SpawnConditions.When.FULL_MOON, "the moon rabbit");
        check(!met("FULL_MOON", 18000, 1, false, false) && met("FULL_MOON", 18000, 0, false, false) && !met("FULL_MOON", 6000, 0, false, false), "a full moon, at night");
        check(met("NIGHT", 24000L * 5 + 14000, 3, false, false) && !met("NIGHT", 1000, 3, false, false), "night on any day");
        check(met("STORM", 1000, 0, true, false) && met("RAIN", 1000, 0, true, false) && !met("RAIN", 1000, 0, false, false), "weather");
        check(com.linkyoshi90.monstergirls.entity.SpawnConditions.of("wolf_canid") == null, "most come whenever");
        helper.succeed();
    }

    private static boolean met(String when, long time, int moon, boolean rain, boolean thunder) {
        return com.linkyoshi90.monstergirls.entity.SpawnConditions.met(com.linkyoshi90.monstergirls.entity.SpawnConditions.When.valueOf(when), time, moon, rain, thunder);
    }
}
