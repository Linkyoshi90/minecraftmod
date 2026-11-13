package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Microhabitats;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Microhabitats (entity/Microhabitats). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class MicrohabitatTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "everyKindItsCorner")
    public static void everyKindItsCorner(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl camel = spawn(helper, GirlSpecies.CAMEL_CENTAUR, new BlockPos(3, 2, 3));
        camel.setNoAi(true);
        check(Microhabitats.needs(camel).contains(Microhabitats.Need.DRY), "a camel wants dry ground: " + Microhabitats.needs(camel));
        AbstractMonsterGirl koi = spawn(helper, GirlSpecies.KOI_MERMAID, new BlockPos(12, 2, 12));
        koi.setNoAi(true);
        check(Microhabitats.needs(koi).isEmpty() && Microhabitats.comfort(koi) == 1.0F, "water kinds have no needs on land");
        BlockPos dry = helper.absolutePos(new BlockPos(3, 2, 3)), wet = helper.absolutePos(new BlockPos(9, 2, 9));
        helper.setBlock(new BlockPos(10, 1, 9), Blocks.WATER);
        check(Microhabitats.meets(level, wet, Microhabitats.Need.WET) && !Microhabitats.meets(level, wet, Microhabitats.Need.DRY), "water beside: wet, not dry");
        check(Microhabitats.meets(level, dry, Microhabitats.Need.DRY), "no water near: dry");
        List<Microhabitats.Need> wants = List.of(Microhabitats.Need.DRY, Microhabitats.Need.WET);
        check(Microhabitats.comfort(level, dry, wants) == 0.5F, "half her needs met, half suited");
        helper.succeed();
    }
}
