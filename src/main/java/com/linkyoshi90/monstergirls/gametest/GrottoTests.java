package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Colonies;
import com.linkyoshi90.monstergirls.entity.HomeLife;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.world.MerfolkGrottoStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;

/** The merfolk grotto (world/MerfolkGrottoStructure). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class GrottoTests {
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aGrottoAndItsQueen")
    public static void aGrottoAndItsQueen(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(7, 1, 7));
        BlockPos chest = MerfolkGrottoStructure.build(level, origin, level.random, true);
        check(level.getBlockState(chest).is(Blocks.CHEST) && level.getBlockState(chest).getValue(BlockStateProperties.WATERLOGGED), "a sunken chest");
        check(level.getBlockState(origin.above(MerfolkGrottoStructure.RADIUS + 1)).is(Blocks.PRISMARINE) || level.getBlockState(origin.above(MerfolkGrottoStructure.RADIUS + 1)).is(Blocks.DARK_PRISMARINE),
                "under a prismarine dome");
        check(level.getBlockState(origin.above(3)).is(Blocks.WATER), "full of water");
        List<AbstractMonsterGirl> court = level.getEntitiesOfClass(AbstractMonsterGirl.class, new AABB(origin).inflate(8), g -> Colonies.colonyOf(g) != null);
        AbstractMonsterGirl queen = court.stream().filter(Colonies::matriarch).findFirst().orElse(null);
        check(queen != null && MerfolkGrottoStructure.QUEENS.contains(queen.getSpecies()) && queen.hasCustomName(), "a named mermaid queen");
        check(court.size() == 4, "with her court of three: " + court.size());
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 1, 1));
        queen.addAffection(p, HomeLife.FRIEND);
        check(Colonies.welcome(level, queen, p) == 3 && court.stream().allMatch(g -> g.getAffection(p) >= HomeLife.FRIEND), "her court takes her friend in");
        HarpyTestUtil.remove(p);
        court.forEach(AbstractMonsterGirl::discard);
        for (BlockPos q : BlockPos.betweenClosed(origin.offset(-7, -1, -7), origin.offset(7, 9, 7))) level.setBlock(q, Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.succeed();
    }
}
