package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.TarShroomBlock;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Foraging;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Foraging errands (entity.girl.Foraging) and tar-shrooms spreading. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class ForagingTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aForagerGathersBerries")
    public static void aForagerGathersBerries(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        p.getInventory().clearContent();
        p.setShiftKeyDown(true);
        var level = helper.getLevel();
        AbstractMonsterGirl cat = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(8, 2, 8));
        cat.setNoAi(true);
        cat.addAffection(p, Orders.AFFECTION);
        ItemStack berries = new ItemStack(Items.SWEET_BERRIES, 3);
        check(Foraging.interact(cat, p, berries) == null, "a cat doesn't forage");
        AbstractMonsterGirl rabbit = spawn(helper, GirlSpecies.INABA_JACKALOPE, new BlockPos(4, 2, 4));
        rabbit.setNoAi(true);
        check(Foraging.interact(rabbit, p, berries) == null, "not for a stranger");
        rabbit.addAffection(p, Orders.AFFECTION);
        check(Foraging.interact(rabbit, p, new ItemStack(Items.DIAMOND)) == null, "diamonds don't grow wild");
        BlockPos chest = new BlockPos(5, 2, 4);
        helper.setBlock(chest, Blocks.CHEST);
        check(Foraging.interact(rabbit, p, berries) != null && rabbit.isRemoved() && Foraging.away(p) == 1 && berries.getCount() == 2, "she goes");
        check(Foraging.returnDue(p, true).size() == 1 && Foraging.away(p) == 0, "she comes back");
        Container box = (Container) helper.getBlockEntity(chest);
        int n = 0;
        for (int i = 0; i < box.getContainerSize(); i++) if (box.getItem(i).is(Items.SWEET_BERRIES)) n += box.getItem(i).getCount();
        check(n == Foraging.FORAGED + 1, "sixteen and the sample, in the chest: " + n);
        check(Foraging.wild(new ItemStack(ModItems.TAR_SHROOM.get())) > 0, "tar-shrooms can be foraged");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "tarShroomsSpread")
    public static void tarShroomsSpread(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 1; x <= 7; x++) for (int z = 1; z <= 7; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.MUD);
        BlockPos at = new BlockPos(4, 2, 4);
        helper.setBlock(at, ModBlocks.TAR_SHROOM.get());
        var state = helper.getBlockState(at);
        BlockPos grew = null;
        for (int i = 0; i < 200 && grew == null; i++) grew = TarShroomBlock.spread(state, level, helper.absolutePos(at), level.random);
        check(grew != null && level.getBlockState(grew).is(ModBlocks.TAR_SHROOM.get()), "it spreads on mud");
        for (int i = 0; i < 400; i++) TarShroomBlock.spread(state, level, helper.absolutePos(at), level.random);
        int n = 0;
        for (BlockPos q : BlockPos.betweenClosed(helper.absolutePos(new BlockPos(0, 1, 0)), helper.absolutePos(new BlockPos(8, 3, 8)))) {
            if (level.getBlockState(q).is(ModBlocks.TAR_SHROOM.get())) n++;
        }
        check(n <= 6, "but not crowded: " + n);
        helper.succeed();
    }
}
