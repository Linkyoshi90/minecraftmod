package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.OwlHarpy;
import com.linkyoshi90.harmontide.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;

/** Run with: ./gradlew runGameTestServer. Floor is at relative y=1. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class OwlHarpyTests {

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "owlSleepsByDay")
    public static void sleepsByDay(GameTestHelper helper) {
        helper.setDayTime(1000);
        OwlHarpy owl = helper.spawn(ModEntities.OWL_HARPY.get(), new BlockPos(7, 2, 7));
        helper.succeedWhen(() -> check(owl.isSleeping(), "not asleep in broad daylight"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 1200, batch = "owlHuntsLivestockAtNight")
    public static void huntsLivestockAtNight(GameTestHelper helper) {
        helper.setNight();
        OwlHarpy owl = helper.spawn(ModEntities.OWL_HARPY.get(), new BlockPos(3, 2, 3));
        var cow = helper.spawn(EntityType.COW, new BlockPos(11, 2, 11));
        cow.setNoAi(true);
        helper.succeedWhen(() -> check(cow.getLastHurtByMob() == owl, "hasn't gone after the cow"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 900, batch = "owlSparesAFriendsAnimals")
    public static void sparesAFriendsAnimals(GameTestHelper helper) {
        helper.setNight();
        OwlHarpy owl = helper.spawn(ModEntities.OWL_HARPY.get(), new BlockPos(3, 2, 3));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(12, 2, 12));
        for (int i = 0; i < OwlHarpy.FRIENDLY; i++) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COOKED_BEEF));
            owl.interact(player, InteractionHand.MAIN_HAND);
        }
        var cow = helper.spawn(EntityType.COW, new BlockPos(11, 2, 11));
        cow.setNoAi(true);
        helper.onEachTick(() -> check(cow.getLastHurtByMob() != owl, "hunted her friend's cow"));
        helper.runAtTickTime(800, () -> {
            HarpyTestUtil.remove(player);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 1200, batch = "owlMeatLuresHerOut")
    public static void meatLuresHerOut(GameTestHelper helper) {
        helper.setDayTime(1000);
        OwlHarpy owl = helper.spawn(ModEntities.OWL_HARPY.get(), new BlockPos(3, 2, 3));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 7)); // within food-tempting range (10)
        helper.runAfterDelay(100, () -> {
            // Walk up to wherever she settled to sleep (food only tempts within 10 blocks), then hold out the meat.
            var near = owl.position().add(owl.position().subtract(helper.absoluteVec(new net.minecraft.world.phys.Vec3(7.5, 2, 7.5)))
                    .multiply(-1, 0, -1).normalize().scale(5));
            player.teleportTo(near.x, owl.getY(), near.z);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COOKED_CHICKEN));
        });
        helper.succeedWhen(() -> {
            check(!owl.isSleeping() && owl.distanceToSqr(player) < 3.0 * 3.0, "didn't come for the meat");
            HarpyTestUtil.remove(player);
        });
    }
}
