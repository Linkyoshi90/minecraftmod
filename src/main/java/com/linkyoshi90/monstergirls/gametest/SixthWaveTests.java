package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.FourSymbols;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.ZhuqueHarpy;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.Canid;
import com.linkyoshi90.monstergirls.entity.girl.Flora;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Lizardfolk;
import com.linkyoshi90.monstergirls.entity.girl.Rodent;
import com.linkyoshi90.monstergirls.entity.girl.SlimeGirl;
import com.linkyoshi90.monstergirls.entity.girl.Undead;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The sixth Harmontide wave (HARMONTIDE_CONCEPTS.md): kobold, grizzly, the rodents, leshy, Xuanwu, La Llorona, Zhuque. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class SixthWaveTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "koboldSniffsOreAndLooseCeilings")
    public static void koboldSniffsOreAndLooseCeilings(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 4));
        Canid kobold = spawn(helper, GirlSpecies.KOBOLD_CANID, new BlockPos(6, 2, 6));
        kobold.setNoAi(true);
        kobold.setAffection(player.getUUID(), Orders.AFFECTION);
        helper.setBlock(new BlockPos(9, 2, 9), Blocks.DEEPSLATE_DIAMOND_ORE);
        check(helper.absolutePos(new BlockPos(9, 2, 9)).equals(kobold.sniffOre(helper.getLevel())), "she smells the diamonds");
        helper.setBlock(new BlockPos(4, 4, 4), Blocks.STONE);
        helper.setBlock(new BlockPos(4, 5, 4), Blocks.GRAVEL);
        player.setXRot(-90.0F);                                             // looking straight up at the stone holding the gravel
        kobold.warnOfCaveIn(helper.getLevel(), player);
        check(kobold.getAction() == AbstractMonsterGirl.Action.POINT, "she yelps at the loose ceiling");
        BlockPos chest = new BlockPos(2, 2, 8);
        helper.setBlock(chest, Blocks.CHEST);
        check(!SlimeGirl.isMimicChest(helper.getLevel(), helper.absolutePos(chest)), "a chest anyone placed is never a mimic");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "grizzlyRaidsAFullHive")
    public static void grizzlyRaidsAFullHive(GameTestHelper helper) {
        BlockPos hive = new BlockPos(6, 2, 6);
        helper.setBlock(hive, Blocks.BEEHIVE.defaultBlockState().setValue(BeehiveBlock.HONEY_LEVEL, BeehiveBlock.MAX_HONEY_LEVELS));
        Canid bear = spawn(helper, GirlSpecies.GRIZZLY_CANID, new BlockPos(6, 2, 5));
        bear.setNoAi(true);
        bear.raidHive(helper.getLevel());
        check(helper.getBlockState(hive).getValue(BeehiveBlock.HONEY_LEVEL) == 0, "she scooped out the honey");
        check(!bear.isHibernating(), "no snow, no hibernation");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "capybaraCalmsPredators")
    public static void capybaraCalmsPredators(GameTestHelper helper) {
        Rodent capy = spawn(helper, GirlSpecies.CAPYBARA_RODENT, new BlockPos(5, 2, 5));
        Wolf wolf = helper.spawn(EntityType.WOLF, new BlockPos(7, 2, 7));
        Sheep sheep = helper.spawn(EntityType.SHEEP, new BlockPos(9, 2, 9));
        wolf.setTarget(sheep);
        capy.spreadCalm(helper.getLevel());
        check(wolf.getTarget() == null, "no wolf hunts near a capybara");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "ratatoskrCarriesMessagesAndPlantsTrees")
    public static void ratatoskrCarriesMessagesAndPlantsTrees(GameTestHelper helper) {
        ServerPlayer from = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Rodent squirrel = spawn(helper, GirlSpecies.RATATOSKR_RODENT, new BlockPos(4, 2, 4));
        squirrel.setNoAi(true);
        squirrel.setAffection(from.getUUID(), Orders.AFFECTION);
        check(!squirrel.takeParcel(from, new ItemStack(Items.DIAMOND)), "nobody else to take it to");
        ServerPlayer to = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 4));
        squirrel.setAffection(to.getUUID(), Orders.AFFECTION);
        ItemStack letter = new ItemStack(Items.DIAMOND);
        check(squirrel.takeParcel(from, letter) && letter.isEmpty() && squirrel.getParcel().is(Items.DIAMOND), "she takes the parcel");
        squirrel.deliver(helper.getLevel());
        check(squirrel.getParcel().isEmpty() && !to.getInventory().isEmpty(), "and hands it (or a prank) to the other friend");
        helper.setBlock(new BlockPos(4, 1, 4), Blocks.GRASS_BLOCK);
        squirrel.getCache().addItem(new ItemStack(Items.OAK_SAPLING));
        check(squirrel.forgetASapling(helper.getLevel()) && helper.getBlockState(new BlockPos(4, 2, 4)).is(Blocks.OAK_SAPLING), "a forgotten sapling takes root");
        HarpyTestUtil.remove(from);
        HarpyTestUtil.remove(to);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "leshyCountsTrees")
    public static void leshyCountsTrees(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Flora leshy = spawn(helper, GirlSpecies.LESHY_FLORA, new BlockPos(6, 2, 6));
        leshy.setNoAi(true);
        check(!leshy.canShareBed(), "the leshy never shares a bed");
        for (int i = 0; i < Flora.PLANTER_REWARD; i++) leshy.onSaplingPlanted(helper.getLevel(), player);
        check(leshy.getAffection(player) == 2, "planters win him over");
        check(!helper.getLevel().getEntitiesOfClass(ItemEntity.class, leshy.getBoundingBox().inflate(3), e -> e.getItem().is(Items.SPRUCE_SAPLING)).isEmpty(),
                "and get saplings");
        for (int i = 0; i < -Flora.CLEAR_CUT; i++) leshy.onLogCut(helper.getLevel(), player);
        check(player.hasEffect(MobEffects.BLINDNESS), "a clear-cutter is lost in his fog");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "blackTortoiseShrugsOffArrows")
    public static void blackTortoiseShrugsOffArrows(GameTestHelper helper) {
        Lizardfolk xuanwu = spawn(helper, GirlSpecies.BLACK_TORTOISE_LIZARDFOLK, new BlockPos(5, 2, 5));
        Arrow arrow = helper.spawn(EntityType.ARROW, new BlockPos(8, 2, 8));
        float health = xuanwu.getHealth();
        check(!xuanwu.hurt(helper.getLevel().damageSources().arrow(arrow, null), 6.0F) && xuanwu.getHealth() == health, "arrows glance off her shell");
        check(xuanwu.hurt(helper.getLevel().damageSources().generic(), 2.0F), "other blows still land");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "lloronaBringsTheLostHome")
    public static void lloronaBringsTheLostHome(GameTestHelper helper) {
        helper.getLevel().setDayTime(18000);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Undead llorona = spawn(helper, GirlSpecies.LLORONA_UNDEAD, new BlockPos(5, 2, 5));
        llorona.setNoAi(true);
        check(llorona.isWeeping(), "she weeps at night");
        Cow calf = helper.spawn(EntityType.COW, new BlockPos(6, 2, 6));
        calf.setBaby(true);
        llorona.bringHome(helper.getLevel(), calf);
        check(!llorona.isWeeping(), "a lost one home, she stops for the night");
        check(llorona.getAffection(player) == 3, "and thanks whoever walked with her");
        helper.getLevel().setDayTime(1000);
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "zhuqueRipensCropsAndTheFourSymbols")
    public static void zhuqueRipensCropsAndTheFourSymbols(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        ZhuqueHarpy bird = helper.spawn(ModEntities.ZHUQUE_HARPY.get(), new BlockPos(5, 2, 5));
        bird.setNoAi(true);
        check(bird.fireImmune(), "the Vermilion Bird never burns");
        for (int x = 4; x <= 6; x++) {
            for (int z = 7; z <= 9; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.FARMLAND);
                helper.setBlock(new BlockPos(x, 2, z), Blocks.WHEAT);
            }
        }
        int grown = 0;
        for (int i = 0; i < 10 && grown == 0; i++) grown = bird.ripenCrops(helper.getLevel());
        check(grown > 0, "crops ripen around her");
        bird.setAffection(player.getUUID(), Orders.AFFECTION);
        var four = helper.getLevel().getServer().getAdvancements().get(FourSymbols.ID);
        check(four != null, "the Four Symbols advancement exists");
        check(player.getAdvancements().getOrStartProgress(four).getCriterion("south").isDone(), "befriending Zhuque marks the south");
        Lizardfolk xuanwu = spawn(helper, GirlSpecies.BLACK_TORTOISE_LIZARDFOLK, new BlockPos(8, 2, 3));
        xuanwu.setAffection(player.getUUID(), Orders.AFFECTION);
        check(player.getAdvancements().getOrStartProgress(four).getCriterion("north").isDone(), "and Xuanwu the north");
        check(!player.getAdvancements().getOrStartProgress(four).isDone(), "the east and west still wait");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }
}
