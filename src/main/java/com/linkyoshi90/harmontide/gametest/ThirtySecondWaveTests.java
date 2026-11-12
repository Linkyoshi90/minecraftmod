package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Arachne;
import com.linkyoshi90.harmontide.entity.girl.Bug;
import com.linkyoshi90.harmontide.entity.girl.Centaur;
import com.linkyoshi90.harmontide.entity.girl.Flora;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Kitsune;
import com.linkyoshi90.harmontide.entity.girl.Lamia;
import com.linkyoshi90.harmontide.entity.girl.Lizardfolk;
import com.linkyoshi90.harmontide.entity.girl.Merfolk;
import com.linkyoshi90.harmontide.entity.girl.Mollusk;
import com.linkyoshi90.harmontide.entity.girl.Nekomata;
import com.linkyoshi90.harmontide.entity.girl.SlimeGirl;
import com.linkyoshi90.harmontide.entity.girl.Undead;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** The thirty-second and thirty-third waves (HARMONTIDE_CONCEPTS.md): the rest of the species ideas. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class ThirtySecondWaveTests {
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

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "panthersFriendGoesUnseen")
    public static void panthersFriendGoesUnseen(GameTestHelper helper) {
        Nekomata n = still(helper, GirlSpecies.BLACK_PANTHER_NEKOMATA, new BlockPos(1, 2, 1));
        check(n.prowl(true) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, n, new BlockPos(3, 2, 3));
        check(n.prowl(true) == 0, "not standing up");
        p.setShiftKeyDown(true);
        check(n.prowl(false) == 0, "not by day");
        check(n.prowl(true) == 1 && p.hasEffect(MobEffects.INVISIBILITY), "unseen");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "kumihoHasNineTails")
    public static void kumihoHasNineTails(GameTestHelper helper) {
        Kitsune k = still(helper, GirlSpecies.KUMIHO_KITSUNE, new BlockPos(1, 2, 1));
        k.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(k.blockPosition()), net.minecraft.world.entity.MobSpawnType.NATURAL, null);
        check(k.getTails() == 9, "nine from birth: " + k.getTails());
        check(!k.foxBead(), "only for a friend");
        ServerPlayer p = friendOf(helper, k, new BlockPos(3, 2, 3));
        int before = p.totalExperience;
        check(k.foxBead() && p.totalExperience > before, "experience");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "batEaredFoxHearsSpawners")
    public static void batEaredFoxHearsSpawners(GameTestHelper helper) {
        Kitsune k = still(helper, GirlSpecies.BAT_EARED_KITSUNE, new BlockPos(1, 2, 1));
        check(k.listen(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, k, new BlockPos(3, 2, 3));
        check(k.listen(helper.getLevel()) == -2, "none yet");
        helper.setBlock(new BlockPos(5, 2, 5), Blocks.SPAWNER);
        check(k.listen(helper.getLevel()) >= 0, "she hears it");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "anacondaGulpsAndGrooves")
    public static void anacondaGulpsAndGrooves(GameTestHelper helper) {
        Lamia l = still(helper, GirlSpecies.ANACONDA_LAMIA, new BlockPos(1, 2, 1));
        check(l.gulp(helper.getLevel()) == -1 && !l.groove(true), "only for a friend");
        ServerPlayer p = friendOf(helper, l, new BlockPos(6, 2, 6));
        check(!l.groove(false), "no music");
        check(l.groove(true) && p.hasEffect(MobEffects.DAMAGE_BOOST) && p.hasEffect(MobEffects.MOVEMENT_SPEED), "the music gets her friend");
        Spider s = helper.spawn(EntityType.CAVE_SPIDER, new BlockPos(2, 2, 2));
        s.setNoAi(true);
        check(l.gulp(helper.getLevel()) == 1 && !s.isAlive(), "swallowed");
        check(l.gulp(helper.getLevel()) == -2, "full");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "seaSerpentCallsTheStorm")
    public static void seaSerpentCallsTheStorm(GameTestHelper helper) {
        Lamia l = still(helper, GirlSpecies.SEA_SERPENT_LAMIA, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, l, new BlockPos(5, 2, 5));
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(l.stormCall(helper.getLevel(), false) == 0, "only in the rain");
        check(l.stormCall(helper.getLevel(), true) == 1 && z.getHealth() < z.getMaxHealth(), "struck");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "spidersWorkForTheirFriend")
    public static void spidersWorkForTheirFriend(GameTestHelper helper) {
        Arachne w = still(helper, GirlSpecies.BLACK_WIDOW_ARACHNE, new BlockPos(1, 2, 1));
        Arachne c = still(helper, GirlSpecies.CAVE_SPIDER_ARACHNE, new BlockPos(1, 2, 2));
        ServerPlayer p = friendOf(helper, w, new BlockPos(5, 2, 5));
        c.addAffection(p, Orders.AFFECTION);
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(w.hourglass(helper.getLevel()) == 1 && z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "venom");
        helper.setBlock(new BlockPos(5, 2, 6), Blocks.COBWEB);
        check(c.pullWebs(helper.getLevel()) == 1 && p.getInventory().countItem(Items.STRING) == 2, "string");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "donkeyGuardsTheFlock")
    public static void donkeyGuardsTheFlock(GameTestHelper helper) {
        Centaur d = still(helper, GirlSpecies.DONKEY_CENTAUR, new BlockPos(1, 2, 1));
        check(!d.isPushable(), "immovable");
        check(d.guardFlock(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, d, new BlockPos(1, 2, 3));
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(5, 2, 5));
        cow.setNoAi(true);
        check(d.guardFlock(helper.getLevel()) == 0, "nothing to kick");
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(d.guardFlock(helper.getLevel()) == 1 && z.getHealth() < z.getMaxHealth(), "kicked");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "copperSlimeWeathers")
    public static void copperSlimeWeathers(GameTestHelper helper) {
        SlimeGirl s = still(helper, GirlSpecies.COPPER_SLIME, new BlockPos(1, 2, 1));
        s.setVariantIndex(0);
        check(s.weather() && "exposed".equals(s.getVariantName()), "a stage on");
        s.setWaxed(true);
        check(!s.weather(), "wax holds");
        check(s.scrape() && !s.isWaxed(), "the wax goes first");
        check(s.scrape() && s.getVariantIndex() == 0, "scraped back");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "komodoVenomSickensTheHurt")
    public static void komodoVenomSickensTheHurt(GameTestHelper helper) {
        Lizardfolk k = still(helper, GirlSpecies.KOMODO_LIZARDFOLK, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, k, new BlockPos(5, 2, 5));
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        check(k.slowVenom(helper.getLevel()) == 0, "not hurt yet");
        z.setHealth(10);
        check(k.slowVenom(helper.getLevel()) == 1 && z.hasEffect(MobEffects.WEAKNESS), "sickens");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "skeletonIsCalmedByABone")
    public static void skeletonIsCalmedByABone(GameTestHelper helper) {
        Undead u = still(helper, GirlSpecies.SKELETON_UNDEAD, new BlockPos(1, 2, 1));
        check(u.isFood(new ItemStack(Items.BONE)), "bones");
        check(u.gatherArrows(helper.getLevel()) == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, u, new BlockPos(5, 2, 5));
        check(u.gatherArrows(helper.getLevel()) == 0, "none lying about");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "roseThornsPrick")
    public static void roseThornsPrick(GameTestHelper helper) {
        Flora r = still(helper, GirlSpecies.ROSE_FLORA, new BlockPos(1, 2, 1));
        check(r.thorns() == -1, "only for a friend");
        ServerPlayer p = friendOf(helper, r, new BlockPos(3, 2, 3));
        check(r.thorns() == 0, "nobody yet");
        Zombie z = zombie(helper, new BlockPos(4, 2, 3));
        p.setLastHurtByMob(z);
        check(r.thorns() == 1 && z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "pricked");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "lotusBloomsAtDawn")
    public static void lotusBloomsAtDawn(GameTestHelper helper) {
        Flora l = still(helper, GirlSpecies.LOTUS_FLORA, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, l, new BlockPos(3, 2, 3));
        p.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
        check(l.dawnBloom(7) && !p.hasEffect(MobEffects.POISON), "cleared");
        check(!l.dawnBloom(7), "once a day");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "pufferfishPuffsUp")
    public static void pufferfishPuffsUp(GameTestHelper helper) {
        Merfolk m = still(helper, GirlSpecies.PUFFERFISH_MERMAID, new BlockPos(1, 2, 1));
        float before = m.sizeScale();
        m.puff(helper.getLevel());
        check(m.isPuffed() && m.sizeScale() > before, "bigger");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "anglerAndManateeTurnMonsters")
    public static void anglerAndManateeTurnMonsters(GameTestHelper helper) {
        Merfolk a = still(helper, GirlSpecies.ANGLER_MERMAID, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, a, new BlockPos(5, 2, 5));
        Zombie z = zombie(helper, new BlockPos(6, 2, 5));
        z.setTarget(p);
        check(a.lure(helper.getLevel()) == 1 && z.getTarget() == a, "drawn away");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "seaSlugSharesAPotion")
    public static void seaSlugSharesAPotion(GameTestHelper helper) {
        Mollusk m = still(helper, GirlSpecies.SEA_SLUG_MOLLUSK, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, m, new BlockPos(3, 2, 3));
        long now = helper.getLevel().getGameTime();
        check(!m.shareSting(now), "nothing borrowed");
        check(m.borrow(PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS), now), "she takes it");
        check(m.shareSting(now) && p.hasEffect(MobEffects.MOVEMENT_SPEED), "shared");
        check(!m.shareSting(now + 4000), "worn off");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "queenRaisesAnts")
    public static void queenRaisesAnts(GameTestHelper helper) {
        Bug q = still(helper, GirlSpecies.QUEEN_ANT_BUG, new BlockPos(1, 2, 1));
        check(!q.brood(helper.getLevel()), "only for a friend");
        ServerPlayer p = friendOf(helper, q, new BlockPos(3, 2, 3));
        check(q.brood(helper.getLevel()), "an ant");
        check(!helper.getLevel().getEntities(com.linkyoshi90.harmontide.registry.ModGirls.type(GirlSpecies.ANT_BUG), net.minecraft.world.entity.Entity::isAlive).isEmpty(), "she is there");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "spiderWaspParalyses")
    public static void spiderWaspParalyses(GameTestHelper helper) {
        Bug w = still(helper, GirlSpecies.SPIDER_WASP_BUG, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, w, new BlockPos(5, 2, 5));
        Spider s = helper.spawn(EntityType.SPIDER, new BlockPos(6, 2, 5));
        s.setNoAi(true);
        check(w.paralyse(helper.getLevel()) == 1 && s.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "stung still");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "newtStaysLowAndRears")
    public static void newtStaysLowAndRears(GameTestHelper helper) {
        Lizardfolk n = still(helper, GirlSpecies.NEWT_LIZARDFOLK, new BlockPos(1, 2, 1));
        check(n.getBbHeight() < 1.0F, "a low hitbox: " + n.getBbHeight());
        check(GirlSpecies.NEWT_LIZARDFOLK.height() >= 1.25F, "a grown woman's height, standing");
        check(n.rearUp() && n.getAction() == AbstractMonsterGirl.Action.REAR, "up she gets");
        check(n.regrow() == 0, "only in water");
        helper.succeed();
    }
}
