package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Flirting;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Bug;
import com.linkyoshi90.harmontide.entity.girl.Canid;
import com.linkyoshi90.harmontide.entity.girl.Centaur;
import com.linkyoshi90.harmontide.entity.girl.Crustacean;
import com.linkyoshi90.harmontide.entity.girl.Devil;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Rodent;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** The thirty-fifth wave (HARMONTIDE_CONCEPTS.md): twelve creatures, flirting, and sitting girls who save themselves. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class ThirtyFifthWaveTests {
    private static ServerPlayer friendOf(GameTestHelper helper, AbstractMonsterGirl girl, BlockPos at, int affection) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, at);
        girl.addAffection(p, affection);
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

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "hyenaScavenges")
    public static void hyenaScavenges(GameTestHelper helper) {
        Canid h = still(helper, GirlSpecies.HYENA_CANID, new BlockPos(1, 2, 1));
        check(h.scavenge(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, h, new BlockPos(5, 2, 5), Orders.AFFECTION);
        helper.spawnItem(Items.ROTTEN_FLESH, 5, 2, 6);
        check(h.scavenge(helper.getLevel()) == 1, "eaten");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "tanukiPaysOnceADay")
    public static void tanukiPaysOnceADay(GameTestHelper helper) {
        Rodent t = still(helper, GirlSpecies.TANUKI_RODENT, new BlockPos(1, 2, 1));
        check(t.leafMoney(3) && !t.leafMoney(3) && t.leafMoney(4), "once a day");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "skunkSprays")
    public static void skunkSprays(GameTestHelper helper) {
        Rodent s = still(helper, GirlSpecies.SKUNK_RODENT, new BlockPos(1, 2, 1));
        check(!s.spray(helper.getLevel()), "unprovoked");
        Zombie z = zombie(helper, new BlockPos(2, 2, 2));
        s.setLastHurtByMob(z);
        check(s.spray(helper.getLevel()) && z.hasEffect(MobEffects.BLINDNESS), "sprayed");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "elephantTrumpets")
    public static void elephantTrumpets(GameTestHelper helper) {
        Centaur e = still(helper, GirlSpecies.ELEPHANT_CENTAUR, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, e, new BlockPos(5, 2, 5), Orders.AFFECTION);
        zombie(helper, new BlockPos(6, 2, 5));
        check(e.trumpet(helper.getLevel()) == 0, "not for one");
        zombie(helper, new BlockPos(6, 2, 6));
        check(e.trumpet(helper.getLevel()) == 2, "thrown back");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "lobsterMoults")
    public static void lobsterMoults(GameTestHelper helper) {
        Crustacean l = still(helper, GirlSpecies.LOBSTER_CRUSTACEAN, new BlockPos(1, 2, 1));
        check(!l.moult(1), "not hurt");
        l.setHealth(2);
        check(l.moult(1) && l.getHealth() == l.getMaxHealth(), "whole again");
        l.setHealth(2);
        check(!l.moult(1), "once a day");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "ladybugBringsLuck")
    public static void ladybugBringsLuck(GameTestHelper helper) {
        Bug b = still(helper, GirlSpecies.LADYBUG_BUG, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, b, new BlockPos(3, 2, 3), Orders.AFFECTION);
        check(b.luckySpots(helper.getLevel()) == 0 && p.hasEffect(MobEffects.LUCK), "luck");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "goblinHasADeal")
    public static void goblinHasADeal(GameTestHelper helper) {
        Devil g = still(helper, GirlSpecies.GOBLIN_DEVIL, new BlockPos(1, 2, 1));
        ItemStack deal = g.badDeal();
        check(!deal.isEmpty(), "something from her pockets");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "trollIsStoneInTheSun")
    public static void trollIsStoneInTheSun(GameTestHelper helper) {
        Devil t = still(helper, GirlSpecies.TROLL_DEVIL, new BlockPos(1, 2, 1));
        check(t.flatten(helper.getLevel()) == -1, "only for a friend");
        helper.getLevel().setDayTime(18000);
        check(!t.isStone(), "not at night");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "flirtingOnlyWithGrownFriends")
    public static void flirtingOnlyWithGrownFriends(GameTestHelper helper) {
        AbstractMonsterGirl g = still(helper, GirlSpecies.WOLF_CANID, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, g, new BlockPos(3, 2, 3), 10);
        check(!Flirting.canFlirt(g, p), "not a close friend yet");
        g.addAffection(p, 30);
        check(Flirting.canFlirt(g, p), "a close friend");
        g.setFather(p.getUUID());
        check(!Flirting.canFlirt(g, p), "never with her father");
        AbstractMonsterGirl small = still(helper, GirlSpecies.FAIRY_BUG, new BlockPos(2, 2, 1));
        small.addAffection(p, 100);
        check(!Flirting.canFlirt(small, p), "anyone childlike stays out");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "sittingGirlGetsUpWhenBurning")
    public static void sittingGirlGetsUpWhenBurning(GameTestHelper helper) {
        AbstractMonsterGirl g = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(1, 2, 1));
        var goal = new com.linkyoshi90.harmontide.entity.ai.SitWhenOrderedGoal<>(g);
        g.setOrderedToSit(true);
        check(goal.canUse(), "she sits");
        g.igniteForSeconds(5);
        check(!goal.canUse(), "not while on fire");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "sittingMermaidGoesForWater")
    public static void sittingMermaidGoesForWater(GameTestHelper helper) {
        com.linkyoshi90.harmontide.entity.girl.Merfolk g = spawn(helper, GirlSpecies.KOI_MERMAID, new BlockPos(1, 2, 1));
        var goal = new com.linkyoshi90.harmontide.entity.ai.SitWhenOrderedGoal<>(g);
        g.setOrderedToSit(true);
        check(goal.canUse(), "she sits");
        g.setDryTicks(g.dryLimit());
        check(g.needsWater() && !goal.canUse(), "not while drying out");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "harpiesAndFrogsBlush")
    public static void harpiesAndFrogsBlush(GameTestHelper helper) {
        var owl = helper.spawn(com.linkyoshi90.harmontide.registry.ModEntities.OWL_HARPY.get(), new BlockPos(1, 2, 1));
        owl.blushFor(3);
        check(owl.isBlushing(), "she blushes");
        helper.runAfterDelay(6, () -> {
            check(!owl.isBlushing(), "and it passes");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "wave36Creatures")
    public static void wave36Creatures(GameTestHelper helper) {
        com.linkyoshi90.harmontide.entity.girl.Crustacean shrimp = still(helper, GirlSpecies.MANTIS_SHRIMP_CRUSTACEAN, new BlockPos(1, 2, 1));
        Rodent mongoose = still(helper, GirlSpecies.MONGOOSE_RODENT, new BlockPos(2, 2, 1));
        com.linkyoshi90.harmontide.entity.girl.Elf gnome = still(helper, GirlSpecies.GNOME_ELF, new BlockPos(3, 2, 1));
        ServerPlayer p = friendOf(helper, shrimp, new BlockPos(5, 2, 5), Orders.AFFECTION);
        mongoose.addAffection(p, Orders.AFFECTION);
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(shrimp.punch(helper.getLevel()) == 1 && z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "the punch");
        mongoose.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.POISON, 100));
        check(!mongoose.hasEffect(MobEffects.POISON), "poison doesn't touch her");
        p.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.POISON, 100));
        check(mongoose.antivenom() && !p.hasEffect(MobEffects.POISON), "and it comes out of her friend");
        check(gnome.tendGarden(helper.getLevel()) == -1, "only for a friend");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "wave38Creatures")
    public static void wave38Creatures(GameTestHelper helper) {
        Bug t = still(helper, GirlSpecies.TARDIGRADE_BUG, new BlockPos(1, 2, 1));
        t.setHealth(1);
        check(t.tun(5) && t.getHealth() == t.getMaxHealth(), "back");
        t.setHealth(1);
        check(!t.tun(5), "once a day");
        com.linkyoshi90.harmontide.entity.girl.Construct s = still(helper, GirlSpecies.SNOWGIRL_CONSTRUCT, new BlockPos(2, 2, 1));
        ServerPlayer p = friendOf(helper, s, new BlockPos(5, 2, 5), 30);
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(s.snowball(helper.getLevel()) == 1 && z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "pelted");
        String id = p.getUUID().toString();
        var missing = com.linkyoshi90.harmontide.entity.Missing.class;
        check(!com.linkyoshi90.harmontide.entity.Missing.anniversary(helper.getLevel(), s, p, id, 100), "the day they met");
        check(!com.linkyoshi90.harmontide.entity.Missing.anniversary(helper.getLevel(), s, p, id, 110), "not yet");
        check(com.linkyoshi90.harmontide.entity.Missing.anniversary(helper.getLevel(), s, p, id, 130), "a month");
        check(!com.linkyoshi90.harmontide.entity.Missing.anniversary(helper.getLevel(), s, p, id, 130), "once");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "grizzlyStaysInHerDen")
    public static void grizzlyStaysInHerDen(GameTestHelper helper) {
        Canid bear = still(helper, GirlSpecies.GRIZZLY_CANID, new BlockPos(2, 2, 2));
        check(helper.getLevel().canSeeSky(bear.blockPosition()) || !bear.seekDen(helper.getLevel()), "under a roof already: no need to move");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "harpiesFlirtToo")
    public static void harpiesFlirtToo(GameTestHelper helper) {
        var owl = helper.spawn(com.linkyoshi90.harmontide.registry.ModEntities.OWL_HARPY.get(), new BlockPos(1, 2, 1));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        owl.addAffection(p, 10);
        check(!Flirting.canFlirt(owl, p), "not a close friend yet");
        owl.addAffection(p, 30);
        check(Flirting.canFlirt(owl, p) == (owl.getBbHeight() >= com.linkyoshi90.harmontide.entity.Bonded.CHILDLIKE_HEIGHT), "a grown close friend");
        owl.setFather(p.getUUID());
        check(!Flirting.canFlirt(owl, p), "never with her father");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
