package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsConfig;
import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.MilkingMachineBlock;
import com.linkyoshi90.monstergirls.block.MilkingMachineBlockEntity;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Minotaur;
import com.linkyoshi90.monstergirls.item.FamilyMilk;
import com.linkyoshi90.monstergirls.item.FamilyMilkItem;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The family milks and the milking machine. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class MilkTests {
    private static final BlockPos MACHINE = new BlockPos(2, 2, 2);

    private static MilkingMachineBlockEntity machine(GameTestHelper helper, ServerPlayer owner) {
        helper.setBlock(MACHINE, ModBlocks.MILKING_MACHINE.get());
        MilkingMachineBlockEntity m = (MilkingMachineBlockEntity) helper.getBlockEntity(MACHINE);
        m.setOwner(owner.getUUID());
        m.store().setItem(0, new ItemStack(Items.BUCKET, 2));
        return m;
    }

    private static <T extends AbstractMonsterGirl> T beside(GameTestHelper helper, GirlSpecies s, ServerPlayer friend) {
        T girl = spawn(helper, s, MACHINE.east());
        girl.setNoAi(true);
        girl.addAffection(friend, Orders.AFFECTION);
        return girl;
    }

    private static int count(MilkingMachineBlockEntity m, FamilyMilk milk) {
        return m.store().countItem(ModItems.MILK.get(milk).get());
    }

    @GameTest(template = PLATFORM, batch = "milk_expecting", timeoutTicks = 140)
    public static void machineMilksOnlyWhenExpecting(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        MilkingMachineBlockEntity m = machine(helper, player);
        AbstractMonsterGirl centaur = beside(helper, GirlSpecies.HORSE_CENTAUR, player);
        helper.runAfterDelay(50, () -> {
            check(count(m, FamilyMilk.CENTAUR) == 0, "not expecting, so no milk");
            centaur.getGestation().conceive(player.getUUID());
        });
        helper.runAfterDelay(120, () -> {
            check(count(m, FamilyMilk.CENTAUR) == 1, "one bucket, then she rests");
            check(m.store().countItem(Items.BUCKET) == 1, "a bucket was used");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, batch = "milk_cow", timeoutTicks = 120)
    public static void machineMilksACowAnyTime(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        MilkingMachineBlockEntity m = machine(helper, player);
        Minotaur cow = beside(helper, GirlSpecies.CATTLE_MINOTAUR, player);
        cow.setMale(false);
        helper.runAfterDelay(50, () -> {
            check(count(m, FamilyMilk.MINOTAUR) == 1, "cows need not be expecting");
            check(!cow.isMilkReady(), "shares the bell's clock");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, batch = "milk_refused", timeoutTicks = 120)
    public static void machineLeavesTheYoungAndStrangersAlone(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        MilkingMachineBlockEntity m = machine(helper, player);
        Minotaur calf = beside(helper, GirlSpecies.CATTLE_MINOTAUR, player);
        calf.setMale(false);
        calf.setBaby(true);
        AbstractMonsterGirl stranger = spawn(helper, GirlSpecies.HORSE_CENTAUR, MACHINE.west());
        stranger.setNoAi(true);
        stranger.getGestation().conceive(null);
        helper.runAfterDelay(50, () -> {
            check(m.store().countItem(Items.BUCKET) == 2, "no bucket filled");
            helper.succeed();
        });
    }

    /** Options are global, so each test here has its own batch and puts back what it found. */
    private static boolean[] options(boolean on) {
        boolean[] found = {MonsterGirlsConfig.MILK_DRIPS.get(), MonsterGirlsConfig.MILK_EMBARRASSED.get(), MonsterGirlsConfig.MILK_HARNESS.get()};
        set(on, on, on);
        return found;
    }

    private static void set(boolean drips, boolean embarrassed, boolean harness) {
        MonsterGirlsConfig.MILK_DRIPS.set(drips);
        MonsterGirlsConfig.MILK_EMBARRASSED.set(embarrassed);
        MonsterGirlsConfig.MILK_HARNESS.set(harness);
    }

    private static void restore(boolean[] found) {
        set(found[0], found[1], found[2]);
    }

    private static boolean machineIsCart(GameTestHelper helper) {
        return helper.getBlockState(MACHINE).getValue(MilkingMachineBlock.HARNESS);
    }

    @GameTest(template = PLATFORM, batch = "milk_options_off", timeoutTicks = 120)
    public static void milkingOptionsAreOffByDefault(GameTestHelper helper) {
        check(!MonsterGirlsConfig.MILK_DRIPS.getDefault() && !MonsterGirlsConfig.MILK_EMBARRASSED.getDefault() && !MonsterGirlsConfig.MILK_HARNESS.getDefault(), "all off");
        boolean[] found = options(false);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        MilkingMachineBlockEntity m = machine(helper, player);
        Minotaur cow = beside(helper, GirlSpecies.CATTLE_MINOTAUR, player);
        cow.setMale(false);
        helper.runAfterDelay(50, () -> {
            m.store().clearContent();                       // or it milks her again as soon as her bell rings
            check(!cow.isMilkReady(), "she was milked");
            check(!cow.hasFlag(AbstractMonsterGirl.FLAG_HARNESSED), "no harness, no blush");
            check(!machineIsCart(helper), "still the plain machine");
            cow.setNextMilk(helper.getLevel().getGameTime() + 2);
        });
        helper.runAfterDelay(60, () -> {
            check(cow.isMilkFull() && !cow.isDripping(), "ready again, but no drips");
            restore(found);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, batch = "milk_harness", timeoutTicks = 260)
    public static void machineHarnessesACowThenLetsHerGo(GameTestHelper helper) {
        boolean[] found = options(true);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        machine(helper, player);
        Minotaur cow = beside(helper, GirlSpecies.CATTLE_MINOTAUR, player);
        cow.setMale(false);
        helper.runAfterDelay(45, () -> {
            check(machineIsCart(helper), "the machine turned into the cart");
            check(cow.hasFlag(AbstractMonsterGirl.FLAG_HARNESSED), "in the harness");
            check(cow.hasFlag(AbstractMonsterGirl.FLAG_BLUSHING), "and embarrassed");
            check(cow.getAction() == AbstractMonsterGirl.Action.MILK, "looking away");
        });
        helper.runAfterDelay(170, () -> {
            check(!cow.hasFlag(AbstractMonsterGirl.FLAG_HARNESSED) && !cow.hasFlag(AbstractMonsterGirl.FLAG_BLUSHING), "out of it again");
            restore(found);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, batch = "milk_calf_harness", timeoutTicks = 120)
    public static void calvesAndBullsNeverWearTheHarness(GameTestHelper helper) {
        boolean[] found = options(true);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        MilkingMachineBlockEntity m = machine(helper, player);
        Minotaur calf = beside(helper, GirlSpecies.CATTLE_MINOTAUR, player);
        calf.setMale(false);
        calf.setBaby(true);
        Minotaur bull = spawn(helper, GirlSpecies.CATTLE_MINOTAUR, MACHINE.west());
        bull.setNoAi(true);
        bull.setMale(true);
        bull.addAffection(player, Orders.AFFECTION);
        helper.runAfterDelay(50, () -> {
            check(m.store().countItem(Items.BUCKET) == 2, "no bucket filled");
            check(!calf.hasFlag(AbstractMonsterGirl.FLAG_HARNESSED) && !calf.hasFlag(AbstractMonsterGirl.FLAG_BLUSHING), "not the calf");
            check(!bull.hasFlag(AbstractMonsterGirl.FLAG_HARNESSED) && !bull.hasFlag(AbstractMonsterGirl.FLAG_BLUSHING), "not the bull");
            restore(found);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, batch = "milk_drips", timeoutTicks = 80)
    public static void onlyAMilkedCowDripsUntilMilkedAgain(GameTestHelper helper) {
        boolean[] found = options(true);
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        Minotaur cow = spawn(helper, GirlSpecies.CATTLE_MINOTAUR, new BlockPos(2, 2, 2));
        cow.setMale(false);
        cow.setNoAi(true);
        check(cow.isMilkReady() && !cow.isDripping(), "a cow never milked has nothing to show");
        cow.setNextMilk(helper.getLevel().getGameTime() + 5);
        helper.runAfterDelay(15, () -> {
            check(cow.isMilkFull() && cow.isDripping(), "her bell rang: she drips");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
            cow.interact(player, InteractionHand.MAIN_HAND);
            check(!cow.isDripping(), "milking stops it");
            check(cow.hasFlag(AbstractMonsterGirl.FLAG_BLUSHING), "a bucket embarrasses her too");
            check(!cow.hasFlag(AbstractMonsterGirl.FLAG_HARNESSED), "but a bucket has no harness");
            restore(found);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, batch = "milk_rates", timeoutTicks = 40)
    public static void ratesScaleSpawnsVariantsAndPaints(GameTestHelper helper) {
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(2, 2, 2));
        check(MonsterGirlsConfig.spawnRate(girl) == 1.0, "normal by default");
        check(MonsterGirlsConfig.paintChance() == 1.0 / MonsterGirlsConfig.PAINT_RARITY, "one in 64 by default");
        check(MonsterGirlsConfig.variantChance(0.5) == 0.5, "half by default");
        MonsterGirlsConfig.SPAWN_RATE.set(50);
        MonsterGirlsConfig.PAINT_RATE.set(0);
        MonsterGirlsConfig.VARIANT_RATE.set(200);
        check(MonsterGirlsConfig.spawnRate(girl) == 0.5, "the overall slider");
        check(MonsterGirlsConfig.paintChance() == 0.0, "no paints at zero");
        check(MonsterGirlsConfig.variantChance(0.5) == 1.0, "always a colour at double");
        MonsterGirlsConfig.SPAWN_RATE.set(100);
        MonsterGirlsConfig.PAINT_RATE.set(100);
        MonsterGirlsConfig.VARIANT_RATE.set(100);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, batch = "milk_stacks", timeoutTicks = 120)
    public static void milkStacksInTheMachine(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        MilkingMachineBlockEntity m = machine(helper, player);
        ItemStack milk = new ItemStack(ModItems.MILK.get(FamilyMilk.MINOTAUR).get(), 3);
        check(milk.getMaxStackSize() == 16, "stacks as buckets do");
        m.store().setItem(1, milk);
        Minotaur cow = beside(helper, GirlSpecies.CATTLE_MINOTAUR, player);
        cow.setMale(false);
        helper.runAfterDelay(50, () -> {
            check(m.store().getItem(1).getCount() == 4, "the new bucket joins the stack");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, batch = "milk_growth", timeoutTicks = 60)
    public static void milkGrowsTheYoungAndBuffsTheDrinker(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        AbstractMonsterGirl foal = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(2, 2, 2));
        foal.setBaby(true);
        FamilyMilkItem item = ModItems.MILK.get(FamilyMilk.LAMIA).get();
        ItemStack stack = new ItemStack(item);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        item.interactLivingEntity(stack, player, foal, InteractionHand.MAIN_HAND);
        check(foal.getGrowTicks() >= FamilyMilkItem.GROWTH_TICKS, "a day closer to grown");
        check(player.getMainHandItem().is(Items.BUCKET), "the bucket comes back");
        check(foal.isBaby(), "one bucket is not enough");
        item.finishUsingItem(new ItemStack(item), helper.getLevel(), player);
        check(FamilyMilk.LAMIA.effects().stream().allMatch(player::hasEffect), "the family's effect");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 500, batch = "cowWalksToTheMachine")
    public static void cowWalksToTheMachine(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 6));
        MilkingMachineBlockEntity m = machine(helper, player);
        m.store().setItem(0, new ItemStack(net.minecraft.world.item.Items.BUCKET, 2));
        Minotaur cow = spawn(helper, GirlSpecies.CATTLE_MINOTAUR, new BlockPos(7, 2, 7));
        cow.addAffection(player, com.linkyoshi90.monstergirls.entity.Orders.AFFECTION);
        check(com.linkyoshi90.monstergirls.entity.Lactation.isLactating(cow) || cow.isMale(), "a cow has milk");
        helper.succeedWhen(() -> {
            if (cow.isMale()) return;                                   // one in a few spawns a bull
            check(count(m, FamilyMilk.of(cow)) == 1, "she came and was milked");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "lastStageLactates")
    public static void lastStageLactates(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 6));
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(5, 2, 5));
        girl.setNoAi(true);
        check(!com.linkyoshi90.monstergirls.entity.Lactation.isLactating(girl), "not expecting");
        girl.getGestation().conceive();
        check(!com.linkyoshi90.monstergirls.entity.Lactation.isLactating(girl), "too early");
        girl.getGestation().setTicksLeft(com.linkyoshi90.monstergirls.entity.Gestation.DURATION / 4);
        check(com.linkyoshi90.monstergirls.entity.Lactation.isLactating(girl), "the last stage");
        boolean drips = MonsterGirlsConfig.MILK_DRIPS.get();
        MonsterGirlsConfig.MILK_DRIPS.set(true);
        check(com.linkyoshi90.monstergirls.entity.Lactation.isDripping(girl), "drips with the option");
        MonsterGirlsConfig.MILK_DRIPS.set(false);
        check(!com.linkyoshi90.monstergirls.entity.Lactation.isDripping(girl), "not without it");
        MonsterGirlsConfig.MILK_DRIPS.set(drips);
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "hoppersOnlyHandleBucketsAndMilk")
    public static void hoppersOnlyHandleBucketsAndMilk(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 6));
        MilkingMachineBlockEntity m = machine(helper, player);
        var hopper = m.hopperHandler();
        check(!hopper.insertItem(0, new ItemStack(net.minecraft.world.item.Items.DIRT), false).isEmpty(), "no dirt");
        check(hopper.insertItem(0, new ItemStack(net.minecraft.world.item.Items.BUCKET), false).isEmpty(), "buckets go in");
        check(hopper.extractItem(0, 1, false).isEmpty(), "empty buckets stay");
        ItemStack milk = new ItemStack(ModItems.MILK.get(FamilyMilk.values()[0]).get());
        m.store().setItem(1, milk);
        check(!hopper.extractItem(1, 1, false).isEmpty(), "milk comes out");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }
}
