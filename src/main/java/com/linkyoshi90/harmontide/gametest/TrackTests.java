package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.FootprintBlock;
import com.linkyoshi90.harmontide.entity.Signs;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Tracks and signs (entity/Signs, block/FootprintBlock). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class TrackTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "footprintsInTheSand")
    public static void footprintsInTheSand(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 2; x <= 6; x++) helper.setBlock(new BlockPos(x, 1, 4), Blocks.SAND);
        AbstractMonsterGirl horse = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(3, 2, 4));
        horse.setNoAi(true);
        check(Signs.printOf(horse) == FootprintBlock.Kind.HOOF, "a centaur leaves hoofprints");
        BlockPos at = Signs.print(level, horse);
        check(at != null && level.getBlockState(at).is(ModBlocks.FOOTPRINT.get()), "pressed into the sand");
        check(Signs.print(level, horse) == null, "not twice in one spot");
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(10, 2, 10));
        wolf.setNoAi(true);
        check(Signs.print(level, wolf) == null, "grass takes no prints");
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 6));
        check(Signs.read(level, at, p) && Signs.noted(p) == 1, "read, and noted in the field notes");
        Signs.read(level, at, p);
        check(Signs.noted(p) == 1, "the same kind only once");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "gnawedBark")
    public static void gnawedBark(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.setBlock(new BlockPos(5, 2, 4), Blocks.OAK_LOG);
        AbstractMonsterGirl beaver = spawn(helper, GirlSpecies.BEAVER_RODENT, new BlockPos(4, 2, 4));
        beaver.setNoAi(true);
        check("bark".equals(Signs.sign(level, beaver)) && level.getBlockState(helper.absolutePos(new BlockPos(5, 2, 4))).is(Blocks.STRIPPED_OAK_LOG), "the beaver strips the bark");
        helper.succeed();
    }
}
