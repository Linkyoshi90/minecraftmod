package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.HearthBlockEntity;
import com.linkyoshi90.monstergirls.entity.Inspections;
import com.linkyoshi90.monstergirls.entity.Liaison;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import com.linkyoshi90.monstergirls.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Liaison inspections (entity/Inspections): the checks, the pay, the warning, the inspector's visit. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class InspectionTests {
    private static final BlockPos HEARTH = new BlockPos(5, 2, 5);

    private static ServerPlayer host(GameTestHelper helper) {
        helper.setBlock(HEARTH, ModBlocks.HEARTH.get());
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        ((HearthBlockEntity) helper.getBlockEntity(HEARTH)).setOwner(p.getUUID());
        p.getInventory().clearContent();
        return p;
    }

    private static AbstractMonsterGirl resident(GameTestHelper helper, ServerPlayer p, int affection) {
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(7, 2, 5));
        girl.setNoAi(true);
        girl.setAffection(p.getUUID(), affection);
        ((HearthBlockEntity) helper.getBlockEntity(HEARTH)).addResident(girl.getUUID());
        return girl;
    }

    private static void furnish(GameTestHelper helper) {
        helper.setBlock(new BlockPos(9, 2, 8), Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.EAST).setValue(BedBlock.PART, BedPart.FOOT));
        helper.setBlock(new BlockPos(10, 2, 8), Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.EAST).setValue(BedBlock.PART, BedPart.HEAD));
        helper.setBlock(new BlockPos(2, 2, 8), Blocks.CHEST);
        ((ChestBlockEntity) helper.getBlockEntity(new BlockPos(2, 2, 8))).setItem(0, new ItemStack(Items.BREAD, 16));
    }

    private static int count(ServerPlayer p, net.minecraft.world.item.Item item) {
        int n = 0;
        for (ItemStack s : p.getInventory().items) if (s.is(item)) n += s.getCount();
        return n;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aModelHome")
    public static void aModelHome(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = host(helper);
        resident(helper, p, 100);
        furnish(helper);
        BlockPos hearth = helper.absolutePos(HEARTH);
        List<Inspections.Check> passed = Inspections.passed(level, hearth, p);
        check(passed.size() == Inspections.Check.values().length, "a bed, food put away, she's well and fond, no monsters: " + passed);
        check(Inspections.inspect(level, null, hearth, p) == Inspections.MODEL, "a perfect score");
        check(count(p, Items.EMERALD) == Inspections.MODEL && count(p, Items.WRITTEN_BOOK) == 1, "an emerald a point, and her report");
        check(p.hasEffect(MobEffects.HERO_OF_THE_VILLAGE), "and the villages hear of it");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aPoorHomeIsWarned")
    public static void aPoorHomeIsWarned(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = host(helper);
        AbstractMonsterGirl girl = resident(helper, p, 0);
        girl.setInjured(true);
        helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 2, 1)).setNoAi(true);
        BlockPos hearth = helper.absolutePos(HEARTH);
        check(Inspections.passed(level, hearth, p).isEmpty(), "no bed, no food, she's hurt and wary, a zombie about");
        check(Inspections.inspect(level, null, hearth, p) == 0, "nothing");
        check(count(p, Items.EMERALD) == 0 && count(p, Items.WRITTEN_BOOK) == 1 && !p.hasEffect(MobEffects.HERO_OF_THE_VILLAGE), "only a warning");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "theInspectorCalls")
    public static void theInspectorCalls(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = host(helper);
        resident(helper, p, 60);
        furnish(helper);
        helper.runAfterDelay(2, () -> {                                     // the hearth counts as loaded from the next tick
            Inspections.setNext(p, 0);
            Inspections.tick(level, p);
            check(Inspections.next(p) == 0, "no permit, no inspections");
            p.getInventory().add(new ItemStack(ModItems.HOMESTAY_PERMIT.get()));
            check(Liaison.hasPermit(p), "a permit");
            Inspections.tick(level, p);
            check(Inspections.next(p) == level.getGameTime() + Inspections.FIRST, "the first is booked a few days ahead: " + Inspections.next(p));
            BlockPos hearth = helper.absolutePos(HEARTH);
            Villager v = Inspections.visit(level, p, hearth);
            check(v != null && Inspections.isInspector(v) && Liaison.isLiaison(v), "a liaison from the office comes");
            v.moveTo(hearth.getX() + 1.5, hearth.getY(), hearth.getZ() + 0.5);
            Inspections.update(level, v);
            check(count(p, Items.WRITTEN_BOOK) == 1 && count(p, Items.EMERALD) == Inspections.MODEL, "at the fire she looks it over and reports");
            Inspections.update(level, v);
            check(count(p, Items.WRITTEN_BOOK) == 1, "once");
            v.getPersistentData().getCompound("Inspector").putLong("Until", 0);
            Inspections.update(level, v);
            check(v.isRemoved(), "then she's gone");
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }
}
