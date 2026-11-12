package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Colds;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Colds (entity.girl.Colds): caught, and cured with soup. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class ColdTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "soupCuresACold")
    public static void soupCuresACold(GameTestHelper helper) {
        var level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        p.getInventory().clearContent();
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.INABA_JACKALOPE, new BlockPos(4, 2, 4));
        AbstractMonsterGirl koi = spawn(helper, GirlSpecies.KOI_MERMAID, new BlockPos(6, 2, 6));
        girl.setNoAi(true);
        check(Colds.catches(girl) && !Colds.catches(koi), "a mermaid never catches cold");
        ItemStack stew = new ItemStack(Items.MUSHROOM_STEW);
        check(Colds.interact(girl, p, stew) == null, "nothing to cure yet");
        Colds.catchCold(level, girl);
        check(Colds.hasCold(girl), "she caught one");
        Colds.tick(level, girl);
        check(girl.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "and is slow with it");
        int before = girl.getAffection(p);
        check(Colds.interact(girl, p, stew) == InteractionResult.SUCCESS && stew.isEmpty(), "soup for her");
        check(!Colds.hasCold(girl) && !girl.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "she is better");
        check(girl.getAffection(p) - before == Colds.CARE && p.getInventory().countItem(Items.BOWL) == 1, "grateful, and the bowl back");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
