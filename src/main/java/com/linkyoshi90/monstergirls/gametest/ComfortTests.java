package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.HearthBlockEntity;
import com.linkyoshi90.monstergirls.entity.Comfort;
import com.linkyoshi90.monstergirls.entity.Orders;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.entity.girl.Settlement;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Friends looking out for you (entity.Comfort) and the safe hearth (HearthBlockEntity.shelters). */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class ComfortTests {
    @GameTest(template = PLATFORM, timeoutTicks = 200, batch = "aFriendSteadiesYou")
    public static void aFriendSteadiesYou(GameTestHelper helper) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(2, 2, 2));
        AbstractMonsterGirl friend = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(10, 2, 10));
        friend.addAffection(p, Orders.AFFECTION);
        var level = helper.getLevel();
        check(Comfort.check(level, p) == null, "nobody comes while they are well");
        p.setHealth(4);
        Object[] steadied = {null};
        helper.onEachTick(() -> {
            if (steadied[0] == null && helper.getTick() % 5 == 0) steadied[0] = Comfort.check(level, p);
        });
        helper.succeedWhen(() -> {
            check(steadied[0] == friend && p.hasEffect(MobEffects.REGENERATION), "hurt: she comes running and steadies them");
            check(Comfort.check(level, p) == null, "once in a while");
            HarpyTestUtil.remove(p);
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "aLivedInHearthIsSafe")
    public static void aLivedInHearthIsSafe(GameTestHelper helper) {
        BlockPos hp = new BlockPos(1, 2, 1);
        helper.setBlock(hp, ModBlocks.HEARTH.get());
        HearthBlockEntity hearth = (HearthBlockEntity) helper.getBlockEntity(hp);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        hearth.setOwner(p.getUUID());
        var level = helper.getLevel();
        Vec3 near = helper.absoluteVec(new Vec3(8, 2, 8)), far = near.add(40, 0, 0);
        for (int i = 0; i < HearthBlockEntity.SAFE_RESIDENTS; i++) {
            AbstractMonsterGirl g = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(4 + i, 2, 2));
            g.addAffection(p, Orders.AFFECTION);
            check(Settlement.settle(level, g, hearth), "she settles");
        }
        helper.succeedWhen(() -> {                                                 // the hearth counts itself in once it has loaded
            check(HearthBlockEntity.shelters(level, near) && !HearthBlockEntity.shelters(level, far), "safe ground near a lived-in hearth");
            HarpyTestUtil.remove(p);
        });
    }
}
