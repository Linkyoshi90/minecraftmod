package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.Flora;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.interact;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The plant family (flora_field_guide.html). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class FloraTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "cactusPricksAndAlrauneDustsPollen")
    public static void cactusPricksAndAlrauneDustsPollen(GameTestHelper helper) {
        Flora cactus = spawn(helper, GirlSpecies.CACTUS_FLORA, new BlockPos(4, 2, 4));
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(5, 2, 4));
        float hp = z.getHealth();
        cactus.hurt(helper.getLevel().damageSources().mobAttack(z), 1.0F);
        check(z.getHealth() < hp, "her spines hurt back");
        Flora alraune = spawn(helper, GirlSpecies.ALRAUNE_FLORA, new BlockPos(10, 2, 10));
        Zombie z2 = helper.spawn(EntityType.ZOMBIE, new BlockPos(11, 2, 10));
        alraune.hurt(helper.getLevel().damageSources().mobAttack(z2), 1.0F);
        check(z2.hasEffect(MobEffects.CONFUSION), "pollen in the attacker's face");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "mandragoraScreamsWhenPulledUp")
    public static void mandragoraScreamsWhenPulledUp(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 7));
        Flora mandragora = spawn(helper, GirlSpecies.MANDRAGORA_FLORA, new BlockPos(7, 2, 7));
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(9, 2, 9));
        mandragora.setHidden(true);
        interact(mandragora, player, ItemStack.EMPTY);
        check(!mandragora.isHidden(), "pulled out of the ground");
        check(z.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && !player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "her scream stuns all but the puller");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "rootedFloraGrowsCrops")
    public static void rootedFloraGrowsCrops(GameTestHelper helper) {
        for (int x = 3; x <= 11; x++) for (int z = 3; z <= 11; z++) {
            helper.setBlock(new BlockPos(x, 1, z), Blocks.FARMLAND);
            if (x != 7 || z != 7) helper.setBlock(new BlockPos(x, 2, z), Blocks.WHEAT);
        }
        helper.setBlock(new BlockPos(7, 1, 7), Blocks.DIRT);
        Flora ent = spawn(helper, GirlSpecies.ENT_FLORA, new BlockPos(7, 2, 7));
        ent.setOrderedToSit(true);
        helper.succeedWhen(() -> {
            boolean grown = false;
            for (int x = 3; x <= 11; x++) for (int z = 3; z <= 11; z++) {
                var s = helper.getBlockState(new BlockPos(x, 2, z));
                if (s.getBlock() instanceof CropBlock c && c.getAge(s) > 0) grown = true;
            }
            check(grown, "crops around a rooted plant girl grow");
        });
    }
}
