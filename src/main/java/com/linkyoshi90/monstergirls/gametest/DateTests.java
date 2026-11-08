package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Dates;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Dates (entity/Dates): asked out with a flower, her kind of place, half a minute together. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class DateTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "onlyACloseFriendSaysYes")
    public static void onlyACloseFriendSaysYes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(10, 2, 10));
        AbstractMonsterGirl stranger = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(2, 2, 2));
        AbstractMonsterGirl daughter = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(4, 2, 2));
        daughter.setBaby(false);
        daughter.bornTo(p.getUUID());
        AbstractMonsterGirl friend = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(6, 2, 2));
        friend.addAffection(p, 40);
        check(!Dates.ask(level, stranger, p), "a stranger says no");
        check(!Dates.ask(level, daughter, p), "never a daughter");
        check(Dates.ask(level, friend, p) && p.getUUID().equals(Dates.dateWith(friend)), "a close friend says yes");
        check(Dates.favourite(friend) == Dates.favourite(friend), "always the same kind of place for her");
        Dates.end(friend);
        check(!Dates.ask(level, friend, p), "one date a day");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aLovelyDateAmongTheFlowers")
    public static void aLovelyDateAmongTheFlowers(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 7));
        AbstractMonsterGirl girl = null;
        for (int i = 0; i < 60 && girl == null; i++) {                     // one who loves flowers
            AbstractMonsterGirl g = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(8, 2, 7));
            if (Dates.favourite(g) == Dates.Place.FLOWERS) girl = g;
            else g.discard();
        }
        check(girl != null, "someone loves flowers");
        check(!Dates.fits(level, p.blockPosition(), Dates.Place.FLOWERS), "no flowers yet");
        for (int i = 0; i < Dates.FLOWERS; i++) helper.setBlock(new BlockPos(3 + i, 2, 4), Blocks.POPPY);
        check(Dates.fits(level, p.blockPosition(), Dates.Place.FLOWERS), "now there are");
        girl.addAffection(p, 40);
        int before = girl.getAffection(p);
        check(Dates.ask(level, girl, p), "she says yes");
        Dates.tick(level, girl, Dates.TOGETHER);
        check(Dates.dateWith(girl) == null && girl.getAffection(p) == before + Dates.AFFECTION, "a lovely date: " + girl.getAffection(p));
        check(p.hasEffect(MobEffects.LUCK), "and the luck of it");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "theWatersEdge")
    public static void theWatersEdge(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos at = helper.absolutePos(new BlockPos(7, 2, 7));
        check(!Dates.fits(level, at, Dates.Place.SHORE), "dry land");
        helper.setBlock(new BlockPos(9, 1, 7), Blocks.WATER);
        check(Dates.fits(level, at, Dates.Place.SHORE) == level.canSeeSky(at.above()), "by the water, under the sky");
        helper.succeed();
    }
}
