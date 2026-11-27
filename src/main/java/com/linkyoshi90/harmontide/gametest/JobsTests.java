package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.CheeseShelfBlockEntity;
import com.linkyoshi90.harmontide.entity.Dairy;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.Shamblers;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Crustacean;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Jobs;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.linkyoshi90.harmontide.registry.ModComponents;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.interact;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Work about the home (entity/girl/Jobs, item/CrabMendRecipe, entity/Shamblers). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class JobsTests {

    private static <T extends AbstractMonsterGirl> T befriended(GameTestHelper helper, GirlSpecies s, BlockPos at) {
        T g = spawn(helper, s, at);
        g.setAffection(UUID.randomUUID(), 5);                                // fond of someone (not here): she works
        return g;
    }

    private static int count(Container c, net.minecraft.world.item.Item item) {
        int n = 0;
        for (int i = 0; i < c.getContainerSize(); i++) if (c.getItem(i).is(item)) n += c.getItem(i).getCount();
        return n;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "dragonBreathesOnAFurnace")
    public static void dragonBreathesOnAFurnace(GameTestHelper helper) {
        BlockPos at = new BlockPos(8, 2, 7);
        helper.setBlock(at, Blocks.FURNACE);
        AbstractFurnaceBlockEntity f = (AbstractFurnaceBlockEntity) helper.getBlockEntity(at);
        f.setItem(0, new ItemStack(Items.RAW_IRON, 2));
        AbstractMonsterGirl dragon = befriended(helper, GirlSpecies.WYRM_DRAGON, new BlockPos(4, 2, 7));
        check(Jobs.breatheOn(helper.getLevel(), dragon, helper.absolutePos(at)), "a furnace with ore in it: she breathes on it");
        check(f.litTime > 0 && helper.getBlockState(at).getValue(AbstractFurnaceBlock.LIT), "it burns with no fuel in it");
        check(Jobs.furnaceToTend(helper.getLevel(), dragon.blockPosition(), Jobs.REACH) != null, "she finds it from where she stands");
        helper.succeedWhen(() -> check(f.getItem(2).is(Items.IRON_INGOT), "and she keeps at it until the iron is done"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "waterDragonsAndFairiesHaveOtherWork")
    public static void waterDragonsAndFairiesHaveOtherWork(GameTestHelper helper) {
        check(Jobs.COLD_DRAGONS.contains(GirlSpecies.LONG_DRAGON), "the rain bringer does not breathe fire");
        check(Jobs.FAIRIES.contains(GirlSpecies.FAIRY_BUG), "fairies keep to their fountains");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "cowCarriesMilkToTheShelf")
    public static void cowCarriesMilkToTheShelf(GameTestHelper helper) {
        BlockPos shelfAt = new BlockPos(9, 2, 7), chestAt = new BlockPos(4, 2, 7);
        helper.setBlock(shelfAt, ModBlocks.CHEESE_SHELF.get());
        helper.setBlock(chestAt, Blocks.CHEST);
        ChestBlockEntity chest = (ChestBlockEntity) helper.getBlockEntity(chestAt);
        chest.setItem(0, new ItemStack(Items.MILK_BUCKET));
        befriended(helper, GirlSpecies.CATTLE_MINOTAUR, new BlockPos(6, 2, 9));
        helper.succeedWhen(() -> {
            CheeseShelfBlockEntity shelf = (CheeseShelfBlockEntity) helper.getBlockEntity(shelfAt);
            check(shelf.count() == 1, "a soft cheese set on the shelf");
            check(count(chest, Items.BUCKET) == 1 && count(chest, Items.MILK_BUCKET) == 0, "the empty bucket back in the chest");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "cowStoresTheHardCheese")
    public static void cowStoresTheHardCheese(GameTestHelper helper) {
        BlockPos shelfAt = new BlockPos(9, 2, 7), chestAt = new BlockPos(4, 2, 7);
        helper.setBlock(shelfAt, ModBlocks.CHEESE_SHELF.get());
        helper.setBlock(chestAt, Blocks.CHEST);
        CheeseShelfBlockEntity shelf = (CheeseShelfBlockEntity) helper.getBlockEntity(shelfAt);
        ItemStack hard = new ItemStack(ModItems.CHEESE.get());
        hard.set(ModComponents.CHEESE_AGE.get(), Dairy.HARD_AGE);
        check(Dairy.hardness(hard) == 2, "a hard cheese");
        shelf.store().setItem(0, hard);
        ChestBlockEntity chest = (ChestBlockEntity) helper.getBlockEntity(chestAt);
        ItemStack soft = new ItemStack(ModItems.CHEESE.get());
        soft.set(ModComponents.CHEESE_AGE.get(), 0);
        chest.setItem(0, soft);
        befriended(helper, GirlSpecies.HOLSTAUR_MINOTAUR, new BlockPos(6, 2, 9));
        helper.succeedWhen(() -> {
            check(shelf.count() == 0, "off the shelf");
            check(count(chest, ModItems.CHEESE.get()) == 2, "into the chest that holds cheese");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 1200, batch = "bugTakesHoneyCalmly")
    public static void bugTakesHoneyCalmly(GameTestHelper helper) {
        BlockPos hive = new BlockPos(9, 2, 7);
        helper.setBlock(hive, Blocks.BEEHIVE.defaultBlockState().setValue(BeehiveBlock.HONEY_LEVEL, BeehiveBlock.MAX_HONEY_LEVELS));
        befriended(helper, GirlSpecies.BEE_BUG, new BlockPos(4, 2, 7));
        helper.succeedWhen(() -> check(helper.getBlockState(hive).getValue(BeehiveBlock.HONEY_LEVEL) == 0, "the honey taken"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 1200, batch = "rodentPlantsAForest")
    public static void rodentPlantsAForest(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 6));
        AbstractMonsterGirl mouse = spawn(helper, GirlSpecies.HOUSE_MOUSE_RODENT, new BlockPos(7, 2, 7));
        mouse.setAffection(p.getUUID(), Orders.AFFECTION);
        interact(mouse, p, new ItemStack(Items.OAK_SAPLING, 3));
        check(p.getMainHandItem().isEmpty(), "she took the whole stack");
        check(Jobs.pouch(mouse).getCount() == 3 && Jobs.grove(mouse) != null, "three saplings in her pouch, for here");
        HarpyTestUtil.remove(p);
        helper.succeedWhen(() -> {
            check(Jobs.pouch(mouse).isEmpty(), "every one planted");
            int planted = 0;
            for (BlockPos q : BlockPos.betweenClosed(helper.absolutePos(new BlockPos(-4, 1, -4)), helper.absolutePos(new BlockPos(18, 4, 18)))) {
                if (helper.getLevel().getBlockState(q).is(Blocks.OAK_SAPLING)) planted++;
            }
            check(planted == 3, "three oak saplings in the ground: " + planted);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "crabPieceMendsAtTheSmithingTable")
    public static void crabPieceMendsAtTheSmithingTable(GameTestHelper helper) {
        ItemStack pick = new ItemStack(Items.DIAMOND_PICKAXE);
        pick.setDamageValue(1500);
        ItemStack piece = new ItemStack(ModItems.CRAB_ARMOUR_PIECE.get());
        var level = helper.getLevel();
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, new SmithingRecipeInput(ItemStack.EMPTY, pick, piece), level);
        check(recipe.isPresent(), "no template, a worn pickaxe and a crab armour piece");
        ItemStack mended = recipe.get().value().assemble(new SmithingRecipeInput(ItemStack.EMPTY, pick, piece), level.registryAccess());
        check(mended.is(Items.DIAMOND_PICKAXE) && mended.getDamageValue() == 1500 - Math.round(pick.getMaxDamage() * 0.5F), "half its wear gone: " + mended.getDamageValue());
        check(level.getRecipeManager().getRecipeFor(RecipeType.SMITHING, new SmithingRecipeInput(ItemStack.EMPTY, new ItemStack(Items.DIAMOND_PICKAXE), piece), level).isEmpty(),
                "nothing to mend on a new one");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "befriendedCrabShedsAPiece")
    public static void befriendedCrabShedsAPiece(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        Crustacean crab = spawn(helper, GirlSpecies.CRAB_CRUSTACEAN, new BlockPos(6, 2, 5));
        check(!crab.shed(helper.getLevel()), "a wild crab keeps her shell to herself");
        crab.setAffection(p.getUUID(), Orders.AFFECTION);
        crab.getPersistentData().putLong("NextShed", helper.getLevel().getGameTime() - 1);
        check(crab.shed(helper.getLevel()), "her time to shed");
        check(p.getInventory().countItem(ModItems.CRAB_ARMOUR_PIECE.get()) == 1, "a piece of it for her friend");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "zombieGirlKeepsTheDeadAway")
    public static void zombieGirlKeepsTheDeadAway(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        net.minecraft.world.entity.monster.Skeleton zombie = helper.spawn(EntityType.SKELETON, new BlockPos(10, 2, 10));   // (zombies are her kin anyway: entity/girl/Kinship)
        Creeper creeper = helper.spawn(EntityType.CREEPER, new BlockPos(2, 2, 10));
        zombie.setTarget(p);
        check(zombie.getTarget() == p, "alone, the dead come for them");
        zombie.setTarget(null);
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.ZOMBIE_UNDEAD, new BlockPos(6, 2, 5));
        girl.setAffection(p.getUUID(), Orders.AFFECTION);
        check(Shamblers.companion(p) == girl, "she walks with them");
        zombie.setTarget(p);
        creeper.setTarget(p);
        check(zombie.getTarget() == null && creeper.getTarget() == null, "with her, the dead and the creepers let them be");
        zombie.setLastHurtByMob(p);
        zombie.setTarget(p);
        check(zombie.getTarget() == p, "until they strike one");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
