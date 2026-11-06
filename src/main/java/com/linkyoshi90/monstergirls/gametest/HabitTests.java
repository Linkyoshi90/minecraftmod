package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Habits;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Habits of a branch (entity.girl.Habits): the full-moon howl, grooming. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class HabitTests {

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "canidsHowlAtTheFullMoon")
    public static void canidsHowlAtTheFullMoon(GameTestHelper helper) {
        var level = helper.getLevel();
        level.setWeatherParameters(6000, 0, false, false);
        helper.setDayTime(18000);                                       // midnight of the first night: a full moon
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(3, 2, 3));
        AbstractMonsterGirl fox = spawn(helper, GirlSpecies.RED_KITSUNE, new BlockPos(2, 2, 1));
        AbstractMonsterGirl hyena = spawn(helper, GirlSpecies.HYENA_CANID, new BlockPos(6, 2, 6));
        fox.addAffection(p, Orders.AFFECTION);
        int before = fox.getAffection(p);
        check(Habits.howl(level, wolf) >= 2 && fox.getPersistentData().getLong(Habits.HOWLED) != 0 && hyena.getPersistentData().getLong(Habits.HOWLED) == 0,
                "the wolf howls and the fox answers; the hyena, a feliform, doesn't");   // (canids of tests nearby may join in)
        check(fox.getAffection(p) == before + 2, "her friend under the moon feels it too");
        check(Habits.howl(level, fox) == 0, "once a night");
        HarpyTestUtil.remove(p);
        helper.setDayTime(1000);                                        // the clock is shared with the tests alongside
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "primatesGroomAFriend")
    public static void primatesGroomAFriend(GameTestHelper helper) {
        var level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        AbstractMonsterGirl gibbon = spawn(helper, GirlSpecies.GIBBON_RODENT, new BlockPos(3, 2, 2));
        check(Habits.groom(level, gibbon) == null, "a stranger keeps her hands to herself");
        gibbon.addAffection(p, Orders.AFFECTION);
        p.addEffect(new MobEffectInstance(MobEffects.POISON, 600));
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600));
        check(Habits.groom(level, gibbon) != null && !p.hasEffect(MobEffects.POISON) && p.hasEffect(MobEffects.MOVEMENT_SPEED), "she picks out the poison and leaves the good");
        p.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600));
        check(Habits.groom(level, gibbon) == null && p.hasEffect(MobEffects.HUNGER), "every two minutes, not every moment");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "elephantsDouseAFriendOnFire")
    public static void elephantsDouseAFriendOnFire(GameTestHelper helper) {
        var level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        AbstractMonsterGirl ele = spawn(helper, GirlSpecies.ELEPHANT_CENTAUR, new BlockPos(4, 2, 2));
        p.setRemainingFireTicks(200);
        check(!Habits.douse(level, ele), "a stranger lets you burn");
        ele.addAffection(p, Orders.AFFECTION);
        p.clearFire();
        check(!Habits.douse(level, ele), "nothing to put out");
        p.setRemainingFireTicks(200);
        check(Habits.douse(level, ele) && !p.isOnFire(), "she sprays the flames out");
        p.setRemainingFireTicks(200);
        check(!Habits.douse(level, ele) && p.isOnFire(), "every half minute, not every moment");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "friendsCheerForAnAdvancement")
    public static void friendsCheerForAnAdvancement(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        AbstractMonsterGirl friend = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(3, 2, 3));
        AbstractMonsterGirl stranger = spawn(helper, GirlSpecies.RED_KITSUNE, new BlockPos(5, 2, 5));
        friend.addAffection(p, Orders.AFFECTION);
        int before = friend.getAffection(p);
        var cheered = com.linkyoshi90.monstergirls.entity.Cheers.cheer(p);
        check(cheered.contains(friend) && !cheered.contains(stranger), "her friend cheers, a stranger doesn't");
        check(friend.getAffection(p) == before, "only a celebration: friendship is counted by the deed itself");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aNameOfHerOwn")
    public static void aNameOfHerOwn(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        AbstractMonsterGirl friend = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(3, 2, 3));
        AbstractMonsterGirl stranger = spawn(helper, GirlSpecies.RED_KITSUNE, new BlockPos(5, 2, 5));
        var name = net.minecraft.network.chat.Component.literal("Fluffy");
        check(!com.linkyoshi90.monstergirls.entity.Names.named(p, stranger, name), "a stranger just gets a name");
        friend.addAffection(p, Orders.AFFECTION);
        int before = friend.getAffection(p);
        check(com.linkyoshi90.monstergirls.entity.Names.named(p, friend, name) && friend.getAffection(p) == before + com.linkyoshi90.monstergirls.entity.Names.CLOSER,
                "a friend loves her first name");
        check(!com.linkyoshi90.monstergirls.entity.Names.named(p, friend, name), "the first time only");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "theJournalRemembersTheDearest")
    public static void theJournalRemembersTheDearest(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(3, 2, 3));
        girl.setCustomName(net.minecraft.network.chat.Component.literal("Rin"));
        check(com.linkyoshi90.monstergirls.entity.Journal.devoted(p, girl) && !com.linkyoshi90.monstergirls.entity.Journal.devoted(p, girl), "noted once");
        var pages = com.linkyoshi90.monstergirls.entity.Journal.book(p).pages();
        check(pages.stream().anyMatch(pg -> pg.raw().toString().contains("journal.monstergirls.dearest.title") && pg.raw().toString().contains("Rin")),
                "her name on the dearest friends page");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
