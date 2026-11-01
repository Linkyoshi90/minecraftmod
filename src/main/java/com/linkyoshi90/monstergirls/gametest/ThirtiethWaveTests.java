package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Crustacean;
import com.linkyoshi90.monstergirls.entity.girl.Dragon;
import com.linkyoshi90.monstergirls.entity.girl.Elf;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Jackalope;
import com.linkyoshi90.monstergirls.entity.girl.Lizardfolk;
import com.linkyoshi90.monstergirls.entity.girl.Merfolk;
import com.linkyoshi90.monstergirls.entity.girl.Mollusk;
import com.linkyoshi90.monstergirls.entity.girl.Undead;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The thirtieth wave (HARMONTIDE_CONCEPTS.md): new creatures for the cold seas, the dripstone caves and the End. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class ThirtiethWaveTests {
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

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "belugaSingsThemIntoView")
    public static void belugaSingsThemIntoView(GameTestHelper helper) {
        Merfolk b = still(helper, GirlSpecies.BELUGA_MERMAID, new BlockPos(1, 2, 1));
        check(b.sonar(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, b, new BlockPos(5, 2, 5));
        check(b.sonar(helper.getLevel()) == 0 && p.hasEffect(MobEffects.DOLPHINS_GRACE), "she carries her friend along");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "olmLiftsTheDark")
    public static void olmLiftsTheDark(GameTestHelper helper) {
        Merfolk o = still(helper, GirlSpecies.OLM_MERMAID, new BlockPos(1, 2, 1));
        check(!o.lightless(), "only for a friend");
        ServerPlayer p = friendOf(helper, o, new BlockPos(3, 2, 3));
        p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200));
        check(o.lightless() && !p.hasEffect(MobEffects.BLINDNESS) && p.hasEffect(MobEffects.NIGHT_VISION), "she sees for them");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "spiderCrabPins")
    public static void spiderCrabPins(GameTestHelper helper) {
        Crustacean c = still(helper, GirlSpecies.SPIDER_CRAB_CRUSTACEAN, new BlockPos(1, 2, 1));
        check(c.pin(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, c, new BlockPos(5, 2, 5));
        check(c.pin(helper.getLevel()) == 0, "nothing near");
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(c.pin(helper.getLevel()) == 1 && z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "held fast");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "giantKrakenStaysInTheWater")
    public static void giantKrakenStaysInTheWater(GameTestHelper helper) {
        Mollusk k = still(helper, GirlSpecies.GIANT_KRAKEN_MOLLUSK, new BlockPos(1, 2, 1));
        check(k.crush(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, k, new BlockPos(5, 2, 5));
        zombie(helper, new BlockPos(6, 2, 5));
        check(k.crush(helper.getLevel()) == 0, "a monster on land is out of her reach");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "caveGekkoSteadiesAFall")
    public static void caveGekkoSteadiesAFall(GameTestHelper helper) {
        Lizardfolk g = still(helper, GirlSpecies.CAVE_GEKKO_LIZARDFOLK, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, g, new BlockPos(3, 2, 3));
        check(!g.steady(), "not falling");
        p.fallDistance = 6.0F;
        check(g.steady() && p.hasEffect(MobEffects.SLOW_FALLING), "she drifts down");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "drownedGivesBreath")
    public static void drownedGivesBreath(GameTestHelper helper) {
        Undead d = still(helper, GirlSpecies.DROWNED_UNDEAD, new BlockPos(1, 2, 1));
        check(!d.deepBreath(), "only for a friend under water");
        check(!d.isCalm(), "hostile until she is fed");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "famishedEatsWhatYouDrop")
    public static void famishedEatsWhatYouDrop(GameTestHelper helper) {
        Undead f = still(helper, GirlSpecies.FAMISHED_UNDEAD, new BlockPos(1, 2, 1));
        f.setVariantIndex(0);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        check(!f.isCalm() && !f.eatDropped(helper.getLevel()), "she has nothing to eat");
        ItemEntity dropped = helper.spawnItem(Items.BREAD, 2, 2, 2);
        check(!f.eatDropped(helper.getLevel()), "it has to come from someone");
        dropped.setThrower(p);
        check(f.eatDropped(helper.getLevel()), "she eats");
        check(f.isCalm() && "sated".equals(f.getVariantName()) && f.getAffection(p) >= 10, "friendly, and less monstrous");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "earnedLooksNeverWild")
    public static void earnedLooksNeverWild(GameTestHelper helper) {
        AbstractMonsterGirl f = still(helper, GirlSpecies.FAMISHED_UNDEAD, new BlockPos(1, 2, 1));
        for (int i = 0; i < 40; i++) {
            f.randomizeVariant();
            check(!"sated".equals(f.getVariantName()), "she has to be fed to look like that");
        }
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "knockerKnocks")
    public static void knockerKnocks(GameTestHelper helper) {
        Elf k = still(helper, GirlSpecies.KNOCKER_ELF, new BlockPos(1, 2, 1));
        check(k.knock(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, k, new BlockPos(5, 2, 5));
        check(k.knock(helper.getLevel()) == 0, "nothing to say");
        helper.setBlock(new BlockPos(7, 3, 7), Blocks.IRON_ORE);
        check(k.knock(helper.getLevel()) == 1, "ore that way");
        helper.setBlock(new BlockPos(5, 6, 5), Blocks.GRAVEL);
        check(k.knock(helper.getLevel()) == 2, "loose rock comes first");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "moonRabbitPounds")
    public static void moonRabbitPounds(GameTestHelper helper) {
        Jackalope m = still(helper, GirlSpecies.MOON_RABBIT_JACKALOPE, new BlockPos(1, 2, 1));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        ItemStack fruit = new ItemStack(Items.CHORUS_FRUIT, 2);
        m.pound(p, fruit);
        check(fruit.getCount() == 1 && p.getInventory().items.stream().anyMatch(s -> s.is(Items.POTION)), "an elixir");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "kaguyaLightensThem")
    public static void kaguyaLightensThem(GameTestHelper helper) {
        Elf k = still(helper, GirlSpecies.KAGUYA_ELF, new BlockPos(1, 2, 1));
        check(!k.moonsWeight(), "only for a friend");
        ServerPlayer p = friendOf(helper, k, new BlockPos(3, 2, 3));
        check(k.moonsWeight() && p.hasEffect(MobEffects.JUMP), "light as the moon");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "ophanimSeesAll")
    public static void ophanimSeesAll(GameTestHelper helper) {
        Elf o = still(helper, GirlSpecies.OPHANIM_ELF, new BlockPos(1, 2, 1));
        check(o.canFly() && o.allSeeing(helper.getLevel()) == -1, "she flies; only for a friend");
        ServerPlayer p = friendOf(helper, o, new BlockPos(3, 2, 3));
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(o.allSeeing(helper.getLevel()) >= 1 && z.hasEffect(MobEffects.GLOWING), "seen");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "nornForetells")
    public static void nornForetells(GameTestHelper helper) {
        Elf n = still(helper, GirlSpecies.NORN_ELF, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, n, new BlockPos(5, 2, 5));
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(!n.foretell(helper.getLevel()), "nothing is coming");
        z.setTarget(p);
        check(n.foretell(helper.getLevel()) && p.hasEffect(MobEffects.DAMAGE_RESISTANCE), "it is now");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "kunCatchesThem")
    public static void kunCatchesThem(GameTestHelper helper) {
        Dragon k = still(helper, GirlSpecies.KUN_DRAGON, new BlockPos(1, 2, 1));
        check(k.canFly(), "she flies");
        ServerPlayer p = friendOf(helper, k, new BlockPos(3, 2, 3));
        check(k.rescue(helper.getLevel(), p) && p.hasEffect(MobEffects.SLOW_FALLING), "caught");
        check(!k.rescue(helper.getLevel(), p), "once a day");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
