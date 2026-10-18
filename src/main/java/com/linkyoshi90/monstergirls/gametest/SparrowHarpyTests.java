package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.HarpyCommands;
import com.linkyoshi90.monstergirls.entity.AbstractHarpy;
import com.linkyoshi90.monstergirls.entity.SparrowHarpy;
import com.linkyoshi90.monstergirls.entity.ai.BegAtFarmGoal;
import com.linkyoshi90.monstergirls.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;

/** Run with: ./gradlew runGameTestServer */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class SparrowHarpyTests {

    @GameTest(template = PLATFORM, skyAccess = true, batch = "spawnRulesNeedSuitableGround")
    public static void spawnRulesNeedSuitableGround(GameTestHelper helper) {
        helper.setDayTime(1000);
        helper.setBlock(new BlockPos(10, 1, 10), Blocks.STONE);
        helper.runAfterDelay(5, () -> {
            var level = helper.getLevel();
            check(SparrowHarpy.checkSparrowHarpySpawnRules(ModEntities.SPARROW_HARPY.get(), level, MobSpawnType.SPAWNER,
                    helper.absolutePos(new BlockPos(4, 2, 4)), level.random), "should spawn on sunlit grass");
            check(!SparrowHarpy.checkSparrowHarpySpawnRules(ModEntities.SPARROW_HARPY.get(), level, MobSpawnType.SPAWNER,
                    helper.absolutePos(new BlockPos(10, 2, 10)), level.random), "should not spawn on stone");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, batch = "variantSurvivesSaving")
    public static void variantSurvivesSaving(GameTestHelper helper) {
        SparrowHarpy sparrow = helper.spawn(ModEntities.SPARROW_HARPY.get(), new BlockPos(7, 2, 7));
        sparrow.setVariant(SparrowHarpy.Variant.PAPI);
        sparrow.setHomePos(new BlockPos(1, 2, 3));
        CompoundTag tag = new CompoundTag();
        sparrow.saveWithoutId(tag);
        SparrowHarpy copy = ModEntities.SPARROW_HARPY.get().create(helper.getLevel());
        copy.load(tag);
        check(copy.getVariant() == SparrowHarpy.Variant.PAPI, "variant lost: " + copy.getVariant());
        check(new BlockPos(1, 2, 3).equals(copy.getHomePos()), "home lost: " + copy.getHomePos());
        helper.succeed();
    }

    @GameTest(template = PLATFORM, batch = "feedingMakesHerSingAndAdoptsHome")
    public static void feedingMakesHerSingAndAdoptsHome(GameTestHelper helper) {
        SparrowHarpy sparrow = helper.spawn(ModEntities.SPARROW_HARPY.get(), new BlockPos(7, 2, 7));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT_SEEDS, 3));
        sparrow.interact(player, InteractionHand.MAIN_HAND);
        check(player.getMainHandItem().getCount() == 2, "feeding should use up one seed");
        check(sparrow.getHomePos() != null, "fed sparrow should adopt the spot as home");
        helper.succeedWhen(() -> check(sparrow.getAction() == AbstractHarpy.Action.SINGING, "not singing yet"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 2000, batch = "bathesInBirdbathAndMovesIn")
    public static void bathesInBirdbathAndMovesIn(GameTestHelper helper) {
        helper.setDayTime(1000);
        BlockPos bath = new BlockPos(10, 2, 10);
        helper.setBlock(bath, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        SparrowHarpy sparrow = helper.spawn(ModEntities.SPARROW_HARPY.get(), new BlockPos(4, 2, 4));
        boolean[] bathed = {false};
        helper.onEachTick(() -> {
            if (sparrow.getAction() != AbstractHarpy.Action.BATHING) return;
            bathed[0] = true;
            var rel = helper.relativeVec(sparrow.position());
            check(Math.abs(rel.x - 10.5) < 0.5 && Math.abs(rel.z - 10.5) < 0.5, "bathing outside the birdbath at " + rel);
        });
        helper.succeedWhen(() -> {
            check(bathed[0], "hasn't bathed yet");
            check(helper.absolutePos(bath.above()).equals(sparrow.getHomePos()), "birdbath should become her home, home=" + sparrow.getHomePos());
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 1200, batch = "eatsSeedsOffTheGround")
    public static void eatsSeedsOffTheGround(GameTestHelper helper) {
        SparrowHarpy sparrow = helper.spawn(ModEntities.SPARROW_HARPY.get(), new BlockPos(4, 2, 4));
        sparrow.setHealth(3.0F);
        helper.spawnItem(Items.WHEAT_SEEDS, new BlockPos(9, 2, 9));
        helper.succeedWhen(() -> {
            check(sparrow.getHealth() > 3.0F, "hasn't eaten yet");
            check(!sparrow.isHoldingSomething(), "should have finished eating");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 1200, batch = "picksUpTrinkets")
    public static void picksUpTrinkets(GameTestHelper helper) {
        SparrowHarpy sparrow = helper.spawn(ModEntities.SPARROW_HARPY.get(), new BlockPos(4, 2, 4));
        helper.spawnItem(Items.FEATHER, new BlockPos(9, 2, 9));
        helper.spawnItem(Items.COBBLESTONE, new BlockPos(4, 2, 9));
        helper.succeedWhen(() -> check(sparrow.getMainHandItem().is(Items.FEATHER), "not carrying the feather"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 8000, batch = "snatchesBreadFromHand")
    public static void snatchesBreadFromHand(GameTestHelper helper) {
        SparrowHarpy sparrow = helper.spawn(ModEntities.SPARROW_HARPY.get(), new BlockPos(4, 2, 4));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(9, 2, 9));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 5));
        helper.succeedWhen(() -> {
            check(player.getMainHandItem().getCount() == 4, "bread not snatched yet");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 2000, batch = "dustBathesOnSand")
    public static void dustBathesOnSand(GameTestHelper helper) {
        helper.setBlock(new BlockPos(10, 1, 10), Blocks.SAND);
        SparrowHarpy sparrow = helper.spawn(ModEntities.SPARROW_HARPY.get(), new BlockPos(4, 2, 4));
        helper.succeedWhen(() -> {
            check(sparrow.getAction() == AbstractHarpy.Action.BATHING, "not dust-bathing yet");
            var rel = helper.relativeVec(sparrow.position());
            check(Math.abs(rel.x - 10.5) < 0.5 && Math.abs(rel.z - 10.5) < 0.5, "bathing off the sand at " + rel);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 6000, batch = "begsAtTheFarm")
    public static void begsAtTheFarm(GameTestHelper helper) {
        helper.setDayTime(1000);
        for (int x = 10; x <= 12; x++) {
            helper.setBlock(new BlockPos(x, 1, 11), Blocks.FARMLAND);
            helper.setBlock(new BlockPos(x, 2, 11), Blocks.WHEAT);
        }
        SparrowHarpy sparrow = helper.spawn(ModEntities.SPARROW_HARPY.get(), new BlockPos(3, 2, 3));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(11, 2, 8));
        helper.succeedWhen(() -> {
            boolean begging = sparrow.goalSelector.getAvailableGoals().stream()
                    .anyMatch(g -> g.isRunning() && g.getGoal() instanceof BegAtFarmGoal beg && beg.isBegging());
            check(begging, "not begging at the field yet");
            HarpyTestUtil.remove(player);
        });
    }

    @GameTest(template = PLATFORM, batch = "sparrowBondsShowInTheCommand")
    public static void bondsShowInTheCommand(GameTestHelper helper) {
        SparrowHarpy sparrow = helper.spawn(ModEntities.SPARROW_HARPY.get(), new BlockPos(7, 2, 7));
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 9));
        for (int i = 0; i < 3; i++) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SWEET_BERRIES));
            sparrow.interact(player, InteractionHand.MAIN_HAND);
        }
        var lines = HarpyCommands.describeAll(player, 8);
        check(lines.size() == 1, "expected one harpy in range, got " + lines.size());
        check(lines.get(0).getContents() instanceof TranslatableContents t && t.getKey().equals("bond.monstergirls.sparrow.likes"),
                "three berries should make her like you: " + lines.get(0).getString());
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, skyAccess = true, batch = "sparrowSpawnsInSnowyVillages")
    public static void spawnsOnSnow(GameTestHelper helper) {
        helper.setDayTime(1000);
        helper.setBlock(new BlockPos(4, 1, 4), Blocks.SNOW_BLOCK);
        helper.runAfterDelay(5, () -> {
            check(SparrowHarpy.checkSparrowHarpySpawnRules(ModEntities.SPARROW_HARPY.get(), helper.getLevel(), MobSpawnType.SPAWNER,
                    helper.absolutePos(new BlockPos(4, 2, 4)), helper.getLevel().random), "should spawn on snow (snowy villages)");
            helper.succeed();
        });
    }
}
