package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.IbisetHarpy;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.StigowlHarpy;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Bug;
import com.linkyoshi90.monstergirls.entity.girl.Canid;
import com.linkyoshi90.monstergirls.entity.girl.Centaur;
import com.linkyoshi90.monstergirls.entity.girl.Devil;
import com.linkyoshi90.monstergirls.entity.girl.Dragon;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Kitsune;
import com.linkyoshi90.monstergirls.entity.girl.Lizardfolk;
import com.linkyoshi90.monstergirls.entity.girl.Merfolk;
import com.linkyoshi90.monstergirls.entity.girl.Nekomata;
import com.linkyoshi90.monstergirls.entity.girl.Undead;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/**
 * The twenty-fourth Harmontide wave (HARMONTIDE_CONCEPTS.md): Quetzalcoatl, the Leviathan, the girtablilu, Cipactli, the Behemoth, the
 * nasnas, the ibiset, the stigowl, and Cerberus, the jiangshi, the baku and the qilin; and the nine tails of one of its homage looks.
 */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class TwentyFourthWaveTests {

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

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "quetzalcoatlRipensTheCrops")
    public static void quetzalcoatlRipensTheCrops(GameTestHelper helper) {
        Dragon q = still(helper, GirlSpecies.QUETZALCOATL_DRAGON, new BlockPos(1, 2, 1));
        check(q.canFly() && q.isMultipartEntity(), "she flies, and her body has hitboxes");
        check(q.morningStar(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, q, new BlockPos(5, 2, 5));
        helper.setBlock(new BlockPos(6, 1, 6), Blocks.FARMLAND);
        helper.setBlock(new BlockPos(6, 2, 6), Blocks.WHEAT);
        check(q.morningStar(helper.getLevel()) == 1, "one crop grown");
        check(((CropBlock) Blocks.WHEAT).getAge(helper.getBlockState(new BlockPos(6, 2, 6))) > 0, "the wheat has grown");
        check(q.morningStar(helper.getLevel()) == 0, "once a day");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "leviathanCallsTheTide")
    public static void leviathanCallsTheTide(GameTestHelper helper) {
        Merfolk l = still(helper, GirlSpecies.LEVIATHAN_MERMAID, new BlockPos(1, 2, 1));
        check(l.tideCall(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, l, new BlockPos(5, 2, 5));
        helper.setBlock(new BlockPos(7, 2, 5), Blocks.WATER);
        Zombie z = zombie(helper, new BlockPos(7, 2, 5));
        helper.succeedWhen(() -> {
            check(z.isInWater(), "the zombie is in the water");
            check(l.tideCall(helper.getLevel()) == 1, "the tide takes it");
            HarpyTestUtil.remove(p);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "girtablilustings")
    public static void girtablilustings(GameTestHelper helper) {
        Bug g = still(helper, GirlSpecies.GIRTABLILU_BUG, new BlockPos(1, 2, 1));
        check(g.gateGuard(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, g, new BlockPos(5, 2, 5));
        check(g.gateGuard(helper.getLevel()) == 0, "nothing near");
        Zombie z = zombie(helper, new BlockPos(7, 2, 5));
        check(g.gateGuard(helper.getLevel()) == 1 && z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "stung");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "cipactliPullsThemUnder")
    public static void cipactliPullsThemUnder(GameTestHelper helper) {
        Lizardfolk c = still(helper, GirlSpecies.CIPACTLI_LIZARDFOLK, new BlockPos(1, 2, 1));
        check(c.pullUnder(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, c, new BlockPos(5, 2, 5));
        Zombie z = zombie(helper, new BlockPos(7, 2, 5));
        check(c.pullUnder(helper.getLevel()) == 0, "not on dry land");
        helper.setBlock(new BlockPos(8, 2, 5), Blocks.WATER);
        check(c.pullUnder(helper.getLevel()) == 1 && z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "bitten at the water's edge");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "behemothAnchorsHerFriend")
    public static void behemothAnchorsHerFriend(GameTestHelper helper) {
        Devil b = still(helper, GirlSpecies.BEHEMOTH_DEVIL, new BlockPos(5, 2, 5));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 5));
        check(!Devil.anchored(p), "only for a friend");
        b.addAffection(p, Orders.AFFECTION);
        check(Devil.anchored(p), "her friend stands fast");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "nasnasTakesTheBlow")
    public static void nasnasTakesTheBlow(GameTestHelper helper) {
        Undead n = still(helper, GirlSpecies.NASNAS_UNDEAD, new BlockPos(5, 2, 5));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 5));
        check(!Undead.takesTheBlow(p, 100.0F), "only for a friend");
        n.addAffection(p, Orders.AFFECTION);
        check(!Undead.takesTheBlow(p, 1.0F), "only a killing blow");
        check(Undead.takesTheBlow(p, 100.0F), "she takes it");
        check(!Undead.takesTheBlow(p, 100.0F), "once a day");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "jiangshiStiffensMonsters")
    public static void jiangshiStiffensMonsters(GameTestHelper helper) {
        Undead j = still(helper, GirlSpecies.JIANGSHI_UNDEAD, new BlockPos(5, 2, 5));
        check(j.stiffHop(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, j, new BlockPos(8, 2, 8));
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(j.stiffHop(helper.getLevel()) == 1 && z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "struck stiff");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "cerberusWatchesHerFriendsBack")
    public static void cerberusWatchesHerFriendsBack(GameTestHelper helper) {
        Canid c = still(helper, GirlSpecies.CERBERUS_CANID, new BlockPos(1, 2, 1));
        check(c.threeWatches(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, c, new BlockPos(5, 2, 5));
        Zombie z = zombie(helper, new BlockPos(5, 2, 5));
        z.setPos(p.position().subtract(p.getLookAngle().scale(3.0)));
        check(c.threeWatches(helper.getLevel()) == 1 && z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "the one behind them is slowed");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "bakuEatsNightmares")
    public static void bakuEatsNightmares(GameTestHelper helper) {
        Nekomata b = still(helper, GirlSpecies.BAKU_NEKOMATA, new BlockPos(1, 2, 1));
        check(b.eatNightmares(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, b, new BlockPos(5, 2, 5));
        p.getStats().setValue(p, Stats.CUSTOM.get(Stats.TIME_SINCE_REST), 100000);
        check(b.eatNightmares(helper.getLevel()) == 1, "she eats");
        check(p.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)) == 0, "as good as a night's sleep");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "qilinHealsAnimals")
    public static void qilinHealsAnimals(GameTestHelper helper) {
        Centaur q = still(helper, GirlSpecies.QILIN_CENTAUR, new BlockPos(1, 2, 1));
        check(q.gentleTread(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, q, new BlockPos(5, 2, 5));
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(7, 2, 5));
        cow.setHealth(4.0F);
        check(q.gentleTread(helper.getLevel()) == 1 && cow.getHealth() > 4.0F, "the cow is healed");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "ibisetWritesABook")
    public static void ibisetWritesABook(GameTestHelper helper) {
        IbisetHarpy i = helper.spawn(ModEntities.IBISET_HARPY.get(), new BlockPos(5, 2, 5));
        i.setNoAi(true);
        check(i.dailyBook(helper.getLevel()) == null, "only for a friend");
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 5));
        i.addAffection(p, Orders.AFFECTION);
        ItemStack book = i.dailyBook(helper.getLevel());
        check(book != null && book.isEnchanted() || book != null && !book.isEmpty(), "an enchanted book");
        check(i.dailyBook(helper.getLevel()) == null, "once a day");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "stigowlClearsTheDark")
    public static void stigowlClearsTheDark(GameTestHelper helper) {
        StigowlHarpy s = helper.spawn(ModEntities.STIGOWL_HARPY.get(), new BlockPos(5, 2, 5));
        s.setNoAi(true);
        check(s.clearSight() == -1, "only for a friend");
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 5));
        s.addAffection(p, Orders.AFFECTION);
        check(s.clearSight() == 0, "nothing to clear");
        p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200));
        check(s.clearSight() == 1 && !p.hasEffect(MobEffects.BLINDNESS), "her friend sees again");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "nineCreamShowsNineTails")
    public static void nineCreamShowsNineTails(GameTestHelper helper) {
        Kitsune k = still(helper, GirlSpecies.RED_KITSUNE, new BlockPos(5, 2, 5));
        k.setVariant("nine_cream");
        check(k.visibleTails() == 9, "nine tails");
        helper.succeed();
    }
}
