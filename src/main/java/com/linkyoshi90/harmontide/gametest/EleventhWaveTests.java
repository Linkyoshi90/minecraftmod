package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.CassowrathHarpy;
import com.linkyoshi90.harmontide.entity.Mailtoad;
import com.linkyoshi90.harmontide.entity.Orders;
import com.linkyoshi90.harmontide.entity.RookmournHarpy;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.Armoured;
import com.linkyoshi90.harmontide.entity.girl.Centaur;
import com.linkyoshi90.harmontide.entity.girl.Construct;
import com.linkyoshi90.harmontide.entity.girl.Elf;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Lizardfolk;
import com.linkyoshi90.harmontide.entity.girl.Marsupial;
import com.linkyoshi90.harmontide.entity.girl.Merfolk;
import com.linkyoshi90.harmontide.entity.girl.Nekomata;
import com.linkyoshi90.harmontide.entity.girl.Rodent;
import com.linkyoshi90.harmontide.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.interact;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/**
 * The eleventh Harmontide wave (HARMONTIDE_CONCEPTS.md): Harmontide's helpers. Anvil tortoise, mailtoad, gliderune,
 * slothren, coatimano, waterlil, rookmourn, cassowrath, talosling, mantarrow, sphinx, brownie.
 */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class EleventhWaveTests {

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "anvilTortoiseMendsForMaterial")
    public static void anvilTortoiseMendsForMaterial(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        Lizardfolk t = spawn(helper, GirlSpecies.ANVIL_TORTOISE_LIZARDFOLK, new BlockPos(3, 2, 3));
        t.setNoAi(true);
        check(!t.isPushable(), "too heavy to push");
        p.getInventory().setItem(8, new ItemStack(Items.IRON_INGOT, 2));
        ItemStack pick = new ItemStack(Items.IRON_PICKAXE);
        pick.setDamageValue(200);
        interact(t, p, pick);
        check(pick.getDamageValue() < 200, "she mends the pickaxe: " + pick.getDamageValue());
        check(p.getInventory().getItem(8).getCount() == 1, "for one iron ingot");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "mailtoadDeliversByName")
    public static void mailtoadDeliversByName(GameTestHelper helper) {
        ServerPlayer sender = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        Mailtoad toad = helper.spawn(ModEntities.MAILTOAD.get(), new BlockPos(2, 2, 2));
        toad.setAffection(sender.getUUID(), Orders.AFFECTION);
        sender.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIRT, 3));
        toad.interact(sender, InteractionHand.MAIN_HAND);
        check(toad.getParcel().is(Items.DIRT) && sender.getMainHandItem().isEmpty(), "she takes the parcel");
        ServerPlayer to = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 7), "mail-test");
        ItemStack paper = new ItemStack(Items.PAPER);
        paper.set(DataComponents.CUSTOM_NAME, Component.literal("mail-test"));
        sender.setItemInHand(InteractionHand.MAIN_HAND, paper);
        toad.interact(sender, InteractionHand.MAIN_HAND);
        check(toad.getAddressee() != null, "a named paper addresses it");
        HarpyTestUtil.remove(sender);
        helper.succeedWhen(() -> {
            check(to.getInventory().countItem(Items.DIRT) == 3, "the parcel arrives");
            check(toad.getParcel().isEmpty(), "and she is empty-handed");
            HarpyTestUtil.remove(to);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "gliderunePerchesOnAShoulder")
    public static void gliderunePerchesOnAShoulder(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Marsupial g = spawn(helper, GirlSpecies.GLIDERUNE_MARSUPIAL, new BlockPos(4, 2, 3));
        check(!g.causeFallDamage(20, 1, helper.getLevel().damageSources().fall()), "she glides, so falls never hurt her");
        g.addAffection(p, Orders.AFFECTION);
        p.setShiftKeyDown(true);
        interact(g, p, ItemStack.EMPTY);
        p.setShiftKeyDown(false);
        check(g.getVehicle() == p, "up onto her friend's shoulder");
        helper.succeedWhen(() -> {
            check(p.hasEffect(MobEffects.SLOW_FALLING), "her friend floats down");
            HarpyTestUtil.remove(p);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "slothrenHangsFromABranch")
    public static void slothrenHangsFromABranch(GameTestHelper helper) {
        helper.setBlock(new BlockPos(5, 6, 5), Blocks.OAK_LEAVES);
        Armoured s = spawn(helper, GirlSpecies.SLOTHREN_ARMOURED, new BlockPos(5, 2, 5));
        BlockPos branch = s.findBranch();
        check(branch != null && branch.equals(helper.absolutePos(new BlockPos(5, 6, 5))), "the leaves above are her branch: " + branch);
        s.hangFrom(branch);
        check(s.isHanging() && s.isNoGravity(), "she hangs there");
        check(s.getY() + s.getBbHeight() <= branch.getY() + 0.01, "under the branch, not in it");
        s.letGo();
        check(!s.isHanging() && !s.isNoGravity(), "and lets go");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 1200, batch = "coatimanoBrushesOutTheFind")
    public static void coatimanoBrushesOutTheFind(GameTestHelper helper) {
        BlockPos sand = new BlockPos(5, 1, 5);
        helper.setBlock(sand.below(), Blocks.STONE);                     // it falls like sand
        helper.setBlock(sand, Blocks.SUSPICIOUS_SAND);
        if (helper.getLevel().getBlockEntity(helper.absolutePos(sand)) instanceof BrushableBlockEntity be) {
            be.setLootTable(BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY, 42L);
        }
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        Rodent c = spawn(helper, GirlSpecies.COATIMANO_RODENT, new BlockPos(5, 2, 6));
        c.addAffection(friend, Orders.AFFECTION);
        check(helper.absolutePos(sand).equals(c.findDig(helper.getLevel())), "she finds the suspicious sand");
        int before = friend.getInventory().items.stream().mapToInt(ItemStack::getCount).sum();
        helper.succeedWhen(() -> {
            check(helper.getBlockState(sand).is(Blocks.SAND), "she brushes it out");
            check(friend.getInventory().items.stream().mapToInt(ItemStack::getCount).sum() > before, "and brings her friend the find");
            HarpyTestUtil.remove(friend);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "waterlilGrowsLilyPads")
    public static void waterlilGrowsLilyPads(GameTestHelper helper) {
        helper.setBlock(new BlockPos(5, 1, 5), Blocks.WATER);
        Nekomata cat = spawn(helper, GirlSpecies.WATERLIL_NEKOMATA, new BlockPos(5, 2, 5));
        cat.setNoAi(true);
        check(cat.growPads(helper.getLevel(), cat), "a lily pad grows under her");
        check(helper.getBlockState(new BlockPos(5, 2, 5)).is(Blocks.LILY_PAD), "on the water");
        cat.discard();
        check(helper.getBlockState(new BlockPos(5, 2, 5)).isAir(), "and fades when she goes");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "rookmournCleanses")
    public static void rookmournCleanses(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        p.addEffect(new MobEffectInstance(MobEffects.POISON, 400));
        p.addEffect(new MobEffectInstance(MobEffects.HUNGER, 400));
        RookmournHarpy rook = helper.spawn(ModEntities.ROOKMOURN_HARPY.get(), new BlockPos(5, 2, 5));
        rook.setNoAi(true);
        check(rook.cleanse(helper.getLevel()) == 1, "she treats the player beside her");
        check(!p.hasEffect(MobEffects.POISON) && !p.hasEffect(MobEffects.HUNGER), "no more poison or hunger");
        check(!rook.cure(helper.getLevel(), p), "no zombie villager, nothing to cure");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "cassowrathKicksHarder")
    public static void cassowrathKicksHarder(GameTestHelper helper) {
        CassowrathHarpy c = helper.spawn(ModEntities.CASSOWRATH_HARPY.get(), new BlockPos(5, 2, 5));
        check(c.getMaxHealth() == 40.0F, "tougher than an ostrich: " + c.getMaxHealth());
        check(c.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) == 9.0, "and her kick hurts more");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "taloslingRunsDownAndWinds")
    public static void taloslingRunsDownAndWinds(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(1, 2, 1));
        Construct t = spawn(helper, GirlSpecies.TALOSLING_CONSTRUCT, new BlockPos(5, 2, 5));
        check(t.getWound() == Construct.MAX_WIND, "she starts fully wound");
        CompoundTag tag = new CompoundTag();
        t.addAdditionalSaveData(tag);
        tag.putInt("Wound", 1);
        t.readAdditionalSaveData(tag);
        helper.runAfterDelay(3, () -> {
            check(t.getAction() == AbstractMonsterGirl.Action.RUNDOWN, "run down, she stops: " + t.getAction());
            interact(t, p, new ItemStack(Items.REDSTONE, 2));
            check(t.getWound() == Construct.WIND_PER_DUST, "a dust winds her: " + t.getWound());
            check(t.getAction() != AbstractMonsterGirl.Action.RUNDOWN, "and she moves again");
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "mantarrowCarriesAFriend")
    public static void mantarrowCarriesAFriend(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Merfolk m = spawn(helper, GirlSpecies.MANTARROW_MERMAID, new BlockPos(4, 2, 4));
        m.setNoAi(true);
        interact(m, p, ItemStack.EMPTY);
        check(p.getVehicle() == null, "strangers do not ride her");
        m.addAffection(p, Orders.AFFECTION);
        interact(m, p, ItemStack.EMPTY);
        check(p.getVehicle() == m && m.getControllingPassenger() == p, "her friend rides and steers her");
        p.stopRiding();
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "sphinxAsksAndJudges")
    public static void sphinxAsksAndJudges(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        Centaur s = spawn(helper, GirlSpecies.SPHINX_CENTAUR, new BlockPos(5, 2, 5));
        s.setNoAi(true);
        check(!s.mayCarry(p), "nobody rides a sphinx");
        interact(s, p, ItemStack.EMPTY);
        int which = s.riddleFor(p);
        check(which >= 0, "she asks a riddle");
        interact(s, p, new ItemStack(Centaur.RIDDLE_ANSWERS.get(which)));
        check(s.riddleFor(p) < 0, "answered");
        check(p.getInventory().countItem(Items.EMERALD) + p.getInventory().countItem(Items.GOLD_INGOT) + p.getInventory().countItem(Items.ENCHANTED_BOOK) > 0,
                "the right answer earns a prize");
        int fond = s.getAffection(p);
        interact(s, p, ItemStack.EMPTY);
        interact(s, p, new ItemStack(Items.DIRT));
        check(s.getAffection(p) < fond && s.getAction() == AbstractMonsterGirl.Action.REAR, "a wrong one earns a pounce");   // newly joined players are invulnerable for a moment
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "brownieWorksWhileHerFriendSleeps")
    public static void brownieWorksWhileHerFriendSleeps(GameTestHelper helper) {
        helper.getLevel().setDayTime(1000);                               // day: she works anyway, because her friend is asleep
        var bed = Blocks.RED_BED.defaultBlockState().setValue(net.minecraft.world.level.block.BedBlock.FACING, net.minecraft.core.Direction.NORTH);
        helper.setBlock(new BlockPos(2, 2, 2), bed.setValue(net.minecraft.world.level.block.BedBlock.PART, net.minecraft.world.level.block.state.properties.BedPart.HEAD));
        helper.setBlock(new BlockPos(2, 2, 3), bed.setValue(net.minecraft.world.level.block.BedBlock.PART, net.minecraft.world.level.block.state.properties.BedPart.FOOT));
        helper.setBlock(new BlockPos(5, 2, 2), Blocks.CHEST);
        ChestBlockEntity chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(new BlockPos(5, 2, 2)));
        chest.setItem(0, new ItemStack(Items.COBBLESTONE, 10));
        chest.setItem(5, new ItemStack(Items.COBBLESTONE, 20));
        ServerPlayer friend = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Elf b = spawn(helper, GirlSpecies.BROWNIE_ELF, new BlockPos(8, 2, 8));
        b.setNoAi(true);
        b.addAffection(friend, Orders.AFFECTION);
        friend.startSleeping(helper.absolutePos(new BlockPos(2, 2, 2)));
        helper.succeedWhen(() -> {
            check(chest.getItem(0).getCount() == 30, "she tidies the chest by her sleeping friend");
            friend.stopSleeping();
            HarpyTestUtil.remove(friend);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "brownieTidiesChests")
    public static void brownieTidiesChests(GameTestHelper helper) {
        BlockPos at = new BlockPos(3, 2, 3);
        helper.setBlock(at, Blocks.CHEST);
        ChestBlockEntity chest = (ChestBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(at));
        chest.setItem(0, new ItemStack(Items.COBBLESTONE, 10));
        chest.setItem(5, new ItemStack(Items.COBBLESTONE, 20));
        helper.spawnItem(Items.STICK, 6, 2, 6);
        Elf b = spawn(helper, GirlSpecies.BROWNIE_ELF, new BlockPos(5, 2, 5));
        b.setNoAi(true);
        check(b.tidy(helper.getLevel(), null) >= 2, "she tidies");
        check(chest.getItem(0).getCount() == 30 && chest.getItem(5).isEmpty(), "the cobblestone in one stack");
        check(chest.countItem(Items.STICK) == 1, "and the stick put away");
        helper.succeed();
    }
}
