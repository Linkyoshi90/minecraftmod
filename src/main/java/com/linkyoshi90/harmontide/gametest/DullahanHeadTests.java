package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.DullahanHeads;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Dullahan head pops (entity/DullahanHeads). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class DullahanHeadTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aDullahanLosesHerHead")
    public static void aDullahanLosesHerHead(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.DULLAHAN_UNDEAD, new BlockPos(5, 2, 5));
        girl.setNoAi(true);
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(9, 2, 9));
        check(DullahanHeads.pop(level, wolf) == null, "only a dullahan's head comes off");
        ItemEntity head = DullahanHeads.pop(level, girl);
        check(head != null && DullahanHeads.headless(girl) && girl.getUUID().equals(DullahanHeads.owner(head.getItem())), "off it pops");
        check(DullahanHeads.pop(level, girl) == null, "only one head to lose");
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 5));
        ItemStack caught = head.getItem().copy();
        head.discard();
        check(!DullahanHeads.handBack(level, wolf, p, caught.copy()), "not hers");
        int before = girl.getAffection(p);
        check(DullahanHeads.handBack(level, girl, p, caught) && !DullahanHeads.headless(girl) && girl.getAffection(p) == before + DullahanHeads.THANKS, "handed back: grateful");
        check(DullahanHeads.stale(caught, level.getGameTime() + DullahanHeads.RECALL), "a head kept too long goes home on its own");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
