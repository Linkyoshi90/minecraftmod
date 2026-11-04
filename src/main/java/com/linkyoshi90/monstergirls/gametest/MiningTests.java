package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Mining;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Mining trips (entity.girl.Mining). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class MiningTests {
    private static int count(Iterable<ItemStack> stacks, Item item) {
        int n = 0;
        for (ItemStack s : stacks) if (s.is(item)) n += s.getCount();
        return n;
    }

    private static List<ItemStack> contents(Container box) {
        List<ItemStack> out = new java.util.ArrayList<>();
        for (int i = 0; i < box.getContainerSize(); i++) out.add(box.getItem(i));
        return out;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aMinerDigsForDiamonds")
    public static void aMinerDigsForDiamonds(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        p.getInventory().clearContent();
        var level = helper.getLevel();
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(9, 2, 9));
        wolf.addAffection(p, Orders.AFFECTION);
        check(!Mining.takePickaxe(wolf, p, new ItemStack(Items.IRON_PICKAXE)), "a wolf doesn't dig");
        BlockPos chest = new BlockPos(6, 2, 5);
        helper.setBlock(chest, Blocks.CHEST);
        AbstractMonsterGirl dwarf = spawn(helper, GirlSpecies.DWARF_ELF, new BlockPos(5, 2, 5));
        dwarf.addAffection(p, Orders.AFFECTION);
        dwarf.getOrders().set(Orders.Command.PATROL, p, dwarf.blockPosition());
        check(Mining.takePickaxe(dwarf, p, new ItemStack(Items.STONE_PICKAXE)) && count(p.getInventory().items, Items.IRON_PICKAXE) == 0,
                "she takes the pickaxe (her own goes nowhere)");
        ItemStack diamonds = new ItemStack(Items.DIAMOND, 2);
        check(Mining.send(level, dwarf, p, diamonds) && !dwarf.isRemoved() && Mining.away(p) == 0 && diamonds.getCount() == 2, "stone can't mine diamonds");
        check(Mining.takePickaxe(dwarf, p, new ItemStack(Items.DIAMOND_PICKAXE)) && count(p.getInventory().items, Items.STONE_PICKAXE) == 1, "a better one; the old one back");
        var plan = Mining.plan(level, dwarf.getMainHandItem(), Mining.vein(new ItemStack(Items.DIAMOND)));
        check(plan.ticks() == 5 * Mining.DAY && plan.found() == Mining.STACK && !plan.breaks(), "five days for a stack of diamonds");
        check(Mining.send(level, dwarf, p, diamonds) && dwarf.isRemoved() && Mining.away(p) == 1 && diamonds.getCount() == 1, "she digs in");
        var back = Mining.returnDue(p, true);
        check(back.size() == 1 && Mining.away(p) == 0, "she climbs out");
        AbstractMonsterGirl miner = back.get(0);
        check(miner.getMainHandItem().is(Items.DIAMOND_PICKAXE) && miner.getMainHandItem().getDamageValue() == plan.wear(), "her pickaxe worn");
        check(count(contents((Container) helper.getBlockEntity(chest)), Items.DIAMOND) == Mining.STACK + 1 && !Mining.holdsHaul(miner),
                "a stack of diamonds (and the sample) into the chest by her spot");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aBrokenPickaxeAndAHaulKept")
    public static void aBrokenPickaxeAndAHaulKept(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        p.getInventory().clearContent();
        var level = helper.getLevel();
        AbstractMonsterGirl mole = spawn(helper, GirlSpecies.MOLE_RODENT, new BlockPos(10, 2, 10));
        mole.addAffection(p, Orders.AFFECTION);
        check(Mining.takePickaxe(mole, p, new ItemStack(Items.WOODEN_PICKAXE)), "a wooden pickaxe");
        var plan = Mining.plan(level, mole.getMainHandItem(), Mining.vein(new ItemStack(Items.COBBLESTONE)));
        check(plan.breaks() && plan.found() < Mining.STACK && plan.found() > 32, "it won't last a whole stack");
        check(Mining.send(level, mole, p, new ItemStack(Items.COBBLESTONE)), "off she goes");
        AbstractMonsterGirl back = Mining.returnDue(p, true).get(0);
        check(back.getMainHandItem().isEmpty() && Mining.holdsHaul(back), "back early with a broken pickaxe, keeping what she found");
        check(!Mining.handOver(level, back), "her friend is too far away");
        p.teleportTo(back.getX() + 1, back.getY(), back.getZ());
        check(Mining.handOver(level, back) && count(p.getInventory().items, Items.COBBLESTONE) == plan.found() + 1 && !Mining.holdsHaul(back), "handed over");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
