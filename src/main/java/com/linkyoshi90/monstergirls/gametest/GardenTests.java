package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Comfort;
import com.linkyoshi90.monstergirls.entity.HomeLife;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.TreeFrog;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.GreenThumb;
import com.linkyoshi90.monstergirls.registry.ModAttachments;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The green thumb (entity.girl.GreenThumb) and the frogs' rain call (entity.Comfort). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class GardenTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aGreenThumb")
    public static void aGreenThumb(GameTestHelper helper) {
        var level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        for (int x = 2; x <= 8; x++) for (int z = 2; z <= 8; z++) {
            helper.setBlock(new BlockPos(x, 1, z), Blocks.FARMLAND);
            helper.setBlock(new BlockPos(x, 2, z), Blocks.WHEAT);
        }
        AbstractMonsterGirl moth = spawn(helper, GirlSpecies.MOTH_BUG, new BlockPos(5, 3, 5));
        AbstractMonsterGirl rabbit = spawn(helper, GirlSpecies.INABA_JACKALOPE, new BlockPos(6, 3, 6));
        moth.setNoAi(true);
        rabbit.setNoAi(true);
        moth.addAffection(p, Orders.AFFECTION);
        rabbit.addAffection(p, Orders.AFFECTION);
        check(GreenThumb.tend(level, moth) == null, "only on patrol");
        moth.getOrders().set(Orders.Command.PATROL, p, moth.blockPosition());
        rabbit.getOrders().set(Orders.Command.PATROL, p, rabbit.blockPosition());
        check(GreenThumb.tend(level, rabbit) == null, "a rabbit is no gardener");
        BlockPos grew;
        grew = GreenThumb.tend(level, moth);
        check(grew != null && ((CropBlock) Blocks.WHEAT).getAge(level.getBlockState(grew)) == 1, "the wheat grows a stage");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aFrogCallsTheRain")
    public static void aFrogCallsTheRain(GameTestHelper helper) {
        var level = helper.getLevel();
        level.setWeatherParameters(24000, 0, false, false);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        TreeFrog frog = helper.spawn(ModEntities.TREE_FROG.get(), new BlockPos(4, 2, 4));
        frog.setNoAi(true);
        frog.addAffection(p, HomeLife.FRIEND);
        check(Comfort.rainCall(level, p) == null, "the dry days start counting");
        var data = p.getData(ModAttachments.JOURNAL);
        data.putLong(Comfort.DRY, level.getGameTime() - Comfort.DRY_SPELL - 1);
        p.setData(ModAttachments.JOURNAL, data);
        if (level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_WEATHER_CYCLE)) {
            check(Comfort.rainCall(level, p) == frog && level.getLevelData().isRaining(), "after a dry spell she croaks the rain in");
        }
        level.setWeatherParameters(6000, 0, false, false);
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
