package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Armoured;
import com.linkyoshi90.harmontide.entity.girl.Centaur;
import com.linkyoshi90.harmontide.entity.girl.Devil;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Kitsune;
import com.linkyoshi90.harmontide.entity.girl.Lamia;
import com.linkyoshi90.harmontide.entity.girl.Lizardfolk;
import com.linkyoshi90.harmontide.entity.girl.Merfolk;
import com.linkyoshi90.harmontide.entity.girl.Minotaur;
import com.linkyoshi90.harmontide.entity.girl.Nekomata;
import com.linkyoshi90.harmontide.entity.girl.Rodent;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.interact;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/**
 * The fifteenth Harmontide wave (HARMONTIDE_CONCEPTS.md): crocora, iron kobold, mooskuh, bellmare, vicunero,
 * rockhyrax, riverdillo, reeffin, caraviper, bakeneko, rokurokubi, huli jing.
 */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class FifteenthWaveTests {

    /** A friend for her: a survival player with enough friendship to count. */
    private static ServerPlayer friendOf(GameTestHelper helper, AbstractMonsterGirl girl, BlockPos at) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, at);
        girl.addAffection(p, Orders.AFFECTION);
        return p;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "crocoraHaulsHerFriendUp")
    public static void crocoraHaulsHerFriendUp(GameTestHelper helper) {
        for (int y = 2; y <= 4; y++) helper.setBlock(new BlockPos(5, y, 5), Blocks.WATER);
        Lizardfolk croc = spawn(helper, GirlSpecies.CROCORA_LIZARDFOLK, new BlockPos(7, 2, 5));
        croc.setNoAi(true);
        ServerPlayer p = friendOf(helper, croc, new BlockPos(5, 2, 5));
        check(!croc.rescue(helper.getLevel()), "with air to spare, she leaves her friend be");
        p.setAirSupply(0);
        check(croc.rescue(helper.getLevel()), "out of air, she hauls her friend up");
        check(p.getAirSupply() == p.getMaxAirSupply(), "and her friend's air is full again");
        check(helper.relativeVec(p.position()).y >= 4.99, "at the surface: " + helper.relativeVec(p.position()));
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "ironKoboldMendsIronGear")
    public static void ironKoboldMendsIronGear(GameTestHelper helper) {
        check(Lizardfolk.isIronGear(new ItemStack(Items.CHAINMAIL_CHESTPLATE)), "chainmail is hers to mend");
        check(!Lizardfolk.isIronGear(new ItemStack(Items.GOLDEN_SWORD)), "gold is not");
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Lizardfolk kobold = spawn(helper, GirlSpecies.IRON_KOBOLD_LIZARDFOLK, new BlockPos(4, 2, 3));
        kobold.setNoAi(true);
        p.getInventory().setItem(8, new ItemStack(Items.IRON_INGOT, 2));
        ItemStack pick = new ItemStack(Items.IRON_PICKAXE);
        pick.setDamageValue(200);
        interact(kobold, p, pick);
        check(p.getMainHandItem().getDamageValue() < 200, "the pickaxe is mended: " + p.getMainHandItem().getDamageValue());
        check(p.getInventory().getItem(8).getCount() == 1, "for one iron ingot");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "mooskuhGrowsMossOnStone")
    public static void mooskuhGrowsMossOnStone(GameTestHelper helper) {
        for (int x = 3; x <= 7; x++) for (int z = 3; z <= 7; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
        Minotaur calf = spawn(helper, GirlSpecies.MOOSKUH_MINOTAUR, new BlockPos(5, 2, 5));
        helper.succeedWhen(() -> {
            check(calf.growMoss(helper.getLevel()), "not grown yet");
            check(helper.getLevel().getBlockState(calf.blockPosition().below()).is(Blocks.MOSS_BLOCK), "the stone under her is moss");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "bellmareLeadsTheHerd")
    public static void bellmareLeadsTheHerd(GameTestHelper helper) {
        Centaur mare = spawn(helper, GirlSpecies.BELLMARE_CENTAUR, new BlockPos(5, 2, 5));
        mare.setNoAi(true);
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(12, 2, 12));
        check(mare.leadHerd(helper.getLevel()) == 0, "with no friend about, she leads no one");
        ServerPlayer p = friendOf(helper, mare, new BlockPos(2, 2, 2));
        helper.runAfterDelay(10, () -> {                                            // the cow has to land before she can walk anywhere
            check(mare.leadHerd(helper.getLevel()) == 1, "befriended, the cow comes to her");
            check(cow.getNavigation().isInProgress(), "the cow is on her way");
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "vicuneroSpitsAtMonsters")
    public static void vicuneroSpitsAtMonsters(GameTestHelper helper) {
        Centaur vic = spawn(helper, GirlSpecies.VICUNERO_CENTAUR, new BlockPos(4, 2, 4));
        vic.setNoAi(true);
        ServerPlayer p = friendOf(helper, vic, new BlockPos(2, 2, 2));
        check(vic.spitAtMonster(helper.getLevel()) == null, "nothing to spit at");
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(8, 2, 8));
        z.setNoAi(true);
        LlamaSpit spit = vic.spitAtMonster(helper.getLevel());
        check(spit != null && spit.getOwner() == vic, "she spits at the zombie");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "rockhyraxWarnsOfCreepers")
    public static void rockhyraxWarnsOfCreepers(GameTestHelper helper) {
        Rodent hyrax = spawn(helper, GirlSpecies.ROCKHYRAX_RODENT, new BlockPos(3, 2, 3));
        hyrax.setNoAi(true);
        ServerPlayer p = friendOf(helper, hyrax, new BlockPos(2, 2, 2));
        Creeper c = helper.spawn(EntityType.CREEPER, new BlockPos(10, 2, 10));
        c.setNoAi(true);
        check(hyrax.sentinel(helper.getLevel()) == c, "she spots the creeper");
        check(c.hasEffect(MobEffects.GLOWING), "and it glows");
        check(hyrax.sentinel(helper.getLevel()) == null, "once it glows, she has warned enough");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "riverdilloCatchesFish")
    public static void riverdilloCatchesFish(GameTestHelper helper) {
        for (int x = 4; x <= 6; x++) for (int z = 4; z <= 6; z++) helper.setBlock(new BlockPos(x, 2, z), Blocks.WATER);
        Armoured dillo = spawn(helper, GirlSpecies.RIVERDILLO_ARMOURED, new BlockPos(5, 2, 5));
        dillo.setNoAi(true);
        ServerPlayer p = friendOf(helper, dillo, new BlockPos(2, 2, 2));
        helper.runAfterDelay(2, () -> {
            ItemStack fish = dillo.angle(helper.getLevel());
            check(!fish.isEmpty(), "in the water she catches a fish");
            check(!helper.getLevel().getEntitiesOfClass(ItemEntity.class, p.getBoundingBox().inflate(1.5)).isEmpty(), "and drops it at her friend's feet");
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "reeffinGrowsCoral")
    public static void reeffinGrowsCoral(GameTestHelper helper) {
        for (int x = 3; x <= 7; x++) {
            for (int z = 3; z <= 7; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);              // so the sand does not fall
                helper.setBlock(new BlockPos(x, 1, z), Blocks.SAND);
                helper.setBlock(new BlockPos(x, 2, z), Blocks.WATER);
                helper.setBlock(new BlockPos(x, 3, z), Blocks.WATER);
            }
        }
        Merfolk reef = spawn(helper, GirlSpecies.REEFFIN_MERMAID, new BlockPos(5, 2, 5));
        reef.setNoAi(true);
        helper.runAfterDelay(2, () -> {
            BlockPos at = reef.tendReef(helper.getLevel());
            check(at != null, "she finds bare sand under the water");
            check(!helper.getLevel().getBlockState(at).is(Blocks.WATER) && helper.getLevel().getFluidState(at).isSource(), "and grows something there, under water");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "caraviperBlindsMonsters")
    public static void caraviperBlindsMonsters(GameTestHelper helper) {
        Lamia viper = spawn(helper, GirlSpecies.CARAVIPER_LAMIA, new BlockPos(4, 2, 4));
        viper.setNoAi(true);
        ServerPlayer p = friendOf(helper, viper, new BlockPos(2, 2, 2));
        Creeper c = helper.spawn(EntityType.CREEPER, new BlockPos(8, 2, 8));       // not a spider: spiders shrug off poison
        c.setNoAi(true);
        check(viper.spitVenom(helper.getLevel()) == c, "she spits at the creeper");
        check(c.hasEffect(MobEffects.BLINDNESS) && c.hasEffect(MobEffects.POISON), "blinded and poisoned");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "bakenekoLightsTheNight")
    public static void bakenekoLightsTheNight(GameTestHelper helper) {
        Nekomata cat = spawn(helper, GirlSpecies.BAKENEKO_NEKOMATA, new BlockPos(4, 2, 4));
        cat.setNoAi(true);
        check(cat.ghostLights(helper.getLevel(), false) == 0, "by day her wisps fade");
        check(cat.ghostLights(helper.getLevel(), true) == 7, "at night they glow");
        ServerPlayer p = friendOf(helper, cat, new BlockPos(3, 2, 3));
        check(cat.ghostLights(helper.getLevel(), true) == 13, "brighter beside her friend");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "rokurokubiWatchesAtNight")
    public static void rokurokubiWatchesAtNight(GameTestHelper helper) {
        Devil neck = spawn(helper, GirlSpecies.ROKUROKUBI_DEVIL, new BlockPos(4, 2, 4));
        neck.setNoAi(true);
        ServerPlayer p = friendOf(helper, neck, new BlockPos(2, 2, 2));
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(10, 2, 10));
        z.setNoAi(true);
        check(neck.nightWatch(helper.getLevel(), false) == 0 && neck.getAction() != AbstractMonsterGirl.Action.WATCH, "by day she is an ordinary girl");
        check(neck.nightWatch(helper.getLevel(), true) >= 1, "at night she spots the zombie");
        check(z.hasEffect(MobEffects.GLOWING), "and it glows");
        check(neck.getAction() == AbstractMonsterGirl.Action.WATCH, "her neck stretched high");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "huliJingCharmsTheVillage")
    public static void huliJingCharmsTheVillage(GameTestHelper helper) {
        Kitsune fox = spawn(helper, GirlSpecies.HULI_JING_KITSUNE, new BlockPos(3, 2, 3));
        fox.setNoAi(true);
        fox.finalizeSpawn(helper.getLevel(), helper.getLevel().getCurrentDifficultyAt(fox.blockPosition()), MobSpawnType.SPAWN_EGG, null);
        check(fox.getTails() == 9, "she has all nine tails: " + fox.getTails());
        ServerPlayer p = friendOf(helper, fox, new BlockPos(2, 2, 2));
        check(!fox.charm(helper.getLevel()), "no villagers, no one to charm");
        Villager v = helper.spawn(EntityType.VILLAGER, new BlockPos(8, 2, 8));
        v.setNoAi(true);
        check(fox.charm(helper.getLevel()), "with villagers about, she charms them");
        check(p.hasEffect(MobEffects.HERO_OF_THE_VILLAGE), "and her friend trades cheaper");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
