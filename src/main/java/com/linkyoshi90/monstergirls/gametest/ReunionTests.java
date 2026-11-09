package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.HearthBlockEntity;
import com.linkyoshi90.monstergirls.entity.Reunions;
import com.linkyoshi90.monstergirls.entity.Sweethearts;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import com.linkyoshi90.monstergirls.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Family reunions (entity/Reunions): a daughter living away comes home with her household, and goes back the next morning. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class ReunionTests {
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "theFamilyComesHome")
    public static void theFamilyComesHome(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos hp = new BlockPos(3, 2, 3);
        helper.setBlock(hp, ModBlocks.HEARTH.get());
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(5, 2, 3));
        ((HearthBlockEntity) helper.getBlockEntity(hp)).setOwner(p.getUUID());
        AbstractMonsterGirl daughter = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(7, 2, 45));
        daughter.setNoAi(true);
        daughter.bornTo(p.getUUID());                                       // theirs (grown here, for the test)
        Villager sweetheart = helper.spawn(EntityType.VILLAGER, new BlockPos(8, 2, 45));
        sweetheart.setNoAi(true);
        check(Sweethearts.court(level, daughter, sweetheart), "she has a sweetheart in the village");
        AbstractMonsterGirl grandchild = daughter.spawnYoung(level, daughter.blockPosition(), p.getUUID());
        check(grandchild != null, "and a little one");
        grandchild.setNoAi(true);
        BlockPos home = helper.absolutePos(hp), away = daughter.blockPosition();
        check(Reunions.isReunionDay(Reunions.DAY * 24000L) && !Reunions.isReunionDay(0), "the 12th of every 24");
        check(Reunions.gather(level, p, home) == 3, "she comes home with her sweetheart and her little one");
        check(daughter.distanceToSqr(home.getCenter()) < 8 * 8 && sweetheart.distanceToSqr(home.getCenter()) < 8 * 8
                && grandchild.distanceToSqr(home.getCenter()) < 8 * 8 && Reunions.visiting(daughter), "all by the hearth");
        check(Reunions.gather(level, p, home) == 0, "those already home don't come twice");
        check(Reunions.celebrate(level, p, home, true) >= 3, "the reunion");
        check(Reunions.goBack(level, daughter) && daughter.distanceToSqr(away.getCenter()) < 2 * 2 && !Reunions.visiting(daughter), "and the next morning she goes back");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
