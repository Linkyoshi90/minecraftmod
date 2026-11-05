package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.HearthBlockEntity;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Market;
import com.linkyoshi90.monstergirls.entity.girl.Rivalries;
import com.linkyoshi90.monstergirls.entity.girl.Settlement;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
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
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Market day (entity.girl.Market) and rivalries (entity.girl.Rivalries). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class MarketTests {
    private static final long DAY = 24000L;

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aTravellerAtTheHearth")
    public static void aTravellerAtTheHearth(GameTestHelper helper) {
        check(Market.marketDay(3 * DAY + 100) && Market.marketDay(9 * DAY) && !Market.marketDay(4 * DAY), "every sixth day");
        BlockPos hp = new BlockPos(1, 2, 1);
        helper.setBlock(hp, ModBlocks.HEARTH.get());
        HearthBlockEntity hearth = (HearthBlockEntity) helper.getBlockEntity(hp);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        hearth.setOwner(p.getUUID());
        AbstractMonsterGirl resident = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(4, 2, 2));
        resident.addAffection(p, Orders.AFFECTION);
        check(Settlement.settle(helper.getLevel(), resident, hearth), "a friend lives there");
        var level = helper.getLevel();
        AbstractMonsterGirl trader = Market.arrive(level, helper.absolutePos(hp), p);
        check(trader != null && Market.trader(trader) && Market.stock(trader) == Market.STOCK && !trader.isBaby()
                && trader.getSpecies().family() != GirlSpecies.Family.CANID, "a grown traveller from another family sets up");
        p.getInventory().clearContent();
        ItemStack emeralds = new ItemStack(Items.EMERALD, 10);
        check(Market.sell(level, trader, p, emeralds) && emeralds.getCount() == 9 && Market.stock(trader) == Market.STOCK - 1, "an emerald buys a bundle");
        int got = 0;
        for (ItemStack s : p.getInventory().items) if (!s.isEmpty()) got++;
        check(got >= 1, "of her country's finds");
        while (Market.stock(trader) > 0) Market.sell(level, trader, p, emeralds);
        check(!Market.sell(level, trader, p, emeralds) && emeralds.getCount() == 10 - Market.STOCK, "until she has sold out");
        check(!Market.sell(level, resident, p, emeralds), "the residents don't trade");
        trader.getPersistentData().putLong(Market.LEAVES, level.getGameTime());
        check(Market.maybeLeave(level, trader) && trader.isRemoved(), "a stranger moves on");
        AbstractMonsterGirl friend = Market.arrive(level, helper.absolutePos(hp), p);
        check(friend != null, "another comes");
        friend.addAffection(p, Orders.AFFECTION);
        friend.getPersistentData().putLong(Market.LEAVES, level.getGameTime());
        check(!Market.maybeLeave(level, friend) && !friend.isRemoved() && !Market.trader(friend), "a friend stays, and is a girl like any other");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "rivalsSquabble")
    public static void rivalsSquabble(GameTestHelper helper) {
        var level = helper.getLevel();
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(3, 2, 3));
        AbstractMonsterGirl hare = spawn(helper, GirlSpecies.JACK_JACKALOPE, new BlockPos(6, 2, 6));
        check(Rivalries.hunts(com.linkyoshi90.monstergirls.entity.Clade.CANIDAE, com.linkyoshi90.monstergirls.entity.Clade.LAGOMORPHA) && !Rivalries.hunts(com.linkyoshi90.monstergirls.entity.Clade.LAGOMORPHA, com.linkyoshi90.monstergirls.entity.Clade.CANIDAE)
                && Rivalries.hunts(GirlSpecies.HYENA_CANID.clade(), GirlSpecies.CAPYBARA_RODENT.clade()),
                "the wolf hunts the hare, not the other way; the hyena is a feliform and minds the mice");
        check(Rivalries.squabble(level, hare) == null, "the hare starts nothing");
        check(Rivalries.squabble(level, wolf) == hare, "the wolf glowers at the hare");
        check(hare.getPersistentData().getLong(Rivalries.FLED) == level.getGameTime(), "and the hare bolts");
        check(Rivalries.squabble(level, wolf) == null && hare.getHealth() == hare.getMaxHealth(), "once in a while, and nobody is hurt");
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        AbstractMonsterGirl pet = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(9, 2, 3));
        AbstractMonsterGirl bunny = spawn(helper, GirlSpecies.EASTER_JACKALOPE, new BlockPos(9, 2, 6));
        bunny.addAffection(p, Orders.AFFECTION);
        check(Rivalries.squabble(level, pet) == null, "a friend is never picked on");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
