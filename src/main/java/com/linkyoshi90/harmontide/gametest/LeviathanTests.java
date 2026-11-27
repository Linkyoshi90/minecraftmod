package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.leviathan.DreamCalf;
import com.linkyoshi90.harmontide.leviathan.DreamHeart;
import com.linkyoshi90.harmontide.leviathan.DreamLeviathan;
import com.linkyoshi90.harmontide.leviathan.Interior;
import com.linkyoshi90.harmontide.leviathan.LeviathanBlocks;
import com.linkyoshi90.harmontide.leviathan.LeviathanEvent;
import com.linkyoshi90.harmontide.leviathan.LeviathanRegistry;
import com.linkyoshi90.harmontide.leviathan.Parasites;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.EnumSet;
import java.util.Set;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;

/** The Dream-Leviathan event (leviathan/*): her insides, the blessing bar, her calf, her parasites, her landing and death. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class LeviathanTests {
    /** Visits used by these tests, far past any a world would reach. */
    private static final int RUN_A = 5000, RUN_B = 5001;

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "leviathanInsidesAreLaidOutAndBuilt")
    public static void leviathanInsidesAreLaidOutAndBuilt(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        Interior.forget(server, RUN_A);
        check(server.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DIMENSION_TYPE)
                .containsKey(LeviathanRegistry.INTERIOR.location()), "her insides' dimension type loaded");
        ServerLevel inside = Interior.level(server) != null ? Interior.level(server) : server.overworld();   // the test server makes no mod dimensions
        BlockPos spawn = Interior.entrySpot(server, RUN_A);
        check(inside.getBlockState(spawn).isAir() && inside.getBlockState(spawn.above()).isAir(), "room to stand at the entry spot");
        check(!inside.getBlockState(spawn.below()).isAir(), "floor under the entry spot");
        Interior.RunState rs = Interior.get(server).run(RUN_A);
        check(rs != null, "visit recorded");
        Interior.Room heart = Interior.currentRoom(server, RUN_A);
        check(heart != null && heart.kind == Interior.Kind.ENTRY, "the first room is the entry");
        Set<Interior.Kind> puzzles = EnumSet.noneOf(Interior.Kind.class);
        int waves = 0;
        Interior.Layout layout = Interior.get(server).run(RUN_A).layoutForTests();
        for (Interior.Room r : layout.chain) {
            if (r.kind == Interior.Kind.WAVE) waves++;
            else if (r.kind != Interior.Kind.ENTRY && r.kind != Interior.Kind.HEART) puzzles.add(r.kind);
        }
        check(waves == 5, "five waves, got " + waves);
        check(puzzles.size() == 5, "each puzzle once, got " + puzzles);
        check(layout.chain.get(layout.chain.size() - 1).kind == Interior.Kind.HEART, "the heart chamber last");
        boolean heartBlock = false;
        Interior.Room last = layout.chain.get(layout.chain.size() - 1);
        for (BlockPos p : BlockPos.betweenClosed(last.centre.offset(-3, 0, -3), last.centre.offset(3, 7, 3))) {
            heartBlock |= inside.getBlockState(p).is(LeviathanRegistry.HEART.get());
        }
        check(heartBlock, "her heart hangs in the last room");
        int shut = 0;
        for (Interior.Room r : layout.chain) {
            for (BlockPos p : BlockPos.betweenClosed(r.centre.offset(-18, 0, -18), r.centre.offset(18, 0, 18))) {
                BlockState s = inside.getBlockState(p);
                if (s.is(LeviathanRegistry.MEMBRANE.get()) && !s.getValue(LeviathanBlocks.OPEN)) shut++;
            }
        }
        check(shut > 0, "the rooms' ways on start shut");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 100, batch = "skippingRoomsOpensTheirMembranes")
    public static void skippingRoomsOpensTheirMembranes(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        Interior.forget(server, RUN_B);
        Interior.entrySpot(server, RUN_B);
        Interior.skip(server, RUN_B, 3);
        check(Interior.isDone(server, RUN_B, 0) && Interior.isDone(server, RUN_B, 1) && Interior.isDone(server, RUN_B, 2), "three rooms done");
        Interior.Room now = Interior.currentRoom(server, RUN_B);
        check(now != null && now.index == 3, "the fourth room is next");
        Interior.skip(server, RUN_B, 20);
        Interior.Room heart = Interior.currentRoom(server, RUN_B);
        check(heart != null && heart.kind == Interior.Kind.HEART, "skipping never passes the heart chamber");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "blessingBarTakesTheHurtFirst")
    public static void blessingBarTakesTheHurtFirst(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        DreamHeart.unbless(p);
        check(DreamHeart.bless(p), "first blessing gives");
        check(DreamHeart.blessed(p) && DreamHeart.pool(p) == DreamHeart.MAX, "a full second bar");
        int eggs = p.getInventory().countItem(LeviathanRegistry.DREAM_EGG.get());
        check(eggs == 1, "her egg given, got " + eggs);
        check(!DreamHeart.bless(p), "a second healing gives nothing more");
        check(p.getInventory().countItem(LeviathanRegistry.DREAM_EGG.get()) == 1, "still one egg");
        var hit = helper.getLevel().damageSources().generic();
        check(DreamHeart.absorb(p, hit, 6.0F) == 0, "the bar takes all of a small hit");
        check(DreamHeart.pool(p) == DreamHeart.MAX - 6, "bar took it: " + DreamHeart.pool(p));
        DreamHeart.setPool(p, 2);
        check(Math.abs(DreamHeart.absorb(p, hit, 5.0F) - 3.0F) < 0.001, "the rest goes to health");
        check(DreamHeart.pool(p) == 0, "the bar is empty");
        check(DreamHeart.absorb(p, helper.getLevel().damageSources().fellOutOfWorld(), 4.0F) == 4.0F, "the void goes straight through");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 300, batch = "calfWrapsHerOwnerInAbsorption")
    public static void calfWrapsHerOwnerInAbsorption(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        DreamCalf calf = helper.spawn(LeviathanRegistry.DREAM_CALF.get(), new BlockPos(4, 3, 4));
        calf.setOwner(p.getUUID());
        calf.setStaying(true);
        calf.setGrowth(0);
        helper.runAfterDelay(3, () -> {
            check(calf.getBbWidth() < 0.6F, "a hatchling is small: " + calf.getBbWidth());
            calf.setGrowth(1);
        });
        helper.runAfterDelay(6, () -> check(calf.getBbWidth() > 1.3F && calf.getBbWidth() < 1.5F, "full grown is horse sized: " + calf.getBbWidth()));
        helper.succeedWhen(() -> {
            check(helper.getTick() > 8, "sizes checked");
            check(p.hasEffect(MobEffects.ABSORPTION), "Absorption from the calf");
            HarpyTestUtil.remove(p);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "killedCalfCurlsBackIntoHerEgg")
    public static void killedCalfCurlsBackIntoHerEgg(GameTestHelper helper) {
        DreamCalf calf = helper.spawn(LeviathanRegistry.DREAM_CALF.get(), new BlockPos(4, 2, 4));
        calf.kill();
        helper.succeedWhen(() -> helper.assertItemEntityPresent(LeviathanRegistry.DREAM_EGG.get(), new BlockPos(4, 2, 4), 3.0));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "infestedZombieBitePoisons")
    public static void infestedZombieBitePoisons(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        Parasites.InfestedZombie z = helper.spawn(LeviathanRegistry.INFESTED_ZOMBIE.get(), new BlockPos(3, 2, 2));
        helper.runAfterDelay(80, () -> {                                                  // past a new player's spawn protection
            z.doHurtTarget(p);
            check(p.hasEffect(MobEffects.POISON), "the bite poisons");
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 400, batch = "leviathanLandsDiesAndGivesScales")
    public static void leviathanLandsDiesAndGivesScales(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        LeviathanEvent e = LeviathanEvent.get(level);
        e.summonNow(level, p, true);
        check(e.phase == LeviathanEvent.LANDED && e.landing != null, "she landed");
        Vec3 door = e.entrance();
        check(door.distanceTo(new Vec3(e.x, e.y, e.z)) > 5, "her opening is away from her middle");
        helper.runAfterDelay(5, () -> {
            check(e.body(level) != null, "her body is there with a player near");
            e.die(level);
            check(e.phase == LeviathanEvent.DEAD, "she died");
            check(e.harvest(level, p), "a scale comes off");
            check(p.getInventory().countItem(LeviathanRegistry.DREAM_SCALE.get()) == 1, "got a dream scale");
            check(e.bodyMode() == DreamLeviathan.DEAD, "her body lies dead");
            e.reset();
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "dreamHornWantsNightAndSky")
    public static void dreamHornWantsNightAndSky(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        LeviathanEvent e = LeviathanEvent.get(level);
        e.reset();
        String why = e.summon(level, p);
        if (level.isDay()) check("message.harmontide.leviathan.horn_day".equals(why), "by day she doesn't come: " + why);
        else check(why == null || why.endsWith("horn_sky"), "by night she comes (under open sky): " + why);
        e.reset();
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "antibodyCuresACyst")
    public static void antibodyCuresACyst(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        BlockPos at = new BlockPos(3, 2, 3);
        helper.setBlock(at, LeviathanRegistry.CYST.get());
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(LeviathanRegistry.ANTIBODY.get()));
        BlockPos abs = helper.absolutePos(at);
        helper.getLevel().getBlockState(abs).useItemOn(p.getMainHandItem(), helper.getLevel(), p, net.minecraft.world.InteractionHand.MAIN_HAND,
                new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(abs), net.minecraft.core.Direction.UP, abs, false));
        check(helper.getBlockState(at).getValue(LeviathanBlocks.CURED), "the cyst is cured");
        check(p.getMainHandItem().isEmpty(), "the antibody is used up");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
