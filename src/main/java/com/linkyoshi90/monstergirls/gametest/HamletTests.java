package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.HearthBlockEntity;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Hamlet;
import com.linkyoshi90.monstergirls.entity.girl.Settlement;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Hamlets and villages (entity/girl/Hamlet): a name, a banner, a name tag, and newcomers who ask to stay. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class HamletTests {
    /** A hearth owned by the player with this many wolf girls settled at it. */
    private static HearthBlockEntity hearth(GameTestHelper helper, ServerPlayer p, int residents) {
        BlockPos hp = new BlockPos(7, 2, 7);
        helper.setBlock(hp, ModBlocks.HEARTH.get());
        HearthBlockEntity h = (HearthBlockEntity) helper.getBlockEntity(hp);
        h.setOwner(p.getUUID());
        for (int i = 0; i < residents; i++) {
            AbstractMonsterGirl g = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(2 + i % 4, 2, 2 + i / 4));
            g.addAffection(p, Orders.AFFECTION);
            check(Settlement.settle(helper.getLevel(), g, h), "she settles");
        }
        return h;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aPlaceHasItsName")
    public static void aPlaceHasItsName(GameTestHelper helper) {
        check(Hamlet.tier(2) == Hamlet.Tier.HEARTH && Hamlet.tier(3) == Hamlet.Tier.HAMLET && Hamlet.tier(6) == Hamlet.Tier.VILLAGE, "three make a hamlet, six a village");
        BlockPos a = new BlockPos(100, 64, -30);
        check(!Hamlet.nameFor(a).isEmpty() && Hamlet.nameFor(a).equals(Hamlet.nameFor(a)), "the same spot, the same name: " + Hamlet.nameFor(a));
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aHamletNamesItselfAndRaisesItsBanner")
    public static void aHamletNamesItselfAndRaisesItsBanner(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(10, 2, 10));
        HearthBlockEntity h = hearth(helper, p, 2);
        Hamlet.morning(level, h);
        check(!h.named(), "two make no hamlet yet");
        AbstractMonsterGirl third = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(11, 2, 2));
        third.addAffection(p, Orders.AFFECTION);
        Settlement.settle(level, third, h);
        Hamlet.morning(level, h);
        check(h.named() && h.placeName().equals(Hamlet.nameFor(h.getBlockPos())), "a hamlet takes its name");
        check(h.bannerRaised(), "and raises its banner");
        boolean found = false;
        for (BlockPos q : BlockPos.betweenClosed(h.getBlockPos().offset(-3, 0, -3), h.getBlockPos().offset(3, 0, 3))) {
            if (level.getBlockEntity(q) instanceof BannerBlockEntity b && !b.getPatterns().layers().isEmpty()) found = true;
        }
        check(found, "a patterned banner beside the hearth");
        helper.runAfterDelay(2, () -> {                                // a hearth counts itself loaded a tick later
            check(Hamlet.describe(h) != null && Hamlet.placeAt(level, h.getBlockPos().east(5)) == h, "anyone walking in knows where they are");
            HarpyTestUtil.remove(p);
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aNameTagRenamesThePlace")
    public static void aNameTagRenamesThePlace(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(10, 2, 10));
        HearthBlockEntity h = hearth(helper, p, 0);
        ItemStack tag = new ItemStack(Items.NAME_TAG);
        tag.set(DataComponents.CUSTOM_NAME, Component.literal("Harmonvale"));
        p.setItemInHand(InteractionHand.MAIN_HAND, tag);
        BlockPos pos = h.getBlockPos();
        level.getBlockState(pos).useItemOn(tag, level, p, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        check("Harmonvale".equals(h.placeName()) && tag.isEmpty(), "the owner names it: " + h.placeName());
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aVillageDrawsANewcomer")
    public static void aVillageDrawsANewcomer(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(10, 2, 10));
        HearthBlockEntity h = hearth(helper, p, Hamlet.VILLAGE);
        h.setPlaceName("Wolfhollow");
        AbstractMonsterGirl girl = Hamlet.newcomer(level, h, p);
        check(girl != null, "someone comes");
        check(h.residentCount() == Hamlet.VILLAGE + 1 && h.isResident(girl.getUUID()), "she settles");
        check(girl.getAffection(p) >= Orders.AFFECTION && !girl.isBaby(), "grown, and their friend enough to stay");
        check(girl.getSpecies().family() != GirlSpecies.Family.CANID, "of a family new to the village: " + girl.getSpecies());
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
