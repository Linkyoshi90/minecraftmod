package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModItems;
import com.linkyoshi90.monstergirls.world.OasisStructure;
import com.linkyoshi90.monstergirls.world.OneChunkStructure;
import com.linkyoshi90.monstergirls.world.SilkDenStructure;
import com.linkyoshi90.monstergirls.world.SkyIslandStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;

/** The oasis, the sky island and the silk den (world/OneChunkStructure). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class WildPlaceTests {
    private static AbstractMonsterGirl resident(ServerLevel level, BlockPos near, GirlSpecies kind) {
        return level.getEntitiesOfClass(AbstractMonsterGirl.class, new AABB(near).inflate(10), g -> g.getSpecies() == kind).stream().findFirst().orElse(null);
    }

    private static void clear(ServerLevel level, BlockPos origin, int down, int up) {
        level.getEntitiesOfClass(Mob.class, new AABB(origin).inflate(12, 20, 12)).forEach(Mob::discard);
        for (BlockPos q : BlockPos.betweenClosed(origin.offset(-8, -down, -8), origin.offset(8, up, 8))) level.setBlock(q, Blocks.AIR.defaultBlockState(), 2 | 16);
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "everyPlaceHasABuilder")
    public static void everyPlaceHasABuilder(GameTestHelper helper) {
        check(OneChunkStructure.BUILDERS.keySet().containsAll(java.util.List.of("oasis", "sky_island", "silk_den")), "all three can be built from their pieces");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "anOasisAndItsSphinx")
    public static void anOasisAndItsSphinx(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(7, 4, 7));
        BlockPos chest = OasisStructure.build(level, origin, level.random, true);
        check(level.getBlockState(origin).is(Blocks.WATER) && level.getBlockState(chest).is(Blocks.CHEST), "a pool and a chest");
        AbstractMonsterGirl sphinx = resident(level, origin, GirlSpecies.SPHINX_CENTAUR);
        check(sphinx != null && sphinx.hasRestriction(), "and the sphinx who keeps it");
        clear(level, origin, 4, 12);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aSkyIslandAndItsSylph")
    public static void aSkyIslandAndItsSylph(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(7, 14, 7));
        BlockPos chest = SkyIslandStructure.build(level, origin, level.random, true);
        check(level.getBlockState(origin).is(Blocks.GRASS_BLOCK) && level.getBlockState(chest).is(Blocks.CHEST), "grass and a chest up in the air");
        check(level.getBlockState(origin.below(SkyIslandStructure.DEPTH - 1)).isSolid(), "a stone cone beneath");
        AbstractMonsterGirl sylph = resident(level, origin, GirlSpecies.SYLPH_ELF);
        check(sylph != null && sylph.hasRestriction(), "and a sylph");
        clear(level, origin, 20, 10);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aSilkDenAndItsCocoons")
    public static void aSilkDenAndItsCocoons(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(7, 4, 7));
        BlockPos chest = SilkDenStructure.build(level, origin, level.random, true);
        int cocoons = 0;
        if (level.getBlockEntity(chest) instanceof ChestBlockEntity box) {
            for (int i = 0; i < box.getContainerSize(); i++) if (box.getItem(i).is(ModItems.SILK_COCOON.get())) cocoons++;
        }
        check(cocoons == SilkDenStructure.COCOONS, "a chest of cocoons: " + cocoons);
        AbstractMonsterGirl weaver = resident(level, origin, GirlSpecies.JOROGUMO_ARACHNE);
        check(weaver != null && weaver.hasRestriction(), "and the jorogumo who weaves them");
        clear(level, origin, 4, 10);
        helper.succeed();
    }
}
