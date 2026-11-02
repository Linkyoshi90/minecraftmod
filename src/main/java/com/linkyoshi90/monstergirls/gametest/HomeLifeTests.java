package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.CookingPotBlockEntity;
import com.linkyoshi90.monstergirls.block.SoakingTubBlock;
import com.linkyoshi90.monstergirls.block.SoakingTubBlockEntity;
import com.linkyoshi90.monstergirls.block.SparringPostBlockEntity;
import com.linkyoshi90.monstergirls.entity.HomeLife;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Life at home (HARMONTIDE_CONCEPTS.md, thirty-fourth wave). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class HomeLifeTests {
    private static AbstractMonsterGirl girl(GameTestHelper helper, BlockPos at) {
        AbstractMonsterGirl g = spawn(helper, GirlSpecies.WOLF_CANID, at);
        g.setNoAi(true);
        return g;
    }

    private static ServerPlayer friendOf(GameTestHelper helper, AbstractMonsterGirl g, BlockPos at) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, at);
        g.addAffection(p, Orders.AFFECTION);
        return p;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "brushBuildsFriendship")
    public static void brushBuildsFriendship(GameTestHelper helper) {
        AbstractMonsterGirl g = girl(helper, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, g, new BlockPos(3, 2, 3));
        int before = g.getAffection(p);
        check(HomeLife.groom(helper.getLevel(), g, p) && g.getAffection(p) == before + 2, "brushed");
        check(!HomeLife.groom(helper.getLevel(), g, p), "only every few minutes");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "potCooksOverAFire")
    public static void potCooksOverAFire(GameTestHelper helper) {
        BlockPos at = new BlockPos(2, 3, 2);
        helper.setBlock(at.below(), Blocks.CAMPFIRE);
        helper.setBlock(at, ModBlocks.COOKING_POT.get());
        BlockPos abs = helper.absolutePos(at);
        CookingPotBlockEntity pot = (CookingPotBlockEntity) helper.getLevel().getBlockEntity(abs);
        check(pot != null && !pot.add(new ItemStack(Items.STONE)), "not food");
        for (int i = 0; i < CookingPotBlockEntity.RECIPE; i++) check(pot.add(new ItemStack(Items.BREAD)), "food goes in");
        check(!pot.add(new ItemStack(Items.BREAD)), "full");
        check(CookingPotBlockEntity.heated(helper.getLevel(), abs), "a lit fire under it");
        for (int i = 0; i < CookingPotBlockEntity.COOK_TICKS; i++) CookingPotBlockEntity.serverTick(helper.getLevel(), abs, pot.getBlockState(), pot);
        check(pot.meals() == CookingPotBlockEntity.MEALS, "cooked");
        check(pot.takeMeal(new ItemStack(Items.STICK)).isEmpty(), "needs a bowl");
        check(pot.takeMeal(new ItemStack(Items.BOWL)).is(ModItems.HOMEMADE_MEAL.get()), "a meal");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "anyoneEatsAMeal")
    public static void anyoneEatsAMeal(GameTestHelper helper) {
        AbstractMonsterGirl g = girl(helper, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, g, new BlockPos(3, 2, 3));
        int before = g.getAffection(p);
        check(HomeLife.eatMeal(helper.getLevel(), g, p) && g.getAffection(p) == before + 5, "she eats");
        check(!HomeLife.eatMeal(helper.getLevel(), g, p), "full for now");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "mirrorChangesHerColour")
    public static void mirrorChangesHerColour(GameTestHelper helper) {
        AbstractMonsterGirl g = girl(helper, new BlockPos(1, 2, 1));
        check(!HomeLife.mirror(helper.getLevel(), helper.absolutePos(new BlockPos(2, 2, 2)), HarpyTestUtil.survivalPlayer(helper, new BlockPos(6, 2, 6))), "no friend");
        ServerPlayer p = friendOf(helper, g, new BlockPos(3, 2, 3));
        int v = g.getVariantIndex();
        check(HomeLife.mirror(helper.getLevel(), helper.absolutePos(new BlockPos(2, 2, 2)), p) && g.getVariantIndex() != v, "a new look");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "picnicBringsFriends")
    public static void picnicBringsFriends(GameTestHelper helper) {
        AbstractMonsterGirl g = girl(helper, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, g, new BlockPos(3, 2, 3));
        int before = g.getAffection(p);
        BlockPos at = helper.absolutePos(new BlockPos(2, 2, 2));
        check(HomeLife.picnic(helper.getLevel(), at, p) == 1 && g.getAffection(p) == before + 2 && p.hasEffect(MobEffects.SATURATION), "a picnic");
        check(HomeLife.picnic(helper.getLevel(), at, p) == 0, "not again so soon");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "sparringMakesHerStronger")
    public static void sparringMakesHerStronger(GameTestHelper helper) {
        AbstractMonsterGirl g = girl(helper, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, g, new BlockPos(3, 2, 3));
        double before = g.getAttributeValue(Attributes.ATTACK_DAMAGE);
        check(SparringPostBlockEntity.round(helper.getLevel(), helper.absolutePos(new BlockPos(2, 2, 2))) == 1, "a round");
        check(HomeLife.training(g) == 1 && g.getAttributeValue(Attributes.ATTACK_DAMAGE) > before, "stronger");
        check(SparringPostBlockEntity.round(helper.getLevel(), helper.absolutePos(new BlockPos(2, 2, 2))) == 0, "a rest between rounds");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "tubHealsAndSharesIt")
    public static void tubHealsAndSharesIt(GameTestHelper helper) {
        BlockPos at = new BlockPos(2, 2, 2);
        helper.setBlock(at, ModBlocks.SOAKING_TUB.get().defaultBlockState().setValue(SoakingTubBlock.FILLED, true));
        AbstractMonsterGirl g = girl(helper, new BlockPos(3, 2, 2));
        ServerPlayer p = friendOf(helper, g, at);
        check(SoakingTubBlockEntity.tick(helper.getLevel(), helper.absolutePos(at)) == 1, "shared");
        check(p.hasEffect(MobEffects.REGENERATION) && g.hasEffect(MobEffects.REGENERATION), "both soak");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "friendFetchesTheBall")
    public static void friendFetchesTheBall(GameTestHelper helper) {
        AbstractMonsterGirl g = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(1, 2, 1));
        ServerPlayer p = friendOf(helper, g, new BlockPos(2, 2, 2));
        BlockPos spot = helper.absolutePos(new BlockPos(6, 2, 6));
        ItemEntity ball = new ItemEntity(helper.getLevel(), spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, new ItemStack(ModItems.TOY_BALL.get()));
        ball.setThrower(p);
        ball.setPickUpDelay(32767);
        helper.getLevel().addFreshEntity(ball);
        helper.succeedWhen(() -> {
            check(p.getInventory().countItem(ModItems.TOY_BALL.get()) == 1, "brought back");
            HarpyTestUtil.remove(p);
        });
    }
}
