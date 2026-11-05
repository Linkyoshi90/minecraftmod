package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.HarpyCommands;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.AnuranSpawnBlock;
import com.linkyoshi90.monstergirls.block.HarpyEggBlock;
import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.BedCall;
import com.linkyoshi90.monstergirls.entity.BedSharing;
import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.block.ParentageBlockEntity;
import com.linkyoshi90.monstergirls.entity.Bullywug;
import com.linkyoshi90.monstergirls.entity.CrowHarpy;
import com.linkyoshi90.monstergirls.entity.Gestation;
import com.linkyoshi90.monstergirls.entity.MucusToad;
import com.linkyoshi90.monstergirls.entity.OwlHarpy;
import com.linkyoshi90.monstergirls.entity.SparrowHarpy;
import com.linkyoshi90.monstergirls.entity.SurinamToad;
import com.linkyoshi90.monstergirls.item.HarpyEggItem;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;

/** Gifts, sharing a bed with someone she trusts, eggs and spawn. Run with: ./gradlew runGameTestServer */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class RelationshipTests {

    /** A bed with its head at the position (relative to the test), made up for a player to lie in. */
    private static void bed(GameTestHelper helper, BlockPos head) {
        BlockState base = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH);
        helper.setBlock(head, base.setValue(BedBlock.PART, BedPart.HEAD));
        helper.setBlock(head.south(), base.setValue(BedBlock.PART, BedPart.FOOT));
    }

    private static void pool(GameTestHelper helper, int cx, int cz) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) helper.setBlock(new BlockPos(cx + dx, 2, cz + dz), Blocks.WATER);
        }
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "meatBuildsAffectionWithEveryone")
    public static void meatBuildsAffectionWithEveryone(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Bullywug frog = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(3, 2, 2));
        MucusToad toad = helper.spawn(ModEntities.MUCUS_TOAD.get(), new BlockPos(3, 2, 4));
        SparrowHarpy sparrow = helper.spawn(ModEntities.SPARROW_HARPY.get(), new BlockPos(5, 2, 2));
        for (net.minecraft.world.entity.Mob mob : List.of(frog, toad, sparrow)) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COOKED_BEEF, 2));
            mob.interact(player, InteractionHand.MAIN_HAND);
            check(((Bonded) mob).getAffection(player) >= 2, mob.getType() + " should like the meat: " + ((Bonded) mob).getAffection(player));
            check(player.getMainHandItem().getCount() == 1, "one piece of meat should be used up");
            // a second gift right away is not wanted (she is not hungry either)
            mob.interact(player, InteractionHand.MAIN_HAND);
            check(player.getMainHandItem().getCount() == 1, mob.getType() + " should refuse a second gift so soon");
        }
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "nightsPassExpectingOnlyWithTrust")
    public static void nightsPassExpectingOnlyWithTrust(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Bullywug trusting = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(3, 2, 2));
        Bullywug stranger = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(3, 2, 3));
        trusting.addAffection(player, Bonded.BED_AFFECTION);
        trusting.getGestation().setSharingWith(player.getUUID());
        player.startSleeping(helper.absolutePos(new BlockPos(2, 2, 2)));
        check(BedSharing.onNightSkipped(helper.getLevel()) == 1, "exactly one should be expecting");
        check(trusting.getGestation().isCarrying(), "the one who shared the bed is expecting");
        check(player.getUUID().equals(trusting.getGestation().getFather()), "and remembers who the father is");
        check(!stranger.getGestation().isCarrying(), "the one who stayed away is not");
        player.stopSleeping();
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 500, batch = "trustedMobComesToTheBed")
    public static void trustedMobComesToTheBed(GameTestHelper helper) {
        helper.setDayTime(24000 + 18000);   // midnight, the night after the full moon
        bed(helper, new BlockPos(2, 2, 2));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Bullywug friend = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(11, 2, 11));
        Bullywug stranger = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(11, 2, 8));
        friend.addAffection(player, Bonded.FULL_AFFECTION);   // certain to come (at BED_AFFECTION it is one night in three)
        helper.runAfterDelay(10, () -> {
            player.startSleeping(helper.absolutePos(new BlockPos(2, 2, 2)));
            helper.succeedWhen(() -> {
                check(player.isSleeping(), "the player should still be asleep");
                check(friend.distanceToSqr(player) < 6.25, "she should have hopped into the bed: " + Math.sqrt(friend.distanceToSqr(player)));
                check(Math.abs(friend.getY() - (helper.absolutePos(new BlockPos(2, 2, 2)).getY() + 0.5625)) < 0.1, "she should lie on the mattress: " + friend.getY());
                check(friend.getAction() == AbstractAnuran.Action.SITTING, "snug in the bed");
                check(player.getUUID().equals(friend.getGestation().getSharingWith()), "she should be sharing the bed");
                check(stranger.getGestation().getSharingWith() == null, "someone who doesn't trust you does not come into the bed");   // she may stroll past
                check(BedSharing.onNightSkipped(helper.getLevel()) == 1, "she should be expecting after the night");
                player.stopSleeping();
                HarpyTestUtil.remove(player);
                helper.setDayTime(1000);   // back to morning: later tests share this world's clock
            });
        });
    }

    /** The family girls lie down at night (SleepGoal), so she has to wake for the call. */
    @GameTest(template = PLATFORM, timeoutTicks = 500, batch = "callFromBedWakesASleepingGirl")
    public static void callFromBedWakesASleepingGirl(GameTestHelper helper) {
        helper.setDayTime(24000 + 18000);
        bed(helper, new BlockPos(2, 2, 2));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        com.linkyoshi90.monstergirls.entity.girl.Minotaur cow = com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn(helper,
                com.linkyoshi90.monstergirls.entity.girl.GirlSpecies.BISON_MINOTAUR, new BlockPos(7, 2, 7));
        cow.setMale(false);
        cow.addAffection(player, Bonded.FULL_AFFECTION);
        boolean[] called = {false};
        helper.succeedWhen(() -> {                                      // she lies down when her sleep goal comes round (not always by 60 ticks)
            if (!called[0]) {
                check(cow.getAction() == com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl.Action.LYING, "she should be asleep by now");
                called[0] = true;
                player.startSleeping(helper.absolutePos(new BlockPos(2, 2, 2)));
                BedCall.begin(player);
            }
            BedCall.tick(player);
            check(BedCall.stage(player) == BedCall.JOINED, "she should have woken and answered the call");
            player.stopSleeping();
            HarpyTestUtil.remove(player);
            helper.setDayTime(1000);
        });
    }

    /** On patrol she holds her post while they just sleep, and comes when they call. */
    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "callFromBedFetchesAGuard")
    public static void callFromBedFetchesAGuard(GameTestHelper helper) {
        helper.setDayTime(24000 + 18000);
        bed(helper, new BlockPos(2, 2, 2));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        com.linkyoshi90.monstergirls.entity.girl.Minotaur cow = com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn(helper,
                com.linkyoshi90.monstergirls.entity.girl.GirlSpecies.BISON_MINOTAUR, new BlockPos(7, 2, 7));
        cow.setMale(false);
        cow.addAffection(player, Bonded.FULL_AFFECTION);
        cow.getOrders().set(com.linkyoshi90.monstergirls.entity.Orders.Command.PATROL, player, cow.blockPosition());
        helper.runAfterDelay(10, () -> player.startSleeping(helper.absolutePos(new BlockPos(2, 2, 2))));
        helper.runAfterDelay(90, () -> {
            check(cow.getGestation().getSharingWith() == null, "not called, so she keeps her post");
            BedCall.begin(player);
            helper.succeedWhen(() -> {
                BedCall.tick(player);
                check(BedCall.stage(player) == BedCall.JOINED, "she should have answered the call");
                player.stopSleeping();
                HarpyTestUtil.remove(player);
                helper.setDayTime(1000);
            });
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 500, batch = "callFromBedWaitsForAFriend")
    public static void callFromBedWaitsForAFriend(GameTestHelper helper) {
        helper.setDayTime(24000 + 18000);   // midnight, the night after the full moon
        bed(helper, new BlockPos(2, 2, 2));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Bullywug friend = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(11, 2, 11));
        friend.addAffection(player, Bonded.FULL_AFFECTION);
        helper.runAfterDelay(10, () -> {
            player.startSleeping(helper.absolutePos(new BlockPos(2, 2, 2)));
            BedCall.begin(player);
            player.sleepCounter = 100;
            BedCall.tick(player);
            check(player.sleepCounter == 0, "the night should wait while the call is going");
            BedCall.skipNight(player);
            check(BedCall.stage(player) == BedCall.WAITING, "nobody has come yet, so there is nothing to skip to");
            helper.succeedWhen(() -> {
                BedCall.tick(player);
                check(BedCall.stage(player) == BedCall.JOINED, "she should have answered the call");
                BedCall.skipNight(player);
                check(!BedCall.isCalling(player), "the skip button hands over to the vanilla sleep");
                player.sleepCounter = 100;
                BedCall.tick(player);
                check(player.sleepCounter == 100, "nothing holds the night any more");
                player.stopSleeping();
                HarpyTestUtil.remove(player);
                helper.setDayTime(1000);   // back to morning: later tests share this world's clock
            });
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "leavingTheBedCancelsTheCall")
    public static void leavingTheBedCancelsTheCall(GameTestHelper helper) {
        bed(helper, new BlockPos(2, 2, 2));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        player.startSleeping(helper.absolutePos(new BlockPos(2, 2, 2)));
        BedCall.begin(player);
        check(BedCall.isCalling(player), "the call should be going");
        player.stopSleeping();
        BedCall.tick(player);
        check(!BedCall.isCalling(player), "leaving the bed ends the call");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 1200, batch = "fullMoonEveryoneTakesHerTurn")
    public static void fullMoonEveryoneTakesHerTurn(GameTestHelper helper) {
        helper.setDayTime(18000);   // midnight of day 0: a full moon
        check(BedSharing.fullMoon(helper.getLevel()), "day 0 should be a full moon");
        bed(helper, new BlockPos(2, 2, 2));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Bullywug first = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(6, 2, 6));
        Bullywug second = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(6, 2, 9));
        Bullywug stranger = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(9, 2, 9));
        first.addAffection(player, Bonded.BED_AFFECTION);    // only one night in three, but the moon calls them all
        second.addAffection(player, Bonded.BED_AFFECTION);
        boolean[] together = {false};
        helper.runAfterDelay(10, () -> {
            player.startSleeping(helper.absolutePos(new BlockPos(2, 2, 2)));
            helper.runAfterDelay(50, () -> {   // she looks around for a sleeper every couple of seconds
                check(first.isMoonstruck() && second.isMoonstruck(), "the moon should call both of them");
                check(!stranger.isMoonstruck(), "not someone who doesn't trust you");
                player.sleepCounter = 100;
                BedSharing.holdNight(player);
                check(player.sleepCounter == 99, "the night should wait for them");
            });
            helper.succeedWhen(() -> {
                if (player.getUUID().equals(first.getGestation().getSharingWith()) && player.getUUID().equals(second.getGestation().getSharingWith())) together[0] = true;
                check(!together[0], "they should take turns, not share the bed at once");
                check(player.isSleeping(), "the player should still be asleep");
                check(first.getGestation().isCarrying() && second.getGestation().isCarrying(), "both should have had their turn");
                check(!first.isMoonstruck() && !second.isMoonstruck(), "their eyes should be back to normal");
                check(first.getAction() == AbstractAnuran.Action.SITTING && second.getAction() == AbstractAnuran.Action.SITTING, "worn out, asleep by the bed");
                check(!stranger.getGestation().isCarrying(), "the stranger stays out of it");
                check(!BedSharing.nightWaits(player), "now the night can pass");
                player.sleepCounter = 100;
                BedSharing.holdNight(player);
                check(player.sleepCounter == 100, "nothing holds the night any more");
                check(BedSharing.onNightSkipped(helper.getLevel()) == 0, "no one is left in the bed at morning");
                player.stopSleeping();
                HarpyTestUtil.remove(player);
                helper.setDayTime(1000);   // back to morning: later tests share this world's clock
            });
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "expectingShowsInBonds")
    public static void expectingShowsInBonds(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        OwlHarpy owl = helper.spawn(ModEntities.OWL_HARPY.get(), new BlockPos(4, 2, 2));
        Bullywug frog = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(3, 2, 3));
        frog.getGestation().conceive();
        List<Component> lines = HarpyCommands.describeAll(player, 16);
        check(lines.size() == 2, "both should be listed: " + lines.size());
        check(lines.stream().anyMatch(c -> c.getString().contains("expecting")), "the expecting frog should be flagged");
        check(owl.getGestation().canConceive(), "an owl who hasn't slept beside anyone isn't");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "harpyBuildsANestWithAClutch")
    public static void harpyBuildsANestWithAClutch(GameTestHelper helper) {
        SparrowHarpy sparrow = helper.spawn(ModEntities.SPARROW_HARPY.get(), new BlockPos(7, 2, 7));
        java.util.UUID father = java.util.UUID.randomUUID();
        sparrow.getGestation().conceive(father);
        sparrow.getGestation().setTicksLeft(1);
        helper.succeedWhen(() -> {
            BlockPos nest = null;
            for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(1, 2, 1), new BlockPos(13, 3, 13))) {
                if (helper.getBlockState(pos).is(ModBlocks.HARPY_EGG.get())) nest = pos.immutable();
            }
            check(nest != null, "no nest yet");
            BlockState state = helper.getBlockState(nest);
            check(state.getValue(HarpyEggBlock.SPECIES) == HarpyEggBlock.Species.SPARROW, "wrong species egg");
            int eggs = state.getValue(HarpyEggBlock.EGGS);
            check(eggs >= 2 && eggs <= 4, "a clutch of two to four: " + eggs);
            check(father.equals(ParentageBlockEntity.fatherAt(helper.getLevel(), helper.absolutePos(nest))), "the nest should remember the father");
            check(helper.absolutePos(nest).equals(sparrow.getHomePos()), "the nest is her home now");
            check(!sparrow.getGestation().isCarrying() && !sparrow.getGestation().canConceive(), "she should be resting now");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "clutchHatchesDevotedDaughters")
    public static void clutchHatchesDevotedDaughters(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        BlockPos pos = new BlockPos(7, 2, 7);
        helper.setBlock(pos, ModBlocks.HARPY_EGG.get().defaultBlockState().setValue(HarpyEggBlock.SPECIES, HarpyEggBlock.Species.CROW)
                .setValue(HarpyEggBlock.EGGS, 3));
        ParentageBlockEntity.setFatherAt(helper.getLevel(), helper.absolutePos(pos), player.getUUID());
        HarpyEggBlock.hatch(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos));
        var chicks = helper.getLevel().getEntitiesOfClass(CrowHarpy.class, new net.minecraft.world.phys.AABB(helper.absolutePos(pos)).inflate(3.0));
        check(chicks.size() == 3, "one hatchling per egg: " + chicks.size());
        for (CrowHarpy chick : chicks) {
            check(chick.isBaby(), "hatchlings are small");
            check(chick.getAffection(player) == Bonded.FULL_AFFECTION, "devoted to her father: " + chick.getAffection(player));
            check(player.getUUID().equals(chick.getFather()), "she knows her father");
            check(chick.isOwnedBy(player), "a crow daughter is her father's companion");
            check(!chick.trustsEnoughForBed(player), "a daughter never shares her father's bed");
        }
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "spawnHatchesDevotedTadpoles")
    public static void spawnHatchesDevotedTadpoles(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        pool(helper, 7, 7);
        BlockPos spawn = new BlockPos(7, 3, 7);
        helper.setBlock(spawn, ModBlocks.ANURAN_SPAWN.get().defaultBlockState().setValue(AnuranSpawnBlock.SPECIES, AnuranSpawnBlock.Species.MUCUS_TOAD));
        ParentageBlockEntity.setFatherAt(helper.getLevel(), helper.absolutePos(spawn), player.getUUID());
        AnuranSpawnBlock.hatch(helper.getLevel(), helper.absolutePos(spawn), helper.getBlockState(spawn));
        var young = helper.getLevel().getEntitiesOfClass(AbstractAnuran.class, new net.minecraft.world.phys.AABB(helper.absolutePos(spawn)).inflate(4.0));
        check(!young.isEmpty(), "tadpoles should have hatched");
        for (AbstractAnuran a : young) {
            check(a.isTadpole(), "they start as tadpoles");
            check(a.getAffection(player) == Bonded.FULL_AFFECTION, "devoted to their father");
        }
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "bellySwellsAsGestationGoesOn")
    public static void bellySwellsAsGestationGoesOn(GameTestHelper helper) {
        SparrowHarpy sparrow = helper.spawn(ModEntities.SPARROW_HARPY.get(), new BlockPos(7, 2, 7));
        Bullywug frog = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(3, 2, 3));
        check(sparrow.getBellySwell() == 0.0F && frog.getBellySwell() == 0.0F, "not expecting, no belly");
        sparrow.getGestation().conceive();
        sparrow.getGestation().setTicksLeft(Gestation.DURATION / 2);
        frog.getGestation().conceive();
        frog.getGestation().setTicksLeft(Gestation.DURATION / 4);
        helper.runAfterDelay(25, () -> {
            check(Math.abs(sparrow.getBellySwell() - 0.5F) < 0.05F, "half way: " + sparrow.getBellySwell());
            check(Math.abs(frog.getBellySwell() - 0.75F) < 0.05F, "three quarters: " + frog.getBellySwell());
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "bedChanceGrowsWithAffection")
    public static void bedChanceGrowsWithAffection(GameTestHelper helper) {
        check(Bonded.bedChance(Bonded.BED_AFFECTION - 1) == 0.0F, "no chance below the threshold");
        check(Bonded.bedChance(Bonded.BED_AFFECTION) > 0.3F && Bonded.bedChance(Bonded.BED_AFFECTION) < 0.4F, "one in three at the threshold");
        check(Bonded.bedChance(60) > Bonded.bedChance(40), "fonder, likelier");
        check(Bonded.bedChance(Bonded.FULL_AFFECTION) == 1.0F, "certain at full affection");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "eggHatchesIntoAGrowingHatchling")
    public static void eggHatchesIntoAGrowingHatchling(GameTestHelper helper) {
        BlockPos pos = new BlockPos(7, 2, 7);
        helper.setBlock(pos, ModBlocks.HARPY_EGG.get().defaultBlockState().setValue(HarpyEggBlock.SPECIES, HarpyEggBlock.Species.OWL));
        HarpyEggBlock.hatch(helper.getLevel(), helper.absolutePos(pos), helper.getBlockState(pos));
        check(helper.getBlockState(pos).isAir(), "the egg should be gone");
        var chicks = helper.getLevel().getEntitiesOfClass(OwlHarpy.class, new net.minecraft.world.phys.AABB(helper.absolutePos(pos)).inflate(3.0));
        check(chicks.size() == 1 && chicks.get(0).isBaby(), "one baby owl harpy should have hatched");
        OwlHarpy chick = chicks.get(0);
        check(chick.getBbHeight() < 1.0F, "a hatchling is small: " + chick.getBbHeight());
        check(!chick.canShareBed(), "a hatchling is not a partner");
        chick.setGrowTicks(AbstractHarpy.GROW_TICKS - 1);
        helper.succeedWhen(() -> {
            check(!chick.isBaby(), "she should have grown up");
            check(chick.getBbHeight() > 1.5F, "grown up: " + chick.getBbHeight());
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "eggItemKeepsItsSpecies")
    public static void eggItemKeepsItsSpecies(GameTestHelper helper) {
        for (HarpyEggBlock.Species species : HarpyEggBlock.Species.values()) {
            ItemStack stack = HarpyEggItem.of(ModBlocks.HARPY_EGG.get(), species);
            check(HarpyEggItem.speciesOf(stack) == species, "species lost for " + species);
            check(stack.is(ModItems.HARPY_EGG.get()), "wrong item");
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 1000, batch = "expectingFrogLaysSpawnInStillWater")
    public static void expectingFrogLaysSpawnInStillWater(GameTestHelper helper) {
        pool(helper, 11, 11);
        var level = helper.getLevel();
        check(!AnuranSpawnBlock.isStillWaterSource(level, helper.absolutePos(new BlockPos(10, 2, 11))),
                "the pool's edge has open air beside it: not still enough");
        Bullywug frog = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(3, 2, 3));
        frog.getGestation().conceive();
        frog.getGestation().setTicksLeft(0);
        helper.succeedWhen(() -> {
            BlockState spawn = helper.getBlockState(new BlockPos(11, 3, 11));
            check(spawn.is(ModBlocks.ANURAN_SPAWN.get()), "no spawn yet");
            check(spawn.getValue(AnuranSpawnBlock.SPECIES) == AnuranSpawnBlock.Species.BULLYWUG, "wrong species");
            check(!frog.getGestation().isCarrying(), "she has laid");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "noStillWaterNoSpawn")
    public static void noStillWaterNoSpawn(GameTestHelper helper) {   // no water at all, in fact
        MucusToad toad = helper.spawn(ModEntities.MUCUS_TOAD.get(), new BlockPos(3, 2, 3));
        toad.getGestation().conceive();
        toad.getGestation().setTicksLeft(0);
        helper.runAfterDelay(150, () -> {
            check(toad.getGestation().isCarrying(), "with no water she keeps carrying");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "spawnHatchesIntoSmallFrogs")
    public static void spawnHatchesIntoSmallFrogs(GameTestHelper helper) {
        pool(helper, 7, 7);
        BlockPos spawn = new BlockPos(7, 3, 7);
        helper.setBlock(spawn, ModBlocks.ANURAN_SPAWN.get().defaultBlockState().setValue(AnuranSpawnBlock.SPECIES, AnuranSpawnBlock.Species.CANE_TOAD));
        check(helper.getBlockState(spawn).is(ModBlocks.ANURAN_SPAWN.get()), "spawn should float on the pool");
        AnuranSpawnBlock.hatch(helper.getLevel(), helper.absolutePos(spawn), helper.getBlockState(spawn));
        var young = helper.getLevel().getEntitiesOfClass(AbstractAnuran.class, new net.minecraft.world.phys.AABB(helper.absolutePos(spawn)).inflate(4.0));
        check(young.size() >= 2 && young.size() <= 4, "a litter of two to four, got " + young.size());
        for (AbstractAnuran a : young) {
            check(a.isBaby() && a.getType() == ModEntities.CANE_TOAD.get(), "hatchlings of her species, small");
            check(!a.canShareBed(), "hatchlings are not partners");
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "surinamToadNeverGoesToBed")
    public static void surinamToadNeverGoesToBed(GameTestHelper helper) {
        SurinamToad toad = helper.spawn(ModEntities.SURINAM_TOAD.get(), new BlockPos(7, 2, 7));
        check(!toad.canShareBed(), "the Surinam toad lives in the water");
        CompoundTag tag = new CompoundTag();
        toad.getGestation().conceive();
        toad.addAdditionalSaveData(tag);
        Bullywug frog = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(3, 2, 3));
        frog.getGestation().conceive();
        frog.getGestation().setTicksLeft(500);
        CompoundTag frogTag = new CompoundTag();
        frog.saveWithoutId(frogTag);
        Bullywug copy = ModEntities.BULLYWUG.get().create(helper.getLevel());
        copy.load(frogTag);
        check(copy.getGestation().isCarrying() && copy.getGestation().getTicksLeft() == 500, "gestation lost when saving");
        helper.succeed();
    }
}
