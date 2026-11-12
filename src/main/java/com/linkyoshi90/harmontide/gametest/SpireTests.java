package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.linkyoshi90.harmontide.world.DemonSpireStructure;
import com.linkyoshi90.harmontide.world.RealmManorStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;

/** The Demon's spire (world/DemonSpireStructure). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class SpireTests {
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "theDemonsSpire")
    public static void theDemonsSpire(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(7, 1, 7));
        BlockPos throne = DemonSpireStructure.build(level, origin, level.random, true);
        int x = origin.getX(), y = origin.getY(), z = origin.getZ(), half = DemonSpireStructure.HALF, storey = DemonSpireStructure.STOREY;
        check(throne.getY() == y + DemonSpireStructure.ROOF + 1 && level.getBlockState(throne).is(ModBlocks.REALM_STAIRS.get())
                && level.getBlockState(throne.south()).is(Blocks.CRYING_OBSIDIAN), "a throne on the roof before crying obsidian");
        check(level.canSeeSky(throne.above()), "under the open sky");
        check(level.getBlockState(new BlockPos(x, y + 1, z - half)).is(ModBlocks.REALM_DOOR.get()), "a door");
        for (int dy = 1; dy <= DemonSpireStructure.ROOF; dy++) {
            check(level.getBlockState(new BlockPos(x + DemonSpireStructure.LADDER_X, y + dy, z + half - 1)).is(Blocks.LADDER), "a ladder all the way up, at " + dy);
        }
        check(level.getBlockState(new BlockPos(x, y + storey + 1, z)).is(Blocks.ENCHANTING_TABLE), "a library");
        check(level.getBlockState(new BlockPos(x + 3, y + storey + 1, z - 3)).is(Blocks.CHEST), "with a chest of books");
        check(level.getBlockState(new BlockPos(x - 5, y + storey * 2 + 1, z - 3)).is(ModBlocks.REALM_CENSER.get()), "a bedchamber with censers");
        check(level.getBlockState(new BlockPos(x, y + storey * 3 + 1, z)).is(Blocks.CHEST), "a treasury");
        List<AbstractMonsterGirl> house = level.getEntitiesOfClass(AbstractMonsterGirl.class, new AABB(origin).inflate(10, 40, 10));
        check(house.stream().filter(g -> g.getSpecies() == GirlSpecies.SUCCUBUS_DEVIL).count() == 2, "two succubi on guard");
        AbstractMonsterGirl demon = house.stream().filter(g -> g.getSpecies() == GirlSpecies.DEMON_DEVIL).findFirst().orElse(null);
        check(demon != null && demon.getY() > y + DemonSpireStructure.ROOF && demon.hasRestriction() && !demon.isBaby(), "the Demon on her roof");
        demon.clearRestriction();
        RealmManorStructure.keepHome(demon);
        check(demon.hasRestriction(), "she keeps to her spire");
        house.forEach(AbstractMonsterGirl::discard);
        for (BlockPos q : BlockPos.betweenClosed(origin.offset(-half, 1, -half), origin.offset(half, DemonSpireStructure.ROOF + 3, half))) {
            level.setBlock(q, Blocks.AIR.defaultBlockState(), 2 | 16);       // take the tower down again: it would shade other tests
        }
        helper.succeed();
    }
}
