package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Carcass;
import com.linkyoshi90.harmontide.entity.Decomposers;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Decomposers (entity/Decomposers). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class DecomposerTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "slimesBreakItDown")
    public static void slimesBreakItDown(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl slime = spawn(helper, GirlSpecies.VERDANT_SLIME, new BlockPos(4, 2, 4));
        slime.setNoAi(true);
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(12, 2, 12));
        wolf.setNoAi(true);
        check(Decomposers.decomposer(slime) && !Decomposers.decomposer(wolf), "slimes break things down, wolves don't");
        helper.setBlock(new BlockPos(6, 2, 4), Blocks.COMPOSTER);
        helper.spawnItem(Items.ROTTEN_FLESH, new BlockPos(5, 2, 4));
        helper.spawnItem(Items.OAK_LEAVES, new BlockPos(4, 2, 5));
        helper.spawnItem(Items.STONE, new BlockPos(4, 2, 3));
        check(Decomposers.pulse(level, slime) == 2, "the scraps are taken in");
        check(level.getEntitiesOfClass(ItemEntity.class, slime.getBoundingBox().inflate(4), i -> i.getItem().is(Items.STONE)).size() == 1, "but not a stone");
        var composter = level.getBlockState(helper.absolutePos(new BlockPos(6, 2, 4)));
        check(composter.getValue(ComposterBlock.LEVEL) == 1, "the composter beside her fills a layer");
        var cow = helper.spawn(EntityType.COW, new BlockPos(3, 2, 6));
        Carcass c = Carcass.of(level, cow);
        cow.discard();
        c.setMeat(0);
        Decomposers.pulse(level, slime);
        check(!c.isAlive(), "the last bones are broken down");
        check(!level.getEntitiesOfClass(ItemEntity.class, slime.getBoundingBox().inflate(5), i -> i.getItem().is(Items.BONE_MEAL)).isEmpty(), "into bone meal");
        helper.setBlock(new BlockPos(9, 1, 9), Blocks.COARSE_DIRT);
        BlockPos changed = Decomposers.enrich(level, helper.absolutePos(new BlockPos(9, 2, 9)), level.getRandom());
        check(changed != null && level.getBlockState(changed).is(Blocks.DIRT), "the coarse dirt loosens");
        helper.succeed();
    }
}
