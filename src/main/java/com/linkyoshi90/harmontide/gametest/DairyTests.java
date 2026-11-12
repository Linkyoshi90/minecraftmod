package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.ButterChurnBlockEntity;
import com.linkyoshi90.harmontide.block.CheeseShelfBlock;
import com.linkyoshi90.harmontide.block.CheeseShelfBlockEntity;
import com.linkyoshi90.harmontide.block.FridgeBlockEntity;
import com.linkyoshi90.harmontide.block.MilkTankBlock;
import com.linkyoshi90.harmontide.block.MilkTankBlockEntity;
import com.linkyoshi90.harmontide.entity.Dairy;
import com.linkyoshi90.harmontide.entity.Lactation;
import com.linkyoshi90.harmontide.entity.MilkRescue;
import com.linkyoshi90.harmontide.entity.Nursing;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Minotaur;
import com.linkyoshi90.harmontide.item.FamilyMilk;
import com.linkyoshi90.harmontide.item.IceCreamRecipe;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.linkyoshi90.harmontide.registry.ModComponents;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Dairy (entity.Dairy): diet flavours, the churn, the cheese shelf and its filter, the milk tank, the fridge, discomfort, ice cream. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class DairyTests {
    private static final BlockPos AT = new BlockPos(2, 2, 2);

    private static ItemStack milk(FamilyMilk family) {
        return new ItemStack(ModItems.MILK.get(family).get());
    }

    private static <T extends AbstractMonsterGirl> T still(GameTestHelper helper, GirlSpecies s, BlockPos at) {
        T girl = spawn(helper, s, at);
        girl.setNoAi(true);
        return girl;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "dietFlavoursHerMilk")
    public static void dietFlavoursHerMilk(GameTestHelper helper) {
        AbstractMonsterGirl girl = still(helper, GirlSpecies.WOLF_CANID, new BlockPos(1, 2, 1));
        check(!Dairy.ate(girl, new ItemStack(Items.BREAD)), "bread is just bread");
        check(Dairy.ate(girl, new ItemStack(Items.SWEET_BERRIES)) && Dairy.ate(girl, new ItemStack(Items.HONEY_BOTTLE)), "berries and honey");
        check(Dairy.ate(girl, new ItemStack(Items.COOKIE)) && Dairy.diet(girl).size() == Dairy.DIET_KINDS && !Dairy.diet(girl).contains(MobEffects.REGENERATION),
                "two flavours at most, the oldest goes");
        ItemStack bucket = Dairy.flavoured(girl, milk(FamilyMilk.CANIDAE));
        check(bucket.getOrDefault(ModComponents.MILK_DIET.get(), List.of()).contains(MobEffects.DIG_SPEED), "the bucket carries it");
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        check(Dairy.apply(p, bucket) == 2 && p.hasEffect(MobEffects.ABSORPTION), "drinking it gives the flavours");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "churnMakesButter")
    public static void churnMakesButter(GameTestHelper helper) {
        helper.setBlock(AT, ModBlocks.BUTTER_CHURN.get());
        ButterChurnBlockEntity churn = (ButterChurnBlockEntity) helper.getBlockEntity(AT);
        BlockPos pos = helper.absolutePos(AT);
        check(!churn.churn(helper.getLevel(), pos), "nothing to churn");
        var hopper = churn.hopperHandler();
        check(!hopper.insertItem(0, new ItemStack(Items.DIRT), false).isEmpty(), "no dirt");
        check(hopper.insertItem(0, milk(FamilyMilk.AVES), false).isEmpty(), "milk goes in");
        check(!churn.pour(milk(FamilyMilk.AMPHIBIA)), "one milk at a time");
        for (int i = 0; i < ButterChurnBlockEntity.CHURNS; i++) {
            check(churn.churn(helper.getLevel(), pos), "a stroke");
            for (int t = 0; t < ButterChurnBlockEntity.STROKE_TICKS; t++) ButterChurnBlockEntity.serverTick(helper.getLevel(), pos, helper.getLevel().getBlockState(pos), churn);
        }
        ItemStack butter = hopper.extractItem(ButterChurnBlockEntity.BUTTER, 64, false);
        check(butter.is(ModItems.BUTTER.get()) && butter.getCount() == ButterChurnBlockEntity.BUTTER_PER_BUCKET
                && butter.get(ModComponents.MILK_FAMILY.get()) == FamilyMilk.AVES, "bird butter");
        check(hopper.extractItem(ButterChurnBlockEntity.BUCKETS, 64, false).is(Items.BUCKET), "the bucket comes back");
        check(hopper.extractItem(ButterChurnBlockEntity.MILK, 64, false).isEmpty(), "milk stays in");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "cheeseRipensAndTheButtonFilters")
    public static void cheeseRipensAndTheButtonFilters(GameTestHelper helper) {
        helper.setBlock(AT, ModBlocks.CHEESE_SHELF.get());
        CheeseShelfBlockEntity shelf = (CheeseShelfBlockEntity) helper.getBlockEntity(AT);
        var hopper = shelf.hopperHandler();
        check(hopper.insertItem(0, milk(FamilyMilk.BOVIDAE), false).isEmpty() && shelf.count() == 1, "milk set as cheese");
        check(helper.getBlockState(AT).getValue(CheeseShelfBlock.CHEESES) == 1, "one wheel on the shelf");
        check(helper.getBlockState(AT).getValue(CheeseShelfBlock.PULL_HARD) && hopper.extractItem(0, 1, true).isEmpty(), "soft cheese stays");
        check(hopper.extractItem(CheeseShelfBlockEntity.CHEESES, 1, false).is(Items.BUCKET), "its bucket comes out");
        shelf.age(Dairy.HARD_AGE);
        ItemStack cheese = hopper.extractItem(0, 1, false);
        check(Dairy.hardness(cheese) == 2 && Dairy.seconds(cheese) == Dairy.HARD, "hard cheese comes out");
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 4));
        check(Dairy.apply(p, cheese) == 1 && p.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() == 1, "and gives level II");
        shelf.addMilk(milk(FamilyMilk.BOVIDAE));
        check(!CheeseShelfBlock.press(helper.getLevel(), helper.absolutePos(AT), helper.getBlockState(AT)) && !hopper.extractItem(0, 1, false).isEmpty(), "the button lets soft cheese go");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "tankFillsItsOwnBuckets")
    public static void tankFillsItsOwnBuckets(GameTestHelper helper) {
        helper.setBlock(AT, ModBlocks.MILK_TANK.get());
        MilkTankBlockEntity tank = (MilkTankBlockEntity) helper.getBlockEntity(AT);
        var top = tank.hopperHandler(Direction.UP);
        var tap = tank.hopperHandler(Direction.DOWN);
        for (int i = 0; i < 3; i++) check(top.insertItem(1, milk(FamilyMilk.PERISSODACTYLA), false).isEmpty(), "milk in");
        check(!top.insertItem(1, milk(FamilyMilk.AVES), false).isEmpty(), "not another milk");
        check(tank.amount() == 3 && tank.buckets() == 3 && helper.getBlockState(AT).getValue(MilkTankBlock.LEVEL) > 0, "three buckets' worth, shown");
        ItemStack out = tap.extractItem(0, 2, false);
        check(out.is(ModItems.MILK.get(FamilyMilk.PERISSODACTYLA).get()) && out.getCount() == 2 && tank.amount() == 1 && tank.buckets() == 1, "filled into its buckets");
        tap.extractItem(0, 1, false);
        check(top.insertItem(1, milk(FamilyMilk.PERISSODACTYLA), false).isEmpty() && tap.extractItem(0, 1, false).getCount() == 1, "empty again, then refilled");
        check(tap.extractItem(0, 1, false).isEmpty(), "no milk, nothing to pull");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "fridgeFeedsTheHousehold")
    public static void fridgeFeedsTheHousehold(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 4));
        helper.setBlock(AT, ModBlocks.FRIDGE.get());
        FridgeBlockEntity fridge = (FridgeBlockEntity) helper.getBlockEntity(AT);
        fridge.setOwner(p.getUUID());
        var hopper = fridge.hopperHandler();
        check(!hopper.insertItem(0, new ItemStack(Items.DIRT), false).isEmpty(), "food only");
        check(hopper.insertItem(0, new ItemStack(Items.BREAD, 3), false).isEmpty() && hopper.insertItem(1, new ItemStack(Items.GLOW_BERRIES), false).isEmpty(), "food goes in");
        AbstractMonsterGirl stranger = still(helper, GirlSpecies.WOLF_CANID, new BlockPos(1, 2, 1));
        check(!fridge.feeds(stranger, helper.getLevel()), "not for strangers");
        AbstractMonsterGirl friend = still(helper, GirlSpecies.RED_KITSUNE, new BlockPos(1, 2, 3));
        friend.addAffection(p, 30);
        check(fridge.feeds(friend, helper.getLevel()) && FridgeBlockEntity.nearestFor(helper.getLevel(), friend, 16) != null, "a friend may");
        check(fridge.serve(helper.getLevel(), friend).is(Items.GLOW_BERRIES) && Dairy.diet(friend).contains(MobEffects.NIGHT_VISION), "she picks what flavours her milk");
        check(fridge.serve(helper.getLevel(), friend).is(Items.BREAD), "then whatever is left");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "fullUntilMilked")
    public static void fullUntilMilked(GameTestHelper helper) {
        boolean was = com.linkyoshi90.harmontide.HarmontideConfig.MILK_DISCOMFORT.get();
        com.linkyoshi90.harmontide.HarmontideConfig.MILK_DISCOMFORT.set(true);
        Minotaur cow = still(helper, GirlSpecies.CATTLE_MINOTAUR, new BlockPos(1, 2, 1));
        cow.setMale(false);
        cow.setNextMilk(0);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        cow.addAffection(p, 30);
        check(Lactation.isLactating(cow), "her milk is in");
        Lactation.comfort(helper.getLevel(), cow);
        check(!Lactation.isFull(cow), "not full yet");
        cow.getPersistentData().putLong("MilkFullSince", helper.getLevel().getGameTime() - Lactation.FULL_AFTER);
        Lactation.comfort(helper.getLevel(), cow);
        check(Lactation.isFull(cow) && cow.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "full, and slowed");
        int before = cow.getAffection(p);
        check(Lactation.relieve(helper.getLevel(), cow, p) && !Lactation.isFull(cow) && cow.getAffection(p) == before + Lactation.RELIEF, "relieved");
        com.linkyoshi90.harmontide.HarmontideConfig.MILK_DISCOMFORT.set(was);
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "iceCreamKeepsTheMilk")
    public static void iceCreamKeepsTheMilk(GameTestHelper helper) {
        IceCreamRecipe recipe = new IceCreamRecipe(CraftingBookCategory.MISC);
        CraftingInput grid = CraftingInput.of(2, 2, List.of(milk(FamilyMilk.SQUAMATA), new ItemStack(Items.SUGAR), new ItemStack(Items.SNOWBALL), new ItemStack(Items.BOWL)));
        check(recipe.matches(grid, helper.getLevel()), "the four make it");
        ItemStack ice = recipe.assemble(grid, helper.getLevel().registryAccess());
        check(ice.is(ModItems.ICE_CREAM.get()) && ice.get(ModComponents.MILK_FAMILY.get()) == FamilyMilk.SQUAMATA, "squamate ice cream");
        check(!recipe.matches(CraftingInput.of(2, 2, List.of(milk(FamilyMilk.SQUAMATA), new ItemStack(Items.SUGAR), new ItemStack(Items.DIRT), new ItemStack(Items.BOWL))), helper.getLevel()), "not with dirt");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "nursingUntilWeaned")
    public static void nursingUntilWeaned(GameTestHelper helper) {
        AbstractMonsterGirl mother = still(helper, GirlSpecies.WOLF_CANID, new BlockPos(1, 2, 1));
        check(!Lactation.isLactating(mother), "no milk before");
        AbstractMonsterGirl baby = mother.spawnYoung(helper.getLevel(), helper.absolutePos(new BlockPos(3, 2, 3)), null);
        baby.setNoAi(true);
        check(Nursing.isNursing(mother) && Lactation.isLactating(mother) && Nursing.status(helper.getLevel(), mother) == 1, "her milk comes in with the baby");
        mother.setAction(AbstractMonsterGirl.Action.NURSE);
        check(baby.startRiding(mother, true) && baby.getVehicle() == mother && mother.getControllingPassenger() == null, "held, not steering");
        int grow = baby.getGrowTicks();
        Nursing.fed(helper.getLevel(), mother, baby);
        check(baby.getGrowTicks() == grow + Nursing.GROWTH && !Lactation.isLactating(mother), "she grows, and the milk is used for now");
        baby.stopRiding();
        mother.setAction(AbstractMonsterGirl.Action.NONE);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 1));
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD));
        baby.interact(p, net.minecraft.world.InteractionHand.MAIN_HAND);
        check(baby.getPersistentData().getBoolean(Nursing.WEANED) && !Nursing.isNursing(mother) && p.getMainHandItem().isEmpty(), "weaned with bread from a hand");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "unweanedSheKeepsHerMilk")
    public static void unweanedSheKeepsHerMilk(GameTestHelper helper) {
        AbstractMonsterGirl mother = still(helper, GirlSpecies.WOLF_CANID, new BlockPos(1, 2, 1));
        AbstractMonsterGirl daughter = mother.spawnYoung(helper.getLevel(), helper.absolutePos(new BlockPos(3, 2, 3)), null);
        daughter.setNoAi(true);
        daughter.setBaby(false);
        check(Nursing.keepsMilk(helper.getLevel(), mother) && Nursing.status(helper.getLevel(), mother) == 2 && Lactation.isLactating(mother), "grown unweaned: milk for good");
        check(!Nursing.wean(helper.getLevel(), daughter, null), "too late to wean");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "dishesCarryTheirFlavour")
    public static void dishesCarryTheirFlavour(GameTestHelper helper) {
        var reg = helper.getLevel().registryAccess();
        var pie = new net.minecraft.world.item.crafting.RecipeHolder<>(HarmontideMod.id("test_pie"), new net.minecraft.world.item.crafting.ShapelessRecipe("",
                CraftingBookCategory.MISC, new ItemStack(Items.BREAD), net.minecraft.core.NonNullList.of(net.minecraft.world.item.crafting.Ingredient.EMPTY,
                net.minecraft.world.item.crafting.Ingredient.of(Items.WHEAT), net.minecraft.world.item.crafting.Ingredient.of(Items.SWEET_BERRIES))));
        var dye = new net.minecraft.world.item.crafting.RecipeHolder<>(HarmontideMod.id("test_dye"), new net.minecraft.world.item.crafting.ShapelessRecipe("",
                CraftingBookCategory.MISC, new ItemStack(Items.RED_DYE), net.minecraft.core.NonNullList.of(net.minecraft.world.item.crafting.Ingredient.EMPTY,
                net.minecraft.world.item.crafting.Ingredient.of(Items.SWEET_BERRIES))));
        var map = Dairy.recipeFlavours(List.of(pie, dye), reg);
        check(map.get(Items.BREAD) == MobEffects.REGENERATION, "a berry bread flavours like berries");
        check(!map.containsKey(Items.RED_DYE), "a dye is no dish");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "sheFostersOrphans")
    public static void sheFostersOrphans(GameTestHelper helper) {
        Minotaur cow = still(helper, GirlSpecies.CATTLE_MINOTAUR, new BlockPos(1, 2, 1));
        cow.setMale(false);
        cow.setNextMilk(0);
        var calf = helper.spawn(net.minecraft.world.entity.EntityType.COW, new BlockPos(3, 2, 3));
        calf.setAge(-24000);
        calf.setNoAi(true);
        var child = helper.spawn(net.minecraft.world.entity.EntityType.VILLAGER, new BlockPos(3, 2, 1));
        child.setAge(-24000);
        child.setNoAi(true);
        check(Nursing.isOrphan(helper.getLevel(), calf) && !Nursing.isOrphan(helper.getLevel(), child), "a lone calf, never a villager's child");
        cow.setAction(AbstractMonsterGirl.Action.NURSE);
        check(cow.nursingRider(calf), "held to her chest");
        int age = calf.getAge();
        Nursing.fed(helper.getLevel(), cow, calf);
        check(calf.getAge() > age && cow.getUUID().equals(calf.getPersistentData().getUUID(Nursing.FOSTER)), "she grows, and remembers her");
        var mom = helper.spawn(net.minecraft.world.entity.EntityType.COW, new BlockPos(3, 2, 4));
        mom.setNoAi(true);
        check(!Nursing.isOrphan(helper.getLevel(), calf), "with her own mother by, no orphan");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aCupOfMilkForThePoisoned")
    public static void aCupOfMilkForThePoisoned(GameTestHelper helper) {
        Minotaur cow = still(helper, GirlSpecies.CATTLE_MINOTAUR, new BlockPos(1, 2, 1));
        cow.setMale(false);
        cow.setNextMilk(0);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        cow.addAffection(p, 30);
        p.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.POISON, 600));
        p.setHealth(4.0F);
        check(MilkRescue.inDanger(p), "poisoned, three hearts left");
        helper.setBlock(new BlockPos(3, 5, 3), net.minecraft.world.level.block.Blocks.STONE);
        check(MilkRescue.sheltered(helper.getLevel(), p), "under a roof");
        check(!MilkRescue.accept(helper.getLevel(), cow, p), "nothing offered yet");
        MilkRescue.offer(helper.getLevel(), cow, p);
        int before = cow.getAffection(p);
        check(MilkRescue.accept(helper.getLevel(), cow, p) && !p.hasEffect(MobEffects.POISON) && p.hasEffect(MobEffects.REGENERATION)
                && cow.getAffection(p) == before + MilkRescue.AFFECTION && !Lactation.isLactating(cow), "she helps them drink, and her milk is used");
        check(!MilkRescue.accept(helper.getLevel(), cow, p), "once per offer");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
