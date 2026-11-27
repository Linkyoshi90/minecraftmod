package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Instincts;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.OwlHarpy;
import com.linkyoshi90.harmontide.entity.PenguinHarpy;
import com.linkyoshi90.harmontide.entity.Traversal;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Dragon;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.interact;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Little instincts (entity/Instincts) and the dragons' catch (entity/Traversal). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class InstinctsTests {

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "minotaurPointsTheWayHome")
    public static void minotaurPointsTheWayHome(GameTestHelper helper) {
        check(Instincts.bearing(Vec3.ZERO, new Vec3(0, 0, 10)).equals("south"), "+z is south");
        check(Instincts.bearing(Vec3.ZERO, new Vec3(10, 0, 0)).equals("east"), "+x is east");
        check(Instincts.bearing(Vec3.ZERO, new Vec3(-10, 0, -10)).equals("north_west"), "-x -z is north-west");
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        AbstractMonsterGirl cow = spawn(helper, GirlSpecies.CATTLE_MINOTAUR, new BlockPos(6, 2, 5));
        check(Instincts.labyrinthSense(cow, p, new ItemStack(Items.COMPASS)) == null, "a stranger shows nothing");
        cow.setAffection(p.getUUID(), Orders.AFFECTION);
        check(Instincts.labyrinthSense(cow, p, new ItemStack(Items.COMPASS)) != null, "a friend points the way");
        check(Instincts.labyrinthSense(cow, p, ItemStack.EMPTY) == null, "only for a compass");
        interact(cow, p, new ItemStack(Items.COMPASS));
        check(p.getMainHandItem().is(Items.COMPASS), "the compass stays in hand");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 80, batch = "catGirlGetsTheZoomies")
    public static void catGirlGetsTheZoomies(GameTestHelper helper) {
        AbstractMonsterGirl cat = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(7, 2, 7));
        check(Instincts.zoomies(helper.getLevel(), cat, true), "off she goes");
        check(cat.getPersistentData().getLong("ZoomiesUntil") > helper.getLevel().getGameTime(), "for a few seconds");
        helper.succeedWhen(() -> check(cat.position().distanceTo(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(7, 2, 7)))) > 1.5, "tearing about"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "owlLendsHerNightEyes")
    public static void owlLendsHerNightEyes(GameTestHelper helper) {
        helper.setNight();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        OwlHarpy owl = helper.spawn(ModEntities.OWL_HARPY.get(), new BlockPos(7, 2, 7));
        helper.runAfterDelay(5, () -> {                                     // (the sky darkens on the next tick)
            check(!Instincts.owlEyes(helper.getLevel(), owl), "not for a stranger");
            owl.setAffection(p.getUUID(), Orders.AFFECTION);
            check(Instincts.owlEyes(helper.getLevel(), owl) && p.hasEffect(MobEffects.NIGHT_VISION), "her friend sees in the dark");
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "penguinSlidesOnIce")
    public static void penguinSlidesOnIce(GameTestHelper helper) {
        for (int x = 3; x <= 11; x++) for (int z = 3; z <= 11; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.PACKED_ICE);
        PenguinHarpy penguin = helper.spawn(ModEntities.PENGUIN_HARPY.get(), new BlockPos(7, 2, 7));
        helper.succeedWhen(() -> check(penguin.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(Instincts.SLIDE), "on her belly on the ice"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "gryphonCatchesAFallingRider")
    public static void gryphonCatchesAFallingRider(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 5, 5));
        Dragon gryphon = spawn(helper, GirlSpecies.GRYPHON_DRAGON, new BlockPos(10, 2, 10));
        gryphon.setAffection(p.getUUID(), gryphon.rideNeed());
        p.fallDistance = 9.0F;
        check(Traversal.catchFalling(p) == gryphon, "she swoops under them");
        check(p.getVehicle() == gryphon, "on her back");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "kitsuneVeilsAHurtFriend")
    public static void kitsuneVeilsAHurtFriend(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        AbstractMonsterGirl fox = spawn(helper, GirlSpecies.RED_KITSUNE, new BlockPos(7, 2, 5));
        fox.setAffection(p.getUUID(), 25);
        var zombie = helper.spawn(net.minecraft.world.entity.EntityType.SKELETON, new BlockPos(3, 2, 5));
        check(com.linkyoshi90.harmontide.entity.Guardians.veil(p) == null, "a healthy friend needs no veil");
        p.setHealth(5.0F);
        zombie.setTarget(p);
        check(com.linkyoshi90.harmontide.entity.Guardians.veil(p) == fox, "badly hurt: she veils them");
        check(p.hasEffect(MobEffects.INVISIBILITY) && zombie.getTarget() == null, "unseen, and lost to what hunted them");
        check(com.linkyoshi90.harmontide.entity.Guardians.veil(p) == null, "once a minute");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "mummyBindsWounds")
    public static void mummyBindsWounds(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        AbstractMonsterGirl mummy = spawn(helper, GirlSpecies.MUMMY_UNDEAD, new BlockPos(7, 2, 5));
        mummy.setAffection(p.getUUID(), Orders.AFFECTION);
        p.setHealth(9.0F);
        check(com.linkyoshi90.harmontide.entity.Guardians.bandage(p) == mummy && p.hasEffect(MobEffects.REGENERATION), "her bandages");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "batKnowsTheWayOut")
    public static void batKnowsTheWayOut(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        AbstractMonsterGirl bat = spawn(helper, GirlSpecies.CAVE_BAT, new BlockPos(7, 2, 5));
        check(Instincts.batGuide(bat, p, new ItemStack(Items.TORCH)) == null, "a stranger keeps her echoes to herself");
        bat.setAffection(p.getUUID(), Orders.AFFECTION);
        check(Instincts.batGuide(bat, p, new ItemStack(Items.TORCH)) != null, "a friend shows the way");
        check(Instincts.batGuide(bat, p, ItemStack.EMPTY) == null, "only for a torch");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "boarRootsUpAFind")
    public static void boarRootsUpAFind(GameTestHelper helper) {
        AbstractMonsterGirl boar = spawn(helper, GirlSpecies.PIGLETTE_ARMOURED, new BlockPos(7, 2, 7));
        helper.runAfterDelay(10, () -> {                                   // (on her feet on the grass first)
            check(Instincts.root(helper.getLevel(), boar, true).isEmpty(), "a wild boar roots for herself");
            boar.setAffection(java.util.UUID.randomUUID(), 5);
            check(!Instincts.root(helper.getLevel(), boar, true).isEmpty(), "a friendly one turns something up");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "anteaterEatsSilverfish")
    public static void anteaterEatsSilverfish(GameTestHelper helper) {
        AbstractMonsterGirl anteater = spawn(helper, GirlSpecies.ANTEATER_ARMOURED, new BlockPos(7, 2, 7));
        anteater.setAffection(java.util.UUID.randomUUID(), 5);
        var bug = helper.spawn(net.minecraft.world.entity.EntityType.SILVERFISH, new BlockPos(9, 2, 7));
        check(Instincts.anteater(helper.getLevel(), anteater) == 1, "she snaps it up");
        helper.succeedWhen(() -> check(!bug.isAlive(), "gone"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "giraffeShakesDownAnApple")
    public static void giraffeShakesDownAnApple(GameTestHelper helper) {
        AbstractMonsterGirl giraffe = spawn(helper, GirlSpecies.GIRAFFE_CENTAUR, new BlockPos(7, 2, 7));
        giraffe.setAffection(java.util.UUID.randomUUID(), 5);
        giraffe.setNoAi(true);
        BlockPos head = BlockPos.containing(giraffe.getX(), giraffe.getEyeY(), giraffe.getZ());
        helper.getLevel().setBlockAndUpdate(head.above(), net.minecraft.world.level.block.Blocks.OAK_LEAVES.defaultBlockState()
                .setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true));
        check(Instincts.browse(helper.getLevel(), giraffe, true).is(Items.APPLE), "an apple from the oak");
        helper.succeed();
    }
}
