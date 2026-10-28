package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.MirrorCraneHarpy;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Arachne;
import com.linkyoshi90.monstergirls.entity.girl.Canid;
import com.linkyoshi90.monstergirls.entity.girl.Centaur;
import com.linkyoshi90.monstergirls.entity.girl.Elf;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Lamia;
import com.linkyoshi90.monstergirls.entity.girl.Lizardfolk;
import com.linkyoshi90.monstergirls.entity.girl.Merfolk;
import com.linkyoshi90.monstergirls.entity.girl.Minotaur;
import com.linkyoshi90.monstergirls.entity.girl.Nekomata;
import com.linkyoshi90.monstergirls.entity.girl.Rodent;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/**
 * The eighteenth Harmontide wave (HARMONTIDE_CONCEPTS.md): spoodr, urchskin, pineskrell, turtfolk, tarnhelm, palmyr,
 * skjoldram, mirror crane, Bastet, rusalka, ushi-oni, domovoi.
 */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class EighteenthWaveTests {

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

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "spoodrSweepsCobwebs")
    public static void spoodrSweepsCobwebs(GameTestHelper helper) {
        Arachne spoodr = still(helper, GirlSpecies.SPOODR_ARACHNE, new BlockPos(1, 2, 1));
        check(spoodr.sweepWebs(helper.getLevel()) == -1, "no friend, no sweeping");
        ServerPlayer p = friendOf(helper, spoodr, new BlockPos(3, 2, 3));
        check(spoodr.sweepWebs(helper.getLevel()) == 0, "no webs yet");
        BlockPos web = new BlockPos(4, 3, 4);
        helper.setBlock(web, Blocks.COBWEB);
        check(spoodr.sweepWebs(helper.getLevel()) == 1, "she clears the web");
        check(helper.getBlockState(web).isAir(), "the web is gone");
        check(p.getInventory().countItem(Items.STRING) == 1, "and her friend has its string");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "ushiOniBellows")
    public static void ushiOniBellows(GameTestHelper helper) {
        Arachne oni = still(helper, GirlSpecies.USHI_ONI_ARACHNE, new BlockPos(1, 2, 1));
        check(oni.bellow(helper.getLevel()) == -1, "no friend, no bellow");
        ServerPlayer p = friendOf(helper, oni, new BlockPos(3, 2, 3));
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(5, 2, 5));
        z.setTarget(p);
        check(oni.bellow(helper.getLevel()) == 1, "she bellows at the zombie");
        check(z.getTarget() == null && z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "it forgets her friend, slowed");
        z.setTarget(p);
        check(oni.bellow(helper.getLevel()) == 0, "not again so soon");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "urchskinPricksAttackers")
    public static void urchskinPricksAttackers(GameTestHelper helper) {
        Lamia urchskin = still(helper, GirlSpecies.URCHSKIN_LAMIA, new BlockPos(1, 2, 1));
        ServerPlayer stranger = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 1));
        check(Lamia.urchskinOf(stranger) == null, "only for a friend");
        HarpyTestUtil.remove(stranger);
        ServerPlayer p = friendOf(helper, urchskin, new BlockPos(3, 2, 3));
        check(Lamia.urchskinOf(p) == urchskin, "she guards her friend");
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(4, 2, 4));
        z.setNoAi(true);
        float before = z.getHealth();
        check(urchskin.prick(z) && z.getHealth() < before, "the zombie that hit her friend is pricked");   // (on Peaceful a mob's hit does no harm, so no damage event)
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "pineskrellSensesOre")
    public static void pineskrellSensesOre(GameTestHelper helper) {
        Rodent skrell = still(helper, GirlSpecies.PINESKRELL_RODENT, new BlockPos(1, 2, 1));
        check(skrell.oreSense(helper.getLevel()) == -1, "no friend, no ore sense");
        ServerPlayer p = friendOf(helper, skrell, new BlockPos(3, 2, 3));
        check(skrell.oreSense(helper.getLevel()) == 0, "no ore near");
        helper.setBlock(new BlockPos(5, 2, 5), Blocks.DIAMOND_ORE);
        helper.setBlock(new BlockPos(1, 2, 5), Blocks.IRON_ORE);
        check(skrell.oreSense(helper.getLevel()) == 2, "both ores sparkle");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "turtfolkShieldsHerFriend")
    public static void turtfolkShieldsHerFriend(GameTestHelper helper) {
        Lizardfolk turt = still(helper, GirlSpecies.TURTFOLK_LIZARDFOLK, new BlockPos(1, 2, 1));
        check(!turt.shellGuard(helper.getLevel()), "no friend, no shield");
        ServerPlayer p = friendOf(helper, turt, new BlockPos(3, 2, 3));
        check(turt.shellGuard(helper.getLevel()), "she shields her friend");
        check(p.hasEffect(MobEffects.ABSORPTION), "two hearts of absorption");
        check(!turt.shellGuard(helper.getLevel()), "not again for half a minute");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "tarnhelmSpotsRaiders")
    public static void tarnhelmSpotsRaiders(GameTestHelper helper) {
        Canid wolf = still(helper, GirlSpecies.TARNHELM_CANID, new BlockPos(1, 2, 1));
        check(wolf.sentinel(helper.getLevel()) == -1, "no friend, no watch");
        ServerPlayer p = friendOf(helper, wolf, new BlockPos(3, 2, 3));
        Pillager pillager = helper.spawn(EntityType.PILLAGER, new BlockPos(6, 2, 6));
        pillager.setNoAi(true);
        check(wolf.sentinel(helper.getLevel()) == 1, "she spots the pillager");
        check(pillager.hasEffect(MobEffects.GLOWING), "it glows");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "palmyrSpeedsASprint")
    public static void palmyrSpeedsASprint(GameTestHelper helper) {
        Centaur deer = still(helper, GirlSpecies.PALMYR_CENTAUR, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, deer, new BlockPos(3, 2, 3));
        check(!deer.fleetFoot(helper.getLevel()), "only while sprinting");
        p.setSprinting(true);
        check(deer.fleetFoot(helper.getLevel()), "her friend sprints");
        check(p.hasEffect(MobEffects.MOVEMENT_SPEED), "and runs faster");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "skjoldramButtsCreepers")
    public static void skjoldramButtsCreepers(GameTestHelper helper) {
        Minotaur ram = still(helper, GirlSpecies.SKJOLDRAM_MINOTAUR, new BlockPos(1, 2, 1));
        check(ram.ramCharge(helper.getLevel()) == -1, "no friend, no charge");
        ServerPlayer p = friendOf(helper, ram, new BlockPos(3, 2, 3));
        Creeper c = helper.spawn(EntityType.CREEPER, new BlockPos(5, 2, 3));
        c.setNoAi(true);
        c.setSwellDir(1);
        check(ram.ramCharge(helper.getLevel()) == 1, "she butts the creeper");
        Vec3 away = c.position().subtract(p.position());
        check(c.getDeltaMovement().dot(away) > 0 && c.getSwellDir() < 0, "away from her friend, its fuse out");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "bastetWardsCreepers")
    public static void bastetWardsCreepers(GameTestHelper helper) {
        Nekomata bastet = still(helper, GirlSpecies.BASTET_NEKOMATA, new BlockPos(1, 2, 1));
        check(bastet.wardCreepers(helper.getLevel()) == -1, "no friend, no ward");
        ServerPlayer p = friendOf(helper, bastet, new BlockPos(3, 2, 3));
        Creeper c = helper.spawn(EntityType.CREEPER, new BlockPos(6, 2, 6));
        c.setTarget(p);
        check(bastet.wardCreepers(helper.getLevel()) == 1, "she wards the creeper off");
        check(c.getTarget() == null, "it stops hunting her friend");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "rusalkaBringsLuck")
    public static void rusalkaBringsLuck(GameTestHelper helper) {
        Merfolk rusalka = still(helper, GirlSpecies.RUSALKA_MERMAID, new BlockPos(1, 2, 1));
        check(rusalka.getType().is(EntityTypeTags.CAN_BREATHE_UNDER_WATER), "she breathes water");
        ServerPlayer p = friendOf(helper, rusalka, new BlockPos(3, 2, 3));
        check(!rusalka.riverLuck(helper.getLevel()), "only while fishing");
        FishingHook hook = new FishingHook(p, helper.getLevel(), 0, 0);
        check(rusalka.riverLuck(helper.getLevel()), "her friend fishes");
        check(p.hasEffect(MobEffects.LUCK), "with luck");
        hook.discard();
        Lamia urchskin = still(helper, GirlSpecies.URCHSKIN_LAMIA, new BlockPos(5, 2, 1));
        check(urchskin.getType().is(EntityTypeTags.CAN_BREATHE_UNDER_WATER), "the urchskin breathes water too");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "domovoiTendsTheFurnace")
    public static void domovoiTendsTheFurnace(GameTestHelper helper) {
        Elf domovoi = still(helper, GirlSpecies.DOMOVOI_ELF, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, domovoi, new BlockPos(3, 2, 3));
        BlockPos at = new BlockPos(5, 2, 5);
        helper.setBlock(at, Blocks.FURNACE);
        AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) helper.getBlockEntity(at);
        furnace.setItem(0, new ItemStack(Items.RAW_IRON));
        furnace.setItem(1, new ItemStack(Items.COAL));
        check(domovoi.keepHearth(helper.getLevel(), true) == 1, "she finds the furnace");
        for (int i = 0; i < 205; i++) domovoi.keepHearth(helper.getLevel(), false);
        check(furnace.getItem(2).is(Items.IRON_INGOT), "the iron is smelted at once");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "mirrorCraneTurnsArrowsAside")
    public static void mirrorCraneTurnsArrowsAside(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        MirrorCraneHarpy crane = helper.spawn(ModEntities.MIRROR_CRANE_HARPY.get(), new BlockPos(5, 2, 5));
        crane.setNoAi(true);
        check(MirrorCraneHarpy.guardOf(friend) == null, "only for a friend");
        crane.setAffection(friend, Orders.AFFECTION);
        check(MirrorCraneHarpy.guardOf(friend) == crane, "she guards her friend");
        Arrow own = new Arrow(EntityType.ARROW, helper.getLevel());
        own.setOwner(friend);
        check(!crane.deflect(own, friend), "not her friend's own arrows");
        Arrow shot = new Arrow(EntityType.ARROW, helper.getLevel());
        shot.setDeltaMovement(1, 0, 0);
        check(crane.deflect(shot, friend), "she turns the arrow aside");
        check(shot.getDeltaMovement().x < 0, "it flies back the way it came");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }
}
