package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.block.GraveGrassBlock;
import com.linkyoshi90.harmontide.entity.DeadRealm;
import com.linkyoshi90.harmontide.entity.Monsterization;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import com.linkyoshi90.harmontide.item.HolyWaterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** The realm of the dead (entity/DeadRealm, block/GraveGrassBlock). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class DeadRealmTests {
    private static BlockPos ground(GameTestHelper helper, int x, int z) {
        return helper.absolutePos(new BlockPos(x, 1, z));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "graveGrassAndItsLimits")
    public static void graveGrassAndItsLimits(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos dark = ground(helper, 3, 3), lit = ground(helper, 11, 11);
        check(GraveGrassBlock.corrupt(level, dark, 0) && GraveGrassBlock.grave(level.getBlockState(dark)), "grass turns to grave grass");
        level.setBlockAndUpdate(lit.above(), Blocks.TORCH.defaultBlockState());
        helper.runAfterDelay(20, () -> {
            check(!GraveGrassBlock.corrupt(level, lit, 0), "never into lamplight");
            check(GraveGrassBlock.corrupt(level, ground(helper, 4, 3), GraveGrassBlock.MAX_DEPTH), "an edge patch");
            check(GraveGrassBlock.spread(level, ground(helper, 4, 3), level.random) == null, "the edge spreads no further");
            check(HolyWaterItem.cleanse(level, dark) >= 2 && level.getBlockState(dark).is(Blocks.GRASS_BLOCK), "holy water cleanses it");
            helper.succeed();
        });
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "aLichSeedsIt")
    public static void aLichSeedsIt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl lich = spawn(helper, GirlSpecies.LICH_UNDEAD, new BlockPos(7, 2, 7));
        lich.setNoAi(true);
        check(DeadRealm.seeds(lich), "a wild lich grows it");
        BlockPos at = null;
        for (int i = 0; i < 20 && at == null; i++) at = DeadRealm.seed(level, lich);
        check(at != null && GraveGrassBlock.grave(level.getBlockState(at)), "the ground near her turns");
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        lich.addAffection(p, 10);
        check(!DeadRealm.seeds(lich), "befriended, she stops");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "theDeadRiseAgain")
    public static void theDeadRiseAgain(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GraveGrassBlock.corrupt(level, ground(helper, 7, 7), 0);
        GraveGrassBlock.corrupt(level, ground(helper, 3, 3), 0);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(7, 2, 7));
        zombie.setNoAi(true);
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(3, 2, 3));
        cow.setNoAi(true);
        check(DeadRealm.inDeadRealm(zombie) && DeadRealm.turnsHere(zombie) && !DeadRealm.turnsHere(cow), "only the undead turn here");
        AbstractMonsterGirl girl = null;
        for (int i = 0; i < Monsterization.STEPS; i++) girl = Monsterization.step(level, zombie, true);
        check(girl != null && girl.getSpecies() == GirlSpecies.ZOMBIE_UNDEAD, "the zombie rises a girl");
        check(Monsterization.step(level, cow, true) == null && Monsterization.soaked(cow) == 0, "a cow on grave grass stays a cow");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "undeadFriendsAreStrongHere")
    public static void undeadFriendsAreStrongHere(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GraveGrassBlock.corrupt(level, ground(helper, 5, 5), 0);
        AbstractMonsterGirl ghoul = spawn(helper, GirlSpecies.GHOUL_UNDEAD, new BlockPos(5, 2, 5));
        ghoul.setNoAi(true);
        check(!DeadRealm.strengthen(ghoul), "a wild one gets nothing");
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(9, 2, 9));
        ghoul.addAffection(p, 10);
        check(DeadRealm.strengthen(ghoul) && ghoul.hasEffect(MobEffects.REGENERATION) && ghoul.hasEffect(MobEffects.DAMAGE_RESISTANCE), "a friend is at her strongest");
        AbstractMonsterGirl risen = DeadRealm.rise(level, ground(helper, 11, 3), true);
        check(DeadRealm.rise(level, ground(helper, 11, 3), false) == null, "never by day");
        check(risen == null || DeadRealm.RISEN.contains(risen.getSpecies()), "one of the dead rises at night: " + risen);
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
