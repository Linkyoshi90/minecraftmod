package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Sabbath;
import com.linkyoshi90.harmontide.entity.order.Order;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Cat;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;

/** The Sabbath (entity/Sabbath). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class SabbathTests {
    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "sabbathStandingMirrorsTheOrder")
    public static void sabbathStandingMirrorsTheOrder(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 7));
        check(Sabbath.rank(p) == Sabbath.Rank.OUTSIDER, "an outsider at first");
        Order.addNotoriety(p, 12);
        check(Sabbath.standing(p) == 12 && Sabbath.rank(p) == Sabbath.Rank.INITIATE, "what the Order holds against you, the Sabbath counts for you");
        Order.addNotoriety(p, -10);
        check(Sabbath.standing(p) == 7 && Sabbath.rank(p) == Sabbath.Rank.OUTSIDER, "the Order's comforts cost half as much");
        check(Sabbath.danced(helper.getLevel(), p) && !Sabbath.danced(helper.getLevel(), p) && Sabbath.standing(p) == 7 + Sabbath.DANCE, "one dance a night");
        Sabbath.setStanding(p, 500);
        check(Sabbath.standing(p) == Sabbath.MAX && Sabbath.rank(p) == Sabbath.Rank.SIBLING, "capped at the top");
        check(p.getDisplayName().getString().contains(",") || p.getDisplayName().getString().contains("Sabbath"), "the title after their name: " + p.getDisplayName().getString());
        check(!Sabbath.offers().isEmpty(), "a witch's table");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 20, batch = "aKeeperGetsAFamiliar")
    public static void aKeeperGetsAFamiliar(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 7));
        Sabbath.setStanding(p, Sabbath.Rank.KEEPER.at);
        Cat cat = Sabbath.familiarOf(level, p);
        check(cat != null && cat.isTame() && cat.isOwnedBy(p) && Sabbath.isFamiliar(cat, p), "a familiar of their own");
        check(Sabbath.familiar(level, p) == null, "only one at a time");
        cat.discard();
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
