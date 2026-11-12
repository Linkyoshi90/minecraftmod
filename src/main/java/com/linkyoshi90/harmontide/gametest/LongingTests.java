package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.TarShroomBlock;
import com.linkyoshi90.harmontide.entity.Missing;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Longing (Missing): the tar-shroom, the salami, and the cuddle when the player comes back. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class LongingTests {
    private static AbstractMonsterGirl still(GameTestHelper helper, BlockPos at) {
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, at);
        girl.setNoAi(true);
        com.linkyoshi90.harmontide.entity.MatingSeason.settle(girl);
        return girl;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "longingAddsToMissing")
    public static void longingAddsToMissing(GameTestHelper helper) {
        check(Missing.stage(60, 0, 0) == 0 && Missing.stage(60, 0, 50) == 1 && Missing.stage(60, 0, 100) == 2, "longing alone");
        int start = Missing.startDays(60);
        check(Missing.stage(60, start, 0) == 1 && Missing.stage(60, start * 2L, 0) == 2 && Missing.stage(60, start - 1, 0) == 0, "days as before");
        check(Missing.stage(60, start, 50) == 2, "together");
        check(Missing.stage(10, 0, 100) == 0, "not close enough");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "tarShroomMakesHerLong")
    public static void tarShroomMakesHerLong(GameTestHelper helper) {
        AbstractMonsterGirl girl = still(helper, new BlockPos(2, 2, 2));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 2));
        check(TarShroomBlock.breathe(helper.getLevel(), helper.absolutePos(new BlockPos(1, 2, 1))) == 0 && Missing.longing(girl, p) == 0, "a stranger doesn't long");
        girl.addAffection(p, 60);
        check(TarShroomBlock.breathe(helper.getLevel(), helper.absolutePos(new BlockPos(1, 2, 1))) == 1, "a friend does");
        check(Missing.longing(girl, p) == Missing.SHROOM, "five percent, with the player right there");
        for (int i = 0; i < 40; i++) TarShroomBlock.breathe(helper.getLevel(), helper.absolutePos(new BlockPos(1, 2, 1)));
        check(Missing.longing(girl, p) == Missing.MAX_LONGING, "capped");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "salamiThenCuddle")
    public static void salamiThenCuddle(GameTestHelper helper) {
        AbstractMonsterGirl girl = still(helper, new BlockPos(2, 2, 2));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 2));
        girl.addAffection(p, 60);
        Missing.meet(girl, p);
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.SALAMI.get(), 2));
        CommonHooks.onInteractEntity(p, girl, InteractionHand.MAIN_HAND);
        CommonHooks.onInteractEntity(p, girl, InteractionHand.MAIN_HAND);
        check(Missing.longing(girl, p) == Missing.SALAMI * 2 && p.getMainHandItem().isEmpty(), "two salami eaten");
        float before = Missing.conceptionChance(girl, p);
        Missing.meet(girl, p);
        check(Missing.activeStage(girl, p) == 0, "still here: no reunion yet");
        var journal = p.getData(com.linkyoshi90.harmontide.registry.ModAttachments.JOURNAL);
        journal.putLong("Clock", journal.getLong("Clock") + Missing.AWAY + 20);
        p.setData(com.linkyoshi90.harmontide.registry.ModAttachments.JOURNAL, journal);
        Missing.meet(girl, p);
        check(Missing.activeStage(girl, p) == 1 && Missing.longing(girl, p) == 0, "came back: she missed them, longing spent");
        check(Missing.cuddled(girl, p) && girl.getAction() == AbstractMonsterGirl.Action.SITTING, "and cuddled up, being right there");
        check(Missing.conceptionChance(girl, p) > before && Missing.bedBonus(girl, p) == 0.5F, "more likely");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
