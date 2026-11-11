package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Squad;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlArms;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.order.Order;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The squad (entity/Squad): enlisting with a horn, the rally, the bodyguard, the call to arms. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class SquadTests {
    private static AbstractMonsterGirl girl(GameTestHelper helper, GirlSpecies s, BlockPos at, ServerPlayer p, int affection) {
        AbstractMonsterGirl g = spawn(helper, s, at);
        g.setNoAi(true);
        g.setAffection(p.getUUID(), affection);
        return g;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "enlistingTheSquad")
    public static void enlistingTheSquad(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        AbstractMonsterGirl cat = girl(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(5, 2, 5), p, 50);
        check(GirlArms.isFighter(GirlSpecies.ONI_DEVIL), "the oni fights");
        check(GirlArms.isFighter(cat) || !Squad.enlist(level, cat, p), "only fighters");
        AbstractMonsterGirl stranger = girl(helper, GirlSpecies.ONI_DEVIL, new BlockPos(6, 2, 5), p, 0);
        check(!Squad.enlist(level, stranger, p), "only friends");
        for (int i = 0; i < Squad.SIZE; i++) {
            check(Squad.enlist(level, girl(helper, GirlSpecies.ONI_DEVIL, new BlockPos(2 + i, 2, 8), p, 20), p), "enlisted " + i);
        }
        AbstractMonsterGirl fifth = girl(helper, GirlSpecies.ONI_DEVIL, new BlockPos(8, 2, 8), p, 20);
        check(Squad.size(p) == Squad.SIZE && !Squad.enlist(level, fifth, p), "four at most");
        AbstractMonsterGirl first = Squad.members(level, p, 32).get(0);
        check(Squad.dismiss(level, first, p) && Squad.size(p) == Squad.SIZE - 1 && Squad.enlist(level, fifth, p), "one let go, room for another");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "theHornAndTheBodyguard")
    public static void theHornAndTheBodyguard(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        AbstractMonsterGirl oni = girl(helper, GirlSpecies.ONI_DEVIL, new BlockPos(10, 2, 10), p, 30);
        check(Squad.enlist(level, oni, p), "enlisted");
        oni.setOrderedToSit(true);
        check(Squad.rally(level, p) == 1 && Squad.rallied(oni) && !oni.isOrderedToSit(), "the horn: up and coming, ready to fight");
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(4, 2, 6));
        z.setNoAi(true);
        check(Squad.duty(level, oni, p).equals("rallied") && oni.getTarget() == z, "rallied, she goes for the monster by her leader");
        oni.setTarget(null);
        Zombie biter = helper.spawn(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
        biter.setNoAi(true);
        check(Squad.defend(level, p, biter) == 1 && oni.getTarget() == biter, "whatever strikes her leader, she turns on");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "theCallToArms")
    public static void theCallToArms(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        AbstractMonsterGirl oni = girl(helper, GirlSpecies.ONI_DEVIL, new BlockPos(10, 2, 10), p, 30);
        check(Squad.enlist(level, oni, p) && Squad.duty(level, oni, p).equals("idle"), "at peace");
        BlockPos hearth = helper.absolutePos(new BlockPos(5, 2, 5));
        var sisters = Order.startRaid(level, hearth, p, 1, 1);
        check(Squad.raidedHearth(level, p) != null, "a crusade on her leader's hearth");
        check(Squad.duty(level, oni, p).equals("defending"), "she marches home to defend it");
        for (Order.Raid raid : Order.raids(level).all()) if (raid.player().equals(p.getUUID())) Order.end(level, raid, false);
        sisters.forEach(net.minecraft.world.entity.Entity::discard);
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
