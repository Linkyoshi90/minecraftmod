package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.EmberuHarpy;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.BatGirl;
import com.linkyoshi90.monstergirls.entity.girl.Canid;
import com.linkyoshi90.monstergirls.entity.girl.Construct;
import com.linkyoshi90.monstergirls.entity.girl.Devil;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Lamia;
import com.linkyoshi90.monstergirls.entity.girl.Merfolk;
import com.linkyoshi90.monstergirls.entity.girl.Minotaur;
import com.linkyoshi90.monstergirls.entity.girl.Nekomata;
import com.linkyoshi90.monstergirls.entity.girl.Undead;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Map;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/**
 * The nineteenth Harmontide wave (HARMONTIDE_CONCEPTS.md): Tin Jack, turquoise serpent, moopie, tuskin, sabre, obsidian
 * hound, kasurenijimi, emberu, Camazotz, akaname, hoop snake, draugr; and the homage variants and paints that came with it.
 */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class NineteenthWaveTests {

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

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "tinJackScaresPhantoms")
    public static void tinJackScaresPhantoms(GameTestHelper helper) {
        Construct jack = still(helper, GirlSpecies.TIN_JACK_CONSTRUCT, new BlockPos(1, 2, 1));
        check(jack.scarePhantoms(helper.getLevel()) == -1, "no friend, no scarecrow");
        ServerPlayer p = friendOf(helper, jack, new BlockPos(3, 2, 3));
        Phantom phantom = helper.spawn(EntityType.PHANTOM, new BlockPos(5, 5, 5));
        phantom.setNoAi(true);
        check(jack.scarePhantoms(helper.getLevel()) == 1, "she scares the phantom");
        check(phantom.hasEffect(MobEffects.WEAKNESS), "it loses heart");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "turquoiseSerpentWardsTheHurt")
    public static void turquoiseSerpentWardsTheHurt(GameTestHelper helper) {
        Lamia serpent = still(helper, GirlSpecies.TURQUOISE_SERPENT_LAMIA, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, serpent, new BlockPos(3, 2, 3));
        check(!serpent.scaleWard(helper.getLevel()), "not while her friend is well");
        p.setHealth(6.0F);
        check(serpent.scaleWard(helper.getLevel()), "her friend is badly hurt");
        check(p.hasEffect(MobEffects.DAMAGE_RESISTANCE), "and her scales ward them");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "hoopSnakeStings")
    public static void hoopSnakeStings(GameTestHelper helper) {
        Lamia hoop = still(helper, GirlSpecies.HOOP_SNAKE_LAMIA, new BlockPos(1, 2, 1));
        check(hoop.sting(helper.getLevel()) == -1, "no friend, no sting");
        ServerPlayer p = friendOf(helper, hoop, new BlockPos(3, 2, 3));
        check(hoop.sting(helper.getLevel()) == 0, "no monster near");
        Creeper c = helper.spawn(EntityType.CREEPER, new BlockPos(5, 2, 5));
        c.setNoAi(true);
        check(hoop.sting(helper.getLevel()) == 1, "she stings the creeper");
        check(c.hasEffect(MobEffects.POISON), "it is poisoned");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "moopieSoothes")
    public static void moopieSoothes(GameTestHelper helper) {
        Merfolk moopie = still(helper, GirlSpecies.MOOPIE_MERMAID, new BlockPos(1, 2, 1));
        check(moopie.seaMilk(helper.getLevel()) == -1, "no friend, no milk");
        ServerPlayer p = friendOf(helper, moopie, new BlockPos(3, 2, 3));
        p.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
        p.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200));
        p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200));
        check(moopie.seaMilk(helper.getLevel()) == 2, "poison and hunger wear off");
        check(!p.hasEffect(MobEffects.POISON) && !p.hasEffect(MobEffects.HUNGER) && p.hasEffect(MobEffects.DIG_SPEED), "the good ones stay");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "tuskinPacksSnow")
    public static void tuskinPacksSnow(GameTestHelper helper) {
        Minotaur tuskin = still(helper, GirlSpecies.TUSKIN_MINOTAUR, new BlockPos(1, 2, 1));
        check(tuskin.snowplough(helper.getLevel()) == -1, "no friend, no plough");
        ServerPlayer p = friendOf(helper, tuskin, new BlockPos(3, 2, 3));
        BlockPos drift = new BlockPos(5, 2, 5);
        helper.setBlock(drift, Blocks.POWDER_SNOW);
        p.setTicksFrozen(100);
        check(tuskin.snowplough(helper.getLevel()) == 1, "she packs the drift");
        check(helper.getBlockState(drift).is(Blocks.SNOW_BLOCK), "into a snow block");
        check(p.getTicksFrozen() == 0, "and the cold lets go of her friend");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "sabreFinishesTheWounded")
    public static void sabreFinishesTheWounded(GameTestHelper helper) {
        Nekomata sabre = still(helper, GirlSpecies.SABRE_NEKOMATA, new BlockPos(1, 2, 1));
        check(sabre.finishingBite(helper.getLevel()) == -1, "no friend, no hunt");
        ServerPlayer p = friendOf(helper, sabre, new BlockPos(3, 2, 3));
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(5, 2, 5));
        z.setNoAi(true);
        check(sabre.finishingBite(helper.getLevel()) == 0, "not while it is whole");
        z.setHealth(4.0F);
        check(sabre.finishingBite(helper.getLevel()) == 1, "she bites the wounded zombie");
        check(!z.isAlive(), "and finishes it");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "obsidianHoundCoolsLava")
    public static void obsidianHoundCoolsLava(GameTestHelper helper) {
        Canid hound = still(helper, GirlSpecies.OBSIDIAN_HOUND_CANID, new BlockPos(1, 2, 1));
        check(hound.coolLava(helper.getLevel()) == -1, "no friend, no cooling");
        ServerPlayer p = friendOf(helper, hound, new BlockPos(3, 2, 3));
        BlockPos pool = new BlockPos(5, 2, 4);
        helper.setBlock(pool, Blocks.LAVA);
        check(hound.coolLava(helper.getLevel()) == 1, "she cools the lava");
        check(helper.getBlockState(pool).is(Blocks.OBSIDIAN), "to obsidian");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "kasurenijimiHidesHerFriend")
    public static void kasurenijimiHidesHerFriend(GameTestHelper helper) {
        Undead ghost = still(helper, GirlSpecies.KASURENIJIMI_UNDEAD, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, ghost, new BlockPos(3, 2, 3));
        check(!ghost.smudge(helper.getLevel()), "only while her friend sneaks");
        p.setShiftKeyDown(true);
        check(ghost.smudge(helper.getLevel()), "her friend sneaks");
        check(p.hasEffect(MobEffects.INVISIBILITY), "and blurs out of sight");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "draugrRulesTheDead")
    public static void draugrRulesTheDead(GameTestHelper helper) {
        Undead draugr = still(helper, GirlSpecies.DRAUGR_UNDEAD, new BlockPos(1, 2, 1));
        check(draugr.layDead(helper.getLevel()) == -1, "no friend, no command");
        ServerPlayer p = friendOf(helper, draugr, new BlockPos(3, 2, 3));
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(5, 2, 5));
        z.setNoAi(true);
        z.setTarget(p);
        Creeper c = helper.spawn(EntityType.CREEPER, new BlockPos(5, 2, 1));
        c.setNoAi(true);
        c.setTarget(p);
        check(draugr.layDead(helper.getLevel()) == 1, "the zombie obeys her");
        check(z.getTarget() == null, "it forgets her friend");
        check(c.getTarget() == p, "the creeper is not hers to command");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "camazotzBitesAtNight")
    public static void camazotzBitesAtNight(GameTestHelper helper) {
        BatGirl bat = still(helper, GirlSpecies.CAMAZOTZ_BAT, new BlockPos(1, 2, 1));
        check(bat.nightBite(helper.getLevel(), true) == -1, "no friend, no bite");
        ServerPlayer p = friendOf(helper, bat, new BlockPos(3, 2, 3));
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(5, 2, 5));
        z.setNoAi(true);
        check(bat.nightBite(helper.getLevel(), false) == 0, "not by day");
        p.setHealth(10.0F);
        float before = z.getHealth();
        check(bat.nightBite(helper.getLevel(), true) == 1, "at night she bites");
        check(z.getHealth() < before && p.getHealth() > 10.0F, "the zombie's strength heals her friend");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "akanameScrubsMoss")
    public static void akanameScrubsMoss(GameTestHelper helper) {
        Devil akaname = still(helper, GirlSpecies.AKANAME_DEVIL, new BlockPos(1, 2, 1));
        check(akaname.scrub(helper.getLevel()) == -1, "no friend, no scrubbing");
        ServerPlayer p = friendOf(helper, akaname, new BlockPos(3, 2, 3));
        check(akaname.scrub(helper.getLevel()) == 0, "nothing mossy yet");
        BlockPos wall = new BlockPos(5, 2, 5);
        helper.setBlock(wall, Blocks.MOSSY_STONE_BRICKS);
        check(akaname.scrub(helper.getLevel()) == 1, "she scrubs the bricks");
        check(helper.getBlockState(wall).is(Blocks.STONE_BRICKS), "clean");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "emberuKicksMonstersAway")
    public static void emberuKicksMonstersAway(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        EmberuHarpy emu = helper.spawn(ModEntities.EMBERU_HARPY.get(), new BlockPos(1, 2, 1));
        emu.setNoAi(true);
        check(emu.kickAway(helper.getLevel()) == -1, "only for a friend");
        emu.setAffection(friend, Orders.AFFECTION);
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(5, 2, 3));
        z.setNoAi(true);
        check(emu.kickAway(helper.getLevel()) == 1, "she kicks the zombie");
        Vec3 away = z.position().subtract(friend.position());
        check(z.getDeltaMovement().dot(away) > 0, "away from her friend");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "nineteenthHomagesAndPaints")
    public static void nineteenthHomagesAndPaints(GameTestHelper helper) {
        Map<GirlSpecies, String> homages = Map.of(GirlSpecies.OCTOPUS_MOLLUSK, "octo_agent", GirlSpecies.CHAMELEON_LIZARDFOLK, "ninja_chameleon",
                GirlSpecies.TURTLE_LIZARDFOLK, "shell_trooper", GirlSpecies.KOBOLD_CANID, "town_clerk", GirlSpecies.MANTIS_BUG, "scythe_green",
                GirlSpecies.MANDRAGORA_FLORA, "leaf_sprout", GirlSpecies.AXOLOTL_MERMAID, "mud_fin", GirlSpecies.SHARK_MERMAID, "trident_hood");
        homages.forEach((s, v) -> {
            check(s.variants().contains(v), s.id() + " has the " + v + " look");
            check(AbstractMonsterGirl.HOMAGES.contains(v), v + " is rolled rarely, as a homage");
        });
        for (String paint : new String[] {"comic", "blueprint", "koi", "lemonade"}) {
            check(AbstractMonsterGirl.PAINTS.contains(paint), "the " + paint + " paint exists");
        }
        helper.succeed();
    }
}
