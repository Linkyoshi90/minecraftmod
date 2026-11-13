package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.FoodWeb;
import com.linkyoshi90.harmontide.entity.Pasture;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Pastures: grazing and regrowth (entity/Pasture). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class PastureTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "pasturesGrowBack")
    public static void pasturesGrowBack(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl sheep = spawn(helper, GirlSpecies.SHEEP_MINOTAUR, new BlockPos(3, 2, 3));
        sheep.setNoAi(true);
        helper.setBlock(new BlockPos(4, 2, 3), Blocks.SHORT_GRASS);
        BlockPos plant = helper.absolutePos(new BlockPos(4, 2, 3));
        check(FoodWeb.graze(level, sheep, plant) && level.getBlockState(plant).isAir(), "cropped");
        long now = level.getGameTime();
        Pasture.regrow(level, now + Pasture.Kind.CROPPED.rest + 1);
        check(level.getBlockState(plant).isAir(), "not while the grazer is still here");
        sheep.discard();
        Pasture.regrow(level, now + 10);
        check(level.getBlockState(plant).isAir(), "not before the land has rested");
        Pasture.regrow(level, now + Pasture.Kind.CROPPED.rest + 1);
        check(!level.getBlockState(plant).isAir(), "the grass grows back: " + level.getBlockState(plant));
        BlockPos trail = helper.absolutePos(new BlockPos(9, 1, 9));
        level.setBlockAndUpdate(trail, Blocks.DIRT_PATH.defaultBlockState());
        Pasture.note(level, trail, Pasture.Kind.TRAIL);
        Pasture.regrow(level, level.getGameTime() + Pasture.Kind.TRAIL.rest + 1);
        check(level.getBlockState(trail).is(Blocks.GRASS_BLOCK), "a trail fades back to grass");
        helper.succeed();
    }
}
