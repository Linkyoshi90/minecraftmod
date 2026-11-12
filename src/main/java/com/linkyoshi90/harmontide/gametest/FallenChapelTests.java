package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.HomeLife;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.entity.order.FallenChapels;
import com.linkyoshi90.harmontide.entity.order.Order;
import com.linkyoshi90.harmontide.entity.order.OrderSister;
import com.linkyoshi90.harmontide.registry.ModBlocks;
import com.linkyoshi90.harmontide.world.OrderChapelStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Fallen chapels (entity/order/FallenChapels): the sabbath raid on an Order chapel. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class FallenChapelTests {
    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "aChapelFalls")
    public static void aChapelFalls(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(7, 1, 7));
        BlockPos font = OrderChapelStructure.build(level, origin, level.random, true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 1));
        check(!FallenChapels.begin(level, font, p, false, true), "not without the realm's own at your side");
        for (int i = 0; i < FallenChapels.FOLLOWERS; i++) {
            AbstractMonsterGirl g = spawn(helper, GirlSpecies.SUCCUBUS_DEVIL, new BlockPos(5 + 2 * i, 2, 1));
            g.addAffection(p, HomeLife.FRIEND);
        }
        check(!FallenChapels.begin(level, font, p, false, false), "not by day");
        check(FallenChapels.begin(level, font, p, false, true), "by night, with two succubi: the raid begins");
        FallenChapels.Raid raid = FallenChapels.raids().get(font);
        check(raid != null && FallenChapels.standing(level, raid) == raid.defenders().size() && raid.defenders().size() >= 3, "the keeper and two more defend it");
        check(Order.notoriety(p) >= Order.MARKED, "the Order marks the raider");
        check(!FallenChapels.tick(level, font, raid, true), "still going while they stand");
        for (UUID id : raid.defenders()) if (level.getEntity(id) instanceof OrderSister s) s.kneel(null);
        check(FallenChapels.tick(level, font, raid, true), "every one kneels: it is over");
        check(level.getBlockState(font).is(ModBlocks.REALM_CENSER.get()), "the font is a realm censer now");
        check(level.getBlockState(origin.offset(OrderChapelStructure.HALF_W, 2, 0)).is(ModBlocks.REALM_PLANKS.get()), "the walls realm wood");
        FallenChapels.raids().remove(font);
        HarpyTestUtil.remove(p);
        level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, new AABB(origin).inflate(16)).forEach(net.minecraft.world.entity.Mob::discard);
        for (BlockPos q : BlockPos.betweenClosed(origin.offset(-7, 0, -7), origin.offset(7, 7, 7))) level.setBlock(q, Blocks.AIR.defaultBlockState(), 2 | 16);
        helper.succeed();
    }
}
