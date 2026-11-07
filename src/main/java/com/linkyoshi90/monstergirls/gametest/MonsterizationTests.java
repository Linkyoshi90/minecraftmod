package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.block.RealmGrassBlock;
import com.linkyoshi90.monstergirls.entity.Monsterization;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Wolf;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Monsterization (entity/Monsterization): vanilla creatures in the Demon Realm become monster girls. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class MonsterizationTests {
    private static void realmUnder(GameTestHelper helper, int x, int z) {
        RealmGrassBlock.corrupt(helper.getLevel(), helper.absolutePos(new BlockPos(x, 1, z)));
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "cowBecomesHolstaur")
    public static void cowBecomesHolstaur(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        realmUnder(helper, 7, 7);
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(7, 2, 7));
        cow.setNoAi(true);
        cow.setCustomName(Component.literal("Bessie"));
        check(Monsterization.step(level, cow, false) == null && Monsterization.soaked(cow) == 0, "not in daylight");
        AbstractMonsterGirl girl = null;
        for (int i = 0; i < Monsterization.STEPS - 1; i++) girl = Monsterization.step(level, cow, true);
        check(girl == null && Monsterization.soaked(cow) == Monsterization.STEPS - 1 && cow.isAlive(), "still soaking it up");
        girl = Monsterization.step(level, cow, true);
        check(girl != null && girl.getSpecies() == GirlSpecies.CATTLE_MINOTAUR && !cow.isAlive(), "a holstaur now");
        check(girl.hasCustomName() && "Bessie".equals(girl.getCustomName().getString()) && girl.isPersistenceRequired(), "her name kept, and she stays");
        ServerPlayer stranger = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        check(Monsterization.monsterized(girl) && girl.trustsEnoughForBed(stranger) && girl.bedChanceFor(stranger) >= Monsterization.EAGER, "eager for a stranger's bed");
        check(Monsterization.describe(girl) != null, "her bond says what happened to her");
        AbstractMonsterGirl born = spawn(helper, GirlSpecies.CATTLE_MINOTAUR, new BlockPos(11, 2, 3));
        check(!Monsterization.monsterized(born) && !born.trustsEnoughForBed(stranger) && born.bedChanceFor(stranger) == 0.0F, "one born a holstaur waits for friendship");
        girl.setFather(stranger.getUUID());
        check(!girl.trustsEnoughForBed(stranger), "never her father");
        HarpyTestUtil.remove(stranger);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "onlyGrownVanillaInTheRealm")
    public static void onlyGrownVanillaInTheRealm(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Cow off = helper.spawn(EntityType.COW, new BlockPos(3, 2, 3));
        off.setNoAi(true);
        check(Monsterization.step(level, off, true) == null && Monsterization.soaked(off) == 0, "no realm under her");
        realmUnder(helper, 10, 10);
        Cow calf = helper.spawn(EntityType.COW, new BlockPos(10, 2, 10));
        calf.setNoAi(true);
        calf.setBaby(true);
        check(Monsterization.kindOf(calf) == null && Monsterization.step(level, calf, true) == null, "a calf stays a calf");
        check(Monsterization.kindOf(helper.spawn(EntityType.WITCH, new BlockPos(5, 2, 10))) == GirlSpecies.SUCCUBUS_DEVIL, "a witch becomes a succubus");
        check(Monsterization.kindOf(helper.spawn(EntityType.CREEPER, new BlockPos(2, 2, 12))) == null, "not everyone has a kind");
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(12, 2, 3));
        check(Monsterization.kindOf(girl) == null, "girls of the mod stay who they are");
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "tamedWolfStaysDevoted")
    public static void tamedWolfStaysDevoted(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 4));
        Wolf wolf = helper.spawn(EntityType.WOLF, new BlockPos(7, 2, 7));
        wolf.setNoAi(true);
        wolf.tame(p);
        AbstractMonsterGirl girl = Monsterization.change(level, wolf, Monsterization.kindOf(wolf));
        check(girl != null && girl.getSpecies() == GirlSpecies.WOLF_CANID, "a wolf girl");
        check(girl.getAffection(p) == Monsterization.PET_AFFECTION, "still devoted to her owner");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
