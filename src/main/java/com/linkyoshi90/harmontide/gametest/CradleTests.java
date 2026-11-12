package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Cradle;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Upbringing;
import com.linkyoshi90.harmontide.registry.ModBlocks;
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

/** The cradle (entity/girl/Cradle): naps at night that count double, and a lullaby. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class CradleTests {
    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "cradleNapsAndLullabies")
    public static void cradleNapsAndLullabies(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos cradle = new BlockPos(7, 2, 7);
        helper.setBlock(cradle, ModBlocks.CRADLE.get());
        AbstractMonsterGirl young = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(8, 2, 7)), grown = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(6, 2, 7));
        young.setBaby(true);
        young.setNoAi(true);
        grown.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 9));
        check(Cradle.near(level, young.blockPosition()).equals(helper.absolutePos(cradle)), "she finds the cradle");
        int before = young.getGrowTicks();
        check(!Cradle.tick(level, young, false) && young.getGrowTicks() == before, "not by day");
        check(Cradle.tick(level, young, true) && young.getGrowTicks() == before + Cradle.EVERY, "at night she naps there, growing faster");
        check(!Cradle.tick(level, grown, true), "only the little ones");
        int fond = young.getAffection(p);
        check(Cradle.rock(level, helper.absolutePos(cradle), p) == 1 && young.getAffection(p) == fond + Cradle.ROCKED, "a lullaby");
        check(Cradle.rock(level, helper.absolutePos(cradle), p) == 1 && young.getAffection(p) == fond + Cradle.ROCKED, "once a minute");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "toysTipHerCalling")
    public static void toysTipHerCalling(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl young = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(7, 2, 7));
        young.setBaby(true);
        young.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(8, 2, 7));
        int[] before = com.linkyoshi90.harmontide.entity.girl.Roles.weights(level, young);
        net.minecraft.world.item.ItemStack sword = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WOODEN_SWORD, 4);
        check(Upbringing.give(level, young, p, sword) == Upbringing.Answer.LOVES_IT && sword.getCount() == 3, "she loves it");
        check(Upbringing.give(level, young, p, sword) == Upbringing.Answer.TOMORROW && sword.getCount() == 3, "one a day");
        int[] after = com.linkyoshi90.harmontide.entity.girl.Roles.weights(level, young);
        int guard = com.linkyoshi90.harmontide.entity.girl.Roles.Role.GUARD.ordinal();
        check(after[guard] == before[guard] + Upbringing.WEIGHT, "a guard is likelier: " + after[guard]);
        check(Upbringing.give(level, young, p, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK)) == null, "not a toy");
        young.setBaby(false);
        check(Upbringing.give(level, young, p, sword) == null, "only for little ones");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "growingUpMoments")
    public static void growingUpMoments(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl young = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(7, 2, 7));
        young.setBaby(true);
        young.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(8, 2, 7));
        young.bornTo(p.getUUID());
        check(young.hasCustomName() && com.linkyoshi90.harmontide.entity.Names.NEWBORN.contains(young.getCustomName().getString()), "she came with a name");
        var listed = com.linkyoshi90.harmontide.entity.Marriage.childList(p);
        check(!listed.isEmpty() && listed.getCompound(listed.size() - 1).getString("Name").equals(young.getCustomName().getString()), "and the journal lists her");
        com.linkyoshi90.harmontide.entity.Marriage.renamed(level, young, "Pip");
        listed = com.linkyoshi90.harmontide.entity.Marriage.childList(p);
        check(listed.getCompound(listed.size() - 1).getString("Name").equals("Pip"), "renamed, the journal follows");
        check(com.linkyoshi90.harmontide.entity.girl.GrowingUp.tick(level, young) == null, "too little yet");
        young.setGrowTicks(AbstractMonsterGirl.GROW_TICKS * 3 / 10);
        check("word".equals(com.linkyoshi90.harmontide.entity.girl.GrowingUp.tick(level, young)), "her first word");
        check(com.linkyoshi90.harmontide.entity.girl.GrowingUp.tick(level, young) == null, "once");
        young.setGrowTicks(AbstractMonsterGirl.GROW_TICKS * 8 / 10);
        check("flower".equals(com.linkyoshi90.harmontide.entity.girl.GrowingUp.tick(level, young)), "a flower");
        check("drawing".equals(com.linkyoshi90.harmontide.entity.girl.GrowingUp.tick(level, young)), "a drawing");
        check(com.linkyoshi90.harmontide.entity.girl.GrowingUp.tick(level, young) == null, "all three");
        check(com.linkyoshi90.harmontide.entity.girl.Roles.comingOfAge(level, young, com.linkyoshi90.harmontide.entity.girl.Roles.Role.SCOUT)
                && p.getInventory().countItem(net.minecraft.world.item.Items.SPYGLASS) == 1, "grown up, she brings the first thing of her calling");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "storyTimeForTheLittleOnes")
    public static void storyTimeForTheLittleOnes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl a = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(7, 2, 7)), b = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(9, 2, 7)),
                grown = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(5, 2, 7));
        a.setBaby(true);
        b.setBaby(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 9));
        net.minecraft.world.item.ItemStack book = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WRITTEN_BOOK);
        int fond = b.getAffection(p);
        check(com.linkyoshi90.harmontide.entity.girl.StoryTime.read(level, grown, p, book) == 0, "a story is for the little ones");
        check(com.linkyoshi90.harmontide.entity.girl.StoryTime.read(level, a, p, book) == 2, "both little ones listen");
        check(b.getAffection(p) == fond + com.linkyoshi90.harmontide.entity.girl.StoryTime.CLOSER && b.getGrowTicks() == com.linkyoshi90.harmontide.entity.girl.StoryTime.GROWTH, "closer, and a little bigger");
        com.linkyoshi90.harmontide.entity.girl.StoryTime.read(level, a, p, book);
        check(b.getAffection(p) == fond + com.linkyoshi90.harmontide.entity.girl.StoryTime.CLOSER, "once a day");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aGrandchildIsListedAsOne")
    public static void aGrandchildIsListedAsOne(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl daughter = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(7, 2, 7));
        daughter.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(8, 2, 7));
        daughter.bornTo(p.getUUID());                                         // their daughter (grown here, for the test)
        check(com.linkyoshi90.harmontide.entity.Marriage.children(p) == 1, "one little one of theirs");
        AbstractMonsterGirl grand = daughter.spawnYoung(level, daughter.blockPosition(), p.getUUID());
        var list = com.linkyoshi90.harmontide.entity.Marriage.childList(p);
        check(grand != null && list.size() == 2 && list.getCompound(1).getBoolean("Grand"), "her little one is listed as a grandchild");
        check(com.linkyoshi90.harmontide.entity.Marriage.children(p) == 1, "and not counted as theirs");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "littleOnesRunToThemAtTheThunder")
    public static void littleOnesRunToThemAtTheThunder(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl young = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(7, 2, 7));
        young.setBaby(true);
        young.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(8, 2, 7));
        young.bornTo(p.getUUID());
        level.setWeatherParameters(0, 600, true, true);
        level.setRainLevel(1.0F);                                           // at full strength now, not after the usual ramp
        level.setThunderLevel(1.0F);
        int huddled = com.linkyoshi90.harmontide.entity.Company.thunder(level, p);
        level.setWeatherParameters(6000, 0, false, false);                  // put the sky back: other tests share it
        level.setRainLevel(0.0F);
        level.setThunderLevel(0.0F);
        check(huddled == 1, "the little wolf hides against her parent: " + huddled);
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "harpyChicksHearStoriesToo")
    public static void harpyChicksHearStoriesToo(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var chick = helper.spawn(com.linkyoshi90.harmontide.registry.ModEntities.SPARROW_HARPY.get(), new BlockPos(7, 2, 7));
        chick.setBaby(true);
        chick.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(8, 2, 7));
        int fond = chick.getAffection(p);
        check(com.linkyoshi90.harmontide.entity.girl.StoryTime.read(level, chick, p, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WRITTEN_BOOK)) == 1
                && chick.getAffection(p) > fond, "a harpy chick listens too");
        helper.setBlock(new BlockPos(8, 2, 8), ModBlocks.CRADLE.get());
        check(Cradle.tick(level, chick, true), "and naps by the cradle");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
