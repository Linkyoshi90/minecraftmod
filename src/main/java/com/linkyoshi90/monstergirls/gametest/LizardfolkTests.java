package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Lizardfolk;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The reptile family (lizardfolk_field_guide.html). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class LizardfolkTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "lizardfolkEatEggsAndWarmUp")
    public static void lizardfolkEatEggsAndWarmUp(GameTestHelper helper) {
        Lizardfolk iguana = spawn(helper, GirlSpecies.IGUANA_LIZARDFOLK, new BlockPos(7, 2, 7));
        check(iguana.isFood(new ItemStack(Items.EGG)) && iguana.isFood(new ItemStack(ModItems.GULL_EGG.get())), "eggs, the harpies' too");
        check(iguana.getArmorValue() > 0, "scales are armour");
        helper.setBlock(new BlockPos(9, 2, 7), Blocks.CAMPFIRE);
        check(iguana.isWarm(), "a campfire close by warms her");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "chameleonVanishesWhenStill")
    public static void chameleonVanishesWhenStill(GameTestHelper helper) {
        Lizardfolk cham = spawn(helper, GirlSpecies.CHAMELEON_LIZARDFOLK, new BlockPos(7, 2, 7));
        cham.setOrderedToSit(true);
        helper.succeedWhen(() -> check(cham.isInvisible(), "still, she vanishes"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "turtleHidesAndSalamanderBurns")
    public static void turtleHidesAndSalamanderBurns(GameTestHelper helper) {
        Lizardfolk turtle = spawn(helper, GirlSpecies.TURTLE_LIZARDFOLK, new BlockPos(4, 2, 4));
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(6, 2, 4));
        turtle.hurt(helper.getLevel().damageSources().mobAttack(z), 1.0F);
        check(turtle.isRetracted(), "into her shell");
        Lizardfolk sal = spawn(helper, GirlSpecies.SALAMANDER_LIZARDFOLK, new BlockPos(10, 2, 10));
        Zombie z2 = helper.spawn(EntityType.ZOMBIE, new BlockPos(11, 2, 10));
        sal.doHurtTarget(z2);
        check(z2.isOnFire() && sal.fireImmune(), "the salamander burns what she hits and fears no fire");
        Lizardfolk croc = spawn(helper, GirlSpecies.CROC_LIZARDFOLK, new BlockPos(10, 2, 4));
        check(croc.isCalm() == false, "a wild crocodile is not calm");
        helper.succeed();
    }
}
