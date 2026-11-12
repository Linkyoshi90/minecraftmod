package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.MineShaft;
import com.linkyoshi90.harmontide.entity.girl.Mining;
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

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Mining trips (entity.girl.Mining). */
@GameTestHolder(HarmontideMod.MODID)
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

    /** The mine she leaves (MineShaft): a laddered shaft beside the spot, deeper while she's down, stopped by a built floor; ore struck comes up. */
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aShaftBesideHerSpot")
    public static void aShaftBesideHerSpot(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        p.getInventory().clearContent();
        var level = helper.getLevel();
        for (int x = 3; x <= 11; x++) for (int z = 3; z <= 11; z++) {             // a hill of stone on a built floor
            helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE_BRICKS);
            for (int y = 2; y <= 5; y++) helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
        }
        BlockPos spot = new BlockPos(7, 6, 7);
        for (net.minecraft.core.Direction d : net.minecraft.core.Direction.Plane.HORIZONTAL) helper.setBlock(spot.relative(d).below(2), Blocks.COAL_ORE);
        BlockPos chest = new BlockPos(9, 6, 7);
        helper.setBlock(chest, Blocks.CHEST);
        AbstractMonsterGirl gnome = spawn(helper, GirlSpecies.GNOME_ELF, spot);
        gnome.addAffection(p, Orders.AFFECTION);
        gnome.getOrders().set(Orders.Command.PATROL, p, helper.absolutePos(spot));
        check(Mining.takePickaxe(gnome, p, new ItemStack(Items.STONE_PICKAXE)), "a pickaxe");
        check(Mining.send(level, gnome, p, new ItemStack(Items.COAL)) && gnome.isRemoved(), "she digs in");
        var trip = p.getData(com.linkyoshi90.harmontide.registry.ModAttachments.EXPEDITIONS).getList(Mining.LIST, net.minecraft.nbt.Tag.TAG_COMPOUND).getCompound(0);
        check(trip.contains(MineShaft.SHAFT) && trip.getInt(MineShaft.DUG) == MineShaft.FIRST && trip.getInt(MineShaft.DEEP) == MineShaft.depth(0.5), "the shaft opens: " + trip);
        BlockPos top = BlockPos.of(trip.getLong(MineShaft.SHAFT));
        check(level.getBlockState(top).is(Blocks.LADDER) && level.getBlockState(top.below()).is(Blocks.LADDER) && level.getBlockState(top.below(2)).is(Blocks.STONE),
                "two rungs down so far");
        check(top.equals(helper.absolutePos(spot).below().north()), "beside her spot: " + top);
        Mining.returnDue(p, true);
        check(level.getBlockState(top.below(2)).is(Blocks.LADDER) && level.getBlockState(top.below(3)).is(Blocks.STONE),
                "deeper by the time she's out, but never through the built floor");
        check(count(contents((Container) helper.getBlockEntity(chest)), Items.COAL) == Mining.STACK + 1 + 1, "the stack, the sample and the coal she struck");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
