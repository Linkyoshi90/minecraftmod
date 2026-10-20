package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Bug;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.interact;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The insect family (bug_field_guide.html). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class BugTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "caterpillarBecomesAMoth")
    public static void caterpillarBecomesAMoth(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Bug larva = spawn(helper, GirlSpecies.MOTH_LARVA_BUG, new BlockPos(7, 2, 7));
        larva.setBaby(true);
        check(!larva.isBaby(), "a caterpillar hatches full size");
        larva.setAffection(player.getUUID(), 30);
        AbstractMonsterGirl moth = larva.metamorphose(helper.getLevel());
        check(moth != null && moth.getSpecies() == GirlSpecies.MOTH_BUG, "she becomes a moth");
        check(moth.getAffection(player) == 30 && !larva.isAlive(), "and keeps her bonds");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "mosquitoCalmsWithHoney")
    public static void mosquitoCalmsWithHoney(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 7));
        Bug mosquito = spawn(helper, GirlSpecies.MOSQUITO_BUG, new BlockPos(7, 2, 7));
        interact(mosquito, player, new ItemStack(Items.BEEF));
        check(!mosquito.isCalm(), "meat is not what she wants");
        interact(mosquito, player, new ItemStack(Items.HONEY_BOTTLE));
        check(mosquito.isCalm(), "honey calms her");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 1600, batch = "fairyBuildsAFountainAndAQueenRises")
    public static void fairyBuildsAFountainAndAQueenRises(GameTestHelper helper) {
        for (int x = 3; x <= 11; x++) for (int z = 3; z <= 11; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.GRASS_BLOCK);
        Bug fairy = spawn(helper, GirlSpecies.FAIRY_BUG, new BlockPos(7, 2, 7));
        fairy.setFountain(helper.absolutePos(new BlockPos(7, 1, 7)));
        fairy.setOrderedToSit(true);
        helper.succeedWhen(() -> {
            check(fairy.isFountainFinished(), "she should finish the fountain");
            check(fairy.summonQueen(helper.getLevel()) != null, "a Fairy Queen can rise from it");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "queenBeeLovesHiveKeepers")
    public static void queenBeeLovesHiveKeepers(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 7));
        Bug queen = spawn(helper, GirlSpecies.QUEEN_BEE_BUG, new BlockPos(7, 2, 7));
        Bug.onHivePlaced(player, helper.absolutePos(new BlockPos(9, 2, 9)));
        check(queen.getAffection(player) == Bug.QUEEN_HIVE_BONUS, "a hive placed near her: +5");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 1200, batch = "beeFillsTheHive")
    public static void beeFillsTheHive(GameTestHelper helper) {
        helper.setBlock(new BlockPos(5, 1, 5), Blocks.GRASS_BLOCK);
        helper.setBlock(new BlockPos(5, 2, 5), Blocks.POPPY.defaultBlockState());
        helper.setBlock(new BlockPos(9, 2, 9), Blocks.BEEHIVE);
        Bug bee = spawn(helper, GirlSpecies.BEE_BUG, new BlockPos(7, 2, 7));
        helper.succeedWhen(() -> check(helper.getBlockState(new BlockPos(9, 2, 9)).getValue(BeehiveBlock.HONEY_LEVEL) > 0, "honey in the hive"));
    }
}
