package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Company;
import com.linkyoshi90.monstergirls.entity.HomeLife;
import com.linkyoshi90.monstergirls.entity.Visits;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Chores;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Daily visits (entity.Visits), the swim buddy (entity.Company) and the lamplighter chore (entity.girl.Chores). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class VisitTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "visitsCountInARow")
    public static void visitsCountInARow(GameTestHelper helper) {
        var level = helper.getLevel();
        long start = level.getDayTime();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.INABA_JACKALOPE, new BlockPos(4, 2, 4));
        girl.setNoAi(true);
        girl.addAffection(p, HomeLife.FRIEND);
        int before = girl.getAffection(p);
        check(Visits.visit(level, girl, p) == 1 && Visits.visit(level, girl, p) == 0, "once a day");
        for (int d = 2; d <= Visits.WEEK; d++) {
            level.setDayTime(level.getDayTime() + 24000);
            check(Visits.visit(level, girl, p) == d, "day " + d + " in a row");
        }
        check(girl.getAffection(p) - before == Visits.WEEK + Visits.WEEKLY, "a week of visits: " + (girl.getAffection(p) - before));
        level.setDayTime(level.getDayTime() + 48000);
        check(Visits.visit(level, girl, p) == 1, "a day missed starts over");
        level.setDayTime(start);
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aMermaidSwimsAlongside")
    public static void aMermaidSwimsAlongside(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 2; x <= 6; x++) for (int z = 2; z <= 6; z++) for (int y = 1; y <= 3; y++) helper.setBlock(new BlockPos(x, y, z), Blocks.WATER);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        AbstractMonsterGirl koi = spawn(helper, GirlSpecies.KOI_MERMAID, new BlockPos(5, 2, 5));
        koi.setNoAi(true);
        helper.runAfterDelay(2, () -> {
            check(Company.swim(level, p) == null, "not with a stranger");
            koi.addAffection(p, HomeLife.FRIEND);
            check(Company.swim(level, p) == koi, "she swims alongside");
            check(p.hasEffect(MobEffects.DOLPHINS_GRACE) && p.hasEffect(MobEffects.WATER_BREATHING), "easy in the water");
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "theLamplighter")
    public static void theLamplighter(GameTestHelper helper) {
        var level = helper.getLevel();
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.INABA_JACKALOPE, new BlockPos(4, 2, 4));
        girl.setNoAi(true);
        BlockPos anchor = helper.absolutePos(new BlockPos(4, 2, 4));
        BlockPos spot = helper.absolutePos(new BlockPos(6, 2, 6));
        check(!Chores.lightUp(level, girl, anchor, spot), "no torches to hand");
        BlockPos chest = new BlockPos(2, 2, 2);
        helper.setBlock(chest, Blocks.CHEST);
        ((Container) helper.getBlockEntity(chest)).setItem(0, new ItemStack(Items.TORCH, 2));
        check(Chores.dark(level, spot), "no lamp near: dark enough to spawn in");
        check(Chores.lightUp(level, girl, anchor, spot) && level.getBlockState(spot).is(Blocks.TORCH), "a torch in the dark");
        check(((Container) helper.getBlockEntity(chest)).getItem(0).getCount() == 1, "taken from the chest");
        check(Chores.tally(girl, Chores.Kind.TORCH) == 1, "on her tally");
        helper.succeed();
    }
}
