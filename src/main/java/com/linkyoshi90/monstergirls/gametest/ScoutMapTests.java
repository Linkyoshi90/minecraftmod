package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.ScoutMaps;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Scout maps (entity/ScoutMaps). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class ScoutMapTests {
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "eachChartsHerOwnKindOfPlace")
    public static void eachChartsHerOwnKindOfPlace(GameTestHelper helper) {
        AbstractMonsterGirl mermaid = spawn(helper, GirlSpecies.DOLPHIN_MERMAID, new BlockPos(3, 1, 3));
        AbstractMonsterGirl succubus = spawn(helper, GirlSpecies.SUCCUBUS_DEVIL, new BlockPos(6, 1, 3));
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(9, 1, 3));
        check(ScoutMaps.places(mermaid).equals(List.of(ModStructures.MERFOLK_GROTTO_KEY)), "the sea's girls chart grottoes");
        check(ScoutMaps.places(succubus).contains(ModStructures.DEMON_SPIRE_KEY) && !ScoutMaps.places(succubus).contains(ModStructures.ORDER_CHAPEL_KEY), "the realm's own, its places");
        check(ScoutMaps.places(wolf).size() == ScoutMaps.ALL.size(), "anyone else, anything");
        ItemStack map = ScoutMaps.chart(helper.getLevel(), helper.absolutePos(new BlockPos(7, 1, 7)), List.of(ModStructures.ORDER_CHAPEL_KEY), 0);
        check(map.isEmpty() || map.is(Items.FILLED_MAP), "a filled map, or none where there's nothing to find");
        helper.succeed();
    }
}
