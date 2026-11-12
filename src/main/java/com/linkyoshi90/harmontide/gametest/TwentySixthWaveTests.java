package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.AlkonostHarpy;
import com.linkyoshi90.harmontide.entity.BennuHarpy;
import com.linkyoshi90.harmontide.entity.EmuHarpy;
import com.linkyoshi90.harmontide.entity.GarudaHarpy;
import com.linkyoshi90.harmontide.entity.LechuzaHarpy;
import com.linkyoshi90.harmontide.entity.MacawHarpy;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.RocHarpy;
import com.linkyoshi90.harmontide.entity.ZizHarpy;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Centaur;
import com.linkyoshi90.harmontide.entity.girl.Devil;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Mollusk;
import com.linkyoshi90.harmontide.entity.girl.Nekomata;
import com.linkyoshi90.harmontide.entity.girl.Undead;
import com.linkyoshi90.harmontide.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/**
 * The twenty-sixth Harmontide wave (HARMONTIDE_CONCEPTS.md): eight birds (emu, macaw, roc, ziz, bennu, Garuda, La Lechuza, alkonost), the smilodon, the
 * caribou, Lilith, the Min Min light, the Hand and Wrath.
 */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class TwentySixthWaveTests {

    private static ServerPlayer friendOf(GameTestHelper helper, AbstractMonsterGirl girl, BlockPos at) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, at);
        girl.addAffection(p, Orders.AFFECTION);
        return p;
    }

    private static ServerPlayer friendOf(GameTestHelper helper, com.linkyoshi90.harmontide.entity.AbstractHarpy bird, BlockPos at) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, at);
        bird.addAffection(p, Orders.AFFECTION);
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

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "rocCarriesOffAMonster")
    public static void rocCarriesOffAMonster(GameTestHelper helper) {
        RocHarpy r = helper.spawn(ModEntities.ROC_HARPY.get(), new BlockPos(5, 2, 5));
        check(r.seize(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, r, new BlockPos(7, 2, 5));
        Zombie z = zombie(helper, new BlockPos(6, 2, 7));
        check(r.seize(helper.getLevel()) == 1 && r.carried() == z, "she takes it");
        double from = z.getY();
        helper.succeedWhen(() -> {
            check(r.carried() == null, "she lets go in the end");
            check(z.getY() > from + 1.0 || !z.isAlive(), "from higher up");
            HarpyTestUtil.remove(f);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "emuKicksAndPilfers")
    public static void emuKicksAndPilfers(GameTestHelper helper) {
        EmuHarpy e = helper.spawn(ModEntities.EMU_HARPY.get(), new BlockPos(5, 2, 5));
        e.setNoAi(true);
        BlockPos at = new BlockPos(6, 2, 7);
        helper.setBlock(at, Blocks.CHEST);
        ChestBlockEntity box = (ChestBlockEntity) helper.getBlockEntity(at);
        box.setItem(0, new ItemStack(Items.BREAD, 2));
        check(e.pilfer(helper.getLevel()) == 1 && box.getItem(0).getCount() == 1, "a loaf gone");
        ServerPlayer f = friendOf(helper, e, new BlockPos(5, 2, 7));
        check(e.pilfer(helper.getLevel()) == 0, "not once they are friends");
        Zombie z = zombie(helper, new BlockPos(4, 2, 7));
        check(e.kickAway(helper.getLevel()) == 1 && z.getHealth() < z.getMaxHealth(), "the kick lands");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "macawGivesThemAway")
    public static void macawGivesThemAway(GameTestHelper helper) {
        MacawHarpy m = helper.spawn(ModEntities.MACAW_HARPY.get(), new BlockPos(1, 2, 1));
        m.setNoAi(true);
        check(m.hiddenCall(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, m, new BlockPos(2, 2, 2));
        Zombie seen = zombie(helper, new BlockPos(3, 2, 3));
        check(m.hiddenCall(helper.getLevel()) == 0, "nothing hidden");
        HarpyTestUtil.remove(f);
        check(!seen.hasEffect(MobEffects.GLOWING), "a monster in plain sight is no news");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "bennuRisesAgain")
    public static void bennuRisesAgain(GameTestHelper helper) {
        BennuHarpy b = helper.spawn(ModEntities.BENNU_HARPY.get(), new BlockPos(5, 2, 5));
        b.setNoAi(true);
        check(b.sunrise(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, b, new BlockPos(7, 2, 5));
        f.setHealth(4.0F);
        check(b.sunrise(helper.getLevel()) == 1 && f.getHealth() == f.getMaxHealth(), "healed whole");
        check(b.sunrise(helper.getLevel()) == 0, "once a day");
        b.hurt(helper.getLevel().damageSources().generic(), 1000.0F);
        check(b.isAlive() && b.getHealth() == b.getMaxHealth(), "she gets up again");
        check(!b.rise(helper.getLevel()), "once a day");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "garudaHatesSerpents")
    public static void garudaHatesSerpents(GameTestHelper helper) {
        GarudaHarpy g = helper.spawn(ModEntities.GARUDA_HARPY.get(), new BlockPos(5, 2, 5));
        g.setNoAi(true);
        check(!g.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0)) && !g.hasEffect(MobEffects.POISON), "poison does not hold on her");
        ServerPlayer f = friendOf(helper, g, new BlockPos(7, 2, 5));
        f.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
        check(g.serpentBane(helper.getLevel()) == 0 && !f.hasEffect(MobEffects.POISON), "nor on her friend");
        Silverfish s = helper.spawn(EntityType.SILVERFISH, new BlockPos(5, 2, 7));
        s.setNoAi(true);
        check(g.serpentBane(helper.getLevel()) == 1 && !s.isAlive(), "the serpent falls");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "lechuzaCriesAtNight")
    public static void lechuzaCriesAtNight(GameTestHelper helper) {
        LechuzaHarpy l = helper.spawn(ModEntities.LECHUZA_HARPY.get(), new BlockPos(1, 2, 1));
        l.setNoAi(true);
        check(l.cry(helper.getLevel(), true) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, l, new BlockPos(5, 2, 5));
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(l.cry(helper.getLevel(), false) == 0, "not by day");
        check(l.cry(helper.getLevel(), true) == 1 && z.hasEffect(MobEffects.GLOWING), "she shows it up");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "alkonostSingsThemToSleep")
    public static void alkonostSingsThemToSleep(GameTestHelper helper) {
        AlkonostHarpy a = helper.spawn(ModEntities.ALKONOST_HARPY.get(), new BlockPos(1, 2, 1));
        a.setNoAi(true);
        check(a.song(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, a, new BlockPos(5, 2, 5));
        f.setHealth(5.0F);
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(a.song(helper.getLevel()) == 2 && f.hasEffect(MobEffects.REGENERATION) && z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "her friend mends, the monster nods off");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "zizNeedsAFriend")
    public static void zizNeedsAFriend(GameTestHelper helper) {
        ZizHarpy z = helper.spawn(ModEntities.ZIZ_HARPY.get(), new BlockPos(5, 2, 5));
        z.setNoAi(true);
        check(z.clearSkies(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, z, new BlockPos(7, 2, 5));
        helper.getLevel().setWeatherParameters(0, 6000, true, false);
        check(z.clearSkies(helper.getLevel()) == 1 && !helper.getLevel().getLevelData().isRaining(), "the rain stops");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "smilodonSprings")
    public static void smilodonSprings(GameTestHelper helper) {
        Nekomata s = still(helper, GirlSpecies.SMILODON_NEKOMATA, new BlockPos(1, 2, 1));
        check(s.ambush(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, s, new BlockPos(5, 2, 5));
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(s.ambush(helper.getLevel()) == 1 && z.getHealth() < z.getMaxHealth(), "a heavy blow");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "caribouLovesSnow")
    public static void caribouLovesSnow(GameTestHelper helper) {
        Centaur c = still(helper, GirlSpecies.CARIBOU_CENTAUR, new BlockPos(5, 2, 5));
        check(c.snowStride(helper.getLevel()) == -1, "only with a rider");
        ServerPlayer f = friendOf(helper, c, new BlockPos(5, 2, 5));
        f.startRiding(c, true);
        check(c.snowStride(helper.getLevel()) == 0, "not off the snow");
        helper.setBlock(new BlockPos(5, 1, 5), Blocks.SNOW_BLOCK);
        check(c.snowStride(helper.getLevel()) == 1 && c.hasEffect(MobEffects.MOVEMENT_SPEED), "faster on it");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "lilithWeakensByNight")
    public static void lilithWeakensByNight(GameTestHelper helper) {
        Devil l = still(helper, GirlSpecies.LILITH_DEVIL, new BlockPos(1, 2, 1));
        check(l.canFly(), "she flies");
        check(l.nightfall(helper.getLevel(), true) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, l, new BlockPos(5, 2, 5));
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(l.nightfall(helper.getLevel(), false) == 0, "not by day");
        check(l.nightfall(helper.getLevel(), true) == 1 && z.hasEffect(MobEffects.WEAKNESS), "weakened");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "minMinLeadsTheWay")
    public static void minMinLeadsTheWay(GameTestHelper helper) {
        Mollusk m = still(helper, GirlSpecies.MIN_MIN_MOLLUSK, new BlockPos(1, 2, 1));
        check(m.canFly(), "she floats");
        check(m.leadTheWay(helper.getLevel(), true) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, m, new BlockPos(5, 2, 5));
        check(m.leadTheWay(helper.getLevel(), false) == 0, "not by day");
        check(m.leadTheWay(helper.getLevel(), true) == 1, "by night");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "handClimbsAndGrips")
    public static void handClimbsAndGrips(GameTestHelper helper) {
        Undead h = still(helper, GirlSpecies.HAND_UNDEAD, new BlockPos(1, 2, 1));
        check(h.getNavigation() instanceof net.minecraft.world.entity.ai.navigation.WallClimberNavigation, "it runs up walls");
        check(h.firmGrip(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, h, new BlockPos(5, 2, 5));
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(h.firmGrip(helper.getLevel()) == 1 && z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "held by the ankle");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "wrathHitsBackHarder")
    public static void wrathHitsBackHarder(GameTestHelper helper) {
        Devil w = still(helper, GirlSpecies.WRATH_DEVIL, new BlockPos(1, 2, 1));
        check(w.fireImmune(), "fire cannot hurt fire");
        check(w.isSensitiveToWater(), "water can");
        Zombie z = zombie(helper, new BlockPos(2, 2, 1));
        check(w.rage() == 0, "calm to begin with");
        w.hurt(helper.getLevel().damageSources().mobAttack(z), 1.0F);
        check(w.rage() == 1, "angrier for every blow");
        float before = z.getHealth();
        check(w.doHurtTarget(z), "she hits back");
        check(z.getHealth() <= before - 7.0F - 2.0F + 0.5F || !z.isAlive(), "harder");
        check(z.getRemainingFireTicks() > 0 || !z.isAlive(), "and burning");
        helper.succeed();
    }
}
