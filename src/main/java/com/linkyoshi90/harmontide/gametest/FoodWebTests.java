package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.FoodWeb;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** The food web (entity/FoodWeb). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class FoodWebTests {
    private static AbstractMonsterGirl still(GameTestHelper helper, GirlSpecies s, BlockPos at) {
        AbstractMonsterGirl g = spawn(helper, s, at);
        g.setNoAi(true);
        return g;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "rolesFromDiet")
    public static void rolesFromDiet(GameTestHelper helper) {
        AbstractMonsterGirl wolf = still(helper, GirlSpecies.WOLF_CANID, new BlockPos(3, 2, 3));
        AbstractMonsterGirl sheep = still(helper, GirlSpecies.SHEEP_MINOTAUR, new BlockPos(6, 2, 3));
        check(FoodWeb.role(wolf) == FoodWeb.Role.HUNTER, "a wolf hunts");
        check(FoodWeb.role(sheep) == FoodWeb.Role.GRAZER, "a sheep grazes: " + FoodWeb.role(sheep));
        check(!FoodWeb.hungry(wolf), "new to the world, she has eaten lately");
        FoodWeb.starve(wolf);
        check(FoodWeb.forages(wolf) && FoodWeb.hungry(wolf), "wild and hungry, she feeds herself");
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(12, 2, 12));
        wolf.addAffection(p, 10);
        check(!FoodWeb.forages(wolf), "befriended, she is fed by her friend");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "huntersSpareTheFarm")
    public static void huntersSpareTheFarm(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl wolf = still(helper, GirlSpecies.WOLF_CANID, new BlockPos(3, 2, 3));
        Sheep wildSheep = helper.spawn(EntityType.SHEEP, new BlockPos(10, 2, 3));
        Sheep named = helper.spawn(EntityType.SHEEP, new BlockPos(10, 2, 10));
        named.setCustomName(Component.literal("Dolly"));
        Cow penned = helper.spawn(EntityType.COW, new BlockPos(3, 2, 11));
        helper.setBlock(new BlockPos(4, 2, 11), Blocks.OAK_FENCE);
        for (var a : new net.minecraft.world.entity.Mob[]{wildSheep, named, penned}) a.setNoAi(true);
        check(FoodWeb.prey(wolf, wildSheep) && !FoodWeb.kept(level, wildSheep), "a wild sheep is fair game");
        check(FoodWeb.kept(level, named) && FoodWeb.kept(level, penned), "named or penned, it's someone's");
        check(FoodWeb.findPrey(level, wolf) == wildSheep, "she picks the wild one");
        Chicken chicken = helper.spawn(EntityType.CHICKEN, new BlockPos(4, 2, 4));
        FoodWeb.starve(wolf);
        boolean down = false;
        for (int i = 0; i < 5 && !down; i++) {
            chicken.invulnerableTime = 0;
            down = FoodWeb.strike(level, wolf, chicken);
        }
        check(down && !FoodWeb.hungry(wolf), "brought down and eaten");
        var left = level.getEntitiesOfClass(ItemEntity.class, wolf.getBoundingBox().inflate(4));
        check(left.stream().anyMatch(i -> i.getItem().is(Items.BONE)) && left.stream().noneMatch(i -> i.getItem().is(Items.CHICKEN)), "the meat eaten, the bones left");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "grazersAndScavengers")
    public static void grazersAndScavengers(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl sheep = still(helper, GirlSpecies.SHEEP_MINOTAUR, new BlockPos(3, 2, 3));
        helper.setBlock(new BlockPos(4, 2, 3), Blocks.SHORT_GRASS);
        BlockPos grass = helper.absolutePos(new BlockPos(4, 2, 3));
        check(FoodWeb.graze(level, sheep, grass) && level.getBlockState(grass).isAir() && !FoodWeb.hungry(sheep), "she crops the grass");
        AbstractMonsterGirl wolf = still(helper, GirlSpecies.WOLF_CANID, new BlockPos(9, 2, 9));
        helper.spawnItem(Items.ROTTEN_FLESH, new BlockPos(9, 2, 10));
        check(FoodWeb.scavenge(level, wolf), "leftovers eaten");
        FoodWeb.died(level, helper.absolutePos(new BlockPos(12, 2, 12)));
        check(FoodWeb.carrion(level, helper.absolutePos(new BlockPos(9, 2, 9)), 20) != null, "the place something died is known");
        helper.succeed();
    }
}
