package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.Workshop;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Jobs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.enchanting.EnchantmentLevelSetEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Hands at the workbench (entity/Workshop, entity/girl/Jobs). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class WorkshopTests {

    private static int levelFor(GameTestHelper helper, BlockPos table) {
        EnchantmentLevelSetEvent e = new EnchantmentLevelSetEvent(helper.getLevel(), helper.absolutePos(table), 2, 0, new ItemStack(Items.IRON_SWORD), 3);
        NeoForge.EVENT_BUS.post(e);
        return e.getEnchantLevel();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "elfStandsInForBookshelves")
    public static void elfStandsInForBookshelves(GameTestHelper helper) {
        BlockPos table = new BlockPos(5, 2, 5);
        helper.setBlock(table, Blocks.ENCHANTING_TABLE);
        check(levelFor(helper, table) == 3, "no elf: as the table offered");
        AbstractMonsterGirl elf = spawn(helper, GirlSpecies.HIGH_ELF, new BlockPos(7, 2, 5));
        check(levelFor(helper, table) == 3, "a wild elf keeps her knowledge to herself");
        elf.setAffection(UUID.randomUUID(), 3);
        check(levelFor(helper, table) == 30, "a friendly one: as if fifteen bookshelves stood around");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "dwarfGivesAnvilLevelsBack")
    public static void dwarfGivesAnvilLevelsBack(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        AbstractMonsterGirl dwarf = spawn(helper, GirlSpecies.DWARF_ELF, new BlockPos(7, 2, 5));
        check(Workshop.anvilDwarf(p) == null, "not before she is a friend");
        dwarf.setAffection(p.getUUID(), Orders.AFFECTION);
        check(Workshop.anvilDwarf(p) == dwarf, "a dwarf friend at their side");
        check(Workshop.dwarfRefund(30) == 15 && Workshop.dwarfRefund(1) == 0, "half the levels back");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "dinnerBellCallsFriends")
    public static void dinnerBellCallsFriends(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        BlockPos bell = new BlockPos(3, 2, 3);
        helper.setBlock(bell, Blocks.BELL);
        AbstractMonsterGirl friend = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(12, 2, 12));
        friend.setAffection(p.getUUID(), 25);
        AbstractMonsterGirl sitter = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(12, 2, 3));
        sitter.setAffection(p.getUUID(), 25);
        sitter.setOrderedToSit(true);
        helper.runAfterDelay(20, () -> {                                    // (once they stand on the ground: a path needs that)
            check(Workshop.ringBell(helper.getLevel(), p, helper.absolutePos(bell)) == 1, "the free friend answers; the sitting one stays");
            HarpyTestUtil.remove(p);
            helper.succeedWhen(() -> check(friend.distanceToSqr(helper.absoluteVec(bell.getCenter())) < 4 * 4, "she came to the bell"));
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "valkyrieCatchesAFall")
    public static void valkyrieCatchesAFall(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 5));
        AbstractMonsterGirl valkyrie = spawn(helper, GirlSpecies.VALKYRIE_ELF, new BlockPos(9, 2, 9));
        valkyrie.setAffection(p.getUUID(), Orders.AFFECTION);
        check(!p.causeFallDamage(12.0F, 1.0F, helper.getLevel().damageSources().fall()), "caught before the ground");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "yukiOnnaFreezesAPath")
    public static void yukiOnnaFreezesAPath(GameTestHelper helper) {
        for (int x = 3; x <= 9; x++) for (int z = 3; z <= 9; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.WATER);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(6, 2, 6));
        p.setOnGround(true);
        check(Workshop.freezePath(helper.getLevel(), p) > 0, "the water under them freezes");
        check(helper.getBlockState(new BlockPos(6, 1, 6)).is(Blocks.FROSTED_ICE), "frosted ice, which melts again");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "lichKeepsAStandBrewing")
    public static void lichKeepsAStandBrewing(GameTestHelper helper) {
        BlockPos at = new BlockPos(6, 2, 6);
        helper.setBlock(at, Blocks.BREWING_STAND);
        BrewingStandBlockEntity stand = (BrewingStandBlockEntity) helper.getBlockEntity(at);
        ItemStack water = new ItemStack(Items.POTION);
        water.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
        stand.setItem(0, water);
        stand.setItem(3, new ItemStack(Items.NETHER_WART));
        AbstractMonsterGirl lich = spawn(helper, GirlSpecies.LICH_UNDEAD, new BlockPos(4, 2, 6));
        check(Jobs.standToTend(helper.getLevel(), lich.blockPosition(), Jobs.REACH) != null, "a stand with work in it");
        check(Jobs.brew(helper.getLevel(), lich, helper.absolutePos(at)) && stand.fuel > 0, "fuelled without blaze powder");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "dragonLightsACampfire")
    public static void dragonLightsACampfire(GameTestHelper helper) {
        BlockPos at = new BlockPos(6, 2, 6);
        helper.setBlock(at, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
        AbstractMonsterGirl dragon = spawn(helper, GirlSpecies.WYRM_DRAGON, new BlockPos(3, 2, 6));
        check(Jobs.fireToTend(helper.getLevel(), dragon.blockPosition(), Jobs.REACH) != null, "a cold campfire");
        Jobs.breatheOnFire(helper.getLevel(), dragon, helper.absolutePos(at));
        check(helper.getBlockState(at).getValue(CampfireBlock.LIT), "it catches");
        helper.succeed();
    }

    private static int count(net.minecraft.world.Container c, net.minecraft.world.item.Item item) {
        int n = 0;
        for (int i = 0; i < c.getContainerSize(); i++) if (c.getItem(i).is(item)) n += c.getItem(i).getCount();
        return n;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 1600, batch = "arachneWeavesStringAtALoom")
    public static void arachneWeavesStringAtALoom(GameTestHelper helper) {
        helper.setBlock(new BlockPos(9, 2, 7), Blocks.LOOM);
        helper.setBlock(new BlockPos(6, 2, 7), Blocks.CHEST);
        var chest = (net.minecraft.world.level.block.entity.ChestBlockEntity) helper.getBlockEntity(new BlockPos(6, 2, 7));
        chest.setItem(0, new ItemStack(Items.STRING, 5));
        AbstractMonsterGirl spider = spawn(helper, GirlSpecies.PEACOCK_SPIDER_ARACHNE, new BlockPos(4, 2, 9));
        spider.setAffection(UUID.randomUUID(), 5);
        helper.succeedWhen(() -> {
            check(count(chest, Items.WHITE_WOOL) == 1, "a block of wool woven");
            check(count(chest, Items.STRING) == 1, "from four of the string");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 1600, batch = "woodElfFletchesArrows")
    public static void woodElfFletchesArrows(GameTestHelper helper) {
        helper.setBlock(new BlockPos(9, 2, 7), Blocks.FLETCHING_TABLE);
        helper.setBlock(new BlockPos(6, 2, 7), Blocks.CHEST);
        var chest = (net.minecraft.world.level.block.entity.ChestBlockEntity) helper.getBlockEntity(new BlockPos(6, 2, 7));
        chest.setItem(0, new ItemStack(Items.FLINT));
        chest.setItem(1, new ItemStack(Items.STICK));
        chest.setItem(2, new ItemStack(Items.FEATHER));
        AbstractMonsterGirl elf = spawn(helper, GirlSpecies.WOOD_ELF, new BlockPos(4, 2, 9));
        elf.setAffection(UUID.randomUUID(), 5);
        helper.succeedWhen(() -> check(count(chest, Items.ARROW) == 4 && count(chest, Items.FLINT) == 0, "four arrows from a flint, a stick and a feather"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "seaLamiaDivesWithHerRider")
    public static void seaLamiaDivesWithHerRider(GameTestHelper helper) {
        for (int x = 2; x <= 10; x++) for (int z = 2; z <= 10; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.WATER);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 1, 5));
        com.linkyoshi90.harmontide.entity.girl.Lamia sea = spawn(helper, GirlSpecies.SEA_SNAKE_LAMIA, new BlockPos(6, 1, 6));
        check(sea.swimsWithRider() && sea.canBeRiddenUnderFluidType(net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value(), p), "a rider stays in her coils under water");
        check(p.startRiding(sea, true), "in her coils");
        p.setAirSupply(5);
        helper.succeedWhen(() -> {
            check(p.getAirSupply() == p.getMaxAirSupply(), "never short of air with her");
            HarpyTestUtil.remove(p);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "magmaSlimeFillsACauldron")
    public static void magmaSlimeFillsACauldron(GameTestHelper helper) {
        BlockPos at = new BlockPos(9, 2, 7);
        helper.setBlock(at, Blocks.CAULDRON);
        AbstractMonsterGirl slime = spawn(helper, GirlSpecies.MAGMA_SLIME, new BlockPos(4, 2, 7));
        slime.setAffection(UUID.randomUUID(), 5);
        helper.succeedWhen(() -> check(helper.getBlockState(at).is(Blocks.LAVA_CAULDRON), "a cauldron of lava"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 1600, batch = "mooskuhFillsBowlsWithStew")
    public static void mooskuhFillsBowlsWithStew(GameTestHelper helper) {
        helper.setBlock(new BlockPos(8, 2, 7), Blocks.CHEST);
        var chest = (net.minecraft.world.level.block.entity.ChestBlockEntity) helper.getBlockEntity(new BlockPos(8, 2, 7));
        chest.setItem(0, new ItemStack(Items.BOWL, 2));
        AbstractMonsterGirl cow = spawn(helper, GirlSpecies.MOOSKUH_MINOTAUR, new BlockPos(5, 2, 7));
        cow.setAffection(UUID.randomUUID(), 5);
        helper.succeedWhen(() -> check(count(chest, Items.MUSHROOM_STEW) >= 1, "a bowl of stew"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "salamanderCrustsTheLava")
    public static void salamanderCrustsTheLava(GameTestHelper helper) {
        for (int x = 3; x <= 9; x++) for (int z = 3; z <= 9; z++) helper.setBlock(new BlockPos(x, 1, z), Blocks.LAVA);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(6, 2, 6));
        p.setOnGround(true);
        check(Workshop.crustPath(helper.getLevel(), p) > 0, "the lava under them cools");
        check(helper.getBlockState(new BlockPos(6, 1, 6)).is(com.linkyoshi90.harmontide.registry.ModBlocks.LAVA_CRUST.get()), "a crust to stand on");
        HarpyTestUtil.remove(p);
        helper.succeedWhen(() -> check(helper.getBlockState(new BlockPos(6, 1, 6)).is(Blocks.LAVA), "and it melts back into the lava it was"));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "plantGirlGrowsAVineLadder")
    public static void plantGirlGrowsAVineLadder(GameTestHelper helper) {
        for (int y = 2; y <= 5; y++) helper.setBlock(new BlockPos(8, y, 6), Blocks.STONE);
        int tall = Workshop.growVines(helper.getLevel(), helper.absolutePos(new BlockPos(7, 2, 6)), net.minecraft.core.Direction.EAST);
        check(tall == 4, "vines as tall as the wall: " + tall);
        check(helper.getBlockState(new BlockPos(7, 3, 6)).is(Blocks.VINE), "real vines");
        helper.succeed();
    }
}
