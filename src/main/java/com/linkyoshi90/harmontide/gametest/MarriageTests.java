package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Marriage;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Marriage (entity/Marriage): the proposal, the wedding at the hearth, married life, and loss. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class MarriageTests {
    private static AbstractMonsterGirl still(GameTestHelper helper, BlockPos at) {
        AbstractMonsterGirl g = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, at);
        g.setNoAi(true);
        return g;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "proposalNeedsHerHeart")
    public static void proposalNeedsHerHeart(GameTestHelper helper) {
        AbstractMonsterGirl girl = still(helper, new BlockPos(3, 2, 3));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 3));
        girl.addAffection(p, 60);
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.WEDDING_RING.get()));
        CommonHooks.onInteractEntity(p, girl, InteractionHand.MAIN_HAND);
        check(!Marriage.engaged(girl, p) && p.getMainHandItem().is(ModItems.WEDDING_RING.get()), "not yet: the ring comes back");
        girl.setAffection(p.getUUID(), 100);
        CommonHooks.onInteractEntity(p, girl, InteractionHand.MAIN_HAND);
        check(Marriage.engaged(girl, p) && p.getMainHandItem().isEmpty() && Marriage.count(p) == 1, "yes!");
        check(Marriage.propose(helper.getLevel(), girl, p) == Marriage.Answer.ALREADY, "already promised to them");
        check(Marriage.describe(girl, p) != null, "it shows in her bond");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "oneSpouseEach")
    public static void oneSpouseEach(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl a = still(helper, new BlockPos(3, 2, 3)), b = still(helper, new BlockPos(9, 2, 9));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5)), q = HarpyTestUtil.survivalPlayer(helper, new BlockPos(6, 2, 6), "rival");
        a.setAffection(p.getUUID(), 100);
        a.setAffection(q.getUUID(), 100);
        b.setAffection(p.getUUID(), 100);
        Marriage.limitOverride = 1;
        try {
            check(Marriage.propose(level, a, p) == Marriage.Answer.ENGAGED, "engaged");
            check(Marriage.propose(level, b, p) == Marriage.Answer.TAKEN, "one spouse when the setting says one");
            check(Marriage.propose(level, a, q) == Marriage.Answer.TAKEN, "her heart is promised");
        } finally {
            Marriage.limitOverride = -1;
        }
        HarpyTestUtil.remove(p);
        HarpyTestUtil.remove(q);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 80, batch = "oneHappyFamily")
    public static void oneHappyFamily(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos hp = new BlockPos(7, 2, 7);
        helper.setBlock(hp, ModBlocks.HEARTH.get());
        AbstractMonsterGirl a = still(helper, new BlockPos(5, 2, 7)), b = still(helper, new BlockPos(6, 2, 9)), c = still(helper, new BlockPos(8, 2, 9));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 5));
        for (AbstractMonsterGirl g : new AbstractMonsterGirl[]{a, b, c}) g.setAffection(p.getUUID(), 100);
        helper.runAfterDelay(3, () -> {
            check(Marriage.propose(level, a, p) == Marriage.Answer.ENGAGED && Marriage.tryWed(level, a, p), "first wedding");
            check(Marriage.propose(level, b, p) == Marriage.Answer.ENGAGED, "any number by default: she says yes too");
            check(Marriage.tryWed(level, b, p) && Marriage.married(a, p) && Marriage.married(b, p), "both are wives");
            check(Marriage.blissFrom(level, p) == 2 && p.getMaxHealth() == 20 + 2 * Marriage.HEALTH, "the hearts add up");
            b.setAffection(p.getUUID(), 90);
            a.getPersistentData().remove("FamilyWarmedAt");
            check(Marriage.warmth(level, a, p) == 1 && b.getAffection(p) == 91, "a pat for one warms the other");
            check(Marriage.warmth(level, a, p) == 0 && b.getAffection(p) == 91, "once in a while");
            check(Marriage.warmth(level, c, p) == 0, "only a wife's pat spreads");
            check(Marriage.sheAsks(level, c, p) && p.getInventory().countItem(ModItems.WEDDING_RING.get()) == 1, "she asks first, ring and all");
            check(!Marriage.sheAsks(level, c, p), "once");
            check(Marriage.propose(level, c, p) == Marriage.Answer.ENGAGED && Marriage.tryWed(level, c, p) && Marriage.count(p) == 3, "three");
            check(p.getInventory().countItem(net.minecraft.world.item.Items.PAINTING) == 1, "the whole family, for the wall");
            check(Marriage.blissFrom(level, p) == 3 && p.getMaxHealth() == 20 + Marriage.BLISS_CAP * Marriage.HEALTH, "capped at three");
            long saved = level.getDayTime();
            level.setDayTime((saved / 24000L + 1) * 24000L + 1000L);              // a morning, put back within the tick
            check(Marriage.lunch(level, p) != null && Marriage.lunch(level, p) == null, "one packed lunch a morning");
            level.setDayTime(saved);
            check(Marriage.welcomeBack(level, p) != null, "a wife welcomes them back");
            Marriage.leaveNote(p, b);
            check(Marriage.notes(p).size() == 1 && Marriage.notes(p).getCompound(0).getString("Name").equals(b.getName().getString()), "a note from home");
            check(com.linkyoshi90.harmontide.HarpyCommands.familyLines(p).size() == 1 + 3, "/harmontide family: a header and three wives");
            net.minecraft.world.item.ItemStack blank = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.PAINTING);
            check(Marriage.familyPortrait(level, helper.absolutePos(new BlockPos(7, 2, 7)), p, blank) && blank.isEmpty(), "a family portrait by the hearth");
            String dreamt = Marriage.sharedDream(level, p, a, true);               // the flat test world may have no landmarks: just that it looks safely
            check(dreamt == null || Marriage.DREAM_PLACES.containsKey(dreamt), "a dream of somewhere real, or none");
            Marriage.dreamOf(level, p, b, p.blockPosition().offset(200, 0, 0), "oasis");
            check(!Marriage.dreamComeTrue(level, p), "not there yet");
            Marriage.dreamOf(level, p, b, p.blockPosition().offset(10, 0, 0), "oasis");
            check(Marriage.dreamComeTrue(level, p) && !Marriage.dreamComeTrue(level, p), "there together: the dream comes true, once");
            a.kill();
            check(!level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, a.getBoundingBox().inflate(3),
                    i -> i.getItem().is(ModItems.WEDDING_RING.get())).isEmpty(), "her ring stays behind");
            check(Marriage.count(p) == 2 && p.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION), "the others hold them close");
            check(Marriage.blissFrom(level, p) == 2 && p.getMaxHealth() == 20 + 2 * Marriage.HEALTH, "and two still near");
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 80, batch = "weddingAtTheHearth")
    public static void weddingAtTheHearth(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos hp = new BlockPos(7, 2, 7);
        helper.setBlock(hp, ModBlocks.HEARTH.get());
        AbstractMonsterGirl bride = still(helper, new BlockPos(5, 2, 7)), guest = still(helper, new BlockPos(2, 2, 12));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(9, 2, 7));
        bride.setAffection(p.getUUID(), 100);
        guest.setAffection(p.getUUID(), 30);
        check(Marriage.propose(level, bride, p) == Marriage.Answer.ENGAGED, "engaged");
        helper.runAfterDelay(3, () -> {                                     // the hearth joins the loaded list a tick later
            check(Marriage.tryWed(level, bride, p), "wed by the fire");
            check(Marriage.married(bride, p) && !Marriage.engaged(bride, p) && bride.hasFlag(AbstractMonsterGirl.FLAG_VEIL), "married, in her veil");
            check(Marriage.honeymoon(bride) && Marriage.bedBonus(bride, p) == Marriage.BED + Marriage.HONEYMOON_BED && Marriage.count(p) == 1, "keener for bed, on honeymoon");
            check(Marriage.familyBed(level, p, 2) && !Marriage.familyBed(level, p, 1), "two wives in bed make a family bed");
            int fond = guest.getAffection(p);
            check(Marriage.bouquet(level, bride, p, java.util.List.of(guest)) == guest && guest.getAffection(p) == fond + Marriage.BOUQUET, "the guest catches the bouquet");
            check(Marriage.bliss(level, p) && p.getMaxHealth() == 20 + Marriage.HEALTH, "two more hearts with her near");
            bride.kill();
            check(Marriage.count(p) == 0, "free again when she is gone");
            check(Marriage.family(p).size() == 1 && Marriage.family(p).getCompound(0).getBoolean("Gone"), "the family page remembers her");
            check(!Marriage.bliss(level, p) && p.getMaxHealth() == 20, "and the hearts go with her");
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "realmWeddingByTheCenser")
    public static void realmWeddingByTheCenser(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.setBlock(new BlockPos(7, 2, 7), ModBlocks.REALM_CENSER.get().defaultBlockState().setValue(com.linkyoshi90.harmontide.block.RealmCenserBlock.LIT, true));
        AbstractMonsterGirl bride = still(helper, new BlockPos(5, 2, 7));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(9, 2, 7));
        bride.setAffection(p.getUUID(), 100);
        int touch = com.linkyoshi90.harmontide.entity.RealmTouched.touch(p);
        check(Marriage.propose(level, bride, p) == Marriage.Answer.ENGAGED, "engaged");
        helper.runAfterDelay(3, () -> {
            check(Marriage.tryWed(level, bride, p) && Marriage.married(bride, p), "wed in violet smoke");
            check(com.linkyoshi90.harmontide.entity.RealmTouched.touch(p) == Math.min(com.linkyoshi90.harmontide.entity.RealmTouched.MAX, touch + Marriage.REALM_VOWS), "the realm keeps the vows");
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "everyKindWedsHerWay")
    public static void everyKindWedsHerWay(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(9, 2, 7));
        AbstractMonsterGirl fox = spawn(helper, GirlSpecies.RED_KITSUNE, new BlockPos(5, 2, 7)), cat = still(helper, new BlockPos(3, 2, 7));
        check("sunshower".equals(Marriage.flourish(level, fox, p)), "a fox's wedding");
        check(Marriage.flourish(level, cat, p) == null, "a cat just weds");
        level.setWeatherParameters(6000, 0, false, false);                    // the sunshower is the test world's only rain: put it back
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "aDoubleWedding")
    public static void aDoubleWedding(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        helper.setBlock(new BlockPos(7, 2, 7), ModBlocks.HEARTH.get());
        AbstractMonsterGirl a = still(helper, new BlockPos(5, 2, 7)), b = still(helper, new BlockPos(9, 2, 9));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 5));
        a.setAffection(p.getUUID(), 100);
        b.setAffection(p.getUUID(), 100);
        check(Marriage.propose(level, a, p) == Marriage.Answer.ENGAGED && Marriage.propose(level, b, p) == Marriage.Answer.ENGAGED, "two engagements");
        helper.runAfterDelay(3, () -> {
            boolean held = Marriage.married(a, p) || Marriage.tryWed(level, a, p);    // (her own tick may have held it already)
            check(held && Marriage.married(a, p) && Marriage.married(b, p), "both wed in one ceremony");
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "weddingBellsRing")
    public static void weddingBellsRing(GameTestHelper helper) {
        helper.setBlock(new BlockPos(10, 2, 10), net.minecraft.world.level.block.Blocks.BELL);
        helper.runAfterDelay(2, () -> {                                     // the bell's meeting point registers a tick later
            check(Marriage.ringBells(helper.getLevel(), helper.absolutePos(new BlockPos(3, 2, 3))) == 1, "the village bell rings for the wedding");
            helper.succeed();
        });
    }
}
