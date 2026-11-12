package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.HomeLife;
import com.linkyoshi90.harmontide.entity.Journal;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Stories;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Stories by the fire (entity.girl.Stories). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class StoryTests {
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aStoryByTheFire")
    public static void aStoryByTheFire(GameTestHelper helper) {
        var level = helper.getLevel();
        helper.setDayTime(18000);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 4));
        BlockPos fire = new BlockPos(5, 2, 5);
        helper.setBlock(fire, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
        AbstractMonsterGirl a = spawn(helper, GirlSpecies.INABA_JACKALOPE, new BlockPos(6, 2, 4));
        AbstractMonsterGirl b = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(6, 2, 6));
        a.setNoAi(true);
        b.setNoAi(true);
        a.addAffection(p, HomeLife.FRIEND);
        helper.runAfterDelay(2, () -> {
            check(Stories.check(level, p) == null, "no fire lit");
            helper.setBlock(fire, Blocks.CAMPFIRE.defaultBlockState());
            check(Stories.check(level, p) == null, "one friend is not a circle");
            b.addAffection(p, HomeLife.FRIEND);
            int before = a.getAffection(p);
            AbstractMonsterGirl teller = Stories.check(level, p);
            check(teller == a || teller == b, "a story");
            check(a.getAffection(p) - before == Stories.CLOSER, "listening brings them closer");
            check(Stories.check(level, p) == null, "one a night");
            check(Journal.book(p).pages().size() > 1, "the journal has a page for it");
            GirlSpecies other = Stories.subject(level, p, a);
            check(other != null && Journal.hearOf(p, other.id()) && !Journal.hearOf(p, other.id()), "a kind heard of once");
            check(Journal.sightings(p, other.id()) == Journal.KNOWN, "her home shows");
            check(other.clade() == GirlSpecies.INABA_JACKALOPE.clade(), "she tells of her own branch first: " + other.id());
            helper.setDayTime(1000);
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }
}
