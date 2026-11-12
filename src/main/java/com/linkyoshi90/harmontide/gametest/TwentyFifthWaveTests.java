package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.YatagarasuHarpy;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Devil;
import com.linkyoshi90.harmontide.entity.girl.Elf;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Mollusk;
import com.linkyoshi90.harmontide.entity.girl.Rider;
import com.linkyoshi90.harmontide.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/**
 * The twenty-fifth Harmontide wave (HARMONTIDE_CONCEPTS.md): the Four Riders, a family of their own (Pestilence on a real horse); the Fire and
 * Frost Giants, the Ifrit, Pazuzu, Charybdis, the Mad Hatter, the Angel and the yatagarasu.
 */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class TwentyFifthWaveTests {

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

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "pestilenceRidesARealHorse")
    public static void pestilenceRidesARealHorse(GameTestHelper helper) {
        Rider p = spawn(helper, GirlSpecies.PESTILENCE_RIDER, new BlockPos(5, 2, 5));
        helper.succeedWhen(() -> {
            check(p.getVehicle() instanceof Horse, "she mounts a real horse");
            check(((Horse) p.getVehicle()).getVariant() == Variant.BLACK, "a black one");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "warRallies")
    public static void warRallies(GameTestHelper helper) {
        Rider w = still(helper, GirlSpecies.WAR_RIDER, new BlockPos(1, 2, 1));
        check(w.battleCry(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, w, new BlockPos(5, 2, 5));
        zombie(helper, new BlockPos(6, 2, 5));
        check(w.battleCry(helper.getLevel()) == 0, "not outnumbered");
        zombie(helper, new BlockPos(4, 2, 5));
        zombie(helper, new BlockPos(5, 2, 7));
        check(w.battleCry(helper.getLevel()) == 1 && f.hasEffect(MobEffects.DAMAGE_BOOST), "Strength when outnumbered");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "famineFeedsAndWeakens")
    public static void famineFeedsAndWeakens(GameTestHelper helper) {
        Rider r = still(helper, GirlSpecies.FAMINE_RIDER, new BlockPos(1, 2, 1));
        check(r.hollowHunger(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, r, new BlockPos(5, 2, 5));
        f.getFoodData().setFoodLevel(10);
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(r.hollowHunger(helper.getLevel()) == 1 && z.hasEffect(MobEffects.WEAKNESS), "the monster is weakened");
        check(f.getFoodData().getFoodLevel() > 10, "her friend is fed");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "pestilenceCloudsTheMonsters")
    public static void pestilenceCloudsTheMonsters(GameTestHelper helper) {
        Rider r = still(helper, GirlSpecies.PESTILENCE_RIDER, new BlockPos(1, 2, 1));
        ServerPlayer f = friendOf(helper, r, new BlockPos(5, 2, 5));
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(r.tealCloud(helper.getLevel()) == 1 && z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && z.getHealth() < z.getMaxHealth(), "the cloud lands");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "deathFinishesTheJob")
    public static void deathFinishesTheJob(GameTestHelper helper) {
        Rider r = still(helper, GirlSpecies.DEATH_RIDER, new BlockPos(1, 2, 1));
        ServerPlayer f = friendOf(helper, r, new BlockPos(5, 2, 5));
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(r.lastCall(helper.getLevel()) == 0 && z.isAlive(), "a healthy monster stands");
        z.setHealth(3.0F);
        check(r.lastCall(helper.getLevel()) == 1 && !z.isAlive(), "a nearly dead one falls");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "fireGiantWarmsAndLights")
    public static void fireGiantWarmsAndLights(GameTestHelper helper) {
        Devil g = still(helper, GirlSpecies.FIRE_GIANT_DEVIL, new BlockPos(5, 2, 5));
        check(g.hearthfire(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, g, new BlockPos(8, 2, 8));
        check(g.hearthfire(helper.getLevel()) == 1 && f.hasEffect(MobEffects.FIRE_RESISTANCE), "Fire Resistance");
        helper.succeedWhen(() -> {
            check(helper.getLevel().getBlockState(g.blockPosition()).is(Blocks.LIGHT), "her lava lights the ground");
            HarpyTestUtil.remove(f);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "frostGiantFreezesTheRoad")
    public static void frostGiantFreezesTheRoad(GameTestHelper helper) {
        Devil g = still(helper, GirlSpecies.FROST_GIANT_DEVIL, new BlockPos(1, 2, 1));
        check(g.iceRoad(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, g, new BlockPos(5, 2, 5));
        helper.setBlock(new BlockPos(6, 1, 5), Blocks.WATER);
        check(g.iceRoad(helper.getLevel()) == 1, "the water freezes");
        check(helper.getBlockState(new BlockPos(6, 1, 5)).is(Blocks.FROSTED_ICE), "frosted ice");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "ifritSmeltsOre")
    public static void ifritSmeltsOre(GameTestHelper helper) {
        Devil i = still(helper, GirlSpecies.IFRIT_DEVIL, new BlockPos(1, 2, 1));
        check(i.smelter(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, i, new BlockPos(5, 2, 5));
        f.getInventory().add(new ItemStack(Items.RAW_IRON, 7));
        check(i.smelter(helper.getLevel()) == 7, "seven ingots");
        check(f.getInventory().countItem(Items.IRON_INGOT) == 7 && f.getInventory().countItem(Items.RAW_IRON) == 0, "raw iron into ingots");
        f.getInventory().add(new ItemStack(Items.RAW_GOLD, 3));
        check(i.smelter(helper.getLevel()) == 0, "once a day");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "pazuzuBlowsThemBack")
    public static void pazuzuBlowsThemBack(GameTestHelper helper) {
        Devil p = still(helper, GirlSpecies.PAZUZU_DEVIL, new BlockPos(1, 2, 1));
        check(p.canFly(), "she flies");
        check(p.southWind(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, p, new BlockPos(5, 2, 5));
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(p.southWind(helper.getLevel()) == 0, "not crowded");
        zombie(helper, new BlockPos(4, 2, 5));
        zombie(helper, new BlockPos(5, 2, 7));
        check(p.southWind(helper.getLevel()) == 3 && z.getDeltaMovement().y > 0, "thrown back");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "charybdisDrawsThemIn")
    public static void charybdisDrawsThemIn(GameTestHelper helper) {
        Mollusk c = still(helper, GirlSpecies.CHARYBDIS_MOLLUSK, new BlockPos(1, 2, 1));
        check(c.whirlpool(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, c, new BlockPos(5, 2, 5));
        helper.setBlock(new BlockPos(4, 2, 2), Blocks.WATER);
        zombie(helper, new BlockPos(4, 2, 2));
        helper.succeedWhen(() -> {
            check(c.whirlpool(helper.getLevel()) == 1, "pulled in");
            HarpyTestUtil.remove(f);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "hatterPoursTea")
    public static void hatterPoursTea(GameTestHelper helper) {
        Elf h = still(helper, GirlSpecies.MAD_HATTER_ELF, new BlockPos(1, 2, 1));
        check(h.teaTime(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, h, new BlockPos(5, 2, 5));
        check(h.teaTime(helper.getLevel()) == 1 && !f.getActiveEffects().isEmpty(), "a cup of something");
        check(h.teaTime(helper.getLevel()) == 0, "once a day");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "angelShowsMercy")
    public static void angelShowsMercy(GameTestHelper helper) {
        Elf a = still(helper, GirlSpecies.ANGEL_ELF, new BlockPos(1, 2, 1));
        check(a.canFly(), "she flies");
        check(a.mercy(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = friendOf(helper, a, new BlockPos(5, 2, 5));
        check(a.mercy(helper.getLevel()) == 0, "her friend is well");
        f.setHealth(3.0F);
        check(a.mercy(helper.getLevel()) == 1 && f.hasEffect(MobEffects.REGENERATION), "Regeneration when hurt");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "yatagarasuBlessesTheDawn")
    public static void yatagarasuBlessesTheDawn(GameTestHelper helper) {
        YatagarasuHarpy y = helper.spawn(ModEntities.YATAGARASU_HARPY.get(), new BlockPos(5, 2, 5));
        y.setNoAi(true);
        check(y.dawn(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer f = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 5));
        y.addAffection(f, Orders.AFFECTION);
        check(y.dawn(helper.getLevel()) == 1 && f.hasEffect(MobEffects.MOVEMENT_SPEED), "the day's blessing");
        check(y.dawn(helper.getLevel()) == 0, "once a day");
        HarpyTestUtil.remove(f);
        helper.succeed();
    }
}
