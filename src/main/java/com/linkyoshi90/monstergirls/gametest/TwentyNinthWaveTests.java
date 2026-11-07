package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.Journal;
import com.linkyoshi90.monstergirls.entity.Missing;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.SparrowHarpy;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Armoured;
import com.linkyoshi90.monstergirls.entity.girl.Centaur;
import com.linkyoshi90.monstergirls.entity.girl.Devil;
import com.linkyoshi90.monstergirls.entity.girl.Flora;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Minotaur;
import com.linkyoshi90.monstergirls.entity.girl.Undead;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.interact;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/**
 * The twenty-ninth wave (HARMONTIDE_CONCEPTS.md): friends who miss their player, and ten new creatures for the mushroom fields, the
 * warped forest and the soul sand valley.
 */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class TwentyNinthWaveTests {
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

    /** Puts her last meeting with the player this many days back. */
    private static void apart(AbstractMonsterGirl girl, ServerPlayer player, int days) {
        Missing.meet(girl, player);
        var seen = girl.getPersistentData().getCompound("MissingSeen");
        seen.putLong(player.getUUID().toString(), Missing.clock(player) - days * (long) Missing.DAY);
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "theCloserTheSooner")
    public static void theCloserTheSooner(GameTestHelper helper) {
        check(Missing.stage(10, 30) == 0, "not yet a friend of that kind");
        check(Missing.stage(30, 3) == 0 && Missing.stage(30, 4) == 1 && Missing.stage(30, 8) == 2, "a fond friend takes her time");
        check(Missing.stage(70, 3) == 1 && Missing.stage(70, 6) == 2, "a devoted one sooner");
        check(Missing.stage(100, 2) == 1 && Missing.stage(100, 4) == 2, "the closest soonest");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "onlyDaysOnlineCount")
    public static void onlyDaysOnlineCount(GameTestHelper helper) {
        AbstractMonsterGirl g = still(helper, GirlSpecies.WOLF_CANID, new BlockPos(1, 2, 1));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        g.setAffection(p.getUUID(), 50);
        long now = helper.getLevel().getDayTime();
        var data = p.getData(com.linkyoshi90.monstergirls.registry.ModAttachments.JOURNAL);
        data.putLong("ClockLast", now - 600);
        p.setData(com.linkyoshi90.monstergirls.registry.ModAttachments.JOURNAL, data);
        long before = Missing.clock(p);
        Missing.advance(p);
        check(Missing.clock(p) == before + 600, "time online counts");
        Missing.meet(g, p);
        data = p.getData(com.linkyoshi90.monstergirls.registry.ModAttachments.JOURNAL);
        data.putLong("ClockLast", now - 6L * Missing.DAY);                  // they logged off six days ago, while the server ran on
        p.setData(com.linkyoshi90.monstergirls.registry.ModAttachments.JOURNAL, data);
        Missing.loggedIn(p);
        Missing.advance(p);
        check(Missing.clock(p) - (before + 600) <= 20, "their clock stood still while they were away");
        Missing.meet(g, p);
        check(g.getAffection(p) == 50 && Missing.activeStage(g, p) == 0, "days offline don't");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "shrugsOffAShortAbsence")
    public static void shrugsOffAShortAbsence(GameTestHelper helper) {
        AbstractMonsterGirl g = still(helper, GirlSpecies.WOLF_CANID, new BlockPos(1, 2, 1));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        g.setAffection(p.getUUID(), 50);
        apart(g, p, 1);
        Missing.meet(g, p);
        check(g.getAffection(p) == 50 && Missing.activeStage(g, p) == 0, "a day is nothing");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "sheMissedYou")
    public static void sheMissedYou(GameTestHelper helper) {
        AbstractMonsterGirl g = still(helper, GirlSpecies.WOLF_CANID, new BlockPos(1, 2, 1));
        com.linkyoshi90.monstergirls.entity.MatingSeason.settle(g);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        g.setAffection(p.getUUID(), 50);
        apart(g, p, 5);
        g.setNextGiftTime(helper.getLevel().getGameTime() + 99999);
        Missing.meet(g, p);
        check(Missing.activeStage(g, p) == 1, "she missed them");
        check(g.getAffection(p) == 48, "at some cost in friendship");
        check(g.isGiftDue(), "and she has something for them at once");
        check(Missing.bedBonus(g, p) == 0.25F, "she is keener tonight");
        check(Missing.conceptionChance(g, p) == 0.75F, "the odds are better");
        check(Missing.describe(g, p) != null, "it shows in her bond");
        check(!Journal.book(p).pages().isEmpty() && p.getData(com.linkyoshi90.monstergirls.registry.ModAttachments.JOURNAL).getString("MissedBy").equals(g.getName().getString()), "the journal notes it");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "friendshipNeverFallsBelowTheBed")
    public static void friendshipNeverFallsBelowTheBed(GameTestHelper helper) {
        AbstractMonsterGirl g = still(helper, GirlSpecies.WOLF_CANID, new BlockPos(1, 2, 1));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        g.setAffection(p.getUUID(), 30);
        com.linkyoshi90.monstergirls.entity.MatingSeason.settle(g);
        apart(g, p, 400);
        Missing.meet(g, p);
        check(g.getAffection(p) == Bonded.BED_AFFECTION, "she stays a friend");
        check(Missing.activeStage(g, p) == 2 && Missing.conceptionChance(g, p) == 1.0F && Missing.bedBonus(g, p) == 0.5F, "and misses them badly");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "harpiesMissYouToo")
    public static void harpiesMissYouToo(GameTestHelper helper) {
        SparrowHarpy s = helper.spawn(ModEntities.SPARROW_HARPY.get(), new BlockPos(1, 2, 1));
        s.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        s.setAffection(p.getUUID(), 40);
        Missing.meet(s, p);
        s.getPersistentData().getCompound("MissingSeen").putLong(p.getUUID().toString(), Missing.clock(p) - 4L * Missing.DAY);
        Missing.meet(s, p);
        check(Missing.activeStage(s, p) == 1, "not only the girl families");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "shiitakeTakesTheBlow")
    public static void shiitakeTakesTheBlow(GameTestHelper helper) {
        Flora s = still(helper, GirlSpecies.SHIITAKE_FLORA, new BlockPos(1, 2, 1));
        check(!s.canShareBed(), "out of the bed system");
        check(s.hasOffshoots() && s.takeBlow(helper.getLevel()) && !s.hasOffshoots(), "they take it");
        check(!s.takeBlow(helper.getLevel()), "and need time to regrow");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "shroomotaurFillsABowl")
    public static void shroomotaurFillsABowl(GameTestHelper helper) {
        Minotaur m = still(helper, GirlSpecies.SHROOMOTAUR_MINOTAUR, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, m, new BlockPos(3, 2, 3));
        ItemStack bowls = new ItemStack(Items.BOWL, 2);
        check(m.fillBowl(helper.getLevel(), p, bowls) && bowls.getCount() == 1 && p.getInventory().contains(new ItemStack(Items.MUSHROOM_STEW)), "stew");
        check(!m.fillBowl(helper.getLevel(), p, bowls) && bowls.getCount() == 1, "not again so soon");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "zoglintaurToss")
    public static void zoglintaurToss(GameTestHelper helper) {
        Centaur c = still(helper, GirlSpecies.ZOGLINTAUR_CENTAUR, new BlockPos(1, 2, 1));
        check(c.gore(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, c, new BlockPos(5, 2, 5));
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(c.gore(helper.getLevel()) == 1 && z.getHealth() < z.getMaxHealth() && z.getDeltaMovement().y > 0.5, "up it goes");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "censhroomKeepsPoisonOff")
    public static void censhroomKeepsPoisonOff(GameTestHelper helper) {
        Centaur c = still(helper, GirlSpecies.CENSHROOM_CENTAUR, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, c, new BlockPos(1, 2, 3));
        p.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
        p.startRiding(c, true);
        helper.runAfterDelay(25, () -> {
            check(!p.hasEffect(MobEffects.POISON), "the rider is clean");
            p.stopRiding();
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "pigletteBarters")
    public static void pigletteBarters(GameTestHelper helper) {
        Armoured a = still(helper, GirlSpecies.PIGLETTE_ARMOURED, new BlockPos(1, 2, 1));
        ItemStack gold = new ItemStack(Items.GOLD_INGOT, 2);
        check(a.barter(helper.getLevel(), gold) && gold.getCount() == 1, "she takes the gold");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "firespriteKindles")
    public static void firespriteKindles(GameTestHelper helper) {
        Devil d = still(helper, GirlSpecies.FIRESPRITE_DEVIL, new BlockPos(1, 2, 1));
        check(d.kindle(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, d, new BlockPos(5, 2, 5));
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(d.kindle(helper.getLevel()) == 1 && z.isOnFire() && p.hasEffect(MobEffects.FIRE_RESISTANCE), "fire for them, none for you");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "fiveFeedingsCureHer")
    public static void fiveFeedingsCureHer(GameTestHelper helper) {
        Undead u = still(helper, GirlSpecies.CORDYCEPS_UNDEAD, new BlockPos(1, 2, 1));
        u.setVariantIndex(0);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        check(!u.isCalm(), "hostile at first");
        interact(u, p, new ItemStack(Items.ROTTEN_FLESH));
        check(u.isCalm() && !u.isCured(), "the first bite calms her");
        for (int i = 0; i < 4; i++) {
            check(!u.isCured(), "not yet");
            interact(u, p, new ItemStack(Items.ROTTEN_FLESH));
        }
        check(u.isCured() && u.getAffection(p) >= Orders.AFFECTION, "well again, and a friend");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "lostSoulKnowsTheWayBack")
    public static void lostSoulKnowsTheWayBack(GameTestHelper helper) {
        Undead u = still(helper, GirlSpecies.LOST_SOUL_UNDEAD, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, u, new BlockPos(3, 2, 3));
        check(u.wayBack(helper.getLevel(), p) == -1, "they haven't died");
        p.setLastDeathLocation(Optional.of(GlobalPos.of(helper.getLevel().dimension(), u.blockPosition().east(30))));
        check(u.wayBack(helper.getLevel(), p) == 30, "thirty blocks that way");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "soulClusterReforms")
    public static void soulClusterReforms(GameTestHelper helper) {
        Undead u = still(helper, GirlSpecies.SOUL_CLUSTER_UNDEAD, new BlockPos(1, 2, 1));
        for (int i = 0; i < 3; i++) {
            u.setHealth(1.0F);
            check(u.scatter(helper.getLevel()) && u.getHealth() == u.getMaxHealth(), "she re-forms");
        }
        check(!u.scatter(helper.getLevel()), "three times, and no more");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "soulLeechDrains")
    public static void soulLeechDrains(GameTestHelper helper) {
        Undead u = still(helper, GirlSpecies.SOUL_LEECH_UNDEAD, new BlockPos(1, 2, 1));
        check(u.drain(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, u, new BlockPos(5, 2, 5));
        p.setHealth(10.0F);
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(u.drain(helper.getLevel()) == 1 && z.getHealth() < z.getMaxHealth() && p.getHealth() > 10.0F, "from it to you");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
