package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.AbstractAnuran;
import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.Bonded;
import com.linkyoshi90.monstergirls.entity.Bullfrog;
import com.linkyoshi90.monstergirls.entity.Bullywug;
import com.linkyoshi90.monstergirls.entity.CrowHarpy;
import com.linkyoshi90.monstergirls.entity.OwlHarpy;
import com.linkyoshi90.monstergirls.entity.PhoenixHarpy;
import com.linkyoshi90.monstergirls.entity.RiflebirdHarpy;
import com.linkyoshi90.monstergirls.entity.SparrowHarpy;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;

/** Sitting on command, sparrows roosting in droppers, presents from fond harpies, the phoenix's healing, bullywug slime. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class CompanionTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "stickMakesThemSit")
    public static void stickMakesThemSit(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        OwlHarpy owl = helper.spawn(ModEntities.OWL_HARPY.get(), new BlockPos(4, 2, 2));
        Bullywug frog = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(3, 2, 4));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        owl.interact(player, InteractionHand.MAIN_HAND);
        frog.interact(player, InteractionHand.MAIN_HAND);
        check(owl.isOrderedToSit() && owl.getAction() == AbstractHarpy.Action.SITTING, "the owl should sit: " + owl.getAction());
        check(frog.isOrderedToSit() && frog.getAction() == AbstractAnuran.Action.SITTING, "the frog should sit: " + frog.getAction());
        check(player.getMainHandItem().is(Items.STICK), "the stick is not used up");
        helper.runAfterDelay(40, () -> {
            check(owl.isOrderedToSit() && frog.isOrderedToSit(), "they stay sitting");
            check(owl.getNavigation().isDone() && frog.getNavigation().isDone(), "and don't wander off");
            owl.interact(player, InteractionHand.MAIN_HAND);
            frog.interact(player, InteractionHand.MAIN_HAND);
            check(!owl.isOrderedToSit() && owl.getAction() != AbstractHarpy.Action.SITTING, "the owl gets up");
            check(!frog.isOrderedToSit(), "the frog gets up");
            HarpyTestUtil.remove(player);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "sparrowRoostsInADropperAndLays")
    public static void sparrowRoostsInADropperAndLays(GameTestHelper helper) {
        BlockPos dropper = new BlockPos(7, 2, 7);
        helper.setBlock(dropper, Blocks.DROPPER);
        SparrowHarpy sparrow = helper.spawn(ModEntities.SPARROW_HARPY.get(), new BlockPos(4, 2, 4));
        sparrow.setEggTime(1);
        helper.succeedWhen(() -> {
            check(helper.absolutePos(dropper).equals(sparrow.getRoost()), "she should claim the dropper");
            check(helper.getBlockEntity(dropper) instanceof DispenserBlockEntity d
                    && d.countItem(ModItems.SPARROW_EGG.get()) == 1, "an egg should be in the dropper");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "sparrowEggIsFood")
    public static void sparrowEggIsFood(GameTestHelper helper) {
        ItemStack egg = new ItemStack(ModItems.SPARROW_EGG.get());
        check(egg.getFoodProperties(null) != null, "eaten raw");
        check(egg.is(net.neoforged.neoforge.common.Tags.Items.EGGS), "counts as an egg for recipes");
        var recipes = helper.getLevel().getRecipeManager();
        check(recipes.byKey(MonsterGirlsMod.id("cake_from_sparrow_egg")).isPresent(), "bakes into a cake");
        check(recipes.byKey(MonsterGirlsMod.id("pumpkin_pie_from_sparrow_egg")).isPresent(), "bakes into a pumpkin pie");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 800, batch = "crowStoresOreInTheMatchingChest")
    public static void crowStoresOreInTheMatchingChest(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        BlockPos chestPos = new BlockPos(10, 2, 10);
        helper.setBlock(chestPos, Blocks.CHEST);
        ChestBlockEntity chest = (ChestBlockEntity) helper.getBlockEntity(chestPos);
        chest.setItem(0, new ItemStack(Items.RAW_IRON));
        chest.setItem(1, new ItemStack(Items.RAW_GOLD));
        chest.setItem(2, new ItemStack(Items.DIAMOND));
        CrowHarpy crow = helper.spawn(ModEntities.CROW_HARPY.get(), new BlockPos(5, 2, 5));
        crow.setAffection(player.getUUID(), Bonded.FULL_AFFECTION);
        crow.setNextGiftTime(0);
        helper.succeedWhen(() -> {
            int total = chest.countItem(Items.RAW_IRON) + chest.countItem(Items.RAW_GOLD) + chest.countItem(Items.DIAMOND);
            check(total == 4, "the crow should have put her find in the chest: " + total);
            check(player.getInventory().isEmpty(), "not into the player's pockets");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 800, batch = "riflebirdBringsSeeds")
    public static void riflebirdBringsSeeds(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        RiflebirdHarpy bird = helper.spawn(ModEntities.RIFLEBIRD_HARPY.get(), new BlockPos(8, 2, 8));
        check(bird.rollGift(player).is(net.neoforged.neoforge.common.Tags.Items.SEEDS), "riflebirds bring seeds");
        bird.setAffection(player.getUUID(), Bonded.FULL_AFFECTION);
        bird.setNextGiftTime(0);
        helper.succeedWhen(() -> {
            check(!player.getInventory().isEmpty(), "seeds should be in the player's inventory");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "giftsNeedHighFriendship")
    public static void giftsNeedHighFriendship(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        RiflebirdHarpy bird = helper.spawn(ModEntities.RIFLEBIRD_HARPY.get(), new BlockPos(4, 2, 4));
        bird.setAffection(player.getUUID(), Bonded.GIFT_AFFECTION - 1);
        bird.setNextGiftTime(0);
        helper.runAfterDelay(80, () -> {
            check(player.getInventory().isEmpty(), "only her favourites get presents");
            HarpyTestUtil.remove(player);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "phoenixHealsHerPlayer")
    public static void phoenixHealsHerPlayer(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        PhoenixHarpy phoenix = helper.spawn(ModEntities.PHOENIX_HARPY.get(), new BlockPos(4, 2, 4));
        phoenix.imprintOn(player);
        player.setHealth(10.0F);
        helper.succeedWhen(() -> {
            check(player.getHealth() > 10.0F, "she should be healing her player: " + player.getHealth());
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "bullywugDropsSlimeBalls")
    public static void bullywugDropsSlimeBalls(GameTestHelper helper) {
        Bullywug wug = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(4, 2, 4));
        Bullfrog bullfrog = helper.spawn(ModEntities.BULLFROG.get(), new BlockPos(10, 2, 10));
        wug.setSlimeTime(2);
        bullfrog.setSlimeTime(2);
        helper.runAfterDelay(20, () -> {
            AABB nearWug = new AABB(helper.absolutePos(new BlockPos(4, 2, 4))).inflate(4.0);
            AABB nearFrog = new AABB(helper.absolutePos(new BlockPos(10, 2, 10))).inflate(3.0);
            check(!helper.getLevel().getEntitiesOfClass(ItemEntity.class, nearWug, i -> i.getItem().is(Items.SLIME_BALL)).isEmpty(),
                    "the bullywug should have left a slime ball");
            check(helper.getLevel().getEntitiesOfClass(ItemEntity.class, nearFrog, i -> i.getItem().is(Items.SLIME_BALL)).isEmpty(),
                    "the bullfrog keeps her skin to herself");
            helper.succeed();
        });
    }
}
