package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Lactation;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Fixes of 2026-11-13: tall blocks mobs walked into, the giraffe's milk. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class FixTests20261113 {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "tallBlocksAreNotOpenGround")
    public static void tallBlocksAreNotOpenGround(GameTestHelper helper) {
        for (Block b : List.of(ModBlocks.MAILBOX.get(), ModBlocks.SPARRING_POST.get(), ModBlocks.VANITY_MIRROR.get(), ModBlocks.CHEESE_SHELF.get(),
                ModBlocks.BUTTER_CHURN.get(), ModBlocks.HARPY_PERCH.get(), ModBlocks.CRADLE.get())) {
            check(!b.defaultBlockState().isPathfindable(PathComputationType.LAND), "mobs don't walk through " + b);
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "giraffeMilkFromHerChest")
    public static void giraffeMilkFromHerChest(GameTestHelper helper) {
        AbstractMonsterGirl giraffe = spawn(helper, GirlSpecies.GIRAFFE_CENTAUR, new BlockPos(7, 2, 7));
        giraffe.setNoAi(true);
        double h = Lactation.chest(giraffe)[0];
        check(h > giraffe.getBbHeight() * 0.72, "high up on her torso, not her back: " + h);
        helper.succeed();
    }
}
