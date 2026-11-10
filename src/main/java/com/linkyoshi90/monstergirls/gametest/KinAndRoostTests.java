package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.SeagullHarpy;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Kinship;
import com.linkyoshi90.monstergirls.entity.girl.Undead;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The two 2026-11-10 decisions: a friend's zombies leave villagers alone; a fletching table speeds up a roost arrow farm. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class KinAndRoostTests {
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "befriendedZombiesSpareVillagers")
    public static void befriendedZombiesSpareVillagers(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        Undead girl = spawn(helper, GirlSpecies.ZOMBIE_UNDEAD, new BlockPos(3, 2, 3));
        girl.setNoAi(true);
        Villager villager = helper.spawn(EntityType.VILLAGER, new BlockPos(11, 2, 11));
        villager.setNoAi(true);
        Zombie wild = helper.spawn(EntityType.ZOMBIE, new BlockPos(9, 2, 11));
        Zombie friend = helper.spawn(EntityType.ZOMBIE, new BlockPos(11, 2, 9));
        Kinship.befriend(helper.getLevel(), friend, player, girl);
        wild.setTarget(villager);
        friend.setTarget(villager);
        check(wild.getTarget() == villager, "a wild zombie still goes for the villager");
        check(friend.getTarget() == null, "a friend's zombie leaves the villager alone");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "fletchingTableSpeedsTheArrowFarm")
    public static void fletchingTableSpeedsTheArrowFarm(GameTestHelper helper) {
        BlockPos hopper = new BlockPos(7, 2, 7);
        helper.setBlock(hopper, Blocks.HOPPER);
        SeagullHarpy gull = helper.spawn(ModEntities.SEAGULL_HARPY.get(), new BlockPos(4, 2, 4));
        gull.setNoAi(true);
        gull.setRoost(helper.absolutePos(hopper));
        int plainMax = 0;
        for (int i = 0; i < 40; i++) plainMax = Math.max(plainMax, gull.nextRoostDrop(helper.getLevel()));
        check(plainMax >= 2 * AbstractHarpy.ROOST_DROP_INTERVAL * 2 / 3, "without a table, the usual five to ten minutes: " + plainMax);
        helper.setBlock(new BlockPos(9, 2, 7), Blocks.FLETCHING_TABLE);
        check(AbstractHarpy.fletchingNear(helper.getLevel(), helper.absolutePos(hopper)), "the table is near the hopper");
        for (int i = 0; i < 40; i++) {
            int t = gull.nextRoostDrop(helper.getLevel());
            check(t < 2 * AbstractHarpy.ROOST_DROP_INTERVAL * 2 / 3 && t >= AbstractHarpy.ROOST_DROP_INTERVAL * 2 / 3, "with it, two thirds of that: " + t);
        }
        helper.succeed();
    }
}
