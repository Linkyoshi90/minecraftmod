package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.IronbeakHarpy;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.BatGirl;
import com.linkyoshi90.monstergirls.entity.girl.Canid;
import com.linkyoshi90.monstergirls.entity.girl.Construct;
import com.linkyoshi90.monstergirls.entity.girl.Devil;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Kitsune;
import com.linkyoshi90.monstergirls.entity.girl.Lamia;
import com.linkyoshi90.monstergirls.entity.girl.Lizardfolk;
import com.linkyoshi90.monstergirls.entity.girl.Mollusk;
import com.linkyoshi90.monstergirls.entity.girl.Rodent;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/**
 * The twenty-third Harmontide wave (HARMONTIDE_CONCEPTS.md): automaton, spewslug, ogre, hellhound, argentinosaurus, titanoboa,
 * sasquatch, ironbeak, and Sun Wukong, Scylla, the taotie and the vetala; and Tails's two tails, one of the homage looks that came
 * with it.
 */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class TwentyThirdWaveTests {

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

    private static Zombie zombie(GameTestHelper helper, BlockPos at) {
        Zombie z = helper.spawn(EntityType.ZOMBIE, at);
        z.setNoAi(true);
        return z;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "automatonLightsTheDark")
    public static void automatonLightsTheDark(GameTestHelper helper) {
        Construct bot = still(helper, GirlSpecies.AUTOMATON_CONSTRUCT, new BlockPos(1, 2, 1));
        check(bot.lamplighter(helper.getLevel(), p -> true) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, bot, new BlockPos(3, 2, 3));
        check(bot.lamplighter(helper.getLevel(), p2 -> true) == 0, "no torch, no light");
        p.getInventory().add(new ItemStack(Items.TORCH, 4));
        check(bot.lamplighter(helper.getLevel(), p2 -> false) == 0, "nowhere dark, nothing to light");
        check(bot.lamplighter(helper.getLevel(), p2 -> true) == 1, "she sets a torch on the dark ground");
        check(p.getInventory().countItem(Items.TORCH) == 3, "one of her friend's torches: " + p.getInventory().countItem(Items.TORCH));
        boolean lit = false;
        for (BlockPos b : BlockPos.betweenClosed(p.blockPosition().offset(-5, -1, -5), p.blockPosition().offset(5, 1, 5)))
            lit |= helper.getLevel().getBlockState(b).is(Blocks.TORCH);
        check(lit, "a torch near her friend");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "spewslugSpewsBile")
    public static void spewslugSpewsBile(GameTestHelper helper) {
        Mollusk slug = still(helper, GirlSpecies.SPEWSLUG_MOLLUSK, new BlockPos(1, 2, 1));
        check(slug.bile(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, slug, new BlockPos(3, 2, 3));
        Zombie z = zombie(helper, new BlockPos(4, 2, 4));
        check(slug.bile(helper.getLevel()) == 0, "nobody hit her friend");
        p.setLastHurtByMob(z);
        check(slug.bile(helper.getLevel()) == 1, "she spews on the zombie that hit her friend");
        check(z.hasEffect(MobEffects.WEAKNESS) && z.hasEffect(MobEffects.HUNGER), "weakened and starving");
        check(slug.bile(helper.getLevel()) == 0, "once for each hit");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "scyllaStripsMonsters")
    public static void scyllaStripsMonsters(GameTestHelper helper) {
        Mollusk scylla = still(helper, GirlSpecies.SCYLLA_MOLLUSK, new BlockPos(1, 2, 1));
        check(scylla.sixHounds(helper.getLevel()) == -1, "only for a friend");
        friendOf(helper, scylla, new BlockPos(3, 2, 3));
        Zombie z = zombie(helper, new BlockPos(4, 2, 4));
        z.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        z.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        check(scylla.sixHounds(helper.getLevel()) == 1, "her hounds strip the zombie");
        check(z.getItemBySlot(EquipmentSlot.HEAD).isEmpty() && z.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty(), "no helmet, no sword");
        check(scylla.sixHounds(helper.getLevel()) == 0, "nothing left to tear off");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "ogreBonksAttackers")
    public static void ogreBonksAttackers(GameTestHelper helper) {
        Devil ogre = still(helper, GirlSpecies.OGRE_DEVIL, new BlockPos(1, 2, 1));
        check(ogre.bonk(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, ogre, new BlockPos(3, 2, 3));
        Zombie z = zombie(helper, new BlockPos(4, 2, 4));
        p.setLastHurtByMob(z);
        check(ogre.bonk(helper.getLevel()) == 1, "she clubs the zombie that hit her friend");
        check(z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && z.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier() == 4, "stunned");
        check(ogre.bonk(helper.getLevel()) == 0, "once for each hit");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "taotieEatsTheJunk")
    public static void taotieEatsTheJunk(GameTestHelper helper) {
        Devil taotie = still(helper, GirlSpecies.TAOTIE_DEVIL, new BlockPos(1, 2, 1));
        check(taotie.gluttony(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, taotie, new BlockPos(3, 2, 3));
        BlockPos at = helper.absolutePos(new BlockPos(4, 2, 4));
        ItemEntity flesh = new ItemEntity(helper.getLevel(), at.getX() + 0.5, at.getY(), at.getZ() + 0.5, new ItemStack(Items.ROTTEN_FLESH, 3));
        ItemEntity gold = new ItemEntity(helper.getLevel(), at.getX() + 0.5, at.getY(), at.getZ() + 0.5, new ItemStack(Items.GOLD_INGOT));
        helper.getLevel().addFreshEntity(flesh);
        helper.getLevel().addFreshEntity(gold);
        int xp = p.totalExperience;
        check(taotie.gluttony(helper.getLevel()) == 3, "she eats the three rotten flesh");
        check(!flesh.isAlive() && gold.isAlive(), "the junk, not the gold");
        check(p.totalExperience == xp + 3, "three experience for her friend: " + (p.totalExperience - xp));
        gold.discard();
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "hellhoundHuntsDown")
    public static void hellhoundHuntsDown(GameTestHelper helper) {
        Canid hound = still(helper, GirlSpecies.HELLHOUND_CANID, new BlockPos(1, 2, 1));
        check(hound.huntDown(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, hound, new BlockPos(3, 2, 3));
        Zombie z = zombie(helper, new BlockPos(5, 2, 5));
        check(hound.huntDown(helper.getLevel()) == 0, "nothing to avenge");
        p.setLastHurtByMob(z);
        check(hound.huntDown(helper.getLevel()) == 1, "she runs down the zombie that hurt her friend");
        check(z.getRemainingFireTicks() > 0, "on fire");
        check(hound.huntDown(helper.getLevel()) == 0, "once for each hit");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "argentinosaurusSpotsFarMonsters")
    public static void argentinosaurusSpotsFarMonsters(GameTestHelper helper) {
        Lizardfolk tall = still(helper, GirlSpecies.ARGENTINOSAURUS_LIZARDFOLK, new BlockPos(1, 2, 1));
        check(tall.lookout(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, tall, new BlockPos(3, 2, 3));
        Zombie near = zombie(helper, new BlockPos(5, 2, 5));
        Zombie far = EntityType.ZOMBIE.create(helper.getLevel());
        far.moveTo(p.getX() + 30, p.getY(), p.getZ());
        far.setNoAi(true);
        helper.getLevel().addFreshEntity(far);
        check(tall.lookout(helper.getLevel()) == 1, "she spots the far zombie");
        check(far.hasEffect(MobEffects.GLOWING) && !near.hasEffect(MobEffects.GLOWING), "the far one glows, the near one does not");
        far.discard();
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "titanoboaCoilsTheBiggest")
    public static void titanoboaCoilsTheBiggest(GameTestHelper helper) {
        Lamia boa = still(helper, GirlSpecies.TITANOBOA_LAMIA, new BlockPos(1, 2, 1));
        check(boa.constrict(helper.getLevel()) == -1, "only for a friend");
        friendOf(helper, boa, new BlockPos(3, 2, 3));
        check(boa.constrict(helper.getLevel()) == 0, "nothing to coil round");
        Zombie z = zombie(helper, new BlockPos(5, 2, 3));
        Spider s = helper.spawn(EntityType.SPIDER, new BlockPos(3, 2, 5));
        s.setNoAi(true);
        check(boa.constrict(helper.getLevel()) == 1, "she coils round one");
        check(s.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && s.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier() == 6, "the spider, the biggest, held fast");
        check(!z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "not the smaller zombie");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "sasquatchHidesHerFriend")
    public static void sasquatchHidesHerFriend(GameTestHelper helper) {
        Rodent big = still(helper, GirlSpecies.SASQUATCH_RODENT, new BlockPos(1, 2, 1));
        check(big.unseen(helper.getLevel(), true) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, big, new BlockPos(3, 2, 3));
        check(big.unseen(helper.getLevel(), true) == 0, "not while walking upright");
        p.setShiftKeyDown(true);
        check(big.unseen(helper.getLevel(), false) == 0, "not out of the woods");
        check(big.unseen(helper.getLevel(), true) == 1, "sneaking in the forest");
        check(p.hasEffect(MobEffects.INVISIBILITY), "unseen");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "wukongCatchesFalls")
    public static void wukongCatchesFalls(GameTestHelper helper) {
        Rodent monkey = still(helper, GirlSpecies.SUN_WUKONG_RODENT, new BlockPos(1, 2, 1));
        check(monkey.somersaultCloud(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, monkey, new BlockPos(3, 2, 3));
        check(monkey.somersaultCloud(helper.getLevel()) == 0, "not falling");
        p.fallDistance = 20;
        check(monkey.somersaultCloud(helper.getLevel()) == 1, "her cloud catches the fall");
        check(p.fallDistance == 0, "no fall to land from");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "vetalaPossessesAZombie")
    public static void vetalaPossessesAZombie(GameTestHelper helper) {
        BatGirl vetala = still(helper, GirlSpecies.VETALA_BAT, new BlockPos(1, 2, 1));
        check(vetala.possess(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, vetala, new BlockPos(3, 2, 3));
        check(vetala.possess(helper.getLevel()) == 0, "no zombie to ride");
        Zombie z = zombie(helper, new BlockPos(4, 2, 4));
        Skeleton sk = helper.spawn(EntityType.SKELETON, new BlockPos(5, 2, 2));
        sk.setNoAi(true);
        check(vetala.possess(helper.getLevel()) == 1, "she rides the zombie");
        check(z.getTarget() == sk, "it turns on the skeleton: " + z.getTarget());
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "ironbeakBringsAMap")
    public static void ironbeakBringsAMap(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        IronbeakHarpy bird = helper.spawn(ModEntities.IRONBEAK_HARPY.get(), new BlockPos(1, 2, 1));
        bird.setNoAi(true);
        check(bird.friend() == null, "only for a friend");
        bird.setAffection(friend, Orders.AFFECTION);
        check(bird.friend() == friend, "her friend");
        BlockPos far = friend.blockPosition().offset(300, 0, -200);
        ItemStack map = bird.deliverMap(helper.getLevel(), friend, far);
        check(map.is(Items.FILLED_MAP), "an explorer's map");
        check(friend.getInventory().countItem(Items.FILLED_MAP) == 1, "in her friend's hands");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "twinTailsShowsTwoTails")
    public static void twinTailsShowsTwoTails(GameTestHelper helper) {
        Kitsune fox = still(helper, GirlSpecies.FENNEC_KITSUNE, new BlockPos(1, 2, 1));
        check(fox.visibleTails() == 1 || fox.visibleTails() > 1, "a fennec");
        int wild = fox.visibleTails();
        fox.setVariant("twin_tails");
        check("twin_tails".equals(fox.getVariantName()), "the homage look exists");
        check(fox.visibleTails() == Math.max(2, wild), "Tails always shows two tails: " + fox.visibleTails());
        helper.succeed();
    }
}
