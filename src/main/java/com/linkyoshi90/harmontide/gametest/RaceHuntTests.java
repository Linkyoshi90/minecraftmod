package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Games;
import com.linkyoshi90.harmontide.entity.HomeLife;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Races and treasure hunts (entity/Games). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class RaceHuntTests {
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aRaceToTheFinish")
    public static void aRaceToTheFinish(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 1, 1));
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(2, 1, 1));
        BlockPos finish = helper.absolutePos(new BlockPos(13, 1, 13));
        check(!Games.startRace(level, girl, p, finish), "only with a friend");
        girl.addAffection(p, HomeLife.FRIEND);
        int before = girl.getAffection(p);
        check(Games.startRace(level, girl, p, finish) && Games.racing(girl), "on your marks");
        check(Games.raceTick(level, girl) == 0, "three... two... one...");
        p.teleportTo(finish.getX() + 0.5, finish.getY(), finish.getZ() + 0.5);
        girl.getPersistentData().putLong("RaceGo", 0L);                       // the count is done
        check(Games.raceTick(level, girl) == 1 && !Games.racing(girl), "the player gets there first");
        check(girl.getAffection(p) == before + Games.RACE_AFFECTION, "and she likes them the better for it");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aTreasureHunt")
    public static void aTreasureHunt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 1, 1));
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(2, 1, 1));
        girl.addAffection(p, HomeLife.FRIEND);
        BlockPos at = helper.absolutePos(new BlockPos(10, 0, 10));
        check(Games.bury(level, girl, p, at, new ItemStack(Items.EMERALD, 2)) && Games.hunting(girl), "she buries it");
        check(level.getBlockState(at).is(Blocks.SUSPICIOUS_GRAVEL) && level.getBlockEntity(at) instanceof BrushableBlockEntity be && be.getItem().is(Items.EMERALD),
                "under suspicious gravel, with the present inside");
        p.teleportTo(at.getX() + 0.5, at.getY() + 1, at.getZ() + 0.5);
        check(Games.huntTick(level, girl) == 0, "warmer, warmer");
        level.setBlockAndUpdate(at, Blocks.GRAVEL.defaultBlockState());          // brushed out
        check(Games.huntTick(level, girl) == 1 && !Games.hunting(girl), "found!");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
