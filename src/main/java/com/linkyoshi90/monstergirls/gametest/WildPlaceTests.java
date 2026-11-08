package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModItems;
import com.linkyoshi90.monstergirls.world.OasisStructure;
import com.linkyoshi90.monstergirls.world.DragonLairStructure;
import com.linkyoshi90.monstergirls.world.CentaurCampStructure;
import com.linkyoshi90.monstergirls.world.GraveyardStructure;
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
        check(OneChunkStructure.BUILDERS.keySet().containsAll(java.util.List.of("oasis", "sky_island", "silk_den", "dragon_lair", "centaur_camp", "graveyard")), "every place can be built from its piece");
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

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aDragonOnHerHoard")
    public static void aDragonOnHerHoard(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(7, 4, 7));
        BlockPos chest = DragonLairStructure.build(level, origin, level.random, true);
        check(level.getBlockState(chest).is(Blocks.CHEST) && level.getBlockState(origin.above(2)).isAir() && level.getBlockState(origin.above(5)).isSolid(),
                "a hollow mound with a chest inside");
        check(level.getBlockState(origin.offset(0, 1, -DragonLairStructure.OUTER)).isAir(), "and a way in");
        AbstractMonsterGirl dragon = level.getEntitiesOfClass(AbstractMonsterGirl.class, new AABB(origin).inflate(8),
                g -> g.getSpecies() == GirlSpecies.WYVERN_DRAGON || g.getSpecies() == GirlSpecies.WYRM_DRAGON).stream().findFirst().orElse(null);
        check(dragon != null && dragon.hasRestriction(), "and a dragon on her hoard");
        clear(level, origin, 4, 10);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aCentaurBandsCamp")
    public static void aCentaurBandsCamp(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(7, 4, 7));
        BlockPos chest = CentaurCampStructure.build(level, origin, level.random, true);
        check(level.getBlockState(chest).is(Blocks.CHEST) && level.getBlockState(origin.above()).is(Blocks.CAMPFIRE), "a campfire and supplies");
        long band = level.getEntitiesOfClass(AbstractMonsterGirl.class, new AABB(origin).inflate(8), g -> g.getSpecies().family() == GirlSpecies.Family.CENTAUR).size();
        check(band == CentaurCampStructure.BAND, "a band of three: " + band);
        clear(level, origin, 4, 8);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "anOldGraveyard")
    public static void anOldGraveyard(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(7, 4, 7));
        BlockPos chest = GraveyardStructure.build(level, origin, level.random, true);
        check(level.getBlockState(chest).is(Blocks.CHEST), "a chest in the crypt");
        check(level.getBlockState(origin.offset(-GraveyardStructure.HALF, 1, 0)).is(Blocks.DARK_OAK_FENCE) && level.getBlockState(origin.offset(0, 1, -GraveyardStructure.HALF)).isAir(),
                "fenced, with a gate");
        check(resident(level, origin, GirlSpecies.YUREI_UNDEAD) != null && resident(level, origin, GirlSpecies.WILL_O_WISP_UNDEAD) != null, "a yurei and a will-o-wisp");
        clear(level, origin, 4, 8);
        helper.succeed();
    }
}
