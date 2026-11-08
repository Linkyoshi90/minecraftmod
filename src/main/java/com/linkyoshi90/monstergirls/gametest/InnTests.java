package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Inn;
import com.linkyoshi90.monstergirls.world.WaysideInnStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;

/** The wayside inn (world/WaysideInnStructure, entity/girl/Inn). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class InnTests {
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "theWaysideInn")
    public static void theWaysideInn(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(7, 1, 7));
        BlockPos stand = WaysideInnStructure.build(level, origin, level.random, true);
        check(level.getBlockState(origin.offset(1, 1, 1)).is(Blocks.BARREL) && level.getBlockState(origin.offset(-2, 1, WaysideInnStructure.HALF_L - 1)).is(Blocks.CAMPFIRE), "a counter and a fire");
        AbstractMonsterGirl keeper = level.getEntitiesOfClass(AbstractMonsterGirl.class, new AABB(stand).inflate(2), Inn::innkeeper).stream().findFirst().orElse(null);
        check(keeper != null && keeper.getSpecies() == GirlSpecies.HOLSTAUR_MINOTAUR && keeper.hasRestriction(), "a holstaur keeps it");
        BlockPos inn = Inn.innOf(keeper);
        List<AbstractMonsterGirl> came = Inn.morning(level, inn);
        check(!came.isEmpty() && came.size() <= Inn.GUESTS && came.stream().allMatch(Inn::guest), "travellers stop over: " + came.size());
        check(Inn.morning(level, inn).size() + came.size() <= Inn.GUESTS, "no more than the inn holds");
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 1, 3));
        came.get(0).addAffection(p, 1);
        check(!Inn.moveOn(level, came.get(0), true) && !Inn.guest(came.get(0)), "a traveller who made a friend stays");
        if (came.size() > 1) check(Inn.moveOn(level, came.get(1), true) && came.get(1).isRemoved(), "the other goes on her way");
        ItemStack emeralds = new ItemStack(Items.EMERALD, 2);
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, emeralds);
        Inn.meal(level, keeper, p, emeralds, false);
        check(emeralds.getCount() == 1 && p.getInventory().countItem(Items.RABBIT_STEW) == 1, "an emerald buys a hot meal");
        HarpyTestUtil.remove(p);
        level.getEntitiesOfClass(Mob.class, new AABB(origin).inflate(12)).forEach(Mob::discard);
        for (BlockPos q : BlockPos.betweenClosed(origin.offset(-7, 0, -7), origin.offset(7, 9, 7))) level.setBlock(q, Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.succeed();
    }
}
