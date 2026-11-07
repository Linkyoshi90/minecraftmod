package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.Clade;
import com.linkyoshi90.monstergirls.entity.Flirting;
import com.linkyoshi90.monstergirls.entity.MatingSeason;
import com.linkyoshi90.monstergirls.entity.Missing;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Mating season (entity/MatingSeason): each branch's restless days, what they do, and the cold that calms them. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class MatingSeasonTests {
    /** A grown, full-size kind of ordinary size whose branch is (or isn't) in season on this day. */
    @javax.annotation.Nullable
    private static GirlSpecies kind(long day, boolean season) {
        Predicate<GirlSpecies> ok = s -> s.height() >= Bonded.CHILDLIKE_HEIGHT && s.height() <= 2.0F && s.family() != GirlSpecies.Family.CONSTRUCT
                && s.family() != GirlSpecies.Family.MERFOLK && s.family() != GirlSpecies.Family.RIDER;
        for (GirlSpecies s : GirlSpecies.values()) if (ok.test(s) && MatingSeason.seasonOn(s.clade(), day) == season) return s;
        return null;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "seasonClock")
    public static void seasonClock(GameTestHelper helper) {
        Set<Integer> starts = new HashSet<>();
        for (Clade c : Clade.values()) {
            int s = MatingSeason.start(c);
            starts.add(s);
            check(MatingSeason.seasonOn(c, s) && MatingSeason.seasonOn(c, s + MatingSeason.LENGTH - 1) && !MatingSeason.seasonOn(c, s + MatingSeason.LENGTH)
                    && MatingSeason.seasonOn(c, s + MatingSeason.CYCLE) && !MatingSeason.seasonOn(c, s - 1), "three days a year for " + c);
        }
        check(starts.size() == Math.min(Clade.values().length, MatingSeason.CYCLE), "each branch at its own time");
        for (int day = 0; day < MatingSeason.CYCLE; day++) {                            // and some branch is restless every day
            final int d = day;
            check(java.util.Arrays.stream(Clade.values()).anyMatch(c -> MatingSeason.seasonOn(c, d)), "nobody in season on day " + day);
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "seasonMakesHerClingy")
    public static void seasonMakesHerClingy(GameTestHelper helper) {
        // Whole days on, to a day when an ordinary-sized kind is in season (the time of day stays); put back before this tick ends.
        net.minecraft.server.level.ServerLevel level = helper.getLevel();
        long saved = level.getDayTime();
        int shift = 0;
        while (kind(MatingSeason.today(level) + shift, true) == null || kind(MatingSeason.today(level) + shift, false) == null) shift++;
        level.setDayTime(saved + shift * 24000L);
        try {
            clingy(helper);
        } finally {
            level.setDayTime(saved);
        }
        helper.succeed();
    }

    private static void clingy(GameTestHelper helper) {
        long today = MatingSeason.today(helper.getLevel());
        AbstractMonsterGirl girl = spawn(helper, kind(today, true), new BlockPos(3, 2, 3)), other = spawn(helper, kind(today, false), new BlockPos(11, 2, 11));
        girl.setNoAi(true);
        other.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(9, 2, 3));
        try {
            girl.addAffection(p, 60);
            other.addAffection(p, 60);
            check(!MatingSeason.inSeason(other), "not her branch's days");
            if (MatingSeason.cold(girl)) {                                       // the test world stands in a cold biome: cold calms her
                check(!MatingSeason.inSeason(girl), "the cold keeps her calm");
                return;
            }
            check(MatingSeason.inSeason(girl), "in season");
            check(Flirting.chance(girl, p) == 2 && Missing.bedBonus(girl, p) == MatingSeason.BED, "flirts twice as often, keener for bed");
            check(MatingSeason.restless(helper.getLevel(), girl) == p && girl.hasFlag(AbstractMonsterGirl.FLAG_BLUSHING), "she seeks them out, blushing");
            p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SNOWBALL, 2));
            CommonHooks.onInteractEntity(p, girl, InteractionHand.MAIN_HAND);
            check(!MatingSeason.inSeason(girl) && p.getMainHandItem().getCount() == 1, "a snowball calms her for the season");
            check(Flirting.chance(girl, p) == 4 && Missing.bedBonus(girl, p) == 0.0F, "as before");
            AbstractMonsterGirl young = spawn(helper, kind(today, true), new BlockPos(6, 2, 10));
            young.setNoAi(true);
            young.setBaby(true);
            check(!MatingSeason.inSeason(young), "never the young");
        } finally {
            HarpyTestUtil.remove(p);
        }
    }
}
