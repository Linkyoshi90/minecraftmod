package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideConfig;
import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Breastfeed;
import com.linkyoshi90.harmontide.entity.Lactation;
import com.linkyoshi90.harmontide.entity.ai.NurseGoal;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModGirls;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;

/** A feed at her chest (entity/Breastfeed): the baby first, and a hungry player only when the option is on. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class BreastfeedTests {
    @SuppressWarnings("unchecked")
    private static AbstractMonsterGirl still(GameTestHelper helper, GirlSpecies species, BlockPos at) {
        AbstractMonsterGirl girl = (AbstractMonsterGirl) helper.spawn(ModGirls.type(species), at);
        girl.setNoAi(true);
        return girl;
    }

    private static void stand(GameTestHelper helper, ServerPlayer player, BlockPos at) {
        Vec3 pos = helper.absoluteVec(Vec3.atBottomCenterOf(at));
        player.teleportTo(helper.getLevel(), pos.x, pos.y, pos.z, 0.0F, 0.0F);
        player.setDeltaMovement(Vec3.ZERO);
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "breastfeed")
    public static void sheOffersOnlyAHungryGrownGirl(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 7));
        AbstractMonsterGirl cow = still(helper, GirlSpecies.HOLSTAUR_MINOTAUR, new BlockPos(9, 2, 7));
        AbstractMonsterGirl fairy = still(helper, GirlSpecies.FAIRY_BUG, new BlockPos(3, 2, 3));
        AbstractMonsterGirl calf = still(helper, GirlSpecies.HOLSTAUR_MINOTAUR, new BlockPos(3, 2, 7));
        calf.setBaby(true);
        boolean was = HarmontideConfig.NSFW.get();
        try {
            HarmontideConfig.NSFW.set(false);
            player.getFoodData().setFoodLevel(2);
            check(!Breastfeed.canOffer(cow, player), "the option is off");
            HarmontideConfig.NSFW.set(true);
            player.getFoodData().setFoodLevel(20);
            check(!Breastfeed.canOffer(cow, player), "he is not hungry");
            player.getFoodData().setFoodLevel(11);
            check(!Breastfeed.canOffer(cow, player), "more than half a bar");
            player.getFoodData().setFoodLevel(10);
            check(Breastfeed.canOffer(cow, player), "half a bar");
            player.getFoodData().setFoodLevel(2);
            check(!Breastfeed.canOffer(fairy, player), "a fairy is too small");
            check(!Breastfeed.canOffer(calf, player), "a baby is a baby");
            cow.setFather(player.getUUID());
            check(!Breastfeed.canOffer(cow, player), "not her own father");
            cow.setFather(null);
            Breastfeed.accept(player, cow.getId());
            check(cow.getAction() != AbstractMonsterGirl.Action.FEED, "yes does nothing until she has asked");
            check(Breastfeed.canOffer(cow, player) && Lactation.isLactating(cow), "a hungry man and a cow with milk");
            player.getFoodData().setFoodLevel(11);
            cow.interact(player, InteractionHand.MAIN_HAND);
            check(!Breastfeed.active(cow), "a right click above half a bar does not ask");
            player.getFoodData().setFoodLevel(10);
            cow.interact(player, InteractionHand.MAIN_HAND);
            check(Breastfeed.active(cow), "a right click at half a bar asks");
            Breastfeed.accept(player, cow.getId());
            Breastfeed.tick(cow);
            check(cow.getAction() == AbstractMonsterGirl.Action.FEED && cow.hasFlag(AbstractMonsterGirl.FLAG_BARE), "yes, and her top comes up");
            Breastfeed.stop(cow);
        } finally {
            HarmontideConfig.NSFW.set(was);
        }
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "breastfeed")
    public static void sheBendsLiftsOrClimbs(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 7));
        player.getFoodData().setFoodLevel(2);
        boolean was = HarmontideConfig.NSFW.get();
        try {
            HarmontideConfig.NSFW.set(true);
            stand(helper, player, new BlockPos(7, 2, 7));
            AbstractMonsterGirl cow = still(helper, GirlSpecies.HOLSTAUR_MINOTAUR, new BlockPos(10, 2, 7));
            check(Breastfeed.kind(cow) == Breastfeed.Kind.BEND, "a holstaur bends");
            check(Breastfeed.startFeed(cow, player), "she comes to him");
            double floor = player.getY();
            Breastfeed.tick(cow);
            check(cow.getAction() == AbstractMonsterGirl.Action.FEED && cow.hasFlag(AbstractMonsterGirl.FLAG_BARE), "her top is up");
            check(cow.distanceTo(player) < 1.2 && Math.abs(cow.getY() - floor) < 0.25, "she bends over him where he stands");
            Breastfeed.stop(cow);

            stand(helper, player, new BlockPos(7, 2, 4));
            AbstractMonsterGirl small = still(helper, GirlSpecies.MOOSKUH_MINOTAUR, new BlockPos(10, 2, 4));
            check(Breastfeed.kind(small) == Breastfeed.Kind.SMALL, "a mooskuh climbs");
            double feet = small.getY();
            check(Breastfeed.startFeed(small, player), "she hops up");
            Breastfeed.tick(small);
            check(small.getY() > feet + 0.4 && small.isNoGravity() && small.hasFlag(AbstractMonsterGirl.FLAG_BARE), "up at his mouth, top up");
            Breastfeed.stop(small);
            check(!small.isNoGravity(), "back on the ground afterwards");

            stand(helper, player, new BlockPos(7, 2, 11));
            AbstractMonsterGirl tall = still(helper, GirlSpecies.GIRAFFE_CENTAUR, new BlockPos(10, 2, 11));
            tall.getGestation().setTicksLeft(1000);
            check(Breastfeed.kind(tall) == Breastfeed.Kind.TOWER && Lactation.isLactating(tall), "a giraffe towers, and the milk is in");
            double before = player.getY();
            check(Breastfeed.startFeed(tall, player), "she picks him up");
            Breastfeed.tick(tall);
            check(player.getY() > before + 0.4 && tall.hasFlag(AbstractMonsterGirl.FLAG_BARE), "he is lifted to her chest");
            check(tall.getAction() == AbstractMonsterGirl.Action.FEED, "she is feeding him");
            Breastfeed.stop(tall);
        } finally {
            HarmontideConfig.NSFW.set(was);
        }
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "breastfeed")
    public static void theMealFillsHim(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 7));
        player.getFoodData().setFoodLevel(2);
        AbstractMonsterGirl cow = still(helper, GirlSpecies.HOLSTAUR_MINOTAUR, new BlockPos(9, 2, 7));
        boolean was = HarmontideConfig.NSFW.get();
        try {
            HarmontideConfig.NSFW.set(true);
            check(Breastfeed.startFeed(cow, player), "the hold starts");
            for (int i = 0; i < Breastfeed.FEED_TICKS; i++) Breastfeed.tick(cow);
            check(player.getFoodData().getFoodLevel() == 20, "the bar is full");
            check(Lactation.isLactating(cow) && cow.getAction() != AbstractMonsterGirl.Action.FEED && !Breastfeed.active(cow), "the hold is over, and the milk is still in");
            player.getFoodData().setFoodLevel(4);
            check(Breastfeed.canOffer(cow, player) && Breastfeed.startFeed(cow, player), "she can feed him again");
            for (int i = 0; i < Breastfeed.FEED_TICKS; i++) {
                cow.tickCount++;
                Breastfeed.ensure(cow);
            }
            check(player.getFoodData().getFoodLevel() == 20, "a click she already accepted still fills him");
            check(!Breastfeed.active(cow), "that one ends itself too");
        } finally {
            HarmontideConfig.NSFW.set(was);
        }
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "breastfeed")
    public static void aMotherHoldsHerBabyToTheBreast(GameTestHelper helper) {
        AbstractMonsterGirl mother = still(helper, GirlSpecies.WOLF_CANID, new BlockPos(4, 2, 4));
        AbstractMonsterGirl baby = mother.spawnYoung(helper.getLevel(), helper.absolutePos(new BlockPos(6, 2, 4)), null);
        baby.setNoAi(true);
        baby.moveTo(mother.getX(), mother.getY(), mother.getZ(), 0.0F, 0.0F);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 6));
        player.getFoodData().setFoodLevel(2);
        boolean was = HarmontideConfig.NSFW.get();
        try {
            HarmontideConfig.NSFW.set(true);
            mother.tickCount = 40;
            check(Breastfeed.canOffer(mother, player), "she has milk and he is hungry");
            check(!Breastfeed.wants(mother) && !Breastfeed.active(mother), "her baby comes first");
            HarmontideConfig.NSFW.set(false);
            mother.tickCount = 41;
            NurseGoal clothed = new NurseGoal(mother);
            check(clothed.canUse(), "she calls the baby");
            clothed.start();
            clothed.tick();
            check(mother.getAction() == AbstractMonsterGirl.Action.NURSE && !mother.hasFlag(AbstractMonsterGirl.FLAG_BARE), "the old cradle, clothed");
            check(!baby.hasFlag(AbstractMonsterGirl.FLAG_BARE) && baby.isBaby() && baby.getVehicle() == mother, "the baby stays a baby in her arms");
            check(mother.getControllingPassenger() == null, "held, not steering");
            clothed.stop();
            baby.stopRiding();
            baby.moveTo(mother.getX(), mother.getY(), mother.getZ(), 0.0F, 0.0F);
            mother.setAction(AbstractMonsterGirl.Action.NONE);
            mother.getPersistentData().putLong(NurseGoal.NEXT, 0);
            mother.tickCount = 41;
            HarmontideConfig.NSFW.set(true);
            NurseGoal open = new NurseGoal(mother);
            check(open.canUse(), "she calls her again");
            open.start();
            open.tick();
            check(mother.getAction() == AbstractMonsterGirl.Action.NURSE, "she is nursing again");
            check(mother.hasFlag(AbstractMonsterGirl.FLAG_BARE), "her top is up for the baby");
            check(!baby.hasFlag(AbstractMonsterGirl.FLAG_BARE) && baby.isBaby(), "the baby is not bared");
            open.stop();
        } finally {
            HarmontideConfig.NSFW.set(was);
        }
        HarpyTestUtil.remove(player);
        helper.succeed();
    }
}
