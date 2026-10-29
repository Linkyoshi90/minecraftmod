package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Bug;
import com.linkyoshi90.monstergirls.entity.girl.Centaur;
import com.linkyoshi90.monstergirls.entity.girl.Dragon;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Lizardfolk;
import com.linkyoshi90.monstergirls.entity.girl.Merfolk;
import com.linkyoshi90.monstergirls.entity.girl.Minotaur;
import com.linkyoshi90.monstergirls.entity.girl.Nekomata;
import com.linkyoshi90.monstergirls.entity.girl.Rodent;
import com.linkyoshi90.monstergirls.entity.girl.Theropod;
import com.linkyoshi90.monstergirls.entity.girl.Undead;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/**
 * The twentieth Harmontide wave (HARMONTIDE_CONCEPTS.md): brasshorn ram, ceibler, sobolnik, turtworld, fernstalker, moo,
 * hairypterix, doggo, the ghoul, the manticore, the Mothman, the basilisk and the house mouse; and the homage variants and paints
 * that came with it.
 */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class TwentiethWaveTests {

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

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "brasshornSteadiesHerFriend")
    public static void brasshornSteadiesHerFriend(GameTestHelper helper) {
        Minotaur ram = still(helper, GirlSpecies.BRASSHORN_MINOTAUR, new BlockPos(1, 2, 1));
        check(!ram.mountainFooting(helper.getLevel()), "no friend, no footing");
        ServerPlayer p = friendOf(helper, ram, new BlockPos(3, 2, 3));
        check(ram.mountainFooting(helper.getLevel()), "her friend is near");
        check(p.hasEffect(MobEffects.JUMP), "and jumps higher");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "ceiblerGuidesOnTheTrail")
    public static void ceiblerGuidesOnTheTrail(GameTestHelper helper) {
        Centaur ceibler = still(helper, GirlSpecies.CEIBLER_CENTAUR, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, ceibler, new BlockPos(3, 2, 3));
        helper.setBlock(new BlockPos(3, 1, 3), Blocks.STONE);
        check(!ceibler.trailGuide(helper.getLevel()), "not on stone");
        helper.setBlock(new BlockPos(3, 1, 3), Blocks.GRASS_BLOCK);
        check(ceibler.trailGuide(helper.getLevel()), "on grass she guides her friend");
        check(p.hasEffect(MobEffects.MOVEMENT_SPEED), "who runs faster");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "sobolnikSeesUnderground")
    public static void sobolnikSeesUnderground(GameTestHelper helper) {
        Rodent sable = still(helper, GirlSpecies.SOBOLNIK_RODENT, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, sable, new BlockPos(3, 2, 3));
        check(!sable.burrowEyes(helper.getLevel()), "not under the open sky");
        helper.setBlock(new BlockPos(3, 5, 3), Blocks.STONE);
        helper.runAfterDelay(10, () -> {                  // once the roof's shadow has spread
            check(sable.burrowEyes(helper.getLevel()), "under a roof");
            check(p.hasEffect(MobEffects.NIGHT_VISION), "her friend sees in the dark");
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "houseMouseSharesCrumbs")
    public static void houseMouseSharesCrumbs(GameTestHelper helper) {
        Rodent mouse = still(helper, GirlSpecies.HOUSE_MOUSE_RODENT, new BlockPos(1, 2, 1));
        check(!mouse.crumbs(helper.getLevel()), "no friend, no crumbs");
        ServerPlayer p = friendOf(helper, mouse, new BlockPos(3, 2, 3));
        p.getFoodData().setFoodLevel(10);
        check(mouse.crumbs(helper.getLevel()), "her friend is hungry");
        check(p.getFoodData().getFoodLevel() == 11, "and gets a crumb");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "turtworldGrowsCrops")
    public static void turtworldGrowsCrops(GameTestHelper helper) {
        Lizardfolk turt = still(helper, GirlSpecies.TURTWORLD_LIZARDFOLK, new BlockPos(1, 2, 1));
        check(turt.livingIsland(helper.getLevel()) == -1, "no friend, no growing");
        ServerPlayer p = friendOf(helper, turt, new BlockPos(3, 2, 3));
        BlockPos crop = new BlockPos(5, 2, 4);
        helper.setBlock(crop.below(), Blocks.FARMLAND);
        helper.setBlock(crop, Blocks.WHEAT);
        check(turt.livingIsland(helper.getLevel()) == 1, "she grows the wheat");
        check(helper.getBlockState(crop).getValue(CropBlock.AGE) > 0, "it is taller");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "fernstalkerAmbushes")
    public static void fernstalkerAmbushes(GameTestHelper helper) {
        Theropod stalker = still(helper, GirlSpecies.FERNSTALKER_THEROPOD, new BlockPos(1, 2, 1));
        check(stalker.ambush(helper.getLevel()) == -1, "no friend, no ambush");
        ServerPlayer p = friendOf(helper, stalker, new BlockPos(3, 2, 3));
        check(stalker.ambush(helper.getLevel()) == 0, "no monster near");
        Zombie z = zombie(helper, new BlockPos(5, 2, 5));
        float before = z.getHealth();
        check(stalker.ambush(helper.getLevel()) == 1, "she pounces");
        check(z.getHealth() < before, "and wounds the zombie");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "basiliskStaresDown")
    public static void basiliskStaresDown(GameTestHelper helper) {
        Lizardfolk basilisk = still(helper, GirlSpecies.BASILISK_LIZARDFOLK, new BlockPos(1, 2, 1));
        check(basilisk.stoneGaze(helper.getLevel()) == -1, "no friend, no gaze");
        ServerPlayer p = friendOf(helper, basilisk, new BlockPos(3, 2, 3));
        Zombie z = zombie(helper, new BlockPos(5, 2, 5));
        check(basilisk.stoneGaze(helper.getLevel()) == 1, "she stares at the zombie");
        check(z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "it can barely move");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "mooMakesAStand")
    public static void mooMakesAStand(GameTestHelper helper) {
        Dragon moo = still(helper, GirlSpecies.MOO_DRAGON, new BlockPos(1, 2, 1));
        check(!moo.herdStand(helper.getLevel()), "no friend, no stand");
        ServerPlayer p = friendOf(helper, moo, new BlockPos(3, 2, 3));
        zombie(helper, new BlockPos(5, 2, 5));
        check(!moo.herdStand(helper.getLevel()), "one monster is not worth a stand");
        zombie(helper, new BlockPos(5, 2, 1));
        check(moo.herdStand(helper.getLevel()), "two are");
        check(p.hasEffect(MobEffects.DAMAGE_BOOST), "her friend is stronger");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "hairypterixCatchesTheFall")
    public static void hairypterixCatchesTheFall(GameTestHelper helper) {
        Dragon ptero = still(helper, GirlSpecies.HAIRYPTERIX_DRAGON, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, ptero, new BlockPos(3, 2, 3));
        check(!ptero.updraft(helper.getLevel()), "not while her friend stands");
        p.fallDistance = 5.0F;
        check(ptero.updraft(helper.getLevel()), "her friend falls");
        check(p.hasEffect(MobEffects.SLOW_FALLING), "and drifts down");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "doggoBringsAir")
    public static void doggoBringsAir(GameTestHelper helper) {
        Merfolk doggo = still(helper, GirlSpecies.DOGGO_MERMAID, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, doggo, new BlockPos(3, 2, 3));
        check(!doggo.lifeguard(helper.getLevel()), "not while her friend has air");
        p.setAirSupply(10);
        check(doggo.lifeguard(helper.getLevel()), "her friend is short of air");
        check(p.getAirSupply() == p.getMaxAirSupply(), "and gets a full breath");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "ghoulEatsCarrion")
    public static void ghoulEatsCarrion(GameTestHelper helper) {
        Undead ghoul = still(helper, GirlSpecies.GHOUL_UNDEAD, new BlockPos(1, 2, 1));
        check(ghoul.carrion(helper.getLevel()) == -1, "no friend, no feast");
        ServerPlayer p = friendOf(helper, ghoul, new BlockPos(3, 2, 3));
        check(ghoul.carrion(helper.getLevel()) == 0, "nothing to eat");
        ItemEntity flesh = helper.spawnItem(Items.ROTTEN_FLESH, new BlockPos(5, 2, 5));
        ghoul.setHealth(ghoul.getMaxHealth() - 4.0F);
        check(ghoul.carrion(helper.getLevel()) == 1, "she eats the rotten flesh");
        check(!flesh.isAlive() && ghoul.getHealth() > ghoul.getMaxHealth() - 4.0F, "it is gone, and she is healed");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "manticoreStings")
    public static void manticoreStings(GameTestHelper helper) {
        Nekomata manticore = still(helper, GirlSpecies.MANTICORE_NEKOMATA, new BlockPos(1, 2, 1));
        check(manticore.tailSpike(helper.getLevel()) == -1, "no friend, no sting");
        ServerPlayer p = friendOf(helper, manticore, new BlockPos(3, 2, 3));
        Zombie z = zombie(helper, new BlockPos(5, 2, 5));
        float before = z.getHealth();
        check(manticore.tailSpike(helper.getLevel()) == 1, "she stings the zombie");
        check(z.getHealth() < before && z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "hurt and slowed");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "mothmanOmen")
    public static void mothmanOmen(GameTestHelper helper) {
        Bug moth = still(helper, GirlSpecies.MOTHMAN_BUG, new BlockPos(1, 2, 1));
        check(moth.omen(helper.getLevel(), true) == -1, "no friend, no omen");
        ServerPlayer p = friendOf(helper, moth, new BlockPos(3, 2, 3));
        Zombie z = zombie(helper, new BlockPos(5, 2, 5));
        check(moth.omen(helper.getLevel(), false) == 0, "not by day");
        check(moth.omen(helper.getLevel(), true) == 1, "at night");
        check(z.hasEffect(MobEffects.GLOWING), "the zombie glows");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "twentiethHomagesAndPaints")
    public static void twentiethHomagesAndPaints(GameTestHelper helper) {
        Map<GirlSpecies, List<String>> homages = Map.of(
                GirlSpecies.HOUSE_MOUSE_RODENT, List.of("innkeeper", "prankster", "soul_keeper"),
                GirlSpecies.HIGH_ELF, List.of("sage_princess"), GirlSpecies.WOOD_ELF, List.of("green_hero"), GirlSpecies.ENT_FLORA, List.of("tree_guard"),
                GirlSpecies.YUKI_ONNA_UNDEAD, List.of("snow_wraith"), GirlSpecies.DULLAHAN_UNDEAD, List.of("headless_rider", "death_envoy"));
        homages.forEach((s, vs) -> vs.forEach(v -> {
            check(s.variants().contains(v), s.id() + " has the " + v + " look");
            check(AbstractMonsterGirl.HOMAGES.contains(v), v + " is rolled rarely, as a homage");
        }));
        check(!AbstractMonsterGirl.HOMAGES.contains(GirlSpecies.HOUSE_MOUSE_RODENT.variants().get(0)), "a plain house mouse is the usual one");
        for (String paint : new String[] {"tie_dye", "jade", "lace", "bluebell"}) {
            check(AbstractMonsterGirl.PAINTS.contains(paint), "the " + paint + " paint exists");
        }
        helper.succeed();
    }

    /** The Mudkip and Gura looks are drawn with models of their own (axolotl_mermaid_mud_fin, shark_mermaid_trident_hood). */
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "homagesWithTheirOwnModels")
    public static void homagesWithTheirOwnModels(GameTestHelper helper) {
        check(AbstractMonsterGirl.OWN_MODELS.equals(java.util.Set.of("mud_fin", "trident_hood")), "the two detailed looks");
        for (String v : AbstractMonsterGirl.OWN_MODELS) check(AbstractMonsterGirl.HOMAGES.contains(v), v + " is still a rare homage");
        check(GirlSpecies.AXOLOTL_MERMAID.variants().contains("mud_fin") && GirlSpecies.SHARK_MERMAID.variants().contains("trident_hood"),
                "on the axolotl and the shark");
        Merfolk gura = still(helper, GirlSpecies.SHARK_MERMAID, new BlockPos(1, 2, 1));
        gura.setVariant("trident_hood");
        check("trident_hood".equals(gura.getVariantName()), "a shark girl can wear the look");
        helper.succeed();
    }
}
