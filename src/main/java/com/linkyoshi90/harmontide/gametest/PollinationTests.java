package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Pollination;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Pollination and seed spreading (entity/Pollination). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class PollinationTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "beesPollinate")
    public static void beesPollinate(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl bee = spawn(helper, GirlSpecies.BEE_BUG, new BlockPos(7, 2, 7));
        bee.setNoAi(true);
        check(Pollination.pollinator(bee), "a bee pollinates");
        helper.setBlock(new BlockPos(2, 2, 2), Blocks.POPPY);
        helper.setBlock(new BlockPos(10, 2, 10), Blocks.DANDELION);
        helper.setBlock(new BlockPos(11, 1, 11), Blocks.FARMLAND);
        helper.setBlock(new BlockPos(11, 2, 11), Blocks.WHEAT);
        check(!Pollination.visit(level, bee, helper.absolutePos(new BlockPos(2, 2, 2))), "the first flower only dusts her with pollen");
        check(Pollination.visit(level, bee, helper.absolutePos(new BlockPos(10, 2, 10))), "the next one, far enough off, is pollinated");
        BlockPos wheat = helper.absolutePos(new BlockPos(11, 2, 11));
        check(((CropBlock) Blocks.WHEAT).getAge(level.getBlockState(wheat)) == 1, "the wheat beside it grows on");
        int dandelions = 0;
        for (BlockPos p : BlockPos.betweenClosed(helper.absolutePos(new BlockPos(8, 2, 8)), helper.absolutePos(new BlockPos(12, 2, 12)))) {
            if (level.getBlockState(p).is(Blocks.DANDELION)) dandelions++;
        }
        check(dandelions >= 2, "another dandelion springs up: " + dandelions);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "seedsCarriedOff")
    public static void seedsCarriedOff(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl bear = spawn(helper, GirlSpecies.BLACK_BEAR_CANID, new BlockPos(2, 2, 2));
        bear.setNoAi(true);
        check(Pollination.seeder(bear), "a berry eater carries seeds");
        Pollination.swallow(level, bear);
        check(Pollination.carrying(bear), "she takes a seed with her");
        long now = level.getGameTime();
        check(Pollination.sow(level, bear, now + Pollination.CARRY) == null, "not where she ate it");
        bear.teleportTo(bear.getX() + 12, bear.getY(), bear.getZ() + 12);
        check(Pollination.sow(level, bear, now + 10) == null, "not so soon");
        BlockPos at = Pollination.sow(level, bear, now + Pollination.CARRY);
        check(at != null && !level.getBlockState(at).isAir() && !Pollination.carrying(bear), "far off and later, she leaves it to grow: " + (at == null ? "" : level.getBlockState(at)));
        helper.succeed();
    }
}
