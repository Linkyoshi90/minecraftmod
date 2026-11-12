package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.BatGirl;
import com.linkyoshi90.harmontide.entity.girl.Bug;
import com.linkyoshi90.harmontide.entity.girl.Canid;
import com.linkyoshi90.harmontide.entity.girl.Centaur;
import com.linkyoshi90.harmontide.entity.girl.Flora;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Lizardfolk;
import com.linkyoshi90.harmontide.entity.girl.Merfolk;
import com.linkyoshi90.harmontide.entity.girl.Minotaur;
import com.linkyoshi90.harmontide.entity.girl.Nekomata;
import com.linkyoshi90.harmontide.entity.girl.SlimeGirl;
import com.linkyoshi90.harmontide.entity.girl.Undead;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/**
 * The seventeenth Harmontide wave (HARMONTIDE_CONCEPTS.md): censer lion, conch mail turtle, date palm beetle, dustbison,
 * frondbat, sawfin, stagel, klabuster, dryad, amarok, yurei, lamassu.
 */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class SeventeenthWaveTests {

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

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "censerLionCalmsPhantoms")
    public static void censerLionCalmsPhantoms(GameTestHelper helper) {
        Nekomata lion = still(helper, GirlSpecies.CENSER_LION_NEKOMATA, new BlockPos(3, 2, 3));
        check(lion.incense(helper.getLevel()) == -1, "no friend, no incense");
        ServerPlayer p = friendOf(helper, lion, new BlockPos(5, 2, 5));
        p.getStats().setValue(p, Stats.CUSTOM.get(Stats.TIME_SINCE_REST), 99999);
        Phantom ph = helper.spawn(EntityType.PHANTOM, new BlockPos(5, 6, 5));
        ph.setNoAi(true);
        ph.setTarget(p);
        check(lion.incense(helper.getLevel()) == 1, "she calms the phantom");
        check(ph.getTarget() == null, "it stops hunting her friend");
        check(p.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)) == 0, "and her friend is rested");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "conchTurtleGivesConduitPower")
    public static void conchTurtleGivesConduitPower(GameTestHelper helper) {
        Lizardfolk turtle = still(helper, GirlSpecies.CONCH_TURTLE_LIZARDFOLK, new BlockPos(3, 2, 3));
        ServerPlayer p = friendOf(helper, turtle, new BlockPos(5, 2, 5));
        check(!turtle.conchCall(helper.getLevel()), "dry, nothing");
        helper.setBlock(new BlockPos(5, 2, 5), Blocks.WATER);
        helper.setBlock(new BlockPos(5, 3, 5), Blocks.WATER);
        check(turtle.conchCall(helper.getLevel()), "underwater, she calls");
        check(p.hasEffect(MobEffects.CONDUIT_POWER), "her friend has Conduit Power");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "dateBeetleSharesADate")
    public static void dateBeetleSharesADate(GameTestHelper helper) {
        Bug beetle = still(helper, GirlSpecies.DATE_BEETLE_BUG, new BlockPos(3, 2, 3));
        ServerPlayer p = friendOf(helper, beetle, new BlockPos(5, 2, 5));
        p.getFoodData().setFoodLevel(20);
        check(!beetle.shareDate(helper.getLevel()), "not hungry, no date");
        p.getFoodData().setFoodLevel(10);
        check(beetle.shareDate(helper.getLevel()), "hungry, a date");
        check(p.getFoodData().getFoodLevel() == 13, "three hunger points");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "dustbisonPutsOutHerFriend")
    public static void dustbisonPutsOutHerFriend(GameTestHelper helper) {
        Minotaur calf = still(helper, GirlSpecies.DUSTBISON_MINOTAUR, new BlockPos(3, 2, 3));
        ServerPlayer p = friendOf(helper, calf, new BlockPos(5, 2, 5));
        check(!calf.dustBath(helper.getLevel()), "not burning, nothing");
        p.igniteForSeconds(5);
        check(calf.dustBath(helper.getLevel()), "burning, a dust bath");
        check(!p.isOnFire() && p.hasEffect(MobEffects.FIRE_RESISTANCE), "the fire is out and she is fireproof a while");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "frondbatShakesTheLeaves")
    public static void frondbatShakesTheLeaves(GameTestHelper helper) {
        BatGirl bat = still(helper, GirlSpecies.FROND_BAT, new BlockPos(3, 2, 3));
        ServerPlayer p = friendOf(helper, bat, new BlockPos(5, 2, 5));
        check(bat.shakeLeaves(helper.getLevel()) == null, "no leaves, nothing");
        helper.setBlock(new BlockPos(6, 4, 5), Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
        List<ItemStack> fell = bat.shakeLeaves(helper.getLevel());
        check(fell != null && !fell.isEmpty(), "something falls from the leaves");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "sawfinGivesHasteUnderwater")
    public static void sawfinGivesHasteUnderwater(GameTestHelper helper) {
        Merfolk saw = still(helper, GirlSpecies.SAWFIN_MERMAID, new BlockPos(3, 2, 3));
        ServerPlayer p = friendOf(helper, saw, new BlockPos(5, 2, 5));
        check(!saw.sapperAid(helper.getLevel()), "dry, nothing");
        helper.setBlock(new BlockPos(5, 2, 5), Blocks.WATER);
        helper.setBlock(new BlockPos(5, 3, 5), Blocks.WATER);
        check(saw.sapperAid(helper.getLevel()), "underwater, she helps");
        check(p.hasEffect(MobEffects.DIG_SPEED) && p.getEffect(MobEffects.DIG_SPEED).getAmplifier() == 1, "Haste II");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "stagelGivesJumpBoost")
    public static void stagelGivesJumpBoost(GameTestHelper helper) {
        Centaur fawn = still(helper, GirlSpecies.STAGEL_CENTAUR, new BlockPos(3, 2, 3));
        check(!fawn.blessFriend(helper.getLevel()), "no friend, nothing");
        ServerPlayer p = friendOf(helper, fawn, new BlockPos(5, 2, 5));
        check(fawn.blessFriend(helper.getLevel()) && p.hasEffect(MobEffects.JUMP), "her friend jumps high");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "lamassuGivesResistance")
    public static void lamassuGivesResistance(GameTestHelper helper) {
        Centaur guard = still(helper, GirlSpecies.LAMASSU_CENTAUR, new BlockPos(3, 2, 3));
        ServerPlayer p = friendOf(helper, guard, new BlockPos(6, 2, 6));
        check(guard.blessFriend(helper.getLevel()) && p.hasEffect(MobEffects.DAMAGE_RESISTANCE), "her friend has Resistance");
        check(!p.hasEffect(MobEffects.JUMP), "and no jump boost: that is the stagel's");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "klabusterManuresCrops")
    public static void klabusterManuresCrops(GameTestHelper helper) {
        SlimeGirl klab = still(helper, GirlSpecies.KLABUSTER_SLIME, new BlockPos(3, 2, 3));
        check(klab.manure(helper.getLevel()) == -1, "no friend, nothing");
        ServerPlayer p = friendOf(helper, klab, new BlockPos(5, 2, 5));
        helper.setBlock(new BlockPos(7, 1, 5), Blocks.FARMLAND);
        helper.setBlock(new BlockPos(7, 2, 5), Blocks.WHEAT);
        check(klab.manure(helper.getLevel()) == 1, "she manures the wheat");
        check(helper.getBlockState(new BlockPos(7, 2, 5)).getValue(net.minecraft.world.level.block.CropBlock.AGE) > 0, "and it grows");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "dryadGrowsSaplings")
    public static void dryadGrowsSaplings(GameTestHelper helper) {
        Flora dryad = still(helper, GirlSpecies.DRYAD_FLORA, new BlockPos(3, 2, 3));
        ServerPlayer p = friendOf(helper, dryad, new BlockPos(5, 2, 5));
        helper.setBlock(new BlockPos(7, 1, 7), Blocks.DIRT);
        helper.setBlock(new BlockPos(7, 2, 7), Blocks.OAK_SAPLING);
        BlockState before = helper.getBlockState(new BlockPos(7, 2, 7));
        check(dryad.nurture(helper.getLevel()) >= 1, "she tends the sapling");
        check(helper.getBlockState(new BlockPos(7, 2, 7)) != before, "and it grows");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "amarokStrengthensTamedWolves")
    public static void amarokStrengthensTamedWolves(GameTestHelper helper) {
        Canid amarok = still(helper, GirlSpecies.AMAROK_CANID, new BlockPos(3, 2, 3));
        ServerPlayer p = friendOf(helper, amarok, new BlockPos(5, 2, 5));
        Wolf wild = helper.spawn(EntityType.WOLF, new BlockPos(6, 2, 3));
        wild.setNoAi(true);
        Wolf tame = helper.spawn(EntityType.WOLF, new BlockPos(6, 2, 6));
        tame.setNoAi(true);
        tame.tame(p);
        check(amarok.packMother(helper.getLevel()) == 1, "only her friend's wolf");
        check(tame.hasEffect(MobEffects.DAMAGE_BOOST) && !wild.hasEffect(MobEffects.DAMAGE_BOOST), "the tamed wolf is stronger, the wild one not");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    /** Every mermaid and every sea mollusk breathes water (the tag gen_girl_data.py writes): the sword koi and the sawfin once drowned. */
    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "everySeaGirlBreathesWater")
    public static void everySeaGirlBreathesWater(GameTestHelper helper) {
        for (GirlSpecies s : GirlSpecies.values()) {
            if (s.family() != GirlSpecies.Family.MERFOLK && s.family() != GirlSpecies.Family.MOLLUSK || s == GirlSpecies.SNAIL_MOLLUSK) continue;
            AbstractMonsterGirl girl = still(helper, s, new BlockPos(4, 2, 4));
            check(girl.canBreatheUnderwater(), s.id() + " breathes water");
            girl.discard();
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "yureiHauntsMonstersAtNight")
    public static void yureiHauntsMonstersAtNight(GameTestHelper helper) {
        Undead ghost = still(helper, GirlSpecies.YUREI_UNDEAD, new BlockPos(3, 2, 3));
        ServerPlayer p = friendOf(helper, ghost, new BlockPos(5, 2, 5));
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(7, 2, 5));
        z.setNoAi(true);
        check(ghost.haunt(helper.getLevel(), false) == -1, "by day she rests");
        check(ghost.haunt(helper.getLevel(), true) >= 1 && z.hasEffect(MobEffects.WEAKNESS), "at night the zombie grows weak");
        check(ghost.canFly(), "she floats");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
