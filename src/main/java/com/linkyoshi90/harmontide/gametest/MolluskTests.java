package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Mollusk;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.interact;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** The shell and tentacle family (mollusk_field_guide.html). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class MolluskTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "snailHidesInHerShell")
    public static void snailHidesInHerShell(GameTestHelper helper) {
        Mollusk snail = spawn(helper, GirlSpecies.SNAIL_MOLLUSK, new BlockPos(7, 2, 7));
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(3, 2, 3));
        float before = snail.getHealth();
        snail.hurt(helper.getLevel().damageSources().mobAttack(zombie), 2.0F);
        check(snail.isRetracted(), "a fright sends her into her shell");
        snail.invulnerableTime = 0;
        float mid = snail.getHealth();
        snail.hurt(helper.getLevel().damageSources().mobAttack(zombie), 10.0F);
        check(mid - snail.getHealth() <= 2.01F, "her shell takes most of a hit: " + (mid - snail.getHealth()));
        check(snail.getSpeed() == 0.0F, "and she cannot move inside it");
        check(before > mid, "the first hit counts");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "snailEatsVegetables")
    public static void snailEatsVegetables(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 7));
        Mollusk snail = spawn(helper, GirlSpecies.SNAIL_MOLLUSK, new BlockPos(7, 2, 7));
        check(snail.isFood(new ItemStack(Items.CARROT)) && !snail.isFood(new ItemStack(Items.COD)), "vegetables, not fish");
        interact(snail, player, new ItemStack(Items.CARROT));
        check(snail.getAffection(player) == 2, "a carrot wins her over: " + snail.getAffection(player));
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "octopusInksHerAttacker")
    public static void octopusInksHerAttacker(GameTestHelper helper) {
        Mollusk octo = spawn(helper, GirlSpecies.OCTOPUS_MOLLUSK, new BlockPos(7, 2, 7));
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(5, 2, 5));
        octo.hurt(helper.getLevel().damageSources().mobAttack(zombie), 1.0F);
        check(zombie.hasEffect(MobEffects.BLINDNESS), "ink in the attacker's eyes");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "krakenGripsAndIkaReachesFar")
    public static void krakenGripsAndIkaReachesFar(GameTestHelper helper) {
        Mollusk kraken = spawn(helper, GirlSpecies.KRAKEN_MOLLUSK, new BlockPos(5, 2, 5));
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(6, 2, 5));
        kraken.doHurtTarget(zombie);
        check(zombie.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "held fast in her grip");
        Mollusk ika = spawn(helper, GirlSpecies.IKA_MOLLUSK, new BlockPos(10, 2, 10));
        Zombie far = helper.spawn(EntityType.ZOMBIE, new BlockPos(13, 2, 10));
        check(ika.isWithinMeleeAttackRange(far), "Ika's hair reaches three blocks");
        helper.succeed();
    }
}
