package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.PeaflareHarpy;
import com.linkyoshi90.harmontide.entity.girl.BatGirl;
import com.linkyoshi90.harmontide.entity.girl.Bug;
import com.linkyoshi90.harmontide.entity.girl.Centaur;
import com.linkyoshi90.harmontide.entity.girl.Construct;
import com.linkyoshi90.harmontide.entity.girl.Devil;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Kitsune;
import com.linkyoshi90.harmontide.entity.girl.Merfolk;
import com.linkyoshi90.harmontide.entity.girl.Nekomata;
import com.linkyoshi90.harmontide.entity.girl.Rodent;
import com.linkyoshi90.harmontide.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.interact;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/**
 * The thirteenth Harmontide wave (HARMONTIDE_CONCEPTS.md): rust otter, tin squirrel, peaflare, wattlefox, scarabronze,
 * datebat, snowloper, jaglare, squirricerat, kamaitachi, momoshika, chochin-obake.
 */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class ThirteenthWaveTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "rustOtterScrapesCopper")
    public static void rustOtterScrapesCopper(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        helper.setBlock(new BlockPos(5, 2, 3), Blocks.WEATHERED_COPPER);
        Merfolk otter = spawn(helper, GirlSpecies.RUST_OTTER_MERMAID, new BlockPos(6, 2, 6));
        otter.setNoAi(true);
        check(!otter.scrape(helper.getLevel()), "she scrapes only for a friend");
        otter.addAffection(friend, Orders.AFFECTION);
        check(otter.scrape(helper.getLevel()), "she scrapes the copper");
        check(helper.getBlockState(new BlockPos(5, 2, 3)).is(Blocks.EXPOSED_COPPER), "one stage greener to one stage cleaner");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "tinSquirrelScrapsAndHoards")
    public static void tinSquirrelScrapsAndHoards(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Construct tin = spawn(helper, GirlSpecies.TIN_SQUIRREL_CONSTRUCT, new BlockPos(4, 2, 3));
        tin.setNoAi(true);
        ItemStack pick = new ItemStack(Items.IRON_PICKAXE);
        pick.setDamageValue(pick.getMaxDamage() / 2);
        interact(tin, p, pick);
        check(pick.isEmpty(), "she takes the worn pickaxe");
        check(p.getInventory().countItem(Items.IRON_NUGGET) >= 4, "and gives back nuggets: " + p.getInventory().countItem(Items.IRON_NUGGET));
        HarpyTestUtil.remove(p);
        helper.spawnItem(Items.GOLD_NUGGET, 4, 2, 3);
        helper.succeedWhen(() -> check(tin.getHoard().countItem(Items.GOLD_NUGGET) == 1, "she hoards the nugget at her feet"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "peaflareDazzles")
    public static void peaflareDazzles(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        PeaflareHarpy pea = helper.spawn(ModEntities.PEAFLARE_HARPY.get(), new BlockPos(5, 2, 5));
        pea.setNoAi(true);
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(6, 2, 3));
        z.setNoAi(true);
        check(pea.dazzle(helper.getLevel()) == 0, "she shows off only for a friend");
        pea.setAffection(friend, Orders.AFFECTION);
        check(pea.dazzle(helper.getLevel()) == 1, "up goes her fan");
        check(z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && z.hasEffect(MobEffects.WEAKNESS), "and the zombie is dazzled");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "wattlefoxDousesFires")
    public static void wattlefoxDousesFires(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Kitsune fox = spawn(helper, GirlSpecies.WATTLEFOX_KITSUNE, new BlockPos(6, 2, 6));
        fox.setNoAi(true);
        fox.addAffection(friend, Orders.AFFECTION);
        check(fox.fireImmune(), "she cannot burn");
        helper.setBlock(new BlockPos(5, 2, 3), Blocks.FIRE);
        check(fox.douse(helper.getLevel()) >= 1 && helper.getBlockState(new BlockPos(5, 2, 3)).isAir(), "the fire near her friend goes out");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "scarabronzeRollsAndGreetsTheSun")
    public static void scarabronzeRollsAndGreetsTheSun(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Bug scarab = spawn(helper, GirlSpecies.SCARABRONZE_BUG, new BlockPos(4, 2, 3));
        scarab.setNoAi(true);
        interact(scarab, friend, new ItemStack(Items.ROTTEN_FLESH));
        check(friend.getInventory().countItem(Items.BONE_MEAL) == 2, "she rolls rotten flesh into bone meal");
        scarab.addAffection(friend, Orders.AFFECTION);
        helper.getLevel().setDayTime(24000 * 3 + 10);
        check(scarab.sunrise(helper.getLevel()) && friend.hasEffect(MobEffects.DIG_SPEED), "at sunrise her friend gets Haste");
        check(!scarab.sunrise(helper.getLevel()), "once a day");
        HarpyTestUtil.remove(friend);
        helper.setDayTime(1000);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "datebatPollinates")
    public static void datebatPollinates(GameTestHelper helper) {
        helper.setBlock(new BlockPos(3, 1, 3), Blocks.FARMLAND);
        helper.setBlock(new BlockPos(3, 2, 3), Blocks.WHEAT);
        BatGirl bat = spawn(helper, GirlSpecies.DATE_BAT, new BlockPos(5, 2, 5));
        bat.setNoAi(true);
        check(bat.pollinate(helper.getLevel()) >= 1, "she pollinates");
        check(helper.getBlockState(new BlockPos(3, 2, 3)).getValue(CropBlock.AGE) == 1, "and the wheat grows");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "snowloperStridesOnSnow")
    public static void snowloperStridesOnSnow(GameTestHelper helper) {
        helper.setBlock(new BlockPos(3, 1, 3), Blocks.SNOW_BLOCK);
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Nekomata cub = spawn(helper, GirlSpecies.SNOWLOPER_NEKOMATA, new BlockPos(5, 2, 5));
        cub.setNoAi(true);
        cub.addAffection(friend, Orders.AFFECTION);
        check(!cub.canFreeze(), "she never freezes");
        check(cub.snowStride(helper.getLevel()) && friend.hasEffect(MobEffects.MOVEMENT_SPEED), "on snow, her friend is quick");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "jaglareDropsAndRoars")
    public static void jaglareDropsAndRoars(GameTestHelper helper) {
        helper.setBlock(new BlockPos(5, 2, 5), Blocks.OAK_LEAVES);
        helper.setBlock(new BlockPos(5, 3, 5), Blocks.OAK_LEAVES);
        Nekomata jag = spawn(helper, GirlSpecies.JAGLARE_NEKOMATA, new BlockPos(5, 4, 5));
        jag.setNoAi(true);
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(7, 2, 6));
        cow.setNoAi(true);
        check(jag.dropOnPrey(helper.getLevel()) == cow, "she drops on the cow below");
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        jag.addAffection(friend, Orders.AFFECTION);
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(2, 2, 6));
        z.setNoAi(true);
        check(jag.roar(helper.getLevel()) >= 1 && z.hasEffect(MobEffects.WEAKNESS), "her roar weakens the monster");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "squirriceratPlantsTrees")
    public static void squirriceratPlantsTrees(GameTestHelper helper) {
        for (int x = 0; x < 11; x++) for (int z = 0; z < 11; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.GRASS_BLOCK);
        Rodent s = spawn(helper, GirlSpecies.SQUIRRICERAT_RODENT, new BlockPos(5, 2, 5));
        s.setNoAi(true);
        boolean planted = false;
        for (int i = 0; i < 20 && !planted; i++) planted = s.buryAcorn(helper.getLevel());
        check(planted, "she buries a sapling in the grass");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "kamaitachiCutsTheUndergrowth")
    public static void kamaitachiCutsTheUndergrowth(GameTestHelper helper) {
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        helper.setBlock(new BlockPos(4, 2, 3), Blocks.SHORT_GRASS);
        helper.setBlock(new BlockPos(3, 2, 4), Blocks.COBWEB);
        helper.setBlock(new BlockPos(2, 3, 3), Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
        Rodent k = spawn(helper, GirlSpecies.KAMAITACHI_RODENT, new BlockPos(7, 2, 7));
        k.setNoAi(true);
        k.addAffection(friend, Orders.AFFECTION);
        check(k.gust(helper.getLevel()) == 2, "her gust cuts the grass and the cobweb");
        check(helper.getBlockState(new BlockPos(2, 3, 3)).is(Blocks.OAK_LEAVES), "but not leaves someone placed");
        HarpyTestUtil.remove(friend);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "momoshikaBlossomsByDay")
    public static void momoshikaBlossomsByDay(GameTestHelper helper) {
        helper.getLevel().setDayTime(6000);
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Centaur deer = spawn(helper, GirlSpecies.MOMOSHIKA_CENTAUR, new BlockPos(5, 2, 5));
        deer.setNoAi(true);
        deer.addAffection(friend, Orders.AFFECTION);
        helper.runAfterDelay(1, () -> {                                  // the sky brightens on the next tick
            check(deer.blossom(helper.getLevel()) && friend.hasEffect(MobEffects.REGENERATION), "by day her friend heals beside her");
            HarpyTestUtil.remove(friend);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "chochinLightsTheNight")
    public static void chochinLightsTheNight(GameTestHelper helper) {
        helper.getLevel().setDayTime(18000);
        Devil lantern = spawn(helper, GirlSpecies.CHOCHIN_OBAKE_DEVIL, new BlockPos(5, 2, 5));
        lantern.setNoAi(true);
        helper.succeedWhen(() -> {
            check(helper.getBlockState(new BlockPos(5, 2, 5)).is(Blocks.LIGHT), "her lantern lights the night");
            helper.setDayTime(1000);
        });
    }
}
