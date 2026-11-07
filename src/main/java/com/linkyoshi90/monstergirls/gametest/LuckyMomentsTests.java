package com.linkyoshi90.monstergirls.gametest;

import com.linkyoshi90.monstergirls.MonsterGirlsMod;
import com.linkyoshi90.monstergirls.entity.LuckyMoments;
import com.linkyoshi90.monstergirls.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.monstergirls.entity.girl.GirlSpecies;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.monstergirls.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.monstergirls.gametest.JackalopeTests.spawn;

/** Lucky moments (entity/LuckyMoments): caught peeking at a bath, and friends who trip into you. */
@GameTestHolder(MonsterGirlsMod.MODID)
@PrefixGameTestTemplate(false)
public class LuckyMomentsTests {
    private static AbstractMonsterGirl bather(GameTestHelper helper) {
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(7, 2, 7));
        girl.setNoAi(true);
        girl.towelFor(400);
        return girl;
    }

    private static ServerPlayer watcher(GameTestHelper helper, AbstractMonsterGirl girl) {
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 2));
        p.lookAt(EntityAnchorArgument.Anchor.EYES, girl.position().add(0, girl.getBbHeight() * 0.6, 0));
        return p;
    }

    /** Watches her for as long as it takes to be noticed; what happened then. */
    private static int watch(ServerLevel level, ServerPlayer p) {
        int result = LuckyMoments.NOTHING;
        for (int i = 0; i < LuckyMoments.PEEK_TICKS / LuckyMoments.PEEK_EVERY; i++) {
            check(result == LuckyMoments.NOTHING, "noticed too soon");
            result = LuckyMoments.peek(level, p);
        }
        return result;
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "peekingStrangerSplashes")
    public static void peekingStrangerSplashes(GameTestHelper helper) {
        AbstractMonsterGirl girl = bather(helper);
        ServerPlayer p = watcher(helper, girl);
        check(LuckyMoments.bathing(girl), "in her towel she is bathing");
        check(watch(helper.getLevel(), p) == LuckyMoments.SPLASHED, "a stranger splashes them");
        check(p.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && girl.hasFlag(AbstractMonsterGirl.FLAG_BLUSHING), "dripping, and she is red");
        check(watch(helper.getLevel(), p) == LuckyMoments.NOTHING, "not twice in a row");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "peekingFriendTeases")
    public static void peekingFriendTeases(GameTestHelper helper) {
        AbstractMonsterGirl girl = bather(helper);
        ServerPlayer p = watcher(helper, girl);
        girl.addAffection(p, 60);
        check(watch(helper.getLevel(), p) == LuckyMoments.TEASED, "a close friend teases");
        check(!p.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && girl.getAffection(p) == 60, "no splash, nothing lost");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "noPeekWithoutBath")
    public static void noPeekWithoutBath(GameTestHelper helper) {
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(7, 2, 7));
        girl.setNoAi(true);
        ServerPlayer p = watcher(helper, girl);
        check(!LuckyMoments.bathing(girl) && watch(helper.getLevel(), p) == LuckyMoments.NOTHING, "dressed: nothing to see");
        girl.towelFor(400);
        p.lookAt(EntityAnchorArgument.Anchor.EYES, girl.position().add(8, 0, 0));
        check(watch(helper.getLevel(), p) == LuckyMoments.NOTHING, "looking away");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "friendsTripIntoYou")
    public static void friendsTripIntoYou(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl friend = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(3, 2, 3)), close = spawn(helper, GirlSpecies.SERVAL_NEKOMATA, new BlockPos(5, 2, 3));
        friend.setNoAi(true);
        close.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(4, 2, 3));
        check(!LuckyMoments.trip(level, friend, p), "a stranger keeps her distance");
        friend.addAffection(p, 10);
        close.addAffection(p, 60);
        check(LuckyMoments.trip(level, friend, p) && p.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "a friend trips into them");
        check(friend.getAffection(p) == 10 && friend.getAction() == AbstractMonsterGirl.Action.SITTING, "she ends up on the ground");
        check(LuckyMoments.trip(level, close, p) && close.getAffection(p) == 61, "a close friend doesn't mind");
        check(LuckyMoments.clumsy(spawn(helper, firstOf(GirlSpecies.Family.LAMIA), new BlockPos(10, 2, 10)))
                && !LuckyMoments.clumsy(friend), "lamias stumble more than cats");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    private static GirlSpecies firstOf(GirlSpecies.Family family) {
        for (GirlSpecies s : GirlSpecies.values()) if (s.family() == family) return s;
        throw new IllegalStateException(family.name());
    }
}
