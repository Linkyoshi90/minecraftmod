package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Undead;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.interact;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The restless family (undead_field_guide.html). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class UndeadTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "zombieGirlOnlyWantsRottenFlesh")
    public static void zombieGirlOnlyWantsRottenFlesh(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 7));
        Undead zombie = spawn(helper, GirlSpecies.ZOMBIE_UNDEAD, new BlockPos(7, 2, 7));
        interact(zombie, player, new ItemStack(Items.BEEF));
        check(!zombie.isCalm(), "beef does not calm a zombie");
        interact(zombie, player, new ItemStack(Items.ROTTEN_FLESH));
        check(zombie.isCalm(), "rotten flesh does");
        interact(zombie, player, new ItemStack(Items.ROTTEN_FLESH));
        check(zombie.getAffection(player) > 0, "and builds friendship");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "vampireDrinksButNeverKills")
    public static void vampireDrinksButNeverKills(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 7));
        Undead vampire = spawn(helper, GirlSpecies.VAMPIRE_UNDEAD, new BlockPos(7, 2, 7));
        player.setHealth(3.0F);
        Undead.drinkFrom(player, vampire);
        check(player.getHealth() >= 2.0F && player.isAlive(), "she stops at one heart: " + player.getHealth());
        check(player.hasEffect(MobEffects.WEAKNESS), "you feel weak");
        check(vampire.getAffection(player) == 3, "and she likes you for it");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "dollTugsYukiOnnaFreezes")
    public static void dollTugsYukiOnnaFreezes(GameTestHelper helper) {
        Undead doll = spawn(helper, GirlSpecies.LIVING_DOLL_UNDEAD, new BlockPos(5, 2, 5));
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(6, 2, 5));
        float hp = z.getHealth();
        doll.doHurtTarget(z);
        check(z.getHealth() == hp, "the doll only tugs at you");
        Undead yuki = spawn(helper, GirlSpecies.YUKI_ONNA_UNDEAD, new BlockPos(9, 2, 9));
        Zombie z2 = helper.spawn(EntityType.ZOMBIE, new BlockPos(10, 2, 9));
        yuki.doHurtTarget(z2);
        check(z2.getTicksFrozen() > 0, "her touch freezes");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "phantomGirlFliesAndWispBurns")
    public static void phantomGirlFliesAndWispBurns(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Undead phantom = spawn(helper, GirlSpecies.PHANTOM_UNDEAD, new BlockPos(7, 4, 7));
        check(phantom.getNavigation() instanceof FlyingPathNavigation, "she flies");
        check(!phantom.isHostileTo(player), "she leaves the well-rested alone");
        Undead wisp = spawn(helper, GirlSpecies.WILL_O_WISP_UNDEAD, new BlockPos(10, 3, 10));
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(11, 2, 10));
        wisp.hurt(helper.getLevel().damageSources().mobAttack(z), 1.0F);
        check(z.isOnFire() && wisp.fireImmune(), "touch the flame, catch fire");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "lightningHealsTheMonster")
    public static void lightningHealsTheMonster(GameTestHelper helper) {
        Undead monster = spawn(helper, GirlSpecies.FRANKENSTEIN_UNDEAD, new BlockPos(7, 2, 7));
        monster.setHealth(10.0F);
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(helper.getLevel());
        monster.thunderHit(helper.getLevel(), bolt);
        check(monster.getHealth() == monster.getMaxHealth() && monster.hasEffect(MobEffects.DAMAGE_BOOST), "lightning wakes her up stronger");
        helper.succeed();
    }
}
