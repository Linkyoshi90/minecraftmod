package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.HearthBlockEntity;
import com.linkyoshi90.harmontide.block.MarketStallBlockEntity;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Hamlet;
import com.linkyoshi90.harmontide.entity.girl.Roles;
import com.linkyoshi90.harmontide.entity.girl.Settlement;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Hamlet trades (block/MarketStallBlockEntity): a village's market stall. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class StallTests {
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "theVillageMarketStall")
    public static void theVillageMarketStall(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(10, 1, 10));
        BlockPos hp = new BlockPos(7, 1, 7);
        helper.setBlock(hp, ModBlocks.HEARTH.get());
        HearthBlockEntity h = (HearthBlockEntity) helper.getBlockEntity(hp);
        h.setOwner(p.getUUID());
        helper.setBlock(new BlockPos(3, 1, 7), ModBlocks.MARKET_STALL.get());
        MarketStallBlockEntity stall = (MarketStallBlockEntity) helper.getBlockEntity(new BlockPos(3, 1, 7));
        BlockPos sp = helper.absolutePos(new BlockPos(3, 1, 7));
        AbstractMonsterGirl farmhand = null;
        for (int i = 0; i < Hamlet.VILLAGE; i++) {
            AbstractMonsterGirl g = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(1 + i * 2, 1, 12));
            g.addAffection(p, Orders.AFFECTION);
            Settlement.settle(level, g, h);
            if (i == 0) farmhand = g;
        }
        Roles.set(farmhand, Roles.Role.FARMHAND);
        ItemStack crop = MarketStallBlockEntity.goods(farmhand, p);
        check(crop.is(Items.WHEAT) || crop.is(Items.CARROT) || crop.is(Items.POTATO), "a farmhand lays out her crops: " + crop);
        helper.runAfterDelay(2, () -> {                                   // a hearth counts itself loaded a tick later
            check(MarketStallBlockEntity.village(level, sp) == h, "the stall serves the village");
            stall.store().setItem(0, new ItemStack(Items.WHEAT, 8));
            stall.store().setItem(1, new ItemStack(Items.BONE, 4));
            int earned = stall.morning(level, sp);
            check(earned == 2 && stall.store().countItem(Items.EMERALD) == 2, "half the goods sold: " + earned);
            check(stall.store().countItem(Items.BONE) >= 4, "the rest still on the stall");
            check(!stall.store().isEmpty() && stall.store().countItem(Items.WHEAT) + stall.store().countItem(Items.CARROT) + stall.store().countItem(Items.POTATO) >= 6,
                    "and the residents laid out more");
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }
}
