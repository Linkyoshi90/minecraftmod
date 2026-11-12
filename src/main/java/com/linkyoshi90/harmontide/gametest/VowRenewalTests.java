package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Marriage;
import com.linkyoshi90.harmontide.entity.MatingSeason;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Vow renewal (Marriage.renewVows): the ring again on the anniversary, by a hearth; once a year. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class VowRenewalTests {
    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "vowsRenewedOnTheDay")
    public static void vowsRenewedOnTheDay(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos hp = new BlockPos(3, 2, 3);
        helper.setBlock(hp, ModBlocks.HEARTH.get());
        AbstractMonsterGirl wife = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(4, 2, 4));
        wife.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 3));
        wife.setAffection(p.getUUID(), 100);
        helper.runAfterDelay(2, () -> {                                     // the hearth counts as loaded from the next tick
        Marriage.wed(level, wife, p, helper.absolutePos(hp));
        check(Marriage.married(wife, p), "wed");
        check(Marriage.propose(level, wife, p) == Marriage.Answer.ALREADY && !Marriage.renewedToday(level, wife), "not on an ordinary day");
        Marriage.setWedDay(wife, MatingSeason.today(level) - Marriage.YEAR);
        int before = wife.getAffection(p);
        check(Marriage.propose(level, wife, p) == Marriage.Answer.RENEWED && Marriage.renewedToday(level, wife), "on the anniversary, by the hearth: the vows renewed");
        check(Marriage.honeymoon(wife) && wife.getAffection(p) >= before, "a second honeymoon");
        CompoundTag entry = Marriage.family(p).getCompound(0);
        check(entry.getInt("Renewed") == 1, "the family page counts it: " + entry);
        check(Marriage.propose(level, wife, p) == Marriage.Answer.ALREADY, "once a year");
        HarpyTestUtil.remove(p);
        helper.succeed();
        });
    }
}
