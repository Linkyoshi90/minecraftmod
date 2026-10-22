package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.Devil;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.interact;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The infernal family (devil_field_guide.html). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class DevilTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "lesserDevilPranksAndGivesBack")
    public static void lesserDevilPranksAndGivesBack(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 7));
        Devil imp = spawn(helper, GirlSpecies.LESSER_DEVIL, new BlockPos(6, 2, 7));
        for (int i = 0; i < 9; i++) player.getInventory().setItem(i, new ItemStack(Items.APPLE));
        for (int i = 0; i < 30 && imp.getLoot().isEmpty(); i++) imp.prank(player);
        check(!imp.getLoot().isEmpty(), "sooner or later she steals something");
        player.getInventory().selected = 0;
        player.getInventory().setItem(0, ItemStack.EMPTY);                     // the cookie goes in this slot (count after clearing it)
        int apples = player.getInventory().countItem(Items.APPLE);
        interact(imp, player, new ItemStack(Items.COOKIE));
        check(imp.getLoot().isEmpty() && player.getInventory().countItem(Items.APPLE) == apples + 1, "a sweet and she gives it back");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "succubusNeedsNoFriendshipAndJiangShiTalisman")
    public static void succubusNeedsNoFriendshipAndJiangShiTalisman(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 7));
        Devil succubus = spawn(helper, GirlSpecies.SUCCUBUS_DEVIL, new BlockPos(7, 2, 7));
        check(succubus.trustsEnoughForBed(player) && succubus.bedChanceFor(player) >= 0.5F, "she climbs into strangers' beds");
        Devil other = spawn(helper, GirlSpecies.GREATER_DEVIL, new BlockPos(10, 2, 10));
        check(!other.trustsEnoughForBed(player), "other devils wait for friendship");
        check(other.canFly() && other.fireImmune(), "the greater devil flies and fears no fire");
        Devil jiang = spawn(helper, GirlSpecies.JIANG_SHI_DEVIL, new BlockPos(3, 2, 3));
        interact(jiang, player, new ItemStack(Items.PAPER));
        check(jiang.isCalm(), "a paper talisman calms the jiang shi");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "jackOLanternHidesAsAPumpkin")
    public static void jackOLanternHidesAsAPumpkin(GameTestHelper helper) {
        Devil jack = spawn(helper, GirlSpecies.JACK_O_LANTERN_DEVIL, new BlockPos(7, 2, 7));
        jack.setOrderedToSit(true);
        helper.succeedWhen(() -> check(jack.isDisguised(), "still for a while, she becomes a pumpkin"));
    }
}
