package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Canid;
import com.linkyoshi90.harmontide.entity.girl.Devil;
import com.linkyoshi90.harmontide.entity.girl.Dragon;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Lamia;
import com.linkyoshi90.harmontide.entity.girl.Lizardfolk;
import com.linkyoshi90.harmontide.entity.girl.Merfolk;
import com.linkyoshi90.harmontide.entity.girl.Nekomata;
import com.linkyoshi90.harmontide.entity.girl.Rodent;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/**
 * The twenty-second Harmontide wave (HARMONTIDE_CONCEPTS.md): macawtl, piranha, mosasaurus, fishman, gibbon, gigantopithecus,
 * sandworm, archelon, and Xiangliu, the cyclops, the genie and Ammit; the paints and the dullahan's homage props that came with it.
 */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class TwentySecondWaveTests {

    /** A friend for her: a survival player with enough friendship to count. */
    private static ServerPlayer friendOf(GameTestHelper helper, AbstractMonsterGirl girl, BlockPos at) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, at);
        girl.addAffection(p, Orders.AFFECTION);
        return p;
    }

    private static <T extends AbstractMonsterGirl> T still(GameTestHelper helper, GirlSpecies s, BlockPos at) {
        T girl = spawn(helper, s, at);
        girl.setNoAi(true);
        return girl;
    }

    private static Zombie zombie(GameTestHelper helper, BlockPos at) {
        Zombie z = helper.spawn(EntityType.ZOMBIE, at);
        z.setNoAi(true);
        return z;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "macawtlTailwind")
    public static void macawtlTailwind(GameTestHelper helper) {
        Dragon macaw = still(helper, GirlSpecies.MACAWTL_DRAGON, new BlockPos(1, 2, 1));
        check(macaw.tailwind(helper.getLevel()) == -1, "no friend, no wind");
        ServerPlayer p = friendOf(helper, macaw, new BlockPos(3, 2, 3));
        int r = macaw.tailwind(helper.getLevel());
        check(r == (helper.getLevel().canSeeSky(p.blockPosition().above()) ? 1 : 0), "Speed only under the open sky");
        if (r == 1) check(p.hasEffect(MobEffects.MOVEMENT_SPEED), "and her friend runs faster");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "piranhaFrenzy")
    public static void piranhaFrenzy(GameTestHelper helper) {
        Merfolk piranha = still(helper, GirlSpecies.PIRANHA_MERMAID, new BlockPos(1, 2, 1));
        check(piranha.frenzy(helper.getLevel()) == -1, "no friend, no frenzy");
        ServerPlayer p = friendOf(helper, piranha, new BlockPos(3, 2, 3));
        helper.setBlock(new BlockPos(5, 2, 5), Blocks.WATER);
        Zombie z = zombie(helper, new BlockPos(5, 2, 5));
        z.baseTick();                                     // notice the water
        check(piranha.frenzy(helper.getLevel()) == 0, "a healthy zombie is left alone");
        z.setHealth(z.getMaxHealth() / 3);
        float before = z.getHealth();
        check(piranha.frenzy(helper.getLevel()) == 1, "a badly hurt one in the water is bitten");
        check(z.getHealth() < before, "and loses more");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "mosasaurusTowsTheBoat")
    public static void mosasaurusTowsTheBoat(GameTestHelper helper) {
        Merfolk mosa = still(helper, GirlSpecies.MOSASAURUS_MERMAID, new BlockPos(1, 2, 1));
        check(mosa.tow(helper.getLevel()) == -1, "no friend, nothing to tow");
        ServerPlayer p = friendOf(helper, mosa, new BlockPos(3, 2, 3));
        check(mosa.tow(helper.getLevel()) == 0, "not in a boat");
        Boat boat = helper.spawn(EntityType.BOAT, new BlockPos(3, 2, 3));
        boat.setDeltaMovement(Vec3.ZERO);
        p.startRiding(boat, true);
        check(mosa.tow(helper.getLevel()) == 1, "she tows the boat");
        check(boat.getDeltaMovement().horizontalDistanceSqr() > 0.001, "and it moves");
        p.stopRiding();
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "fishmanDisarmsTheDrowned")
    public static void fishmanDisarmsTheDrowned(GameTestHelper helper) {
        Merfolk fish = still(helper, GirlSpecies.FISHMAN_MERMAID, new BlockPos(1, 2, 1));
        check(fish.disarm(helper.getLevel()) == -1, "no friend");
        ServerPlayer p = friendOf(helper, fish, new BlockPos(3, 2, 3));
        Drowned d = helper.spawn(EntityType.DROWNED, new BlockPos(5, 2, 5));
        d.setNoAi(true);
        d.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        check(fish.disarm(helper.getLevel()) == 0, "an unarmed drowned is no matter");
        d.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
        check(fish.disarm(helper.getLevel()) == 1, "she wrenches the trident away");
        check(d.getMainHandItem().isEmpty(), "and the drowned is empty-handed");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "gibbonClimbsFast")
    public static void gibbonClimbsFast(GameTestHelper helper) {
        Rodent gibbon = still(helper, GirlSpecies.GIBBON_RODENT, new BlockPos(1, 2, 1));
        check(gibbon.brachiation(helper.getLevel()) == -1, "no friend");
        ServerPlayer p = friendOf(helper, gibbon, new BlockPos(3, 2, 3));
        p.setDeltaMovement(0, 0.12, 0);
        check(gibbon.brachiation(helper.getLevel()) == 0, "not on a ladder");
        helper.setBlock(new BlockPos(3, 2, 4), Blocks.STONE);
        helper.setBlock(new BlockPos(3, 2, 3), Blocks.LADDER);
        p.doTick();                                       // the test player isn't ticked by a connection: notice the ladder
        p.setDeltaMovement(0, 0.12, 0);
        check(p.onClimbable(), "her friend is on the ladder");
        check(gibbon.brachiation(helper.getLevel()) == 1, "she helps them up");
        check(p.getDeltaMovement().y >= 0.3, "twice as fast");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "gigantopithecusGroundPound")
    public static void gigantopithecusGroundPound(GameTestHelper helper) {
        Canid ape = still(helper, GirlSpecies.GIGANTOPITHECUS_CANID, new BlockPos(1, 2, 1));
        check(ape.groundPound(helper.getLevel()) == -1, "no friend");
        ServerPlayer p = friendOf(helper, ape, new BlockPos(4, 2, 4));
        zombie(helper, new BlockPos(6, 2, 4));
        zombie(helper, new BlockPos(4, 2, 6));
        check(ape.groundPound(helper.getLevel()) == 0, "two are no crowd");
        Zombie z = zombie(helper, new BlockPos(2, 2, 4));
        check(ape.groundPound(helper.getLevel()) == 3, "three are: she slams the ground");
        check(z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "and they are slowed");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "sandwormSwallows")
    public static void sandwormSwallows(GameTestHelper helper) {
        Lamia worm = still(helper, GirlSpecies.SANDWORM_LAMIA, new BlockPos(1, 2, 1));
        check(worm.swallow(helper.getLevel()) == -1, "no friend");
        ServerPlayer p = friendOf(helper, worm, new BlockPos(3, 2, 3));
        check(worm.swallow(helper.getLevel()) == 0, "nothing near");
        Zombie z = zombie(helper, new BlockPos(5, 2, 5));
        check(worm.swallow(helper.getLevel()) == 1, "she swallows the zombie");
        check(z.distanceTo(p) > 10, "and spits it out far away");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "xiangliuBitesThree")
    public static void xiangliuBitesThree(GameTestHelper helper) {
        Lamia hydra = still(helper, GirlSpecies.XIANGLIU_LAMIA, new BlockPos(1, 2, 1));
        check(hydra.nineHeads(helper.getLevel()) == -1, "no friend");
        ServerPlayer p = friendOf(helper, hydra, new BlockPos(4, 2, 4));
        for (int i = 0; i < 4; i++) zombie(helper, new BlockPos(2 + i, 2, 6));
        check(hydra.nineHeads(helper.getLevel()) == 3, "three heads bite three of the four");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "archelonKnocksArrowsAway")
    public static void archelonKnocksArrowsAway(GameTestHelper helper) {
        Lizardfolk turtle = still(helper, GirlSpecies.ARCHELON_LIZARDFOLK, new BlockPos(1, 2, 1));
        check(turtle.shellShield(helper.getLevel()) == -1, "no friend");
        ServerPlayer p = friendOf(helper, turtle, new BlockPos(3, 2, 3));
        Arrow a = new Arrow(EntityType.ARROW, helper.getLevel());
        Vec3 from = p.position().add(3, 1, 0);
        a.setPos(from.x, from.y, from.z);
        a.setNoGravity(true);
        a.setDeltaMovement(-1.5, 0, 0);                   // flying straight at her friend
        helper.getLevel().addFreshEntity(a);
        check(turtle.shellShield(helper.getLevel()) == 1, "she knocks the arrow away");
        check(a.getDeltaMovement().x > 0, "it flies back the way it came");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "cyclopsHurlsABoulder")
    public static void cyclopsHurlsABoulder(GameTestHelper helper) {
        Devil cyclops = still(helper, GirlSpecies.CYCLOPS_DEVIL, new BlockPos(1, 2, 1));
        check(cyclops.boulder(helper.getLevel()) == -1, "no friend");
        ServerPlayer p = friendOf(helper, cyclops, new BlockPos(3, 2, 3));
        check(cyclops.boulder(helper.getLevel()) == 0, "nothing to throw at");
        Zombie z = zombie(helper, new BlockPos(6, 2, 6));
        float before = z.getHealth();
        check(cyclops.boulder(helper.getLevel()) == 1, "she throws");
        check(z.getHealth() < before, "and hits");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "genieGrantsAWish")
    public static void genieGrantsAWish(GameTestHelper helper) {
        Devil genie = still(helper, GirlSpecies.GENIE_DEVIL, new BlockPos(1, 2, 1));
        check(genie.wish(helper.getLevel()) == -1, "no friend, no wish");
        ServerPlayer p = friendOf(helper, genie, new BlockPos(3, 2, 3));
        long day = helper.getLevel().getDayTime() / 24000L * 24000L;
        helper.getLevel().setDayTime(day + 3000);
        check(genie.wish(helper.getLevel()) == 0, "mid-morning is no wishing hour");
        helper.getLevel().setDayTime(day + 6100);
        int before = p.getActiveEffects().size();
        check(genie.wish(helper.getLevel()) == 1, "at noon she grants a wish");
        check(p.getActiveEffects().size() > before, "a boon");
        check(genie.wish(helper.getLevel()) == 0, "once per hour");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "ammitDevoursHearts")
    public static void ammitDevoursHearts(GameTestHelper helper) {
        Nekomata ammit = still(helper, GirlSpecies.AMMIT_NEKOMATA, new BlockPos(1, 2, 1));
        check(ammit.devour(helper.getLevel()) == -1, "no friend");
        ServerPlayer p = friendOf(helper, ammit, new BlockPos(3, 2, 3));
        p.setHealth(8.0F);
        Zombie z = zombie(helper, new BlockPos(5, 2, 5));
        check(ammit.devour(helper.getLevel()) == 0, "nothing has died");
        z.setHealth(0.0F);
        check(ammit.devour(helper.getLevel()) == 1, "she eats the heart");
        check(p.getHealth() > 8.0F, "and her friend heals");
        check(ammit.devour(helper.getLevel()) == 0, "each heart once");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
