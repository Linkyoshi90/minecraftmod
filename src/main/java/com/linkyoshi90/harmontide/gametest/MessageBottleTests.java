package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.MessageBottles;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.EnumSet;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;

/** Messages in bottles (entity/MessageBottles). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class MessageBottleTests {
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aBottleOnTheBeach")
    public static void aBottleOnTheBeach(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        EnumSet<MessageBottles.Contents> seen = EnumSet.noneOf(MessageBottles.Contents.class);
        for (int i = 0; i < 200; i++) seen.add(MessageBottles.roll(level));
        check(seen.size() == 3, "maps, treasure and letters all turn up");
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 1, 7));
        MessageBottles.open(level, p, MessageBottles.Contents.TREASURE);
        check(p.getInventory().countItem(Items.GLASS_BOTTLE) == 1, "the bottle is kept");
        int items = 0;
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) if (!p.getInventory().getItem(i).isEmpty()) items++;
        check(items >= 2, "and there was something in it");
        MessageBottles.open(level, p, MessageBottles.Contents.LETTER);
        check(p.getInventory().countItem(Items.GLASS_BOTTLE) == 2, "a letter, read");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
