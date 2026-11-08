package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Aurora;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Aurora nights (entity/Aurora). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class AuroraTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "auroraNightsAreRare")
    public static void auroraNightsAreRare(GameTestHelper helper) {
        int nights = 0;
        for (long n = 0; n < 600; n++) if (Aurora.auroraNight(n * 24000L + 18000L)) nights++;
        check(nights > 50 && nights < 150, "about one night in six: " + nights + " of 600");
        check(!Aurora.auroraNight(6000L) && !Aurora.auroraNight(24000L * 7 + 6000L), "never by day");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "underTheAurora")
    public static void underTheAurora(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 1, 7));
        List<Mob> came = Aurora.begin(level, p);
        check(!came.isEmpty() && came.stream().allMatch(Aurora::frostTouched), "the frost-touched come out: " + came.size());
        check(came.stream().allMatch(m -> m instanceof AbstractMonsterGirl g && Aurora.FROST_TOUCHED.contains(g.getSpecies()) && !g.isBaby()), "grown, of the cold country");
        Mob first = came.get(0);
        check(!Aurora.dawn(level, first, true), "she stays the night");
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(9, 1, 9));
        ItemStack shard = new ItemStack(ModItems.AURORA_SHARD.get(), 2);
        check(Aurora.gift(level, girl, p, shard) && girl.getAffection(p) == Aurora.SHARD_AFFECTION && shard.getCount() == 1, "every girl loves a shard");
        ((com.linkyoshi90.monstergirls.entity.Bonded) first).addAffection(p, 1);
        check(!Aurora.dawn(level, first, false) && !Aurora.frostTouched(first), "a friend stays past dawn");
        if (came.size() > 1) check(Aurora.dawn(level, came.get(1), false) && came.get(1).isRemoved(), "the other goes at dawn");
        came.forEach(Mob::discard);
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
