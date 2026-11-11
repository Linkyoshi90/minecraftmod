package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Clade;
import com.linkyoshi90.monstergirls.entity.WanderingMerchants;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Wandering merchants (entity/WanderingMerchants): her clade's wares, trading, and moving on or staying. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class WanderingMerchantTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "everyCladeHasWares")
    public static void everyCladeHasWares(GameTestHelper helper) {
        for (Clade c : Clade.values()) check(WanderingMerchants.WARES_OF.containsKey(c), "no wares for " + c);
        check(!WanderingMerchants.canTrade(GirlSpecies.KOI_MERMAID) && !WanderingMerchants.canTrade(GirlSpecies.WOOD_ELF)
                && !WanderingMerchants.canTrade(GirlSpecies.MEDUSA_LAMIA) && WanderingMerchants.canTrade(GirlSpecies.ORB_WEAVER_ARACHNE),
                "not merfolk, elves or legends");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aSpinnerSellsSilk")
    public static void aSpinnerSellsSilk(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        AbstractMonsterGirl spider = spawn(helper, GirlSpecies.ORB_WEAVER_ARACHNE, new BlockPos(5, 2, 5));
        spider.setNoAi(true);
        check(!WanderingMerchants.isMerchant(spider) && WanderingMerchants.open(spider, p) == null, "an ordinary spider has nothing to sell");
        WanderingMerchants.makeMerchant(level, spider);
        var offers = WanderingMerchants.offers(spider);
        check(offers.size() == WanderingMerchants.WARES + 1 && spider.isCalm(), "her pack: four wares and bread bought back; she won't fight");
        var allowed = WanderingMerchants.WARES_OF.get(Clade.CHELICERATA);
        for (int i = 0; i < WanderingMerchants.WARES; i++) {
            MerchantOffer o = offers.get(i);
            check(o.getCostA().is(Items.EMERALD) && allowed.stream().anyMatch(w -> o.getResult().is(w.item().get().asItem())), "a spinner's ware: " + o.getResult());
        }
        var stall = WanderingMerchants.open(spider, p);
        check(stall != null && stall.getTradingPlayer() == p, "her stall opens");
        stall.notifyTrade(stall.getOffers().get(0));
        stall.setTradingPlayer(null);
        check(spider.getAffection(p) == 1 && WanderingMerchants.offers(spider).get(0).getUses() == 1, "a trade: a little fondness, and her stock remembers it");
        check(WanderingMerchants.update(level, spider), "still trading");
        WanderingMerchants.endNow(spider);
        check(!WanderingMerchants.update(level, spider) && spider.isRemoved(), "a stranger: she moves on");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aFriendStays")
    public static void aFriendStays(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        AbstractMonsterGirl cow = spawn(helper, GirlSpecies.CATTLE_MINOTAUR, new BlockPos(5, 2, 5));
        cow.setNoAi(true);
        WanderingMerchants.makeMerchant(level, cow);
        cow.setAffection(p.getUUID(), 20);
        WanderingMerchants.endNow(cow);
        check(!WanderingMerchants.update(level, cow) && cow.isAlive() && !WanderingMerchants.isMerchant(cow), "a friend made: she puts her pack down and stays");
        check(ModItems.SPIDER_SILK.get() != null, "registry up");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
