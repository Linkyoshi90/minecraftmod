package com.linkyoshi90.harpies.gametest;

import com.linkyoshi90.harpies.HarpiesMod;
import com.linkyoshi90.harpies.entity.AbstractHarpy;
import com.linkyoshi90.harpies.entity.OstrichHarpy;
import com.linkyoshi90.harpies.entity.ai.GlareGoal;
import com.linkyoshi90.harpies.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harpies.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harpies.gametest.HarpyTestUtil.check;

/** Run with: ./gradlew runGameTestServer. Floor is at relative y=1. */
@GameTestHolder(HarpiesMod.MODID)
@PrefixGameTestTemplate(false)
public class OstrichHarpyTests {
    private static final BlockPos NEST = new BlockPos(7, 2, 7);

    private static OstrichHarpy ostrichWithNest(GameTestHelper helper) {
        OstrichHarpy ostrich = helper.spawn(ModEntities.OSTRICH_HARPY.get(), NEST);
        ostrich.setHomePos(helper.absolutePos(NEST));
        return ostrich;
    }

    private static void feed(OstrichHarpy ostrich, ServerPlayer player, int times) {
        for (int i = 0; i < times; i++) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT));
            ostrich.interact(player, InteractionHand.MAIN_HAND);
        }
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "ostrichWarnsThenChargesIntruders")
    public static void warnsThenChargesIntruders(GameTestHelper helper) {
        OstrichHarpy ostrich = ostrichWithNest(helper);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 11));
        boolean[] displayed = {false};
        helper.onEachTick(() -> {
            if (ostrich.getAction() == AbstractHarpy.Action.DISPLAYING) {
                displayed[0] = true;
                check(ostrich.getTarget() == null, "should warn before charging");
            }
        });
        helper.succeedWhen(() -> {
            check(displayed[0], "no warning display");
            check(player.getHealth() < player.getMaxHealth(), "intruder hasn't been kicked yet");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "ostrichToleratesCarefulSneaking")
    public static void toleratesCarefulSneaking(GameTestHelper helper) {
        OstrichHarpy ostrich = ostrichWithNest(helper);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 14));
        player.setShiftKeyDown(true);
        helper.onEachTick(() -> {
            check(ostrich.getAction() != AbstractHarpy.Action.DISPLAYING && ostrich.getTarget() == null,
                    "a sneaking player at a respectful distance shouldn't be threatened");
            player.setShiftKeyDown(true);
        });
        helper.runAtTickTime(250, () -> {
            HarpyTestUtil.remove(player);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "ostrichTrustStopsTheWarnings")
    public static void trustStopsTheWarnings(GameTestHelper helper) {
        OstrichHarpy ostrich = ostrichWithNest(helper);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 10));
        feed(ostrich, player, OstrichHarpy.TRUSTED);
        check(ostrich.isTrusted(player), "five treats should earn her trust");
        helper.onEachTick(() -> check(ostrich.getAction() != AbstractHarpy.Action.DISPLAYING && ostrich.getTarget() == null,
                "threatened someone she trusts"));
        helper.runAtTickTime(250, () -> {
            HarpyTestUtil.remove(player);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "ostrichLoyalCompanionFollows")
    public static void loyalCompanionFollows(GameTestHelper helper) {
        OstrichHarpy ostrich = ostrichWithNest(helper);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 9));
        feed(ostrich, player, OstrichHarpy.LOYAL);
        check(ostrich.isLoyalTo(player), "should be loyal after enough treats");
        // She only sets off once her companion is more than 10 blocks away: put them in opposite corners.
        var far = helper.absoluteVec(new BlockPos(13, 2, 13).getBottomCenter());
        player.teleportTo(far.x, far.y, far.z);
        var corner = helper.absoluteVec(new BlockPos(1, 2, 1).getBottomCenter());
        ostrich.teleportTo(corner.x, corner.y, corner.z);
        helper.succeedWhen(() -> {
            check(ostrich.distanceToSqr(player) < 4.0 * 4.0, "not following her companion");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "ostrichDrivesMonstersFromTheNest")
    public static void drivesMonstersFromTheNest(GameTestHelper helper) {
        OstrichHarpy ostrich = ostrichWithNest(helper);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(11, 2, 7));
        zombie.setNoAi(true);
        helper.succeedWhen(() -> check(zombie.getLastHurtByMob() == ostrich, "hasn't kicked the zombie yet"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 900, batch = "ostrichBroodsAtNight")
    public static void broodsOnTheNestAtNight(GameTestHelper helper) {
        helper.setNight();
        OstrichHarpy ostrich = helper.spawn(ModEntities.OSTRICH_HARPY.get(), new BlockPos(3, 2, 3));
        ostrich.setHomePos(helper.absolutePos(NEST));
        helper.succeedWhen(() -> {
            check(ostrich.getAction() == AbstractHarpy.Action.BROODING, "not brooding");
            check(ostrich.distanceToSqr(helper.absoluteVec(NEST.getBottomCenter())) < 1.5 * 1.5, "brooding away from the nest");
        });
    }

    @GameTest(template = PLATFORM, batch = "ostrichTrustAndNestSurviveSaving")
    public static void trustAndNestSurviveSaving(GameTestHelper helper) {
        OstrichHarpy ostrich = ostrichWithNest(helper);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 9));
        feed(ostrich, player, 3);
        ostrich.setVariant(OstrichHarpy.Variant.CASSOWARY);
        ostrich.setSaddled(true);
        var tag = new net.minecraft.nbt.CompoundTag();
        ostrich.saveWithoutId(tag);
        OstrichHarpy copy = ModEntities.OSTRICH_HARPY.get().create(helper.getLevel());
        copy.load(tag);
        check(copy.getAffection(player) == 3, "affection lost: " + copy.getAffection(player));
        check(copy.getVariant() == OstrichHarpy.Variant.CASSOWARY, "variant lost");
        check(copy.isSaddled(), "saddle lost");
        check(helper.absolutePos(NEST).equals(copy.getHomePos()) && copy.hasRestriction(), "nest/patrol area lost");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 1500, batch = "ostrichKicksAFewTimesThenGlares")
    public static void kicksAFewTimesThenGlares(GameTestHelper helper) {
        OstrichHarpy ostrich = ostrichWithNest(helper);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 10));
        int[] kicks = {0};
        float[] lastHealth = {player.getHealth()};
        boolean[] glared = {false};
        helper.onEachTick(() -> {
            // Stand your ground: the player keeps getting put back inside her territory.
            if (helper.getTick() % 20 == 0) {
                var spot = helper.absoluteVec(new BlockPos(7, 2, 10).getBottomCenter());
                player.teleportTo(spot.x, spot.y, spot.z);
            }
            if (player.getHealth() < lastHealth[0]) kicks[0]++;
            lastHealth[0] = player.getHealth();
            check(player.getHealth() >= 4.0F, "kicked the player below two hearts: " + player.getHealth());
            check(kicks[0] <= OstrichHarpy.MAX_KICKS, "kicked more than " + OstrichHarpy.MAX_KICKS + " times");
            glared[0] |= ostrich.goalSelector.getAvailableGoals().stream()
                    .anyMatch(g -> g.isRunning() && g.getGoal() instanceof GlareGoal);
        });
        helper.runAtTickTime(1400, () -> {
            check(kicks[0] >= 1, "never kicked the trespasser");
            check(glared[0] && ostrich.getTarget() == null, "should end up glaring instead of attacking");
            HarpyTestUtil.remove(player);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "ostrichNeverKicksAWeakPlayerToDeath")
    public static void neverKicksAWeakPlayerToDeath(GameTestHelper helper) {
        OstrichHarpy ostrich = ostrichWithNest(helper);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 9));
        player.setHealth(5.0F);
        ostrich.setTarget(player);
        helper.onEachTick(() -> {
            if (player.isRemoved()) return; // removed at the end of the test
            check(player.isAlive() && player.getHealth() >= 4.0F, "a weak player was hurt below two hearts: " + player.getHealth());
        });
        helper.runAtTickTime(300, () -> {
            check(ostrich.isSpared(player), "she should give up on a player she can't kick safely");
            HarpyTestUtil.remove(player);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "ostrichPettingCalmsHer")
    public static void pettingCalmsHer(GameTestHelper helper) {
        OstrichHarpy ostrich = ostrichWithNest(helper);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 9));
        ostrich.setTarget(player);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        ostrich.interact(player, InteractionHand.MAIN_HAND);
        check(ostrich.getTarget() == null && ostrich.isSpared(player), "petting should calm her");
        helper.runAtTickTime(200, () -> {
            check(ostrich.getTarget() == null, "charged again right after being petted");
            HarpyTestUtil.remove(player);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, batch = "ostrichEatsAnythingLovesGreens")
    public static void eatsAnythingButLovesGreens(GameTestHelper helper) {
        OstrichHarpy ostrich = ostrichWithNest(helper);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 9));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COOKED_BEEF));
        ostrich.interact(player, InteractionHand.MAIN_HAND);
        check(ostrich.getAffection(player) == 1 && player.getMainHandItem().isEmpty(), "an omnivore should take meat too");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHORT_GRASS));
        ostrich.interact(player, InteractionHand.MAIN_HAND);
        check(ostrich.getAffection(player) == 3, "leafy greens should count double, got " + ostrich.getAffection(player));
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "ostrichTerritoryMovesWithHerCompanion")
    public static void territoryMovesWithHerCompanion(GameTestHelper helper) {
        OstrichHarpy ostrich = ostrichWithNest(helper);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(13, 2, 13));
        feed(ostrich, player, OstrichHarpy.LOYAL);
        var corner = helper.absoluteVec(new BlockPos(1, 2, 1).getBottomCenter());
        ostrich.teleportTo(corner.x, corner.y, corner.z);
        // A monster right next to her companion, far from where she hatched: still her territory now.
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(12, 2, 13));
        zombie.setNoAi(true);
        check(ostrich.isInTerritory(zombie), "her territory should be wherever her companion is");
        helper.succeedWhen(() -> {
            check(zombie.getLastHurtByMob() == ostrich, "hasn't driven the monster off her companion yet");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "ostrichAttacksWhatHerCompanionAttacks")
    public static void attacksWhatHerCompanionAttacks(GameTestHelper helper) {
        OstrichHarpy ostrich = ostrichWithNest(helper);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 9));
        feed(ostrich, player, OstrichHarpy.LOYAL);
        var cow = helper.spawn(EntityType.COW, new BlockPos(12, 2, 12));
        cow.setNoAi(true);
        player.attack(cow);
        helper.succeedWhen(() -> {
            check(cow.getLastHurtByMob() == ostrich, "didn't join in on her companion's attack");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, batch = "ostrichCanBeSaddledAndRidden")
    public static void canBeSaddledAndRidden(GameTestHelper helper) {
        OstrichHarpy ostrich = ostrichWithNest(helper);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 9));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SADDLE));
        ostrich.interact(player, InteractionHand.MAIN_HAND);
        check(!ostrich.isSaddled(), "only her loyal companion may saddle her");
        feed(ostrich, player, OstrichHarpy.LOYAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SADDLE));
        ostrich.interact(player, InteractionHand.MAIN_HAND);
        check(ostrich.isSaddled() && player.getMainHandItem().isEmpty(), "saddling failed");
        ostrich.interact(player, InteractionHand.MAIN_HAND);
        check(player.getVehicle() == ostrich && ostrich.getControllingPassenger() == player, "couldn't ride her");
        check(ostrich.canSprint(), "rider should be able to sprint");
        player.stopRiding();
        HarpyTestUtil.remove(player);
        helper.succeed();
    }
}
