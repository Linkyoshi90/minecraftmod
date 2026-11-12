package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Armoured;
import com.linkyoshi90.harmontide.entity.girl.Canid;
import com.linkyoshi90.harmontide.entity.girl.Centaur;
import com.linkyoshi90.harmontide.entity.girl.Dragon;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Lamia;
import com.linkyoshi90.harmontide.entity.girl.Merfolk;
import com.linkyoshi90.harmontide.entity.girl.Minotaur;
import com.linkyoshi90.harmontide.entity.girl.Nekomata;
import com.linkyoshi90.harmontide.entity.girl.Undead;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/**
 * The twenty-first Harmontide wave (HARMONTIDE_CONCEPTS.md): nenekea, rheintalensis, daeodon, anteater, oryx, caracal, walrus,
 * anomalocaris, the serpopard, Urcuchillay, the rakshasa and the gashadokuro; the paints and Muffet's look that came with it.
 */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class TwentyFirstWaveTests {

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

    /** The items lying within `r` blocks of the player. */
    private static List<ItemEntity> itemsNear(GameTestHelper helper, ServerPlayer p, double r) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, p.getBoundingBox().inflate(r));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "nenekeaHealsInWater")
    public static void nenekeaHealsInWater(GameTestHelper helper) {
        Dragon nenekea = still(helper, GirlSpecies.NENEKEA_DRAGON, new BlockPos(1, 2, 1));
        check(!nenekea.tidalBalm(helper.getLevel()), "no friend, no balm");
        ServerPlayer p = friendOf(helper, nenekea, new BlockPos(3, 2, 3));
        p.setHealth(10.0F);
        check(!nenekea.tidalBalm(helper.getLevel()), "not on dry land");
        helper.setBlock(new BlockPos(3, 2, 3), Blocks.WATER);
        p.doTick();                                       // the test player isn't ticked by a connection: notice the water
        check(nenekea.tidalBalm(helper.getLevel()), "her hurt friend stands in water");
        check(p.hasEffect(MobEffects.REGENERATION), "and heals");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "rheintalensisSwingsHerClub")
    public static void rheintalensisSwingsHerClub(GameTestHelper helper) {
        Canid bear = still(helper, GirlSpecies.RHEINTALENSIS_CANID, new BlockPos(1, 2, 1));
        check(bear.bruteSwing(helper.getLevel()) == -1, "no friend, no swing");
        ServerPlayer p = friendOf(helper, bear, new BlockPos(3, 2, 3));
        check(bear.bruteSwing(helper.getLevel()) == 0, "nothing close");
        Zombie z = zombie(helper, new BlockPos(5, 2, 3));
        check(bear.bruteSwing(helper.getLevel()) == 1, "a zombie by her friend");
        check(z.getHealth() < z.getMaxHealth(), "is clubbed");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "daeodonRootsUpFood")
    public static void daeodonRootsUpFood(GameTestHelper helper) {
        Armoured pig = still(helper, GirlSpecies.DAEODON_ARMOURED, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, pig, new BlockPos(3, 2, 3));
        helper.setBlock(new BlockPos(3, 1, 3), Blocks.STONE);
        check(!pig.rooting(helper.getLevel()), "nothing to root in stone");
        helper.setBlock(new BlockPos(3, 1, 3), Blocks.GRASS_BLOCK);
        check(pig.rooting(helper.getLevel()), "on grass she roots");
        check(itemsNear(helper, p, 2).stream().anyMatch(i -> i.getItem().is(Items.POTATO) || i.getItem().is(Items.CARROT)
                || i.getItem().is(Items.BEETROOT) || i.getItem().is(Items.GOLDEN_CARROT)), "something to eat at her friend's feet");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "anteaterLicksUpItems")
    public static void anteaterLicksUpItems(GameTestHelper helper) {
        Armoured anteater = still(helper, GirlSpecies.ANTEATER_ARMOURED, new BlockPos(1, 2, 1));
        check(anteater.longTongue(helper.getLevel()) == -1, "no friend, no licking");
        ServerPlayer p = friendOf(helper, anteater, new BlockPos(2, 2, 2));
        ItemEntity stick = helper.spawnItem(Items.STICK, 6.5F, 2.0F, 6.5F);
        check(anteater.longTongue(helper.getLevel()) == 1, "one loose item");
        check(stick.distanceTo(p) < 1.5F, "brought to her friend's feet");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "oryxStridesInTheHeat")
    public static void oryxStridesInTheHeat(GameTestHelper helper) {
        Minotaur oryx = still(helper, GirlSpecies.ORYX_MINOTAUR, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, oryx, new BlockPos(3, 2, 3));
        p.getFoodData().addExhaustion(3.0F);
        check(!oryx.desertStride(helper.getLevel(), false), "not in the cool");
        check(p.getFoodData().getExhaustionLevel() > 2.9F, "her friend still tires there");
        check(oryx.desertStride(helper.getLevel(), true), "in the heat");
        check(p.getFoodData().getExhaustionLevel() == 0.0F, "her friend doesn't tire");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "caracalCatchesFlyers")
    public static void caracalCatchesFlyers(GameTestHelper helper) {
        Nekomata caracal = still(helper, GirlSpecies.CARACAL_NEKOMATA, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, caracal, new BlockPos(3, 2, 3));
        zombie(helper, new BlockPos(5, 2, 5));
        check(caracal.birdCatch(helper.getLevel()) == 0, "a zombie doesn't fly");
        Phantom ph = helper.spawn(EntityType.PHANTOM, new BlockPos(4, 4, 3));
        ph.setNoAi(true);
        check(caracal.birdCatch(helper.getLevel()) == 1, "a phantom does");
        check(ph.getHealth() < ph.getMaxHealth(), "and is struck");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "rakshasaGlamour")
    public static void rakshasaGlamour(GameTestHelper helper) {
        Nekomata rakshasa = still(helper, GirlSpecies.RAKSHASA_NEKOMATA, new BlockPos(1, 2, 1));
        check(rakshasa.glamour(helper.getLevel()) == -1, "no friend, no glamour");
        ServerPlayer p = friendOf(helper, rakshasa, new BlockPos(3, 2, 3));
        Zombie hunter = zombie(helper, new BlockPos(5, 2, 5));
        Zombie idle = zombie(helper, new BlockPos(5, 2, 1));
        hunter.setTarget(p);
        check(rakshasa.glamour(helper.getLevel()) == 1, "one was hunting her friend");
        check(hunter.getTarget() == null, "and has lost them");
        check(idle.getTarget() == null, "the other never cared");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "walrusKeepsHerFriendWarm")
    public static void walrusKeepsHerFriendWarm(GameTestHelper helper) {
        Merfolk walrus = still(helper, GirlSpecies.WALRUS_MERMAID, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, walrus, new BlockPos(3, 2, 3));
        check(!walrus.blubber(helper.getLevel()), "not frozen");
        p.setTicksFrozen(100);
        check(walrus.blubber(helper.getLevel()), "frost on her friend");
        check(p.getTicksFrozen() == 0, "thaws");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "anomalocarisGrapples")
    public static void anomalocarisGrapples(GameTestHelper helper) {
        Merfolk ano = still(helper, GirlSpecies.ANOMALOCARIS_MERMAID, new BlockPos(1, 2, 1));
        check(ano.raptorGrasp(helper.getLevel()) == -1, "no friend, no grasp");
        ServerPlayer p = friendOf(helper, ano, new BlockPos(3, 2, 3));
        Zombie z = zombie(helper, new BlockPos(5, 2, 5));
        check(ano.raptorGrasp(helper.getLevel()) == 0, "a zombie on dry land is out of her reach");
        helper.setBlock(new BlockPos(5, 2, 5), Blocks.WATER);
        helper.setBlock(new BlockPos(5, 3, 5), Blocks.WATER);
        helper.runAfterDelay(5, () -> {                   // once the zombie has noticed it is swimming
            check(ano.raptorGrasp(helper.getLevel()) == 1, "a zombie in the water");
            check(z.getHealth() < z.getMaxHealth(), "is seized and bitten");
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "serpopardBites")
    public static void serpopardBites(GameTestHelper helper) {
        Lamia serpopard = still(helper, GirlSpecies.SERPOPARD_LAMIA, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, serpopard, new BlockPos(3, 2, 3));
        check(serpopard.venomBite(helper.getLevel()) == 0, "nothing near");
        net.minecraft.world.entity.monster.Creeper c = helper.spawn(EntityType.CREEPER, new BlockPos(5, 2, 5));
        c.setNoAi(true);
        check(serpopard.venomBite(helper.getLevel()) == 1, "a creeper near her friend");
        check(c.hasEffect(MobEffects.POISON), "is poisoned");
        c.discard();
        Zombie z = zombie(helper, new BlockPos(5, 2, 4));
        check(serpopard.venomBite(helper.getLevel()) == 1, "a zombie near her friend");
        check(z.getHealth() < z.getMaxHealth(), "can't be poisoned, but the venom burns");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "urcuchillayRaisesTheHerd")
    public static void urcuchillayRaisesTheHerd(GameTestHelper helper) {
        Centaur urcu = still(helper, GirlSpecies.URCUCHILLAY_CENTAUR, new BlockPos(1, 2, 1));
        check(urcu.herdKeeper(helper.getLevel()) == -1, "no friend, no herd");
        ServerPlayer p = friendOf(helper, urcu, new BlockPos(3, 2, 3));
        Cow calf = helper.spawn(EntityType.COW, new BlockPos(5, 2, 5));
        calf.setNoAi(true);
        calf.setAge(-24000);
        Cow grown = helper.spawn(EntityType.COW, new BlockPos(5, 2, 1));
        grown.setNoAi(true);
        check(urcu.herdKeeper(helper.getLevel()) == 1, "one calf");
        check(calf.getAge() > -24000, "grows up sooner");
        check(grown.getAge() == 0, "the grown cow is left be");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "gashadokuroLifts")
    public static void gashadokuroLifts(GameTestHelper helper) {
        Undead gasha = still(helper, GirlSpecies.GASHADOKURO_UNDEAD, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, gasha, new BlockPos(3, 2, 3));
        check(gasha.giantsGrasp(helper.getLevel()) == 0, "nothing near");
        Zombie z = zombie(helper, new BlockPos(5, 2, 5));
        check(gasha.giantsGrasp(helper.getLevel()) == 1, "a zombie near her friend");
        check(z.hasEffect(MobEffects.LEVITATION), "is lifted");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "twentyFirstPaintsAndMuffet")
    public static void twentyFirstPaintsAndMuffet(GameTestHelper helper) {
        for (String paint : new String[] {"obsidian", "patchwork", "sandstone", "acorn"}) {
            check(AbstractMonsterGirl.PAINTS.contains(paint), "the " + paint + " paint exists");
        }
        check(GirlSpecies.JOROGUMO_ARACHNE.variants().contains("bake_sale"), "the jorogumo has Muffet's look");
        check(AbstractMonsterGirl.HOMAGES.contains("bake_sale"), "rolled rarely, as a homage");
        check(!AbstractMonsterGirl.OWN_MODELS.contains("bake_sale"), "a look on the jorogumo's own model");
        helper.succeed();
    }
}
