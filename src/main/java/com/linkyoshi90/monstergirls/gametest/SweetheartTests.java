package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Sweethearts;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Villager sweethearts and grandchildren (entity/Sweethearts). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class SweetheartTests {
    private static AbstractMonsterGirl daughter(GameTestHelper helper, ServerPlayer p, BlockPos pos) {
        AbstractMonsterGirl g = spawn(helper, GirlSpecies.WOLF_CANID, pos);
        g.setBaby(false);
        g.bornTo(p.getUUID());
        return g;
    }

    private static Villager villager(GameTestHelper helper, BlockPos pos) {
        Villager v = helper.spawn(EntityType.VILLAGER, pos);
        v.setNoAi(true);
        return v;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "onlyAGrownDaughterCourts")
    public static void onlyAGrownDaughterCourts(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(10, 2, 10));
        AbstractMonsterGirl wild = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(2, 2, 2));
        AbstractMonsterGirl little = daughter(helper, p, new BlockPos(4, 2, 2));
        little.setBaby(true);
        AbstractMonsterGirl grown = daughter(helper, p, new BlockPos(6, 2, 2));
        check(!Sweethearts.eligible(wild), "a wild girl isn't anyone's daughter");
        check(!Sweethearts.eligible(little), "a little one never courts");
        check(Sweethearts.eligible(grown), "a grown daughter may");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aFlowerIntroducesThem")
    public static void aFlowerIntroducesThem(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(10, 2, 10));
        AbstractMonsterGirl g = daughter(helper, p, new BlockPos(4, 2, 4));
        Villager v = villager(helper, new BlockPos(8, 2, 8));
        ItemStack flower = new ItemStack(Items.POPPY, 2);
        p.setItemInHand(InteractionHand.MAIN_HAND, flower);
        PlayerInteractEvent.EntityInteract event = new PlayerInteractEvent.EntityInteract(p, InteractionHand.MAIN_HAND, v);
        Sweethearts.onInteract(event);
        check(event.isCanceled() && v.getUUID().equals(Sweethearts.sweetheart(g)), "she is sweet on him now");
        check(flower.getCount() == 1, "the flower was given");
        check(!Sweethearts.free(level, v), "and he is taken");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "threeMorningsAndTheyAreWed")
    public static void threeMorningsAndTheyAreWed(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(10, 2, 10));
        AbstractMonsterGirl g = daughter(helper, p, new BlockPos(4, 2, 4));
        Villager v = villager(helper, new BlockPos(8, 2, 8));
        check(Sweethearts.court(level, g, v), "she takes a shine to him");
        for (int day = 1; day < Sweethearts.COURT_DAYS; day++) Sweethearts.morning(level, g, day);
        check(!Sweethearts.wed(g) && Sweethearts.courted(g) == Sweethearts.COURT_DAYS - 1, "still courting");
        Sweethearts.morning(level, g, Sweethearts.COURT_DAYS);
        check(Sweethearts.wed(g), "wed on the third morning");
        check(Sweethearts.describe(g) != null, "the bonds list knows");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aGrandchildIsDevotedToHerGrandparent")
    public static void aGrandchildIsDevotedToHerGrandparent(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(10, 2, 10));
        AbstractMonsterGirl g = daughter(helper, p, new BlockPos(4, 2, 4));
        Villager v = villager(helper, new BlockPos(8, 2, 8));
        Sweethearts.court(level, g, v);
        Sweethearts.wedding(level, g, v);
        Sweethearts.morning(level, g, 1);
        check(!g.getGestation().isCarrying(), "not every morning");
        Sweethearts.morning(level, g, Sweethearts.EVERY);
        check(g.getGestation().isCarrying() && p.getUUID().equals(g.getGestation().getFather()) && Sweethearts.littleOnes(g) == 1,
                "a little one on the way, born to be her grandparent's");
        AbstractMonsterGirl young = g.spawnYoung(level, g.blockPosition(), g.getGestation().getFather());
        check(young != null && p.getUUID().equals(young.getFather()) && !young.trustsEnoughForBed(p), "devoted to her grandparent, never more");
        g.getGestation().clear();
        Sweethearts.checkBirth(level, g);
        check(Sweethearts.describe(g) != null && !g.getPersistentData().getBoolean("SweetCarrying"), "the birth is noticed");
        AbstractMonsterGirl sister = daughter(helper, p, new BlockPos(2, 2, 8));
        Villager w = villager(helper, new BlockPos(2, 2, 10));
        Sweethearts.court(level, sister, w);
        Sweethearts.wedding(level, sister, w);
        sister.getPersistentData().putInt("SweetLittleOnes", Sweethearts.LITTLE_ONES);
        check(sister.getGestation().canConceive(), "she could");
        Sweethearts.morning(level, sister, Sweethearts.EVERY * 10L);
        check(!sister.getGestation().isCarrying(), "but two at most");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "sheGrievesHerSweetheart")
    public static void sheGrievesHerSweetheart(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(10, 2, 10));
        AbstractMonsterGirl g = daughter(helper, p, new BlockPos(4, 2, 4));
        Villager v = villager(helper, new BlockPos(8, 2, 8));
        Sweethearts.court(level, g, v);
        v.kill();
        check(Sweethearts.sweetheart(g) == null && !Sweethearts.wed(g), "she is free again, one day");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
