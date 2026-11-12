package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.girl.Elf;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.interact;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** The elven family (elf_field_guide.html). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class ElfTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "elvesTradeWithFriends")
    public static void elvesTradeWithFriends(GameTestHelper helper) {
        ServerPlayer player = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 7));
        Elf elf = spawn(helper, GirlSpecies.WOOD_ELF, new BlockPos(7, 2, 7));
        interact(elf, player, ItemStack.EMPTY);
        check(elf.getTradingPlayer() == null, "strangers get no trades");
        elf.setAffection(player.getUUID(), Elf.TRADE_AFFECTION);
        interact(elf, player, ItemStack.EMPTY);
        check(elf.getTradingPlayer() == player, "a friend gets the trading screen");
        check(elf.getOffers().size() >= 4, "she has goods to sell");
        elf.notifyTrade(elf.getOffers().get(0));
        check(elf.getAffection(player) == Elf.TRADE_AFFECTION + 1, "every trade is worth +1");
        for (GirlSpecies s : new GirlSpecies[]{GirlSpecies.NIGHT_ELF, GirlSpecies.HIGH_ELF, GirlSpecies.BLOOD_ELF, GirlSpecies.DESERT_ELF}) {
            Elf other = spawn(helper, s, new BlockPos(10, 2, 10));
            check(!other.makeOffers().isEmpty(), s + " sells something");
            other.discard();
        }
        HarpyTestUtil.remove(player);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "highElfBuildsAPavilion")
    public static void highElfBuildsAPavilion(GameTestHelper helper) {
        Elf elf = spawn(helper, GirlSpecies.HIGH_ELF, new BlockPos(7, 2, 7));
        BlockPos origin = helper.absolutePos(new BlockPos(7, 2, 7));
        elf.setSettlement(origin);
        helper.succeedWhen(() -> {
            check(elf.getBuilt() >= 4, "she should be building: " + elf.getBuilt());
            check(helper.getLevel().getBlockState(origin.offset(-2, -1, -2)).is(Blocks.QUARTZ_BRICKS), "the pavilion floor");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "woodElfShootsAndBloodElfBurns")
    public static void woodElfShootsAndBloodElfBurns(GameTestHelper helper) {
        Elf archer = spawn(helper, GirlSpecies.WOOD_ELF, new BlockPos(3, 2, 3));
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(10, 2, 10));
        archer.performRangedAttack(zombie, 1.0F);
        check(!helper.getLevel().getEntitiesOfClass(Arrow.class, new AABB(helper.absolutePos(new BlockPos(3, 2, 3))).inflate(4)).isEmpty(), "an arrow flies");
        Elf blood = spawn(helper, GirlSpecies.BLOOD_ELF, new BlockPos(9, 2, 10));
        check(blood.fireImmune(), "fire cannot hurt a blood elf");
        blood.doHurtTarget(zombie);
        check(zombie.isOnFire(), "her blade sets enemies alight");
        helper.succeed();
    }
}
