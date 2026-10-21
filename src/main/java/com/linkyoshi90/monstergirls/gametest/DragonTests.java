package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.girl.Dragon;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.interact;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** The dragon family (dragon_field_guide.html). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class DragonTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "dragonHoardsGold")
    public static void dragonHoardsGold(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 7));
        helper.setBlock(new BlockPos(9, 2, 9), Blocks.CHEST);
        Dragon kannae = spawn(helper, GirlSpecies.KANNAE_DRAGON, new BlockPos(7, 2, 7));
        helper.runAfterDelay(5, () -> {
            interact(kannae, player, new ItemStack(Items.GOLD_INGOT));
            check(kannae.getAffection(player) == 2, "gold for her hoard is worth 2");
            check(helper.getBlockEntity(new BlockPos(9, 2, 9)) instanceof ChestBlockEntity c && c.countItem(Items.GOLD_INGOT) == 1,
                    "it goes into the chest by her lair");
            HarpyTestUtil.remove(player);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "wyrmBreathesFire")
    public static void wyrmBreathesFire(GameTestHelper helper) {
        Dragon wyrm = spawn(helper, GirlSpecies.WYRM_DRAGON, new BlockPos(3, 2, 3));
        Zombie z = helper.spawn(EntityType.ZOMBIE, new BlockPos(10, 2, 10));
        wyrm.breatheFire(helper.getLevel(), z);
        check(!helper.getLevel().getEntitiesOfClass(SmallFireball.class, new AABB(helper.absolutePos(new BlockPos(3, 2, 3))).inflate(4)).isEmpty(), "fire");
        check(wyrm.canFly(), "the wyrm flies");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "gryphonCarriesAFriend")
    public static void gryphonCarriesAFriend(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 7));
        Dragon gryphon = spawn(helper, GirlSpecies.GRYPHON_DRAGON, new BlockPos(7, 2, 7));
        interact(gryphon, player, ItemStack.EMPTY);
        check(player.getVehicle() != gryphon, "not a stranger");
        gryphon.setAffection(player.getUUID(), Dragon.GRYPHON_RIDE);
        interact(gryphon, player, ItemStack.EMPTY);
        check(player.getVehicle() == gryphon && gryphon.getControllingPassenger() == player, "a friend rides her");
        player.stopRiding();
        Dragon orochi = spawn(helper, GirlSpecies.OROCHI_DRAGON, new BlockPos(11, 2, 11));
        interact(orochi, player, new ItemStack(Items.HONEY_BOTTLE));
        check(orochi.getAffection(player) == 3, "her sake: +3");
        HarpyTestUtil.remove(player);
        helper.succeed();
    }
}
