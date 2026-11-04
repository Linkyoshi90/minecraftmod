package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.HomeLife;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Favours;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Favours (entity.girl.Favours): a friend asks for something, and is glad of it. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class FavourTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aFriendAsksAFavour")
    public static void aFriendAsksAFavour(GameTestHelper helper) {
        var level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        p.getInventory().clearContent();
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.INABA_JACKALOPE, new BlockPos(5, 2, 5));
        girl.setNoAi(true);
        check(Favours.check(level, p, true) == null, "a stranger asks nothing");
        girl.addAffection(p, HomeLife.FRIEND);
        check(Favours.check(level, p, true) == girl, "a friend asks");
        check(Favours.check(level, p, true) == null, "one favour at a time");
        var f = Favours.of(girl, p);
        check(f != null, "she remembers what she asked for");
        var want = Favours.item(f);
        int need = f.getInt("Count");
        check(Favours.interact(girl, p, new ItemStack(Items.DEAD_BUSH)) == null, "not what she asked for");
        int before = girl.getAffection(p);
        if (need > 1) {
            ItemStack some = new ItemStack(want, 1);
            check(Favours.interact(girl, p, some) == InteractionResult.SUCCESS && some.isEmpty(), "one now");
            check(Favours.of(girl, p).getInt("Count") == need - 1, "the rest later");
        }
        ItemStack rest = new ItemStack(want, 64);
        check(Favours.interact(girl, p, rest) != null, "the rest");
        check(rest.getCount() == 64 - (need > 1 ? need - 1 : need), "she takes only what she needs: " + rest.getCount());
        check(Favours.of(girl, p) == null && girl.getAffection(p) - before == Favours.THANKS, "glad of it");
        check(p.getInventory().countItem(Items.EMERALD) >= 2, "and pays them back");
        check(Favours.check(level, p, true) == girl, "now she can ask again");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
