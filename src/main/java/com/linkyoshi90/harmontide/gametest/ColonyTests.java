package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.entity.Colonies;
import com.linkyoshi90.harmontide.entity.Flocks;
import com.linkyoshi90.harmontide.entity.HomeLife;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.linkyoshi90.harmontide.registry.ModEntities;
import com.linkyoshi90.harmontide.world.HarpyAerieStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;

/** Harpy sky colonies (world/HarpyAerieStructure, entity/Colonies, block/HarpyPerchBlock). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class ColonyTests {
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "anAerieAndItsMatriarch")
    public static void anAerieAndItsMatriarch(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(7, 1, 7));
        BlockPos nest = HarpyAerieStructure.build(level, origin, level.random, true);
        check(nest.getY() > origin.getY() + 6 && level.getBlockState(nest.below()).is(Blocks.HAY_BLOCK), "the matriarch's nest, up on the tallest crag");
        check(level.getBlockState(nest.east()).is(ModBlocks.HARPY_EGG.get()), "with her clutch");
        List<AbstractHarpy> colony = level.getEntitiesOfClass(AbstractHarpy.class, new AABB(origin).inflate(8, 16, 8), h -> Colonies.colonyOf(h) != null);
        AbstractHarpy matriarch = colony.stream().filter(Colonies::matriarch).findFirst().orElse(null);
        check(matriarch != null && nest.equals(matriarch.getHomePos()) && matriarch.hasCustomName() && !matriarch.isBaby(), "a named matriarch who nests there");
        check(colony.size() >= 3 && colony.stream().allMatch(h -> h.getType() == matriarch.getType()), "and two or three more of her kind: " + colony.size());
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 1, 1));
        matriarch.addAffection(p, HomeLife.FRIEND);
        int took = Colonies.welcome(level, matriarch, p);
        check(took == colony.size() - 1 && colony.stream().allMatch(h -> h.getAffection(p) >= HomeLife.FRIEND), "the colony welcomes her friend");
        check(Colonies.welcome(level, matriarch, p) == 0, "once");
        HarpyTestUtil.remove(p);
        colony.forEach(Mob::discard);
        for (BlockPos q : BlockPos.betweenClosed(origin.offset(-7, -1, -7), origin.offset(7, 14, 7))) level.setBlock(q, Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aPerchKeepsAFlock")
    public static void aPerchKeepsAFlock(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.setBlock(new BlockPos(7, 1, 7), ModBlocks.HARPY_PERCH.get());
        BlockPos perch = helper.absolutePos(new BlockPos(7, 1, 7));
        check(perch.equals(Colonies.perchNear(level, helper.absolutePos(new BlockPos(2, 1, 2)))), "the perch is found");
        List<Mob> flock = Flocks.land(level, perch, ModEntities.SPARROW_HARPY.get(), 3, 2, 4);
        check(!flock.isEmpty() && flock.stream().allMatch(Flocks::resting), "a flock comes down");
        Colonies.settleAtPerch(flock, perch);
        check(flock.stream().noneMatch(Flocks::resting), "and stays");
        check(Colonies.matriarch(flock.get(0)) && perch.above().equals(((AbstractHarpy) flock.get(0)).getHomePos()), "roosting at the perch, the first leading");
        flock.forEach(Mob::discard);
        helper.succeed();
    }
}
