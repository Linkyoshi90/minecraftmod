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

/** Habits of a branch (entity.girl.Habits): the full-moon howl, grooming, the trunk's spray, beachcombing, fairy rings, the hare's thump; pressed flowers in the journal. */
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

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "crabsCombTheBeach")
    public static void crabsCombTheBeach(GameTestHelper helper) {
        var level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        AbstractMonsterGirl crab = spawn(helper, GirlSpecies.CRAB_CRUSTACEAN, new BlockPos(4, 2, 2));
        crab.setNoAi(true);
        crab.addAffection(p, Orders.AFFECTION);
        check(Habits.beachcomb(level, crab) == null, "not on sand");
        helper.setBlock(new BlockPos(2, 1, 2), net.minecraft.world.level.block.Blocks.SAND);
        check(Habits.beachcomb(level, crab) == null, "sand, but no sea in sight");
        helper.setBlock(new BlockPos(2, 1, 6), net.minecraft.world.level.block.Blocks.WATER);
        var find = Habits.beachcomb(level, crab);
        check(find != null && !find.isEmpty(), "on the beach she digs something up");
        check(!level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, p.getBoundingBox().inflate(1.5), e -> e.getItem().is(find.getItem())).isEmpty(),
                "at her friend's feet");
        check(Habits.beachcomb(level, crab) == null, "every couple of minutes, not every moment");
        AbstractMonsterGirl gibbon = spawn(helper, GirlSpecies.GIBBON_RODENT, new BlockPos(3, 2, 3));
        gibbon.addAffection(p, Orders.AFFECTION);
        check(Habits.beachcomb(level, gibbon) == null, "a gibbon doesn't dig for shells");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "fairiesLeaveAFairyRing")
    public static void fairiesLeaveAFairyRing(GameTestHelper helper) {
        var level = helper.getLevel();
        for (int x = 2; x <= 8; x++) for (int z = 2; z <= 8; z++) helper.setBlock(new BlockPos(x, 1, z), net.minecraft.world.level.block.Blocks.GRASS_BLOCK);
        helper.setDayTime(18000);                                       // night (checked in this one tick: the clock is shared)
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 9));
        AbstractMonsterGirl fairy = spawn(helper, GirlSpecies.FAIRY_BUG, new BlockPos(5, 2, 5));
        fairy.setNoAi(true);
        fairy.setNoGravity(true);
        check(Habits.fairyRing(level, fairy, true) == 0, "no friend near: no ring");
        fairy.addAffection(p, Orders.AFFECTION);
        fairy.teleportTo(helper.absoluteVec(new BlockPos(5, 2, 5).getBottomCenter()).x, helper.absolutePos(new BlockPos(5, 2, 5)).getY(), helper.absoluteVec(new BlockPos(5, 2, 5).getBottomCenter()).z);
        int ring = Habits.fairyRing(level, fairy, true);
        check(ring == Habits.RING.size(), "a ring of mushrooms comes up around her, got " + ring);
        check(Habits.fairyRing(level, fairy, true) == 0, "one ring every few days");
        BlockPos mid = helper.absolutePos(new BlockPos(5, 2, 5));
        check(Habits.inRing(level, mid), "it's a ring");
        p.teleportTo(mid.getX() + 0.5, mid.getY(), mid.getZ() + 0.5);
        check(Habits.ringLuck(level, p) && p.hasEffect(MobEffects.LUCK), "standing in it at night: lucky");
        check(!Habits.ringLuck(level, p), "once a night");
        AbstractMonsterGirl gibbon = spawn(helper, GirlSpecies.GIBBON_RODENT, new BlockPos(3, 2, 3));
        gibbon.addAffection(p, Orders.AFFECTION);
        check(Habits.fairyRing(level, gibbon, true) == 0, "a gibbon grows no mushrooms");
        HarpyTestUtil.remove(p);
        helper.setDayTime(1000);
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

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "haresThumpAWarning")
    public static void haresThumpAWarning(GameTestHelper helper) {
        var level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        AbstractMonsterGirl hare = spawn(helper, GirlSpecies.JACK_JACKALOPE, new BlockPos(2, 2, 1));
        hare.setNoAi(true);
        var zombie = helper.spawn(net.minecraft.world.entity.EntityType.ZOMBIE, new BlockPos(9, 2, 9));
        zombie.setNoAi(true);
        check(Habits.thump(level, hare) == null, "a stranger keeps her warnings to herself");
        hare.addAffection(p, Orders.AFFECTION);
        check(Habits.thump(level, hare) == zombie, "a zombie coming on her friend: she thumps");
        check(Habits.thump(level, hare) == null, "every half minute, not every moment");
        hare.getPersistentData().remove(Habits.THUMP_AT);
        zombie.teleportTo(p.getX() + 2, p.getY(), p.getZ() + 2);
        check(Habits.thump(level, hare) == null, "on top of them already: they know");
        AbstractMonsterGirl gibbon = spawn(helper, GirlSpecies.GIBBON_RODENT, new BlockPos(3, 2, 3));
        gibbon.addAffection(p, Orders.AFFECTION);
        zombie.teleportTo(helper.absoluteVec(new BlockPos(9, 2, 9).getBottomCenter()).x, p.getY(), helper.absoluteVec(new BlockPos(9, 2, 9).getBottomCenter()).z);
        check(Habits.thump(level, gibbon) == null, "a gibbon doesn't thump");
        zombie.discard();
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "flowersPressedInTheJournal")
    public static void flowersPressedInTheJournal(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(3, 2, 3));
        wolf.setNoAi(true);
        wolf.addAffection(p, Orders.AFFECTION);
        int before = com.linkyoshi90.monstergirls.entity.Journal.book(p).pages().size();
        var poppies = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.POPPY, 2);
        check(!com.linkyoshi90.monstergirls.entity.Journal.press(p, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIRT)), "only flowers press");
        check(com.linkyoshi90.monstergirls.entity.Journal.press(p, poppies) && poppies.getCount() == 1, "a poppy pressed, one taken");
        var pressed = com.linkyoshi90.monstergirls.entity.Journal.pressed(p);
        check(pressed.size() == 1 && pressed.getCompound(0).getString("With").equals(wolf.getName().getString()), "the friend beside them is remembered");
        check(!com.linkyoshi90.monstergirls.entity.Journal.press(p, poppies) && poppies.getCount() == 1, "one of each kind");
        check(com.linkyoshi90.monstergirls.entity.Journal.press(p, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CORNFLOWER)), "a cornflower too");
        check(com.linkyoshi90.monstergirls.entity.Journal.book(p).pages().size() == before + 1, "both on one Pressed Flowers page");
        for (int i = 0; i < 20; i++) {
            var gift = com.linkyoshi90.monstergirls.entity.Journal.missingFlower(p, wolf.getRandom());
            check(gift.is(net.minecraft.tags.ItemTags.SMALL_FLOWERS) && !gift.is(net.minecraft.world.item.Items.POPPY) && !gift.is(net.minecraft.world.item.Items.CORNFLOWER)
                    && !gift.is(net.minecraft.world.item.Items.WITHER_ROSE), "a friend brings one they haven't pressed, got " + gift);
        }
        check(com.linkyoshi90.monstergirls.entity.Journal.flowerKinds() >= 13, "every small flower counts, got " + com.linkyoshi90.monstergirls.entity.Journal.flowerKinds());
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "devotedFriendsTeachALesson")
    public static void devotedFriendsTeachALesson(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        check(com.linkyoshi90.monstergirls.entity.Lessons.TAUGHT.size() == com.linkyoshi90.monstergirls.entity.Clade.values().length, "every branch teaches something");
        var speed = p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        double base = speed.getValue();
        int pages = com.linkyoshi90.monstergirls.entity.Journal.book(p).pages().size();
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(3, 2, 3));
        AbstractMonsterGirl fox = spawn(helper, GirlSpecies.RED_KITSUNE, new BlockPos(4, 2, 3));
        AbstractMonsterGirl hare = spawn(helper, GirlSpecies.JACK_JACKALOPE, new BlockPos(5, 2, 3));
        for (AbstractMonsterGirl g : java.util.List.of(wolf, fox, hare)) g.setNoAi(true);
        check(com.linkyoshi90.monstergirls.entity.Journal.devoted(p, wolf) && com.linkyoshi90.monstergirls.entity.Lessons.knows(p, com.linkyoshi90.monstergirls.entity.Clade.CANIDAE),
                "a devoted wolf teaches her branch's lesson");
        check(speed.getValue() > base, "a wolf's stride: faster");
        check(com.linkyoshi90.monstergirls.entity.Journal.devoted(p, fox) && com.linkyoshi90.monstergirls.entity.Lessons.learned(p).size() == 1, "a second canid has nothing new to teach");
        com.linkyoshi90.monstergirls.entity.Journal.devoted(p, hare);
        var lessons = com.linkyoshi90.monstergirls.entity.Lessons.learned(p);
        check(lessons.size() == 2 && lessons.getCompound(1).getString("Teacher").equals(hare.getName().getString()), "the hare teaches hers, and the journal says who");
        speed.removeModifier(com.linkyoshi90.monstergirls.entity.Lessons.id(com.linkyoshi90.monstergirls.entity.Clade.CANIDAE));
        check(com.linkyoshi90.monstergirls.entity.Lessons.apply(p) == 2 && speed.getValue() > base, "lessons come back after death");
        double once = speed.getValue();
        check(com.linkyoshi90.monstergirls.entity.Lessons.apply(p) == 2 && speed.getValue() == once, "and are never doubled");
        check(com.linkyoshi90.monstergirls.entity.Journal.book(p).pages().size() == pages + 2, "a Lessons page (and the dearest friends page)");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
