package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Territories;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Territories and scent marks (entity/Territories). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class TerritoryTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "rivalsKeepOff")
    public static void rivalsKeepOff(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl holder = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(3, 2, 3));
        AbstractMonsterGirl rival = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(6, 2, 3));
        AbstractMonsterGirl sheep = spawn(helper, GirlSpecies.SHEEP_MINOTAUR, new BlockPos(6, 2, 6));
        for (AbstractMonsterGirl g : new AbstractMonsterGirl[]{holder, rival, sheep}) g.setNoAi(true);
        check(Territories.holdsGround(holder) && !Territories.holdsGround(sheep), "hunters hold ground, grazers don't");
        check(Territories.home(holder).getX() == holder.getBlockX(), "she claims where she is");
        Territories.mark(level, holder, holder.blockPosition());
        check(Territories.rivalMark(level, holder, holder.blockPosition(), 8) == null, "her own mark is no rival's");
        check(Territories.rivalMark(level, rival, rival.blockPosition(), 8) != null, "the rival finds it");
        BlockPos before = Territories.home(rival);
        check(Territories.yields(level, rival) && !Territories.home(rival).equals(before), "and makes her home somewhere else");
        check(Territories.home(rival).getX() > rival.getX(), "away from the mark: " + Territories.home(rival));
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aSharpNoseSmellsMarks")
    public static void aSharpNoseSmellsMarks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 7));
        AbstractMonsterGirl sheep = spawn(helper, GirlSpecies.SHEEP_MINOTAUR, new BlockPos(5, 2, 5));
        sheep.setNoAi(true);
        sheep.addAffection(p, 20);
        check(!Territories.smells(level, p), "a sheep friend has no nose for it");
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(9, 2, 9));
        wolf.setNoAi(true);
        check(!Territories.sniffer(wolf, p), "a wild wolf won't show you");
        wolf.addAffection(p, 20);
        check(Territories.sniffer(wolf, p) && Territories.smells(level, p), "a wolf friend smells them for you");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
