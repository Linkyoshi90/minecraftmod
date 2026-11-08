package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.GeyseriteBlock;
import com.linkyoshi90.monstergirls.entity.HomeLife;
import com.linkyoshi90.monstergirls.entity.HotSprings;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Colds;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.world.HotSpringFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Hot springs (entity/HotSprings, block/GeyseriteBlock, world/HotSpringFeature). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class HotSpringTests {
    /** A little onsen: geyserite floor (the platform's own level, y=0) under one block of water, 3 by 3, walled in stone. */
    private static void onsen(GameTestHelper helper) {
        for (int dx = 2; dx <= 6; dx++) {
            for (int dz = 2; dz <= 6; dz++) {
                boolean wall = dx == 2 || dx == 6 || dz == 2 || dz == 6;
                helper.setBlock(new BlockPos(dx, 0, dz), wall ? Blocks.STONE : ModBlocks.GEYSERITE.get());
                helper.setBlock(new BlockPos(dx, 1, dz), wall ? Blocks.STONE : Blocks.WATER);
            }
        }
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "aSoakWithFriends")
    public static void aSoakWithFriends(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        onsen(helper);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 1, 4));
        AbstractMonsterGirl friend = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(5, 1, 5));
        friend.addAffection(p, HomeLife.FRIEND);
        friend.getPersistentData().putLong(Colds.COLD, level.getGameTime() + 10000);
        helper.runAfterDelay(5, () -> {
            check(HotSprings.inSpring(p), "the player is in hot spring water");
            p.setTicksFrozen(100);
            HotSprings.soak(level, p);
            check(p.hasEffect(MobEffects.REGENERATION) && p.getTicksFrozen() == 0, "healed and warmed");
            check(HotSprings.inSpring(friend) && friend.hasEffect(MobEffects.REGENERATION) && !Colds.hasCold(friend), "the friend soaks too, and her cold is gone");
            p.getPersistentData().putInt("SpringSoaked", HotSprings.WELL_SOAKED);
            HotSprings.soak(level, p);
            check(p.hasEffect(MobEffects.HEALTH_BOOST), "a long soak: well soaked");
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "theSpringNeverFreezes")
    public static void theSpringNeverFreezes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        onsen(helper);
        helper.setBlock(new BlockPos(4, 2, 4), Blocks.ICE);              // ice over the water
        BlockPos floor = helper.absolutePos(new BlockPos(4, 0, 4));
        check(GeyseriteBlock.thaw(level, floor) == 1 && !level.getBlockState(floor.above(2)).is(Blocks.ICE), "the ice melts");
        check(GeyseriteBlock.surface(level, floor) != null, "and it has a surface to steam from");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aSpringForms")
    public static void aSpringForms(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos surface = helper.absolutePos(new BlockPos(7, 0, 7));
        int water = HotSpringFeature.place(level, surface, level.random, null);
        check(water > 0, "a pool on level ground; heights " + level.getHeight(Heightmap.Types.WORLD_SURFACE, surface.getX(), surface.getZ()) + "/" + level.getHeight(Heightmap.Types.WORLD_SURFACE, surface.getX() + 5, surface.getZ() + 5) + " at y " + surface.getY());
        check(level.getBlockState(surface).is(Blocks.WATER) && level.getBlockState(surface.below(HotSpringFeature.DEPTH)).is(ModBlocks.GEYSERITE.get()), "water over geyserite");
        helper.succeed();
    }
}
