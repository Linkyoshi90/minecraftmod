package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.CoinMagpieHarpy;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.TricksterRavenHarpy;
import com.linkyoshi90.harmontide.entity.girl.Armoured;
import com.linkyoshi90.harmontide.entity.girl.Canid;
import com.linkyoshi90.harmontide.entity.girl.Centaur;
import com.linkyoshi90.harmontide.entity.girl.Construct;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Jackalope;
import com.linkyoshi90.harmontide.entity.girl.Kinship;
import com.linkyoshi90.harmontide.entity.girl.Merfolk;
import com.linkyoshi90.harmontide.entity.girl.Minotaur;
import com.linkyoshi90.harmontide.entity.girl.Nekomata;
import com.linkyoshi90.harmontide.entity.girl.Rodent;
import com.linkyoshi90.harmontide.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.interact;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/**
 * The tenth Harmontide wave (HARMONTIDE_CONCEPTS.md): Harmontide's own creatures. Tusktaur, aurorantler, rhiwool,
 * meloncollie, needlehog, clockhare, axolume, aegis boar, blaze cat, gargoyle, coin magpie, trickster raven.
 */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class TenthWaveTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "tusktaurSweepsAndKeepsWarm")
    public static void tusktaurSweepsAndKeepsWarm(GameTestHelper helper) {
        Centaur t = spawn(helper, GirlSpecies.TUSKTAUR_CENTAUR, new BlockPos(5, 2, 3));
        t.setNoAi(true);
        t.setYRot(0);
        t.setYHeadRot(0);
        Zombie a = helper.spawn(EntityType.ZOMBIE, new BlockPos(5, 2, 5));
        Zombie b = helper.spawn(EntityType.ZOMBIE, new BlockPos(6, 2, 5));
        a.setNoAi(true);
        b.setNoAi(true);
        check(t.sweep(null) >= 1, "her tusks sweep what stands in front of her");
        check(!t.canFreeze(), "she never freezes");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "aurorantlerLightsTheNight")
    public static void aurorantlerLightsTheNight(GameTestHelper helper) {
        helper.getLevel().setDayTime(18000);
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 4));
        Centaur deer = spawn(helper, GirlSpecies.AURORANTLER_CENTAUR, new BlockPos(5, 2, 6));
        deer.setNoAi(true);
        deer.addAffection(friend, Orders.AFFECTION);
        helper.succeedWhen(() -> {                                       // the sky darkens on the next tick; her own tick lights the snow
            check(helper.getLevel().getBlockState(deer.blockPosition()).is(Blocks.LIGHT), "her antlers light the snow");
            check(friend.hasEffect(MobEffects.NIGHT_VISION), "her friend sees in the dark");
            HarpyTestUtil.remove(friend);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "rhiwoolShearsAndCharges")
    public static void rhiwoolShearsAndCharges(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        Minotaur rhino = spawn(helper, GirlSpecies.RHIWOOL_MINOTAUR, new BlockPos(5, 2, 2));
        interact(rhino, p, new ItemStack(Items.SHEARS));
        check(rhino.isShorn(), "shears take her wool");
        check(!helper.getLevel().getEntitiesOfClass(ItemEntity.class, rhino.getBoundingBox().inflate(3), e -> e.getItem().is(Items.BLACK_WOOL)).isEmpty(),
                "black wool from a black rhiwool");
        HarpyTestUtil.remove(p);
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(5, 2, 9));
        z.setNoAi(true);
        rhino.startCharge(z.position());
        check(rhino.isCharging(), "head down, she charges");
        helper.succeedWhen(() -> check(z.getHealth() < z.getMaxHealth(), "the charge lands: " + z.getHealth()));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "meloncollieHerdsAndPlants")
    public static void meloncollieHerdsAndPlants(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 8));
        helper.setBlock(new BlockPos(1, 2, 1), Blocks.OAK_FENCE_GATE);
        helper.setBlock(new BlockPos(7, 1, 7), Blocks.FARMLAND);
        Canid dog = spawn(helper, GirlSpecies.MELONCOLLIE_CANID, new BlockPos(7, 2, 5));
        dog.setNoAi(true);
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(9, 2, 9));
        check(dog.herd(helper.getLevel()) == null, "she herds only for a friend");
        dog.addAffection(friend, Orders.AFFECTION);
        check(dog.herd(helper.getLevel()) == cow, "she goes after the stray");
        check(dog.plantMelon(helper.getLevel()) && helper.getBlockState(new BlockPos(7, 2, 7)).is(Blocks.MELON_STEM), "a melon seed in the farmland");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "needlehogCurlsAndPricks")
    public static void needlehogCurlsAndPricks(GameTestHelper helper) {
        Rodent hog = spawn(helper, GirlSpecies.NEEDLEHOG_RODENT, new BlockPos(5, 2, 5));
        hog.setNoAi(true);
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(6, 2, 5));
        z.setNoAi(true);
        hog.hurt(helper.getLevel().damageSources().mobAttack(z), 1.0F);
        check(hog.isCurled(), "hit, she rolls into a ball");
        float before = z.getHealth();
        check(!hog.hurt(helper.getLevel().damageSources().mobAttack(z), 4.0F), "nothing gets through her quills");
        check(z.getHealth() < before, "and whoever hits her is pricked");
        check(hog.wantsToPickUp(new ItemStack(Items.PAPER)), "paper is for her notes");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "clockhareKnowsWhenPhantomsCome")
    public static void clockhareKnowsWhenPhantomsCome(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 4));
        Jackalope hare = spawn(helper, GirlSpecies.CLOCKHARE_JACKALOPE, new BlockPos(5, 2, 6));
        hare.setNoAi(true);
        check(!Jackalope.phantomsDue(p), "a rested player is safe");
        p.getStats().setValue(p, Stats.CUSTOM.get(Stats.TIME_SINCE_REST), Jackalope.PHANTOM_TICKS + 100);
        check(Jackalope.phantomsDue(p), "three nights awake: the phantoms are due");
        hare.chime(helper.getLevel(), p, "phantoms");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 120, batch = "axolumeGlowsAndHeals")
    public static void axolumeGlowsAndHeals(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 4));
        Merfolk axo = spawn(helper, GirlSpecies.AXOLUME_MERMAID, new BlockPos(5, 2, 6));
        axo.setNoAi(true);
        axo.addAffection(friend, Orders.AFFECTION);
        helper.succeedWhen(() -> {
            check(helper.getLevel().getBlockState(axo.blockPosition()).is(Blocks.LIGHT), "she lights the water");
            check(friend.hasEffect(MobEffects.REGENERATION), "her friend heals beside her");
            HarpyTestUtil.remove(friend);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "aegisBoarShieldsHerFriend")
    public static void aegisBoarShieldsHerFriend(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 4));
        Armoured boar = spawn(helper, GirlSpecies.AEGIS_BOAR_ARMOURED, new BlockPos(5, 2, 6));
        boar.setNoAi(true);
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(2, 2, 2));
        z.setNoAi(true);
        check(Armoured.shield(friend, helper.getLevel().damageSources().mobAttack(z), 9.0F) == 9.0F, "a stranger she does not shield");
        boar.addAffection(friend, Orders.AFFECTION);
        float before = boar.getHealth();
        float left = Armoured.shield(friend, helper.getLevel().damageSources().mobAttack(z), 9.0F);
        check(Math.abs(left - 6.0F) < 0.01F, "a third of the blow is hers: " + left);
        check(boar.getHealth() < before && boar.getTarget() == z, "she takes it, and turns on the zombie");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "blazeCatKindlesFires")
    public static void blazeCatKindlesFires(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 4));
        BlockPos fire = new BlockPos(7, 2, 6);
        helper.setBlock(fire, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
        Nekomata cat = spawn(helper, GirlSpecies.BLAZE_CAT_NEKOMATA, new BlockPos(5, 2, 6));
        cat.setNoAi(true);
        cat.addAffection(friend, Orders.AFFECTION);
        check(cat.fireImmune(), "fireproof");
        check(cat.kindle(helper.getLevel()) >= 1 && helper.getBlockState(fire).getValue(CampfireBlock.LIT), "the campfire lights up");
        check(friend.hasEffect(MobEffects.FIRE_RESISTANCE), "her friend takes no harm from fire");
        check(Kinship.kin(GirlSpecies.BLAZE_CAT_NEKOMATA).contains(EntityType.BLAZE), "blazes are her kin");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "gargoyleIsStoneByDay")
    public static void gargoyleIsStoneByDay(GameTestHelper helper) {
        helper.getLevel().setDayTime(1000);
        Construct g = spawn(helper, GirlSpecies.GARGOYLE_CONSTRUCT, new BlockPos(5, 2, 5));
        helper.succeedWhen(() -> {
            check(g.isStone(), "by day she is stone");
            check(!g.hurt(helper.getLevel().damageSources().generic(), 5.0F), "and nothing hurts her");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "coinMagpiePaysFromHerHoard")
    public static void coinMagpiePaysFromHerHoard(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 4));
        CoinMagpieHarpy magpie = helper.spawn(ModEntities.COIN_MAGPIE_HARPY.get(), new BlockPos(5, 2, 6));
        magpie.setNoAi(true);
        magpie.getHoard().addItem(new ItemStack(Items.EMERALD));
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.GOLD_NUGGET));
        magpie.interact(p, net.minecraft.world.InteractionHand.MAIN_HAND);
        check(p.getInventory().contains(s -> s.is(Items.EMERALD)), "a nugget buys the emerald from her hoard");
        check(magpie.getHoard().isEmpty(), "and it is gone from the hoard");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "jadeShowsBondAndPregnancy")
    public static void jadeShowsBondAndPregnancy(GameTestHelper helper) {
        check(com.linkyoshi90.harmontide.compat.BondInfo.nextMilestone(0) == 10 && com.linkyoshi90.harmontide.compat.BondInfo.nextMilestone(12) == 25
                && com.linkyoshi90.harmontide.compat.BondInfo.nextMilestone(60) == 100 && com.linkyoshi90.harmontide.compat.BondInfo.nextMilestone(100) == 100,
                "friendship counts toward the next milestone");
        Minotaur cow = spawn(helper, GirlSpecies.CATTLE_MINOTAUR, new BlockPos(3, 2, 3));
        Minotaur bull = spawn(helper, GirlSpecies.CATTLE_MINOTAUR, new BlockPos(7, 2, 7));
        bull.setMale(true);
        check(com.linkyoshi90.harmontide.compat.BondInfo.canCarry(cow) && !com.linkyoshi90.harmontide.compat.BondInfo.canCarry(bull),
                "a cow can be expecting, a bull never");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "tricksterRavenLuresMonsters")
    public static void tricksterRavenLuresMonsters(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        TricksterRavenHarpy raven = helper.spawn(ModEntities.TRICKSTER_RAVEN_HARPY.get(), new BlockPos(3, 2, 3));
        raven.setNoAi(true);
        raven.addAffection(friend, Orders.AFFECTION);
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(8, 2, 8));
        z.setTarget(friend);
        check(raven.lure(helper.getLevel(), friend) == 1 && z.getTarget() == null, "she calls the zombie away");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }
}
