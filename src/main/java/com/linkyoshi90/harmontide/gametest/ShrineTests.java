package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.OfferingBoxBlock;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.linkyoshi90.harmontide.world.KitsuneShrineStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.EnumSet;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;

/** The kitsune shrine (world/KitsuneShrineStructure, block/OfferingBoxBlock). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class ShrineTests {
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aKitsuneShrine")
    public static void aKitsuneShrine(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(7, 0, 7));
        BlockPos box = KitsuneShrineStructure.build(level, origin, level.random, true);
        check(level.getBlockState(box).is(ModBlocks.OFFERING_BOX.get()), "an offering box in the hall");
        for (int dz : KitsuneShrineStructure.TORII) {
            check(level.getBlockState(origin.offset(-2, 1, dz)).is(Blocks.RED_TERRACOTTA) && level.getBlockState(origin.offset(0, 2, dz)).isAir(), "a torii to walk under at " + dz);
        }
        AbstractMonsterGirl maiden = level.getEntitiesOfClass(AbstractMonsterGirl.class, new AABB(origin).inflate(10)).stream()
                .filter(g -> g.getSpecies() == GirlSpecies.RED_KITSUNE).findFirst().orElse(null);
        check(maiden != null && maiden.hasRestriction(), "a shrine maiden who keeps it");
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 3));
        ItemStack berries = new ItemStack(Items.SWEET_BERRIES, 2);
        p.setItemInHand(InteractionHand.MAIN_HAND, berries);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(box), Direction.UP, box, false);
        level.getBlockState(box).useItemOn(berries, level, p, InteractionHand.MAIN_HAND, hit);
        check(berries.getCount() == 1 && p.getInventory().countItem(Items.PAPER) == 1, "an offering, and a fortune slip");
        level.getBlockState(box).useItemOn(berries, level, p, InteractionHand.MAIN_HAND, hit);
        check(berries.getCount() == 1, "once a day");
        OfferingBoxBlock.draw(level, box, p, OfferingBoxBlock.Fortune.GREAT_BLESSING);
        check(p.hasEffect(MobEffects.LUCK), "a great blessing brings luck");
        EnumSet<OfferingBoxBlock.Fortune> seen = EnumSet.noneOf(OfferingBoxBlock.Fortune.class);
        for (int i = 0; i < 300; i++) seen.add(OfferingBoxBlock.roll(level.random));
        check(seen.size() == 4, "every fortune turns up");
        HarpyTestUtil.remove(p);
        level.getEntitiesOfClass(Mob.class, new AABB(origin).inflate(12)).forEach(Mob::discard);
        for (BlockPos q : BlockPos.betweenClosed(origin.offset(-5, 0, -8), origin.offset(5, 8, 8))) level.setBlock(q, Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.succeed();
    }
}
