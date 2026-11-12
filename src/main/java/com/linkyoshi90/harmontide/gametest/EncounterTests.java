package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.Encounters;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Encounters (entity.Encounters): cut free of a web, saved from her chasers, led in from the rain. */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class EncounterTests {
    private static boolean criterion(ServerPlayer p, String c) {
        var a = p.server.getAdvancements().get(HarmontideMod.id(Encounters.EVERY));
        return a != null && p.getAdvancements().getOrStartProgress(a).getCriterion(c).isDone();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "cutFreeOfAWeb")
    public static void cutFreeOfAWeb(GameTestHelper helper) {
        BlockPos at = new BlockPos(2, 2, 2);
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, at);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        var level = helper.getLevel();
        check(Encounters.start(level, girl, Encounters.Kind.WEBBED) && helper.getBlockState(at).is(Blocks.COBWEB), "caught in a web");
        Encounters.update(level, girl);
        check(Encounters.active(girl) && girl.getAffection(p) == 0, "still caught");
        for (BlockPos pos : Encounters.webs(level, girl)) level.destroyBlock(pos, false);
        Encounters.update(level, girl);
        check(!Encounters.active(girl) && girl.getAffection(p) == Encounters.REWARD && criterion(p, "webbed"), "cut free, and grateful");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "savedFromHerChasers")
    public static void savedFromHerChasers(GameTestHelper helper) {
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.HORSE_CENTAUR, new BlockPos(2, 2, 2));
        girl.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        var level = helper.getLevel();
        check(Encounters.start(level, girl, Encounters.Kind.CHASED), "she is chased");
        var chasers = Encounters.chasers(level, girl);
        check(chasers.size() >= 2 && chasers.stream().allMatch(z -> z.getTarget() == girl), "zombies after her");
        Encounters.update(level, girl);
        check(Encounters.active(girl), "while they live it goes on");
        chasers.forEach(Mob::discard);
        Encounters.update(level, girl);
        check(!Encounters.active(girl) && girl.getAffection(p) == Encounters.REWARD && criterion(p, "chased"), "saved");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }

    @GameTest(template = PLATFORM, timeoutTicks = 60, batch = "ledInFromTheRain")
    public static void ledInFromTheRain(GameTestHelper helper) {
        AbstractMonsterGirl girl = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(2, 2, 2));
        girl.setNoAi(true);
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(3, 2, 3));
        var level = helper.getLevel();
        for (int y = 3; y < 3 + Encounters.ROOF + 3; y++) helper.setBlock(new BlockPos(2, y, 2), Blocks.AIR);   // nothing overhead
        check(!Encounters.sheltered(level, girl), "nothing over her: " + girl.blockPosition() + " " + girl.getBbHeight());
        check(!Encounters.start(level, girl, Encounters.Kind.LOST) || level.isRaining(), "lost only in the rain");
        girl.getPersistentData().putString("Encounter", "LOST");
        girl.getPersistentData().putLong("EncounterUntil", level.getGameTime() + Encounters.LIMIT);
        Encounters.update(level, girl);
        check(Encounters.active(girl), "out in the open");
        helper.setBlock(new BlockPos(2, 2 + (int) Math.ceil(girl.getBbHeight()) + 1, 2), Blocks.STONE);
        Encounters.update(level, girl);
        check(!Encounters.active(girl) && girl.getAffection(p) == Encounters.REWARD && criterion(p, "lost"), "under a roof, and grateful");
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
