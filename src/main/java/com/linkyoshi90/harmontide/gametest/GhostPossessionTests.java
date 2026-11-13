package com.linkyoshi90.harmontide.gametest;

import com.linkyoshi90.harmontide.HarmontideMod;
import com.linkyoshi90.harmontide.entity.GhostPossession;
import com.linkyoshi90.harmontide.entity.girl.AbstractMonsterGirl;
import com.linkyoshi90.harmontide.entity.girl.GirlSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.PLATFORM;
import static com.linkyoshi90.harmontide.gametest.HarpyTestUtil.check;
import static com.linkyoshi90.harmontide.gametest.JackalopeTests.spawn;

/** Ghost possession (entity/GhostPossession). */
@GameTestHolder(HarmontideMod.MODID)
@PrefixGameTestTemplate(false)
public class GhostPossessionTests {
    @GameTest(template = PLATFORM, timeoutTicks = 40, batch = "aGhostFriendSlipsIn")
    public static void aGhostFriendSlipsIn(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        AbstractMonsterGirl yurei = spawn(helper, GirlSpecies.YUREI_UNDEAD, new BlockPos(5, 2, 5));
        AbstractMonsterGirl wolf = spawn(helper, GirlSpecies.WOLF_CANID, new BlockPos(9, 2, 5));
        ServerPlayer p = HarpyTestUtil.survivalPlayer(helper, new BlockPos(7, 2, 7));
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(11, 2, 11));
        zombie.setNoAi(true);
        zombie.setTarget(p);
        check(!GhostPossession.possess(level, wolf, p, false), "only ghosts");
        check(GhostPossession.possess(level, yurei, p, false), "she slips in");
        check(GhostPossession.possessed(level, p) && yurei.isInvisible() && yurei.isInvulnerable(), "inside, out of sight");
        check(p.hasEffect(MobEffects.NIGHT_VISION) && zombie.getTarget() == null, "night eyes, and the monster loses track");
        check(!GhostPossession.possess(level, yurei, p, false), "not twice at once");
        GhostPossession.release(level, yurei, p);
        check(!GhostPossession.possessed(level, p) && !yurei.isInvisible() && !yurei.isNoAi(), "she slips out again");
        check(!GhostPossession.possess(level, yurei, p, false), "once a day");
        check(GhostPossession.possess(level, yurei, p, true) && p.hasEffect(MobEffects.CONFUSION), "a prank still works: a hiccup");
        GhostPossession.release(level, yurei, p);
        HarpyTestUtil.remove(p);
        helper.succeed();
    }
}
