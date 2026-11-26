package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.AnuranSpawnBlock;
import com.linkyoshi90.harmontide.block.GirlEggBlockEntity;
import com.linkyoshi90.harmontide.block.HarpyEggBlock;
import com.linkyoshi90.harmontide.entity.AbstractHarpy;
import com.linkyoshi90.harmontide.entity.Bullywug;
import com.linkyoshi90.harmontide.entity.SparrowHarpy;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.girl.Lamia;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.linkyoshi90.harmontide.registry.ModEntities;
import com.linkyoshi90.harmontide.registry.ModGirls;
import com.linkyoshi90.harmontide.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;

/** Labor poses: a sit before live young, a crouch before a spider's sac, one egg at a time after a nest. A loose harpy egg is the same push with no nest. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class BirthTests {
    @SuppressWarnings("unchecked")
    private static <T extends AbstractMonsterGirl> T spawn(GameTestHelper helper, GirlSpecies species, BlockPos pos) {
        return (T) helper.spawn(ModGirls.type(species), pos);
    }

    private static List<AbstractMonsterGirl> babies(GameTestHelper helper, BlockPos pos, net.minecraft.world.entity.EntityType<?> type) {
        return helper.getLevel().getEntitiesOfClass(AbstractMonsterGirl.class, new AABB(helper.absolutePos(pos)).inflate(8),
                g -> g.isBaby() && g.getType() == type);
    }

    private static int clutchCount(ServerLevel level, BlockPos origin) {
        int best = 0;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-8, -3, -8), origin.offset(8, 3, 8))) {
            if (level.getBlockState(pos).is(ModBlocks.GIRL_EGG.get()) && level.getBlockEntity(pos) instanceof GirlEggBlockEntity be) {
                best = Math.max(best, be.getCount());
            }
        }
        return best;
    }

    private static void pool(GameTestHelper helper, int cx, int cz) {
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) helper.setBlock(new BlockPos(cx + dx, 2, cz + dz), Blocks.WATER);
    }

    @GameTest(template = PLATFORM, timeoutTicks = 160, batch = "liveBirthSitsBeforeTheBaby")
    public static void liveBirthSitsBeforeTheBaby(GameTestHelper helper) {
        AbstractMonsterGirl cat = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(7, 2, 7));
        cat.getGestation().conceive(null);
        cat.getGestation().setTicksLeft(0);
        int[] sawLabor = {0};
        helper.succeedWhen(() -> {
            boolean babyOut = !babies(helper, new BlockPos(7, 2, 7), cat.getType()).isEmpty();
            if (cat.getAction() == AbstractMonsterGirl.Action.LABOR && cat.getGestation().isCarrying() && !babyOut) sawLabor[0] = 1;
            List<AbstractMonsterGirl> kittens = babies(helper, new BlockPos(7, 2, 7), cat.getType());
            check(!cat.getGestation().isCarrying() && !kittens.isEmpty() && kittens.stream().allMatch(AbstractMonsterGirl::isBaby), "the kittens are out, and they are babies");
            check(sawLabor[0] == 1, "she sat the labor before they were born");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "liveBirthUsesABedWhenOneIsClose")
    public static void liveBirthUsesABedWhenOneIsClose(GameTestHelper helper) {
        BlockPos head = new BlockPos(10, 2, 8);
        helper.setBlock(new BlockPos(9, 2, 8), Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.EAST).setValue(BedBlock.PART, BedPart.FOOT));
        helper.setBlock(head, Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.EAST).setValue(BedBlock.PART, BedPart.HEAD));
        AbstractMonsterGirl cat = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(5, 2, 8));
        cat.getGestation().conceive(null);
        cat.getGestation().setTicksLeft(0);
        Vec3 bed = Vec3.atCenterOf(helper.absolutePos(head));
        int[] onBed = {0};
        helper.succeedWhen(() -> {
            if (cat.getAction() == AbstractMonsterGirl.Action.LABOR && cat.distanceToSqr(bed) < 6.0) onBed[0] = 1;
            check(!cat.getGestation().isCarrying() && !babies(helper, new BlockPos(8, 2, 8), cat.getType()).isEmpty(), "she gave birth");
            check(onBed[0] == 1, "she labored on the bed");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 160, batch = "spiderCrouchesThenLeavesASac")
    public static void spiderCrouchesThenLeavesASac(GameTestHelper helper) {
        AbstractMonsterGirl weaver = spawn(helper, GirlSpecies.ORB_WEAVER_ARACHNE, new BlockPos(7, 2, 7));
        weaver.getGestation().conceive(null);
        weaver.getGestation().setTicksLeft(0);
        int[] saw = {0};
        helper.succeedWhen(() -> {
            boolean sac = false;
            for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(2, 1, 2), new BlockPos(12, 4, 12))) {
                if (helper.getBlockState(pos).is(ModBlocks.GIRL_EGG.get())) sac = true;
            }
            if (weaver.getAction() == AbstractMonsterGirl.Action.LABOR && weaver.getGestation().isCarrying() && !sac) saw[0] = 1;
            check(sac && !weaver.getGestation().isCarrying(), "the sac is on the floor");
            check(saw[0] == 1, "she crouched before it was out");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "pythonPushesEggsOneByOne")
    public static void pythonPushesEggsOneByOne(GameTestHelper helper) {
        Lamia python = spawn(helper, GirlSpecies.PYTHON_LAMIA, new BlockPos(7, 2, 7));
        python.getGestation().conceive(null);
        python.getGestation().setTicksLeft(0);
        int[] phase = {0};
        helper.succeedWhen(() -> {
            int count = clutchCount(helper.getLevel(), python.blockPosition());
            if (phase[0] == 0 && count == 0 && python.getGestation().isCarrying()) phase[0] = 1;
            if (phase[0] == 1 && count == 1 && python.getAction() == AbstractMonsterGirl.Action.LABOR && python.getGestation().isCarrying()) phase[0] = 2;
            if (phase[0] == 2 && count >= 2) phase[0] = 3;
            check(phase[0] == 3, "one egg, then another (phase " + phase[0] + ", count " + count + ")");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 500, batch = "harpyFillsTheNestOneEggAtATime")
    public static void harpyFillsTheNestOneEggAtATime(GameTestHelper helper) {
        SparrowHarpy sparrow = helper.spawn(ModEntities.SPARROW_HARPY.get(), new BlockPos(7, 2, 7));
        sparrow.getGestation().conceive(null);
        sparrow.getGestation().setTicksLeft(0);
        int[] sawOne = {0};
        helper.succeedWhen(() -> {
            BlockPos nest = null;
            int eggs = 0;
            for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(1, 2, 1), new BlockPos(13, 4, 13))) {
                if (!helper.getBlockState(pos).is(ModBlocks.HARPY_EGG.get())) continue;
                nest = pos.immutable();
                eggs = helper.getBlockState(pos).getValue(HarpyEggBlock.EGGS);
            }
            if (eggs == 1 && sparrow.getGestation().isCarrying() && sparrow.getAction() == com.linkyoshi90.harmontide.entity.AbstractHarpy.Action.LAYING) sawOne[0] = 1;
            check(nest != null && eggs >= 2 && eggs <= 4, "a full clutch: " + eggs);
            check(!sparrow.getGestation().isCarrying(), "she is done");
            check(sawOne[0] == 1, "the first egg was alone while she was still laying");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "frogPushesThenLaysSpawn")
    public static void frogPushesThenLaysSpawn(GameTestHelper helper) {
        pool(helper, 7, 7);
        Bullywug frog = helper.spawn(ModEntities.BULLYWUG.get(), new BlockPos(7, 3, 7));
        frog.getGestation().conceive();
        frog.getGestation().setTicksLeft(0);
        int[] saw = {0};
        helper.succeedWhen(() -> {
            if (frog.getAction() == com.linkyoshi90.harmontide.entity.AbstractAnuran.Action.LAYING && frog.getGestation().isCarrying()) saw[0] = 1;
            boolean spawn = false;
            for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(5, 2, 5), new BlockPos(9, 4, 9))) {
                if (helper.getBlockState(pos).is(ModBlocks.ANURAN_SPAWN.get())
                        && helper.getBlockState(pos).getValue(AnuranSpawnBlock.SPECIES) == AnuranSpawnBlock.Species.BULLYWUG) spawn = true;
            }
            check(spawn && !frog.getGestation().isCarrying(), "the spawn is on the water");
            check(saw[0] == 1, "she lowered herself before it was out");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "looseHarpyEggIsAPushWithoutANest")
    public static void looseHarpyEggIsAPushWithoutANest(GameTestHelper helper) {
        SparrowHarpy sparrow = helper.spawn(ModEntities.SPARROW_HARPY.get(), new BlockPos(7, 2, 7));
        sparrow.setEggTime(1);
        int[] saw = {0};
        helper.succeedWhen(() -> {
            if (sparrow.getAction() == AbstractHarpy.Action.LAYING) saw[0] = 1;
            boolean item = !helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(new BlockPos(7, 2, 7))).inflate(8),
                    e -> e.getItem().is(ModItems.SPARROW_EGG.get())).isEmpty();
            check(item, "the loose egg is on the ground");
            check(!nestNear(helper), "no nest was built");
            check(saw[0] == 1, "she lay prone and pushed it");
            check(sparrow.getAction() != AbstractHarpy.Action.LAYING, "the push is over");
            check(!sparrow.getGestation().isCarrying(), "it was not a fertilised clutch");
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 600, batch = "looseHarpyEggGoesInTheHopper")
    public static void looseHarpyEggGoesInTheHopper(GameTestHelper helper) {
        BlockPos hopper = new BlockPos(7, 2, 7);
        helper.setBlock(hopper, Blocks.HOPPER);
        SparrowHarpy sparrow = helper.spawn(ModEntities.SPARROW_HARPY.get(), new BlockPos(4, 2, 4));
        boolean[] due = {false};
        int[] saw = {0};
        helper.onEachTick(() -> {
            if (sparrow.getAction() == AbstractHarpy.Action.LAYING) saw[0] = 1;
            if (sparrow.isRoosting() && !due[0]) {
                sparrow.setEggTime(1);
                due[0] = true;
            }
        });
        helper.succeedWhen(() -> {
            check(helper.getBlockEntity(hopper) instanceof HopperBlockEntity d && d.countItem(ModItems.SPARROW_EGG.get()) == 1, "the loose egg is in the hopper");
            check(!nestNear(helper), "no nest was built");
            check(saw[0] == 1, "she lay prone on the roost before it dropped");
        });
    }

    private static boolean nestNear(GameTestHelper helper) {
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(1, 1, 1), new BlockPos(13, 4, 13))) {
            if (helper.getBlockState(pos).is(ModBlocks.HARPY_EGG.get())) return true;
        }
        return false;
    }
}
